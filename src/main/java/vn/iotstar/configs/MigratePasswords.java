package vn.iotstar.configs;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import vn.iotstar.entity.UserModel;
import vn.iotstar.utils.PasswordUtil;

public class MigratePasswords {

    public static void main(String[] args) {
        EntityManager em = null;

        try {
            em = JPAConfig.getEntityManager();
            EntityTransaction transaction = em.getTransaction();

            try {
                transaction.begin();

                List<UserModel> users = em.createQuery(
                        "SELECT u FROM UserModel u",
                        UserModel.class)
                        .getResultList();

                int changed = 0;

                for (UserModel user : users) {
                    String password = user.getPassword();

                    if (password == null || password.isEmpty()) {
                        throw new IllegalStateException(
                                "User ID " + user.getId()
                                        + " không có mật khẩu. "
                                        + "Dừng chuyển đổi để kiểm tra.");
                    }

                    if (PasswordUtil.isEncoded(password)) {
                        continue;
                    }

                    user.setPassword(PasswordUtil.hash(password));
                    changed++;
                }

                transaction.commit();

                System.out.println(
                        "Đã chuyển mật khẩu sang hash cho "
                                + changed + " tài khoản.");
            } catch (RuntimeException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }

                throw e;
            }
        } catch (RuntimeException e) {
            System.err.println(
                    "Chuyển đổi thất bại. Kiểm tra lỗi bên dưới.");
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }

            JPAConfig.shutdown();
        }
    }
}