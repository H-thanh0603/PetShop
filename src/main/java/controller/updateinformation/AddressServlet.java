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

        String method = request.getParameter("_method");
        if ("put".equalsIgnoreCase(method)) {
            handleUpdate(request, response);
            return;
        }
        if ("delete".equalsIgnoreCase(method)) {
            handleDelete(request, response);
            return;
        }
        handleAdd(request, response);
    }
    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String idRaw = request.getParameter("id");
        if (idRaw == null || idRaw.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/checkout");
            return;
        }

        int addressId = Integer.parseInt(idRaw);

        boolean wasDefault = dao.isDefaultAddress(addressId, user.getId());
        boolean deleted = dao.deleteAddress(addressId, user.getId());

        if (deleted && wasDefault && dao.hasAnyAddress(user.getId())) {
            dao.setNewestAddressAsDefault(user.getId());
        }

        response.sendRedirect(request.getContextPath() + "/checkout");
    }
    private void handleAdd(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

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

        if (addressDetail == null || addressDetail.trim().isEmpty()
                || province == null || province.trim().isEmpty()
                || district == null || district.trim().isEmpty()
                || ward == null || ward.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/checkout");
            return;
        }

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

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String idRaw = request.getParameter("id");
        String addressDetail = request.getParameter("addressDetail");
        String province = request.getParameter("province");
        String district = request.getParameter("district");
        String ward = request.getParameter("ward");
        boolean isDefault = request.getParameter("isDefault") != null;

        if (idRaw == null || idRaw.trim().isEmpty()
                || addressDetail == null || addressDetail.trim().isEmpty()
                || province == null || province.trim().isEmpty()
                || district == null || district.trim().isEmpty()
                || ward == null || ward.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/checkout");
            return;
        }

        int id = Integer.parseInt(idRaw);

        dao.updateAddress(
                id,
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