package vn.iotstar.dao.impl;

import java.util.List;
import java.util.Objects;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import vn.iotstar.configs.JPAConfig;
import vn.iotstar.dao.IUserDao;
import vn.iotstar.entity.UserModel;

public class UserDaoImpl implements IUserDao {

    @Override
    public List<UserModel> findAll() {
        EntityManager em = JPAConfig.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT u FROM UserModel u ORDER BY u.id",
                    UserModel.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public UserModel findById(int id) {
        if (id <= 0) {
            return null;
        }

        EntityManager em = JPAConfig.getEntityManager();

        try {
            return em.find(UserModel.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public UserModel findByUserName(String username) {
        if (username == null || username.isBlank()) {
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

            if (users.size() > 1) {
                throw new IllegalStateException(
                        "Database có username bị trùng.");
            }

            return users.isEmpty() ? null : users.get(0);
        } finally {
            em.close();
        }
    }

    @Override
    public void insert(UserModel user) {
        Objects.requireNonNull(user, "User không được null.");

        if (user.getId() != 0) {
            throw new IllegalArgumentException(
                    "User mới phải có id = 0 để database tự sinh.");
        }

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            em.persist(user);
            transaction.commit();
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw e;
        } finally {
            em.close();
        }
    }
    @Override
    public void updateProfile(
            int userId,
            String fullname,
            String phone,
            String images) {

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            UserModel user = em.find(UserModel.class, userId);

            if (user == null) {
                throw new IllegalArgumentException(
                        "Tài khoản không còn tồn tại.");
            }

            user.setFullname(fullname);
            user.setPhone(phone);

            // null nghĩa là người dùng không chọn ảnh mới.
            if (images != null) {
                user.setImages(images);
            }

            transaction.commit();
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            throw e;
        } finally {
            em.close();
        }
    }
}