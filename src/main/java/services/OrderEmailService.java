package services;

import Model.Order;
import Model.OrderItem;
import Util.EmailUtil;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Sends order confirmation emails asynchronously after checkout.
 */
public class OrderEmailService {

    private static final NumberFormat VND = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    /**
     * Send order confirmation email asynchronously.
     * Failures are logged but do NOT affect the order transaction.
     */
    public void sendOrderConfirmationAsync(String toEmail, Order order, List<OrderItem> items) {
        String subject = "Xác nhận đơn hàng #" + order.getId() + " - PetShop";
        String html = buildConfirmationHtml(order, items);
        EmailUtil.sendHtmlEmailAsync(toEmail, subject, html);
    }

    private String buildConfirmationHtml(Order order, List<OrderItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'></head><body>");
        sb.append("<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px;'>");
        sb.append("<h2 style='color:#e67e22;'>🐾 PetShop - Xác nhận đơn hàng</h2>");
        sb.append("<p>Xin chào <strong>").append(escHtml(order.getFullname())).append("</strong>,</p>");
        sb.append("<p>Đơn hàng <strong>#").append(order.getId()).append("</strong> của bạn đã được đặt thành công!</p>");

        // Items table
        sb.append("<table style='width:100%;border-collapse:collapse;margin:16px 0;'>");
        sb.append("<thead><tr style='background:#f5f5f5;'>");
        sb.append("<th style='padding:8px;text-align:left;border:1px solid #ddd;'>Sản phẩm</th>");
        sb.append("<th style='padding:8px;text-align:center;border:1px solid #ddd;'>Số lượng</th>");
        sb.append("<th style='padding:8px;text-align:right;border:1px solid #ddd;'>Đơn giá</th>");
        sb.append("</tr></thead><tbody>");

        if (items != null) {
            for (OrderItem item : items) {
                String name = item.getProduct() != null ? item.getProduct().getName() : "Sản phẩm #" + item.getProductId();
                sb.append("<tr>");
                sb.append("<td style='padding:8px;border:1px solid #ddd;'>").append(escHtml(name)).append("</td>");
                sb.append("<td style='padding:8px;text-align:center;border:1px solid #ddd;'>").append(item.getQuantity()).append("</td>");
                sb.append("<td style='padding:8px;text-align:right;border:1px solid #ddd;'>").append(VND.format(item.getPrice())).append(" ₫</td>");
                sb.append("</tr>");
            }
        }
        sb.append("</tbody></table>");

        // Summary
        sb.append("<p><strong>Tổng thanh toán: ").append(VND.format(order.getTotalAmount())).append(" ₫</strong></p>");
        sb.append("<p><strong>Địa chỉ giao hàng:</strong> ").append(escHtml(order.getAddress())).append("</p>");
        sb.append("<hr style='border:none;border-top:1px solid #eee;margin:20px 0;'>");
        sb.append("<p style='color:#888;font-size:12px;'>Cảm ơn bạn đã mua sắm tại PetShop! 🐾</p>");
        sb.append("</div></body></html>");
        return sb.toString();
    }

    private String escHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
