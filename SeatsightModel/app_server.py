
from flask import Flask, request, jsonify, Response
import logging
import threading
import time
import json
import gc
import psycopg2

# Set up logger with more details
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger("SeatTrackingServer")

# Initialize Flask app
app = Flask(__name__)

# Reference to the controller (will be set in main)
controller = None

# Track active connections by client IP and restaurant ID
active_connections = {}
connection_lock = threading.Lock()

# Track restaurant viewers for reference counting
restaurant_viewers = {}
viewer_lock = threading.Lock()

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
            
        # Process the restaurant ID - only register it, don't start tracking yet
        # Just load and validate the configuration
        success = controller.register_restaurant_id(int(restaurant_id))
        
        if success:
            return jsonify({"status": "success", "message": f"Registered restaurant ID: {restaurant_id}"}), 200
        else:
            return jsonify({"status": "error", "message": "Failed to register restaurant ID"}), 500
            
    except Exception as e:
        logger.error(f"Error processing update-restaurant request: {e}")
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/start-tracking/<int:restaurant_id>', methods=['POST'])
def start_tracking(restaurant_id):
    """
    Start tracking a restaurant's seat availability on demand.
    Implements reference counting to handle multiple viewers.
    """
    client_id = request.remote_addr
    logger.info(f"Request to start tracking for restaurant {restaurant_id} from {client_id}")
    
    # Check if controller is ready
    global controller
    if not controller:
        logger.error("Controller not initialized yet")
        return jsonify({"status": "error", "message": "System not ready"}), 503
    
    try:
        with viewer_lock:
            if restaurant_id not in restaurant_viewers:
                restaurant_viewers[restaurant_id] = {}
            
            # Add this viewer to the restaurant
            restaurant_viewers[restaurant_id][client_id] = {"timestamp": time.time()}
            
            viewer_count = len(restaurant_viewers[restaurant_id])
            logger.info(f"Added viewer {client_id} for restaurant {restaurant_id}. Total viewers: {viewer_count}")
            
            # If this is the first viewer, start tracking
            if viewer_count == 1:
                logger.info(f"First viewer for restaurant {restaurant_id}, starting tracking...")
                success = controller.process_restaurant_id(restaurant_id)
                if not success:
                    # If start failed, remove the viewer
                    del restaurant_viewers[restaurant_id][client_id]
                    if not restaurant_viewers[restaurant_id]:
                        del restaurant_viewers[restaurant_id]
                    return jsonify({"status": "error", "message": "Failed to start tracking"}), 500
            
        return jsonify({
            "status": "success", 
            "message": f"Tracking started for restaurant {restaurant_id}",
            "viewers": viewer_count
        }), 200
        
    except Exception as e:
        logger.error(f"Error starting tracking for restaurant {restaurant_id}: {e}")
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/stop-tracking/<int:restaurant_id>', methods=['POST'])
def stop_tracking(restaurant_id):
    """
    Stop tracking a restaurant's seat availability when a viewer navigates away.
    Only stops tracking when the last viewer disconnects.
    """
    client_id = request.remote_addr
    logger.info(f"Request to stop tracking for restaurant {restaurant_id} from {client_id}")
    
    # Check if controller is ready
    global controller
    if not controller:
        logger.error("Controller not initialized yet")
        return jsonify({"status": "error", "message": "System not ready"}), 503
    
    try:
        with viewer_lock:
            if restaurant_id not in restaurant_viewers:
                logger.warning(f"Restaurant {restaurant_id} not being tracked")
                return jsonify({
                    "status": "warning", 
                    "message": f"Restaurant {restaurant_id} not being tracked"
                }), 200
                
            # Remove this viewer
            if client_id in restaurant_viewers[restaurant_id]:
                del restaurant_viewers[restaurant_id][client_id]
                logger.info(f"Removed viewer {client_id} for restaurant {restaurant_id}")
            
            # Check if this was the last viewer
            viewer_count = len(restaurant_viewers[restaurant_id])
            if viewer_count == 0:
                logger.info(f"Last viewer for restaurant {restaurant_id}, stopping tracking...")
                del restaurant_viewers[restaurant_id]
                
                # Find the session and stop it
                if restaurant_id in controller.get_active_sessions():
                    controller.session_manager.stop_session(restaurant_id)
            
        return jsonify({
            "status": "success", 
            "message": f"Tracking stopped for client {client_id}",
            "remaining_viewers": viewer_count
        }), 200
        
    except Exception as e:
        logger.error(f"Error stopping tracking for restaurant {restaurant_id}: {e}")
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/status', methods=['GET'])
def status():
    """Simple endpoint to check if the service is running."""
    global controller
    if not controller:
        return jsonify({"status": "initializing", "message": "System starting up"}), 200
        
    active_sessions = controller.get_active_sessions() if controller else []
    
    # Include active connection count in status
    with connection_lock:
        connection_count = len(active_connections)
    
    # Include restaurant viewers in status
    with viewer_lock:
        tracked_restaurants = list(restaurant_viewers.keys())
        viewers_by_restaurant = {rid: len(viewers) for rid, viewers in restaurant_viewers.items()}
    
    return jsonify({
        "status": "ready",
        "message": "System running",
        "active_sessions": active_sessions,
        "active_connections": connection_count,
        "tracked_restaurants": tracked_restaurants,
        "viewers": viewers_by_restaurant,
        "version": "1.0.0-trueharmonyalan-2025-03-25"
    }), 200

def get_seats_from_db(restaurant_id):
    """Helper function to get seats from the database."""
    seats = []
    try:
        global controller
        if not controller or not controller.db_conn_str:
            logger.error("Database connection string not available")
            return seats
            
        # Create a new connection for each query to avoid locks
        conn = psycopg2.connect(controller.db_conn_str)
        conn.autocommit = True
        
        with conn.cursor() as cur:
            # Query for all seats in the restaurant with their status
            cur.execute("""
                SELECT id, seat_number, status, is_booked, pos_x, pos_y 
                FROM seats 
                WHERE restaurant_id = %s
            """, (restaurant_id,))
            
            for row in cur.fetchall():
                seat_id, seat_number, status, is_booked, pos_x, pos_y = row
                seats.append({
                    "id": seat_id,
                    "seatNumber": seat_number,
                    "status": status,
                    "isBooked": is_booked,
                    "posX": pos_x,
                    "posY": pos_y
                })
        
        # Close the connection when done
        conn.close()
            
    except Exception as e:
        logger.error(f"Database error in get_seats_from_db: {e}")
        
    return seats

@app.route('/api/seats/stream/<int:restaurant_id>', methods=['GET'])
def stream_seats(restaurant_id):
    """Endpoint to stream real-time seat updates for a restaurant."""
    client_id = request.remote_addr
    logger.info(f"New SSE connection from {client_id} for restaurant {restaurant_id}")
    
    # Register this client as a viewer for the restaurant
    with viewer_lock:
        if restaurant_id not in restaurant_viewers:
            restaurant_viewers[restaurant_id] = {}
        
        restaurant_viewers[restaurant_id][client_id] = {"timestamp": time.time()}
        
        # Check if we need to start tracking for this restaurant
        viewer_count = len(restaurant_viewers[restaurant_id])
        if viewer_count == 1 and controller:
            logger.info(f"First viewer streaming restaurant {restaurant_id}, ensuring tracking is active...")
            controller.process_restaurant_id(restaurant_id)
    
    # Clean up closed connections
    cleanup_connections()
    
    def generate():
        connection_key = f"{client_id}:{restaurant_id}"
        
        try:
            # Register this connection
            with connection_lock:
                # If this client already has a connection for this restaurant, replace it
                if connection_key in active_connections:
                    logger.warning(f"Client {client_id} already has an active connection for restaurant {restaurant_id}. Replacing.")
                
                active_connections[connection_key] = {"timestamp": time.time()}
                logger.info(f"Registered connection {connection_key}. Total active: {len(active_connections)}")
            
            # Send immediate headers for SSE
            yield "HTTP/1.1 200 OK\r\n"
            yield "Content-Type: text/event-stream\r\n"
            yield "Cache-Control: no-cache\r\n"
            yield "Connection: keep-alive\r\n\r\n"
            
            # Send initial connection confirmation
            yield f"data: {json.dumps({'status': 'connected', 'restaurantId': restaurant_id})}\n\n"
            
            last_update_time = 0
            consecutive_errors = 0
            max_consecutive_errors = 5
            
            while True:
                current_time = time.time()
                
                # Check if connection is still registered (could be closed by cleanup)
                with connection_lock:
                    if connection_key not in active_connections:
                        logger.info(f"Connection {connection_key} was closed externally.")
                        break
                    
                    # Update timestamp to show this connection is still active
                    active_connections[connection_key]["timestamp"] = current_time
                
                # Also update viewer timestamp to keep the viewer active
                with viewer_lock:
                    if restaurant_id in restaurant_viewers and client_id in restaurant_viewers[restaurant_id]:
                        restaurant_viewers[restaurant_id][client_id]["timestamp"] = current_time
                
                # Only get updates every 1 second to reduce database load
                if current_time - last_update_time >= 1.0:
                    try:
                        # Get seats from database
                        seats = get_seats_from_db(restaurant_id)
                        
                        # Format data as SSE
                        data = json.dumps({"seats": seats, "timestamp": current_time})
                        yield f"data: {data}\n\n"
                        
                        # Reset error counter on success
                        consecutive_errors = 0
                        
                    except Exception as e:
                        consecutive_errors += 1
                        logger.error(f"Error in stream for {connection_key}: {e}")
                        
                        if consecutive_errors >= max_consecutive_errors:
                            logger.error(f"Too many consecutive errors ({consecutive_errors}), closing connection {connection_key}")
                            break
                            
                        # Send error to client
                        yield f"data: {json.dumps({'error': str(e)})}\n\n"
                    
                    last_update_time = current_time
                
                # Send a heartbeat every 15 seconds if no updates
                elif current_time - last_update_time >= 15.0:
                    yield f"data: {json.dumps({'heartbeat': True, 'timestamp': current_time})}\n\n"
                
                # Sleep to prevent high CPU usage
                time.sleep(0.2)
                
        except Exception as e:
            logger.error(f"Fatal error in seat stream for {connection_key}: {e}")
            yield f"data: {json.dumps({'error': str(e), 'fatal': True})}\n\n"
        finally:
            # Always remove the connection when done
            with connection_lock:
                if connection_key in active_connections:
                    del active_connections[connection_key]
                    logger.info(f"Removed connection {connection_key}. Remaining active: {len(active_connections)}")
            
            # Remove the viewer registration when the connection is closed
            with viewer_lock:
                if restaurant_id in restaurant_viewers and client_id in restaurant_viewers[restaurant_id]:
                    del restaurant_viewers[restaurant_id][client_id]
                    logger.info(f"Removed viewer {client_id} for restaurant {restaurant_id}")
                    
                    # If this was the last viewer, stop tracking
                    if len(restaurant_viewers[restaurant_id]) == 0:
                        logger.info(f"Last viewer disconnected for restaurant {restaurant_id}, stopping tracking...")
                        del restaurant_viewers[restaurant_id]
                        
                        # Find the session and stop it
                        if controller:
                            active_sessions = controller.get_active_sessions()
                            if restaurant_id in active_sessions:
                                controller.session_manager.stop_session(restaurant_id)
            
            # Perform garbage collection to free resources
            gc.collect()
    
    # Set appropriate headers for SSE
    response = Response(generate(), mimetype="text/event-stream")
    response.headers['Cache-Control'] = 'no-cache'
    response.headers['X-Accel-Buffering'] = 'no'  # Disable buffering in Nginx
    response.headers['Access-Control-Allow-Origin'] = '*'  # Allow cross-origin requests (CORS)
    return response

def cleanup_connections():
    """Remove any stale connections (inactive for more than 30 seconds)."""
    current_time = time.time()
    removed = 0
    
    with connection_lock:
        keys_to_remove = []
        
        for key, info in active_connections.items():
            # If no activity for 30 seconds, consider the connection dead
            if current_time - info["timestamp"] > 30:
                keys_to_remove.append(key)
        
        # Remove stale connections
        for key in keys_to_remove:
            del active_connections[key]
            removed += 1
            
        if removed > 0:
            logger.info(f"Cleanup removed {removed} stale connections. Remaining active: {len(active_connections)}")

def cleanup_viewers():
    """Remove any viewers with no activity for more than 2 minutes."""
    current_time = time.time()
    removed = 0
    
    with viewer_lock:
        for restaurant_id in list(restaurant_viewers.keys()):
            viewers_to_remove = []
            
            for client_id, info in restaurant_viewers[restaurant_id].items():
                # If no activity for 2 minutes, consider the viewer gone
                if current_time - info["timestamp"] > 120:
                    viewers_to_remove.append(client_id)
            
            # Remove inactive viewers
            for client_id in viewers_to_remove:
                logger.info(f"Removing inactive viewer {client_id} for restaurant {restaurant_id}")
                del restaurant_viewers[restaurant_id][client_id]
                removed += 1
            
            # If no more viewers, stop tracking
            if not restaurant_viewers[restaurant_id] and controller:
                logger.info(f"No more viewers for restaurant {restaurant_id}, stopping tracking...")
                
                # Find the session and stop it
                active_sessions = controller.get_active_sessions()
                if restaurant_id in active_sessions:
                    controller.session_manager.stop_session(restaurant_id)
                
                # Remove empty restaurant entry
                del restaurant_viewers[restaurant_id]
    
    if removed > 0:
        logger.info(f"Cleanup removed {removed} inactive viewers")

@app.before_request
def before_request():
    """Log each request for debugging."""
    if not request.path.startswith('/api/seats/stream'):  # Skip logging SSE requests
        logger.info(f"Request: {request.method} {request.path} from {request.remote_addr}")

@app.after_request
def after_request(response):
    """Add CORS headers to allow connections from mobile app."""
    if not request.path.startswith('/api/seats/stream'):  # Skip modifying SSE responses
        response.headers['Access-Control-Allow-Origin'] = '*'
        response.headers['Access-Control-Allow-Headers'] = 'Content-Type'
        response.headers['Access-Control-Allow-Methods'] = 'GET, POST, OPTIONS'
    return response

def start_server(host='0.0.0.0', port=3003):
    """Start the Flask server in a separate thread."""
    server_thread = threading.Thread(
        target=lambda: app.run(host=host, port=port, debug=False, use_reloader=False, threaded=True),
        daemon=True
    )
    server_thread.start()
    logger.info(f"API server started on {host}:{port}")
    
    # Start a cleanup thread to periodically remove inactive viewers
    cleanup_thread = threading.Thread(
        target=periodic_cleanup,
        daemon=True
    )
    cleanup_thread.start()
    
    return server_thread

def periodic_cleanup():
    """Run cleanup functions periodically."""
    while True:
        try:
            time.sleep(60)  # Run every minute
            cleanup_connections()
            cleanup_viewers()
        except Exception as e:
            logger.error(f"Error during periodic cleanup: {e}")
