import requests
import threading
import time
import logging
import json
import os

logger = logging.getLogger(__name__)

class RestaurantPoller:
    """
    A background service that periodically checks for restaurant IDs to track
    from either a file, environment variable, or manual input.
    """
    
    def __init__(self, base_api_endpoint, polling_interval=60, callback=None):
        """
        Initialize the RestaurantPoller.
        
        Args:
            base_api_endpoint (str): The base API URL (e.g., "http://localhost:3001/api/restaurants")
            polling_interval (int): How often to check for ID changes in seconds
            callback (function): Function to call when new restaurant data is detected
        """
        self.base_api_endpoint = base_api_endpoint
        self.polling_interval = polling_interval
        self.callback = callback
        self.current_restaurant_id = None
        self.running = False
        self.polling_thread = None
        self.logger = logging.getLogger(__name__)
        
        # File to store/read the current restaurant ID
        self.id_file_path = "current_restaurant_id.txt"
    
    def start(self):
        """Start the polling service in a background thread."""
        if self.running:
            self.logger.warning("RestaurantPoller already running")
            return
            
        self.running = True
        self.polling_thread = threading.Thread(target=self._polling_loop, daemon=True)
        self.polling_thread.start()
        self.logger.info("RestaurantPoller started")
    
    def stop(self):
        """Stop the polling service."""
        self.running = False
        if self.polling_thread:
            self.polling_thread.join(timeout=2.0)
        self.logger.info("RestaurantPoller stopped")
    
    def _polling_loop(self):
        """Main polling loop that runs in a background thread."""
        while self.running:
            try:
                # Check for restaurant ID from various sources
                self._check_for_restaurant_id_change()
            except Exception as e:
                self.logger.error(f"Error checking for restaurant ID changes: {e}")
            
            # Wait for next polling interval
            time.sleep(self.polling_interval)
    
    def _check_for_restaurant_id_change(self):
        """Check for changes in the restaurant ID from various sources."""
        # Priority for ID source:
        # 1. Environment variable
        # 2. File on disk
        # 3. Keep current ID if already set
        
        # Check environment variable first
        env_id = os.environ.get("SEATSIGHT_RESTAURANT_ID")
        if env_id and env_id.isdigit():
            new_id = int(env_id)
            if new_id != self.current_restaurant_id:
                self.logger.info(f"Restaurant ID from environment: {new_id}")
                self._process_new_restaurant_id(new_id)
                return
        
        # Then check the file
        try:
            if os.path.exists(self.id_file_path):
                with open(self.id_file_path, 'r') as f:
                    content = f.read().strip()
                    if content and content.isdigit():
                        new_id = int(content)
                        if new_id != self.current_restaurant_id:
                            self.logger.info(f"Restaurant ID from file: {new_id}")
                            self._process_new_restaurant_id(new_id)
                            return
        except Exception as e:
            self.logger.error(f"Error reading restaurant ID file: {e}")
    
    def _process_new_restaurant_id(self, restaurant_id):
        """
        Process a newly detected restaurant ID.
        
        Args:
            restaurant_id: The new restaurant ID to track
        """
        if restaurant_id == self.current_restaurant_id:
            return
            
        self.logger.info(f"New restaurant ID to track: {restaurant_id}")
        
        # Fetch restaurant data from the API
        restaurant_data = self._fetch_restaurant_data(restaurant_id)
        
        if restaurant_data:
            # Update current ID and notify callback
            self.current_restaurant_id = restaurant_id
            if self.callback:
                self.callback(restaurant_id, restaurant_data)
        else:
            self.logger.warning(f"Could not fetch data for restaurant ID: {restaurant_id}")
    
    def _fetch_restaurant_data(self, restaurant_id):
        """
        Fetch restaurant data using ID from the API.
        
        Args:
            restaurant_id: The restaurant ID to fetch
            
        Returns:
            dict: Restaurant data or None if fetch fails
        """
        try:
            # Construct the restaurant-specific API endpoint
            endpoint = f"{self.base_api_endpoint}/get-restaurant-id/{restaurant_id}"
            
            self.logger.info(f"Fetching restaurant data from: {endpoint}")
            
            response = requests.get(endpoint, timeout=10)
            response.raise_for_status()
            
            api_data = response.json()
            self.logger.debug(f"API response: {api_data}")
            
            # The API gives us the ID, but we need to query the database for actual details
            # So we'll just return a minimal data structure here that will trigger
            # the database queries in DynamicConfigManager
            return {
                'id': restaurant_id,
                'need_db_data': True  # Flag to indicate we need to fetch more data from DB
            }
                
        except requests.exceptions.RequestException as e:
            self.logger.warning(f"Failed to fetch restaurant data: {e}")
            return None
    
    def set_restaurant_id(self, restaurant_id):
        """
        Manually set a restaurant ID to track and save it to the file.
        
        Args:
            restaurant_id: The restaurant ID to track
        """
        try:
            # Save to file for persistence
            with open(self.id_file_path, 'w') as f:
                f.write(str(restaurant_id))
            
            self.logger.info(f"Saved restaurant ID {restaurant_id} to file")
            
            # Process the new ID immediately
            self._process_new_restaurant_id(restaurant_id)
            
            return True
        except Exception as e:
            self.logger.error(f"Error saving restaurant ID to file: {e}")
            return False