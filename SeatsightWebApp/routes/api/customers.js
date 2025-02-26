import express from "express";
import argon2 from "argon2";
import db from "../../config/database.js";

const router = express.Router();

router.post("/register", async (req, res) => {
    const { email, password } = req.body;

    if (!email || !password) {
        return res.status(400).json({ error: "Email and password are required." });
    }

    try {
        // ✅ Hash password
        const hashedPassword = await argon2.hash(password);

        // ✅ Insert user as "customer"
        const userResult = await db.query(
            "INSERT INTO users (email, password, role) VALUES ($1, $2, 'customer') RETURNING id, email",
            [email, hashedPassword]
        );

        res.json({
            message: "Registration successful",
            user: userResult.rows[0]
        });

    } catch (err) {
        console.error("Registration Error:", err);
        res.status(500).json({ error: "Registration failed" });
    }
});

export default router;
