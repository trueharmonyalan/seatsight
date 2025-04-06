import express from "express";
import db from "../../config/database.js";
import passport from "passport";

const router = express.Router();
router.use(express.json());

// Create a new booking
router.post("/", async (req, res) => {
    // Updated to match Android app data structure
    const { customerId, restaurantId, selectedSeats, selectedMenu, startTime, endTime } = req.body;

    // Validation with proper error messages
    if (!customerId) return res.status(400).json({ message: "Customer ID is required" });
    if (!restaurantId) return res.status(400).json({ message: "Restaurant ID is required" });
    if (!startTime) return res.status(400).json({ message: "Start time is required" });
    if (!endTime) return res.status(400).json({ message: "End time is required" });
    if (!selectedSeats || !Array.isArray(selectedSeats) || selectedSeats.length === 0) 
        return res.status(400).json({ message: "At least one seat must be selected" });

    try {
        await db.query("BEGIN");

        // 1. Create booking record
        const bookingResult = await db.query(
            "INSERT INTO bookings (customer_id, restaurant_id, booking_time_start, booking_time_end, status) VALUES ($1, $2, $3, $4, $5) RETURNING id",
            [customerId, restaurantId, startTime, endTime, "confirmed"]
        );

        const bookingId = bookingResult.rows[0].id;

        // 2. Process selected seats - UPDATED FOR NEW SCHEMA
        const seatInsertQuery = `
            INSERT INTO booking_seats (booking_id, seat_id, seat_number)
            SELECT $1, s.id, s.seat_number
            FROM seats s
            WHERE s.restaurant_id = $2 AND s.seat_number = ANY($3)
            RETURNING seat_id
        `;
        
        const seatResult = await db.query(seatInsertQuery, [
            bookingId, 
            restaurantId, 
            selectedSeats
        ]);
        
        // Check if all seats were found and inserted
        if (seatResult.rows.length !== selectedSeats.length) {
            throw new Error(`Some seats were not found. Expected ${selectedSeats.length} seats, but only ${seatResult.rows.length} were processed.`);
        }
        
        // Update seat status - CORRECTED TO ONLY UPDATE is_booked FIELD
        const updateSeatsQuery = `
            UPDATE seats 
            SET is_booked = TRUE
            WHERE restaurant_id = $1 AND seat_number = ANY($2)
        `;
        
        await db.query(updateSeatsQuery, [restaurantId, selectedSeats]);

        // 3. Process selected menu items (if any) - UPDATED FOR NEW SCHEMA
        if (selectedMenu && Object.keys(selectedMenu).length > 0) {
            const menuItemNames = Object.keys(selectedMenu).filter(name => selectedMenu[name] > 0);
            
            if (menuItemNames.length > 0) {
                // First get menu item info
                const menuItemsPromises = menuItemNames.map(name => {
                    return db.query(
                        "SELECT id, name FROM menu_items WHERE restaurant_id = $1 AND name = $2",
                        [restaurantId, name]
                    );
                });
                
                const menuItemsResults = await Promise.all(menuItemsPromises);
                
                // Create values for bulk insert
                const menuItemValues = [];
                
                for (let i = 0; i < menuItemsResults.length; i++) {
                    const menuItemName = menuItemNames[i];
                    const result = menuItemsResults[i];
                    
                    if (result.rows.length === 0) {
                        throw new Error(`Menu item "${menuItemName}" not found`);
                    }
                    
                    const menuItem = result.rows[0];
                    menuItemValues.push([
                        bookingId,
                        menuItem.id,
                        menuItem.name, // Store the actual name
                        selectedMenu[menuItemName]
                    ]);
                }
                
                // If we have any valid menu items
                if (menuItemValues.length > 0) {
                    // Build the query dynamically
                    const placeholders = menuItemValues.map((_, i) => 
                        `($${i*4+1}, $${i*4+2}, $${i*4+3}, $${i*4+4})`
                    );
                    
                    const params = menuItemValues.flat();
                    
                    const menuItemInsertQuery = `
                        INSERT INTO booking_menu_items (booking_id, menu_item_id, menu_item_name, quantity)
                        VALUES ${placeholders.join(', ')}
                    `;
                    
                    await db.query(menuItemInsertQuery, params);
                }
            }
        }

        await db.query("COMMIT");
        
        // Return a more informative success response
        res.status(201).json({ 
            success: true,
            message: "Booking created successfully!",
            data: {
                bookingId: bookingId,
                customerId: customerId,
                restaurantId: restaurantId,
                startTime: startTime,
                endTime: endTime,
                selectedSeatsCount: selectedSeats.length,
                hasMenuItems: selectedMenu && Object.keys(selectedMenu).length > 0
            }
        });

    } catch (error) {
        await db.query("ROLLBACK");
        console.error("Booking Creation Error:", error);
        res.status(500).json({ 
            success: false,
            message: "Error creating booking", 
            error: error.message 
        });
    }
});

// Get bookings for a specific customer
router.get("/customer/:customerId", async (req, res) => {
    try {
        const customerId = req.params.customerId;
        
        const bookingsResult = await db.query(`
            SELECT b.id, b.restaurant_id, r.name as restaurant_name, 
                   b.booking_time_start, b.booking_time_end, b.status, 
                   b.created_at
            FROM bookings b
            JOIN restaurants r ON b.restaurant_id = r.id
            WHERE b.customer_id = $1
            ORDER BY b.created_at DESC
        `, [customerId]);
        
        const bookings = [];
        
        for (const booking of bookingsResult.rows) {
            // Get seats for this booking
            const seatsResult = await db.query(`
                SELECT s.seat_number
                FROM booking_seats bs
                JOIN seats s ON bs.seat_id = s.id
                WHERE bs.booking_id = $1
            `, [booking.id]);
            
            // Get menu items for this booking
            const menuItemsResult = await db.query(`
                SELECT mi.name, mi.price, bmi.quantity
                FROM booking_menu_items bmi
                JOIN menu_items mi ON bmi.menu_item_id = mi.id
                WHERE bmi.booking_id = $1
            `, [booking.id]);
            
            bookings.push({
                id: booking.id,
                restaurantId: booking.restaurant_id,
                restaurantName: booking.restaurant_name,
                startTime: booking.booking_time_start,
                endTime: booking.booking_time_end,
                status: booking.status,
                createdAt: booking.created_at,
                seats: seatsResult.rows.map(row => row.seat_number),
                menuItems: menuItemsResult.rows.map(row => ({
                    name: row.name,
                    price: row.price,
                    quantity: row.quantity
                }))
            });
        }
        
        res.json({
            success: true,
            data: bookings
        });
    } catch (error) {
        console.error("Get Customer Bookings Error:", error);
        res.status(500).json({ 
            success: false,
            message: "Error retrieving bookings", 
            error: error.message 
        });
    }
});

// Get booking details by ID
router.get("/:bookingId", async (req, res) => {
    try {
        const bookingId = req.params.bookingId;
        
        const bookingResult = await db.query(`
            SELECT b.id, b.customer_id, b.restaurant_id, r.name as restaurant_name, 
                   b.booking_time_start, b.booking_time_end, b.status, 
                   b.created_at, b.updated_at
            FROM bookings b
            JOIN restaurants r ON b.restaurant_id = r.id
            WHERE b.id = $1
        `, [bookingId]);
        
        if (bookingResult.rows.length === 0) {
            return res.status(404).json({ 
                success: false,
                message: "Booking not found" 
            });
        }
        
        const booking = bookingResult.rows[0];
        
        // Get seats for this booking
        const seatsResult = await db.query(`
            SELECT s.id, s.seat_number
            FROM booking_seats bs
            JOIN seats s ON bs.seat_id = s.id
            WHERE bs.booking_id = $1
        `, [bookingId]);
        
        // Get menu items for this booking
        const menuItemsResult = await db.query(`
            SELECT mi.id, mi.name, mi.price, bmi.quantity
            FROM booking_menu_items bmi
            JOIN menu_items mi ON bmi.menu_item_id = mi.id
            WHERE bmi.booking_id = $1
        `, [bookingId]);
        
        res.json({
            success: true,
            data: {
                id: booking.id,
                customerId: booking.customer_id,
                restaurantId: booking.restaurant_id,
                restaurantName: booking.restaurant_name,
                startTime: booking.booking_time_start,
                endTime: booking.booking_time_end,
                status: booking.status,
                createdAt: booking.created_at,
                updatedAt: booking.updated_at,
                seats: seatsResult.rows.map(row => ({
                    id: row.id,
                    seatNumber: row.seat_number
                })),
                menuItems: menuItemsResult.rows.map(row => ({
                    id: row.id,
                    name: row.name,
                    price: row.price,
                    quantity: row.quantity
                }))
            }
        });
    } catch (error) {
        console.error("Get Booking Details Error:", error);
        res.status(500).json({ 
            success: false,
            message: "Error retrieving booking details", 
            error: error.message 
        });
    }
});

// Cancel booking
router.patch("/:bookingId/cancel", async (req, res) => {
    try {
        const bookingId = req.params.bookingId;
        
        await db.query("BEGIN");
        
        // Update booking status
        await db.query(
            "UPDATE bookings SET status = $1, updated_at = NOW() WHERE id = $2",
            ["cancelled", bookingId]
        );
        
        // Get seats associated with this booking
        const seatsResult = await db.query(
            "SELECT seat_id FROM booking_seats WHERE booking_id = $1",
            [bookingId]
        );
        
        // Update seat status
        for (const row of seatsResult.rows) {
            await db.query(
                "UPDATE seats SET is_booked = FALSE, status = $1 WHERE id = $2",
                ["vacant", row.seat_id]
            );
        }
        
        await db.query("COMMIT");
        
        res.json({ 
            success: true,
            message: "Booking cancelled successfully" 
        });
    } catch (error) {
        await db.query("ROLLBACK");
        console.error("Cancel Booking Error:", error);
        res.status(500).json({ 
            success: false,
            message: "Error cancelling booking", 
            error: error.message 
        });
    }
});

// Add this to your existing bookingRoutes.js file

// Get all bookings with real-time seat status for a specific restaurant
router.get("/restaurant/:restaurantId", async (req, res) => {
    try {
        const restaurantId = req.params.restaurantId;
        
        if (!restaurantId) {
            return res.status(400).json({ 
                success: false,
                message: "Restaurant ID is required" 
            });
        }
        
        // First, get restaurant information
        const restaurantResult = await db.query(`
            SELECT name FROM restaurants WHERE id = $1
        `, [restaurantId]);
        
        if (restaurantResult.rows.length === 0) {
            return res.status(404).json({
                success: false,
                message: "Restaurant not found"
            });
        }
        
        const restaurantName = restaurantResult.rows[0].name;
        
        // Get all bookings for this restaurant
        const bookingsResult = await db.query(`
            SELECT b.id, b.customer_id, u.email as customer_email,
                   b.booking_time_start, b.booking_time_end, b.status,
                   b.created_at
            FROM bookings b
            JOIN users u ON b.customer_id = u.id
            WHERE b.restaurant_id = $1 AND b.status = 'confirmed'
            ORDER BY b.booking_time_start ASC
        `, [restaurantId]);
        
        const bookings = [];
        
        // Process each booking to get details
        for (const booking of bookingsResult.rows) {
            // Get seats for this booking with their current status
            const seatsResult = await db.query(`
                SELECT s.seat_number, s.status as current_status, s.is_booked
                FROM booking_seats bs
                JOIN seats s ON bs.seat_id = s.id
                WHERE bs.booking_id = $1
            `, [booking.id]);
            
            // Get menu items for this booking
            const menuItemsResult = await db.query(`
                SELECT mi.name, bmi.quantity
                FROM booking_menu_items bmi
                JOIN menu_items mi ON bmi.menu_item_id = mi.id
                WHERE bmi.booking_id = $1
            `, [booking.id]);
            
            // Format menu items as "name (quantity)"
            const formattedMenuItems = menuItemsResult.rows.map(item => 
                `${item.name} (${item.quantity})`
            ).join(", ");
            
            // Add to bookings array
            bookings.push({
                id: booking.id,
                customerEmail: booking.customer_email,
                startTime: booking.booking_time_start,
                endTime: booking.booking_time_end,
                bookingStatus: booking.status,
                selectedSeats: seatsResult.rows.map(row => row.seat_number).join(", "),
                selectedMenuItems: formattedMenuItems,
                seatStatus: seatsResult.rows.map(seat => 
                    `${seat.seat_number} (${seat.current_status})`
                ).join(", ")
            });
        }
        
        // Get all seats for the restaurant with their current status
        const allSeatsResult = await db.query(`
            SELECT seat_number, status, is_booked
            FROM seats
            WHERE restaurant_id = $1
            ORDER BY seat_number
        `, [restaurantId]);
        
        // Get count of booked seats
        const bookedSeatsResult = await db.query(`
            SELECT COUNT(*) as count
            FROM seats
            WHERE restaurant_id = $1 AND is_booked = true
        `, [restaurantId]);
        
        // Get total seats
        const totalSeatsResult = await db.query(`
            SELECT COUNT(*) as count
            FROM seats
            WHERE restaurant_id = $1
        `, [restaurantId]);
        
        const bookedSeats = parseInt(bookedSeatsResult.rows[0].count);
        const totalSeats = parseInt(totalSeatsResult.rows[0].count);
        
        res.json({
            success: true,
            data: {
                restaurantName: restaurantName,
                bookings: bookings,
                allSeats: allSeatsResult.rows,
                seats: {
                    bookedSeats: bookedSeats,
                    totalSeats: totalSeats
                }
            }
        });
    } catch (error) {
        console.error("Get Restaurant Bookings Error:", error);
        res.status(500).json({ 
            success: false,
            message: "Error retrieving restaurant bookings and seat status", 
            error: error.message 
        });
    }
});




export default router;