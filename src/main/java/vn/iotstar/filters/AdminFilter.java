package vn.iotstar.filters;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.iotstar.entity.UserModel;

@WebFilter(urlPatterns = "/admin/*")
public class AdminFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);

        Object account = session == null
                ? null
                : session.getAttribute("account");

        if (!(account instanceof UserModel)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        UserModel user = (UserModel) account;

        // Giữ quy ước role của project: 2 = admin.
        if (user.getRoleid() != 2) {
            resp.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập trang quản trị.");
            return;
        }

        chain.doFilter(request, response);
    }
}