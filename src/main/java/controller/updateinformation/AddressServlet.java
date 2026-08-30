package controller.updateinformation;

import DAO.AddressDao;
import Model.User;
import Util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class AddressServlet extends HttpServlet {
    private AddressDao dao = new AddressDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(resolveRedirectTarget(request));
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
        if ("patch".equalsIgnoreCase(method) || "setDefault".equalsIgnoreCase(request.getParameter("action"))) {
            handleSetDefault(request, response);
            return;
        }
        if ("delete".equalsIgnoreCase(method)) {
            handleDelete(request, response);
            return;
        }
        handleAdd(request, response);
    }

    private void handleSetDefault(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Integer addressId = ValidationUtil.parseIntOrNull(request.getParameter("id"));
        if (addressId != null && !dao.setDefaultAddress(user.getId(), addressId)) {
            redirectToCheckoutWithToast(request, response, "Không thể chọn địa chỉ giao hàng. Vui lòng thử lại.");
            return;
        }

        redirectToCheckout(request, response);
    }
    private void handleDelete(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Integer addressId = ValidationUtil.parseIntOrNull(request.getParameter("id"));
        if (addressId == null) {
            redirectToCheckout(request, response);
            return;
        }

        boolean wasDefault = dao.isDefaultAddress(addressId, user.getId());
        boolean deleted = dao.deleteAddress(addressId, user.getId());

        if (deleted && wasDefault && dao.hasAnyAddress(user.getId())) {
            dao.setNewestAddressAsDefault(user.getId());
        }

        redirectToCheckout(request, response);
    }
    private void handleAdd(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String addressDetail = ValidationUtil.normalizeAddressDetail(request.getParameter("addressDetail"));
        String province = trimToEmpty(request.getParameter("province"));
        String district = trimToEmpty(request.getParameter("district"));
        String ward = trimToEmpty(request.getParameter("ward"));
        boolean isDefault = request.getParameter("isDefault") != null;
        if (shouldPromoteSavedAddress(request)) {
            isDefault = true;
        }

        if (addressDetail.isEmpty() || province.isEmpty() || district.isEmpty() || ward.isEmpty()) {
            redirectToCheckoutWithToast(request, response, "Vui lòng nhập đầy đủ thông tin địa chỉ.");
            return;
        }

        String addressDetailError = ValidationUtil.validateAddressDetail(addressDetail);
        if (addressDetailError != null) {
            redirectToCheckoutWithToast(request, response, addressDetailError);
            return;
        }

        if (!dao.hasAnyAddress(user.getId())) {
            isDefault = true;
        }

        boolean saved = dao.addAddress(
                user.getId(),
                isDefault,
                Timestamp.valueOf(LocalDateTime.now()),
                addressDetail,
                province,
                district,
                ward
        );
        if (!saved) {
            redirectToCheckoutWithToast(request, response, "Không thể lưu địa chỉ. Vui lòng thử lại.");
            return;
        }

        redirectToCheckout(request, response);
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Integer id = ValidationUtil.parseIntOrNull(request.getParameter("id"));
        String addressDetail = ValidationUtil.normalizeAddressDetail(request.getParameter("addressDetail"));
        String province = trimToEmpty(request.getParameter("province"));
        String district = trimToEmpty(request.getParameter("district"));
        String ward = trimToEmpty(request.getParameter("ward"));
        boolean isDefault = request.getParameter("isDefault") != null;
        if (shouldPromoteSavedAddress(request)) {
            isDefault = true;
        }

        if (id == null || addressDetail.isEmpty() || province.isEmpty() || district.isEmpty() || ward.isEmpty()) {
            redirectToCheckoutWithToast(request, response, "Vui lòng nhập đầy đủ thông tin địa chỉ.");
            return;
        }

        String addressDetailError = ValidationUtil.validateAddressDetail(addressDetail);
        if (addressDetailError != null) {
            redirectToCheckoutWithToast(request, response, addressDetailError);
            return;
        }

        boolean updated = dao.updateAddress(
                id,
                user.getId(),
                isDefault,
                Timestamp.valueOf(LocalDateTime.now()),
                addressDetail,
                province,
                district,
                ward
        );
        if (!updated) {
            redirectToCheckoutWithToast(request, response, "Không thể cập nhật địa chỉ. Vui lòng thử lại.");
            return;
        }

        redirectToCheckout(request, response);
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean shouldPromoteSavedAddress(HttpServletRequest request) {
        String source = trimToEmpty(request.getParameter("source"));
        return "checkout".equalsIgnoreCase(source) || "account".equalsIgnoreCase(source);
    }

    private void redirectToCheckout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(resolveRedirectTarget(request));
    }

    private void redirectToCheckoutWithToast(HttpServletRequest request, HttpServletResponse response,
                                             String message) throws IOException {
        HttpSession session = request.getSession();
        session.setAttribute("toastMessage", message);
        session.setAttribute("toastType", "warning");
        redirectToCheckout(request, response);
    }

    private String resolveRedirectTarget(HttpServletRequest request) {
        String redirect = request.getParameter("redirect");
        if ("account".equalsIgnoreCase(redirect)) {
            return request.getContextPath() + "/my-account";
        }
        String source = request.getParameter("source");
        if ("account".equalsIgnoreCase(source)) {
            return request.getContextPath() + "/my-account";
        }
        return request.getContextPath() + "/checkout";
    }
}
