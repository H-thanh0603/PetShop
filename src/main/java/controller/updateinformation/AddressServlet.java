package controller.updateinformation;

import DAO.AddressDao;
import DAO.UserDAO;
import Model.User;
import Model.Address;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet("/addresses")
public class AddressServlet extends HttpServlet {
    private AddressDao dao = new AddressDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String defaultIdStr = request.getParameter("defaultId");

        if (defaultIdStr != null && !defaultIdStr.isEmpty()) {
            int defaultId = Integer.parseInt(defaultIdStr);
            dao.setDefaultAddress(user.getId(), defaultId);
        }

        response.sendRedirect(request.getContextPath() + "/checkout");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String addressDetail = request.getParameter("addressDetail");
        String province = request.getParameter("province");
        String district = request.getParameter("district");
        String ward = request.getParameter("ward");

        boolean isDefault = request.getParameter("isDefault") != null;

        // validate
        if (addressDetail == null || addressDetail.trim().isEmpty()
                || province == null || province.trim().isEmpty()
                || district == null || district.trim().isEmpty()
                || ward == null || ward.trim().isEmpty()) {

            response.sendRedirect(request.getContextPath() + "/checkout");
            return;
        }

        // nếu chưa có địa chỉ → auto default
        if (!dao.hasAnyAddress(user.getId())) {
            isDefault = true;
        }

        dao.addAddress(
                user.getId(),
                isDefault,
                Timestamp.valueOf(LocalDateTime.now()),
                addressDetail.trim(),
                province.trim(),
                district.trim(),
                ward.trim()
        );

        response.sendRedirect(request.getContextPath() + "/checkout");
    }
}