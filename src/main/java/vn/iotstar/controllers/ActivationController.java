package vn.iotstar.controllers;

import java.io.IOException;

import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.iotstar.services.impl.ActivationService;
import vn.iotstar.services.impl.ActivationService.VerifyResult;

@WebServlet("/activate")
public class ActivationController extends HttpServlet {

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

        HttpSession session = req.getSession(false);

        if (pendingUserId(session) == null) {
        	resp.sendRedirect(req.getContextPath() + "/activate/resume");
            return;
        }

        req.setAttribute(
                "notice", session.getAttribute("activationNotice"));
        req.setAttribute(
                "error", session.getAttribute("activationError"));

        session.removeAttribute("activationNotice");
        session.removeAttribute("activationError");

        req.getRequestDispatcher("/views/activate.jsp")
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
        Integer userId = pendingUserId(session);

        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/register");
            return;
        }

        String expected = (String) session.getAttribute("csrfToken");
        String submitted = req.getParameter("csrfToken");

        if (expected == null || !expected.equals(submitted)) {
            resp.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Phiên biểu mẫu không hợp lệ.");
            return;
        }

        String action = req.getParameter("action");

        if ("resend".equals(action)) {
            resend(session, userId);
        } else if ("verify".equals(action)) {
            boolean activated = verify(
                    session, userId, req.getParameter("otp"));

            if (activated) {
                session.removeAttribute("pendingActivationUserId");
                session.removeAttribute("activationNotice");
                session.removeAttribute("activationError");

                session.setAttribute(
                        "authSuccess",
                        "Kích hoạt thành công. Bạn có thể đăng nhập.");

                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/activate");
    }

    private void resend(HttpSession session, int userId) {
        try {
            activationService.sendActivationOtp(userId);

            session.setAttribute(
                    "activationNotice",
                    "Đã gửi mã mới. Mã trước đó không còn hiệu lực.");
        } catch (IllegalArgumentException e) {
            session.setAttribute("activationError", e.getMessage());
        } catch (MessagingException | RuntimeException e) {
            getServletContext().log("Gửi lại OTP thất bại.", e);

            session.setAttribute(
                    "activationError",
                    "Chưa gửi được OTP. Vui lòng thử lại sau.");
        }
    }

    private boolean verify(
            HttpSession session,
            int userId,
            String code) {

        try {
            VerifyResult result =
                    activationService.verifyActivationOtp(userId, code);

            switch (result) {
                case SUCCESS:
                case ALREADY_ACTIVE:
                    return true;

                case EXPIRED:
                    session.setAttribute(
                            "activationError",
                            "Mã đã hết hạn. Hãy yêu cầu mã mới.");
                    break;

                case TOO_MANY_ATTEMPTS:
                    session.setAttribute(
                            "activationError",
                            "Bạn đã nhập sai quá số lần cho phép. "
                                    + "Hãy yêu cầu mã mới.");
                    break;

                default:
                    session.setAttribute(
                            "activationError",
                            "Mã không đúng hoặc không còn hiệu lực.");
            }
        } catch (RuntimeException e) {
            getServletContext().log("Xác nhận OTP thất bại.", e);

            session.setAttribute(
                    "activationError",
                    "Không thể xác nhận lúc này. Vui lòng thử lại.");
        }

        return false;
    }

    private Integer pendingUserId(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object value = session.getAttribute("pendingActivationUserId");

        return value instanceof Integer ? (Integer) value : null;
    }
}