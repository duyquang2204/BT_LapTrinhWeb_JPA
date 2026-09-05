package vn.iotstar.controllers;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.iotstar.entity.UserModel;
import vn.iotstar.services.IUserService;
import vn.iotstar.services.impl.UserService;
import vn.iotstar.utils.Constant;

@WebServlet("/login")
public class LoginController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final IUserService userService = new UserService();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");

        HttpSession session = req.getSession();

        if (session.getAttribute("account") instanceof UserModel) {
            resp.sendRedirect(req.getContextPath() + "/waiting");
            return;
        }

        ensureLoginToken(session);

        req.getRequestDispatcher("/views/login.jsp")
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

        String expectedToken = session == null
                ? null
                : (String) session.getAttribute("loginToken");

        String submittedToken = req.getParameter("loginToken");

        if (expectedToken == null
                || !expectedToken.equals(submittedToken)) {

            resp.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Phiên đăng nhập không hợp lệ. "
                            + "Hãy mở lại trang đăng nhập.");
            return;
        }

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        username = username == null ? "" : username.trim();

        // Giữ username khi form có lỗi.
        req.setAttribute("username", username);

        if (username.isBlank()
                || password == null
                || password.isEmpty()) {

            showError(
                    req, resp,
                    "Vui lòng nhập tên đăng nhập và mật khẩu.");
            return;
        }

        UserModel user;

        try {
            user = userService.login(username, password);
        } catch (RuntimeException e) {
            getServletContext().log(
                    "Không thể xử lý đăng nhập.", e);

            showError(
                    req, resp,
                    "Không thể đăng nhập lúc này. "
                            + "Vui lòng thử lại sau.");
            return;
        }

        if (user == null) {
            showError(
                    req, resp,
                    "Tên đăng nhập hoặc mật khẩu không đúng.");
            return;
        }

        // Bỏ session cũ và tạo session mới sau khi xác thực.
        session.invalidate();

        HttpSession newSession = req.getSession(true);
        newSession.setMaxInactiveInterval(30 * 60);
        newSession.setAttribute("account", user);

        // Token dùng cho các form Category/Profile.
        newSession.setAttribute(
                "csrfToken",
                UUID.randomUUID().toString());

        clearOldRememberCookie(req, resp);

        resp.sendRedirect(req.getContextPath() + "/waiting");
    }

    private void ensureLoginToken(HttpSession session) {
        synchronized (session) {
            if (session.getAttribute("loginToken") == null) {
                session.setAttribute(
                        "loginToken",
                        UUID.randomUUID().toString());
            }
        }
    }

    private void showError(
            HttpServletRequest req,
            HttpServletResponse resp,
            String message)
            throws ServletException, IOException {

        req.setAttribute("alert", message);

        req.getRequestDispatcher("/views/login.jsp")
                .forward(req, resp);
    }

    private void clearOldRememberCookie(
            HttpServletRequest req,
            HttpServletResponse resp) {

        Cookie cookie = new Cookie(Constant.COOKIE_REMEMBER, "");
        cookie.setMaxAge(0);
        cookie.setPath(
                req.getContextPath().isEmpty()
                        ? "/"
                        : req.getContextPath());

        resp.addCookie(cookie);
    }
}