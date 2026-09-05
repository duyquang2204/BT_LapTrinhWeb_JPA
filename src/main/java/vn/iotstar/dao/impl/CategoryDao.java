package vn.iotstar.dao.impl;

import java.util.List;
import java.util.Objects;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import vn.iotstar.configs.JPAConfig;
import vn.iotstar.dao.ICategoryDao;
import vn.iotstar.entity.Category;

public class CategoryDao implements ICategoryDao {

    @Override
    public List<Category> findAll() {
        EntityManager em = JPAConfig.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT c FROM Category c ORDER BY c.categoryid DESC",
                    Category.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Category findById(int cateid) {
        if (cateid <= 0) {
            return null;
        }

        EntityManager em = JPAConfig.getEntityManager();

        try {
            return em.find(Category.class, cateid);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Category> findByCategoryname(String catname) {
        String keyword = catname == null ? "" : catname.trim();

        EntityManager em = JPAConfig.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT c FROM Category c "
                            + "WHERE LOWER(c.categoryname) LIKE LOWER(:keyword) "
                            + "ORDER BY c.categoryid DESC",
                    Category.class)
                    .setParameter("keyword", "%" + keyword + "%")
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Category> findAll(int page, int pagesize) {
        // Giữ quy ước của DAO cũ: trang đầu tiên có page = 0.
        if (page < 0 || pagesize <= 0) {
            throw new IllegalArgumentException(
                    "Trang phải >= 0 và kích thước trang phải > 0.");
        }

        int offset = Math.multiplyExact(page, pagesize);
        EntityManager em = JPAConfig.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT c FROM Category c ORDER BY c.categoryid DESC",
                    Category.class)
                    .setFirstResult(offset)
                    .setMaxResults(pagesize)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public int count() {
        EntityManager em = JPAConfig.getEntityManager();

        try {
            Long total = em.createQuery(
                    "SELECT COUNT(c) FROM Category c",
                    Long.class)
                    .getSingleResult();

            return Math.toIntExact(total);
        } finally {
            em.close();
        }
    }

    @Override
    public void insert(Category category) {
        validate(category);

        if (category.getCategoryid() != 0) {
            throw new IllegalArgumentException(
                    "Category mới phải có categoryid = 0.");
        }

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            category.setCategoryname(
                    category.getCategoryname().trim());

            em.persist(category);
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
    public void update(Category category) {
        validate(category);

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            Category existing = em.find(
                    Category.class, category.getCategoryid());

            if (existing == null) {
                throw new IllegalArgumentException(
                        "Không tìm thấy danh mục cần cập nhật.");
            }

            existing.setCategoryname(
                    category.getCategoryname().trim());
            existing.setStatus(category.getStatus());
            existing.setImages(category.getImages());

            // existing đang được JPA quản lý:
            // commit sẽ ghi các thay đổi xuống database.
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
    public void delete(int cateid) throws Exception {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            Category category = em.find(Category.class, cateid);

            if (category == null) {
                throw new IllegalArgumentException(
                        "Không tìm thấy danh mục cần xóa.");
            }

            // Chưa xóa Category đang có Video.
            Long videoCount = em.createQuery(
                    "SELECT COUNT(v) FROM Video v "
                            + "WHERE v.category.categoryid = :categoryid",
                    Long.class)
                    .setParameter("categoryid", cateid)
                    .getSingleResult();

            if (videoCount > 0) {
                throw new IllegalStateException(
                        "Danh mục đang có video, không thể xóa.");
            }

            em.remove(category);
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

    private void validate(Category category) {
        Objects.requireNonNull(
                category, "Danh mục không được null.");

        String name = category.getCategoryname();

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Tên danh mục không được để trống.");
        }

        if (name.trim().length() > 200) {
            throw new IllegalArgumentException(
                    "Tên danh mục không được vượt quá 200 ký tự.");
        }

        if (category.getStatus() != 0
                && category.getStatus() != 1) {
            throw new IllegalArgumentException(
                    "Trạng thái chỉ nhận giá trị 0 hoặc 1.");
        }
    }
}