package vn.edu.webexam.service;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BookImageTest_24162023 {
  @TempDir Path directory;

  private byte[] png() throws IOException {
    var output = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(12, 16, BufferedImage.TYPE_INT_RGB), "png", output);
    return output.toByteArray();
  }

  @Test
  void validatesContentsAndUsesUniqueSafeFilenames() throws IOException {
    var service = new BookImageService_24162023(directory);
    byte[] data = png();
    String first = service.store(new ByteArrayInputStream(data), "../../cover.PNG", data.length);
    String second = service.store(new ByteArrayInputStream(data), "cover.png", data.length);
    assertNotEquals(first, second);
    assertTrue(first.length() <= 100);
    assertTrue(first.matches("assets/uploads/books/[0-9a-f-]{36}\\.png"));
    assertArrayEquals(
        data, Files.readAllBytes(service.resolve(first.substring(first.lastIndexOf('/') + 1))));
    service.discard(first);
    try (var files = Files.list(directory)) {
      assertEquals(1, files.count());
    }
  }

  @Test
  void rejectsSpoofedFilesMismatchAndOversize() throws IOException {
    var service = new BookImageService_24162023(directory);
    byte[] data = png();
    assertThrows(
        IllegalArgumentException.class,
        () ->
            service.store(
                new ByteArrayInputStream("<script>bad</script>".getBytes()), "fake.png", 20));
    assertThrows(
        IllegalArgumentException.class,
        () -> service.store(new ByteArrayInputStream(data), "fake.jpg", data.length));
    assertThrows(
        IllegalArgumentException.class,
        () -> service.store(new ByteArrayInputStream(data), "cover.svg", data.length));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            service.store(
                new ByteArrayInputStream(data),
                "cover.png",
                BookImageService_24162023.MAX_BYTES + 1L));
    assertThrows(
        IllegalArgumentException.class, () -> service.resolve("../../application.properties"));
    assertThrows(IllegalArgumentException.class, () -> service.resolve("cover.jsp"));
    try (var files = Files.list(directory)) {
      assertEquals(0, files.count());
    }
  }
}
