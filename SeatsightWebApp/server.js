import express from "express"
import env from "dotenv"

// Import route modules
import indexRoutes from "./routes/index.js";
import authRoutes from "./routes/authentication.js";
// Optionally import db configuration to initialize connection
import "./config/database.js";


env.config()
const app = express()
const port = process.env.APP_PORT || 3000;



app.use(express.static('public'));
app.use(express.urlencoded({extended:true}))



// Setup view engine (assuming you're using ejs)
app.set("view engine", "ejs");

// Use routes
app.use("/", indexRoutes);
app.use("/", authRoutes);









app.listen(port,()=>{
    console.log(`server is running on port ${port}`)
})