package vn.iotstar.services;

import java.util.List;

import vn.iotstar.entity.Product;

public interface IProductService {

    List<Product> findAll();

    Product findById(int id);

    Product findPublicById(int id);

    void insert(Product product, int categoryId);

    void update(Product product, int categoryId);

    void delete(int id);

    List<Product> findNewest();

    List<Product> findPublicPage(int page);

    long countPublic();
}