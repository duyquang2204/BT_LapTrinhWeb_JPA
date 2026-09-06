package vn.iotstar.services.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

import jakarta.mail.MessagingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;

import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entity.EmailOtp;
import vn.iotstar.entity.UserModel;
import vn.iotstar.utils.PasswordUtil;

public class PasswordResetService {

    private static final String PURPOSE = "PASSWORD_RESET";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final MailService mailService = new MailService();

    public enum Result {
        SUCCESS,
        INVALID,
        EXPIRED,
        TOO_MANY_ATTEMPTS
    }

    public void requestOtp(String email)
            throws MessagingException {

        email = normalizeEmail(email);

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        Long otpId = null;
        int userId = 0;
        String recipient = null;
        String code = null;

        try {
            tx.begin();

            UserModel user = findAndLockUser(em, email);

            // Không tiết lộ email có tài khoản hay không.
            // Tài khoản chưa kích hoạt dùng luồng kích hoạt riêng.
            if (user == null || user.getActive() != 1) {
                tx.commit();
                return;
            }

            LocalDateTime now = utcNow();
            EmailOtp latest = findLatest(em, user.getId());

            if (latest != null
                    && now.isBefore(
                            latest.getCreatedAt().plusSeconds(60))) {

                tx.commit();
                return;
            }

            Long recentCount = em.createQuery(
                    "SELECT COUNT(o) FROM EmailOtp o "
                            + "WHERE o.userId = :userId "
                            + "AND o.purpose = :purpose "
                            + "AND o.createdAt >= :since",
                    Long.class)
                    .setParameter("userId", user.getId())
                    .setParameter("purpose", PURPOSE)
                    .setParameter("since", now.minusHours(1))
                    .getSingleResult();

            if (recentCount >= 5) {
                tx.commit();
                return;
            }

            if (latest != null) {
                latest.setUsed(true);
            }

            code = String.format(
                    Locale.ROOT,
                    "%06d",
                    RANDOM.nextInt(1_000_000));

            EmailOtp otp = new EmailOtp();
            otp.setUserId(user.getId());
            otp.setPurpose(PURPOSE);
            otp.setCodeHash(PasswordUtil.hash(code));
            otp.setCreatedAt(now);
            otp.setExpiresAt(now.plusMinutes(5));
            otp.setAttempts(0);
            otp.setUsed(false);

            em.persist(otp);
            em.flush();

            otpId = otp.getId();
            userId = user.getId();
            recipient = user.getEmail();

            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }

            throw e;
        } finally {
            em.close();
        }

        try {
            mailService.sendText(
                    recipient,
                    "Mã OTP đặt lại mật khẩu",
                    "Mã OTP đặt lại mật khẩu của bạn: " + code
                            + "\n\nMã có hiệu lực trong 5 phút."
                            + "\nKhông chia sẻ mã này với người khác."
                            + "\nNếu bạn không yêu cầu đổi mật khẩu, "
                            + "hãy bỏ qua email này.");
        } catch (MessagingException | RuntimeException e) {
            try {
                invalidateOtp(otpId, userId);
            } catch (RuntimeException cleanupError) {
                e.addSuppressed(cleanupError);
            }

            throw e;
        }
    }

    public Result resetPassword(
            String email,
            String code,
            String password,
            String confirmation) {

        if (password == null
                || password.isBlank()
                || password.length() < 3
                || password.length() > 128) {

            throw new IllegalArgumentException(
                    "Mật khẩu phải có từ 3 đến 128 ký tự.");
        }

        if (!password.equals(confirmation)) {
            throw new IllegalArgumentException(
                    "Mật khẩu nhập lại không khớp.");
        }

        email = normalizeEmail(email);
        code = code == null ? "" : code.trim();

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            UserModel user = findAndLockUser(em, email);
            Result result;

            if (user == null || user.getActive() != 1) {
                result = Result.INVALID;
            } else {
                EmailOtp otp = findLatest(em, user.getId());

                if (otp == null) {
                    result = Result.INVALID;
                } else if (otp.getAttempts() >= 5) {
                    result = Result.TOO_MANY_ATTEMPTS;
                } else if (otp.isUsed()) {
                    result = Result.INVALID;
                } else if (!utcNow().isBefore(otp.getExpiresAt())) {
                    otp.setUsed(true);
                    result = Result.EXPIRED;
                } else {
                    boolean correct =
                            code.matches("[0-9]{6}")
                            && PasswordUtil.verify(
                                    code, otp.getCodeHash());

                    if (!correct) {
                        otp.setAttempts(otp.getAttempts() + 1);

                        if (otp.getAttempts() >= 5) {
                            otp.setUsed(true);
                            result = Result.TOO_MANY_ATTEMPTS;
                        } else {
                            result = Result.INVALID;
                        }
                    } else {
                    	user.setPassword(PasswordUtil.hash(password));

                    	user.setSessionVersion(
                    	        Math.addExact(user.getSessionVersion(), 1));

                    	otp.setUsed(true);
                    	result = Result.SUCCESS;
                    }
                }
            }

            // Commit cả khi sai mã để lưu số lần thử.
            tx.commit();
            return result;

        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }

            throw e;
        } finally {
            em.close();
        }
    }

    private UserModel findAndLockUser(
            EntityManager em,
            String email) {

        List<Integer> ids = em.createQuery(
                "SELECT u.id FROM UserModel u "
                        + "WHERE LOWER(u.email) = :email",
                Integer.class)
                .setParameter("email", email)
                .setMaxResults(2)
                .getResultList();

        if (ids.size() != 1) {
            return null;
        }

        UserModel user = em.find(
                UserModel.class,
                ids.get(0),
                LockModeType.PESSIMISTIC_WRITE);

        // Kiểm tra lại sau khi lấy khóa.
        if (user == null
                || !normalizeEmail(user.getEmail()).equals(email)) {
            return null;
        }

        return user;
    }

    private EmailOtp findLatest(EntityManager em, int userId) {
        List<EmailOtp> list = em.createQuery(
                "SELECT o FROM EmailOtp o "
                        + "WHERE o.userId = :userId "
                        + "AND o.purpose = :purpose "
                        + "ORDER BY o.id DESC",
                EmailOtp.class)
                .setParameter("userId", userId)
                .setParameter("purpose", PURPOSE)
                .setMaxResults(1)
                .getResultList();

        return list.isEmpty() ? null : list.get(0);
    }

    private void invalidateOtp(Long otpId, int userId) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            em.find(
                    UserModel.class,
                    userId,
                    LockModeType.PESSIMISTIC_WRITE);

            EmailOtp otp = em.find(EmailOtp.class, otpId);

            if (otp != null) {
                otp.setUsed(true);
            }

            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }

            throw e;
        } finally {
            em.close();
        }
    }

    private String normalizeEmail(String email) {
        return email == null
                ? ""
                : email.trim().toLowerCase(Locale.ROOT);
    }

    private LocalDateTime utcNow() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}