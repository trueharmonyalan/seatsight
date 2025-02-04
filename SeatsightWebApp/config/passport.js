import passport from "passport";
import { Strategy } from "passport-local";
import argon2d from "argon2";

import db from "./database.js";

passport.use(
  new Strategy(async function verify(usename, password, cb) {
    try {
      const result = await db.query("select * from users where email = $1", [
        usename,
      ]);

      if (result.rows.length > 0) {
        const user = result.rows[0];
        const storedHasH = user.password;

        if (await argon2d.verify(storedHasH, password)) {
          return cb(null, user);
        } else {
          return cb(null, false);
        }
      } else {
        console.log("user not found");
        return cb("user not found");
      }
    } catch (err) {
      console.error(err);
    }
  })
);

passport.serializeUser((user, cb) => {
  cb(null, user);
});

passport.deserializeUser((user, cb) => {
  cb(null, user);
});

export default passport;
