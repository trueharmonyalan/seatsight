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




export default router;
