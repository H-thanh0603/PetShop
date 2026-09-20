package com.petshop.web;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import DAO.AddressDao;
import DAO.OrderDAO;
import DAO.OrderSignDAO;
import DAO.OrderSignatureDAO;
import DAO.UserDAO;
import Model.Address;
import Model.Order;
import Model.OrderSign;
import Model.OrderSignature;
import Model.User;
import Util.PasswordUtil;
import Util.ValidationUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Replaces MyAccountServlet (/my-account), AddressServlet (/addresses) and
 * UpdateProfileCheckoutServlet (/update-profile-checkout) 1:1.
 */
@Controller
public class AccountController {

    private final AddressDao addressDao;
    private final OrderDAO orderDAO;
    private final UserDAO userDAO;
    private final OrderSignDAO orderSignDAO;
    private final OrderSignatureDAO orderSignatureDAO;

    public AccountController() {
        this(new AddressDao(), new OrderDAO(), new UserDAO(), new OrderSignDAO(), new OrderSignatureDAO());
    }

    AccountController(AddressDao addressDao, OrderDAO orderDAO, UserDAO userDAO,
                      OrderSignDAO orderSignDAO, OrderSignatureDAO orderSignatureDAO) {
        this.addressDao = addressDao;
        this.orderDAO = orderDAO;
        this.userDAO = userDAO;
        this.orderSignDAO = orderSignDAO;
        this.orderSignatureDAO = orderSignatureDAO;
    }

    // ── MY ACCOUNT (/my-account) ──

    @GetMapping("/my-account")
    public String myAccount(Model model, HttpServletRequest request, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:" + request.getContextPath() + "/login?redirect="
                    + request.getContextPath() + "/my-account";
        }

        List<Address> addressList = addressDao.getAddressesByUserId(user.getId());
        model.addAttribute("addressList", addressList);
        model.addAttribute("defaultAddress", addressDao.getDefaultAddressByUserId(user.getId()));
        model.addAttribute("countPending", orderDAO.countPendingOrdersByUserId(user.getId()));
        model.addAttribute("countCompleted", orderDAO.countCompletedOrdersByUserId(user.getId()));
        model.addAttribute("totalSpent", orderDAO.getTotalSpentByUserId(user.getId()));
        model.addAttribute("totalOrders", orderDAO.countOrdersByUserId(user.getId()));

        // Lấy thông tin đầy đủ từ DB (bao gồm createdAt)
        User dbUser = userDAO.getUserFullById(user.getId());
        if (dbUser != null) {
            model.addAttribute("memberSince", dbUser.getCreatedAt());
        }

        List<Order> recentOrders = orderDAO.getOrdersByUserId(user.getId());
        if (recentOrders.size() > 3) {
            recentOrders = recentOrders.subList(0, 3);
        }
        model.addAttribute("recentOrders", recentOrders);

        List<OrderSign> pendingSignatureOrders = orderSignDAO.findPendingByUserId(user.getId());
        model.addAttribute("pendingSignatureOrders", pendingSignatureOrders);
        List<OrderSignature> orderSignatures = orderSignatureDAO.findByUserId(user.getId());
        model.addAttribute("orderSignatures", orderSignatures);

        moveFlashAttribute(session, request, "success");
        moveFlashAttribute(session, request, "error");
        moveFlashAttribute(session, request, "pwSuccess");
        moveFlashAttribute(session, request, "pwError");
        return "pages/shop/my-account";
    }

    @PostMapping("/my-account")
    public String myAccountSubmit(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "fullname", required = false) String fullnameRaw,
            @RequestParam(value = "email", required = false) String emailRaw,
            @RequestParam(value = "phone", required = false) String phoneRaw,
            @RequestParam(value = "currentPassword", required = false) String currentPassword,
            @RequestParam(value = "newPassword", required = false) String newPassword,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            HttpServletRequest request,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:" + request.getContextPath() + "/login?redirect="
                    + request.getContextPath() + "/my-account";
        }

        String ctx = request.getContextPath();
        if ("changePassword".equals(action)) {
            handleChangePassword(session, user, currentPassword, newPassword, confirmPassword);
            return "redirect:" + ctx + "/my-account";
        }

        String fullname = fullnameRaw == null ? "" : fullnameRaw.trim();
        String email = emailRaw == null ? "" : emailRaw.trim().toLowerCase();
        String phone = ValidationUtil.normalizePhone(phoneRaw);

        if (fullname.isEmpty() || !fullname.matches("^[\\p{L}\\s]+$") || fullname.replaceAll("\\s", "").length() < 2) {
            session.setAttribute("error", "Họ tên không hợp lệ.");
            return "redirect:" + ctx + "/my-account";
        }

        if (!ValidationUtil.isValidEmail(email)) {
            session.setAttribute("error", "Email không hợp lệ.");
            return "redirect:" + ctx + "/my-account";
        }

        if (!phone.isEmpty() && !ValidationUtil.isValidPhone(phone)) {
            session.setAttribute("error", "Số điện thoại không hợp lệ.");
            return "redirect:" + ctx + "/my-account";
        }

        if (userDAO.isEmailTakenByAnotherUser(email, user.getId())) {
            session.setAttribute("error", "Email đã được tài khoản khác sử dụng.");
            return "redirect:" + ctx + "/my-account";
        }

        if (userDAO.isPhoneTakenByAnotherUser(phone, user.getId())) {
            session.setAttribute("error", "Số điện thoại đã được tài khoản khác sử dụng.");
            return "redirect:" + ctx + "/my-account";
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
        return "redirect:" + ctx + "/my-account";
    }

    private void handleChangePassword(HttpSession session, User user,
                                      String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword == null || newPassword == null || confirmPassword == null) {
            session.setAttribute("pwError", "Vui lòng điền đầy đủ thông tin.");
            return;
        }

        // Verify current password
        User dbUser = userDAO.getUserById(user.getId());
        if (dbUser == null || !PasswordUtil.verifyPassword(currentPassword, dbUser.getPassword())) {
            session.setAttribute("pwError", "Mật khẩu hiện tại không đúng.");
            return;
        }

        // New password must differ from current
        if (PasswordUtil.verifyPassword(newPassword, dbUser.getPassword())) {
            session.setAttribute("pwError", "Mật khẩu mới phải khác mật khẩu hiện tại.");
            return;
        }

        // Validate new password strength
        if (!isStrongPassword(newPassword)) {
            session.setAttribute("pwError", "Mật khẩu mới phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt.");
            return;
        }

        // Confirm match
        if (!newPassword.equals(confirmPassword)) {
            session.setAttribute("pwError", "Xác nhận mật khẩu không khớp.");
            return;
        }

        if (userDAO.updatePassword(user.getEmail(), newPassword)) {
            session.setAttribute("pwSuccess", "Đổi mật khẩu thành công.");
        } else {
            session.setAttribute("pwError", "Không thể đổi mật khẩu. Vui lòng thử lại.");
        }
    }

    private boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    private void moveFlashAttribute(HttpSession session, HttpServletRequest request, String attributeName) {
        Object value = session.getAttribute(attributeName);
        if (value != null) {
            request.setAttribute(attributeName, value);
            session.removeAttribute(attributeName);
        }
    }

    // ── ADDRESSES (/addresses) ──

    @GetMapping("/addresses")
    public String addressesGet(HttpServletRequest request) {
        return "redirect:" + resolveRedirectTarget(request);
    }

    @PostMapping("/addresses")
    public String addressesPost(
            @RequestParam(value = "_method", required = false) String method,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "id", required = false) String idRaw,
            @RequestParam(value = "addressDetail", required = false) String addressDetailRaw,
            @RequestParam(value = "province", required = false) String provinceRaw,
            @RequestParam(value = "district", required = false) String districtRaw,
            @RequestParam(value = "ward", required = false) String wardRaw,
            @RequestParam(value = "isDefault", required = false) String isDefaultRaw,
            @RequestParam(value = "source", required = false) String source,
            HttpServletRequest request,
            HttpSession session) {
        if ("put".equalsIgnoreCase(method)) {
            return handleUpdateAddress(idRaw, addressDetailRaw, provinceRaw, districtRaw, wardRaw,
                    isDefaultRaw, source, request, session);
        }
        if ("patch".equalsIgnoreCase(method) || "setDefault".equalsIgnoreCase(action)) {
            return handleSetDefault(idRaw, request, session);
        }
        if ("delete".equalsIgnoreCase(method)) {
            return handleDeleteAddress(idRaw, request, session);
        }
        return handleAddAddress(addressDetailRaw, provinceRaw, districtRaw, wardRaw,
                isDefaultRaw, source, request, session);
    }

    private String handleSetDefault(String idRaw, HttpServletRequest request, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:" + request.getContextPath() + "/login";
        }

        Integer addressId = ValidationUtil.parseIntOrNull(idRaw);
        if (addressId != null && !addressDao.setDefaultAddress(user.getId(), addressId)) {
            return redirectToCheckoutWithToast(request, session, "Không thể chọn địa chỉ giao hàng. Vui lòng thử lại.");
        }

        return "redirect:" + resolveRedirectTarget(request);
    }

    private String handleDeleteAddress(String idRaw, HttpServletRequest request, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:" + request.getContextPath() + "/login";
        }

        Integer addressId = ValidationUtil.parseIntOrNull(idRaw);
        if (addressId == null) {
            return "redirect:" + resolveRedirectTarget(request);
        }

        boolean wasDefault = addressDao.isDefaultAddress(addressId, user.getId());
        boolean deleted = addressDao.deleteAddress(addressId, user.getId());

        if (deleted && wasDefault && addressDao.hasAnyAddress(user.getId())) {
            addressDao.setNewestAddressAsDefault(user.getId());
        }

        return "redirect:" + resolveRedirectTarget(request);
    }

    private String handleAddAddress(String addressDetailRaw, String provinceRaw, String districtRaw,
                                    String wardRaw, String isDefaultRaw, String source,
                                    HttpServletRequest request, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:" + request.getContextPath() + "/login";
        }

        String addressDetail = ValidationUtil.normalizeAddressDetail(addressDetailRaw);
        String province = trimToEmpty(provinceRaw);
        String district = trimToEmpty(districtRaw);
        String ward = trimToEmpty(wardRaw);
        boolean isDefault = isDefaultRaw != null;
        if (shouldPromoteSavedAddress(source)) {
            isDefault = true;
        }

        if (addressDetail.isEmpty() || province.isEmpty() || district.isEmpty() || ward.isEmpty()) {
            return redirectToCheckoutWithToast(request, session, "Vui lòng nhập đầy đủ thông tin địa chỉ.");
        }

        String addressDetailError = ValidationUtil.validateAddressDetail(addressDetail);
        if (addressDetailError != null) {
            return redirectToCheckoutWithToast(request, session, addressDetailError);
        }

        if (!addressDao.hasAnyAddress(user.getId())) {
            isDefault = true;
        }

        boolean saved = addressDao.addAddress(
                user.getId(),
                isDefault,
                Timestamp.valueOf(LocalDateTime.now()),
                addressDetail,
                province,
                district,
                ward
        );
        if (!saved) {
            return redirectToCheckoutWithToast(request, session, "Không thể lưu địa chỉ. Vui lòng thử lại.");
        }

        return "redirect:" + resolveRedirectTarget(request);
    }

    private String handleUpdateAddress(String idRaw, String addressDetailRaw, String provinceRaw,
                                       String districtRaw, String wardRaw, String isDefaultRaw,
                                       String source, HttpServletRequest request, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:" + request.getContextPath() + "/login";
        }

        Integer id = ValidationUtil.parseIntOrNull(idRaw);
        String addressDetail = ValidationUtil.normalizeAddressDetail(addressDetailRaw);
        String province = trimToEmpty(provinceRaw);
        String district = trimToEmpty(districtRaw);
        String ward = trimToEmpty(wardRaw);
        boolean isDefault = isDefaultRaw != null;
        if (shouldPromoteSavedAddress(source)) {
            isDefault = true;
        }

        if (id == null || addressDetail.isEmpty() || province.isEmpty() || district.isEmpty() || ward.isEmpty()) {
            return redirectToCheckoutWithToast(request, session, "Vui lòng nhập đầy đủ thông tin địa chỉ.");
        }

        String addressDetailError = ValidationUtil.validateAddressDetail(addressDetail);
        if (addressDetailError != null) {
            return redirectToCheckoutWithToast(request, session, addressDetailError);
        }

        boolean updated = addressDao.updateAddress(
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
            return redirectToCheckoutWithToast(request, session, "Không thể cập nhật địa chỉ. Vui lòng thử lại.");
        }

        return "redirect:" + resolveRedirectTarget(request);
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean shouldPromoteSavedAddress(String source) {
        String s = source == null ? "" : source.trim();
        return "checkout".equalsIgnoreCase(s) || "account".equalsIgnoreCase(s);
    }

    private String redirectToCheckoutWithToast(HttpServletRequest request, HttpSession session, String message) {
        session.setAttribute("toastMessage", message);
        session.setAttribute("toastType", "warning");
        return "redirect:" + resolveRedirectTarget(request);
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

    // ── UPDATE PROFILE FROM CHECKOUT (/update-profile-checkout) ──

    @PostMapping("/update-profile-checkout")
    public String updateProfileCheckout(
            @RequestParam(value = "fullname", required = false) String fullnameRaw,
            @RequestParam(value = "phone", required = false) String phoneRaw,
            @RequestParam(value = "redirect", required = false) String redirect,
            HttpServletRequest request,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:" + request.getContextPath() + "/login";
        }

        String fullname = fullnameRaw == null ? "" : fullnameRaw.trim();
        String phone = ValidationUtil.normalizePhone(phoneRaw);

        if (fullname.isEmpty() || !fullname.matches("^[\\p{L}\\s]+$") || fullname.replaceAll("\\s", "").length() < 2) {
            session.setAttribute("toastMessage", "Họ tên không hợp lệ.");
            session.setAttribute("toastType", "warning");
            return "redirect:" + resolveProfileRedirect(request, redirect);
        }

        if (!phone.isEmpty() && !ValidationUtil.isValidPhone(phone)) {
            session.setAttribute("toastMessage", "Số điện thoại không hợp lệ.");
            session.setAttribute("toastType", "warning");
            return "redirect:" + resolveProfileRedirect(request, redirect);
        }

        userDAO.updateProfile(user.getId(), fullname, phone);

        user.setFullname(fullname);
        user.setPhone(phone);

        session.setAttribute("user", user);
        session.setAttribute("toastMessage", "Đã cập nhật thông tin nhận hàng.");
        session.setAttribute("toastType", "success");

        return "redirect:" + resolveProfileRedirect(request, redirect);
    }

    private String resolveProfileRedirect(HttpServletRequest request, String redirect) {
        if ("account".equalsIgnoreCase(redirect)) {
            return request.getContextPath() + "/my-account";
        }
        return request.getContextPath() + "/checkout";
    }
}
