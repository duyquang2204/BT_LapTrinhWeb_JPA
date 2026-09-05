package vn.iotstar.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import jakarta.servlet.http.Part;

public final class ImageUploadUtil {

    private static final long MAX_FILE_SIZE =
            5L * 1024 * 1024;

    private static final long MAX_PIXELS =
            16_000_000L;

    private ImageUploadUtil() {
    }

    public static Path getUploadDirectory() throws IOException {
        Path directory = Path.of(Constant.DIR)
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(directory);

        return directory;
    }

    public static String saveImage(Part part) throws IOException {
        // Không chọn ảnh mới thì trả null.
        if (part == null || part.getSize() == 0) {
            return null;
        }

        if (part.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Ảnh không được vượt quá 5 MB.");
        }

        BufferedImage image;

        try (InputStream input = part.getInputStream();
             ImageInputStream imageInput =
                     ImageIO.createImageInputStream(input)) {

            if (imageInput == null) {
                throw new IllegalArgumentException(
                        "Không thể đọc file ảnh.");
            }

            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(imageInput);

            if (!readers.hasNext()) {
                throw new IllegalArgumentException(
                        "File không phải ảnh hợp lệ.");
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(imageInput, true, true);

                String format = reader.getFormatName();

                if (!"JPEG".equalsIgnoreCase(format)
                        && !"JPG".equalsIgnoreCase(format)
                        && !"PNG".equalsIgnoreCase(format)) {

                    throw new IllegalArgumentException(
                            "Chỉ chấp nhận ảnh JPG, JPEG hoặc PNG.");
                }

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width <= 0
                        || height <= 0
                        || (long) width * height > MAX_PIXELS) {

                    throw new IllegalArgumentException(
                            "Ảnh không được vượt quá 16 triệu điểm ảnh.");
                }

                image = reader.read(0);

                if (image == null) {
                    throw new IllegalArgumentException(
                            "Nội dung ảnh không hợp lệ.");
                }
            } finally {
                reader.dispose();
            }
        }

        Path directory = getUploadDirectory();

        String filename = UUID.randomUUID() + ".png";
        Path destination = directory.resolve(filename);

        // Ghi ra file tạm trước, tránh để lại ảnh chưa ghi xong.
        Path temporary = Files.createTempFile(
                directory, "image-", ".tmp");

        try {
            boolean written = ImageIO.write(
                    image, "png", temporary.toFile());

            if (!written) {
                throw new IOException(
                        "Không thể lưu ảnh dưới định dạng PNG.");
            }

            Files.move(temporary, destination);

            return filename;
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}