import express from "express";
import db from "../../config/database.js";
import axios from "axios"

const router = express.Router();

// ✅ Fetch All Restaurants (For Web Admin)
router.get("/", async (req, res) => {
    try {
        const result = await db.query("SELECT id, name, address FROM restaurants");
        res.json(result.rows);
    } catch (err) {
        console.error("Database Error:", err);
        res.status(500).json({ error: "Failed to fetch restaurants" });
    }
});

// ✅ Add a New Restaurant (Owner Only)
router.post("/", async (req, res) => {
    const { owner_id, name, ip_camera_url } = req.body;

    try {
        const result = await db.query(
            "INSERT INTO restaurants (owner_id, name, ip_camera_url) VALUES ($1, $2, $3, $4) RETURNING id, name",
            [owner_id, name, address, ip_camera_url]
        );
        res.json({ message: "Restaurant added successfully", restaurant: result.rows[0] });
    } catch (err) {
        console.error("Insert Error:", err);
        res.status(500).json({ error: "Failed to add restaurant" });
    }
});

// ✅ Fetch Restaurant Details Including IP Camera URL
router.get("/owner/:owner_id", async (req, res) => {
    const { owner_id } = req.params;

    try {
        const result = await db.query(
            "SELECT id, name, ip_camera_url FROM restaurants WHERE owner_id = $1",
            [owner_id]
        );

        if (result.rows.length === 0) {
            return res.status(404).json({ error: "No restaurant found for this owner." });
        }

        res.json(result.rows[0]);

    } catch (error) {
        console.error("Database error:", error);
        res.status(500).json({ error: "Failed to fetch restaurant data." });
    }
});

// ✅ Update IP Camera URL
router.post("/update-ip-url", async (req, res) => {
    const { owner_id, ip_camera_url } = req.body;

    if (!owner_id || ip_camera_url === undefined) {
        return res.status(400).json({ error: "Invalid request: Missing required fields." });
    }

    try {
        const updateRes = await db.query(
            "UPDATE restaurants SET ip_camera_url = $1 WHERE owner_id = $2 RETURNING ip_camera_url",
            [ip_camera_url, owner_id]
        );

        if (updateRes.rowCount === 0) {
            return res.status(404).json({ error: "Restaurant not found." });
        }

        res.json({ message: "IP Camera URL updated successfully!", ip_camera_url });

    } catch (error) {
        console.error("Database Update Error:", error);
        res.status(500).json({ error: "Failed to update IP Camera URL." });
    }
});

// ✅ Fetch Hotel Names & Their Menu Items (For Android App)
router.get("/android/hotels-menu", async (req, res) => {
    try {
        const result = await db.query(`
            SELECT r.id AS restaurant_id, r.name AS hotel_name, 
                   json_agg(json_build_object('id', m.id, 'name', m.name, 'price', m.price)) AS menu
            FROM restaurants r
            LEFT JOIN menu_items m ON r.id = m.restaurant_id
            GROUP BY r.id
            ORDER BY r.name;
        `);

        res.json(result.rows);
    } catch (err) {
        console.error("Database Error:", err);
        res.status(500).json({ error: "Failed to fetch hotels and menu data." });
    }
});


router.get("/ip-url/:owner_id", async (req, res) => {
    const { owner_id } = req.params;
    try {
        const result = await db.query("SELECT ip_camera_url FROM restaurants WHERE owner_id = $1",[owner_id ]);
        res.json(result.rows);
    } catch (err) {
        console.error("Database Error:", err);
        res.status(500).json({ error: "Failed to fetch restaurants" });
    }
});


// router.get('/get-restaurant-id/:restaurant_id', (req, res) => {
//     const { restaurant_id } = req.params;

//     // Validate the restaurant_id
//     if (!restaurant_id) {
//         return res.status(400).json({ error: 'restaurant_id is required' });
//     }

//     // Simulate processing the restaurant_id
//     console.log(`Received restaurant_id: ${restaurant_id}`);

//     // Send a success response
//     res.status(200).json({ message: 'restaurant_id received successfully', restaurant_id });
// });

router.get('/get-restaurant-id/:restaurant_id', (req, res) => {
    const { restaurant_id } = req.params;

    // Validate the restaurant_id
    if (!restaurant_id) {
        return res.status(400).json({ error: 'restaurant_id is required' });
    }

    // Simulate processing the restaurant_id
    console.log(`Received restaurant_id: ${restaurant_id}`);

    // Notify the Python system about the restaurant ID
    axios.post('http://localhost:3003/update-restaurant', { restaurant_id })
        .then(response => {
            console.log('Successfully notified Python system:', response.data);
        })
        .catch(error => {
            console.error('Failed to notify Python system:', error.message);
        });

    // Send a success response to the Android app
    res.status(200).json({ message: 'restaurant_id received successfully', restaurant_id });
});

export default router;
