package vn.iotstar.services.impl;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Pattern;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entity.UserModel;
import vn.iotstar.utils.PasswordUtil;

public class RegistrationService {

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("[A-Za-z0-9_]{3,30}");

    public int register(
            String username,
            String email,
            String fullname,
            String password,
            String confirmPassword) {

        username = trim(username);
        email = trim(email).toLowerCase(Locale.ROOT);
        fullname = trim(fullname);

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException(
                    "Tên đăng nhập phải có 3–30 ký tự, "
                            + "chỉ gồm chữ không dấu, số và dấu gạch dưới.");
        }

        if (email.isBlank() || email.length() > 100) {
            throw new IllegalArgumentException(
                    "Email không hợp lệ hoặc vượt quá 100 ký tự.");
        }

        try {
            InternetAddress address = new InternetAddress(email, true);
            address.validate();

            if (!email.equals(address.getAddress())
                    || email.contains("\r")
                    || email.contains("\n")) {
                throw new IllegalArgumentException("Email không hợp lệ.");
            }
        } catch (AddressException e) {
            throw new IllegalArgumentException("Email không hợp lệ.");
        }

        if (fullname.isBlank() || fullname.length() > 100) {
            throw new IllegalArgumentException(
                    "Họ tên phải có từ 1 đến 100 ký tự.");
        }

        if (password == null
                || password.isBlank()
                || password.length() < 3
                || password.length() > 128) {

            throw new IllegalArgumentException(
                    "Mật khẩu phải có từ 3 đến 128 ký tự.");
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException(
                    "Mật khẩu nhập lại không khớp.");
        }

        String passwordHash = PasswordUtil.hash(password);

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Long count = em.createQuery(
                    "SELECT COUNT(u) FROM UserModel u "
                            + "WHERE LOWER(u.username) = :username "
                            + "OR LOWER(u.email) = :email",
                    Long.class)
                    .setParameter(
                            "username", username.toLowerCase(Locale.ROOT))
                    .setParameter("email", email)
                    .getSingleResult();

            if (count > 0) {
                throw new IllegalArgumentException(
                        "Tên đăng nhập hoặc email đã được sử dụng.");
            }

            UserModel user = new UserModel();
            user.setUsername(username);
            user.setEmail(email);
            user.setFullname(fullname);
            user.setPassword(passwordHash);

            user.setPhone("");
            user.setImages(null);
            user.setCreateDate(Date.valueOf(LocalDate.now()));

            // Giữ quy ước hiện tại: 1 = user, 2 = admin.
            user.setRoleid(1);

            // Chưa xác nhận OTP thì chưa được đăng nhập.
            user.setActive(0);

            em.persist(user);
            em.flush();

            int userId = user.getId();

            tx.commit();
            return userId;

        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }

            throw e;
        } finally {
            em.close();
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}