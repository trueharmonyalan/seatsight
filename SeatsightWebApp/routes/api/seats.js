import express from "express";
import db from "../../config/database.js";

const router = express.Router();
router.use(express.json());

// ✅ Fetch Current Seat Status
router.get("/status/:restaurant_id", async (req, res) => {
    const { restaurant_id } = req.params;

    try {
        const bookedResult = await db.query(
            "SELECT COUNT(*) AS booked_seats FROM seats WHERE restaurant_id = $1 AND is_booked = TRUE",
            [restaurant_id]
        );

        const totalResult = await db.query(
            "SELECT seating_capacity FROM restaurants WHERE id = $1",
            [restaurant_id]
        );

        if (totalResult.rows.length === 0) {
            return res.status(404).json({ error: "Restaurant not found." });
        }

        res.json({
            bookedSeats: parseInt(bookedResult.rows[0].booked_seats) || 0,
            totalSeats: parseInt(totalResult.rows[0].seating_capacity) || 0
        });

    } catch (error) {
        console.error("Database Error:", error);
        res.status(500).json({ error: "Failed to fetch seat status." });
    }
});

// ✅ Update Seating Capacity API
router.post("/update", async (req, res) => {
    const { restaurant_id, seating_capacity } = req.body;

    if (!restaurant_id || !seating_capacity || seating_capacity <= 0) {
        return res.status(400).json({ error: "Invalid request: Missing or invalid fields." });
    }

    try {
        await db.query("BEGIN");

        // ✅ Update seating capacity in the restaurants table
        const updateRes = await db.query(
            "UPDATE restaurants SET seating_capacity = $1 WHERE id = $2 RETURNING seating_capacity",
            [seating_capacity, restaurant_id]
        );

        if (updateRes.rowCount === 0) {
            await db.query("ROLLBACK");
            return res.status(404).json({ error: "Restaurant not found." });
        }

        // ✅ Delete old seats if capacity changes
        await db.query("DELETE FROM seats WHERE restaurant_id = $1", [restaurant_id]);

        // ✅ Insert new seats based on the updated capacity
        for (let i = 1; i <= seating_capacity; i++) {
            await db.query(
                "INSERT INTO seats (restaurant_id, seat_number, is_booked) VALUES ($1, $2, FALSE)",
                [restaurant_id, i]
            );
        }

        await db.query("COMMIT");
        res.json({ message: "Seating capacity updated successfully!" });

    } catch (error) {
        await db.query("ROLLBACK");
        console.error("Database Update Error:", error);
        res.status(500).json({ error: "Failed to update capacity." });
    }
});



// ✅ Fetch Seat List with Availability
router.get("/:restaurant_id", async (req, res) => {
    const { restaurant_id } = req.params;

    try {
        const seatResults = await db.query(
            "SELECT id AS seatId, seat_number AS seatNumber, is_booked FROM seats WHERE restaurant_id = $1",
            [restaurant_id]
        );

        if (seatResults.rows.length === 0) {
            return res.status(404).json({ error: "No seats found for this restaurant." });
        }

        console.log("Returning seat data:", seatResults.rows); // ✅ Log data before sending
        res.json(seatResults.rows);
    } catch (error) {
        console.error("Database Error:", error);
        res.status(500).json({ error: "Failed to fetch seat data." });
    }
});

router.get("/restaurant/:restaurant_id", async (req, res) => {
    const { restaurant_id } = req.params;

    try {
        const seatResults = await db.query(
            "SELECT id AS seatId, seat_number AS seatNumber, is_booked FROM seats WHERE restaurant_id = $1",
            [restaurant_id]
        );

        if (seatResults.rows.length === 0) {
            return res.status(404).json({ error: "No seats found for this restaurant." });
        }

        console.log("Returning seat data:", seatResults.rows); // ✅ Log data before sending
        res.json(seatResults.rows);
    } catch (error) {
        console.error("Database Error:", error);
        res.status(500).json({ error: "Failed to fetch seat data." });
    }
});

// ✅ POST: Record new seat status updates from sensor/deep learning pipeline
router.post("/seat_status", async (req, res) => {
    const { restaurant_id, statuses } = req.body;
    if (!restaurant_id || !statuses || !Array.isArray(statuses)) {
        return res.status(400).json({ error: "Invalid request payload." });
    }

    try {
        await db.query("BEGIN");
        for (const statusUpdate of statuses) {
            const { seat_id, status, timestamp } = statusUpdate;
            if (!seat_id || !status || !["vacant", "occupied"].includes(status)) {
                continue; // You could also return an error here for invalid entries
            }
            await db.query(
                "INSERT INTO seat_status (restaurant_id, seat_id, status, timestamp) VALUES ($1, $2, $3, COALESCE($4, NOW()))",
                [restaurant_id, seat_id, status, timestamp]
            );
        }
        await db.query("COMMIT");
        res.json({ message: "Seat status updates recorded successfully." });
    } catch (error) {
        await db.query("ROLLBACK");
        console.error("Error updating seat status:", error);
        res.status(500).json({ error: "Failed to update seat status." });
    }
});


// // ✅ GET: Fetch the latest seat status for each seat in a restaurant
// router.get("/seat_status/:restaurant_id", async (req, res) => {
//     const { restaurant_id } = req.params;
//     try {
//         const query = `
//             SELECT s.id AS seatId, s.seat_number AS seatNumber,
//                    COALESCE(ss.status, 'vacant') AS status,
//                    ss.timestamp
//             FROM seats s
//             LEFT JOIN LATERAL (
//                 SELECT status, timestamp
//                 FROM seat_status
//                 WHERE seat_id = s.id
//                 ORDER BY timestamp DESC
//                 LIMIT 1
//             ) ss ON true
//             WHERE s.restaurant_id = $1
//             ORDER BY s.seat_number
//         `;
//         const result = await db.query(query, [restaurant_id]);
//         res.json(result.rows);
//     } catch (error) {
//         console.error("Error fetching live seat status:", error);
//         res.status(500).json({ error: "Failed to fetch live seat status." });
//     }
// });








// router.get("/seat/:owner_id", async (req, res) => {
//     const { owner_id } = req.params;

//     try {
//         // Find the restaurant corresponding to the owner id.
//         const restaurantQuery = "SELECT id FROM restaurants WHERE owner_id = $1";
//         const restaurantResult = await db.query(restaurantQuery, [owner_id]);
//         if (restaurantResult.rows.length === 0) {
//             return res.status(404).json({ error: "Restaurant not found for this owner." });
//         }
//         const restaurant_id = restaurantResult.rows[0].id;

//         // Fetch seats for the restaurant.
//         const seatResults = await db.query(
//             "SELECT id AS seatId, seat_number AS seatNumber, is_booked, status, pos_x, pos_y FROM seats WHERE restaurant_id = $1 ORDER BY seat_number",
//             [restaurant_id]
//         );
//         if (seatResults.rows.length === 0) {
//             return res.status(404).json({ error: "No seats found for this restaurant." });
//         }
//         res.json(seatResults.rows);
//     } catch (error) {
//         console.error("Error fetching seats:", error);
//         res.status(500).json({ error: "Failed to fetch seats." });
//     }
// });

router.get("/seat/:owner_id", async (req, res) => {
    const { owner_id } = req.params;

    try {
        // Find the restaurant corresponding to the owner id.
        const restaurantQuery = "SELECT id FROM restaurants WHERE owner_id = $1";
        const restaurantResult = await db.query(restaurantQuery, [owner_id]);

        if (restaurantResult.rows.length === 0) {
            console.log(`No restaurant found for owner_id: ${owner_id}`);
            return res.status(404).json({ error: "Restaurant not found for this owner." });
        }

        const restaurant_id = restaurantResult.rows[0].id;
        console.log(`Fetched restaurant_id: ${restaurant_id} for owner_id: ${owner_id}`);

        // Fetch seats for the restaurant.
        const seatQuery = `
            SELECT id AS seatId, restaurant_id, seat_number AS seatNumber, is_booked, status, pos_x, pos_y 
            FROM seats WHERE restaurant_id = $1 ORDER BY seat_number
        `;
        const seatResults = await db.query(seatQuery, [restaurant_id]);

        console.log(`Seats found for restaurant_id ${restaurant_id}:`, seatResults.rows);

        if (seatResults.rows.length === 0) {
            return res.status(404).json({ error: "No seats found for this restaurant." });
        }

        res.json(seatResults.rows);
    } catch (error) {
        console.error("Error fetching seats:", error);
        res.status(500).json({ error: "Failed to fetch seats." });
    }
});







router.post("/seat_status/:owner_id", async (req, res) => {
    const { owner_id } = req.params;
    const { statuses } = req.body;

    // Input validation
    if (!Array.isArray(statuses) || statuses.length === 0) {
        return res.status(400).json({ error: "Invalid or empty statuses array" });
    }

    try {
        // Validate owner and get restaurant
        const restaurant = await db.query(
            "SELECT id FROM restaurants WHERE owner_id = $1", 
            [owner_id]
        );
        if (restaurant.rows.length === 0) {
            return res.status(404).json({ error: "Restaurant not found" });
        }
        const restaurant_id = restaurant.rows[0].id;

        await db.query("BEGIN");

        const invalidSeats = [];
        const validStatuses = new Set(['available', 'occupied', 'reserved']); // Example statuses

        for (const statusUpdate of statuses) {
            const { seat_number, status } = statusUpdate;

            // Validate input
            if (!Number.isInteger(seat_number) || seat_number <= 0 || !validStatuses.has(status)) {
                invalidSeats.push({ seat_number, error: "Invalid data" });
                continue;
            }

            // Check seat existence
            const seat = await db.query(
                "SELECT id FROM seats WHERE restaurant_id = $1 AND seat_number = $2",
                [restaurant_id, seat_number]
            );
            if (seat.rows.length === 0) {
                invalidSeats.push({ seat_number, error: "Seat not found" });
                continue;
            }

            // Update seat status
            await db.query(
                "UPDATE seats SET status = $1 WHERE id = $2",
                [status, seat.rows[0].id]
            );

            // Log status change
            await db.query(
                "INSERT INTO seat_status (seat_id, status) VALUES ($1, $2)",
                [seat.rows[0].id, status]
            );
        }

        if (invalidSeats.length > 0) {
            await db.query("ROLLBACK");
            return res.status(400).json({
                error: "Some seats are invalid",
                invalidSeats
            });
        }

        await db.query("COMMIT");
        res.json({ message: "All seat statuses updated successfully" });
    } catch (error) {
        await db.query("ROLLBACK");
        console.error("Update error:", error);
        res.status(500).json({ error: "Failed due to internal error" });
    }
});

export default router;
