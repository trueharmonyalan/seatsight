import express from "express";
import argon2 from "argon2";
import db from "../../config/database.js";

const router = express.Router();
router.use(express.json());

router.post("/register", async (req, res) => {
    const { email, password, restaurant_name } = req.body;  // ✅ Ensure consistency

    if (!email || !password || !restaurant_name) {
        return res.status(400).json({ error: "Email, password, and restaurant name are required." });
    }

    try {
        await db.query("BEGIN");  // ✅ Start transaction

        // ✅ Hash password and insert owner
        const hashedPassword = await argon2.hash(password);
        const ownerResult = await db.query(
            "INSERT INTO users (email, password, role) VALUES ($1, $2, 'owner') RETURNING id, email",
            [email, hashedPassword]
        );

        const ownerId = ownerResult.rows[0].id;

        // ✅ Insert restaurant for the owner
        const restaurantResult = await db.query(
            "INSERT INTO restaurants (owner_id, name) VALUES ($1, $2) RETURNING id, name",
            [ownerId, restaurant_name]
        );

        await db.query("COMMIT");  // ✅ Commit transaction

        console.log("✅ Owner and Restaurant Registered:", {
            user: ownerResult.rows[0],
            restaurant: restaurantResult.rows[0],
        });

        res.json({
            message: "Owner registered successfully",
            user: ownerResult.rows[0],
            restaurant: restaurantResult.rows[0],
        });

    } catch (err) {
        await db.query("ROLLBACK");  // ❌ Rollback if an error occurs
        console.error("❌ Registration Error:", err);
        res.status(500).json({ error: "Registration failed" });
    }
});

router.post("/login", async (req, res) => {
    const { email, password } = req.body;

    console.log("API Debug: Login Request ->", { email, password });

    try {
        const result = await db.query(
            "SELECT * FROM users WHERE email = $1 AND role = 'owner'", 
            [email]
        );

        if (result.rows.length === 0) {
            console.log("API Debug: Owner not found");
            return res.status(401).json({ error: "Owner not found" });
        }

        const user = result.rows[0];

        console.log("API Debug: Fetched user from DB ->", user);

        const isPasswordValid = await argon2.verify(user.password, password);
        console.log("API Debug: Password verification result ->", isPasswordValid);

        if (!isPasswordValid) {
            return res.status(401).json({ error: "Invalid credentials" });
        }

        res.json({
            message: "Login successful",
            user: { id: user.id, email: user.email }
        });

    } catch (err) {
        console.error("API Debug: Login Error", err);
        res.status(500).json({ error: "Login failed" });
    }
});


router.get("/:id", async (req, res) => {
    const { id } = req.params;
    try {
        const result = await db.query(
            "SELECT u.id, u.email, r.id AS restaurant_id, r.name AS restaurant_name FROM users u LEFT JOIN restaurants r ON u.id = r.owner_id WHERE u.id = $1 AND u.role = 'owner'",
            [id]
        );

        if (result.rows.length === 0) {
            console.error(`API Error: Owner with ID ${id} not found`);
            return res.status(404).json({ error: "Owner not found" });
        }

        console.log(`API Response:`, result.rows[0]); // ✅ Debugging
        res.json(result.rows[0]);

    } catch (err) {
        console.error("API Error: Database error", err);
        res.status(500).json({ error: "Database error" });
    }
});





export default router;
