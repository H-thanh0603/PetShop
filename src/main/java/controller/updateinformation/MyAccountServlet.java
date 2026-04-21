package controller.updateinformation;

import DAO.AddressDao;
import DAO.OrderDAO;
import DAO.UserDAO;
import Model.Address;
import Model.User;
import Util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/my-account")
public class MyAccountServlet extends HttpServlet {
    private final AddressDao addressDao = new AddressDao();
    private final OrderDAO orderDAO = new OrderDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect="
                    + request.getContextPath() + "/my-account");
            return;
        }

        List<Address> addressList = addressDao.getAddressesByUserId(user.getId());
        request.setAttribute("addressList", addressList);
        request.setAttribute("defaultAddress", addressDao.getDefaultAddressByUserId(user.getId()));
        request.setAttribute("countPending", orderDAO.countPendingOrdersByUserId(user.getId()));
        request.setAttribute("countCompleted", orderDAO.countCompletedOrdersByUserId(user.getId()));
        java.util.List<Model.Order> recentOrders = orderDAO.getOrdersByUserId(user.getId());
        if (recentOrders.size() > 3) {
            recentOrders = recentOrders.subList(0, 3);
        }
        request.setAttribute("recentOrders", recentOrders);
        request.getRequestDispatcher("/pages/shop/my-account.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?redirect="
                    + request.getContextPath() + "/my-account");
            return;
        }

        String fullname = request.getParameter("fullname") == null ? "" : request.getParameter("fullname").trim();
        String email = request.getParameter("email") == null ? "" : request.getParameter("email").trim().toLowerCase();
        String phone = ValidationUtil.normalizePhone(request.getParameter("phone"));

        if (fullname.isEmpty() || !fullname.matches("^[\\p{L}\\s]+$") || fullname.replaceAll("\\s", "").length() < 2) {
            session.setAttribute("error", "Họ tên không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/my-account");
            return;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            session.setAttribute("error", "Email không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/my-account");
            return;
        }

        if (!phone.isEmpty() && !ValidationUtil.isValidPhone(phone)) {
            session.setAttribute("error", "Số điện thoại không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/my-account");
            return;
        }

        if (userDAO.isEmailTakenByAnotherUser(email, user.getId())) {
            session.setAttribute("error", "Email đã được tài khoản khác sử dụng.");
            response.sendRedirect(request.getContextPath() + "/my-account");
            return;
        }

        if (userDAO.isPhoneTakenByAnotherUser(phone, user.getId())) {
            session.setAttribute("error", "Số điện thoại đã được tài khoản khác sử dụng.");
            response.sendRedirect(request.getContextPath() + "/my-account");
            return;
        }

        if (userDAO.updateProfileAndEmail(user.getId(), fullname, phone, email)) {
            user.setFullname(fullname);
            user.setPhone(phone);
            user.setEmail(email);
            session.setAttribute("user", user);
            session.setAttribute("success", "Cập nhật tài khoản thành công.");
        } else {
            session.setAttribute("error", "Không thể cập nhật tài khoản.");
        }
        response.sendRedirect(request.getContextPath() + "/my-account");
    }
}
