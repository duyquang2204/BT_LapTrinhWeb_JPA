package vn.iotstar.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import vn.iotstar.utils.Constant;

@WebServlet(urlPatterns = "/image")
public class DownloadFileController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        String filename = req.getParameter("fname");

        if (!isValidFilename(filename)) {
            resp.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Tên file không hợp lệ.");
            return;
        }

        String contentType = getImageContentType(filename);

        if (contentType == null) {
            resp.sendError(
                    HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
                    "Loại ảnh không được hỗ trợ.");
            return;
        }

        Path directory = Path.of(Constant.DIR)
                .toAbsolutePath()
                .normalize();

        if (!Files.isDirectory(directory)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Path candidate = directory.resolve(filename).normalize();

        if (!candidate.startsWith(directory)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!Files.isRegularFile(candidate)) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Kiểm tra cả đường dẫn thật để tránh liên kết
        // trỏ ra ngoài thư mục upload.
        Path realDirectory = directory.toRealPath();
        Path realFile = candidate.toRealPath();

        if (!realFile.startsWith(realDirectory)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        resp.setContentType(contentType);
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setContentLengthLong(Files.size(realFile));

        Files.copy(realFile, resp.getOutputStream());
    }

    private boolean isValidFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return false;
        }

        return !filename.contains("/")
                && !filename.contains("\\")
                && !filename.contains(":")
                && !filename.contains("\u0000")
                && !".".equals(filename)
                && !"..".equals(filename);
    }

    private String getImageContentType(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);

        if (lower.endsWith(".png")) {
            return "image/png";
        }

        if (lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }

        if (lower.endsWith(".gif")) {
            return "image/gif";
        }

        if (lower.endsWith(".webp")) {
            return "image/webp";
        }

        return null;
    }
}