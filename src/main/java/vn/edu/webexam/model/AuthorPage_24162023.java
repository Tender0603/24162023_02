package vn.edu.webexam.model;

import java.util.ArrayList;
import java.util.List;

public class AuthorPage_24162023 {
  private final int authorId, total, page;
  private final String authorName;
  private final List<Book_24162023> books = new ArrayList<>();

  public AuthorPage_24162023(int id, String name, int total, int page) {
    authorId = id;
    authorName = name;
    this.total = total;
    this.page = page;
  }

  public int getAuthorId() {
    return authorId;
  }

  public String getAuthorName() {
    return authorName;
  }

  public int getTotal() {
    return total;
  }

  public int getPage() {
    return page;
  }

  public int getPages() {
    return Math.max(1, (total + 2) / 3);
  }

  public List<Book_24162023> getBooks() {
    return books;
  }
}
