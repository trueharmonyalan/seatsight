
# import cv2
# import threading
# import logging
# from ultralytics import YOLO

# class SeatTracker:
#     def __init__(self, model_path='best.pt'):
#         """
#         Initialize SeatTracker with settings.
#         Expected model classes: 'empty_seat' (mapped to 'vacant') and 'person_on_seat' (mapped to 'occupied').
#         """
#         self.model_path = model_path
#         self.frame_size = (640, 480)  # Default, will be updated from config
#         self.seat_limit = 10  # Default, will be updated from config
#         self.conf_threshold = 0.5
        
#         self.logger = logging.getLogger("SeatTracker")
#         self.logger.setLevel(logging.INFO)
        
#         # Load the YOLO model
#         try:
#             self.logger.info(f"Initializing SeatTracker with model: {model_path}")
#             self.model = YOLO(model_path)
#             self.lock = threading.Lock()
#             self.seat_status = []
            
#             # Map expected model classes
#             self.expected_classes = {'empty_seat': 'vacant', 'person_on_seat': 'occupied'}
#             self.model_class_ids = {}
#             for class_id, class_name in self.model.names.items():
#                 name_lower = class_name.lower()
#                 if name_lower in self.expected_classes:
#                     self.model_class_ids[class_id] = name_lower
#                     self.logger.info(f"Detected model class: {class_name} (ID: {class_id})")
#             if len(self.model_class_ids) < 2:
#                 self.logger.warning("Model may not contain both 'empty_seat' and 'person_on_seat' classes.")
                
#         except Exception as e:
#             self.logger.error(f"Error loading model: {e}")
#             self.model = None

#     def set_seat_config(self, seats):
#         """
#         Set the seat configuration to track.
        
#         Args:
#             seats: List of seat dictionaries with id, position, etc.
#         """
#         # Update seat limit based on config
#         self.seat_limit = len(seats)
#         self.logger.info(f"Set configuration for {self.seat_limit} seats")
        
#         # Initialize seat status
#         self.seat_status = []
#         for seat in seats:
#             self.seat_status.append({
#                 "seatNumber": seat['seat_number'],
#                 "status": "vacant",
#                 "bbox": None,
#                 "predicted_class": "empty_seat",
#                 "confidence": 0.0,
#                 "id": seat['id'],
#                 "centroid": None
#             })

#     def process_frame(self, frame):
#         """
#         Process a video frame to detect seat occupancy.
        
#         Args:
#             frame: The video frame as a numpy array
            
#         Returns:
#             A copy of the frame with visualizations, and seat occupancy data
#         """
#         if frame is None or self.model is None:
#             return frame, {}

#         try:
#             # Create a copy for visualization
#             vis_frame = frame.copy()
            
#             # Resize frame if needed
#             if frame.shape[1::-1] != self.frame_size:
#                 frame = cv2.resize(frame, self.frame_size, interpolation=cv2.INTER_AREA)
            
#             # Get predictions from the model
#             results = self.model(frame, imgsz=self.frame_size, conf=self.conf_threshold, verbose=False)
            
#             detections_list = []
#             for box in results[0].boxes:
#                 cls_id = int(box.cls)
#                 conf = float(box.conf)
#                 if conf < self.conf_threshold:
#                     continue
#                 if cls_id not in self.model_class_ids:
#                     continue
                
#                 predicted_class = self.model_class_ids[cls_id]
#                 status = self.expected_classes[predicted_class]
#                 x1, y1, x2, y2 = map(int, box.xyxy[0].cpu().numpy())
#                 centroid = ((x1 + x2) // 2, (y1 + y2) // 2)
#                 detections_list.append({
#                     "centroid": centroid,
#                     "bbox": (x1, y1, x2, y2),
#                     "predicted_class": predicted_class,
#                     "status": status,
#                     "confidence": conf
#                 })
            
#             # Sort detections by position (top to bottom, left to right)
#             detections_list.sort(key=lambda d: (d["centroid"][1], d["centroid"][0]))
            
#             # Map detections to seat numbers
#             mapped_seats = []
#             for idx, detection in enumerate(detections_list):
#                 if idx >= self.seat_limit:
#                     break
#                 detection["seatNumber"] = idx + 1
#                 detection["id"] = self.seat_status[idx]["id"] if idx < len(self.seat_status) else idx + 1
#                 mapped_seats.append(detection)
            
#             # Pad with vacant seats if needed
#             while len(mapped_seats) < self.seat_limit:
#                 seat_idx = len(mapped_seats)
#                 mapped_seats.append({
#                     "seatNumber": seat_idx + 1,
#                     "id": self.seat_status[seat_idx]["id"] if seat_idx < len(self.seat_status) else seat_idx + 1,
#                     "status": "vacant",
#                     "bbox": None,
#                     "centroid": None,
#                     "predicted_class": "empty_seat",
#                     "confidence": 0.0
#                 })
            
#             # Update seat status
#             with self.lock:
#                 self.seat_status = mapped_seats
            
#             # Draw visualizations
#             self.draw_visualizations(vis_frame)
            
#             # Display the frame if not in headless mode
#             try:
#                 cv2.imshow("Seat Tracking", vis_frame)
#                 cv2.waitKey(1)
#             except:
#                 pass
            
#             # Convert to format expected by restaurant session
#             seat_status_dict = {}
#             for seat in self.seat_status:
#                 seat_id = seat["id"]
                
#                 # Include position coordinates in the output dictionary
#                 pos_x = None
#                 pos_y = None
#                 if "centroid" in seat and seat["centroid"]:
#                     pos_x, pos_y = seat["centroid"]
                
#                 seat_status_dict[seat_id] = {
#                     'status': 'occupied' if seat["status"] == "occupied" else 'empty',
#                     'confidence': seat["confidence"],
#                     'class': seat["predicted_class"],
#                     'seat_number': seat["seatNumber"],
#                     'pos_x': pos_x,
#                     'pos_y': pos_y,
#                     'last_update': None  # Will be set by the session
#                 }
            
#             return vis_frame, seat_status_dict
            
#         except Exception as e:
#             self.logger.error(f"Error processing frame: {e}")
#             return frame, {}

#     def draw_visualizations(self, frame):
#         """
#         Draw seat bounding boxes and labels on the frame.
#         """
#         try:
#             for seat in self.seat_status:
#                 bbox = seat.get("bbox")
#                 if bbox:
#                     x1, y1, x2, y2 = bbox
#                     color = (0, 0, 255) if seat["status"] == "occupied" else (0, 255, 0)
#                     cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
                    
#                     # Draw centroid point
#                     if "centroid" in seat and seat["centroid"]:
#                         cx, cy = seat["centroid"]
#                         cv2.circle(frame, (cx, cy), 5, color, -1)  # Draw centroid point
                        
#                     label = f"Seat {seat['seatNumber']} : {seat['predicted_class']}"
#                     cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)
                    
#             # Display summary stats
#             total = self.seat_limit
#             occupied = len([s for s in self.seat_status if s["status"] == "occupied"])
#             vacant = total - occupied
#             cv2.putText(frame, f"Occupied: {occupied}", (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)
#             cv2.putText(frame, f"Vacant: {vacant}", (10, 60), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)
        
#         except Exception as e:
#             self.logger.error(f"Error drawing visualizations: {e}")

#     def get_status(self):
#         """
#         Return a thread-safe copy of the current seat status.
#         """
#         with self.lock:
#             return self.seat_status.copy()
            
#     def close(self):
#         """
#         Cleanup operations.
#         """
#         try:
#             cv2.destroyAllWindows()
#         except:
#             pass


import logging
import threading
import time
import sys
from restaurant_session import RestaurantSessionManager
from dynamic_config import DynamicConfigManager

# Set up logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('seatsight_controller.log'),
        logging.StreamHandler(sys.stdout)
    ]
)

logger = logging.getLogger("SeatSightController")

class SeatSightController:
    """
    Main controller for the SeatSight system.
    Coordinates API polling, configuration management, and restaurant sessions.
    """
    
    def __init__(self, base_api_endpoint, db_conn_str, polling_interval=60):
        """
        Initialize the SeatSight controller.
        
        Args:
            base_api_endpoint (str): Base API endpoint for restaurant data
            db_conn_str (str): Database connection string
            polling_interval (int): How often to poll in seconds
        """
        self.base_api_endpoint = base_api_endpoint
        self.db_conn_str = db_conn_str
        self.polling_interval = polling_interval
        self.running = False
        self.restaurant_configs = {}  # Cache restaurant configurations
        self.paused_sessions = set()  # Track paused sessions
        
        # Create component instances
        self.config_manager = DynamicConfigManager(db_conn_str)
        self.session_manager = RestaurantSessionManager(db_conn_str)
    
    def start(self):
        """Start the SeatSight controller."""
        if self.running:
            logger.warning("Controller already running")
            return
        
        logger.info("Starting SeatSight controller")
        self.running = True
        logger.info("SeatSight controller started")
    
    def stop(self):
        """Stop the SeatSight controller and all managed services."""
        if not self.running:
            return
        
        logger.info("Stopping SeatSight controller")
        self.running = False
        
        # Stop all restaurant sessions
        self.session_manager.stop_all_sessions()
        self.paused_sessions.clear()
        
        logger.info("SeatSight controller stopped")

    def register_restaurant_id(self, restaurant_id):
        """
        Register a restaurant ID without starting tracking.
        Just loads and validates the configuration.
        
        Args:
            restaurant_id: The restaurant ID to register
            
        Returns:
            bool: True if registration was successful, False otherwise
        """
        try:
            logger.info(f"Registering restaurant ID: {restaurant_id}")
            
            # Load restaurant configuration
            config = self.config_manager.load_restaurant_config(restaurant_id)
            if not config:
                logger.error(f"Unable to load configuration for restaurant {restaurant_id}")
                return False
            
            # Check if restaurant has a valid camera URL
            if not config.get('IP_CAMERA_URL'):
                logger.error(f"Restaurant {restaurant_id} has no camera URL. Registration failed.")
                return False
            
            # Cache the configuration for future use
            self.restaurant_configs[restaurant_id] = config
            
            # Set as active restaurant in the config manager
            self.config_manager.set_active_restaurant(restaurant_id)
            
            logger.info(f"Successfully registered restaurant ID: {restaurant_id}")
            return True
                
        except Exception as e:
            logger.error(f"Error registering restaurant ID {restaurant_id}: {e}")
            return False
    
    def process_restaurant_id(self, restaurant_id):
        """
        Process a new restaurant ID received from the API.
        Loads configuration and starts tracking session.
        
        Args:
            restaurant_id: The restaurant ID to process
            
        Returns:
            bool: True if processing started successfully, False otherwise
        """
        try:
            logger.info(f"Processing restaurant ID: {restaurant_id}")
            
            # Check if this is a paused session that can be resumed
            if restaurant_id in self.paused_sessions:
                return self.resume_session(restaurant_id)
            
            # Use cached config if available
            config = self.restaurant_configs.get(restaurant_id)
            
            # If not cached, load it
            if not config:
                config = self.config_manager.load_restaurant_config(restaurant_id)
                if not config:
                    logger.error(f"Unable to load configuration for restaurant {restaurant_id}")
                    return False
                
                # Cache the configuration
                self.restaurant_configs[restaurant_id] = config
            
            # Check if restaurant has a valid camera URL
            if not config.get('IP_CAMERA_URL'):
                logger.error(f"Restaurant {restaurant_id} has no camera URL. Skipping.")
                return False
            
            # Set as active restaurant in the config manager
            self.config_manager.set_active_restaurant(restaurant_id)
            
            # Stop any previously running sessions for this restaurant
            if restaurant_id in self.session_manager.get_active_sessions():
                logger.info(f"Stopping existing session for restaurant {restaurant_id}")
                self.session_manager.stop_session(restaurant_id)
            
            # Start a tracking session for this restaurant
            success = self.session_manager.start_session(restaurant_id, config)
            if success:
                logger.info(f"Successfully started tracking for restaurant {restaurant_id}")
                # Make sure it's removed from paused sessions if it was there
                if restaurant_id in self.paused_sessions:
                    self.paused_sessions.remove(restaurant_id)
                return True
            else:
                logger.error(f"Failed to start tracking for restaurant {restaurant_id}")
                return False
                
        except Exception as e:
            logger.error(f"Error processing restaurant ID {restaurant_id}: {e}")
            return False

    def pause_session(self, restaurant_id):
        """
        Pause a tracking session without fully stopping it.
        This keeps resources allocated but suspends processing.
        
        Args:
            restaurant_id: The restaurant ID to pause
            
        Returns:
            bool: True if successfully paused, False otherwise
        """
        try:
            logger.info(f"Pausing tracking session for restaurant {restaurant_id}")
            
            # Check if the restaurant is being tracked
            if restaurant_id not in self.session_manager.get_active_sessions():
                logger.warning(f"Restaurant {restaurant_id} is not being tracked, cannot pause")
                return False
            
            # Call the session manager to pause the session
            success = self.session_manager.pause_session(restaurant_id)
            
            if success:
                logger.info(f"Successfully paused session for restaurant {restaurant_id}")
                # Add to paused sessions set
                self.paused_sessions.add(restaurant_id)
                return True
            else:
                logger.error(f"Failed to pause session for restaurant {restaurant_id}")
                return False
                
        except Exception as e:
            logger.error(f"Error pausing session for restaurant {restaurant_id}: {e}")
            return False

    def resume_session(self, restaurant_id):
        """
        Resume a previously paused tracking session.
        
        Args:
            restaurant_id: The restaurant ID to resume
            
        Returns:
            bool: True if successfully resumed, False otherwise
        """
        try:
            logger.info(f"Resuming tracking session for restaurant {restaurant_id}")
            
            # Check if session exists in active sessions
            active_sessions = self.session_manager.get_active_sessions()
            
            if restaurant_id in active_sessions:
                # Session exists, resume it
                success = self.session_manager.resume_session(restaurant_id)
                
                if success:
                    logger.info(f"Successfully resumed existing session for restaurant {restaurant_id}")
                    # Remove from paused sessions set
                    if restaurant_id in self.paused_sessions:
                        self.paused_sessions.remove(restaurant_id)
                    return True
                else:
                    logger.error(f"Failed to resume existing session for restaurant {restaurant_id}")
                    return False
            
            # Session doesn't exist, check if we have config for it
            elif restaurant_id in self.restaurant_configs:
                # We have config, start a new session
                logger.info(f"Restaurant {restaurant_id} doesn't have an active session, starting fresh")
                # IMPORTANT: Call process_restaurant_id directly instead of resume_session to avoid recursion
                return self.process_restaurant_id(restaurant_id)
            
            else:
                logger.warning(f"No configuration for restaurant {restaurant_id}, cannot resume")
                return False
                    
        except Exception as e:
            logger.error(f"Error resuming session for restaurant {restaurant_id}: {e}")
            return False

    def get_active_sessions(self):
        """
        Get a list of active restaurant session IDs.
        
        Returns:
            list: List of active restaurant IDs
        """
        return self.session_manager.get_active_sessions()
        
    def get_paused_sessions(self):
        """
        Get a list of paused restaurant session IDs.
        
        Returns:
            set: Set of paused restaurant IDs
        """
        return self.paused_sessions