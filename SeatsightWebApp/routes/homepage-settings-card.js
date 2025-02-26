import express from "express";
import fetch from "node-fetch";

const router = express.Router();
const currentYear = new Date().getFullYear();

const ensureAuthenticated = (req, res, next) => {
    if (req.isAuthenticated()) return next();
    res.redirect("/");
};

// ✅ Load Settings Page with Current IP URL
router.get("/settings", ensureAuthenticated, async (req, res) => {
    try {
        const response = await fetch(`http://localhost:3001/api/restaurants/owner/${req.user.id}`);
        const restaurantData = await response.json();

        if (!response.ok) {
            console.error("Error fetching restaurant data:", restaurantData.error);
            return res.render("homepage-settings-card.ejs", {
                year: currentYear,
                info: { showFromUrlState: false, showUrl: null },
            });
        }

        res.render("homepage-settings-card.ejs", {
            year: currentYear,
            info: {
                showFromUrlState: !!restaurantData.ip_camera_url,
                showUrl: restaurantData.ip_camera_url || "Save URL here",
            },
        });

    } catch (error) {
        console.error("Error loading settings:", error);
        res.render("homepage-settings-card.ejs", {
            year: currentYear,
            info: { showFromUrlState: false, showUrl: "Save URL here" },
        });
    }
});

// ✅ Update IP Camera URL via API
router.post("/settings", ensureAuthenticated, async (req, res) => {
    const ipurl = req.body.ipurl;

    try {
        const response = await fetch("http://localhost:3001/api/restaurants/update-ip-url", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                owner_id: req.user.id,
                ip_camera_url: ipurl,
            }),
        });

        const result = await response.json();
        if (!response.ok) throw new Error(result.error || "Failed to update URL.");

        res.redirect("/settings");

    } catch (error) {
        console.error("Update Error:", error);
        res.redirect("/settings");
    }
});

export default router;
