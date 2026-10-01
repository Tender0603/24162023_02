package vn.edu.webexam.model;

import java.time.Instant;

public class PendingRegistration_24162023 implements java.io.Serializable {
  private final User_24162023 user;
  private final String otp;
  private final Instant expiresAt;
  private int attempts;

  public PendingRegistration_24162023(User_24162023 user, String otp, Instant expiresAt) {
    this.user = user;
    this.otp = otp;
    this.expiresAt = expiresAt;
  }

  public User_24162023 getUser() {
    return user;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public synchronized boolean verify(String input) {
    if (!Instant.now().isBefore(expiresAt) || attempts >= 5)
      throw new IllegalArgumentException(
          "OTP đã hết hạn hoặc vượt quá 5 lần thử. Hãy đăng ký lại.");
    attempts++;
    return otp.equals(input);
  }
}
