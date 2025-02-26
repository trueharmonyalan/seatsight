import express from "express";
import fetch from "node-fetch";

const router = express.Router();
const currentYear = new Date().getFullYear();

// ✅ Authentication middleware
const ensureAuthenticated = (req, res, next) => {
    if (req.isAuthenticated()) return next();
    res.redirect("/");
};

router.use(express.json());

// ✅ Get menu update page (Load everything in backend)
router.get("/menu", ensureAuthenticated, async (req, res) => {
    try {
        console.log("🔹 User ID:", req.user.id);

        // ✅ Fetch restaurant details from API
        const response = await fetch(`http://localhost:3001/api/restaurants/owner/${req.user.id}`);
        const restaurantData = await response.json();

        if (!response.ok || !restaurantData.id) {
            console.error("❌ No restaurant found for owner:", req.user.id);
            return res.render("update-menu.ejs", {
                year: currentYear,
                user: req.user,
                restaurant: null,
                menuItems: [],
                error: "No restaurant found. Please register your restaurant first.",
            });
        }

        const restaurant = restaurantData;
        console.log("✅ Restaurant Found:", restaurant);

        // ✅ Fetch menu items
        const menuResponse = await fetch(`http://localhost:3001/api/menu/${restaurant.id}`);
        const menuItems = await menuResponse.json();

        if (!menuResponse.ok) {
            console.error("❌ Failed to fetch menu items.");
            return res.render("update-menu.ejs", {
                year: currentYear,
                user: req.user,
                restaurant,
                menuItems: [],
                error: "Failed to load menu items.",
            });
        }

        console.log("✅ Menu Items Found:", menuItems);

        // ✅ Send data directly to frontend
        res.render("update-menu.ejs", {
            year: currentYear,
            user: req.user,
            restaurant,
            menuItems,
            error: null
        });

    } catch (error) {
        console.error("❌ Menu Fetch Error:", error);
        res.render("update-menu.ejs", {
            year: currentYear,
            user: req.user,
            restaurant: null,
            menuItems: [],
            error: "An unexpected error occurred.",
        });
    }
});

// ✅ Submit menu updates from frontend (POST)
router.post("/submit-items", ensureAuthenticated, async (req, res) => {
    console.log("🔹 Received menu update request:", req.body);

    const { items, removedItems } = req.body;

    try {
        // ✅ Get restaurant ID for owner
        const response = await fetch(`http://localhost:3001/api/restaurants/owner/${req.user.id}`);
        const restaurantData = await response.json();

        if (!response.ok || !restaurantData.id) {
            return res.status(404).json({ message: "No restaurant found for this owner." });
        }

        const restaurantId = restaurantData.id;

        // ✅ Send updates to API
        const apiResponse = await fetch("http://localhost:3001/api/menu/update", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                owner_id: req.user.id,
                restaurant_id: restaurantId,
                items,
                removedItems,
            }),
        });

        const result = await apiResponse.json();

        if (!apiResponse.ok) {
            return res.status(apiResponse.status).json(result);
        }

        console.log("✅ Menu updated successfully!");
        res.json({ message: "Menu updated successfully!" });

    } catch (error) {
        console.error("❌ Submit Error:", error);
        res.status(500).json({ message: "Error updating menu." });
    }
});

export default router;
