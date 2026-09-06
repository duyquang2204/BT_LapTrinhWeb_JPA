package vn.iotstar.filters;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.iotstar.entity.UserModel;
import vn.iotstar.services.IUserService;
import vn.iotstar.services.impl.UserService;

public class SessionValidationFilter implements Filter {

    private final IUserService userService = new UserService();

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getServletPath();

        // Không truy vấn database cho ảnh và tài nguyên tĩnh.
        if (path.startsWith("/assets/") || "/image".equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);

        if (session == null) {
            chain.doFilter(request, response);
            return;
        }

        Object accountValue = session.getAttribute("account");

        // Các session đăng ký/OTP chưa đăng nhập vẫn hoạt động.
        if (accountValue == null) {
            chain.doFilter(request, response);
            return;
        }

        if (!(accountValue instanceof UserModel)) {
            expireSession(req, resp, session);
            return;
        }

        UserModel account = (UserModel) accountValue;
        Object versionValue = session.getAttribute("sessionVersion");

        UserModel currentUser;

        try {
            currentUser = userService.findById(account.getId());
        } catch (RuntimeException e) {
            req.getServletContext().log(
                    "Không thể kiểm tra phiên đăng nhập.", e);

            // Không cho qua khi không kiểm tra được trạng thái.
            resp.sendError(
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Tạm thời không thể kiểm tra phiên đăng nhập. "
                            + "Vui lòng thử lại.");
            return;
        }

        if (currentUser == null
                || currentUser.getActive() != 1
                || !(versionValue instanceof Integer)
                || ((Integer) versionValue).intValue()
                        != currentUser.getSessionVersion()) {

            expireSession(req, resp, session);
            return;
        }

        // Cập nhật profile và role từ database trước khi
        // AdminFilter kiểm tra quyền.
        session.setAttribute("account", currentUser);

        resp.setHeader("Cache-Control", "no-store");

        chain.doFilter(request, response);
    }

    private void expireSession(
            HttpServletRequest req,
            HttpServletResponse resp,
            HttpSession session) throws IOException {

        session.invalidate();

        HttpSession newSession = req.getSession(true);
        newSession.setAttribute(
                "authNotice",
                "Phiên đăng nhập không còn hợp lệ. "
                        + "Vui lòng đăng nhập lại.");

        resp.setHeader("Cache-Control", "no-store");
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}