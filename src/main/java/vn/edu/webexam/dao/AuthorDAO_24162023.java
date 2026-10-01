package vn.edu.webexam.dao;

import java.sql.*;
import java.util.*;
import vn.edu.webexam.model.*;
import vn.edu.webexam.util.DBConnection_24162023;

public class AuthorDAO_24162023 {
  public List<Author_24162023> findAll() throws SQLException {
    var list = new ArrayList<Author_24162023>();
    try (var c = DBConnection_24162023.getConnection();
        var p = c.prepareStatement("SELECT * FROM author ORDER BY author_id");
        var r = p.executeQuery()) {
      while (r.next()) {
        var a = new Author_24162023();
        a.setAuthorId(r.getInt("author_id"));
        a.setAuthorName(r.getString("author_name"));
        var d = r.getDate("date_of_birth");
        a.setDateOfBirth(d == null ? null : d.toLocalDate());
        list.add(a);
      }
    }
    return list;
  }
}
