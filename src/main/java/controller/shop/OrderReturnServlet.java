package controller.shop;

import DAO.OrderReturnDAO;
import Model.OrderReturn;
import Model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/order-return")
public class OrderReturnServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        request.setAttribute("orderId", request.getParameter("orderId"));
        request.setAttribute("totalPrice", request.getParameter("totalPrice"));
        request.getRequestDispatcher("/pages/shop/order-return.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        try {
            int orderId       = Integer.parseInt(request.getParameter("orderId"));
            double refundAmount = Double.parseDouble(request.getParameter("totalPrice"));
            String reason     = request.getParameter("reason");

            OrderReturn req = new OrderReturn();
            req.setOrderId(orderId);
            req.setUserId(user.getId());
            req.setReason(reason);
            req.setRefundAmount(refundAmount);

            boolean success = new OrderReturnDAO().insertReturnRequest(req);
            if (success) {
                response.sendRedirect(request.getContextPath() + "/my-orders?msg=return_requested");
            } else {
                request.setAttribute("error", "Có lỗi xảy ra, vui lòng thử lại!");
                request.setAttribute("orderId", orderId);
                request.setAttribute("totalPrice", refundAmount);
                request.getRequestDispatcher("/pages/shop/order-return.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/my-orders");
        }
    }
}