
import logging
import os
import sys
import signal
import time
import traceback

# Set up logging immediately
logging.basicConfig(
    level=logging.DEBUG,  # Set to DEBUG to get maximum information
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout)  # Log to console for immediate feedback
    ]
)

logger = logging.getLogger("SeatsightMain")
logger.info("Starting main.py - initializing...")

try:
    # Attempt to import required modules
    logger.info("Importing modules...")
    from controller import SeatSightController
    from app_server import start_server, app
    logger.info("Modules imported successfully")
except ImportError as e:
    logger.critical(f"Failed to import required modules: {e}")
    logger.critical(traceback.format_exc())
    sys.exit(1)

# Default configuration
BASE_API_ENDPOINT = "http://localhost:3001/api/restaurants"
DATABASE_URL = "postgres://postgres:postgres@localhost/server"
POLLING_INTERVAL = 60  # seconds

def signal_handler(sig, frame):
    """Handle termination signals to ensure clean shutdown."""
    logger.info("Shutting down SeatSight...")
    if hasattr(signal_handler, 'controller'):
        signal_handler.controller.stop()
    sys.exit(0)

def main():
    """Main entry point for the SeatSight application."""
    try:
        logger.info("In main function - starting SeatSight system...")
        
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
        logger.info("Initializing controller...")
        controller = SeatSightController(base_api_endpoint, db_conn_str, polling_interval)
        signal_handler.controller = controller  # Store for signal handler
        
        # Set the controller reference in the Flask app
        logger.info("Setting controller reference in Flask app...")
        import app_server
        app_server.controller = controller
        
        # Start the controller
        logger.info("Starting controller...")
        controller.start()
        
        # Start the API server
        logger.info("Starting API server...")
        api_thread = app_server.start_server(host=api_host, port=api_port)
        logger.info("API server started - entering main loop")
        
        # Keep the main thread alive
        while True:
            logger.debug("Main thread heartbeat")
            time.sleep(10)  # Log every 10 seconds
            
    except Exception as e:
        logger.critical(f"Fatal error in main: {e}")
        logger.critical(traceback.format_exc())
        return 1
    
    return 0

if __name__ == "__main__":
    # Register signal handlers for graceful shutdown
    signal.signal(signal.SIGINT, signal_handler)
    signal.signal(signal.SIGTERM, signal_handler)
    
    logger.info("Registered signal handlers, calling main()")
    try:
        exit_code = main()
        logger.info(f"main() returned with code {exit_code}")
        sys.exit(exit_code)
    except Exception as e:
        logger.critical(f"Uncaught exception: {e}")
        logger.critical(traceback.format_exc())
        sys.exit(1)
