package vn.iotstar.dao.impl;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import vn.iotstar.configs.JPAConfig;
import vn.iotstar.dao.IProductDao;
import vn.iotstar.entity.Category;
import vn.iotstar.entity.Product;

public class ProductDao implements IProductDao {

    private static final String PUBLIC_CONDITION =
            "p.active = 1 AND c.status = 1";

    @Override
    public List<Product> findAll() {
        EntityManager em = JPAConfig.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT p FROM Product p "
                            + "JOIN FETCH p.category "
                            + "ORDER BY p.createdAt DESC, p.id DESC",
                    Product.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Product findById(int id) {
        EntityManager em = JPAConfig.getEntityManager();

        try {
            List<Product> list = em.createQuery(
                    "SELECT p FROM Product p "
                            + "JOIN FETCH p.category "
                            + "WHERE p.id = :id",
                    Product.class)
                    .setParameter("id", id)
                    .getResultList();

            return list.isEmpty() ? null : list.get(0);
        } finally {
            em.close();
        }
    }

    @Override
    public void insert(Product product, int categoryId) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            if (product.getId() != 0) {
                throw new IllegalArgumentException(
                        "Sản phẩm mới phải có ID bằng 0.");
            }

            Category category = requireCategory(em, categoryId);

            product.setCategory(category);
            product.setCreatedAt(
                    LocalDateTime.now(ZoneOffset.UTC));

            em.persist(product);
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

    @Override
    public void update(Product product, int categoryId) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Product existing = em.find(Product.class, product.getId());

            if (existing == null) {
                throw new IllegalArgumentException(
                        "Không tìm thấy sản phẩm.");
            }

            Category category = requireCategory(em, categoryId);

            existing.setName(product.getName());
            existing.setDescription(product.getDescription());
            existing.setPrice(product.getPrice());
            existing.setQuantity(product.getQuantity());
            existing.setImages(product.getImages());
            existing.setActive(product.getActive());
            existing.setCategory(category);

            // Giữ ngày tạo cũ khi cập nhật.
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

    @Override
    public void delete(int id) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Product product = em.find(Product.class, id);

            if (product == null) {
                throw new IllegalArgumentException(
                        "Không tìm thấy sản phẩm.");
            }

            em.remove(product);
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

    @Override
    public List<Product> findNewest(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException(
                    "Số sản phẩm phải lớn hơn 0.");
        }

        EntityManager em = JPAConfig.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT p FROM Product p "
                            + "JOIN FETCH p.category c "
                            + "WHERE " + PUBLIC_CONDITION + " "
                            + "ORDER BY p.createdAt DESC, p.id DESC",
                    Product.class)
                    .setMaxResults(limit)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Product> findPublicPage(int page, int pageSize) {
        if (page < 1 || pageSize < 1) {
            throw new IllegalArgumentException(
                    "Trang và kích thước trang phải lớn hơn 0.");
        }

        int offset = Math.multiplyExact(page - 1, pageSize);

        EntityManager em = JPAConfig.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT p FROM Product p "
                            + "JOIN FETCH p.category c "
                            + "WHERE " + PUBLIC_CONDITION + " "
                            + "ORDER BY p.createdAt DESC, p.id DESC",
                    Product.class)
                    .setFirstResult(offset)
                    .setMaxResults(pageSize)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public long countPublic() {
        EntityManager em = JPAConfig.getEntityManager();

        try {
            return em.createQuery(
                    "SELECT COUNT(p) FROM Product p "
                            + "JOIN p.category c "
                            + "WHERE " + PUBLIC_CONDITION,
                    Long.class)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    private Category requireCategory(
            EntityManager em,
            int categoryId) {

        Category category = em.find(Category.class, categoryId);

        if (category == null) {
            throw new IllegalArgumentException(
                    "Danh mục không tồn tại.");
        }

        return category;
    }
}