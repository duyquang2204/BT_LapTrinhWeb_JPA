package vn.iotstar.controllers.user;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.iotstar.entity.UserModel;
import vn.iotstar.services.IUserService;
import vn.iotstar.services.impl.UserService;
import vn.iotstar.utils.ImageUploadUtil;

@WebServlet("/user/profile")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5L * 1024 * 1024,
        maxRequestSize = 6L * 1024 * 1024
)
public class ProfileController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final IUserService userService = new UserService();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        UserModel account = getAccount(req);

        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        UserModel user = userService.findById(account.getId());

        if (user == null) {
            req.getSession().invalidate();
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        HttpSession session = req.getSession();

        synchronized (session) {
            if (session.getAttribute("csrfToken") == null) {
                session.setAttribute(
                        "csrfToken",
                        UUID.randomUUID().toString());
            }
        }

        req.setAttribute(
                "success", session.getAttribute("profileSuccess"));
        session.removeAttribute("profileSuccess");

        req.setAttribute("profile", user);
        showForm(req, resp);
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        UserModel account = getAccount(req);

        if (account == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String token;

        try {
            token = req.getParameter("csrfToken");
        } catch (IllegalStateException e) {
            resp.sendError(
                    413,
                    "File hoặc tổng dữ liệu gửi lên quá lớn.");
            return;
        }

        HttpSession session = req.getSession(false);

        String expectedToken =
                (String) session.getAttribute("csrfToken");

        if (expectedToken == null
                || !expectedToken.equals(token)) {

            resp.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Phiên biểu mẫu không hợp lệ. Hãy tải lại trang.");
            return;
        }

        // ID lấy từ session, không nhận ID cần sửa từ form.
        int userId = account.getId();

        UserModel profile = userService.findById(userId);

        if (profile == null) {
            session.invalidate();
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String fullname = req.getParameter("fullname");
        String phone = req.getParameter("phone");

        fullname = fullname == null ? "" : fullname.trim();
        phone = phone == null ? "" : phone.trim();

        // Đây là bản dữ liệu vừa đọc từ DAO, không phải object
        // account đang nằm trong session.
        profile.setFullname(fullname);
        profile.setPhone(phone);

        req.setAttribute("profile", profile);

        // Kiểm tra trước khi lưu ảnh.
        if (fullname.isBlank() || fullname.length() > 100) {
            showError(
                    req, resp,
                    "Họ tên phải có từ 1 đến 100 ký tự.");
            return;
        }

        if (!phone.matches("0[0-9]{9}")) {
            showError(
                    req, resp,
                    "Số điện thoại phải gồm 10 chữ số, bắt đầu bằng 0.");
            return;
        }

        String newImage;

        try {
            newImage = ImageUploadUtil.saveImage(
                    req.getPart("images"));
        } catch (IllegalArgumentException e) {
            showError(req, resp, e.getMessage());
            return;
        } catch (IllegalStateException e) {
            showError(
                    req, resp,
                    "Ảnh không được vượt quá 5 MB.");
            return;
        } catch (IOException | ServletException e) {
            getServletContext().log(
                    "Upload ảnh profile thất bại.", e);

            showError(
                    req, resp,
                    "Không thể đọc hoặc lưu ảnh. "
                            + "Hãy kiểm tra ảnh và thư mục upload.");
            return;
        }

        try {
            userService.updateProfile(
                    userId, fullname, phone, newImage);
        } catch (RuntimeException e) {
            deleteNewImageQuietly(newImage);

            getServletContext().log(
                    "Cập nhật profile thất bại.", e);

            showError(
                    req, resp,
                    "Không thể lưu thông tin. "
                            + "Hãy thử lại và chọn lại ảnh nếu có.");
            return;
        }

        // Chỉ cập nhật session sau khi database đã lưu thành công.
        account.setFullname(fullname);
        account.setPhone(phone);

        if (newImage != null) {
            account.setImages(newImage);
        }

        session.setAttribute("account", account);
        session.setAttribute(
                "profileSuccess",
                "Cập nhật thông tin thành công.");

        resp.sendRedirect(
                req.getContextPath() + "/user/profile");
    }

    private UserModel getAccount(HttpServletRequest req) {
        HttpSession session = req.getSession(false);

        if (session == null) {
            return null;
        }

        Object account = session.getAttribute("account");

        return account instanceof UserModel
                ? (UserModel) account
                : null;
    }

    private void showForm(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        req.getRequestDispatcher("/views/user/profile.jsp")
                .forward(req, resp);
    }

    private void showError(
            HttpServletRequest req,
            HttpServletResponse resp,
            String message)
            throws ServletException, IOException {

        req.setAttribute("error", message);
        showForm(req, resp);
    }

    private void deleteNewImageQuietly(String filename) {
        if (filename == null) {
            return;
        }

        try {
            Files.deleteIfExists(
                    ImageUploadUtil.getUploadDirectory()
                            .resolve(filename));
        } catch (IOException e) {
            getServletContext().log(
                    "Không dọn được ảnh profile mới.", e);
        }
    }
}