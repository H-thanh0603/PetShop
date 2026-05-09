package controller.admin;

import DAO.OrderReturnDAO;
import Model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/order-returns")
public class AdminOrderReturnServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }
        request.setAttribute("returnList", new OrderReturnDAO().getAllReturns());
        request.getRequestDispatcher("/pages/admin/manage-returns.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = (User) request.getSession().getAttribute("user");
        if (user == null || !"admin".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }
        int returnId        = Integer.parseInt(request.getParameter("returnId"));
        int orderId         = Integer.parseInt(request.getParameter("orderId"));
        String action       = request.getParameter("action");
        String adminComment = request.getParameter("adminComment");
        String status       = "APPROVE".equals(action) ? "Approved" : "Rejected";

        boolean success = new OrderReturnDAO().updateReturnStatus(returnId, orderId, status, adminComment);
        response.sendRedirect(request.getContextPath() + "/admin/order-returns?msg=" + (success ? "success" : "fail"));
    }
}