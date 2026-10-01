package vn.edu.webexam.model;

public class User_24162023 implements java.io.Serializable {
  private int id;

  public int getId() {
    return id;
  }

  public void setId(int value) {
    this.id = value;
  }

  private String email;

  public String getEmail() {
    return email;
  }

  public void setEmail(String value) {
    this.email = value;
  }

  private String fullname;

  public String getFullname() {
    return fullname;
  }

  public void setFullname(String value) {
    this.fullname = value;
  }

  private Integer phone;

  public Integer getPhone() {
    return phone;
  }

  public void setPhone(Integer value) {
    this.phone = value;
  }

  private String passwd;

  public String getPasswd() {
    return passwd;
  }

  public void setPasswd(String value) {
    this.passwd = value;
  }

  private java.time.LocalDateTime signupDate;

  public java.time.LocalDateTime getSignupDate() {
    return signupDate;
  }

  public void setSignupDate(java.time.LocalDateTime value) {
    this.signupDate = value;
  }

  private java.time.LocalDateTime lastLogin;

  public java.time.LocalDateTime getLastLogin() {
    return lastLogin;
  }

  public void setLastLogin(java.time.LocalDateTime value) {
    this.lastLogin = value;
  }

  private boolean admin;

  public boolean isAdmin() {
    return admin;
  }

  public void setAdmin(boolean value) {
    this.admin = value;
  }
}
