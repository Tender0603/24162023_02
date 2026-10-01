package vn.edu.webexam.util;

import java.sql.*;

public final class DBConnection_24162023 {
  private DBConnection_24162023() {}

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(
        Config_24162023.get("DB_URL"),
        Config_24162023.get("DB_USER"),
        Config_24162023.get("DB_PASSWORD"));
  }
}
