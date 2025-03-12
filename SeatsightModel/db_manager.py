# import psycopg2
# import logging

# logger = logging.getLogger(__name__)

# def get_connection(conn_str):
#     """
#     Establish and return a connection to the PostgreSQL database.
#     """
#     return psycopg2.connect(conn_str)

# def update_seats_status(seat_status, restaurant_id, conn_str):
#     """
#     Update the seats table with the latest seat statuses for a given restaurant.
#     Assumes that the seats already exist in the table.
#     """
#     try:
#         conn = get_connection(conn_str)
#         cur = conn.cursor()
#         for seat in seat_status:
#             seat_number = seat["seatNumber"]
#             status = seat["status"]
#             # Use centroid coordinates if available.
#             centroid = seat.get("centroid", (None, None))
#             pos_x, pos_y = centroid if centroid is not None else (None, None)
#             query = """
#                 UPDATE seats 
#                 SET status = %s, pos_x = %s, pos_y = %s 
#                 WHERE restaurant_id = %s AND seat_number = %s;
#             """
#             cur.execute(query, (status, pos_x, pos_y, restaurant_id, seat_number))
#         conn.commit()
#         cur.close()
#         conn.close()
#         logger.info(f"Updated seat statuses for restaurant_id {restaurant_id}.")
#     except Exception as e:
#         logger.error(f"Error updating seats status: {e}")

# def fetch_seats_status(restaurant_id, conn_str):
#     """
#     Fetch the current status of seats for a restaurant.
#     Returns a list of dictionaries with seat_number, status, pos_x, pos_y, and updated_at.
#     """
#     try:
#         conn = get_connection(conn_str)
#         cur = conn.cursor()
#         query = """
#             SELECT seat_number, status, pos_x, pos_y, updated_at 
#             FROM seats 
#             WHERE restaurant_id = %s 
#             ORDER BY seat_number;
#         """
#         cur.execute(query, (restaurant_id,))
#         rows = cur.fetchall()
#         cur.close()
#         conn.close()
#         results = []
#         for row in rows:
#             results.append({
#                 "seat_number": row[0],
#                 "status": row[1],
#                 "pos_x": row[2],
#                 "pos_y": row[3],
#                 "updated_at": row[4]
#             })
#         return results
#     except Exception as e:
#         logger.error(f"Error fetching seats status: {e}")
#         return []


#version 2
# import psycopg2
# import logging

# logger = logging.getLogger(__name__)

# def get_connection(conn_str):
#     """
#     Establish and return a connection to the PostgreSQL database using the provided connection string.
#     The connection string should include credentials along with host, port, and database name.
#     """
#     try:
#         connection = psycopg2.connect(conn_str)
#         logger.info("Database connection established.")
#         return connection
#     except Exception as e:
#         logger.error(f"Error connecting to the database: {e}")
#         raise

# def update_seats_status(seat_status, restaurant_id, conn_str):
#     """
#     Update the seats table with the latest seat statuses for a given restaurant.
    
#     For each seat from the detection, update its status and position using centroid coordinates (if available).
#     It assumes that the seats already exist in the table.
#     """
#     try:
#         conn = get_connection(conn_str)
#         cur = conn.cursor()
#         for seat in seat_status:
#             seat_number = seat["seatNumber"]
#             status = seat["status"]
#             # Use centroid coordinates if available; otherwise, set pos_x and pos_y to NULL.
#             centroid = seat.get("centroid", (None, None))
#             pos_x, pos_y = centroid if centroid is not None else (None, None)
#             query = """
#                 UPDATE seats 
#                 SET status = %s, pos_x = %s, pos_y = %s 
#                 WHERE restaurant_id = %s AND seat_number = %s;
#             """
#             cur.execute(query, (status, pos_x, pos_y, restaurant_id, seat_number))
#         conn.commit()
#         cur.close()
#         conn.close()
#         logger.info(f"Updated seat statuses for restaurant_id {restaurant_id}.")
#     except Exception as e:
#         logger.error(f"Error updating seats status: {e}")

# def fetch_seats_status(restaurant_id, conn_str):
#     """
#     Fetch the current status of seats for the specified restaurant.
#     Returns a list of dictionaries, one for each seat, with keys:
#       - seat_number
#       - status
#       - pos_x
#       - pos_y
#       - updated_at
#     Logs a summary including the total number of seats and each seat's number with its status.
#     """
#     try:
#         conn = get_connection(conn_str)
#         cur = conn.cursor()
#         query = """
#             SELECT seat_number, status, pos_x, pos_y, updated_at 
#             FROM seats 
#             WHERE restaurant_id = %s 
#             ORDER BY seat_number;
#         """
#         cur.execute(query, (restaurant_id,))
#         rows = cur.fetchall()
#         cur.close()
#         conn.close()
#         results = []
#         for row in rows:
#             results.append({
#                 "seat_number": row[0],
#                 "status": row[1],
#                 "pos_x": row[2],
#                 "pos_y": row[3],
#                 "updated_at": row[4]
#             })
        
#         logger.info(f"Total seats for restaurant_id {restaurant_id}: {len(results)}")
#         for seat in results:
#             logger.info(f"seat[{seat.get('seat_number', 'unknown')}]: {seat.get('status', 'unknown')}")
        
#         return results
#     except Exception as e:
#         logger.error(f"Error fetching seats status: {e}")
#         return []

# def fetch_seat_count(restaurant_id, conn_str):
#     """
#     Fetch the total seat count for the specified restaurant from the database.
#     Returns the count as an integer.
#     """
#     try:
#         conn = get_connection(conn_str)
#         cur = conn.cursor()
#         query = "SELECT COUNT(*) FROM seats WHERE restaurant_id = %s;"
#         cur.execute(query, (restaurant_id,))
#         count = cur.fetchone()[0]
#         cur.close()
#         conn.close()
#         logger.info(f"Fetched total seat count for restaurant_id {restaurant_id}: {count}")
#         return count
#     except Exception as e:
#         logger.error(f"Error fetching seat count: {e}")
#         return 0


#version 3
import psycopg2
import logging

logger = logging.getLogger(__name__)

def get_connection(conn_str):
    """
    Establish and return a connection to the PostgreSQL database using the provided connection string.
    The connection string should include credentials along with host, port, and database name.
    """
    try:
        connection = psycopg2.connect(conn_str)
        logger.info("Database connection established.")
        return connection
    except Exception as e:
        logger.error(f"Error connecting to the database: {e}")
        raise

def update_seats_status(seat_status, restaurant_id, conn_str):
    """
    Update the seats table with the latest seat statuses for a given restaurant.
    
    For each seat from the detection, update its status and position using centroid coordinates (if available).
    It assumes that the seats already exist in the table.
    Logs details for each seat that is updated.
    """
    try:
        conn = get_connection(conn_str)
        cur = conn.cursor()
        for seat in seat_status:
            seat_number = seat["seatNumber"]
            status = seat["status"]
            # Use centroid coordinates if available; otherwise, set pos_x and pos_y to NULL.
            centroid = seat.get("centroid", (None, None))
            pos_x, pos_y = centroid if centroid is not None else (None, None)
            query = """
                UPDATE seats 
                SET status = %s, pos_x = %s, pos_y = %s 
                WHERE restaurant_id = %s AND seat_number = %s;
            """
            cur.execute(query, (status, pos_x, pos_y, restaurant_id, seat_number))
            logger.info(
                f"Updated seat[{seat_number}]: status: {status}, pos_x: {pos_x}, pos_y: {pos_y} for restaurant_id {restaurant_id}"
            )
        conn.commit()
        cur.close()
        conn.close()
        logger.info(f"Finished updating seat statuses for restaurant_id {restaurant_id}.")
    except Exception as e:
        logger.error(f"Error updating seats status: {e}")

def fetch_seats_status(restaurant_id, conn_str):
    """
    Fetch the current status of seats for the specified restaurant.
    Returns a list of dictionaries, one for each seat, with keys:
      - seat_number
      - status
      - pos_x
      - pos_y
      - updated_at
    Logs a summary including the total number of seats and each seat's number with its status.
    """
    try:
        conn = get_connection(conn_str)
        cur = conn.cursor()
        query = """
            SELECT seat_number, status, pos_x, pos_y, updated_at 
            FROM seats 
            WHERE restaurant_id = %s 
            ORDER BY seat_number;
        """
        cur.execute(query, (restaurant_id,))
        rows = cur.fetchall()
        cur.close()
        conn.close()
        results = []
        for row in rows:
            results.append({
                "seat_number": row[0],
                "status": row[1],
                "pos_x": row[2],
                "pos_y": row[3],
                "updated_at": row[4]
            })
        
        logger.info(f"Total seats for restaurant_id {restaurant_id}: {len(results)}")
        for seat in results:
            logger.info(f"seat[{seat.get('seat_number', 'unknown')}]: {seat.get('status', 'unknown')}")
        
        return results
    except Exception as e:
        logger.error(f"Error fetching seats status: {e}")
        return []

def fetch_seat_count(restaurant_id, conn_str):
    """
    Fetch the total seat count for the specified restaurant from the database.
    Returns the count as an integer.
    """
    try:
        conn = get_connection(conn_str)
        cur = conn.cursor()
        query = "SELECT COUNT(*) FROM seats WHERE restaurant_id = %s;"
        cur.execute(query, (restaurant_id,))
        count = cur.fetchone()[0]
        cur.close()
        conn.close()
        logger.info(f"Fetched total seat count for restaurant_id {restaurant_id}: {count}")
        return count
    except Exception as e:
        logger.error(f"Error fetching seat count: {e}")
        return 0