package vn.edu.webexam.util;

import java.util.Properties;

public final class Config_24162023 {
  private static final Properties VALUES = new Properties();

  static {
    try (var in = Config_24162023.class.getResourceAsStream("/application.properties")) {
      if (in != null) VALUES.load(in);
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private Config_24162023() {}

  public static String get(String key) {
    String value = System.getenv(key);
    return value == null ? VALUES.getProperty(key, "") : value;
  }
}
