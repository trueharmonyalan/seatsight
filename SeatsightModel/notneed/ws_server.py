import asyncio
import json
import logging
import websockets
from config import ConfigManager
import db_manager

logger = logging.getLogger(__name__)

# Configuration
config = ConfigManager()
RESTAURANT_ID = config.get("RESTAURANT_ID")
DB_CONN_STR = config.get("DATABASE_URL")
POLL_INTERVAL = 2  # seconds

# Set of currently connected WebSocket clients.
connected_clients = set()

async def broadcast_seat_status():
    while True:
        # Fetch the latest seat statuses from the database.
        seats = db_manager.fetch_seats_status(RESTAURANT_ID, DB_CONN_STR)
        message = json.dumps({"restaurant_id": RESTAURANT_ID, "seats": seats})
        # Broadcast the update to all connected clients.
        if connected_clients:
            await asyncio.wait([client.send(message) for client in connected_clients])
        await asyncio.sleep(POLL_INTERVAL)

async def handler(websocket, path):
    # Register the new client.
    connected_clients.add(websocket)
    logger.info("New client connected.")
    try:
        # Keep the connection open.
        async for _ in websocket:
            pass
    except Exception as e:
        logger.error(f"Error with client WebSocket connection: {e}")
    finally:
        # Unregister the client.
        connected_clients.remove(websocket)
        logger.info("Client disconnected.")

async def main():
    # Start the WebSocket server.
    server = await websockets.serve(handler, "0.0.0.0", 6789)
    logger.info("WebSocket server started on port 6789")
    # Run the broadcaster concurrently.
    broadcaster_task = asyncio.create_task(broadcast_seat_status())
    await server.wait_closed()
    await broadcaster_task

if __name__ == "__main__":
    asyncio.run(main())