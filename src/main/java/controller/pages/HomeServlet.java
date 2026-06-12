package controller.pages;

import java.io.IOException;
import java.util.List;

import DAO.ProductDAO;
import Model.Product;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Top 5 bán chạy — dùng method có sẵn trong ProductDAO
        List<Product> popularProducts = productDAO.getPopularProductsPage(1, 5);

        // Tổng sản phẩm active cho hero stats — dùng method có sẵn
        int totalProducts = productDAO.getTotalPopularProductsCount();

        request.setAttribute("popularProducts", popularProducts);
        request.setAttribute("totalProducts", totalProducts);

        request.getRequestDispatcher("/pages/main/home.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}