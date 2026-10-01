package vn.edu.webexam.dao;

import java.sql.*;
import vn.edu.webexam.model.User_24162023;
import vn.edu.webexam.util.DBConnection_24162023;

public class UserDAO_24162023 {
  public User_24162023 findByEmail(String email) throws SQLException {
    try (var c = DBConnection_24162023.getConnection();
        var p = c.prepareStatement("SELECT TOP 1 * FROM users WHERE email=? ORDER BY id")) {
      p.setString(1, email);
      try (var r = p.executeQuery()) {
        if (!r.next()) return null;
        var u = new User_24162023();
        u.setId(r.getInt("id"));
        u.setEmail(r.getString("email"));
        u.setFullname(r.getString("fullname"));
        u.setPhone((Integer) r.getObject("phone"));
        u.setPasswd(r.getString("passwd"));
        u.setAdmin(r.getBoolean("is_admin"));
        var signup = r.getTimestamp("signup_date");
        var login = r.getTimestamp("last_login");
        u.setSignupDate(signup == null ? null : signup.toLocalDateTime());
        u.setLastLogin(login == null ? null : login.toLocalDateTime());
        return u;
      }
    }
  }

  public void create(User_24162023 u) throws SQLException {
    try (var c = DBConnection_24162023.getConnection()) {
      c.setAutoCommit(false);
      try {
        // SERIALIZABLE range locking prevents duplicate email registration without changing the
        // schema.
        try (var p =
            c.prepareStatement("SELECT id FROM users WITH (UPDLOCK,HOLDLOCK) WHERE email=?")) {
          p.setString(1, u.getEmail());
          try (var r = p.executeQuery()) {
            if (r.next()) throw new IllegalArgumentException("Email đã được đăng ký.");
          }
        }
        try (var p =
            c.prepareStatement(
                "INSERT users(email,fullname,phone,passwd,signup_date,is_admin)"
                    + " VALUES(?,?,?,?,GETDATE(),0)")) {
          p.setString(1, u.getEmail());
          p.setNString(2, u.getFullname());
          p.setObject(3, u.getPhone(), Types.INTEGER);
          p.setString(4, u.getPasswd());
          p.executeUpdate();
        }
        c.commit();
      } catch (SQLException | RuntimeException e) {
        c.rollback();
        throw e;
      }
    }
  }

  public void updateLastLogin(int id) throws SQLException {
    try (var c = DBConnection_24162023.getConnection();
        var p = c.prepareStatement("UPDATE users SET last_login=GETDATE() WHERE id=?")) {
      p.setInt(1, id);
      p.executeUpdate();
    }
  }
}
