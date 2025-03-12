# import configparser
# import os
# import logging

# class ConfigManager:
#     def __init__(self, config_path='config.ini'):
#         self.config = configparser.ConfigParser()
#         self.logger = logging.getLogger(__name__)
#         self.defaults = {
#             'RESTAURANT_ID': 1,
#             'REST_API_BASE_URL': 'http://localhost:3001',
#             'FALLBACK_CAMERA_URL': 'http://192.168.1.8:8080/video',
#             'FRAME_SIZE': '480,480',
#             'PROCESS_EVERY_N_FRAMES': 2,
#             'MODEL_PATH': 'best.pt'
#         }
#         if os.path.exists(config_path):
#             self.config.read(config_path)
#         else:
#             self.create_default_config(config_path)

#     def create_default_config(self, config_path):
#         self.config['DEFAULT'] = self.defaults
#         with open(config_path, 'w') as configfile:
#             self.config.write(configfile)

#     def get(self, key, section='DEFAULT'):
#         value = self.config.get(section, key, fallback=self.defaults.get(key))
#         if key == 'FRAME_SIZE':
#             return tuple(map(int, value.split(',')))
#         elif key in ['RESTAURANT_ID', 'PROCESS_EVERY_N_FRAMES']:
#             return int(value)
#         return value

#     def get_camera_url(self, owner_id):
#         from api_client import fetch_camera_url
#         return fetch_camera_url(self, owner_id)


# version 1
# import configparser
# import os
# import logging

# class ConfigManager:
#     def __init__(self, config_path='config.ini'):
#         self.config = configparser.ConfigParser()
#         self.logger = logging.getLogger(__name__)
#         self.defaults = {
#             'RESTAURANT_ID': 1,
#             'REST_API_BASE_URL': 'http://localhost:3001',
#             'FALLBACK_CAMERA_URL': 'http://192.168.1.8:8080/video',
#             'FRAME_SIZE': '480,480',
#             'PROCESS_EVERY_N_FRAMES': 2,
#             'MODEL_PATH': 'best.pt',
#             'SEAT_LIMIT': 4  # Added seat limit configuration
#         }
#         if os.path.exists(config_path):
#             self.config.read(config_path)
#         else:
#             self.create_default_config(config_path)

#     def create_default_config(self, config_path):
#         self.config['DEFAULT'] = self.defaults
#         with open(config_path, 'w') as configfile:
#             self.config.write(configfile)

#     def get(self, key, section='DEFAULT'):
#         value = self.config.get(section, key, fallback=self.defaults.get(key))
#         if key == 'FRAME_SIZE':
#             return tuple(map(int, value.split(',')))
#         elif key in ['RESTAURANT_ID', 'PROCESS_EVERY_N_FRAMES', 'SEAT_LIMIT']:
#             return int(value)
#         return value

#     def get_camera_url(self, owner_id):
#         from api_client import fetch_camera_url
#         return fetch_camera_url(self, owner_id)



# version 3
# import configparser
# import os
# import logging

# class ConfigManager:
#     def __init__(self, config_path='config.ini'):
#         self.config = configparser.ConfigParser()
#         self.logger = logging.getLogger(__name__)
#         self.defaults = {
#             'RESTAURANT_ID': '1',
#             'REST_API_BASE_URL': 'http://localhost:3001',
#             'FALLBACK_CAMERA_URL': 'http://192.168.1.8:8080/video',
#             'FRAME_SIZE': '480,480',
#             'PROCESS_EVERY_N_FRAMES': '2',
#             'MODEL_PATH': 'best.pt',
#             'SEAT_LIMIT': '4',
#             # Connection string for Postgres in the format:
#             # postgres://username:password@host:port/dbname
#             'DATABASE_URL': 'postgres://postgres:postgres@localhost/server'
#         }
#         if os.path.exists(config_path):
#             self.config.read(config_path)
#         else:
#             self.create_default_config(config_path)

#     def create_default_config(self, config_path):
#         self.config['DEFAULT'] = self.defaults
#         with open(config_path, 'w') as configfile:
#             self.config.write(configfile)

#     def get(self, key, section='DEFAULT'):
#         value = self.config.get(section, key, fallback=self.defaults.get(key))
#         if key == 'FRAME_SIZE':
#             return tuple(map(int, value.split(',')))
#         elif key in ['RESTAURANT_ID', 'PROCESS_EVERY_N_FRAMES', 'SEAT_LIMIT']:
#             try:
#                 return int(value)
#             except ValueError:
#                 return value
#         return value

#     def get_camera_url(self, owner_id):
#         from api_client import fetch_camera_url
#         return fetch_camera_url(self, owner_id)


# version 4
import configparser
import os
import logging

class ConfigManager:
    def __init__(self, config_path='config.ini'):
        self.config = configparser.ConfigParser()
        self.logger = logging.getLogger(__name__)
        self.defaults = {
            'RESTAURANT_ID': '1',
            'REST_API_BASE_URL': 'http://localhost:3001',
            'FALLBACK_CAMERA_URL': 'http://192.168.1.8:8080/video',
            'FRAME_SIZE': '480,480',
            'PROCESS_EVERY_N_FRAMES': '2',
            'MODEL_PATH': 'best.pt',
            'SEAT_LIMIT': '4',
            # Connection string for Postgres in the format:
            # postgres://username:password@host:port/dbname
            'DATABASE_URL': 'postgres://postgres:postgres@localhost/server'
        }
        if os.path.exists(config_path):
            self.config.read(config_path)
        else:
            self.create_default_config(config_path)

    def create_default_config(self, config_path):
        self.config['DEFAULT'] = self.defaults
        with open(config_path, 'w') as configfile:
            self.config.write(configfile)

    def get(self, key, section='DEFAULT'):
        value = self.config.get(section, key, fallback=self.defaults.get(key))
        if key == 'FRAME_SIZE':
            return tuple(map(int, value.split(',')))
        elif key in ['RESTAURANT_ID', 'PROCESS_EVERY_N_FRAMES', 'SEAT_LIMIT']:
            try:
                return int(value)
            except ValueError:
                return value
        return value

    def get_camera_url(self, owner_id):
        from api_client import fetch_camera_url
        return fetch_camera_url(self, owner_id)