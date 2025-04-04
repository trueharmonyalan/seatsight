import logging
import threading
import time
import psycopg2
from seat_tracker import SeatTracker
from camera import VideoProcessor

logger = logging.getLogger(__name__)

class RestaurantSession:
    """
    Manages a tracking session for a specific restaurant.
    """
    
    def __init__(self, restaurant_id, config, db_conn_str):
        """
        Initialize a restaurant session.
        
        Args:
            restaurant_id: The ID of the restaurant
            config: Configuration dictionary
            db_conn_str: Database connection string
        """
        self.restaurant_id = restaurant_id
        self.config = config
        self.db_conn_str = db_conn_str
        self.running = False
        self.processing_thread = None
        self.logger = logging.getLogger(f"RestaurantSession_{restaurant_id}")
        
        # Initialize camera
        camera_url = config.get('IP_CAMERA_URL')
        if not camera_url:
            self.logger.error("No camera URL provided in configuration")
            self.video_processor = None
        else:
            self.video_processor = VideoProcessor(
                source=camera_url,
                process_every_n=config.get('PROCESS_EVERY_N_FRAMES', 30),
                resolution=config.get('FRAME_SIZE', (640, 480))
            )
        
        # Initialize seat tracker with model path
        model_path = config.get('MODEL_PATH', 'best.pt')
        self.seat_tracker = SeatTracker(model_path=model_path)
        
        # Configure seat locations
        seats = config.get('SEATS', [])
        if seats:
            self.seat_tracker.set_seat_config(seats)
        else:
            self.logger.warning("No seat configuration provided")
    
    def start(self):
        """
        Start the restaurant tracking session.
        
        Returns:
            bool: True if started successfully
        """
        if self.running:
            self.logger.info(f"Session for restaurant {self.restaurant_id} already running")
            return True
            
        try:
            self.logger.info(f"Starting session for restaurant {self.restaurant_id}")
            
            # Check if we have a camera
            if not self.video_processor:
                self.logger.error("No video processor available, cannot start session")
                return False
            
            # Start video processor
            self.video_processor.start()
            
            # Start processing thread
            self.running = True
            self.processing_thread = threading.Thread(
                target=self._processing_loop,
                name=f"ProcessingThread_{self.restaurant_id}"
            )
            self.processing_thread.daemon = True
            self.processing_thread.start()
            
            self.logger.info(f"Restaurant session started for ID: {self.restaurant_id}")
            return True
            
        except Exception as e:
            self.logger.error(f"Error starting restaurant session: {e}")
            self.running = False
            return False
    
    def stop(self):
        """Stop the restaurant tracking session."""
        if not self.running:
            return
            
        self.logger.info(f"Stopping session for restaurant {self.restaurant_id}")
        self.running = False
        
        # Stop video processor
        if self.video_processor:
            self.video_processor.stop()
            
        # Wait for processing thread to finish
        if self.processing_thread:
            self.processing_thread.join(timeout=5.0)
        
        # Clean up seat tracker
        if self.seat_tracker:
            self.seat_tracker.close()
            
        self.logger.info(f"Session for restaurant {self.restaurant_id} stopped")
    
    def _processing_loop(self):
        """Main processing loop for the restaurant session."""
        self.logger.info(f"Processing loop started for restaurant {self.restaurant_id}")
        
        last_db_update = time.time()
        db_update_interval = 10.0  # Update database every 10 seconds
        
        try:
            while self.running:
                # Get latest frame
                frame = None
                if self.video_processor:
                    frame = self.video_processor.get_frame()
                
                if frame is None:
                    self.logger.error(f"Error in processing loop: No frame available")
                    time.sleep(0.1)
                    continue
                
                # Process frame if it's time
                if self.video_processor.should_process_frame():
                    # Process frame with seat tracker
                    processed_frame, seat_status = self.seat_tracker.process_frame(frame)
                    
                    # Update database if needed
                    if seat_status and time.time() - last_db_update > db_update_interval:
                        self._update_database(seat_status)
                        last_db_update = time.time()
                
                # Add a small sleep to avoid high CPU usage
                time.sleep(0.01)
                
        except Exception as e:
            self.logger.error(f"Error in processing loop: {e}")
            import traceback
            self.logger.error(traceback.format_exc())
            
    def _update_database(self, seat_status):
        """
        Update seat status and position in the database based on AI detection.
        
        Args:
            seat_status: Dictionary of seat status updates
        """
        if not seat_status:
            return
            
        try:
            # Connect to database
            conn = psycopg2.connect(self.db_conn_str)
            cur = conn.cursor()
            
            # Update each seat status
            for seat_id, status_info in seat_status.items():
                # Convert status to match schema's allowed values ('vacant', 'occupied')
                # Note that 'empty' from SeatTracker maps to 'vacant' in the database
                db_status = 'occupied' if status_info['status'] == 'occupied' else 'vacant'
                
                # Extract position coordinates if available
                pos_x = status_info.get('pos_x')
                pos_y = status_info.get('pos_y')
                
                # Construct the SQL update query based on available data
                if pos_x is not None and pos_y is not None:
                    # Update both status and position
                    cur.execute(
                        """
                        UPDATE seats 
                        SET status = %s, pos_x = %s, pos_y = %s 
                        WHERE id = %s AND restaurant_id = %s
                        """,
                        (db_status, pos_x, pos_y, seat_id, self.restaurant_id)
                    )
                else:
                    # Update only status if positions are not available
                    cur.execute(
                        """
                        UPDATE seats 
                        SET status = %s 
                        WHERE id = %s AND restaurant_id = %s
                        """,
                        (db_status, seat_id, self.restaurant_id)
                    )
            
            # Commit changes
            conn.commit()
            
            # Close database connection
            cur.close()
            conn.close()
            
            self.logger.info(f"Updated {len(seat_status)} seat statuses and positions in database for restaurant {self.restaurant_id}")
            
        except Exception as e:
            self.logger.error(f"Error updating database: {e}")
            # Log the traceback for easier debugging
            import traceback
            self.logger.error(traceback.format_exc())


class RestaurantSessionManager:
    """
    Manages tracking sessions for multiple restaurants.
    """
    
    def __init__(self, db_conn_str):
        """
        Initialize the session manager.
        
        Args:
            db_conn_str: Database connection string
        """
        self.db_conn_str = db_conn_str
        self.active_sessions = {}
        self.lock = threading.RLock()
        self.logger = logging.getLogger("RestaurantSessionManager")
    
    def start_session(self, restaurant_id, config):
        """
        Start a tracking session for a restaurant.
        
        Args:
            restaurant_id: The ID of the restaurant
            config: Configuration dictionary
            
        Returns:
            bool: True if session started successfully
        """
        with self.lock:
            # Check if session already exists
            if restaurant_id in self.active_sessions:
                self.logger.info(f"Session already exists for restaurant {restaurant_id}")
                return True
            
            # Create and start a new session
            self.logger.info(f"Starting new session for restaurant {restaurant_id}")
            session = RestaurantSession(restaurant_id, config, self.db_conn_str)
            success = session.start()
            
            if success:
                self.active_sessions[restaurant_id] = session
                return True
            else:
                self.logger.error(f"Failed to start session for restaurant {restaurant_id}")
                return False
    
    def stop_session(self, restaurant_id):
        """
        Stop a restaurant tracking session.
        
        Args:
            restaurant_id: The ID of the restaurant
            
        Returns:
            bool: True if session was stopped
        """
        with self.lock:
            if restaurant_id not in self.active_sessions:
                self.logger.warning(f"No active session for restaurant {restaurant_id}")
                return False
            
            session = self.active_sessions[restaurant_id]
            session.stop()
            del self.active_sessions[restaurant_id]
            self.logger.info(f"Stopped session for restaurant {restaurant_id}")
            return True
    
    def stop_all_sessions(self):
        """Stop all active restaurant sessions."""
        with self.lock:
            restaurant_ids = list(self.active_sessions.keys())
            for restaurant_id in restaurant_ids:
                self.stop_session(restaurant_id)
            self.logger.info("All sessions stopped")
    
    def get_active_sessions(self):
        """
        Get a list of active restaurant session IDs.
        
        Returns:
            list: List of active restaurant IDs
        """
        with self.lock:
            return list(self.active_sessions.keys())


# import logging
# import threading
# import time
# import os
# import gc
# from camera import VideoProcessor
# from seat_tracker import SeatTracker

# logger = logging.getLogger(__name__)

# class RestaurantSession:
#     """
#     Manages a session for tracking seat occupancy in a specific restaurant.
#     Each session handles camera capture, seat detection, and database updates.
#     """
    
#     def __init__(self, restaurant_id, config, db_conn_str):
#         """
#         Initialize a restaurant session.
        
#         Args:
#             restaurant_id (int): Restaurant ID
#             config (dict): Restaurant configuration
#             db_conn_str (str): Database connection string
#         """
#         self.restaurant_id = restaurant_id
#         self.config = config
#         self.db_conn_str = db_conn_str
#         self.running = False
#         self.processing_thread = None
#         self.logger = logging.getLogger(f"RestaurantSession_{restaurant_id}")
#         self.video_processor = None
#         self.seat_tracker = None
#         self.frame_processed_count = 0  # Track number of frames processed
#         self.last_processed_time = 0    # Last time a frame was processed
#         self.lock = threading.Lock()    # Thread safety lock
        
#     def start(self):
#         """Start the restaurant session."""
#         with self.lock:
#             if self.running:
#                 self.logger.warning(f"Session already running for restaurant {self.restaurant_id}")
#                 return True
                
#             self.logger.info(f"Starting session for restaurant {self.restaurant_id}")
#             self.running = True
            
#             try:
#                 # Initialize video processor
#                 camera_url = self.config.get('IP_CAMERA_URL')
#                 self.logger.info(f"Using camera URL: {camera_url}")
                
#                 # Set frame size from config (default to 640x480)
#                 frame_size = self.config.get('FRAME_SIZE', (640, 480))
#                 process_every_n = self.config.get('PROCESS_EVERY_N_FRAMES', 30)
                
#                 self.video_processor = VideoProcessor(
#                     source=camera_url,
#                     process_every_n=process_every_n,
#                     resolution=frame_size
#                 )
                
#                 # Initialize seat tracker
#                 model_path = self.config.get('MODEL_PATH', 'best.pt')
#                 self.seat_tracker = SeatTracker(
#                     model_path=model_path,
#                     db_conn_str=self.db_conn_str,
#                     restaurant_id=self.restaurant_id,
#                     seats=self.config.get('SEATS', [])
#                 )
                
#                 # Start video processing
#                 self.video_processor.start()
                
#                 # Start processing thread
#                 self.processing_thread = threading.Thread(
#                     target=self._processing_loop,
#                     daemon=True,
#                     name=f"Processing_Thread_{self.restaurant_id}"
#                 )
#                 self.processing_thread.start()
                
#                 self.logger.info(f"Session started successfully for restaurant {self.restaurant_id}")
#                 return True
                
#             except Exception as e:
#                 self.logger.error(f"Error starting session: {e}")
#                 self.running = False
#                 return False
    
#     def stop(self):
#         """Stop the restaurant session."""
#         with self.lock:
#             if not self.running:
#                 self.logger.warning(f"Session already stopped for restaurant {self.restaurant_id}")
#                 return True
                
#             self.logger.info(f"Stopping session for restaurant {self.restaurant_id}")
#             self.running = False
            
#             try:
#                 # Wait for processing thread to stop
#                 if self.processing_thread:
#                     self.processing_thread.join(timeout=5.0)
                
#                 # Stop video processor
#                 if self.video_processor:
#                     self.video_processor.stop()
#                     self.video_processor = None
                
#                 # Clean up seat tracker
#                 if self.seat_tracker:
#                     self.seat_tracker = None
                
#                 # Force garbage collection
#                 gc.collect()
                
#                 self.logger.info(f"Session stopped successfully for restaurant {self.restaurant_id}")
#                 return True
                
#             except Exception as e:
#                 self.logger.error(f"Error stopping session: {e}")
#                 return False
    
#     def _processing_loop(self):
#         """Main processing loop for seat tracking."""
#         self.logger.info(f"Processing thread started for restaurant {self.restaurant_id}")
        
#         # Reset frame counter
#         self.frame_processed_count = 0
        
#         consecutive_errors = 0
#         max_consecutive_errors = 10
        
#         while self.running:
#             try:
#                 # Get latest frame from video processor
#                 frame = self.video_processor.get_frame() if self.video_processor else None
                
#                 if frame is not None:
#                     # Only process certain frames to reduce load
#                     if self.video_processor.should_process_frame():
#                         # Process frame with seat tracker
#                         self.seat_tracker.process_frame(frame)
                        
#                         # Update processing stats
#                         with self.lock:
#                             self.frame_processed_count += 1
#                             self.last_processed_time = time.time()
                        
#                         # Reset error counter on success
#                         consecutive_errors = 0
#                 else:
#                     # No frame available yet, wait a bit
#                     time.sleep(0.1)
#                     continue
                
#                 # Brief sleep to prevent high CPU usage
#                 time.sleep(0.01)
                
#             except Exception as e:
#                 consecutive_errors += 1
#                 self.logger.error(f"Error in processing loop: {e}")
                
#                 if consecutive_errors >= max_consecutive_errors:
#                     self.logger.error(f"Too many consecutive errors ({consecutive_errors}), stopping session")
#                     with self.lock:
#                         self.running = False
#                     break
                
#                 # Sleep to prevent rapid error cycling
#                 time.sleep(1.0)
        
#         self.logger.info(f"Processing thread stopped for restaurant {self.restaurant_id}")
    
#     def is_processing(self):
#         """
#         Check if this session is actively processing frames.
        
#         Returns:
#             bool: True if at least one frame has been processed, False otherwise
#         """
#         with self.lock:
#             # Consider processing active if at least one frame has been processed
#             # and the last processing happened within the last 10 seconds
#             current_time = time.time()
#             return (self.frame_processed_count > 0 and 
#                     self.last_processed_time > 0 and 
#                     current_time - self.last_processed_time < 10)

# class RestaurantSessionManager:
#     """
#     Manages multiple restaurant sessions.
#     Handles starting, stopping, and checking status of sessions.
#     """
    
#     def __init__(self, db_conn_str):
#         """
#         Initialize the RestaurantSessionManager.
        
#         Args:
#             db_conn_str (str): Database connection string
#         """
#         self.db_conn_str = db_conn_str
#         self.sessions = {}
#         self.logger = logging.getLogger(__name__)
#         self.lock = threading.Lock()  # For thread safety
    
#     def start_session(self, restaurant_id, config):
#         """
#         Start a new session for a restaurant.
        
#         Args:
#             restaurant_id (int): The restaurant ID
#             config (dict): Restaurant configuration
            
#         Returns:
#             bool: True if session started successfully, False otherwise
#         """
#         with self.lock:
#             # Check if session already exists
#             if restaurant_id in self.sessions:
#                 session = self.sessions[restaurant_id]
#                 if session.running:
#                     self.logger.info(f"Session already running for restaurant {restaurant_id}")
#                     return True
                
#                 # Restart existing session
#                 self.logger.info(f"Restarting existing session for restaurant {restaurant_id}")
#                 return session.start()
            
#             # Create and start new session
#             self.logger.info(f"Creating new session for restaurant {restaurant_id}")
#             session = RestaurantSession(restaurant_id, config, self.db_conn_str)
#             success = session.start()
            
#             if success:
#                 self.sessions[restaurant_id] = session
                
#             return success
    
#     def stop_session(self, restaurant_id):
#         """
#         Stop a session for a restaurant.
        
#         Args:
#             restaurant_id (int): The restaurant ID
            
#         Returns:
#             bool: True if session stopped successfully, False otherwise
#         """
#         with self.lock:
#             if restaurant_id not in self.sessions:
#                 self.logger.warning(f"No session exists for restaurant {restaurant_id}")
#                 return True
            
#             # Stop the session
#             session = self.sessions[restaurant_id]
#             success = session.stop()
            
#             if success:
#                 del self.sessions[restaurant_id]
                
#             return success
    
#     def stop_all_sessions(self):
#         """Stop all active sessions."""
#         with self.lock:
#             for restaurant_id in list(self.sessions.keys()):
#                 self.logger.info(f"Stopping session for restaurant {restaurant_id}")
#                 self.stop_session(restaurant_id)
    
#     def get_active_sessions(self):
#         """
#         Get a list of active restaurant session IDs.
        
#         Returns:
#             list: List of restaurant IDs with active sessions
#         """
#         with self.lock:
#             return [
#                 restaurant_id for restaurant_id, session in self.sessions.items()
#                 if session and session.running
#             ]
    
#     def is_session_active(self, restaurant_id):
#         """
#         Check if a session is active for a restaurant.
        
#         Args:
#             restaurant_id (int): The restaurant ID
            
#         Returns:
#             bool: True if session is active, False otherwise
#         """
#         with self.lock:
#             return (restaurant_id in self.sessions and 
#                     self.sessions[restaurant_id] and 
#                     self.sessions[restaurant_id].running)
    
#     def is_session_processing(self, restaurant_id):
#         """
#         Check if a session is actively processing frames.
        
#         Args:
#             restaurant_id (int): The restaurant ID
            
#         Returns:
#             bool: True if session is processing frames, False otherwise
#         """
#         with self.lock:
#             if (restaurant_id in self.sessions and 
#                 self.sessions[restaurant_id] and 
#                 self.sessions[restaurant_id].running):
#                 return self.sessions[restaurant_id].is_processing()
#             return False