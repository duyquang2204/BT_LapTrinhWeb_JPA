package vn.iotstar.controllers.admin;

import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import vn.iotstar.entity.Category;
import vn.iotstar.services.ICategoryService;
import vn.iotstar.services.impl.CategoryService;
import vn.iotstar.utils.ImageUploadUtil;

@WebServlet(urlPatterns = {
        "/admin/categories",
        "/admin/category/search",
        "/admin/category/add",
        "/admin/category/insert",
        "/admin/category/edit",
        "/admin/category/update",
        "/admin/category/delete"
})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5L * 1024 * 1024,
        maxRequestSize = 6L * 1024 * 1024
)
public class CategoryController extends HttpServlet {

    private static final long serialVersionUID = 1L;

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

        try {
            switch (path) {
                case "/admin/categories":
                case "/admin/category/search":
                    showList(req, resp);
                    break;

                case "/admin/category/add":
                    Category category = new Category();
                    category.setStatus(1);

                    req.setAttribute("cate", category);
                    showForm(req, resp, false);
                    break;

                case "/admin/category/edit":
                    int id = parsePositiveId(req.getParameter("id"));
                    Category existing = categoryService.findById(id);

                    if (existing == null) {
                        resp.sendError(
                                HttpServletResponse.SC_NOT_FOUND,
                                "Không tìm thấy danh mục.");
                        return;
                    }

                    req.setAttribute("cate", existing);
                    showForm(req, resp, true);
                    break;

                default:
                    // Các URL ghi dữ liệu chỉ chấp nhận POST.
                    resp.setHeader("Allow", "POST");
                    resp.sendError(
                            HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } catch (IllegalArgumentException e) {
            resp.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Tham số không hợp lệ.");
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        String submittedToken;

        try {
            submittedToken = req.getParameter("csrfToken");
        } catch (IllegalStateException e) {
            resp.sendError(
                    413,
                    "File hoặc tổng dữ liệu gửi lên quá lớn.");
            return;
        }

        HttpSession session = req.getSession(false);

        String expectedToken = session == null
                ? null
                : (String) session.getAttribute("csrfToken");

        if (expectedToken == null
                || !expectedToken.equals(submittedToken)) {

            resp.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Phiên biểu mẫu không hợp lệ. Hãy tải lại trang.");
            return;
        }

        String path = req.getServletPath();

        switch (path) {
            case "/admin/category/insert":
                saveCategory(req, resp, false);
                break;

            case "/admin/category/update":
                saveCategory(req, resp, true);
                break;

            case "/admin/category/delete":
                deleteCategory(req, resp);
                break;

            default:
                resp.setHeader("Allow", "GET");
                resp.sendError(
                        HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void showList(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        String keyword = req.getParameter("keyword");

        if (keyword == null) {
            keyword = "";
        }

        keyword = keyword.trim();
        req.setAttribute("keyword", keyword);

        if (keyword.isEmpty()) {
            req.setAttribute(
                    "listcate", categoryService.findAll());
        } else {
            req.setAttribute(
                    "listcate",
                    categoryService.findByCategoryname(keyword));
        }

        HttpSession session = req.getSession();

        // Thông báo chỉ hiển thị một lần sau redirect.
        req.setAttribute(
                "success", session.getAttribute("categorySuccess"));
        req.setAttribute(
                "error", session.getAttribute("categoryError"));

        session.removeAttribute("categorySuccess");
        session.removeAttribute("categoryError");

        req.getRequestDispatcher(
                "/views/admin/category-list.jsp")
                .forward(req, resp);
    }

    private void saveCategory(
            HttpServletRequest req,
            HttpServletResponse resp,
            boolean editing)
            throws ServletException, IOException {

        Category category;

        if (editing) {
            int id;

            try {
                id = parsePositiveId(
                        req.getParameter("categoryid"));
            } catch (IllegalArgumentException e) {
                resp.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Mã danh mục không hợp lệ.");
                return;
            }

            category = categoryService.findById(id);

            if (category == null) {
                resp.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy danh mục.");
                return;
            }
        } else {
            category = new Category();
            category.setStatus(1);
        }

        String name = req.getParameter("categoryname");
        String status = req.getParameter("status");

        category.setCategoryname(
                name == null ? "" : name.trim());

        boolean validStatus =
                "0".equals(status) || "1".equals(status);

        if (validStatus) {
            category.setStatus(Integer.parseInt(status));
        }

        req.setAttribute("cate", category);

        if (category.getCategoryname().isBlank()) {
            showFormError(
                    req, resp, editing,
                    "Tên danh mục không được để trống.");
            return;
        }

        if (category.getCategoryname().length() > 200) {
            showFormError(
                    req, resp, editing,
                    "Tên danh mục không được vượt quá 200 ký tự.");
            return;
        }

        if (!validStatus) {
            showFormError(
                    req, resp, editing,
                    "Bạn phải chọn trạng thái hợp lệ.");
            return;
        }

        String oldImage = category.getImages();
        String newImage = null;

        try {
            newImage = ImageUploadUtil.saveImage(
                    req.getPart("images"));

            // Không chọn ảnh mới: giữ nguyên ảnh cũ.
            if (newImage != null) {
                category.setImages(newImage);
            }
        } catch (IllegalArgumentException e) {
            showFormError(req, resp, editing, e.getMessage());
            return;
        } catch (IllegalStateException e) {
            showFormError(
                    req, resp, editing,
                    "Ảnh không được vượt quá 5 MB.");
            return;
        } catch (IOException | ServletException e) {
            getServletContext().log("Upload ảnh thất bại.", e);

            showFormError(
                    req, resp, editing,
                    "Không thể đọc hoặc lưu ảnh. "
                            + "Kiểm tra ảnh và quyền ghi thư mục upload.");
            return;
        }

        try {
            if (editing) {
                categoryService.update(category);
            } else {
                categoryService.insert(category);
            }
        } catch (RuntimeException e) {
            getServletContext().log(
                    "Lưu danh mục thất bại.", e);

            // Nếu DB lưu thất bại, dọn ảnh mới vừa tạo.
            deleteNewImageQuietly(newImage);
            category.setImages(oldImage);

            if (!editing) {
                category.setCategoryid(0);
            }

            showFormError(
                    req, resp, editing,
                    "Không thể lưu danh mục. "
                            + "Vui lòng thử lại và chọn lại ảnh nếu có.");
            return;
        }

        req.getSession().setAttribute(
                "categorySuccess",
                editing
                        ? "Cập nhật danh mục thành công."
                        : "Thêm danh mục thành công.");

        resp.sendRedirect(
                req.getContextPath() + "/admin/categories");
    }

    private void deleteCategory(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws IOException {

        int id;

        try {
            id = parsePositiveId(req.getParameter("id"));
        } catch (IllegalArgumentException e) {
            resp.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Mã danh mục không hợp lệ.");
            return;
        }

        try {
            categoryService.delete(id);

            req.getSession().setAttribute(
                    "categorySuccess",
                    "Xóa danh mục thành công.");
        } catch (Exception e) {
            getServletContext().log(
                    "Xóa danh mục thất bại.", e);

            req.getSession().setAttribute(
                    "categoryError",
                    "Không thể xóa danh mục. "
                            + "Danh mục có thể đang được sử dụng "
                            + "hoặc không còn tồn tại.");
        }

        resp.sendRedirect(
                req.getContextPath() + "/admin/categories");
    }

    private void showForm(
            HttpServletRequest req,
            HttpServletResponse resp,
            boolean editing)
            throws ServletException, IOException {

        String view = editing
                ? "/views/admin/category-edit.jsp"
                : "/views/admin/category-add.jsp";

        req.getRequestDispatcher(view).forward(req, resp);
    }

    private void showFormError(
            HttpServletRequest req,
            HttpServletResponse resp,
            boolean editing,
            String message)
            throws ServletException, IOException {

        req.setAttribute("error", message);
        showForm(req, resp, editing);
    }

    private int parsePositiveId(String value) {
        try {
            int id = Integer.parseInt(value);

            if (id <= 0) {
                throw new IllegalArgumentException();
            }

            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Mã danh mục phải là số nguyên dương.", e);
        }
    }

    private void ensureCsrfToken(HttpServletRequest req) {
        HttpSession session = req.getSession();

        synchronized (session) {
            if (session.getAttribute("csrfToken") == null) {
                session.setAttribute(
                        "csrfToken",
                        UUID.randomUUID().toString());
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
                    "Không dọn được ảnh mới sau lỗi lưu dữ liệu.", e);
        }
    }
}