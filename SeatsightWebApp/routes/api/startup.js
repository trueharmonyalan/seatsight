import express from "express"
import env from "dotenv";

import "../../config/database.js";

// Import API routes
import ownerRoutes from "./owners.js";
import restaurantRoutes from "./restaurants.js";
import menuRoutes from "./menu.js";
import seatRoutes from "./seats.js";
import customerRoutes from "./customers.js";
import test from "./test.js";
import bookings from "./bookings.js"


const app = express()
const port = process.env.API_PORT || 3001;
env.config();

app.use(express.json());
app.use(express.static("public"));
app.use(express.urlencoded({ extended: true }));

//API routes for web app
app.use("/api/owners", ownerRoutes);
app.use("/api/restaurants", restaurantRoutes);
app.use("/api/menu", menuRoutes);
app.use("/api/seats", seatRoutes);
app.use("/api/customers", customerRoutes);
app.use("/api/test", test);
app.use("/api/bookings", bookings);


console.log("DB_PASSWORD TYPE:", typeof process.env.DB_PASSWORD);
console.log("DB_PASSWORD VALUE:", process.env.DB_PASSWORD);






app.listen(port,()=>{
    console.log(`api server is running on port ${port}`)
})

