package vn.iotstar.controllers.admin;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.iotstar.entity.Product;
import vn.iotstar.services.ICategoryService;
import vn.iotstar.services.IProductService;
import vn.iotstar.services.impl.CategoryService;
import vn.iotstar.services.impl.ProductService;
import vn.iotstar.utils.ImageUploadUtil;

@WebServlet(urlPatterns = {
        "/admin/products",
        "/admin/product/add",
        "/admin/product/insert",
        "/admin/product/edit",
        "/admin/product/update",
        "/admin/product/delete"
})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5L * 1024 * 1024,
        maxRequestSize = 6L * 1024 * 1024
)
public class ProductController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final IProductService productService =
            new ProductService();

    private final ICategoryService categoryService =
            new CategoryService();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        ensureCsrfToken(req);

        String path = req.getServletPath();

        if ("/admin/products".equals(path)) {
            req.setAttribute("products", productService.findAll());

            HttpSession session = req.getSession();

            req.setAttribute(
                    "success", session.getAttribute("productSuccess"));
            req.setAttribute(
                    "error", session.getAttribute("productError"));

            session.removeAttribute("productSuccess");
            session.removeAttribute("productError");

            req.getRequestDispatcher("/views/admin/product-list.jsp")
                    .forward(req, resp);
            return;
        }

        if ("/admin/product/add".equals(path)) {
            req.setAttribute("editing", false);
            req.setAttribute("productId", 0);
            req.setAttribute("name", "");
            req.setAttribute("description", "");
            req.setAttribute("price", "");
            req.setAttribute("quantity", "0");
            req.setAttribute("active", "1");
            req.setAttribute("categoryId", "");

            showForm(req, resp);
            return;
        }

        if ("/admin/product/edit".equals(path)) {
            int id;

            try {
                id = positiveId(req.getParameter("id"));
            } catch (IllegalArgumentException e) {
                resp.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Mã sản phẩm không hợp lệ.");
                return;
            }

            Product product = productService.findById(id);

            if (product == null) {
                resp.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy sản phẩm.");
                return;
            }

            req.setAttribute("editing", true);
            req.setAttribute("productId", product.getId());
            req.setAttribute("name", product.getName());
            req.setAttribute("description", product.getDescription());
            req.setAttribute("price", product.getPrice().toPlainString());
            req.setAttribute("quantity",
                    String.valueOf(product.getQuantity()));
            req.setAttribute("active",
                    String.valueOf(product.getActive()));
            req.setAttribute("categoryId",
                    String.valueOf(product.getCategory().getCategoryid()));
            req.setAttribute("currentImage", product.getImages());

            showForm(req, resp);
            return;
        }

        resp.setHeader("Allow", "POST");
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        String submitted;

        try {
            submitted = req.getParameter("csrfToken");
        } catch (IllegalStateException e) {
            resp.sendError(413, "File hoặc tổng dữ liệu gửi lên quá lớn.");
            return;
        }

        HttpSession session = req.getSession(false);

        String expected = session == null
                ? null
                : (String) session.getAttribute("csrfToken");

        if (expected == null || !expected.equals(submitted)) {
            resp.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Phiên biểu mẫu không hợp lệ. Hãy tải lại trang.");
            return;
        }

        switch (req.getServletPath()) {
            case "/admin/product/insert":
                save(req, resp, false);
                break;

            case "/admin/product/update":
                save(req, resp, true);
                break;

            case "/admin/product/delete":
                delete(req, resp);
                break;

            default:
                resp.setHeader("Allow", "GET");
                resp.sendError(
                        HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void save(
            HttpServletRequest req,
            HttpServletResponse resp,
            boolean editing)
            throws ServletException, IOException {

        Product product;

        if (editing) {
            int id;

            try {
                id = positiveId(req.getParameter("id"));
            } catch (IllegalArgumentException e) {
                resp.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Mã sản phẩm không hợp lệ.");
                return;
            }

            product = productService.findById(id);

            if (product == null) {
                resp.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy sản phẩm.");
                return;
            }
        } else {
            product = new Product();
        }

        String name = text(req, "name");
        String description = text(req, "description");
        String priceText = text(req, "price");
        String quantityText = text(req, "quantity");
        String activeText = text(req, "active");
        String categoryText = text(req, "categoryId");

        // Giữ dữ liệu người dùng đã nhập nếu form có lỗi.
        req.setAttribute("editing", editing);
        req.setAttribute("productId", product.getId());
        req.setAttribute("name", name);
        req.setAttribute("description", description);
        req.setAttribute("price", priceText);
        req.setAttribute("quantity", quantityText);
        req.setAttribute("active", activeText);
        req.setAttribute("categoryId", categoryText);
        req.setAttribute("currentImage", product.getImages());

        int categoryId;

        try {
            if (name.isBlank() || name.length() > 200) {
                throw new IllegalArgumentException(
                        "Tên sản phẩm phải có từ 1 đến 200 ký tự.");
            }

            if (description.length() > 5000) {
                throw new IllegalArgumentException(
                        "Mô tả không được vượt quá 5000 ký tự.");
            }

            // Nhập giá không có dấu phân cách hàng nghìn.
            if (!priceText.matches("[0-9]{1,13}(\\.[0-9]{1,2})?")) {
                throw new IllegalArgumentException(
                        "Giá không hợp lệ. Ví dụ: 150000 hoặc 150000.50.");
            }

            if (!quantityText.matches("[0-9]{1,10}")) {
                throw new IllegalArgumentException(
                        "Số lượng phải là số nguyên không âm.");
            }

            if (!"0".equals(activeText) && !"1".equals(activeText)) {
                throw new IllegalArgumentException(
                        "Hãy chọn trạng thái hợp lệ.");
            }

            categoryId = positiveId(categoryText);

            product.setName(name);
            product.setDescription(description);
            product.setPrice(new BigDecimal(priceText));
            product.setQuantity(Integer.parseInt(quantityText));
            product.setActive(Integer.parseInt(activeText));

        } catch (NumberFormatException e) {
            showError(
                    req, resp,
                    "Số lượng hoặc mã danh mục vượt giới hạn cho phép.");
            return;
        } catch (IllegalArgumentException e) {
            showError(req, resp, e.getMessage());
            return;
        }

        if (categoryService.findById(categoryId) == null) {
            showError(req, resp, "Danh mục không tồn tại.");
            return;
        }

        String oldImage = product.getImages();
        String newImage;

        try {
            newImage = ImageUploadUtil.saveImage(
                    req.getPart("images"));
        } catch (IllegalArgumentException e) {
            showError(req, resp, e.getMessage());
            return;
        } catch (IllegalStateException e) {
            showError(req, resp, "Ảnh không được vượt quá 5 MB.");
            return;
        } catch (IOException | ServletException e) {
            getServletContext().log("Upload ảnh sản phẩm thất bại.", e);
            showError(req, resp, "Không thể đọc hoặc lưu ảnh.");
            return;
        }

        if (newImage != null) {
            product.setImages(newImage);
        }

        try {
            if (editing) {
                productService.update(product, categoryId);
            } else {
                productService.insert(product, categoryId);
            }
        } catch (RuntimeException e) {
            deleteNewImageQuietly(newImage);
            product.setImages(oldImage);

            getServletContext().log("Lưu sản phẩm thất bại.", e);

            showError(
                    req, resp,
                    "Không thể lưu sản phẩm. Kiểm tra dữ liệu "
                            + "và chọn lại ảnh nếu có.");
            return;
        }

        req.getSession().setAttribute(
                "productSuccess",
                editing
                        ? "Cập nhật sản phẩm thành công."
                        : "Thêm sản phẩm thành công.");

        resp.sendRedirect(req.getContextPath() + "/admin/products");
    }

    private void delete(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws IOException {

        int id;

        try {
            id = positiveId(req.getParameter("id"));
        } catch (IllegalArgumentException e) {
            resp.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Mã sản phẩm không hợp lệ.");
            return;
        }

        try {
            productService.delete(id);

            req.getSession().setAttribute(
                    "productSuccess", "Xóa sản phẩm thành công.");
        } catch (RuntimeException e) {
            getServletContext().log("Xóa sản phẩm thất bại.", e);

            req.getSession().setAttribute(
                    "productError",
                    "Không thể xóa sản phẩm. "
                            + "Sản phẩm có thể không còn tồn tại.");
        }

        resp.sendRedirect(req.getContextPath() + "/admin/products");
    }

    private void showForm(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        req.setAttribute("categories", categoryService.findAll());

        req.getRequestDispatcher("/views/admin/product-form.jsp")
                .forward(req, resp);
    }

    private void showError(
            HttpServletRequest req,
            HttpServletResponse resp,
            String message)
            throws ServletException, IOException {

        req.setAttribute("error", message);
        showForm(req, resp);
    }

    private String text(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private int positiveId(String value) {
        try {
            int id = Integer.parseInt(value);

            if (id <= 0) {
                throw new IllegalArgumentException(
                        "Hãy chọn một mã hợp lệ.");
            }

            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Hãy chọn một mã hợp lệ.", e);
        }
    }

    private void ensureCsrfToken(HttpServletRequest req) {
        HttpSession session = req.getSession();

        synchronized (session) {
            if (session.getAttribute("csrfToken") == null) {
                session.setAttribute(
                        "csrfToken", UUID.randomUUID().toString());
            }
        }
    }

    private void deleteNewImageQuietly(String filename) {
        if (filename == null) {
            return;
        }

        try {
            Files.deleteIfExists(
                    ImageUploadUtil.getUploadDirectory()
                            .resolve(filename));
        } catch (IOException e) {
            getServletContext().log(
                    "Không dọn được ảnh sản phẩm mới.", e);
        }
    }
}