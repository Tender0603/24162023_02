package vn.edu.webexam.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import vn.edu.webexam.model.PendingRegistration_24162023;
import vn.edu.webexam.model.User_24162023;
import vn.edu.webexam.util.PasswordUtil_24162023;

class SecurityTest_24162023 {
  @Test
  void seededPasswordMatchesSchemaAndRejectsWrongPassword() {
    String hash = PasswordUtil_24162023.hash("admin@example.com", "Admin@123");
    assertEquals("zA/wK7Gm5s0gnTVfLsrXT2S905f1bYNr", hash);
    assertEquals(32, hash.length());
    assertFalse(PasswordUtil_24162023.matches("admin@example.com", "wrong", hash));
    assertNotEquals(hash, PasswordUtil_24162023.hash("user1@example.com", "Admin@123"));
  }

  @Test
  void expiredOtpCannotActivate() {
    var pending =
        new PendingRegistration_24162023(
            new User_24162023(), "123456", Instant.now().minusSeconds(1));
    assertThrows(IllegalArgumentException.class, () -> pending.verify("123456"));
  }

  @Test
  void otpStopsAfterFiveAttempts() {
    var pending =
        new PendingRegistration_24162023(
            new User_24162023(), "123456", Instant.now().plusSeconds(300));
    for (int i = 0; i < 5; i++) assertFalse(pending.verify("000000"));
    assertThrows(IllegalArgumentException.class, () -> pending.verify("123456"));
  }

  @Test
  void correctOtpAndReviewValidation() {
    var pending =
        new PendingRegistration_24162023(
            new User_24162023(), "123456", Instant.now().plusSeconds(300));
    assertTrue(pending.verify("123456"));
    var ratings = new RatingService_24162023();
    assertThrows(IllegalArgumentException.class, () -> ratings.save(1, 1, 6, "Text"));
    assertThrows(IllegalArgumentException.class, () -> ratings.save(1, 1, 5, " "));
    assertThrows(IllegalArgumentException.class, () -> ratings.save(0, 1, 5, "Text"));
  }
}
