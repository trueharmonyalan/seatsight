// import passport from "passport";
// import { Strategy } from "passport-local";
// import argon2d from "argon2";

// import db from "./database.js";

// passport.use(
//   new Strategy(async function verify(usename, password, cb) {
//     try {
//       const result = await db.query("select * from users where username = $1", [
//         usename,
//       ]);

//       if (result.rows.length > 0) {
//         const user = result.rows[0];
//         const storedHasH = user.password;

//         if (await argon2d.verify(storedHasH, password)) {
//           return cb(null, user);
//         } else {
//           return cb(null, false);
//         }
//       } else {
//         console.log("user not found");
//         return cb("user not found");
//       }
//     } catch (err) {
//       console.error(err);
//     }
//   })
// );

// passport.serializeUser((user, cb) => {
//   cb(null, user);
// });

// passport.deserializeUser((user, cb) => {
//   cb(null, user);
// });

// export default passport;


import passport from "passport";
import { Strategy } from "passport-local";
import argon2 from "argon2";  // Use correct argon2 import
import fetch from "node-fetch"; // Ensure fetch is available

passport.use(
  new Strategy({ usernameField: "email" }, async (email, password, done) => {
    if (!email) return done(null, false, { message: "Enter email" });
    if (!password) return done(null, false, { message: "Enter password" });

    try {
      const response = await fetch("http://localhost:3001/api/owners/login", { // ✅ Ensure correct port
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });

      const data = await response.json();
      
      if (!response.ok) {
        return done(null, false, { message: data.error || "Login failed" });
      }

      // ✅ Fix: Only store `owner_id` and `email` (do not return password)
      return done(null, { id: data.owner_id, email: data.email });
    } catch (err) {
      console.error("Passport: Error during authentication:", err);
      return done(err);
    }
  })
);

// ✅ Serialize User (Save only user ID in session)
passport.serializeUser((user, done) => {
  done(null, user.id);
});

// ✅ Deserialize User (Fetch owner details from API)
passport.deserializeUser(async (id, done) => {
  try {
    const response = await fetch(`http://localhost:3001/api/owners/${id}`);
    const user = await response.json();

    if (!response.ok) {
      return done(new Error("User not found"));
    }

    done(null, user);
  } catch (err) {
    done(err);
  }
});


export default passport;
