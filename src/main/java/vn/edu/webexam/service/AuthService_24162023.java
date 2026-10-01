package vn.edu.webexam.service;

import java.sql.SQLException;
import java.util.Locale;
import vn.edu.webexam.dao.UserDAO_24162023;
import vn.edu.webexam.model.User_24162023;
import vn.edu.webexam.util.PasswordUtil_24162023;

public class AuthService_24162023 {
  private final UserDAO_24162023 dao = new UserDAO_24162023();

  public User_24162023 login(String email, String password) throws SQLException {
    var u = dao.findByEmail(normalize(email));
    if (u == null
        || password == null
        || !PasswordUtil_24162023.matches(u.getEmail(), password, u.getPasswd())) return null;
    dao.updateLastLogin(u.getId());
    u.setPasswd(null);
    return u;
  }

  public User_24162023 prepareRegistration(String email, String name, String phone, String password)
      throws SQLException {
    email = normalize(email);
    if (email.length() > 50
        || !email.matches("[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"))
      throw new IllegalArgumentException("Email không hợp lệ (tối đa 50 ký tự).");
    if (name == null || name.isBlank() || name.length() > 50)
      throw new IllegalArgumentException("Họ tên cần 1–50 ký tự.");
    if (password == null || password.length() < 8 || password.length() > 128)
      throw new IllegalArgumentException("Mật khẩu cần 8–128 ký tự.");
    Integer number = null;
    if (phone != null && !phone.isBlank()) {
      try {
        if (!phone.matches("[0-9]{1,10}")) throw new NumberFormatException();
        number = Integer.valueOf(phone);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException(
            "Điện thoại phải là số trong phạm vi SQL int (0–2147483647).");
      }
    }
    if (dao.findByEmail(email) != null)
      throw new IllegalArgumentException("Email đã được đăng ký.");
    var u = new User_24162023();
    u.setEmail(email);
    u.setFullname(name.trim());
    u.setPhone(number);
    u.setPasswd(PasswordUtil_24162023.hash(email, password));
    return u;
  }

  public void activate(User_24162023 user) throws SQLException {
    dao.create(user);
  }

  private String normalize(String s) {
    return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
  }
}
