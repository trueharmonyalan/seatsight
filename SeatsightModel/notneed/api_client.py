# import requests
# import logging

# class APIClient:
#     def __init__(self, config):
#         self.config = config
#         self.restaurant_id = config.get('RESTAURANT_ID')
#         self.base_url = config.get('REST_API_BASE_URL')
#         self.logger = logging.getLogger(__name__)

#     # def publish_status(self, tracker):
#     #     statuses = []
#     #     with tracker.lock:
#     #         for seat_id, seat in tracker.seat_zones.items():
#     #             statuses.append({"seat_id": seat_id, "status": seat["status"]})
#     #     payload = {"restaurant_id": self.restaurant_id, "statuses": statuses}
#     #     try:
#     #         response = requests.post(f"{self.base_url}/seats_status", json=payload)
#     #         if response.status_code != 200:
#     #             self.logger.error(f"API status update failed: {response.text}")
#     #     except Exception as e:
#     #         self.logger.error(f"API error: {e}")

# def fetch_camera_url(config, owner_id):
#     try:
#         response = requests.get(f"{config.get('REST_API_BASE_URL')}/api/restaurants/ip-url/{owner_id}")
#         data = response.json()
#         print(data)
#         return data.get('ip_camera_url', config.get('FALLBACK_CAMERA_URL'))
#     except:
#         return config.get('FALLBACK_CAMERA_URL')

#version 2
# import requests
# import logging

# class APIClient:
#     def __init__(self, config):
#         self.config = config
#         self.restaurant_id = config.get('RESTAURANT_ID')
#         # Optionally, you can add OWNER_ID in your config. If not provided, we'll use restaurant_id.
#         try:
#             self.owner_id = config.get('OWNER_ID')
#         except Exception:
#             self.owner_id = self.restaurant_id
#         self.base_url = config.get('REST_API_BASE_URL')
#         self.logger = logging.getLogger(__name__)

#     def update_seat_status(self, seat_status):
#         """
#         Update seat statuses based on the detected deep model output.
#         The input 'seat_status' should be a list of dictionaries with keys 'seatNumber' and 'status'.
#         This method calls the API endpoint: POST /seat_status/:owner_id.
#         Example payload:
#             {
#                 "statuses": [
#                     {"seat_number": 1, "status": "vacant"},
#                     {"seat_number": 2, "status": "occupied"},
#                     ...
#                 ]
#             }
#         """
#         try:
#             url = f"{self.base_url}/seat_status/{self.owner_id}"
#             payload = {
#                 "statuses": [
#                     {
#                         "seat_number": seat["seatNumber"],
#                         "status": seat["status"]
#                     }
#                     for seat in seat_status
#                 ]
#             }
#             response = requests.post(url, json=payload)
#             if response.status_code == 200:
#                 self.logger.info("Seat statuses updated successfully.")
#             else:
#                 self.logger.error(f"Failed to update seat statuses: {response.text}")
#         except Exception as e:
#             self.logger.error(f"Error updating seat statuses: {e}")

# def fetch_camera_url(config, owner_id):
#     """
#     Fetch the IP camera URL from the web configuration service.
#     """
#     try:
#         url = f"{config.get('REST_API_BASE_URL')}/api/restaurants/ip-url/{owner_id}"
#         response = requests.get(url)
#         data = response.json()
#         print(data)
#         return data.get('ip_camera_url', config.get('FALLBACK_CAMERA_URL'))
#     except Exception as e:
#         logging.getLogger(__name__).error(f"Error fetching camera URL: {e}")
#         return config.get('FALLBACK_CAMERA_URL')

# version 3
# import requests
# import logging

# class APIClient:
#     def __init__(self, config):
#         self.config = config
#         self.restaurant_id = config.get('RESTAURANT_ID')
#         try:
#             self.owner_id = config.get('OWNER_ID')
#         except Exception:
#             self.owner_id = self.restaurant_id
#         self.base_url = config.get('REST_API_BASE_URL')
#         self.logger = logging.getLogger(__name__)

#     def update_seat_status(self, seat_status):
#         """
#         Update seat statuses based on the detected deep model output.
#         The input 'seat_status' should be a list of dictionaries with keys 'seatNumber' and 'status'.
#         This method calls the API endpoint: POST /seat_status/:owner_id.
#         Example payload:
#             {
#                 "statuses": [
#                     {"seat_number": 1, "status": "vacant"},
#                     {"seat_number": 2, "status": "occupied"},
#                     ...
#                 ]
#             }
#         """
#         try:
#             url = f"{self.base_url}/seat_status/{self.owner_id}"
#             payload = {
#                 "statuses": [
#                     {
#                         "seat_number": seat["seatNumber"],
#                         "status": seat["status"]
#                     }
#                     for seat in seat_status
#                 ]
#             }
#             response = requests.post(url, json=payload)
#             if response.status_code == 200:
#                 self.logger.info("Seat statuses updated successfully.")
#             else:
#                 self.logger.error(f"Failed to update seat statuses: {response.text}")
#         except Exception as e:
#             self.logger.error(f"Error updating seat statuses: {e}")

# def fetch_camera_url(config, owner_id):
#     """
#     Fetch the IP camera URL from the web configuration service.
#     If the returned data is a list, this function takes the first element.
#     """
#     try:
#         url = f"{config.get('REST_API_BASE_URL')}/api/restaurants/ip-url/{owner_id}"
#         response = requests.get(url)
#         data = response.json()
#         print(data)
#         if isinstance(data, list) and len(data) > 0:
#             data = data[0]
#         return data.get('ip_camera_url', config.get('FALLBACK_CAMERA_URL'))
#     except Exception as e:
#         logging.getLogger(__name__).error(f"Error fetching camera URL: {e}")
#         return config.get('FALLBACK_CAMERA_URL')

# version 4
# import requests
# import logging

# class APIClient:
#     def __init__(self, config):
#         self.config = config
#         # Use restaurant_id consistently
#         self.restaurant_id = config.get('RESTAURANT_ID')
#         self.base_url = config.get('REST_API_BASE_URL')
#         self.logger = logging.getLogger(__name__)

#     def update_seat_status(self, seat_status):
#         """
#         Update seat statuses based on the detected deep model output.
#         The input 'seat_status' should be a list of dictionaries with keys 'seatNumber' and 'status'.
#         This method calls the API endpoint: POST /seat_status/<restaurant_id>.
#         Example payload:
#             {
#                 "statuses": [
#                     {"seat_number": 1, "status": "vacant"},
#                     {"seat_number": 2, "status": "occupied"},
#                     ...
#                 ]
#             }
#         """
#         try:
#             url = f"{self.base_url}/seat_status/{self.restaurant_id}"
#             payload = {
#                 "statuses": [
#                     {
#                         "seat_number": seat["seatNumber"],
#                         "status": seat["status"]
#                     }
#                     for seat in seat_status
#                 ]
#             }
#             response = requests.post(url, json=payload)
#             if response.status_code == 200:
#                 self.logger.info("Seat statuses updated successfully.")
#             else:
#                 self.logger.error(f"Failed to update seat statuses: {response.text}")
#         except Exception as e:
#             self.logger.error(f"Error updating seat statuses: {e}")

# def fetch_camera_url(config, restaurant_id):
#     """
#     Fetch the IP camera URL from the web configuration service using restaurant_id.
#     If the returned data is a list, take the first element.
#     """
#     try:
#         url = f"{config.get('REST_API_BASE_URL')}/api/restaurants/ip-url/{restaurant_id}"
#         response = requests.get(url)
#         data = response.json()
#         print(data)
#         if isinstance(data, list) and len(data) > 0:
#             data = data[0]
#         return data.get('ip_camera_url', config.get('FALLBACK_CAMERA_URL'))
#     except Exception as e:
#         print("failed to fetch ip-url from the api")
#         logging.getLogger(__name__).error(f"Error fetching camera URL: {e}")
#         return config.get('FALLBACK_CAMERA_URL')

#version 5
import requests
import logging

class APIClient:
    def __init__(self, config):
        self.config = config
        self.restaurant_id = config.get('RESTAURANT_ID')
        self.base_url = config.get('REST_API_BASE_URL')
        self.logger = logging.getLogger(__name__)

    

def fetch_camera_url(config, restaurant_id):
    """
    Fetch the IP camera URL from the web configuration service using restaurant_id.
    """
    try:
        url = f"{config.get('REST_API_BASE_URL')}/api/restaurants/get-restaurant-id/{restaurant_id}"
        response = requests.get(url)
        data = response.json()
        print(data)
        if isinstance(data, list) and len(data) > 0:
            data = data[0]
        return data.get('ip_camera_url', config.get('FALLBACK_CAMERA_URL'))
    except Exception as e:
        print("failed to fetch ip-url from the api")
        logging.getLogger(__name__).error(f"Error fetching camera URL: {e}")
        return config.get('FALLBACK_CAMERA_URL')