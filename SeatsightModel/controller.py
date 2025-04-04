# import logging
# import threading
# import time
# import sys
# from restaurant_session import RestaurantSessionManager
# from dynamic_config import DynamicConfigManager

# # Set up logging
# logging.basicConfig(
#     level=logging.INFO,
#     format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
#     handlers=[
#         logging.FileHandler('seatsight_controller.log'),
#         logging.StreamHandler(sys.stdout)
#     ]
# )

# logger = logging.getLogger("SeatSightController")

# class SeatSightController:
#     """
#     Main controller for the SeatSight system.
#     Coordinates API polling, configuration management, and restaurant sessions.
#     """
    
#     def __init__(self, base_api_endpoint, db_conn_str, polling_interval=60):
#         """
#         Initialize the SeatSight controller.
        
#         Args:
#             base_api_endpoint (str): Base API endpoint for restaurant data
#             db_conn_str (str): Database connection string
#             polling_interval (int): How often to poll in seconds
#         """
#         self.base_api_endpoint = base_api_endpoint
#         self.db_conn_str = db_conn_str
#         self.polling_interval = polling_interval
#         self.running = False
        
#         # Create component instances
#         self.config_manager = DynamicConfigManager(db_conn_str)
#         self.session_manager = RestaurantSessionManager(db_conn_str)
    
#     def start(self):
#         """Start the SeatSight controller."""
#         if self.running:
#             logger.warning("Controller already running")
#             return
        
#         logger.info("Starting SeatSight controller")
#         self.running = True
#         logger.info("SeatSight controller started")
    
#     def stop(self):
#         """Stop the SeatSight controller and all managed services."""
#         if not self.running:
#             return
        
#         logger.info("Stopping SeatSight controller")
#         self.running = False
        
#         # Stop all restaurant sessions
#         self.session_manager.stop_all_sessions()
        
#         logger.info("SeatSight controller stopped")
    
#     def process_restaurant_id(self, restaurant_id):
#         """
#         Process a new restaurant ID received from the API.
        
#         Args:
#             restaurant_id: The restaurant ID to process
            
#         Returns:
#             bool: True if processing started successfully, False otherwise
#         """
#         try:
#             logger.info(f"Processing restaurant ID: {restaurant_id}")
            
#             # Load restaurant configuration
#             config = self.config_manager.load_restaurant_config(restaurant_id)
#             if not config:
#                 logger.error(f"Unable to load configuration for restaurant {restaurant_id}")
#                 return False
            
#             # Check if restaurant has a valid camera URL
#             if not config.get('IP_CAMERA_URL'):
#                 logger.error(f"Restaurant {restaurant_id} has no camera URL. Skipping.")
#                 return False
            
#             # Set as active restaurant in the config manager
#             self.config_manager.set_active_restaurant(restaurant_id)
            
#             # Stop any previously running sessions for this restaurant
#             if restaurant_id in self.session_manager.get_active_sessions():
#                 logger.info(f"Stopping existing session for restaurant {restaurant_id}")
#                 self.session_manager.stop_session(restaurant_id)
            
#             # Start a tracking session for this restaurant
#             success = self.session_manager.start_session(restaurant_id, config)
#             if success:
#                 logger.info(f"Successfully started tracking for restaurant {restaurant_id}")
#                 return True
#             else:
#                 logger.error(f"Failed to start tracking for restaurant {restaurant_id}")
#                 return False
                
#         except Exception as e:
#             logger.error(f"Error processing restaurant ID {restaurant_id}: {e}")
#             return False

#     def get_active_sessions(self):
#         """
#         Get a list of active restaurant session IDs.
        
#         Returns:
#             list: List of active restaurant IDs
#         """
#         return self.session_manager.get_active_sessions()


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
                return True
            else:
                logger.error(f"Failed to start tracking for restaurant {restaurant_id}")
                return False
                
        except Exception as e:
            logger.error(f"Error processing restaurant ID {restaurant_id}: {e}")
            return False

    def get_active_sessions(self):
        """
        Get a list of active restaurant session IDs.
        
        Returns:
            list: List of active restaurant IDs
        """
        return self.session_manager.get_active_sessions()