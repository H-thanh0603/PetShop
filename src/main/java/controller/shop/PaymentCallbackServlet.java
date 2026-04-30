package controller.shop;

import DAO.CartDAO;
import DAO.UserDAO;
import Model.CartItem;
import Model.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import services.InventoryService;
import services.payment.BankTransferPaymentService;
import services.payment.MomoPaymentService;
import services.payment.PaymentTransactionService;
import services.payment.VnpayPaymentService;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@WebServlet({
        "/payment/momo/return",
        "/payment/momo/ipn",
        "/payment/bank/return",
        "/payment/bank/ipn",
        "/payment/vnpay/return",
        "/payment/vnpay/ipn"
})
public class PaymentCallbackServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private final PaymentTransactionService paymentTransactionService = new PaymentTransactionService();
    private final BankTransferPaymentService bankTransferPaymentService = new BankTransferPaymentService();
    private final MomoPaymentService momoPaymentService = new MomoPaymentService();
    private final VnpayPaymentService vnpayPaymentService = new VnpayPaymentService();
    private final UserDAO userDAO = new UserDAO();
    private final CartDAO cartDAO = new CartDAO();
    private final InventoryService inventoryService = new InventoryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleCallback(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleCallback(request, response);
    }

    private void handleCallback(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String servletPath = request.getServletPath();
        boolean momo = servletPath.contains("/momo/");
        boolean bank = servletPath.contains("/bank/");
        boolean ipn = servletPath.endsWith("/ipn");

        Map<String, String> params = momo && "POST".equalsIgnoreCase(request.getMethod())
                ? readJsonBody(request)
                : readRequestParams(request);

        if (params.isEmpty()) {
            if (ipn) {
                if (momo) {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    writeVnpayIpnResponse(response, "99", "Invalid request");
                }
            } else {
                handleReturnFailure(request, response, "Khong nhan duoc du lieu thanh toan.");
            }
            return;
        }

        boolean validSignature;
        if (momo) {
            validSignature = momoPaymentService.validateCallbackSignature(params);
        } else if (bank) {
            validSignature = bankTransferPaymentService.validateCallbackSignature(params);
        } else {
            validSignature = vnpayPaymentService.validateSignature(params);
        }

        if (!validSignature) {
            if (ipn) {
                if (momo) {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    writeVnpayIpnResponse(response, "97", "Invalid Signature");
                }
            } else {
                handleReturnFailure(request, response, "Chu ky thanh toan khong hop le.");
            }
            return;
        }

        String providerOrderId;
        String resultCode;
        String message;
        String paymentToken;
        String providerTransactionId;
        boolean success;

        if (momo) {
            providerOrderId = momoPaymentService.getProviderOrderId(params);
            resultCode = momoPaymentService.getResultCode(params);
            message = momoPaymentService.getMessage(params);
            paymentToken = momoPaymentService.getPaymentToken(params);
            providerTransactionId = momoPaymentService.getProviderTransactionId(params);
            success = momoPaymentService.isSuccess(params);
        } else if (bank) {
            providerOrderId = bankTransferPaymentService.getProviderOrderId(params);
            resultCode = bankTransferPaymentService.getResultCode(params);
            message = bankTransferPaymentService.getMessage(params);
            paymentToken = bankTransferPaymentService.getPaymentToken(params);
            providerTransactionId = bankTransferPaymentService.getProviderTransactionId(params);
            success = bankTransferPaymentService.isSuccess(params);
        } else {
            providerOrderId = vnpayPaymentService.getProviderOrderId(params);
            resultCode = vnpayPaymentService.getResultCode(params);
            message = vnpayPaymentService.getMessage(params);
            paymentToken = vnpayPaymentService.getPaymentToken(params);
            providerTransactionId = vnpayPaymentService.getProviderTransactionId(params);
            success = vnpayPaymentService.isSuccess(params);
        }
        String rawPayload = gson.toJson(params);

        PaymentTransactionService.FinalizeResult finalizeResult = success
                ? paymentTransactionService.confirmSuccess(
                        providerOrderId,
                        paymentToken,
                        providerTransactionId,
                        resultCode,
                        message,
                        rawPayload
                )
                : paymentTransactionService.failTransaction(
                        providerOrderId,
                        inferFailureStatus(momo, bank, params),
                        resultCode,
                        message,
                        providerTransactionId,
                        rawPayload
                );

        if (ipn) {
            if (momo) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else {
                writeVnpayIpnResponse(
                        response,
                        finalizeResult.isSuccess() ? "00" : "99",
                        finalizeResult.isSuccess() ? "Confirm Success" : "Processing Error"
                );
            }
            return;
        }

        if (!finalizeResult.isSuccess()) {
            handleReturnFailure(request, response, finalizeResult.getMessage());
            return;
        }

        if (success) {
            handleReturnSuccess(request, response, finalizeResult, paymentToken);
            return;
        }

        handleReturnCancelled(request, response, finalizeResult, message);
    }

    private Map<String, String> readRequestParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            params.put(name, request.getParameter(name));
        }
        return params;
    }

    private Map<String, String> readJsonBody(HttpServletRequest request) throws IOException {
        JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);
        if (jsonObject == null) {
            return Collections.emptyMap();
        }

        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, com.google.gson.JsonElement> entry : jsonObject.entrySet()) {
            params.put(entry.getKey(), entry.getValue().isJsonNull() ? "" : entry.getValue().getAsString());
        }
        return params;
    }

    private String inferFailureStatus(boolean momo, boolean bank, Map<String, String> params) {
        if (bank && bankTransferPaymentService.isCancelled(params)) {
            return "CANCELLED";
        }
        if (!momo && !bank && vnpayPaymentService.isCancelled(params)) {
            return "CANCELLED";
        }

        String message = momo
                ? momoPaymentService.getMessage(params).toLowerCase()
                : bank
                ? bankTransferPaymentService.getMessage(params).toLowerCase()
                : vnpayPaymentService.getMessage(params).toLowerCase();
        if (message.contains("cancel")) {
            return "CANCELLED";
        }
        return "FAILED";
    }

    private void handleReturnSuccess(HttpServletRequest request, HttpServletResponse response,
                                     PaymentTransactionService.FinalizeResult finalizeResult,
                                     String paymentToken) throws IOException {
        refreshSessionState(request.getSession(), finalizeResult.getUserId(), true);
        HttpSession session = request.getSession();
        session.setAttribute("toastMessage", "Thanh toan thanh cong. Ma giao dich: " + paymentToken);
        session.setAttribute("toastType", "success");
        response.sendRedirect(request.getContextPath() + "/my-orders?action=view&id=" + finalizeResult.getOrderId());
    }

    private void handleReturnCancelled(HttpServletRequest request, HttpServletResponse response,
                                       PaymentTransactionService.FinalizeResult finalizeResult,
                                       String message) throws IOException {
        refreshSessionState(request.getSession(), finalizeResult.getUserId(), false);
        HttpSession session = request.getSession();
        session.setAttribute("toastMessage", message);
        session.setAttribute("toastType", "warning");
        response.sendRedirect(request.getContextPath() + "/checkout");
    }

    private void handleReturnFailure(HttpServletRequest request, HttpServletResponse response,
                                     String message) throws IOException {
        HttpSession session = request.getSession();
        session.setAttribute("toastMessage", message);
        session.setAttribute("toastType", "error");
        response.sendRedirect(request.getContextPath() + "/checkout");
    }

    private void refreshSessionState(HttpSession session, Integer userId, boolean clearCheckoutState) {
        if (userId == null) {
            return;
        }

        User latestUser = userDAO.getUserById(userId);
        if (latestUser != null) {
            session.setAttribute("user", latestUser);
            Map<Integer, CartItem> cart = cartDAO.getCartByUserId(userId);
            inventoryService.refreshCartProducts(cart);
            session.setAttribute("cart", cart);
            int totalQuantity = 0;
            for (CartItem item : cart.values()) {
                totalQuantity += item.getQuantity();
            }
            session.setAttribute("totalQuantity", totalQuantity);
        }

        if (clearCheckoutState) {
            session.removeAttribute("appliedCoupon");
            session.removeAttribute("couponMessage");
            session.removeAttribute("checkoutNote");
            session.removeAttribute("checkoutPaymentMethod");
        }
    }

    private void writeVnpayIpnResponse(HttpServletResponse response, String rspCode, String message)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> data = new HashMap<>();
        data.put("RspCode", rspCode);
        data.put("Message", message);
        response.getWriter().write(gson.toJson(data));
    }
}
