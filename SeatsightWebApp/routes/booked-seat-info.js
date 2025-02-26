import express from "express";
import fetch from "node-fetch";

const router = express.Router();
const currentYear = new Date().getFullYear();

const ensureAuthenticated = (req, res, next) => {
    if (req.isAuthenticated()) return next();
    res.redirect("/");
};

// ✅ Fetch seat info dynamically via API and render it in EJS
router.get("/bookedInfo", ensureAuthenticated, async (req, res) => {
    try {
        console.log("User ID:", req.user.id);

        // ✅ Fetch restaurant details using API
        const restaurantResponse = await fetch(`http://localhost:3001/api/restaurants/owner/${req.user.id}`);
        const restaurantData = await restaurantResponse.json();

        if (!restaurantResponse.ok || !restaurantData.id) {
            return res.render("booked-seat-info.ejs", {
                year: currentYear,
                restaurant: null,
                restaurantName: null,
                error: "No restaurant found.",
            });
        }

        // ✅ Fetch seat status using API
        const seatStatusResponse = await fetch(`http://localhost:3001/api/seats/status/${restaurantData.id}`);
        const seatStatusData = await seatStatusResponse.json();

        if (!seatStatusResponse.ok) {
            return res.render("booked-seat-info.ejs", {
                year: currentYear,
                restaurant: restaurantData,
                restaurantName: restaurantData.name, // ✅ Pass restaurant name
                error: "Failed to load seat data.",
            });
        }

        res.render("booked-seat-info.ejs", {
            year: currentYear,
            restaurant: restaurantData,
            restaurantName: restaurantData.name, // ✅ Pass restaurant name
            seats: seatStatusData, 
        });

    } catch (error) {
        console.error("Error loading booked seat info:", error);
        res.render("booked-seat-info.ejs", {
            year: currentYear,
            restaurant: null,
            restaurantName: null,
            error: "Failed to load data.",
        });
    }
});

// ✅ Update Seating Capacity (Backend-Processed API Call)
router.post("/update-seating", ensureAuthenticated, async (req, res) => {
    try {
        const { seating_capacity } = req.body;

        // ✅ Fetch the restaurant ID first
        const restaurantResponse = await fetch(`http://localhost:3001/api/restaurants/owner/${req.user.id}`);
        const restaurantData = await restaurantResponse.json();

        if (!restaurantResponse.ok || !restaurantData.id) {
            return res.status(404).json({ error: "No restaurant found for this owner." });
        }

        // ✅ Make the API call to update the seating capacity
        const updateResponse = await fetch("http://localhost:3001/api/seats/update", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded" },
            body: new URLSearchParams({
                restaurant_id: restaurantData.id,
                seating_capacity
            }).toString(),
        });

        if (!updateResponse.ok) {
            throw new Error("Failed to update capacity.");
        }

        // ✅ Redirect instead of sending JSON response
        res.redirect("/bookedInfo");

    } catch (error) {
        console.error("Update Error:", error);
        res.status(500).json({ error: "Failed to update capacity." });
    }
});

export default router;
