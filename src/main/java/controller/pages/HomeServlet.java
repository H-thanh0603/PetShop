package controller.pages;

import java.io.IOException;
import DAO.PromotionDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final PromotionDAO promotionDAO = new PromotionDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("flashSaleProducts", promotionDAO.getFlashSaleProducts(8));
        request.getRequestDispatcher("/pages/main/home.jsp").forward(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}

