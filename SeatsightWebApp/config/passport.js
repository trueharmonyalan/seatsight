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
import { Strategy} from "passport-local";
import argon2 from "argon2";  // Use correct argon2 import
import fetch from "node-fetch"; // Ensure fetch is available
passport.use(
  new Strategy({ usernameField: "email" }, async (email, password, done) => {
    console.log("Passport Debug: Login attempt ->", { email, password });

    if (!email) return done(null, false, { message: "Enter email" });
    if (!password) return done(null, false, { message: "Enter password" });

    try {
      const response = await fetch("http://localhost:3001/api/owners/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });

      console.log("Passport Debug: API Response Status ->", response.status);

      const data = await response.json();
      console.log("Passport Debug: API Response Data ->", data);

      if (!response.ok) {
        return done(null, false, { message: data.error || "Login failed" });
      }

      if (!data.user || !data.user.id) {
        console.error("Passport Debug: No valid user returned from API.");
        return done(null, false, { message: "Invalid response from API" });
      }

      return done(null, { id: data.user.id, email: data.user.email });

    } catch (err) {
      console.error("Passport Debug: Error during authentication:", err);
      return done(err);
    }
  })
);


passport.serializeUser((user, done) => {
  done(null, user.id);
});

passport.deserializeUser(async (id, done) => {
  try {
    const response = await fetch(`http://localhost:3001/api/owners/${id}`);
    const user = await response.json();
    if (!response.ok || !user || !user.id) {
      return done(new Error("User not found"));
    }
    done(null, user);
  } catch (err) {
    console.error("Passport: Error during deserialization:", err);
    done(err);
  }
});


export default passport;
