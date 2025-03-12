# //////////////////////////////////////
# version 1 
# import cv2
# import numpy as np
# import time
# import logging
# from ultralytics import YOLO
# import threading

# class SeatTracker:
#     def __init__(self, config):
#         model_path = config.get('MODEL_PATH')
#         self.frame_size = config.get('FRAME_SIZE')
#         self.logger = logging.getLogger(__name__)
#         self.model = YOLO(model_path).to("cpu")
#         self.seat_zones = {}
#         self.lock = threading.Lock()
#         self.next_seat_id = 0

#     def get_centroid(self, box):
#         x1, y1, x2, y2 = box
#         return ((x1 + x2) // 2, (y1 + y2) // 2)

#     def update_detections(self, frame):
#         results = self.model(frame, imgsz=self.frame_size, verbose=False)
#         with self.lock:
#             self.seat_zones = {}  # Reset seat statuses

#             for box in results[0].boxes:
#                 x1, y1, x2, y2 = map(int, box.xyxy[0].cpu().numpy())
#                 centroid = self.get_centroid((x1, y1, x2, y2))

#                 self.seat_zones[self.next_seat_id] = {"position": centroid, "status": "vacant"}
#                 self.next_seat_id += 1

#         self.draw_seat_markers(frame)
#         cv2.imshow("Seat Detection", frame)
#         cv2.waitKey(1)

#     def draw_seat_markers(self, frame):
#         """Marks each seat's center with a colored dot."""
#         for seat_id, seat in self.seat_zones.items():
#             centroid = seat["position"]
#             color = (0, 0, 255) if seat["status"] == "occupied" else (0, 255, 0)
#             cv2.circle(frame, centroid, 10, color, -1)  # Draw filled circle


# ///////////////////////////////
#version 2
# import cv2
# import numpy as np
# import logging
# from ultralytics import YOLO
# import threading

# class SeatTracker:
#     def __init__(self, config):
#         model_path = config.get('MODEL_PATH')
#         self.frame_size = config.get('FRAME_SIZE')
#         self.distance_threshold = config.get('DISTANCE_THRESHOLD', 30)  # Configurable distance threshold
#         self.logger = logging.getLogger(__name__)
#         self.model = YOLO(model_path)  # Load YOLO model (defaults to GPU if available)
#         self.seat_zones = {}  # Store seat positions and statuses
#         self.lock = threading.Lock()
#         self.next_seat_id = 0

#     def get_centroid(self, box):
#         """Find the center of a bounding box."""
#         x1, y1, x2, y2 = box
#         return ((x1 + x2) // 2, (y1 + y2) // 2)

#     def update_detections(self, frame):
#         """Detects both seats and persons, then updates seat statuses."""
#         try:
#             results = self.model(frame, imgsz=self.frame_size, conf=0.5, verbose=False)  # Adjust confidence threshold

#             with self.lock:
#                 self.seat_zones = {}  # Reset seat statuses

#                 # Detect seats
#                 for box in results[0].boxes:
#                     cls_id = int(box.cls)  # Class ID of detected object
#                     label = self.model.names[cls_id].lower().replace(" ", "").replace("_", "")

#                     if "emptyseat" in label:
#                         # Detected an empty seat
#                         x1, y1, x2, y2 = map(int, box.xyxy[0].cpu().numpy())
#                         centroid = self.get_centroid((x1, y1, x2, y2))

#                         self.seat_zones[self.next_seat_id] = {
#                             "bbox": (x1, y1, x2, y2),  # Store bounding box
#                             "position": centroid,
#                             "status": "vacant"
#                         }
#                         self.next_seat_id += 1

#                 # Detect persons
#                 persons = []
#                 for box in results[0].boxes:
#                     cls_id = int(box.cls)
#                     label = self.model.names[cls_id].lower()
#                     if "person" in label:
#                         # Detected a person
#                         x1, y1, x2, y2 = map(int, box.xyxy[0].cpu().numpy())
#                         person_centroid = self.get_centroid((x1, y1, x2, y2))
#                         persons.append(person_centroid)

#                 # Check if any person is sitting on a seat
#                 for seat in self.seat_zones.values():
#                     seat["status"] = "vacant"  # Default: Seat is vacant
#                     for person in persons:
#                         if np.linalg.norm(np.array(seat["position"]) - np.array(person)) < self.distance_threshold:
#                             seat["status"] = "occupied"  # Mark as occupied
#                             break  # No need to check further

#             self.draw_seat_markers(frame)
#             cv2.imshow("Seat Detection", frame)
#             cv2.waitKey(1)

#         except Exception as e:
#             self.logger.error(f"Error in update_detections: {e}")

#     def draw_seat_markers(self, frame):
#         """Draws bounding boxes around seats with colors indicating their status."""
#         for seat_id, seat in self.seat_zones.items():
#             x1, y1, x2, y2 = seat["bbox"]  # Get bounding box coordinates
#             color = (0, 0, 255) if seat["status"] == "occupied" else (0, 255, 0)  # Red for occupied, Green for vacant
#             cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)  # Draw bounding box
#             label = f"Seat {seat_id} ({seat['status']})"
#             cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)  # Add label

#     def close(self):
#         """Release resources and close windows."""
#         cv2.destroyAllWindows()

#version3
# import cv2
# import numpy as np
# import logging
# import time
# from ultralytics import YOLO
# import threading

# class SeatTracker:
#     def __init__(self, config):
#         model_path = config.get('MODEL_PATH')
#         self.frame_size = config.get('FRAME_SIZE')
#         self.distance_threshold = config.get('DISTANCE_THRESHOLD', 30)
#         self.logger = logging.getLogger(__name__)
#         self.model = YOLO(model_path)
#         self.seat_zones = {}  # Stores detected seat clusters
#         self.lock = threading.Lock()
#         self.next_seat_id = 0
#         self.calibrated = False

#     def get_centroid(self, box):
#         x1, y1, x2, y2 = box
#         return ((x1 + x2) // 2, (y1 + y2) // 2)

#     def calibrate(self, frame):
#         """Initial calibration phase to detect and cluster seats."""
#         self.logger.info("Starting calibration... Detecting seats for 10 seconds.")
#         start_time = time.time()
#         while time.time() - start_time < 10:
#             remaining_time = 10 - int(time.time() - start_time)
#             self.logger.info(f"Calibration in progress... {remaining_time} seconds left.")
#             self.update_detections(frame, calibrating=True)
#         self.calibrated = True
#         self.logger.info("Calibration complete. Now tracking seat status changes.")

#     def update_detections(self, frame, calibrating=False):
#         try:
#             results = self.model(frame, imgsz=self.frame_size, conf=0.5, verbose=False)
#             with self.lock:
#                 if calibrating:
#                     self.seat_zones = {}  # Reset clusters during calibration
                
#                 detected_seats = []
#                 persons = []
                
#                 for box in results[0].boxes:
#                     cls_id = int(box.cls)
#                     label = self.model.names[cls_id].lower().replace(" ", "").replace("_", "")

#                     x1, y1, x2, y2 = map(int, box.xyxy[0].cpu().numpy())
#                     centroid = self.get_centroid((x1, y1, x2, y2))
                    
#                     if "emptyseat" in label:
#                         detected_seats.append((centroid, (x1, y1, x2, y2)))
#                     elif "person" in label:
#                         persons.append(centroid)

#                 # Clustering logic
#                 self.cluster_seats(detected_seats)
#                 self.update_seat_status(persons)
            
#             self.draw_seat_markers(frame)
#             cv2.imshow("Seat Detection", frame)
#             cv2.waitKey(1)
#         except Exception as e:
#             self.logger.error(f"Error in update_detections: {e}")

#     def cluster_seats(self, detected_seats):
#         """Groups detected seats into clusters."""
#         self.seat_zones = {}  # Reset clusters
        
#         for centroid, bbox in detected_seats:
#             added = False
#             for cluster_id, cluster in self.seat_zones.items():
#                 if len(cluster) < 4:
#                     cluster.append((centroid, bbox))
#                     added = True
#                     break
#             if not added:
#                 self.seat_zones[self.next_seat_id] = [(centroid, bbox)]
#                 self.next_seat_id += 1
        
#         self.logger.info(f"Clustered into {len(self.seat_zones)} areas.")

#     def update_seat_status(self, persons):
#         """Updates seat statuses based on detected persons."""
#         for cluster_id, cluster in self.seat_zones.items():
#             for i, (centroid, bbox) in enumerate(cluster):
#                 status = "vacant"
#                 for person in persons:
#                     if np.linalg.norm(np.array(centroid) - np.array(person)) < self.distance_threshold:
#                         status = "occupied"
#                         break
#                 self.seat_zones[cluster_id][i] = (centroid, bbox, status)
        
#         self.logger.info("Seat statuses updated.")

#     def draw_seat_markers(self, frame):
#         """Draws bounding boxes around seats with colors indicating their status."""
#         for cluster in self.seat_zones.values():
#             for centroid, bbox, status in cluster:
#                 x1, y1, x2, y2 = bbox
#                 color = (0, 0, 255) if status == "occupied" else (0, 255, 0)
#                 cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
#                 label = f"{status}" 
#                 cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)
    
#     def start_tracking(self, video_source):
#         cap = cv2.VideoCapture(video_source)
#         if not cap.isOpened():
#             self.logger.error("Failed to open video source.")
#             return
        
#         ret, frame = cap.read()
#         if ret:
#             self.calibrate(frame)
        
#         while cap.isOpened():
#             ret, frame = cap.read()
#             if not ret:
#                 break
#             self.update_detections(frame)
        
#         cap.release()
#         cv2.destroyAllWindows()
    
#     def close(self):
#         cv2.destroyAllWindows()

# if __name__ == "__main__":
#     logging.basicConfig(level=logging.INFO)
#     config = {
#         "MODEL_PATH": "your_model_path.pt",
#         "FRAME_SIZE": 640,
#         "DISTANCE_THRESHOLD": 30
#     }
#     tracker = SeatTracker(config)
#     tracker.start_tracking(0)  # Use 0 for webcam, or replace with video file path


# version4
# import cv2
# import numpy as np
# import logging
# import time
# from ultralytics import YOLO
# import threading
# from sklearn.cluster import DBSCAN

# class SeatTracker:
#     def __init__(self, config):
#         model_path = config.get('MODEL_PATH')
#         self.frame_size = config.get('FRAME_SIZE')
#         self.distance_threshold = config.get('DISTANCE_THRESHOLD')
#         self.calibration_duration = config.get('CALIBRATION_DURATION')

#         # Set default values if None
#         if self.distance_threshold is None:
#             self.distance_threshold = 30
#         if self.calibration_duration is None:
#             self.calibration_duration = 10

#         self.logger = logging.getLogger(__name__)
#         self.model = YOLO(model_path)
#         self.seat_zones = {}  # Stores detected seat clusters
#         self.lock = threading.Lock()
#         self.next_seat_id = 0
#         self.calibrated = False

#         # Log the initial configuration values
#         self.logger.info(f"Model path: {model_path}")
#         self.logger.info(f"Frame size: {self.frame_size}")
#         self.logger.info(f"Distance threshold: {self.distance_threshold}")
#         self.logger.info(f"Calibration duration: {self.calibration_duration}")

#     def get_centroid(self, box):
#         x1, y1, x2, y2 = box
#         return ((x1 + x2) // 2, (y1 + y2) // 2)

#     def calibrate(self, frame):
#         """Initial calibration phase to detect and cluster seats."""
#         self.logger.info("Starting calibration... Detecting seats for {} seconds.".format(self.calibration_duration))
#         start_time = time.time()
#         while time.time() - start_time < self.calibration_duration:
#             remaining_time = self.calibration_duration - int(time.time() - start_time)
#             self.logger.info(f"Calibration in progress... {remaining_time} seconds left.")
#             self.update_detections(frame, calibrating=True)
#         self.calibrated = True
#         self.logger.info("Calibration complete. Now tracking seat status changes.")

#     def update_detections(self, frame, calibrating=False):
#         try:
#             results = self.model(frame, imgsz=self.frame_size, conf=0.5, verbose=False)
#             with self.lock:
#                 if calibrating:
#                     self.seat_zones = {}  # Reset clusters during calibration
                
#                 detected_seats = []
#                 persons = []
                
#                 for box in results[0].boxes:
#                     cls_id = int(box.cls)
#                     label = self.model.names[cls_id].lower().replace(" ", "").replace("_", "")

#                     x1, y1, x2, y2 = map(int, box.xyxy[0].cpu().numpy())
#                     centroid = self.get_centroid((x1, y1, x2, y2))
                    
#                     if "emptyseat" in label:
#                         detected_seats.append((centroid, (x1, y1, x2, y2)))
#                     elif "person" in label:
#                         persons.append(centroid)

#                 # Clustering logic
#                 self.cluster_seats(detected_seats)
#                 self.update_seat_status(persons)
            
#             self.draw_seat_markers(frame)
#             cv2.imshow("Seat Detection", frame)
#             cv2.waitKey(1)
#         except Exception as e:
#             self.logger.error(f"Error in update_detections: {e}")
#             # Add recovery mechanism if needed

#     def cluster_seats(self, detected_seats):
#         """Groups detected seats into clusters using DBSCAN."""
#         self.seat_zones = {}  # Reset clusters
        
#         if detected_seats:
#             centroids = np.array([seat[0] for seat in detected_seats])
#             self.logger.info(f"Using distance_threshold: {self.distance_threshold}")
#             clustering = DBSCAN(eps=self.distance_threshold, min_samples=1).fit(centroids)
#             labels = clustering.labels_

#             for label, seat in zip(labels, detected_seats):
#                 if label not in self.seat_zones:
#                     self.seat_zones[label] = []
#                 self.seat_zones[label].append(seat)
        
#         self.logger.info(f"Clustered into {len(self.seat_zones)} areas.")

#     def update_seat_status(self, persons):
#         """Updates seat statuses based on detected persons."""
#         for cluster_id, cluster in self.seat_zones.items():
#             for i, (centroid, bbox) in enumerate(cluster):
#                 status = "vacant"
#                 for person in persons:
#                     if np.linalg.norm(np.array(centroid) - np.array(person)) < self.distance_threshold:
#                         status = "occupied"
#                         break
#                 self.seat_zones[cluster_id][i] = (centroid, bbox, status)
#                 self.logger.info(f"Seat {cluster_id}-{i} status: {status}")  # Log seat status
        
#         self.logger.info("Seat statuses updated.")

#     def draw_seat_markers(self, frame):
#         """Draws bounding boxes around seats with colors indicating their status."""
#         for cluster in self.seat_zones.values():
#             for centroid, bbox, status in cluster:
#                 x1, y1, x2, y2 = bbox
#                 color = (0, 0, 255) if status == "occupied" else (0, 255, 0)
#                 cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
#                 label = f"{status}" 
#                 cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)
    
#     def start_tracking(self, video_source):
#         cap = cv2.VideoCapture(video_source)
#         if not cap.isOpened():
#             self.logger.error("Failed to open video source.")
#             return
        
#         ret, frame = cap.read()
#         if ret:
#             self.calibrate(frame)
        
#         while cap.isOpened():
#             ret, frame = cap.read()
#             if not ret:
#                 break
#             self.update_detections(frame)
        
#         cap.release()
#         cv2.destroyAllWindows()
    
#     def close(self):
#         cv2.destroyAllWindows()

# if __name__ == "__main__":
#     logging.basicConfig(level=logging.INFO)
#     config = {
#         "MODEL_PATH": "your_model_path.pt",
#         "FRAME_SIZE": (480, 480),
#         "DISTANCE_THRESHOLD": 30,
#         "CALIBRATION_DURATION": 10
#     }
#     tracker = SeatTracker(config)
#     tracker.start_tracking(0)  # Use 0 for webcam, or replace with video file path



#version 5
# import cv2
# import numpy as np
# import logging
# import time
# from ultralytics import YOLO
# import threading
# from sklearn.cluster import DBSCAN

# class SeatTracker:
#     def __init__(self, config):
#         model_path = config.get('MODEL_PATH')
#         self.frame_size = config.get('FRAME_SIZE')
#         self.distance_threshold = config.get('DISTANCE_THRESHOLD')
#         self.calibration_duration = config.get('CALIBRATION_DURATION')

#         # Set default values if None
#         if self.distance_threshold is None:
#             self.distance_threshold = 30
#         if self.calibration_duration is None:
#             self.calibration_duration = 10

#         self.logger = logging.getLogger(__name__)
#         self.model = YOLO(model_path)
#         self.seat_zones = {}  # Stores detected seat clusters
#         self.lock = threading.Lock()
#         self.next_seat_id = 0
#         self.calibrated = False

#         # Log the initial configuration values
#         self.logger.info(f"Model path: {model_path}")
#         self.logger.info(f"Frame size: {self.frame_size}")
#         self.logger.info(f"Distance threshold: {self.distance_threshold}")
#         self.logger.info(f"Calibration duration: {self.calibration_duration}")

#     def get_centroid(self, box):
#         x1, y1, x2, y2 = box
#         return ((x1 + x2) // 2, (y1 + y2) // 2)

#     def calibrate(self, frame):
#         """Initial calibration phase to detect and cluster seats."""
#         self.logger.info("Starting calibration... Detecting seats for {} seconds.".format(self.calibration_duration))
#         start_time = time.time()
#         while time.time() - start_time < self.calibration_duration:
#             remaining_time = self.calibration_duration - int(time.time() - start_time)
#             self.logger.info(f"Calibration in progress... {remaining_time} seconds left.")
#             self.update_detections(frame, calibrating=True)
#         self.calibrated = True
#         self.logger.info("Calibration complete. Now tracking seat status changes.")

#     def update_detections(self, frame, calibrating=False):
#         try:
#             results = self.model(frame, imgsz=self.frame_size, conf=0.5, verbose=False)
#             with self.lock:
#                 if calibrating:
#                     self.seat_zones = {}  # Reset clusters during calibration
                
#                 detected_seats = []
#                 persons = []
                
#                 for box in results[0].boxes:
#                     cls_id = int(box.cls)
#                     label = self.model.names[cls_id].lower().replace(" ", "").replace("_", "")

#                     x1, y1, x2, y2 = map(int, box.xyxy[0].cpu().numpy())
#                     centroid = self.get_centroid((x1, y1, x2, y2))
                    
#                     if "emptyseat" in label:
#                         detected_seats.append((centroid, (x1, y1, x2, y2)))
#                     elif "persononseat" in label:
#                         persons.append(centroid)

#                 # Clustering logic
#                 self.cluster_seats(detected_seats)
#                 self.update_seat_status(persons)
            
#             self.draw_seat_markers(frame)
#             cv2.imshow("Seat Detection", frame)
#             cv2.waitKey(1)
#         except Exception as e:
#             self.logger.error(f"Error in update_detections: {e}")
#             # Add recovery mechanism if needed

#     def cluster_seats(self, detected_seats):
#         """Groups detected seats into clusters using DBSCAN."""
#         self.seat_zones = {}  # Reset clusters
        
#         if detected_seats:
#             centroids = np.array([seat[0] for seat in detected_seats])
#             self.logger.info(f"Using distance_threshold: {self.distance_threshold}")
#             clustering = DBSCAN(eps=self.distance_threshold, min_samples=1).fit(centroids)
#             labels = clustering.labels_

#             for label, seat in zip(labels, detected_seats):
#                 if label not in self.seat_zones:
#                     self.seat_zones[label] = []
#                 self.seat_zones[label].append(seat)
        
#         self.logger.info(f"Clustered into {len(self.seat_zones)} areas.")

#     def update_seat_status(self, persons):
#         """Updates seat statuses based on detected persons."""
#         for cluster_id, cluster in self.seat_zones.items():
#             for i, (centroid, bbox) in enumerate(cluster):
#                 status = "vacant"
#                 for person in persons:
#                     if np.linalg.norm(np.array(centroid) - np.array(person)) < self.distance_threshold:
#                         status = "occupied"
#                         break
#                 self.seat_zones[cluster_id][i] = (centroid, bbox, status)
#                 self.logger.info(f"Seat {cluster_id}-{i} status: {status}")  # Log seat status
        
#         self.logger.info("Seat statuses updated.")

#     def draw_seat_markers(self, frame):
#         """Draws bounding boxes around seats with colors indicating their status."""
#         for cluster in self.seat_zones.values():
#             for centroid, bbox, status in cluster:
#                 x1, y1, x2, y2 = bbox
#                 color = (0, 0, 255) if status == "occupied" else (0, 255, 0)
#                 cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
#                 label = f"{status}" 
#                 cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)
    
#     def start_tracking(self, video_source):
#         cap = cv2.VideoCapture(video_source)
#         if not cap.isOpened():
#             self.logger.error("Failed to open video source.")
#             return
        
#         ret, frame = cap.read()
#         if ret:
#             self.calibrate(frame)
        
#         while cap.isOpened():
#             ret, frame = cap.read()
#             if not ret:
#                 break
#             self.update_detections(frame)
        
#         cap.release()
#         cv2.destroyAllWindows()
    
#     def close(self):
#         cv2.destroyAllWindows()

# if __name__ == "__main__":
#     logging.basicConfig(level=logging.INFO)
#     config = {
#         "MODEL_PATH": "your_model_path.pt",
#         "FRAME_SIZE": (480, 480),
#         "DISTANCE_THRESHOLD": 30,
#         "CALIBRATION_DURATION": 10
#     }
#     tracker = SeatTracker(config)
#     tracker.start_tracking(0)  # Use 0 for webcam, or replace with video file path

#version6
# import cv2
# import numpy as np
# import logging
# import time
# from ultralytics import YOLO
# import threading
# from sklearn.cluster import DBSCAN

# class SeatTracker:
#     def __init__(self, config):
#         model_path = config.get('MODEL_PATH')
#         self.frame_size = config.get('FRAME_SIZE')
#         self.distance_threshold = config.get('DISTANCE_THRESHOLD')
#         self.calibration_duration = config.get('CALIBRATION_DURATION')

#         # Set default values if None
#         if self.distance_threshold is None:
#             self.distance_threshold = 30
#         if self.calibration_duration is None:
#             self.calibration_duration = 10

#         self.logger = logging.getLogger(__name__)
#         self.model = YOLO(model_path)
#         self.seat_zones = {}  # Stores detected seat clusters
#         self.lock = threading.Lock()
#         self.next_seat_id = 0
#         self.calibrated = False

#         # Log the initial configuration values
#         self.logger.info(f"Model path: {model_path}")
#         self.logger.info(f"Frame size: {self.frame_size}")
#         self.logger.info(f"Distance threshold: {self.distance_threshold}")
#         self.logger.info(f"Calibration duration: {self.calibration_duration}")

#     def get_centroid(self, box):
#         x1, y1, x2, y2 = box
#         return ((x1 + x2) // 2, (y1 + y2) // 2)

#     def calibrate(self, frame):
#         """Initial calibration phase to detect and cluster seats."""
#         self.logger.info("Starting calibration... Detecting seats for {} seconds.".format(self.calibration_duration))
#         start_time = time.time()
#         while time.time() - start_time < self.calibration_duration:
#             remaining_time = self.calibration_duration - int(time.time() - start_time)
#             self.logger.info(f"Calibration in progress... {remaining_time} seconds left.")
#             self.update_detections(frame, calibrating=True)
#         self.calibrated = True
#         self.logger.info("Calibration complete. Now tracking seat status changes.")

#     def update_detections(self, frame, calibrating=False):
#         try:
#             results = self.model(frame, imgsz=self.frame_size, conf=0.5, verbose=False)
#             with self.lock:
#                 if calibrating:
#                     self.seat_zones = {}  # Reset clusters during calibration
                
#                 detected_seats = []
#                 persons = []
                
#                 for box in results[0].boxes:
#                     cls_id = int(box.cls)
#                     label = self.model.names[cls_id].lower().replace(" ", "").replace("_", "")

#                     x1, y1, x2, y2 = map(int, box.xyxy[0].cpu().numpy())
#                     centroid = self.get_centroid((x1, y1, x2, y2))
                    
#                     if "emptyseat" in label:
#                         detected_seats.append((centroid, (x1, y1, x2, y2)))
#                     elif "persononseat" in label:
#                         persons.append(centroid)

#                 # Clustering logic
#                 self.cluster_seats(detected_seats)
#                 self.update_seat_status(persons)
            
#             self.draw_seat_markers(frame)
#             cv2.imshow("Seat Detection", frame)
#             cv2.waitKey(1)
#         except Exception as e:
#             self.logger.error(f"Error in update_detections: {e}")
#             # Add recovery mechanism if needed

#     def cluster_seats(self, detected_seats):
#         """Groups detected seats into clusters using DBSCAN."""
#         self.seat_zones = {}  # Reset clusters
        
#         if detected_seats:
#             centroids = np.array([seat[0] for seat in detected_seats])
#             self.logger.info(f"Using distance_threshold: {self.distance_threshold}")
#             clustering = DBSCAN(eps=self.distance_threshold, min_samples=1).fit(centroids)
#             labels = clustering.labels_

#             for label, seat in zip(labels, detected_seats):
#                 if label not in self.seat_zones:
#                     self.seat_zones[label] = []
#                 self.seat_zones[label].append(seat)
        
#         self.logger.info(f"Clustered into {len(self.seat_zones)} areas.")

#     def update_seat_status(self, persons):
#         """Updates seat statuses based on detected persons."""
#         for cluster_id, cluster in self.seat_zones.items():
#             for i, (centroid, bbox) in enumerate(cluster):
#                 status = "vacant"
#                 for person in persons:
#                     if np.linalg.norm(np.array(centroid) - np.array(person)) < self.distance_threshold:
#                         status = "occupied"
#                         break
#                 self.seat_zones[cluster_id][i] = (centroid, bbox, status)
#                 self.logger.info(f"Seat {cluster_id}-{i} status: {status}")  # Log seat status
        
#         self.logger.info("Seat statuses updated.")

#     def draw_seat_markers(self, frame):
#         """Draws bounding boxes around seats with colors indicating their status."""
#         for cluster_id, cluster in self.seat_zones.items():
#             for i, (centroid, bbox, status) in enumerate(cluster):
#                 x1, y1, x2, y2 = bbox
#                 color = (0, 0, 255) if status == "occupied" else (0, 255, 0)
#                 cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
#                 label = f"{status}" 
#                 cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)
    
#     def start_tracking(self, video_source):
#         cap = cv2.VideoCapture(video_source)
#         if not cap.isOpened():
#             self.logger.error("Failed to open video source.")
#             return
        
#         ret, frame = cap.read()
#         if ret:
#             self.calibrate(frame)
        
#         while cap.isOpened():
#             ret, frame = cap.read()
#             if not ret:
#                 break
#             self.update_detections(frame)
        
#         cap.release()
#         cv2.destroyAllWindows()
    
#     def close(self):
#         cv2.destroyAllWindows()

# if __name__ == "__main__":
#     logging.basicConfig(level=logging.INFO)
#     config = {
#         "MODEL_PATH": "your_model_path.pt",
#         "FRAME_SIZE": (480, 480),
#         "DISTANCE_THRESHOLD": 30,
#         "CALIBRATION_DURATION": 10
#     }
#     tracker = SeatTracker(config)
#     tracker.start_tracking(0)  # Use 0 for webcam, or replace with video file path


#version 7
# import cv2
# import numpy as np
# import logging
# import time
# from ultralytics import YOLO
# import threading
# from collections import defaultdict

# class SeatTracker:
#     def __init__(self, config):
#         self.model_path = config.get('MODEL_PATH')
#         self.frame_size = config.get('FRAME_SIZE')
#         self.conf_threshold = 0.5
#         self.logger = logging.getLogger(__name__)
        
#         # Initialize model and tracking variables
#         self.model = YOLO(self.model_path)
#         self.lock = threading.Lock()
#         self.seat_status = {"occupied": 0, "vacant": 0}
#         self.seat_locations = []
#         self.person_locations = []
        
#         # Log model information
#         self.logger.info(f"Model loaded from: {self.model_path}")
#         self.logger.info(f"Model classes: {self.model.names}")
        
#         # Map class names for seat tracking
#         self.seat_class_ids = []
#         self.person_class_ids = []
        
#         for class_id, class_name in self.model.names.items():
#             name_lower = class_name.lower()
#             if "empty" in name_lower or "seat" in name_lower:
#                 if "person" not in name_lower:  # Avoid person_on_seat being counted as seat
#                     self.seat_class_ids.append(class_id)
#                     self.logger.info(f"Detected seat class: {class_name} (ID: {class_id})")
#             if "person" in name_lower:
#                 self.person_class_ids.append(class_id)
#                 self.logger.info(f"Detected person class: {class_name} (ID: {class_id})")

#     def update_detections(self, frame):
#         """Process a frame and update seat status"""
#         try:
#             # Resize frame if needed
#             if frame.shape[:2] != self.frame_size:
#                 frame = cv2.resize(frame, self.frame_size, interpolation=cv2.INTER_AREA)
            
#             # Run YOLO detection
#             results = self.model(frame, imgsz=self.frame_size, conf=self.conf_threshold, verbose=False)
            
#             with self.lock:
#                 self.seat_locations = []
#                 self.person_locations = []
                
#                 # Process each detection
#                 for box in results[0].boxes:
#                     cls_id = int(box.cls)
#                     conf = float(box.conf)
                    
#                     # Extract coordinates
#                     x1, y1, x2, y2 = map(int, box.xyxy[0].cpu().numpy())
#                     centroid = ((x1 + x2) // 2, (y1 + y2) // 2)
                    
#                     if cls_id in self.seat_class_ids:
#                         self.seat_locations.append((centroid, (x1, y1, x2, y2), "vacant"))
                    
#                     if cls_id in self.person_class_ids:
#                         self.person_locations.append((centroid, (x1, y1, x2, y2)))
                
#                 # Check if seats are occupied by people
#                 occupied_count = 0
#                 for i, (center, bbox, _) in enumerate(self.seat_locations):
#                     status = "vacant"
#                     for person_center, _ in self.person_locations:
#                         # Check if person is close to seat
#                         distance = np.sqrt(
#                             (center[0] - person_center[0]) ** 2 + 
#                             (center[1] - person_center[1]) ** 2
#                         )
#                         # If person is close enough to seat, mark as occupied
#                         if distance < 50:  # Adjustable threshold
#                             status = "occupied"
#                             occupied_count += 1
#                             break
                    
#                     # Update seat status
#                     self.seat_locations[i] = (center, bbox, status)
                
#                 # Direct detection of person_on_seat class
#                 for box in results[0].boxes:
#                     cls_id = int(box.cls)
#                     class_name = self.model.names[cls_id].lower()
                    
#                     if "person_on_seat" in class_name or ("person" in class_name and "seat" in class_name):
#                         occupied_count += 1
                
#                 # Update overall status count
#                 self.seat_status = {
#                     "occupied": occupied_count,
#                     "vacant": max(0, len(self.seat_locations) - occupied_count)
#                 }
                
#                 self.logger.info(f"Seats: {len(self.seat_locations)}, Occupied: {self.seat_status['occupied']}, Vacant: {self.seat_status['vacant']}")
            
#             # Draw visualizations on the frame
#             self.draw_visualizations(frame)
#             cv2.imshow("Seat Tracking", frame)
#             cv2.waitKey(1)
            
#         except Exception as e:
#             self.logger.error(f"Error in update_detections: {str(e)}")
#             import traceback
#             self.logger.error(traceback.format_exc())
    
#     def draw_visualizations(self, frame):
#         """Draw bounding boxes and status information on the frame"""
#         # Draw seats
#         for center, bbox, status in self.seat_locations:
#             x1, y1, x2, y2 = bbox
#             color = (0, 0, 255) if status == "occupied" else (0, 255, 0)
#             cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
#             cv2.putText(frame, status, (x1, y1-10), 
#                        cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)
        
#         # Draw people
#         for center, bbox in self.person_locations:
#             x1, y1, x2, y2 = bbox
#             cv2.rectangle(frame, (x1, y1), (x2, y2), (255, 0, 0), 2)
        
#         # Display count information
#         cv2.putText(frame, f"Occupied: {self.seat_status['occupied']}", (10, 30), 
#                    cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)
#         cv2.putText(frame, f"Vacant: {self.seat_status['vacant']}", (10, 60), 
#                    cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)
    
#     def get_status(self):
#         """Return the current seat status for API reporting"""
#         with self.lock:
#             return {
#                 "occupied": self.seat_status["occupied"],
#                 "vacant": self.seat_status["vacant"],
#                 "total": len(self.seat_locations)
#             }
    
#     def close(self):
#         cv2.destroyAllWindows()


# version 8
# import cv2
# import numpy as np
# import logging
# import time
# from ultralytics import YOLO
# import threading
# from collections import defaultdict

# class SeatTracker:
#     def __init__(self, config):
#         self.model_path = config.get('MODEL_PATH')
#         self.frame_size = config.get('FRAME_SIZE')
#         self.conf_threshold = 0.5
#         self.logger = logging.getLogger(__name__)
        
#         # Initialize model and tracking variables
#         self.model = YOLO(self.model_path)
#         self.lock = threading.Lock()
#         self.seat_status = []  # List that will hold seat statuses with seat numbers
#         self.seat_locations = []  # Detected seat bounding boxes and their centers
#         self.person_locations = []
        
#         # Log model information
#         self.logger.info(f"Model loaded from: {self.model_path}")
#         self.logger.info(f"Model classes: {self.model.names}")
        
#         # Map class names for seat tracking
#         self.seat_class_ids = []
#         self.person_class_ids = []
        
#         for class_id, class_name in self.model.names.items():
#             name_lower = class_name.lower()
#             if "empty" in name_lower or "seat" in name_lower:
#                 if "person" not in name_lower:  # Avoid person_on_seat being counted as seat
#                     self.seat_class_ids.append(class_id)
#                     self.logger.info(f"Detected seat class: {class_name} (ID: {class_id})")
#             if "person" in name_lower:
#                 self.person_class_ids.append(class_id)
#                 self.logger.info(f"Detected person class: {class_name} (ID: {class_id})")
        
#         # Get the seat limit from configuration
#         self.seat_limit = config.get('SEAT_LIMIT')
        
#     def update_detections(self, frame):
#         """Process a frame and update seat status based on the seat limit."""
#         try:
#             # Resize frame if needed
#             if frame.shape[:2] != self.frame_size:
#                 frame = cv2.resize(frame, self.frame_size, interpolation=cv2.INTER_AREA)
            
#             # Run YOLO detection
#             results = self.model(frame, imgsz=self.frame_size, conf=self.conf_threshold, verbose=False)
            
#             with self.lock:
#                 self.seat_locations = []
#                 self.person_locations = []
                
#                 # Process each detection from the model's first result
#                 for box in results[0].boxes:
#                     cls_id = int(box.cls)
#                     conf = float(box.conf)
                    
#                     # Extract coordinates
#                     x1, y1, x2, y2 = map(int, box.xyxy[0].cpu().numpy())
#                     centroid = ((x1 + x2) // 2, (y1 + y2) // 2)
                    
#                     if cls_id in self.seat_class_ids:
#                         self.seat_locations.append((centroid, (x1, y1, x2, y2)))
                    
#                     if cls_id in self.person_class_ids:
#                         self.person_locations.append((centroid, (x1, y1, x2, y2)))
                
#                 # Sort detected seats based on a consistent order (e.g., by x then y coordinate)
#                 self.seat_locations.sort(key=lambda tup: (tup[0][1], tup[0][0]))  # Sort by row then column
                
#                 # Determine status for each seat location based on proximity to a person
#                 mapped_seats = []
#                 for idx, (center, bbox) in enumerate(self.seat_locations):
#                     status = "vacant"
#                     for person_center, _ in self.person_locations:
#                         # Check if person is close to seat using Euclidean distance
#                         distance = np.sqrt((center[0] - person_center[0])**2 + (center[1] - person_center[1])**2)
#                         if distance < 50:  # Adjustable threshold
#                             status = "occupied"
#                             break
#                     mapped_seats.append({
#                         "seatNumber": idx + 1,
#                         "status": status,
#                         "bbox": bbox
#                     })
                
#                 # Apply seat limit: only update up to seat_limit seats.
#                 if len(mapped_seats) > self.seat_limit:
#                     mapped_seats = mapped_seats[:self.seat_limit]
#                 # If fewer detections than seat limit, add missing ones as vacant.
#                 while len(mapped_seats) < self.seat_limit:
#                     mapped_seats.append({
#                         "seatNumber": len(mapped_seats) + 1,
#                         "status": "vacant",
#                         "bbox": None
#                     })
                
#                 self.seat_status = mapped_seats
#                 self.logger.info(f"Updated seat statuses: {self.seat_status}")
                
#             # Draw visualizations on the frame for debugging purposes
#             self.draw_visualizations(frame)
#             cv2.imshow("Seat Tracking", frame)
#             cv2.waitKey(1)
            
#         except Exception as e:
#             self.logger.error(f"Error in update_detections: {str(e)}")
            
#     def draw_visualizations(self, frame):
#         """Draw bounding boxes and status information on the frame."""
#         for seat in self.seat_status:
#             bbox = seat["bbox"]
#             if bbox:
#                 x1, y1, x2, y2 = bbox
#                 color = (0, 0, 255) if seat["status"] == "occupied" else (0, 255, 0)
#                 cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
#                 cv2.putText(frame, f"Seat {seat['seatNumber']} {seat['status']}", (x1, y1-10),
#                             cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)
#         # Display count information
#         total = self.seat_limit
#         occupied = len([s for s in self.seat_status if s["status"] == "occupied"])
#         vacant = total - occupied
#         cv2.putText(frame, f"Occupied: {occupied}", (10, 30),
#                     cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)
#         cv2.putText(frame, f"Vacant: {vacant}", (10, 60),
#                     cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)
        
#     def get_status(self):
#         """Return the current detailed seat status for API reporting."""
#         with self.lock:
#             return self.seat_status
        
#     def close(self):
#         cv2.destroyAllWindows()

#version 9
# import cv2
# import numpy as np
# import logging

# class SeatTracker:
#     def __init__(self, config):
#         self.config = config
#         self.frame_size = config.get("FRAME_SIZE")
#         self.seat_limit = config.get("SEAT_LIMIT")
#         self.model_path = config.get("MODEL_PATH")
#         self.logger = logging.getLogger("SeatTracker")
        
#         # Load the model (for simulation, we simply log that the model is loaded)
#         self.logger.info(f"Model loaded from: {self.model_path}")
        
#         # Define model classes mapping: 0 = empty_seat, 1 = person_on_seat
#         self.model_classes = {0: 'empty_seat', 1: 'person_on_seat'}
#         self.logger.info(f"Model classes: {self.model_classes}")
#         self.seat_status = []  # List that will hold seat records

#     def update_detections(self, frame):
#         """
#         Simulate detection by randomly selecting a predicted class for each seat.
#         The prediction from the (simulated) deep model is mapped to the appropriate seat status.
#           - 'empty_seat' => vacant
#           - 'person_on_seat' => occupied
#         """
#         simulated_seats = []
#         for i in range(1, self.seat_limit + 1):
#             # Simulate model output by randomly picking one of the two classes.
#             # You can adjust the threshold probabilities as needed.
#             predicted_class_id = 0 if np.random.rand() < 0.5 else 1
#             predicted_class = self.model_classes[predicted_class_id]
            
#             # Determine seat status based on the predicted class.
#             if predicted_class == 'person_on_seat':
#                 status = "occupied"
#             else:
#                 status = "vacant"
            
#             # For simulation, generate a fake bounding box (or None if not detected)
#             bbox = (50 * i, 50, 50 * i + 40, 90) if np.random.rand() > 0.3 else None
            
#             simulated_seats.append({
#                 "seatNumber": i,
#                 "status": status,
#                 "bbox": bbox,
#                 "predicted_class": predicted_class  # Logging the predicted class for debugging
#             })
        
#         self.seat_status = simulated_seats
#         self.logger.info(f"Updated seat statuses: {self.seat_status}")

#         # Draw simulated detections on frame for visualization.
#         for seat in simulated_seats:
#             if seat["bbox"]:
#                 x1, y1, x2, y2 = seat["bbox"]
#                 # Use green for vacant and red for occupied
#                 color = (0, 255, 0) if seat["status"] == "vacant" else (0, 0, 255)
#                 cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
#                 cv2.putText(frame, f"Seat {seat['seatNumber']}: {seat['predicted_class']}", (x1, y1 - 10),
#                             cv2.FONT_HERSHEY_SIMPLEX, 0.5, (255, 255, 255), 1)
#         return frame

#     def get_status(self):
#         return self.seat_status

#     def close(self):
#         # If there were resources to clean up, do it here.
#         self.logger.info("Closing SeatTracker resources.")

#version 10
# import cv2
# import numpy as np
# import logging
# import time
# import threading
# from ultralytics import YOLO

# class SeatTracker:
#     def __init__(self, config):
#         """
#         Initialize the SeatTracker with configuration settings.
#         It loads the YOLO model, sets up threading lock for safe detection updates,
#         and configures detection parameters. The model is expected to have exactly two classes:
#         'empty_seat' and 'person_on_seat'.
#         """
#         self.config = config
#         self.model_path = config.get('MODEL_PATH')
#         self.frame_size = config.get('FRAME_SIZE')
#         self.seat_limit = config.get('SEAT_LIMIT')
#         self.conf_threshold = 0.5  # Confidence threshold for detections
        
#         self.logger = logging.getLogger("SeatTracker")
#         self.logger.setLevel(logging.INFO)
        
#         # Load YOLO model from the specified model path.
#         self.model = YOLO(self.model_path)
        
#         # Threading lock for safe updates.
#         self.lock = threading.Lock()
        
#         # Lists to store detections.
#         self.seat_status = []   # Final aggregated list of seat statuses.
#         self.detections = []    # Raw detection results from the model.
        
#         # Expected model class mapping.
#         # Expecting model.names to be a dict where keys are class IDs and values are class names.
#         # The two expected classes: 'empty_seat' and 'person_on_seat'
#         self.expected_classes = {'empty_seat': 'vacant', 'person_on_seat': 'occupied'}
#         self.model_class_ids = {}
#         for class_id, class_name in self.model.names.items():
#             name_lower = class_name.lower()
#             if name_lower in self.expected_classes:
#                 self.model_class_ids[class_id] = name_lower
#                 self.logger.info(f"Detected model class: {class_name} (ID: {class_id})")

#         # Verifying model has both expected classes.
#         if len(self.model_class_ids) < 2:
#             self.logger.warning("Model might not contain both 'empty_seat' and 'person_on_seat' classes.")

#     def update_detections(self, frame):
#         """
#         Process a frame to detect seats and persons using the YOLO model.
#         It assigns a seat number and a status based on detected class:
#           - 'empty_seat' corresponds to "vacant"
#           - 'person_on_seat' corresponds to "occupied"
#         Detections are sorted by their vertical position (y-coordinate) and then horizontal for consistent seat numbering.
#         The method also draws visualizations on the frame and displays it.
#         """
#         try:
#             # Resize frame to expected frame size if different.
#             if frame.shape[1::-1] != self.frame_size:
#                 frame = cv2.resize(frame, self.frame_size, interpolation=cv2.INTER_AREA)
            
#             # Run YOLO model on the frame.
#             results = self.model(frame, imgsz=self.frame_size, conf=self.conf_threshold, verbose=False)
            
#             # Temporary list for detections.
#             detections_list = []
            
#             # Process detections from the first result (assuming single image inference).
#             for box in results[0].boxes:
#                 cls_id = int(box.cls)
#                 conf = float(box.conf)
#                 # Only process boxes if confidence meets threshold.
#                 if conf < self.conf_threshold:
#                     continue
#                 # Check if the detected class is one we expect.
#                 if cls_id not in self.model_class_ids:
#                     continue
                
#                 predicted_class = self.model_class_ids[cls_id]
#                 status = self.expected_classes[predicted_class]
                
#                 # Extract bounding box coordinates (x1, y1, x2, y2)
#                 x1, y1, x2, y2 = map(int, box.xyxy[0].cpu().numpy())
#                 centroid = ((x1 + x2)//2, (y1 + y2)//2)
                
#                 detections_list.append({
#                     "centroid": centroid,
#                     "bbox": (x1, y1, x2, y2),
#                     "predicted_class": predicted_class,
#                     "status": status,
#                     "confidence": conf
#                 })
            
#             # Sort detections by the centroid's y (and x) for consistent seat numbering.
#             detections_list.sort(key=lambda d: (d["centroid"][1], d["centroid"][0]))
            
#             # Assign seat numbers and enforce the seat limit.
#             mapped_seats = []
#             for idx, detection in enumerate(detections_list):
#                 if idx >= self.seat_limit:
#                     break
#                 detection["seatNumber"] = idx + 1
#                 mapped_seats.append(detection)
            
#             # If fewer detections than seat limit, add placeholder seats as vacant.
#             while len(mapped_seats) < self.seat_limit:
#                 mapped_seats.append({
#                     "seatNumber": len(mapped_seats) + 1,
#                     "status": "vacant",
#                     "bbox": None,
#                     "predicted_class": "empty_seat",
#                     "confidence": 0.0
#                 })
            
#             with self.lock:
#                 self.seat_status = mapped_seats
            
#             self.logger.info(f"Updated seat statuses: {self.seat_status}")
            
#             # Draw visualizations on the frame.
#             self.draw_visualizations(frame)
#             cv2.imshow("Seat Tracking", frame)
#             cv2.waitKey(1)
#             return frame
        
#         except Exception as e:
#             self.logger.error(f"Error processing detections: {str(e)}")
#             return frame

#     def draw_visualizations(self, frame):
#         """
#         Draw bounding boxes and seat tracking information on the frame.
#         Occupied seats are drawn with a red rectangle, while vacant seats are drawn green.
#         Additionally, aggregate counts of occupied and vacant seats are displayed.
#         """
#         for seat in self.seat_status:
#             bbox = seat.get("bbox")
#             if bbox:
#                 x1, y1, x2, y2 = bbox
#                 # Red for "occupied", green for "vacant"
#                 color = (0, 0, 255) if seat["status"] == "occupied" else (0, 255, 0)
#                 cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
#                 label = f"Seat {seat['seatNumber']} : {seat['predicted_class']}"
#                 cv2.putText(frame, label, (x1, y1 - 10),
#                             cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)
        
#         # Display overall counts.
#         total = self.seat_limit
#         occupied = len([s for s in self.seat_status if s["status"] == "occupied"])
#         vacant = total - occupied
#         cv2.putText(frame, f"Occupied: {occupied}", (10, 30),
#                     cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)
#         cv2.putText(frame, f"Vacant: {vacant}", (10, 60),
#                     cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)
    
#     def get_status(self):
#         """
#         Get the current seat status data in a thread-safe manner.
#         Returns a list of dictionaries, each containing details about a seat.
#         """
#         with self.lock:
#             return self.seat_status.copy()
    
#     def close(self):
#         """
#         Perform cleanup operations, such as closing the display window.
#         """
#         cv2.destroyAllWindows()

# if __name__ == "__main__":
#     # Example configuration
#     config = {
#         "MODEL_PATH": "path/to/your/yolo_model.pt",  # Provide the correct model path.
#         "FRAME_SIZE": (640, 480),
#         "SEAT_LIMIT": 10
#     }
    
#     logging.basicConfig(level=logging.INFO)
#     seat_tracker = SeatTracker(config)
    
#     # Open a sample video or webcam capture.
#     cap = cv2.VideoCapture(0)  # Use 0 for webcam. Replace with video file path if needed.
    
#     try:
#         while True:
#             ret, frame = cap.read()
#             if not ret:
#                 break
#             seat_tracker.update_detections(frame)
#     except KeyboardInterrupt:
#         pass
#     finally:
#         cap.release()
#         seat_tracker.close()


#version 10
# import cv2
# import threading
# import logging
# from ultralytics import YOLO

# class SeatTracker:
#     def __init__(self, config):
#         """
#         Initialize the SeatTracker with configuration settings.
#         Loads the YOLO model and creates thread-safe storage for seat status.
#         Expected model classes: 'empty_seat' and 'person_on_seat'.
#         """
#         self.config = config
#         self.model_path = config.get("MODEL_PATH")
#         self.frame_size = config.get("FRAME_SIZE")
#         self.seat_limit = config.get("SEAT_LIMIT")
#         self.conf_threshold = 0.5
        
#         self.logger = logging.getLogger("SeatTracker")
#         self.logger.setLevel(logging.INFO)
        
#         # Load the YOLO model.
#         self.model = YOLO(self.model_path)
#         self.lock = threading.Lock()
#         self.seat_status = []
        
#         # Map expected model classes.
#         self.expected_classes = {'empty_seat': 'vacant', 'person_on_seat': 'occupied'}
#         self.model_class_ids = {}
#         for class_id, class_name in self.model.names.items():
#             name_lower = class_name.lower()
#             if name_lower in self.expected_classes:
#                 self.model_class_ids[class_id] = name_lower
#                 self.logger.info(f"Detected model class: {class_name} (ID: {class_id})")
#         if len(self.model_class_ids) < 2:
#             self.logger.warning("Model may not contain both 'empty_seat' and 'person_on_seat' classes.")

#     def update_detections(self, frame):
#         """
#         Process a video frame to detect seats using the YOLO model.
#         Assign seat numbers based on the sorted position of detections.
#         """
#         try:
#             if frame.shape[1::-1] != self.frame_size:
#                 frame = cv2.resize(frame, self.frame_size, interpolation=cv2.INTER_AREA)
            
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
#             detections_list.sort(key=lambda d: (d["centroid"][1], d["centroid"][0]))
#             mapped_seats = []
#             for idx, detection in enumerate(detections_list):
#                 if idx >= self.seat_limit:
#                     break
#                 detection["seatNumber"] = idx + 1
#                 mapped_seats.append(detection)
#             # Pad with vacant seats if required.
#             while len(mapped_seats) < self.seat_limit:
#                 mapped_seats.append({
#                     "seatNumber": len(mapped_seats) + 1,
#                     "status": "vacant",
#                     "bbox": None,
#                     "predicted_class": "empty_seat",
#                     "confidence": 0.0
#                 })
#             with self.lock:
#                 self.seat_status = mapped_seats
#             # Optional: Draw visualizations and display frame.
#             self.draw_visualizations(frame)
#             cv2.imshow("Seat Tracking", frame)
#             cv2.waitKey(1)
#             return frame
        
#         except Exception as e:
#             self.logger.error(f"Error in update_detections: {e}")
#             return frame

#     def draw_visualizations(self, frame):
#         """
#         Draw bounding boxes and seat labels on the frame for visual inspection.
#         """
#         for seat in self.seat_status:
#             bbox = seat.get("bbox")
#             if bbox:
#                 x1, y1, x2, y2 = bbox
#                 color = (0, 0, 255) if seat["status"] == "occupied" else (0, 255, 0)
#                 cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
#                 label = f"Seat {seat['seatNumber']} : {seat['predicted_class']}"
#                 cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)
#         total = self.seat_limit
#         occupied = len([s for s in self.seat_status if s["status"] == "occupied"])
#         vacant = total - occupied
#         cv2.putText(frame, f"Occupied: {occupied}", (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)
#         cv2.putText(frame, f"Vacant: {vacant}", (10, 60), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)
    
#     def get_status(self):
#         """
#         Returns a thread-safe copy of the current seat status.
#         """
#         with self.lock:
#             return self.seat_status.copy()

#     def close(self):
#         """
#         Cleanup operations.
#         """
#         cv2.destroyAllWindows()


#version 11
# import cv2
# import threading
# import logging
# from ultralytics import YOLO

# class SeatTracker:
#     def __init__(self, config):
#         """
#         Initialize SeatTracker with settings.
#         Expected model classes: 'empty_seat' (mapped to 'vacant') and 'person_on_seat' (mapped to 'occupied').
#         """
#         self.config = config
#         self.model_path = config.get("MODEL_PATH")
#         self.frame_size = config.get("FRAME_SIZE")
#         self.seat_limit = config.get("SEAT_LIMIT")
#         self.conf_threshold = 0.5
        
#         self.logger = logging.getLogger("SeatTracker")
#         self.logger.setLevel(logging.INFO)
        
#         # Load the YOLO model.
#         self.model = YOLO(self.model_path)
#         self.lock = threading.Lock()
#         self.seat_status = []
        
#         # Map expected model classes.
#         self.expected_classes = {'empty_seat': 'vacant', 'person_on_seat': 'occupied'}
#         self.model_class_ids = {}
#         for class_id, class_name in self.model.names.items():
#             name_lower = class_name.lower()
#             if name_lower in self.expected_classes:
#                 self.model_class_ids[class_id] = name_lower
#                 self.logger.info(f"Detected model class: {class_name} (ID: {class_id})")
#         if len(self.model_class_ids) < 2:
#             self.logger.warning("Model may not contain both 'empty_seat' and 'person_on_seat' classes.")

#     def update_detections(self, frame):
#         """
#         Process a video frame to detect seat status.
#         Assign seat numbers based on position order.
#         """
#         try:
#             if frame.shape[1::-1] != self.frame_size:
#                 frame = cv2.resize(frame, self.frame_size, interpolation=cv2.INTER_AREA)
            
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
#             detections_list.sort(key=lambda d: (d["centroid"][1], d["centroid"][0]))
#             mapped_seats = []
#             for idx, detection in enumerate(detections_list):
#                 if idx >= self.seat_limit:
#                     break
#                 detection["seatNumber"] = idx + 1
#                 mapped_seats.append(detection)
#             # Pad with vacant seats if needed.
#             while len(mapped_seats) < self.seat_limit:
#                 mapped_seats.append({
#                     "seatNumber": len(mapped_seats) + 1,
#                     "status": "vacant",
#                     "bbox": None,
#                     "predicted_class": "empty_seat",
#                     "confidence": 0.0
#                 })
#             with self.lock:
#                 self.seat_status = mapped_seats
#             self.draw_visualizations(frame)
#             cv2.imshow("Seat Tracking", frame)
#             cv2.waitKey(1)
#             return frame
        
#         except Exception as e:
#             self.logger.error(f"Error in update_detections: {e}")
#             return frame

#     def draw_visualizations(self, frame):
#         """
#         Draw seat bounding boxes and labels on the frame.
#         """
#         for seat in self.seat_status:
#             bbox = seat.get("bbox")
#             if bbox:
#                 x1, y1, x2, y2 = bbox
#                 color = (0, 0, 255) if seat["status"] == "occupied" else (0, 255, 0)
#                 cv2.rectangle(frame, (x1, y1), (x2, y2), color, 2)
#                 label = f"Seat {seat['seatNumber']} : {seat['predicted_class']}"
#                 cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, color, 2)
#         total = self.seat_limit
#         occupied = len([s for s in self.seat_status if s["status"] == "occupied"])
#         vacant = total - occupied
#         cv2.putText(frame, f"Occupied: {occupied}", (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)
#         cv2.putText(frame, f"Vacant: {vacant}", (10, 60), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2)
    
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
#         cv2.destroyAllWindows()

# version 12
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
                "id": seat['id']
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
                seat_status_dict[seat_id] = {
                    'status': 'occupied' if seat["status"] == "occupied" else 'empty',
                    'confidence': seat["confidence"],
                    'class': seat["predicted_class"],
                    'seat_number': seat["seatNumber"],
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