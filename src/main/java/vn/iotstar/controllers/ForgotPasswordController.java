package vn.iotstar.controllers;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.iotstar.services.impl.PasswordResetService;
import vn.iotstar.services.impl.PasswordResetService.Result;

@WebServlet("/forgetpassword")
public class ForgotPasswordController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final PasswordResetService resetService =
            new PasswordResetService();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");

        HttpSession session = req.getSession();

        synchronized (session) {
            if (session.getAttribute("csrfToken") == null) {
                session.setAttribute(
                        "csrfToken",
                        UUID.randomUUID().toString());
            }
        }

        req.setAttribute(
                "notice", session.getAttribute("resetNotice"));
        req.setAttribute(
                "error", session.getAttribute("resetError"));

        session.removeAttribute("resetNotice");
        session.removeAttribute("resetError");

        req.getRequestDispatcher("/views/forgetpassword.jsp")
                .forward(req, resp);
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-store");

        HttpSession session = req.getSession(false);

        if (session == null) {
            redirect(req, resp);
            return;
        }

        String expected = (String) session.getAttribute("csrfToken");
        String submitted = req.getParameter("csrfToken");

        if (expected == null || !expected.equals(submitted)) {
            resp.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Phiên biểu mẫu không hợp lệ. Hãy tải lại trang.");
            return;
        }

        String action = req.getParameter("action");

        if ("request".equals(action)) {
            requestOtp(req, session);
            redirect(req, resp);
            return;
        }

        if ("reset".equals(action)) {
            String email = (String) session.getAttribute("resetEmail");

            if (email == null) {
                session.setAttribute(
                        "resetError",
                        "Hãy nhập email và yêu cầu OTP trước.");

                redirect(req, resp);
                return;
            }

            try {
                Result result = resetService.resetPassword(
                        email,
                        req.getParameter("otp"),
                        req.getParameter("password"),
                        req.getParameter("confirmPassword"));

                if (result == Result.SUCCESS) {
                    // Kết thúc phiên hiện tại sau khi đổi password.
                    session.invalidate();

                    HttpSession newSession = req.getSession(true);
                    newSession.setAttribute(
                            "authSuccess",
                            "Đặt lại mật khẩu thành công. "
                                    + "Hãy đăng nhập bằng mật khẩu mới.");

                    resp.sendRedirect(
                            req.getContextPath() + "/login");
                    return;
                }

                switch (result) {
                    case EXPIRED:
                        session.setAttribute(
                                "resetError",
                                "OTP đã hết hạn. Hãy yêu cầu mã mới.");
                        break;

                    case TOO_MANY_ATTEMPTS:
                        session.setAttribute(
                                "resetError",
                                "Đã nhập sai quá số lần cho phép. "
                                        + "Hãy yêu cầu mã mới.");
                        break;

                    default:
                        session.setAttribute(
                                "resetError",
                                "OTP không đúng hoặc không còn hiệu lực.");
                }
            } catch (IllegalArgumentException e) {
                session.setAttribute("resetError", e.getMessage());
            } catch (RuntimeException e) {
                getServletContext().log(
                        "Đặt lại mật khẩu thất bại.", e);

                session.setAttribute(
                        "resetError",
                        "Không thể đổi mật khẩu lúc này. "
                                + "Vui lòng thử lại.");
            }

            redirect(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    private void requestOtp(
            HttpServletRequest req,
            HttpSession session) {

        String email = req.getParameter("email");
        email = email == null
                ? ""
                : email.trim().toLowerCase(Locale.ROOT);

        if (!validEmail(email)) {
            session.setAttribute(
                    "resetError",
                    "Vui lòng nhập email hợp lệ, tối đa 100 ký tự.");
            return;
        }

        // Lưu email cho bước nhập OTP; không nhận ID từ client.
        session.setAttribute("resetEmail", email);

        try {
            resetService.requestOtp(email);
        } catch (MessagingException | RuntimeException e) {
            // Giữ thông báo chung để không tiết lộ email
            // nào có tài khoản trong hệ thống.
            getServletContext().log(
                    "Yêu cầu email đặt lại mật khẩu thất bại.", e);
        }

        session.setAttribute(
                "resetNotice",
                "Nếu email thuộc tài khoản đã kích hoạt "
                        + "và chưa vượt giới hạn gửi, OTP sẽ được gửi. "
                        + "Hãy kiểm tra hộp thư và thư rác. "
                        + "Mỗi lần gửi cách nhau ít nhất 60 giây, "
                        + "tối đa 5 lượt mỗi giờ.");
    }

    private boolean validEmail(String email) {
        if (email.isBlank()
                || email.length() > 100
                || email.contains("\r")
                || email.contains("\n")) {
            return false;
        }

        try {
            InternetAddress address = new InternetAddress(email, true);
            address.validate();
            return email.equals(address.getAddress());
        } catch (AddressException e) {
            return false;
        }
    }

    private void redirect(
            HttpServletRequest req,
            HttpServletResponse resp) throws IOException {

        resp.sendRedirect(
                req.getContextPath() + "/forgetpassword");
    }
}