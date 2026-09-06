package vn.iotstar.controllers;

import java.io.IOException;
import java.util.UUID;

import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.iotstar.services.impl.ActivationService;
import vn.iotstar.services.impl.RegistrationService;

@WebServlet("/register")
public class RegisterController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final RegistrationService registrationService =
            new RegistrationService();

    private final ActivationService activationService =
            new ActivationService();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");

        HttpSession session = req.getSession();

        if (session.getAttribute("account") != null) {
            resp.sendRedirect(req.getContextPath() + "/user/home");
            return;
        }

        synchronized (session) {
            if (session.getAttribute("csrfToken") == null) {
                session.setAttribute(
                        "csrfToken", UUID.randomUUID().toString());
            }
        }

        req.getRequestDispatcher("/views/register.jsp")
                .forward(req, resp);
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");

        HttpSession session = req.getSession(false);

        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/register");
            return;
        }

        if (session.getAttribute("account") != null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
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

        String username = req.getParameter("username");
        String email = req.getParameter("email");
        String fullname = req.getParameter("fullname");

        req.setAttribute("username", username);
        req.setAttribute("email", email);
        req.setAttribute("fullname", fullname);

        int userId;

        try {
            userId = registrationService.register(
                    username,
                    email,
                    fullname,
                    req.getParameter("password"),
                    req.getParameter("confirmPassword"));
        } catch (IllegalArgumentException e) {
            showError(req, resp, e.getMessage());
            return;
        } catch (RuntimeException e) {
            getServletContext().log("Đăng ký thất bại.", e);

            showError(
                    req, resp,
                    "Không thể tạo tài khoản. "
                            + "Tên đăng nhập/email có thể vừa được sử dụng. "
                            + "Vui lòng kiểm tra và thử lại.");
            return;
        }

        // Không đặt session account: tài khoản chưa đăng nhập.
        session.setAttribute("pendingActivationUserId", userId);

        try {
            activationService.sendActivationOtp(userId);

            session.setAttribute(
                    "activationNotice",
                    "Đã gửi mã OTP. Hãy kiểm tra email và thư rác.");
        } catch (MessagingException | RuntimeException e) {
            getServletContext().log("Gửi OTP đăng ký thất bại.", e);

            session.setAttribute(
                    "activationError",
                    "Tài khoản đã được tạo nhưng chưa gửi được OTP. "
                            + "Bạn có thể chờ ít nhất 60 giây rồi gửi lại.");
        }

        resp.sendRedirect(req.getContextPath() + "/activate");
    }

    private void showError(
            HttpServletRequest req,
            HttpServletResponse resp,
            String message)
            throws ServletException, IOException {

        req.setAttribute("error", message);

        req.getRequestDispatcher("/views/register.jsp")
                .forward(req, resp);
    }
}