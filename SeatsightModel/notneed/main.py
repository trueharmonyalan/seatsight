#version 1
# from config import ConfigManager
# from logger import setup_logging
# from camera import CaptureThread
# from seat_tracker import SeatTracker
# from api_client import APIClient
# import threading
# import time

# def main():
#     logger = setup_logging()
#     config = ConfigManager()
    
#     # Fetch camera URL
#     owner_id = config.get("RESTAURANT_ID")
#     ip_camera_url = config.get_camera_url(owner_id)

#     # Initialize components
#     seat_tracker = SeatTracker(config)
#     api_client = APIClient(config)
    
#     # Start capturing frames
#     capture_thread = CaptureThread(ip_camera_url, config)
#     capture_thread.start()

#     logger.info("Seat tracking system started.")

#     try:
#         while True:
#             if not capture_thread.frame_queue.empty():
#                 frame = capture_thread.frame_queue.get()
#                 seat_tracker.update_detections(frame)
#                 # api_client.publish_status(seat_tracker)
#             time.sleep(0.5)  # Adjust based on processing speed
#     except KeyboardInterrupt:
#         logger.info("Shutting down...")
#         capture_thread.stop()
#         capture_thread.join()

# if __name__ == "__main__":
#     main()




#version2
# from config import ConfigManager
# from logger import setup_logging
# from camera import CaptureThread
# from seat_tracker import SeatTracker
# import cv2
# import time
# import queue

# def main():
#     logger = setup_logging()
#     config = ConfigManager()
    
#     # Get camera URL
#     try:
#         owner_id = config.get("RESTAURANT_ID")
#         ip_camera_url = config.get_camera_url(owner_id)
#         logger.info(f"Using camera URL: {ip_camera_url}")
#     except Exception as e:
#         logger.error(f"Failed to get camera URL: {e}")
#         ip_camera_url = config.get("FALLBACK_CAMERA_URL")
#         logger.info(f"Using fallback camera URL: {ip_camera_url}")
    
#     # Initialize seat tracker
#     seat_tracker = SeatTracker(config)
    
#     # Start capturing frames
#     capture_thread = CaptureThread(ip_camera_url, config)
#     capture_thread.start()

#     logger.info("Seat tracking system started.")
    
#     try:
#         while True:
#             try:
#                 # Process frames when available
#                 if not capture_thread.frame_queue.empty():
#                     frame = capture_thread.frame_queue.get(timeout=0.1)
#                     seat_tracker.update_detections(frame)
                    
#                     # Print status to console every 2 seconds
#                     current_time = time.time()
#                     if not hasattr(main, 'last_status_time') or current_time - main.last_status_time >= 2:
#                         status = seat_tracker.get_status()
#                         logger.info(f"Current seat status: Occupied {status['occupied']}, Vacant {status['vacant']}")
#                         main.last_status_time = current_time
                    
#             except queue.Empty:
#                 pass  # No frames available
                
#             # Break on key press
#             if cv2.waitKey(1) & 0xFF == ord('q'):
#                 break
                
#             # Short sleep to prevent CPU overload
#             time.sleep(0.01)
            
#     except KeyboardInterrupt:
#         logger.info("Shutting down...")
#     finally:
#         capture_thread.stop()
#         capture_thread.join()
#         seat_tracker.close()
#         logger.info("System shutdown complete.")

# if __name__ == "__main__":
#     main()

# version 3
# from config import ConfigManager
# from logger import setup_logging
# from camera import CaptureThread
# from seat_tracker import SeatTracker
# import cv2
# import time
# import queue

# def main():
#     logger = setup_logging()
#     config = ConfigManager()
    
#     # Get camera URL
#     try:
#         owner_id = config.get("RESTAURANT_ID")
#         ip_camera_url = config.get_camera_url(owner_id)
#         logger.info(f"Using camera URL: {ip_camera_url}")
#     except Exception as e:
#         logger.error(f"Failed to get camera URL: {e}")
#         ip_camera_url = config.get("FALLBACK_CAMERA_URL")
#         logger.info(f"Using fallback camera URL: {ip_camera_url}")
    
#     # Initialize seat tracker
#     seat_tracker = SeatTracker(config)
    
#     # Start capturing frames
#     capture_thread = CaptureThread(ip_camera_url, config)
#     capture_thread.start()

#     logger.info("Seat tracking system started.")
    
#     try:
#         while True:
#             try:
#                 # Process frames when available
#                 if not capture_thread.frame_queue.empty():
#                     frame = capture_thread.frame_queue.get(timeout=0.1)
#                     seat_tracker.update_detections(frame)
                    
#                     # Print status to console every 2 seconds
#                     current_time = time.time()
#                     if not hasattr(main, 'last_status_time') or current_time - main.last_status_time >= 2:
#                         statuses = seat_tracker.get_status()  # statuses is now a list of seat records
#                         # Calculate total occupied and vacant seats
#                         occupied_count = sum(1 for seat in statuses if seat['status'] == 'occupied')
#                         vacant_count = sum(1 for seat in statuses if seat['status'] == 'vacant')
#                         logger.info(f"Current seat status: Occupied {occupied_count}, Vacant {vacant_count}")
#                         main.last_status_time = current_time
#             except queue.Empty:
#                 pass  # No frames available
                
#             # Break on key press
#             if cv2.waitKey(1) & 0xFF == ord('q'):
#                 break
                
#             # Short sleep to prevent CPU overload
#             time.sleep(0.01)
            
#     except KeyboardInterrupt:
#         logger.info("Shutting down...")
#     finally:
#         capture_thread.stop()
#         capture_thread.join()
#         seat_tracker.close()
#         logger.info("System shutdown complete.")

# if __name__ == "__main__":
#     main()

#version 4
# from config import ConfigManager
# from logger import setup_logging
# from camera import CaptureThread
# from seat_tracker import SeatTracker
# import time
# import queue
# import db_manager

# def main():
#     logger = setup_logging()
#     config = ConfigManager()
    
#     # Get camera URL
#     try:
#         owner_id = config.get("RESTAURANT_ID")
#         ip_camera_url = config.get_camera_url(owner_id)
#         logger.info(f"Using camera URL: {ip_camera_url}")
#     except Exception as e:
#         logger.error(f"Failed to get camera URL: {e}")
#         ip_camera_url = config.get("FALLBACK_CAMERA_URL")
#         logger.info(f"Using fallback camera URL: {ip_camera_url}")
    
#     # Initialize seat tracker
#     seat_tracker = SeatTracker(config)
    
#     # Start capturing frames
#     capture_thread = CaptureThread(ip_camera_url, config)
#     capture_thread.start()

#     logger.info("Seat tracking system started.")
    
#     # Retrieve the restaurant id and database connection string from the config.
#     restaurant_id = config.get("RESTAURANT_ID")
#     db_conn_str = config.get("DATABASE_URL")
    
#     try:
#         while True:
#             try:
#                 # Process frames when available
#                 if not capture_thread.frame_queue.empty():
#                     frame = capture_thread.frame_queue.get(timeout=0.1)
#                     seat_tracker.update_detections(frame)
                    
#                     # Update the PostgreSQL database with the latest seat status.
#                     statuses = seat_tracker.get_status()  # Each record includes seatNumber, status, centroid, etc.
#                     db_manager.update_seats_status(statuses, restaurant_id, db_conn_str)
                    
#                     # Print simple counts to the console every 2 seconds.
#                     current_time = time.time()
#                     if not hasattr(main, 'last_status_time') or current_time - main.last_status_time >= 2:
#                         occupied_count = sum(1 for seat in statuses if seat['status'] == 'occupied')
#                         vacant_count = sum(1 for seat in statuses if seat['status'] == 'vacant')
#                         logger.info(f"Current seat status - Occupied: {occupied_count}, Vacant: {vacant_count}")
#                         main.last_status_time = current_time
#             except queue.Empty:
#                 pass  # No frames available
                
#             # Press "q" to exit.
#             import cv2
#             if cv2.waitKey(1) & 0xFF == ord('q'):
#                 break
                
#             time.sleep(0.01)
            
#     except KeyboardInterrupt:
#         logger.info("Shutting down...")
#     finally:
#         capture_thread.stop()
#         capture_thread.join()
#         seat_tracker.close()
#         logger.info("System shutdown complete.")

# if __name__ == "__main__":
#     main()

# version 5
# from config import ConfigManager
# from logger import setup_logging
# from camera import CaptureThread
# from seat_tracker import SeatTracker
# import time
# import queue
# import db_manager

# def main():
#     logger = setup_logging()
#     config = ConfigManager()
    
#     # Get camera URL
#     try:
#         owner_id = config.get("RESTAURANT_ID")
#         ip_camera_url = config.get_camera_url(owner_id)
#         logger.info(f"Using camera URL: {ip_camera_url}")
#     except Exception as e:
#         logger.error(f"Failed to get camera URL: {e}")
#         ip_camera_url = config.get("FALLBACK_CAMERA_URL")
#         logger.info(f"Using fallback camera URL: {ip_camera_url}")
    
#     # Retrieve the restaurant id and database connection string from the config.
#     restaurant_id = config.get("RESTAURANT_ID")
#     db_conn_str = config.get("DATABASE_URL")
    
#     # Fetch seat_limit from the database using the restaurant id.
#     seat_limit_from_db = db_manager.fetch_seat_count(restaurant_id, db_conn_str)
#     logger.info(f"Updating SEAT_LIMIT configuration to: {seat_limit_from_db}")
#     # Update the configuration with the seat_limit fetched from the database.
#     config.config["DEFAULT"]["SEAT_LIMIT"] = str(seat_limit_from_db)
    
#     # Initialize seat tracker with the updated configuration.
#     seat_tracker = SeatTracker(config)
    
#     # Start capturing frames.
#     capture_thread = CaptureThread(ip_camera_url, config)
#     capture_thread.start()
    
#     logger.info("Seat tracking system started.")
    
#     try:
#         while True:
#             try:
#                 # Process frames when available.
#                 if not capture_thread.frame_queue.empty():
#                     frame = capture_thread.frame_queue.get(timeout=0.1)
#                     seat_tracker.update_detections(frame)
                    
#                     # Update the PostgreSQL database with the latest seat status.
#                     statuses = seat_tracker.get_status()  # Each record includes seatNumber, status, centroid, etc.
#                     db_manager.update_seats_status(statuses, restaurant_id, db_conn_str)
                    
#                     # Print simple counts to the console every 2 seconds.
#                     current_time = time.time()
#                     if not hasattr(main, 'last_status_time') or current_time - main.last_status_time >= 2:
#                         occupied_count = sum(1 for seat in statuses if seat['status'] == 'occupied')
#                         vacant_count = sum(1 for seat in statuses if seat['status'] == 'vacant')
#                         logger.info(f"Current seat status - Occupied: {occupied_count}, Vacant: {vacant_count}")
#                         main.last_status_time = current_time
#             except queue.Empty:
#                 pass  # No frames available
                
#             # Press "q" to exit.
#             import cv2
#             if cv2.waitKey(1) & 0xFF == ord('q'):
#                 break
                
#             time.sleep(0.01)
            
#     except KeyboardInterrupt:
#         logger.info("Shutting down...")
#     finally:
#         capture_thread.stop()
#         capture_thread.join()
#         seat_tracker.close()
#         logger.info("System shutdown complete.")

# if __name__ == "__main__":
#     main()

#version 6
# Add these imports at the top
from app_server import start_server, app

# Update the main function
def main():
    """Main entry point for the SeatSight application."""
    logger.info("Starting SeatSight system...")
    
    # Get configuration from environment variables or use defaults
    base_api_endpoint = os.environ.get('BASE_API_ENDPOINT', BASE_API_ENDPOINT)
    db_conn_str = os.environ.get('DATABASE_URL', DATABASE_URL)
    polling_interval = int(os.environ.get('POLLING_INTERVAL', POLLING_INTERVAL))
    
    # Get API server configuration
    api_host = os.environ.get('API_HOST', '0.0.0.0')
    api_port = int(os.environ.get('API_PORT', 3003))
    
    logger.info(f"Using database: {db_conn_str}")
    logger.info(f"API server will listen on: {api_host}:{api_port}")
    
    # Initialize the controller
    controller = SeatSightController(base_api_endpoint, db_conn_str, polling_interval)
    signal_handler.controller = controller  # Store for signal handler
    
    # Set the controller reference in the Flask app
    app.controller = controller
    
    # Start the controller
    controller.start()
    
    # Start the API server
    api_thread = start_server(host=api_host, port=api_port)
    
    # Keep the main thread alive
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        logger.info("Keyboard interrupt received")
    finally:
        controller.stop()
        logger.info("SeatSight system shutdown complete")