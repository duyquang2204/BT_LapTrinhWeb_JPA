package vn.iotstar.dao;

import java.util.List;

import vn.iotstar.entity.Product;

public interface IProductDao {

    List<Product> findAll();

    Product findById(int id);

    void insert(Product product, int categoryId);

    void update(Product product, int categoryId);

    void delete(int id);

    List<Product> findNewest(int limit);

    List<Product> findPublicPage(int page, int pageSize);

    long countPublic();
}