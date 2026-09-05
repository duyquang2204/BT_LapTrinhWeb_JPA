package vn.iotstar.controllers;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/logout")
public class LogoutController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        if (session == null
                || session.getAttribute("account") == null) {

            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        synchronized (session) {
            if (session.getAttribute("csrfToken") == null) {
                session.setAttribute(
                        "csrfToken",
                        UUID.randomUUID().toString());
            }
        }

        resp.setHeader("Cache-Control", "no-store");

        req.getRequestDispatcher("/views/logout.jsp")
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
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String expectedToken =
                (String) session.getAttribute("csrfToken");

        String submittedToken = req.getParameter("csrfToken");

        if (expectedToken == null
                || !expectedToken.equals(submittedToken)) {

            resp.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Yêu cầu đăng xuất không hợp lệ.");
            return;
        }

        session.invalidate();

        resp.sendRedirect(req.getContextPath() + "/login");
    }
}