package vn.edu.webexam.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Properties;
import java.util.logging.Logger;
import vn.edu.webexam.model.*;
import vn.edu.webexam.util.Config_24162023;

public class OTPService_24162023 {
  private static final SecureRandom RANDOM = new SecureRandom();

  public PendingRegistration_24162023 send(User_24162023 user) throws MessagingException {
    String otp = String.format("%06d", RANDOM.nextInt(1000000));
    if ("demo".equals(Config_24162023.get("OTP_MODE"))) {
      Logger.getLogger(OTPService_24162023.class.getName())
          .warning("DEMO ONLY - OTP for " + user.getEmail() + ": " + otp);
    } else {
      String host = Config_24162023.get("SMTP_HOST"), sender = Config_24162023.get("SMTP_FROM");
      if (host.isBlank() || sender.isBlank())
        throw new MessagingException("SMTP chưa được cấu hình.");
      var props = new Properties();
      props.setProperty("mail.smtp.host", host);
      props.setProperty("mail.smtp.port", Config_24162023.get("SMTP_PORT"));
      props.setProperty("mail.smtp.starttls.enable", Config_24162023.get("SMTP_STARTTLS"));
      props.setProperty("mail.smtp.starttls.required", Config_24162023.get("SMTP_STARTTLS"));
      props.setProperty("mail.smtp.connectiontimeout", "10000");
      props.setProperty("mail.smtp.timeout", "10000");
      props.setProperty("mail.smtp.writetimeout", "10000");
      boolean auth = !Config_24162023.get("SMTP_USER").isBlank();
      props.setProperty("mail.smtp.auth", Boolean.toString(auth));
      Session mailSession = Session.getInstance(props);
      var message = new MimeMessage(mailSession);
      message.setFrom(new InternetAddress(sender));
      message.setRecipient(Message.RecipientType.TO, new InternetAddress(user.getEmail()));
      message.setSubject("Web Exam 02 - OTP kích hoạt", "UTF-8");
      message.setText("Mã OTP: " + otp + "\nCó hiệu lực 5 phút, tối đa 5 lần thử.", "UTF-8");
      if (auth)
        Transport.send(
            message, Config_24162023.get("SMTP_USER"), Config_24162023.get("SMTP_PASSWORD"));
      else Transport.send(message);
    }
    return new PendingRegistration_24162023(user, otp, Instant.now().plusSeconds(300));
  }
}
