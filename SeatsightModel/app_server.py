from flask import Flask, request, jsonify
import logging
import threading

# Set up logger
logger = logging.getLogger(__name__)

# Initialize Flask app
app = Flask(__name__)

# Reference to the controller (will be set in main)
controller = None

@app.route('/update-restaurant', methods=['POST'])
def update_restaurant():
    """
    Endpoint that the Node.js API will call when it receives a restaurant ID.
    Expected JSON payload: {"restaurant_id": 123}
    """
    try:
        data = request.json
        if not data or 'restaurant_id' not in data:
            logger.warning("Received invalid request - missing restaurant_id")
            return jsonify({"status": "error", "message": "Missing restaurant_id"}), 400
        
        restaurant_id = data['restaurant_id']
        
        if not restaurant_id or not str(restaurant_id).isdigit():
            logger.warning(f"Received invalid restaurant_id: {restaurant_id}")
            return jsonify({"status": "error", "message": "Invalid restaurant_id"}), 400
            
        logger.info(f"Received request to update restaurant ID to: {restaurant_id}")
        
        # Check if controller is ready
        global controller
        if not controller:
            logger.error("Controller not initialized yet")
            return jsonify({"status": "error", "message": "System not ready"}), 503
            
        # Process the restaurant ID
        success = controller.process_restaurant_id(int(restaurant_id))
        
        if success:
            return jsonify({"status": "success", "message": f"Processing restaurant ID: {restaurant_id}"}), 200
        else:
            return jsonify({"status": "error", "message": "Failed to process restaurant ID"}), 500
            
    except Exception as e:
        logger.error(f"Error processing update-restaurant request: {e}")
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/status', methods=['GET'])
def status():
    """Simple endpoint to check if the service is running."""
    global controller
    if not controller:
        return jsonify({"status": "initializing", "message": "System starting up"}), 200
        
    active_sessions = controller.get_active_sessions() if controller else []
    
    return jsonify({
        "status": "ready",
        "message": "System running",
        "active_sessions": active_sessions
    }), 200

def start_server(host='0.0.0.0', port=3003):
    """Start the Flask server in a separate thread."""
    server_thread = threading.Thread(
        target=lambda: app.run(host=host, port=port, debug=False, use_reloader=False),
        daemon=True
    )
    server_thread.start()
    logger.info(f"API server started on {host}:{port}")
    return server_thread