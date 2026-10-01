package vn.edu.webexam.dao;

import java.sql.*;
import java.util.*;
import vn.edu.webexam.model.Rating_24162023;
import vn.edu.webexam.util.DBConnection_24162023;

public class RatingDAO_24162023 {
  public List<Rating_24162023> forBook(int id) throws SQLException {
    var list = new ArrayList<Rating_24162023>();
    try (var c = DBConnection_24162023.getConnection();
        var p =
            c.prepareStatement(
                "SELECT r.*,COALESCE(u.fullname,u.email) fullname FROM rating r JOIN users u ON"
                    + " u.id=r.userid WHERE r.bookid=? ORDER BY r.userid")) {
      p.setInt(1, id);
      try (var r = p.executeQuery()) {
        while (r.next()) {
          var v = new Rating_24162023();
          v.setUserid(r.getInt("userid"));
          v.setBookid(id);
          v.setRating(r.getObject("rating") == null ? null : r.getInt("rating"));
          v.setReviewText(r.getString("review_text"));
          v.setFullname(r.getString("fullname"));
          list.add(v);
        }
      }
    }
    return list;
  }

  public void save(int userId, int bookId, int rating, String review) throws SQLException {
    try (var c = DBConnection_24162023.getConnection()) {
      c.setAutoCommit(false);
      try {
        try (var p =
            c.prepareStatement("SELECT bookid FROM books WITH (HOLDLOCK) WHERE bookid=?")) {
          p.setInt(1, bookId);
          try (var r = p.executeQuery()) {
            if (!r.next()) throw new IllegalArgumentException("Sách không tồn tại.");
          }
        }
        boolean exists;
        try (var p =
            c.prepareStatement(
                "SELECT userid FROM rating WITH (UPDLOCK,HOLDLOCK) WHERE userid=? AND bookid=?")) {
          p.setInt(1, userId);
          p.setInt(2, bookId);
          try (var r = p.executeQuery()) {
            exists = r.next();
          }
        }
        String sql =
            exists
                ? "UPDATE rating SET rating=?,review_text=? WHERE userid=? AND bookid=?"
                : "INSERT rating(rating,review_text,userid,bookid) VALUES(?,?,?,?)";
        try (var p = c.prepareStatement(sql)) {
          p.setInt(1, rating);
          p.setString(2, review);
          p.setInt(3, userId);
          p.setInt(4, bookId);
          p.executeUpdate();
        }
        c.commit();
      } catch (SQLException | RuntimeException e) {
        c.rollback();
        throw e;
      }
    }
  }
}
