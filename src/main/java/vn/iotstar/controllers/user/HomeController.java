package vn.iotstar.controllers.user;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import vn.iotstar.services.IProductService;
import vn.iotstar.services.impl.ProductService;

@WebServlet("/user/home")
public class HomeController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final IProductService productService =
            new ProductService();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/html;charset=UTF-8");

        req.setAttribute("products", productService.findNewest());

        req.getRequestDispatcher("/views/user/home.jsp")
                .forward(req, resp);
    }
}