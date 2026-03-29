package controller.shop;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import DAO.CouponDAO;
import Model.Coupon;
import Model.User;

/**
 * API Servlet xử lý Coupon - Mã giảm giá
 */
@WebServlet("/api/coupon")
public class CouponServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CouponDAO couponDAO = new CouponDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        
        if (user == null) {
            response.getWriter().write("{\"success\":false,\"message\":\"Vui lòng đăng nhập\"}");
            return;
        }
        
        String action = request.getParameter("action");
        String code = request.getParameter("code");
        
        if ("apply".equals(action)) {
            // Áp dụng mã giảm giá
            if (code == null || code.trim().isEmpty()) {
                response.getWriter().write("{\"success\":false,\"message\":\"Vui lòng nhập mã giảm giá\"}");
                return;
            }
            
            double orderTotal = Double.parseDouble(request.getParameter("orderTotal"));
            Coupon coupon = couponDAO.getCouponByCode(code.trim());
            
            if (coupon == null) {
                response.getWriter().write("{\"success\":false,\"message\":\"Mã giảm giá không tồn tại\"}");
                return;
            }
            
            if (!couponDAO.isValidCoupon(code, orderTotal)) {
                String reason = "";
                if (orderTotal < coupon.getMinOrder()) {
                    reason = "Đơn hàng tối thiểu " + String.format("%,.0f", coupon.getMinOrder()) + "đ";
                } else if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
                    reason = "Mã đã hết lượt sử dụng";
                } else {
                    reason = "Mã không còn hiệu lực";
                }
                response.getWriter().write("{\"success\":false,\"message\":\"" + reason + "\"}");
                return;
            }
            
            double discount = couponDAO.calculateDiscount(code, orderTotal);
            
            // Lưu coupon vào session
            session.setAttribute("appliedCoupon", coupon);
            session.setAttribute("couponDiscount", discount);
            
            response.getWriter().write("{\"success\":true" +
                    ",\"message\":\"Áp dụng thành công: " + coupon.getDescription() + "\"" +
                    ",\"discount\":" + discount +
                    ",\"code\":\"" + coupon.getCode() + "\"" +
                    ",\"description\":\"" + coupon.getDescription() + "\"}");
            
        } else if ("remove".equals(action)) {
            // Xóa mã giảm giá
            session.removeAttribute("appliedCoupon");
            session.removeAttribute("couponDiscount");
            response.getWriter().write("{\"success\":true,\"message\":\"Đã xóa mã giảm giá\"}");
        }
    }
}
