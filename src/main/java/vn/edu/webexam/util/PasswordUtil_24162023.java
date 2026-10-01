package vn.edu.webexam.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Locale;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil_24162023 {
  private PasswordUtil_24162023() {}

  // 192-bit PBKDF2 output encoded as 32 Base64 characters, fitting passwd varchar(32).
  // Email is the per-account salt because the mandated schema has no salt column.
  public static String hash(String email, String password) {
    try {
      var spec =
          new PBEKeySpec(
              password.toCharArray(),
              ("WebExam02:" + email.trim().toLowerCase(Locale.ROOT))
                  .getBytes(StandardCharsets.UTF_8),
              210000,
              192);
      try {
        return Base64.getEncoder()
            .encodeToString(
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded());
      } finally {
        spec.clearPassword();
      }
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public static boolean matches(String email, String password, String expected) {
    return expected != null
        && MessageDigest.isEqual(
            hash(email, password).getBytes(StandardCharsets.US_ASCII),
            expected.getBytes(StandardCharsets.US_ASCII));
  }
}
