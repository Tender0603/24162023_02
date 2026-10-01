package vn.edu.webexam.service;

import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import vn.edu.webexam.util.Config_24162023;

/** Files live outside webapps so redeploying a WAR does not remove book covers. */
public class BookImageService_24162023 {
  public static final int MAX_BYTES = 5 * 1024 * 1024;
  private static final String URL_PREFIX = "assets/uploads/books/";
  private final Path directory;

  public BookImageService_24162023() {
    String configured = Config_24162023.get("BOOK_UPLOAD_DIR");
    directory =
        (configured.isBlank()
                ? Path.of(
                    System.getProperty("catalina.base", System.getProperty("user.dir")),
                    "uploads",
                    "books")
                : Path.of(configured))
            .toAbsolutePath()
            .normalize();
  }

  public BookImageService_24162023(Path directory) {
    this.directory = directory.toAbsolutePath().normalize();
  }

  public String store(InputStream input, String originalName, long size) throws IOException {
    if (size <= 0 || size > MAX_BYTES)
      throw new IllegalArgumentException("Ảnh phải có dữ liệu và không vượt quá 5 MB.");
    String name = originalName.replace('\\', '/');
    name = name.substring(name.lastIndexOf('/') + 1);
    int dot = name.lastIndexOf('.');
    String extension = dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    if (!Set.of("jpg", "jpeg", "png", "webp").contains(extension))
      throw new IllegalArgumentException("Chỉ chấp nhận ảnh JPG, JPEG, PNG hoặc WEBP.");
    byte[] data = input.readNBytes(MAX_BYTES + 1);
    if (data.length == 0 || data.length > MAX_BYTES)
      throw new IllegalArgumentException("Ảnh phải có dữ liệu và không vượt quá 5 MB.");
    validate(data, extension);
    Files.createDirectories(directory);
    String filename = UUID.randomUUID() + "." + extension;
    Path target = directory.resolve(filename);
    try {
      Files.write(target, data, StandardOpenOption.CREATE_NEW);
    } catch (IOException e) {
      // A failed disk write must not leave a partial image.
      if (!(e instanceof FileAlreadyExistsException)) Files.deleteIfExists(target);
      throw e;
    }
    return URL_PREFIX + filename;
  }

  private void validate(byte[] data, String extension) {
    ImageReader reader = null;
    try (var stream = new MemoryCacheImageInputStream(new ByteArrayInputStream(data))) {
      if ("webp".equals(extension)) {
        var provider = new WebPImageReaderSpi();
        if (!provider.canDecodeInput(stream)) throw new IOException("Not WEBP");
        reader = provider.createReaderInstance();
      } else {
        var readers = ImageIO.getImageReaders(stream);
        if (!readers.hasNext()) throw new IOException("Not an image");
        reader = readers.next();
      }
      reader.setInput(stream, true, true);
      String expected = extension.equals("jpg") ? "jpeg" : extension;
      if (!expected.equalsIgnoreCase(reader.getFormatName()))
        throw new IOException("Format mismatch");
      int width = reader.getWidth(0), height = reader.getHeight(0);
      if (width <= 0
          || height <= 0
          || width > 8000
          || height > 8000
          || (long) width * height > 16_000_000)
        throw new IllegalArgumentException("Ảnh tối đa 8000 px mỗi chiều và 16 triệu điểm ảnh.");
      if (reader.read(0) == null) throw new IOException("Cannot decode image");
    } catch (IOException e) {
      throw new IllegalArgumentException(
          "File không phải ảnh hợp lệ hoặc nội dung không khớp đuôi file.", e);
    } finally {
      if (reader != null) reader.dispose();
    }
  }

  public Path resolve(String filename) {
    if (filename == null
        || !filename.matches(
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.(jpg|jpeg|png|webp)"))
      throw new IllegalArgumentException("Tên ảnh không hợp lệ.");
    return directory.resolve(filename);
  }

  public void discard(String relativeUrl) throws IOException {
    if (relativeUrl != null && relativeUrl.startsWith(URL_PREFIX))
      Files.deleteIfExists(resolve(relativeUrl.substring(URL_PREFIX.length())));
  }
}
