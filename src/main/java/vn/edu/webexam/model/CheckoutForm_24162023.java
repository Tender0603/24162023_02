package vn.edu.webexam.model;

public class CheckoutForm_24162023 {
  private final String receiverName, receiverPhone, shippingAddress;
  public CheckoutForm_24162023(String name, String phone, String address) {
    receiverName = name == null ? "" : name.trim();
    receiverPhone = phone == null ? "" : phone.trim();
    shippingAddress = address == null ? "" : address.trim();
  }
  public String getReceiverName() { return receiverName; }
  public String getReceiverPhone() { return receiverPhone; }
  public String getShippingAddress() { return shippingAddress; }
  public void validate() {
    if (receiverName.isBlank() || receiverName.length() > 100)
      throw new IllegalArgumentException("Tên người nhận cần 1–100 ký tự.");
    if (!receiverPhone.matches("\\+?[0-9]{9,15}"))
      throw new IllegalArgumentException("Điện thoại cần 9–15 chữ số, có thể bắt đầu bằng +.");
    if (shippingAddress.isBlank() || shippingAddress.length() > 500)
      throw new IllegalArgumentException("Địa chỉ nhận hàng cần 1–500 ký tự.");
  }
}
