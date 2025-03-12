import logging
import psycopg2
import json

logger = logging.getLogger(__name__)

class DynamicConfigManager:
    """
    Manages dynamic configuration for restaurants.
    Loads configuration from database based on restaurant ID.
    """
    
    def __init__(self, db_conn_str):
        """
        Initialize the DynamicConfigManager.
        
        Args:
            db_conn_str (str): Database connection string
        """
        self.db_conn_str = db_conn_str
        self.active_restaurant_id = None
        self.configs = {}  # Cache for restaurant configs
        self.logger = logging.getLogger(__name__)
    
    def load_restaurant_config(self, restaurant_id):
        """
        Load configuration for a specific restaurant from the database.
        
        Args:
            restaurant_id: The ID of the restaurant
            
        Returns:
            dict: Restaurant configuration or None if not found
        """
        # Check cache first
        if restaurant_id in self.configs:
            self.logger.info(f"Using cached config for restaurant {restaurant_id}")
            return self.configs[restaurant_id]
            
        self.logger.info(f"Loading configuration for restaurant {restaurant_id}")
        
        try:
            # Connect to the database
            conn = psycopg2.connect(self.db_conn_str)
            cur = conn.cursor()
            
            # Get restaurant details
            cur.execute("SELECT id, name, ip_camera_url FROM restaurants WHERE id = %s", (restaurant_id,))
            restaurant = cur.fetchone()
            
            if not restaurant:
                self.logger.warning(f"Restaurant {restaurant_id} not found in database")
                return None
                
            restaurant_id, name, ip_camera_url = restaurant
            
            # Get seat configurations
            cur.execute("SELECT id, seat_number, is_booked, status, pos_x, pos_y FROM seats WHERE restaurant_id = %s", (restaurant_id,))
            seats = cur.fetchall()
            
            # Build configuration dictionary
            config = {
                'RESTAURANT_ID': restaurant_id,
                'RESTAURANT_NAME': name,
                'IP_CAMERA_URL': ip_camera_url,
                'FRAME_SIZE': (640, 480),  # Default frame size
                'PROCESS_EVERY_N_FRAMES': 30,  # Process every 30 frames
                'MODEL_PATH': 'best.pt',  # Default model path
                'SEAT_LIMIT': len(seats),
                'SEATS': []
            }
            
            # Add seat configurations
            for seat in seats:
                seat_id, seat_number, is_booked, status, pos_x, pos_y = seat
                config['SEATS'].append({
                    'id': seat_id,
                    'seat_number': seat_number,
                    'is_booked': is_booked,
                    'status': status,
                    'position': (pos_x, pos_y)
                })
            
            # Close database connection
            cur.close()
            conn.close()
            
            # Cache and return the configuration
            self.configs[restaurant_id] = config
            return config
            
        except Exception as e:
            self.logger.error(f"Error loading restaurant configuration: {e}")
            return None
    
    def set_active_restaurant(self, restaurant_id):
        """Set the currently active restaurant ID."""
        self.active_restaurant_id = restaurant_id
        self.logger.info(f"Active restaurant set to {restaurant_id}")
    
    def get_active_restaurant_id(self):
        """Get the currently active restaurant ID."""
        return self.active_restaurant_id