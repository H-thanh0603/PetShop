package com.petshop.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import DAO.OrderDAO;
import Model.CustomerRepurchaseSuggestion;
import Model.Order;
import Model.OrderStatus;
import Model.User;
import Util.VnpayUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import services.ReorderService;

/**
 * Replaces MyOrdersServlet (/my-orders) 1:1 — same views, same session
 * toast attributes, same cancel/reorder/confirmReceipt/repay flows.
 */
@Controller
public class MyOrdersController {

    private final OrderDAO orderDAO;
    private final ReorderService reorderService;

    public MyOrdersController() {
        this(new OrderDAO(), new ReorderService());
    }

    MyOrdersController(OrderDAO orderDAO, ReorderService reorderService) {
        this.orderDAO = orderDAO;
        this.reorderService = reorderService;
    }

    @GetMapping("/my-orders")
    public String myOrders(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "id", required = false) String idRaw,
            @RequestParam(value = "status", required = false) String statusFilter,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if ("view".equals(action)) {
            int orderId;
            try {
                orderId = Integer.parseInt(idRaw);
            } catch (NumberFormatException e) {
                session.setAttribute("error", "Mã đơn hàng không hợp lệ.");
                return "redirect:/my-orders";
            }
            Order order = orderDAO.getOrderById(orderId);

            // Bảo mật: Chỉ cho phép xem nếu đơn hàng thuộc về user đang đăng nhập
            if (order != null && order.getUserId() == user.getId()) {
                model.addAttribute("order", order);
                return "pages/shop/order-detail";
            }
            return "redirect:/my-orders";
        }

        int countPending = orderDAO.countPendingOrdersByUserId(user.getId());
        int countCompleted = orderDAO.countCompletedOrdersByUserId(user.getId());
        orderDAO.autoCompleteDeliveredOrders();
        List<Order> allOrders = orderDAO.getOrdersByUserId(user.getId());
        List<Order> list = filterOrders(allOrders, statusFilter, keyword);
        List<CustomerRepurchaseSuggestion> repurchaseSuggestions =
                orderDAO.getRepurchaseSuggestions(user.getId(), 30, 5);
        model.addAttribute("countPending", countPending);
        model.addAttribute("countCompleted", countCompleted);
        model.addAttribute("orders", list);
        model.addAttribute("repurchaseSuggestions", repurchaseSuggestions);
        model.addAttribute("totalOrders", allOrders.size());
        model.addAttribute("selectedStatus", statusFilter);
        model.addAttribute("keyword", keyword);
        return "pages/shop/my-orders";
    }

    @PostMapping("/my-orders")
    public String myOrdersPost(
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "orderId", required = false) String orderIdRaw,
            HttpServletRequest request,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if ("cancel".equals(action)) {
            try {
                int orderId = Integer.parseInt(orderIdRaw);
                // Check cancellation window first for a clear error message
                if (!orderDAO.isWithinCancellationWindow(orderId)) {
                    session.setAttribute("toastMessage", "Đã quá thời gian hủy đơn hàng (1 giờ kể từ khi đặt).");
                    session.setAttribute("toastType", "warning");
                } else if (orderDAO.cancelOrderByUser(orderId, user.getId())) {
                    session.setAttribute("toastMessage", "Đơn hàng đã được hủy thành công.");
                    session.setAttribute("toastType", "success");
                } else {
                    session.setAttribute("toastMessage", "Không thể hủy đơn hàng này. Có thể trạng thái đơn hàng đã thay đổi.");
                    session.setAttribute("toastType", "error");
                }
            } catch (Exception e) {
                session.setAttribute("toastMessage", "Có lỗi xảy ra khi hủy đơn hàng.");
                session.setAttribute("toastType", "error");
            }
        } else if ("reorder".equals(action)) {
            try {
                int orderId = Integer.parseInt(orderIdRaw);
                if (reorderService.reorderToCart(user.getId(), orderId)) {
                    session.setAttribute("toastMessage", "Đã thêm lại sản phẩm từ đơn cũ vào giỏ hàng.");
                    session.setAttribute("toastType", "success");
                    return "redirect:/cart";
                }
                session.setAttribute("toastMessage", "Không thể mua lại đơn hàng này.");
                session.setAttribute("toastType", "error");
            } catch (Exception e) {
                session.setAttribute("toastMessage", "Có lỗi xảy ra khi mua lại đơn hàng.");
                session.setAttribute("toastType", "error");
            }
        } else if ("confirmReceipt".equals(action)) {
            try {
                int orderId = Integer.parseInt(orderIdRaw);
                Order order = orderDAO.getOrderById(orderId);
                if (order != null && order.getUserId() == user.getId()) {
                    OrderStatus currentStatus = OrderStatus.fromString(order.getStatus());
                    if (currentStatus == OrderStatus.DELIVERED) {
                        if (orderDAO.updateStatus(orderId, OrderStatus.COMPLETED.getDisplayName(), user.getId())) {
                            session.setAttribute("toastMessage", "Cảm ơn bạn đã xác nhận. Đơn hàng đã được hoàn tất.");
                            session.setAttribute("toastType", "success");
                        } else {
                            session.setAttribute("toastMessage", "Không thể xác nhận đơn hàng này. Vui lòng thử lại sau.");
                            session.setAttribute("toastType", "error");
                        }
                    } else if (currentStatus == OrderStatus.COMPLETED) {
                        session.setAttribute("toastMessage", "Đơn hàng này đã được hoàn thành trước đó.");
                        session.setAttribute("toastType", "info");
                    } else {
                        session.setAttribute("toastMessage", "Hành động không hợp lệ. Đơn hàng chưa ở trạng thái 'Đã giao hàng'.");
                        session.setAttribute("toastType", "warning");
                    }
                } else {
                    session.setAttribute("toastMessage", "Không tìm thấy đơn hàng hoặc bạn không có quyền thực hiện hành động này.");
                    session.setAttribute("toastType", "error");
                }
            } catch (Exception e) {
                session.setAttribute("toastMessage", "Có lỗi xảy ra khi xác nhận đơn hàng.");
                session.setAttribute("toastType", "error");
            }
        } else if ("repay".equals(action)) {
            try {
                int orderId = Integer.parseInt(orderIdRaw);
                Order order = orderDAO.getOrderById(orderId);

                // Bảo mật + trạng thái: chỉ chủ đơn mới được thanh toán lại,
                // và đơn phải còn trong điều kiện thanh toán lại (chưa trả, còn hạn).
                if (order == null || order.getUserId() != user.getId() || !order.isRepayable()) {
                    session.setAttribute("toastMessage", "Đơn hàng không thể thanh toán lại.");
                    session.setAttribute("toastType", "error");
                    return "redirect:/my-orders";
                }

                // Nạp lại dữ liệu cho trang order-success (đọc từ session).
                session.setAttribute("paymentMethod", "VNPAY");
                session.setAttribute("paymentStatus", 0);
                session.setAttribute("successOrderId", orderId);
                session.setAttribute("successUser", user);
                session.setAttribute("successTotalAmount", order.getTotalAmount());
                session.setAttribute("successFinalTotal", order.getTotalAmount());
                session.setAttribute("successShippingFee", 0);
                session.setAttribute("successDiscount", 0);
                session.setAttribute("successShippingAddress", order.getAddress());
                session.setAttribute("successOrderNote", order.getNote());
                session.setAttribute("successOrderItems", order.getItems());

                // Tái sử dụng đúng luồng VNPAY hiện có cho chính đơn hàng này.
                String vnpayUrl = VnpayUtil.createPaymentUrl(request, orderId, order.getTotalAmount());
                return "redirect:" + vnpayUrl;
            } catch (Exception e) {
                session.setAttribute("error", "Có lỗi xảy ra khi thanh toán lại đơn hàng.");
            }
        }
        return "redirect:/my-orders";
    }

    private List<Order> filterOrders(List<Order> orders, String statusFilter, String keyword) {
        List<Order> filtered = new ArrayList<>();
        String normalizedStatus = statusFilter == null ? "" : statusFilter.trim();
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase();

        for (Order order : orders) {
            boolean matchesStatus = normalizedStatus.isEmpty() || "all".equalsIgnoreCase(normalizedStatus)
                    || order.getStatus().equalsIgnoreCase(normalizedStatus);
            boolean matchesKeyword = normalizedKeyword.isEmpty()
                    || String.valueOf(order.getId()).contains(normalizedKeyword)
                    || (order.getCustomerFullname() != null && order.getCustomerFullname().toLowerCase().contains(normalizedKeyword))
                    || (order.getFullname() != null && order.getFullname().toLowerCase().contains(normalizedKeyword))
                    || (order.getPhone() != null && order.getPhone().toLowerCase().contains(normalizedKeyword))
                    || (order.getAddress() != null && order.getAddress().toLowerCase().contains(normalizedKeyword));
            if (matchesStatus && matchesKeyword) {
                filtered.add(order);
            }
        }
        return filtered;
    }
}
