package vn.iotstar.services.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import vn.iotstar.dao.IProductDao;
import vn.iotstar.dao.impl.ProductDao;
import vn.iotstar.entity.Product;
import vn.iotstar.services.IProductService;

public class ProductService implements IProductService {

    private final IProductDao productDao = new ProductDao();

    @Override
    public List<Product> findAll() {
        return productDao.findAll();
    }

    @Override
    public Product findById(int id) {
        return id <= 0 ? null : productDao.findById(id);
    }

    @Override
    public Product findPublicById(int id) {
        Product product = findById(id);

        if (product == null
                || product.getActive() != 1
                || product.getCategory().getStatus() != 1) {
            return null;
        }

        return product;
    }

    @Override
    public void insert(Product product, int categoryId) {
        validate(product, categoryId);
        productDao.insert(product, categoryId);
    }

    @Override
    public void update(Product product, int categoryId) {
        validate(product, categoryId);

        if (product.getId() <= 0) {
            throw new IllegalArgumentException(
                    "Mã sản phẩm không hợp lệ.");
        }

        productDao.update(product, categoryId);
    }

    @Override
    public void delete(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Mã sản phẩm không hợp lệ.");
        }

        productDao.delete(id);
    }

    @Override
    public List<Product> findNewest() {
        return productDao.findNewest(10);
    }

    @Override
    public List<Product> findPublicPage(int page) {
        return productDao.findPublicPage(page, 6);
    }

    @Override
    public long countPublic() {
        return productDao.countPublic();
    }

    private void validate(Product product, int categoryId) {
        if (product == null) {
            throw new IllegalArgumentException(
                    "Dữ liệu sản phẩm không được để trống.");
        }

        String name = product.getName() == null
                ? ""
                : product.getName().trim();

        if (name.isBlank() || name.length() > 200) {
            throw new IllegalArgumentException(
                    "Tên sản phẩm phải có từ 1 đến 200 ký tự.");
        }

        product.setName(name);

        if (product.getDescription() != null
                && product.getDescription().length() > 5000) {
            throw new IllegalArgumentException(
                    "Mô tả không được vượt quá 5000 ký tự.");
        }

        BigDecimal price = product.getPrice();

        if (price == null
                || price.signum() < 0
                || price.compareTo(
                        new BigDecimal("9999999999999.99")) > 0) {

            throw new IllegalArgumentException(
                    "Giá phải từ 0 đến 9999999999999.99.");
        }

        try {
            product.setPrice(
                    price.setScale(2, RoundingMode.UNNECESSARY));
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Giá chỉ được có tối đa 2 chữ số thập phân.");
        }

        if (product.getQuantity() < 0) {
            throw new IllegalArgumentException(
                    "Số lượng không được âm.");
        }

        if (product.getActive() != 0
                && product.getActive() != 1) {
            throw new IllegalArgumentException(
                    "Trạng thái sản phẩm không hợp lệ.");
        }

        if (categoryId <= 0) {
            throw new IllegalArgumentException(
                    "Bạn phải chọn danh mục.");
        }
    }
}