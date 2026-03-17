package controller.shop;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import DAO.ProductDAO;
import Model.Product;

@WebServlet("/shop")
public class ShopServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. Gọi DAO để lấy dữ liệu
        ProductDAO dao = new ProductDAO();
        List<Product> list = dao.getAllProducts();

        // 2. Đẩy dữ liệu sang trang JSP với tên biến là "products"
        request.setAttribute("products", list);

        // 3. Chuyển hướng trang
        request.getRequestDispatcher("/pages/shop/shop.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
