package vn.iotstar.controllers;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.iotstar.services.impl.ActivationService;

@WebServlet("/activate/resume")
public class ResumeActivationController extends HttpServlet {

    private static final long serialVersionUID = 1L;

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
                        "csrfToken",
                        UUID.randomUUID().toString());
            }
        }

        showForm(req, resp);
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
            resp.sendRedirect(
                    req.getContextPath() + "/activate/resume");
            return;
        }

        if (session.getAttribute("account") != null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String expected =
                (String) session.getAttribute("csrfToken");

        if (expected == null
                || !expected.equals(req.getParameter("csrfToken"))) {

            resp.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Phiên biểu mẫu không hợp lệ. Hãy tải lại trang.");
            return;
        }

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        username = username == null ? "" : username.trim();

        req.setAttribute("username", username);

        if (username.isBlank()
                || password == null
                || password.isEmpty()) {

            req.setAttribute(
                    "error",
                    "Vui lòng nhập tên đăng nhập và mật khẩu.");

            showForm(req, resp);
            return;
        }

        Integer userId;

        try {
            userId = activationService.authenticatePendingAccount(
                    username, password);
        } catch (RuntimeException e) {
            getServletContext().log(
                    "Không thể tiếp tục kích hoạt tài khoản.", e);

            req.setAttribute(
                    "error",
                    "Không thể xử lý lúc này. Vui lòng thử lại.");

            showForm(req, resp);
            return;
        }

        if (userId == null) {
            req.setAttribute(
                    "error",
                    "Thông tin không đúng hoặc tài khoản "
                            + "không ở trạng thái chờ kích hoạt.");

            showForm(req, resp);
            return;
        }

        // Đổi session ID sau khi xác minh mật khẩu.
        req.changeSessionId();

        session.setAttribute("pendingActivationUserId", userId);
        session.setAttribute(
                "csrfToken",
                UUID.randomUUID().toString());

        session.removeAttribute("activationError");
        session.setAttribute(
                "activationNotice",
                "Đã khôi phục bước kích hoạt. "
                        + "Nhập mã còn hiệu lực hoặc bấm Gửi lại OTP.");

        // Chưa đặt account: người dùng vẫn chưa đăng nhập.
        resp.sendRedirect(req.getContextPath() + "/activate");
    }

    private void showForm(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        req.getRequestDispatcher("/views/resume-activation.jsp")
                .forward(req, resp);
    }
}