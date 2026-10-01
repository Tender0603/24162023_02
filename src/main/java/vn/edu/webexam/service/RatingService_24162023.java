package vn.edu.webexam.service;

import java.sql.SQLException;
import java.util.List;
import vn.edu.webexam.dao.RatingDAO_24162023;
import vn.edu.webexam.model.Rating_24162023;

public class RatingService_24162023 {
  private final RatingDAO_24162023 dao = new RatingDAO_24162023();

  public List<Rating_24162023> forBook(int id) throws SQLException {
    return dao.forBook(id);
  }

  public void save(int userId, int bookId, int rating, String review) throws SQLException {
    if (userId <= 0 || bookId <= 0)
      throw new IllegalArgumentException("Người dùng hoặc sách không hợp lệ.");
    if (rating < 1 || rating > 5) throw new IllegalArgumentException("Điểm đánh giá từ 1 đến 5.");
    if (review == null || review.isBlank() || review.length() > 4000)
      throw new IllegalArgumentException("Review cần 1–4000 ký tự.");
    dao.save(userId, bookId, rating, review.trim());
  }
}
