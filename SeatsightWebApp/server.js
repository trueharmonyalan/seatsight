import express from "express";
import env from "dotenv";
import session from "express-session";

// Import route modules
import indexRoutes from "./routes/index.js";
import authRoutes from "./routes/authentication.js";
import homePageSettingcard from "./routes/homepage-settings-card.js"
import bookedSeats from "./routes/booked-seat-info.js"
import updateMenu from "./routes/update-menu.js"
// Optionally import db configuration to initialize connection
import "./config/database.js";

// authentication
import passport from "./config/passport.js";


// // Import API routes
// import ownerRoutes from "./routes/api/owners.js";
// import restaurantRoutes from "./routes/api/restaurants.js";
// import menuRoutes from "./routes/api/menu.js";
// import seatRoutes from "./routes/api/seats.js";
// import customerRoutes from "./routes/api/customers.js";




env.config();
const app = express();
const port = process.env.APP_PORT || 3000;

// session implementation
app.use(
  session({
    secret: process.env.SESSION_SECRET,
    resave: false,
    saveUninitialized: true,
    cookie: {
      maxAge: 1000 * 60 * 60, // 1 hour
    },
  })
);

app.use(passport.initialize());
app.use(passport.session());

app.use(express.static("public"));
app.use(express.urlencoded({ extended: true }));

// Setup view engine (assuming you're using ejs)
app.set("view engine", "ejs");

//routes
app.use("/", indexRoutes);
app.use("/", authRoutes);
app.use("/",homePageSettingcard)
app.use("/",bookedSeats)
app.use("/",updateMenu)








app.listen(port, "0.0.0.0", ()=>{
    console.log(`server is running on port ${port}`)
})