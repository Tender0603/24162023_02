package vn.edu.webexam.model;

public class Book_24162023 implements java.io.Serializable {
  // Stock observed when the admin edit form was opened; prevents stale stock overwrite.
  private Integer originalQuantity;
  public Integer getOriginalQuantity() { return originalQuantity; }
  public void setOriginalQuantity(Integer value) { originalQuantity = value; }
  private int bookid;

  public int getBookid() {
    return bookid;
  }

  public void setBookid(int value) {
    this.bookid = value;
  }

  private Integer isbn;

  public Integer getIsbn() {
    return isbn;
  }

  public void setIsbn(Integer value) {
    this.isbn = value;
  }

  private String title;

  public String getTitle() {
    return title;
  }

  public void setTitle(String value) {
    this.title = value;
  }

  private String publisher;

  public String getPublisher() {
    return publisher;
  }

  public void setPublisher(String value) {
    this.publisher = value;
  }

  private java.math.BigDecimal price;

  public java.math.BigDecimal getPrice() {
    return price;
  }

  public void setPrice(java.math.BigDecimal value) {
    this.price = value;
  }

  private String description;

  public String getDescription() {
    return description;
  }

  public void setDescription(String value) {
    this.description = value;
  }

  private java.time.LocalDate publishDate;

  public java.time.LocalDate getPublishDate() {
    return publishDate;
  }

  public void setPublishDate(java.time.LocalDate value) {
    this.publishDate = value;
  }

  private String coverImage;

  public String getCoverImage() {
    return coverImage;
  }

  public void setCoverImage(String value) {
    this.coverImage = value;
  }

  private Integer quantity;

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer value) {
    this.quantity = value;
  }

  private String authors;

  public String getAuthors() {
    return authors;
  }

  public void setAuthors(String value) {
    this.authors = value;
  }

  private int reviewCount;

  public int getReviewCount() {
    return reviewCount;
  }

  public void setReviewCount(int value) {
    this.reviewCount = value;
  }
}
