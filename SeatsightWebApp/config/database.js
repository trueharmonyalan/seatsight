// /config/db.js
import pg from "pg";
import env from "dotenv";

env.config();

const db = new pg.Client({
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  host: process.env.HOST,
  database: process.env.DB_DATABASE,
  port: process.env.PORT,
});

db.connect()
  .then(() => console.log("Connected to the database"))
  .catch((err) => console.error("Database connection error", err));

export default db;
