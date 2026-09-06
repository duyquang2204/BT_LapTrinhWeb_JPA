// Service này xử lý:
//OTP 6 chữ số, hạn 5 phút.
//Gửi lại phải chờ 60 giây.
//Tối đa 5 lượt tạo mã mỗi giờ cho một tài khoản.
//Nhập sai tối đa 5 lần.
//Mã mới vô hiệu hóa mã cũ.
//Xác nhận mã và kích hoạt user trong cùng transaction.
//Lưu ý: số lượt gửi vẫn được tính khi SMTP thất bại, để tránh một lỗi gửi thư khiến ứng dụng bị gọi gửi liên tục. Database lưu thời gian OTP theo UTC.
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

public class ActivationService {

    private static final String PURPOSE = "ACTIVATION";
    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final MailService mailService = new MailService();

    public enum VerifyResult {
        SUCCESS,
        INVALID,
        EXPIRED,
        TOO_MANY_ATTEMPTS,
        ALREADY_ACTIVE
    }

    public void sendActivationOtp(int userId)
            throws MessagingException {

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        String recipient;
        String code;
        Long otpId;

        try {
            tx.begin();

            // Khóa user để các yêu cầu gửi/xác nhận đồng thời
            // của cùng tài khoản được xử lý lần lượt.
            UserModel user = em.find(
                    UserModel.class,
                    userId,
                    LockModeType.PESSIMISTIC_WRITE);

            if (user == null) {
                throw new IllegalArgumentException(
                        "Tài khoản không tồn tại.");
            }

            if (user.getActive() == 1) {
                throw new IllegalArgumentException(
                        "Tài khoản đã được kích hoạt.");
            }

            recipient = user.getEmail();

            if (recipient == null || recipient.isBlank()) {
                throw new IllegalArgumentException(
                        "Tài khoản chưa có email.");
            }

            LocalDateTime now = utcNow();
            EmailOtp latest = findLatest(em, userId);

            if (latest != null
                    && now.isBefore(
                            latest.getCreatedAt().plusSeconds(60))) {

                throw new IllegalArgumentException(
                        "Vui lòng chờ 60 giây trước khi gửi lại mã.");
            }

            Long recentCount = em.createQuery(
                    "SELECT COUNT(o) FROM EmailOtp o "
                            + "WHERE o.userId = :userId "
                            + "AND o.purpose = :purpose "
                            + "AND o.createdAt >= :since",
                    Long.class)
                    .setParameter("userId", userId)
                    .setParameter("purpose", PURPOSE)
                    .setParameter("since", now.minusHours(1))
                    .getSingleResult();

            if (recentCount >= 5) {
                throw new IllegalArgumentException(
                        "Bạn đã yêu cầu quá nhiều mã. "
                                + "Vui lòng thử lại sau.");
            }

            if (latest != null) {
                latest.setUsed(true);
            }

            code = String.format(
                    Locale.ROOT,
                    "%06d",
                    RANDOM.nextInt(1_000_000));

            EmailOtp otp = new EmailOtp();
            otp.setUserId(userId);
            otp.setPurpose(PURPOSE);
            otp.setCodeHash(PasswordUtil.hash(code));
            otp.setCreatedAt(now);
            otp.setExpiresAt(now.plusMinutes(5));
            otp.setAttempts(0);
            otp.setUsed(false);

            em.persist(otp);
            em.flush();

            otpId = otp.getId();

            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }

            throw e;
        } finally {
            em.close();
        }

        // Gửi email sau khi transaction database kết thúc.
        try {
            mailService.sendText(
                    recipient,
                    "Mã OTP kích hoạt tài khoản",
                    "Mã OTP của bạn là: " + code + "\n\n"
                            + "Mã có hiệu lực trong 5 phút "
                            + "kể từ khi được tạo.\n"
                            + "Không chia sẻ mã này với người khác.\n"
                            + "Nếu bạn không đăng ký tài khoản, "
                            + "hãy bỏ qua email.");
        } catch (MessagingException | RuntimeException e) {
            // Không để mã gửi thất bại tiếp tục có hiệu lực.
            try {
                invalidateOtp(otpId, userId);
            } catch (RuntimeException cleanupError) {
                e.addSuppressed(cleanupError);
            }

            throw e;
        }
    }

    public VerifyResult verifyActivationOtp(
            int userId,
            String submittedCode) {

        if (submittedCode == null) {
            submittedCode = "";
        }

        submittedCode = submittedCode.trim();

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            UserModel user = em.find(
                    UserModel.class,
                    userId,
                    LockModeType.PESSIMISTIC_WRITE);

            VerifyResult result;

            if (user == null) {
                result = VerifyResult.INVALID;
            } else if (user.getActive() == 1) {
                result = VerifyResult.ALREADY_ACTIVE;
            } else {
                EmailOtp otp = findLatest(em, userId);

                if (otp == null) {
                    result = VerifyResult.INVALID;
                } else if (otp.getAttempts() >= MAX_ATTEMPTS) {
                    result = VerifyResult.TOO_MANY_ATTEMPTS;
                } else if (otp.isUsed()) {
                    result = VerifyResult.INVALID;
                } else if (!utcNow().isBefore(otp.getExpiresAt())) {
                    otp.setUsed(true);
                    result = VerifyResult.EXPIRED;
                } else {
                    boolean correct =
                            submittedCode.matches("[0-9]{6}")
                            && PasswordUtil.verify(
                                    submittedCode,
                                    otp.getCodeHash());

                    if (!correct) {
                        otp.setAttempts(otp.getAttempts() + 1);

                        if (otp.getAttempts() >= MAX_ATTEMPTS) {
                            otp.setUsed(true);
                            result = VerifyResult.TOO_MANY_ATTEMPTS;
                        } else {
                            result = VerifyResult.INVALID;
                        }
                    } else {
                        otp.setUsed(true);
                        user.setActive(1);
                        result = VerifyResult.SUCCESS;
                    }
                }
            }

            // Cần commit cả khi nhập sai để lưu số lần thử.
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

    private EmailOtp findLatest(
            EntityManager em,
            int userId) {

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

    private LocalDateTime utcNow() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
    public Integer authenticatePendingAccount(
            String username,
            String password) {

        if (username == null
                || username.isBlank()
                || password == null
                || password.isEmpty()) {
            return null;
        }

        EntityManager em = JPAConfig.getEntityManager();

        try {
            List<UserModel> users = em.createQuery(
                    "SELECT u FROM UserModel u "
                            + "WHERE u.username = :username",
                    UserModel.class)
                    .setParameter("username", username.trim())
                    .setMaxResults(2)
                    .getResultList();

            if (users.size() != 1) {
                return null;
            }

            UserModel user = users.get(0);

            if (!PasswordUtil.verify(password, user.getPassword())) {
                return null;
            }

            if (user.getActive() != 0) {
                return null;
            }

            return user.getId();
        } finally {
            em.close();
        }
    }
}