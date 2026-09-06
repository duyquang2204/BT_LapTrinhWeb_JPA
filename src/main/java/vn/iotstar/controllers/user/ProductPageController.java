package vn.iotstar.controllers.user;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import vn.iotstar.entity.Product;
import vn.iotstar.services.IProductService;
import vn.iotstar.services.impl.ProductService;

@WebServlet(urlPatterns = {
        "/product",
        "/product/detail"
})
public class ProductPageController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 6;

    private final IProductService productService =
            new ProductService();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        if ("/product/detail".equals(req.getServletPath())) {
            showDetail(req, resp);
        } else {
            showList(req, resp);
        }
    }

    private void showList(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        String pageText = req.getParameter("page");
        int page = 1;

        if (pageText != null) {
            try {
                page = Integer.parseInt(pageText);

                if (page < 1) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                resp.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Số trang phải là số nguyên dương.");
                return;
            }
        }

        long totalProducts = productService.countPublic();

        long totalPages = Math.max(
                1L,
                totalProducts / PAGE_SIZE
                        + (totalProducts % PAGE_SIZE == 0 ? 0 : 1));

        if (page > totalPages) {
            resp.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Trang sản phẩm không tồn tại.");
            return;
        }

        // DAO hiện dùng offset kiểu int.
        if ((long) (page - 1) * PAGE_SIZE > Integer.MAX_VALUE) {
            resp.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Số trang vượt giới hạn.");
            return;
        }

        req.setAttribute(
                "products", productService.findPublicPage(page));

        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalProducts", totalProducts);

        // Chỉ hiển thị một nhóm nút trang quanh trang hiện tại.
        req.setAttribute("startPage", Math.max(1, page - 2));
        req.setAttribute(
                "endPage", Math.min(totalPages, (long) page + 2));

        req.getRequestDispatcher("/views/user/product-list.jsp")
                .forward(req, resp);
    }

    private void showDetail(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        int id;

        try {
            id = Integer.parseInt(req.getParameter("id"));

            if (id <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            resp.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Mã sản phẩm không hợp lệ.");
            return;
        }

        Product product = productService.findPublicById(id);

        if (product == null) {
            resp.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Sản phẩm không tồn tại hoặc đang bị ẩn.");
            return;
        }

        req.setAttribute("product", product);

        req.getRequestDispatcher("/views/user/product-detail.jsp")
                .forward(req, resp);
    }
}