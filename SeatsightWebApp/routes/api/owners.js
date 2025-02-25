import express from "express";
import argon2 from "argon2";
import db from "../../config/database.js";

const router = express.Router();

// ✅ Register Owner
router.post("/register", async (req, res) => {
    const { email, password } = req.body;

    try {
        const hashedPassword = await argon2.hash(password);
        const result = await db.query(
            "INSERT INTO owners (email, password) VALUES ($1, $2) RETURNING id, email",
            [email, hashedPassword]
        );
        res.json({ message: "Owner registered successfully", owner: result.rows[0] });
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Registration failed" });
    }
});

// ✅ Login Owner
router.post("/login", async (req, res) => {
    const { email, password } = req.body;

    try {
        const result = await db.query("SELECT * FROM owners WHERE email = $1", [email]);

        if (result.rows.length === 0) {
            return res.status(401).json({ error: "User not found" });
        }

        const owner = result.rows[0];

        if (await argon2.verify(owner.password, password)) {
            res.json({ message: "Login successful", owner_id: owner.id, email: owner.email });
        } else {
            res.status(401).json({ error: "Invalid credentials" });
        }
    } catch (err) {
        console.error(err);
        res.status(500).json({ error: "Login failed" });
    }
});

export default router;
