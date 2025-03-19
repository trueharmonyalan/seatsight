import cv2
import threading
import logging
from ultralytics import YOLO

class SeatTracker:
    def __init__(self, model_path='best.pt'):
        """
        Initialize SeatTracker with settings.
        Expected model classes: 'empty_seat' (mapped to 'vacant') and 'person_on_seat' (mapped to 'occupied').
        """
        self.model_path = model_path
        self.frame_size = (640, 480)  # Default, will be updated from config
        self.seat_limit = 10  # Default, will be updated from config
        self.conf_threshold = 0.5
        
        self.logger = logging.getLogger("SeatTracker")
        self.logger.setLevel(logging.INFO)
        
        # Load the YOLO model
        try:
            self.logger.info(f"Initializing SeatTracker with model: {model_path}")
            self.model = YOLO(model_path)
            self.lock = threading.Lock()
            self.seat_status = []
            
            # Map expected model classes
            self.expected_classes = {'empty_seat': 'vacant', 'person_on_seat': 'occupied'}
            self.model_class_ids = {}
            for class_id, class_name in self.model.names.items():
                name_lower = class_name.lower()
                if name_lower in self.expected_classes:
                    self.model_class_ids[class_id] = name_lower
                    self.logger.info(f"Detected model class: {class_name} (ID: {class_id})")
            if len(self.model_class_ids) < 2:
                self.logger.warning("Model may not contain both 'empty_seat' and 'person_on_seat' classes.")
                
        except Exception as e:
            self.logger.error(f"Error loading model: {e}")
            self.model = None

    def set_seat_config(self, seats):
        """
        Set the seat configuration to track.
        
        Args:
            seats: List of seat dictionaries with id, position, etc.
        """
        # Update seat limit based on config
        self.seat_limit = len(seats)
        self.logger.info(f"Set configuration for {self.seat_limit} seats")
        
        # Initialize seat status
        self.seat_status = []
        for seat in seats:
            self.seat_status.append({
                "seatNumber": seat['seat_number'],
                "status": "vacant",
                "bbox": None,
                "predicted_class": "empty_seat",
                "confidence": 0.0,
                "id": seat['id'],
                "centroid": None
            })

    def process_frame(self, frame):
        """
        Process a video frame to detect seat occupancy.
        
        Args:
            frame: The video frame as a numpy array
            
        Returns:
            A copy of the frame with visualizations, and seat occupancy data
        """
        if frame is None or self.model is None:
            return frame, {}

        try:
            # Create a copy for visualization
            vis_frame = frame.copy()
            
            # Resize frame if needed
            if frame.shape[1::-1] != self.frame_size:
                frame = cv2.resize(frame, self.frame_size, interpolation=cv2.INTER_AREA)
            
            # Get predictions from the model
            results = self.model(frame, imgsz=self.frame_size, conf=self.conf_threshold, verbose=False)
            
            detections_list = []
            for box in results[0].boxes:
                cls_id = int(box.cls)
                conf = float(box.conf)
                if conf < self.conf_threshold:
                    continue
                if cls_id not in self.model_class_ids:
                    continue
                
                predicted_class = self.model_class_ids[cls_id]
                status = self.expected_classes[predicted_class]
                x1, y1, x2, y2 = map(int, box.xyxy[0].cpu().numpy())
                centroid = ((x1 + x2) // 2, (y1 + y2) // 2)
                detections_list.append({
                    "centroid": centroid,
                    "bbox": (x1, y1, x2, y2),
                    "predicted_class": predicted_class,
                    "status": status,
                    "confidence": conf
                })
            
            # Sort detections by position (top to bottom, left to right)
            detections_list.sort(key=lambda d: (d["centroid"][1], d["centroid"][0]))
            
            # Map detections to seat numbers
            mapped_seats = []
            for idx, detection in enumerate(detections_list):
                if idx >= self.seat_limit:
                    break
                detection["seatNumber"] = idx + 1
                detection["id"] = self.seat_status[idx]["id"] if idx < len(self.seat_status) else idx + 1
                mapped_seats.append(detection)
            
            # Pad with vacant seats if needed
            while len(mapped_seats) < self.seat_limit:
                seat_idx = len(mapped_seats)
                mapped_seats.append({
                    "seatNumber": seat_idx + 1,
                    "id": self.seat_status[seat_idx]["id"] if seat_idx < len(self.seat_status) else seat_idx + 1,
                    "status": "vacant",
                    "bbox": None,
                    "centroid": None,
                    "predicted_class": "empty_seat",
                    "confidence": 0.0
                })
            
            # Update seat status
            with self.lock:
                self.seat_status = mapped_seats
            
            # Draw visualizations
            self.draw_visualizations(vis_frame)
            
            # Display the frame if not in headless mode
            try:
                cv2.imshow("Seat Tracking", vis_frame)
                cv2.waitKey(1)
            except:
                pass
            
            # Convert to format expected by restaurant session
            seat_status_dict = {}
            for seat in self.seat_status:
                seat_id = seat["id"]
                
                # Include position coordinates in the output dictionary
                pos_x = None
                pos_y = None
                if "centroid" in seat and seat["centroid"]:
                    pos_x, pos_y = seat["centroid"]
                
                seat_status_dict[seat_id] = {
                    'status': 'occupied' if seat["status"] == "occupied" else 'empty',
                    'confidence': seat["confidence"],
                    'class': seat["predicted_class"],
                    'seat_number': seat["seatNumber"],
                    'pos_x': pos_x,
                    'pos_y': pos_y,
                    'last_update': None  # Will be set by the session
                }
            
            return vis_frame, seat_status_dict
            
        except Exception as e:
            self.logger.error(f"Error processing frame: {e}")
            return frame, {}

    def draw_visualizations(self, frame):
        """
        Draw seat bounding boxes and labels on the frame.
        """
        try:
            for seat in self.seat_status:
                bbox = seat.get("bbox")
                if bbox:
                    x1, y1, x2, y2 = bbox
                    color = (0, 0, 255) if seat["status"] == "occupied" else (0, 255, 0)
                    cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
                    
                    # Draw centroid point
                    if "centroid" in seat and seat["centroid"]:
                        cx, cy = seat["centroid"]
                        cv2.circle(frame, (cx, cy), 5, color, -1)  # Draw centroid point
                        
                    label = f"Seat {seat['seatNumber']} : {seat['predicted_class']}"
                    cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)
                    
            # Display summary stats
            total = self.seat_limit
            occupied = len([s for s in self.seat_status if s["status"] == "occupied"])
            vacant = total - occupied
            cv2.putText(frame, f"Occupied: {occupied}", (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)
            cv2.putText(frame, f"Vacant: {vacant}", (10, 60), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)
        
        except Exception as e:
            self.logger.error(f"Error drawing visualizations: {e}")

    def get_status(self):
        """
        Return a thread-safe copy of the current seat status.
        """
        with self.lock:
            return self.seat_status.copy()
            
    def close(self):
        """
        Cleanup operations.
        """
        try:
            cv2.destroyAllWindows()
        except:
            pass