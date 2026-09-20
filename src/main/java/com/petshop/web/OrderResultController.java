package com.petshop.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

/**
 * Replaces OrderSuccessServlet (/order-success) 1:1.
 * Reads flash data from session, moves to model, clears session —
 * preventing the page from re-rendering on refresh (F5).
 */
@Controller
public class OrderResultController {

    private static final String[] COPY_ATTRS = {
            "successOrderId", "successUser", "successTotalAmount", "successShippingFee",
            "successDiscount", "successFinalTotal", "successShippingAddress", "successOrderNote",
            "successOrderItems", "orderHash", "privateKeyBase64", "toolUrl", "showSignatureModal",
            "paymentMethod", "transferReference", "paymentExpiresAt", "bankId", "bankDisplayName",
            "bankAccountNumber", "bankAccountName", "paymentTtlSeconds"
    };

    @GetMapping("/order-success")
    public String orderSuccess(Model model, HttpSession session) {
        Integer orderId = (Integer) session.getAttribute("successOrderId");
        if (orderId == null) {
            return "redirect:/shop";
        }

        model.addAttribute("orderId", orderId);
        model.addAttribute("user", session.getAttribute("successUser"));
        model.addAttribute("totalAmount", session.getAttribute("successTotalAmount"));
        model.addAttribute("shippingFee", session.getAttribute("successShippingFee"));
        model.addAttribute("discount", session.getAttribute("successDiscount"));
        model.addAttribute("finalTotal", session.getAttribute("successFinalTotal"));
        model.addAttribute("shippingAddress", session.getAttribute("successShippingAddress"));
        model.addAttribute("orderNote", session.getAttribute("successOrderNote"));
        model.addAttribute("orderItems", session.getAttribute("successOrderItems"));
        model.addAttribute("paymentMethod", session.getAttribute("paymentMethod"));
        Object pending = session.getAttribute("pendingVerification");
        model.addAttribute("pendingVerification", pending != null && (Boolean) pending);
        model.addAttribute("transferReference", session.getAttribute("transferReference"));
        model.addAttribute("paymentExpiresAt", session.getAttribute("paymentExpiresAt"));
        model.addAttribute("bankId", session.getAttribute("bankId"));
        model.addAttribute("bankDisplayName", session.getAttribute("bankDisplayName"));
        model.addAttribute("bankAccountNumber", session.getAttribute("bankAccountNumber"));
        model.addAttribute("bankAccountName", session.getAttribute("bankAccountName"));
        model.addAttribute("paymentTtlSeconds", session.getAttribute("paymentTtlSeconds"));

        model.addAttribute("orderHash", session.getAttribute("orderHash"));
        model.addAttribute("privateKeyBase64", session.getAttribute("privateKeyBase64"));
        model.addAttribute("toolUrl", session.getAttribute("toolUrl"));
        model.addAttribute("showSignatureModal", session.getAttribute("showSignatureModal"));

        for (String attr : COPY_ATTRS) {
            session.removeAttribute(attr);
        }
        session.removeAttribute("pendingVerification");

        return "pages/shop/orderSuccess";
    }
}
