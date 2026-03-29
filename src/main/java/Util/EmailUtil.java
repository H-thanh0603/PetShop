package Util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class để gửi email
 * Hỗ trợ gửi email đồng bộ và bất đồng bộ
 */
public class EmailUtil {
    
    // Thread pool để gửi email bất đồng bộ
    private static final ExecutorService emailExecutor = Executors.newFixedThreadPool(3);
    
    /**
     * Gửi email đồng bộ (chờ đến khi gửi xong)
     */
    public static boolean sendEmail(String toEmail, String subject, String htmlContent) {
        try {
            // Cấu hình SMTP properties
            Properties props = new Properties();props.put("mail.smtp.host", EmailConfig.getSmtpHost());
            props.put("mail.smtp.port", EmailConfig.getSmtpPort());
            props.put("mail.smtp.auth", String.valueOf(EmailConfig.isSmtpAuth()));
            props.put("mail.smtp.starttls.enable", String.valueOf(EmailConfig.isSmtpStarttls()));
            
            // Tạo session với authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                        EmailConfig.getSmtpEmail(), 
                        EmailConfig.getSmtpPassword()
                    );
                }
            });
            
            // Tạo message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EmailConfig.getSmtpEmail(), EmailConfig.getSenderName(), "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(MimeUtility.encodeText(subject, "UTF-8", "B"));
            message.setContent(htmlContent, "text/html; charset=UTF-8");
            
            // Gửi email
            Transport.send(message);
            System.out.println("Email sent successfully to: " + toEmail);
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to send email to: " + toEmail);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Gửi email bất đồng bộ (không chờ, chạy background)
     * Sử dụng khi không cần biết kết quả ngay
     */
    public static void sendEmailAsync(String toEmail, String subject, String htmlContent) {
        emailExecutor.submit(() -> sendEmail(toEmail, subject, htmlContent));
    }

    
    // ============ EMAIL TEMPLATES CHO PET VACCINE ============
    
    /**
     * Gửi email xác nhận đơn hàng
     */
    public static void sendOrderConfirmation(String toEmail, String customerName,
            int orderId, String orderDetails, double totalAmount) {
        
        String subject = "Xác nhận đơn hàng #" + orderId + " - PetShop";
        String htmlContent = buildOrderConfirmationEmail(customerName, orderId, orderDetails, totalAmount);
        sendEmailAsync(toEmail, subject, htmlContent);
    }
    
    /**
     * Gửi email cập nhật trạng thái đơn hàng
     */
    public static void sendOrderStatusUpdate(String toEmail, String customerName,
            int orderId, String newStatus) {
        
        String subject = "Cập nhật đơn hàng #" + orderId + " - PetShop";
        String htmlContent = buildOrderStatusEmail(customerName, orderId, newStatus);
        sendEmailAsync(toEmail, subject, htmlContent);
    }
    
    /**
     * Gửi email khuyến mãi
     */
    public static void sendPromotionEmail(String toEmail, String customerName,
            String promoTitle, String promoContent, String couponCode) {
        
        String subject = promoTitle + " - PetShop";
        String htmlContent = buildPromotionEmail(customerName, promoTitle, promoContent, couponCode);
        sendEmailAsync(toEmail, subject, htmlContent);
    }
    
    /**
     * Gửi email xác nhận đặt lịch hẹn
     */
    public static void sendBookingConfirmation(String toEmail, String customerName, 
            String petName, String serviceName, String appointmentDate, String appointmentTime) {
        
        String subject = "Xác nhận đặt lịch hẹn - PetVaccine";
        String htmlContent = buildBookingConfirmationEmail(customerName, petName, 
                serviceName, appointmentDate, appointmentTime);
        
        sendEmailAsync(toEmail, subject, htmlContent);
    }
    
    /**
     * Gửi email nhắc nhở lịch hẹn
     */
    public static void sendAppointmentReminder(String toEmail, String customerName,
            String petName, String serviceName, String appointmentDate, String appointmentTime) {
        
        String subject = "Nhắc nhở lịch hẹn ngày mai - PetVaccine";
        String htmlContent = buildReminderEmail(customerName, petName, 
                serviceName, appointmentDate, appointmentTime);
        
        sendEmailAsync(toEmail, subject, htmlContent);
    }
    
    /**
     * Gửi email thông báo hủy lịch hẹn
     */
    public static void sendCancellationNotification(String toEmail, String customerName,
            String petName, String serviceName, String appointmentDate, String reason) {
        
        String subject = "Thông báo hủy lịch hẹn - PetVaccine";
        String htmlContent = buildCancellationEmail(customerName, petName, 
                serviceName, appointmentDate, reason);
        
        sendEmailAsync(toEmail, subject, htmlContent);
    }
    
    /**
     * Gửi email thông báo lịch hẹn đã được duyệt
     */
    public static void sendAppointmentApproved(String toEmail, String customerName,
            String petName, String serviceName, String appointmentDate, String doctorName) {
        
        String subject = "✅ Lịch hẹn đã được xác nhận - PetVaccine";
        String htmlContent = buildApprovalEmail(customerName, petName, 
                serviceName, appointmentDate, doctorName);
        
        sendEmailAsync(toEmail, subject, htmlContent);
    }
    
    /**
     * Gửi email thông báo lịch hẹn bị từ chối
     */
    public static void sendAppointmentRejected(String toEmail, String customerName,
            String petName, String serviceName, String appointmentDate, String reason) {
        
        String subject = "❌ Lịch hẹn không được duyệt - PetVaccine";
        String htmlContent = buildRejectionEmail(customerName, petName, 
                serviceName, appointmentDate, reason);
        
        sendEmailAsync(toEmail, subject, htmlContent);
    }
    
    // ============ HTML EMAIL TEMPLATES ============
    
    private static String buildOrderConfirmationEmail(String customerName, int orderId,
            String orderDetails, double totalAmount) {
        return "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'></head>" +
            "<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
            "<div style='background: linear-gradient(135deg, #00bfa5 0%, #008f7a 100%); padding: 30px; text-align: center;'>" +
            "  <h1 style='color: white; margin: 0;'>🐾 PetShop</h1>" +
            "  <p style='color: rgba(255,255,255,0.9); margin: 5px 0 0;'>Xác nhận đơn hàng</p>" +
            "</div>" +
            "<div style='padding: 30px; background: #f9f9f9;'>" +
            "  <h2 style='color: #333;'>Đơn hàng #" + orderId + " đã được tiếp nhận!</h2>" +
            "  <p>Xin chào <strong>" + customerName + "</strong>,</p>" +
            "  <p>Cảm ơn bạn đã mua hàng tại PetShop!</p>" +
            "  <div style='background: white; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
            "    <h3 style='margin-top: 0;'>Chi tiết đơn hàng:</h3>" +
            "    <pre style='font-family: Arial; white-space: pre-wrap;'>" + orderDetails + "</pre>" +
            "    <hr style='border: 1px solid #eee;'>" +
            "    <p style='font-size: 18px;'><strong>Tổng cộng: " + String.format("%,.0f", totalAmount) + "đ</strong></p>" +
            "  </div>" +
            "  <p>Chúng tôi sẽ liên hệ với bạn sớm nhất để xác nhận giao hàng.</p>" +
            "  <p style='color: #666;'>Trân trọng,<br>PetShop Team</p>" +
            "</div></body></html>";
    }
    
    private static String buildOrderStatusEmail(String customerName, int orderId, String newStatus) {
        String statusColor = "#333";
        String statusIcon = "📦";
        switch (newStatus) {
            case "Confirmed": statusColor = "#2196F3"; statusIcon = "✅"; break;
            case "Shipping": statusColor = "#FF9800"; statusIcon = "🚚"; break;
            case "Delivered": statusColor = "#4CAF50"; statusIcon = "🎉"; break;
            case "Cancelled": statusColor = "#F44336"; statusIcon = "❌"; break;
        }
        return "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'></head>" +
            "<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
            "<div style='background: linear-gradient(135deg, #00bfa5 0%, #008f7a 100%); padding: 30px; text-align: center;'>" +
            "  <h1 style='color: white; margin: 0;'>🐾 PetShop</h1>" +
            "</div>" +
            "<div style='padding: 30px; background: #f9f9f9;'>" +
            "  <p>Xin chào <strong>" + customerName + "</strong>,</p>" +
            "  <p>Đơn hàng <strong>#" + orderId + "</strong> đã được cập nhật:</p>" +
            "  <div style='background: white; padding: 20px; border-radius: 8px; margin: 20px 0; text-align: center;'>" +
            "    <p style='font-size: 40px; margin: 0;'>" + statusIcon + "</p>" +
            "    <p style='font-size: 20px; color: " + statusColor + "; font-weight: bold;'>" + newStatus + "</p>" +
            "  </div>" +
            "  <p style='color: #666;'>Trân trọng,<br>PetShop Team</p>" +
            "</div></body></html>";
    }
    
    private static String buildPromotionEmail(String customerName, String promoTitle,
            String promoContent, String couponCode) {
        return "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'></head>" +
            "<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
            "<div style='background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%); padding: 30px; text-align: center;'>" +
            "  <h1 style='color: white; margin: 0;'>🎉 " + promoTitle + "</h1>" +
            "</div>" +
            "<div style='padding: 30px; background: #f9f9f9;'>" +
            "  <p>Xin chào <strong>" + customerName + "</strong>,</p>" +
            "  <p>" + promoContent + "</p>" +
            (couponCode != null ? 
            "  <div style='background: #fff3e0; padding: 20px; border-radius: 8px; margin: 20px 0; text-align: center;'>" +
            "    <p style='margin: 0; color: #e65100;'>Mã giảm giá của bạn:</p>" +
            "    <p style='font-size: 28px; font-weight: bold; color: #FF6B6B; letter-spacing: 3px; margin: 10px 0;'>" + couponCode + "</p>" +
            "  </div>" : "") +
            "  <p style='color: #666;'>Trân trọng,<br>PetShop Team</p>" +
            "</div></body></html>";
    }
    
    private static String buildBookingConfirmationEmail(String customerName, String petName,
            String serviceName, String appointmentDate, String appointmentTime) {
        
        return "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'></head>" +
            "<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
            "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center;'>" +
            "  <h1 style='color: white; margin: 0;'>🐾 PetVaccine</h1>" +
            "</div>" +
            "<div style='padding: 30px; background: #f9f9f9;'>" +
            "  <h2 style='color: #333;'>Xác nhận đặt lịch thành công!</h2>" +
            "  <p>Xin chào <strong>" + customerName + "</strong>,</p>" +
            "  <p>Cảm ơn bạn đã đặt lịch hẹn tại PetVaccine. Dưới đây là thông tin chi tiết:</p>" +
            "  <div style='background: white; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
            "    <p><strong>🐕 Thú cưng:</strong> " + petName + "</p>" +
            "    <p><strong>💉 Dịch vụ:</strong> " + serviceName + "</p>" +
            "    <p><strong>📅 Ngày hẹn:</strong> " + appointmentDate + "</p>" +
            "    <p><strong>⏰ Giờ hẹn:</strong> " + appointmentTime + "</p>" +
            "  </div>" +
            "  <p>Vui lòng đến đúng giờ. Nếu cần thay đổi, hãy liên hệ với chúng tôi.</p>" +
            "  <p style='color: #666;'>Trân trọng,<br>Đội ngũ PetVaccine</p>" +
            "</div>" +
            "</body></html>";
    }
    
    private static String buildReminderEmail(String customerName, String petName,
            String serviceName, String appointmentDate, String appointmentTime) {
        
        return "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'></head>" +
            "<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
            "<div style='background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); padding: 30px; text-align: center;'>" +
            "  <h1 style='color: white; margin: 0;'>⏰ Nhắc nhở lịch hẹn</h1>" +
            "</div>" +
            "<div style='padding: 30px; background: #f9f9f9;'>" +
            "  <h2 style='color: #333;'>Đừng quên lịch hẹn ngày mai!</h2>" +
            "  <p>Xin chào <strong>" + customerName + "</strong>,</p>" +
            "  <p>Đây là email nhắc nhở về lịch hẹn của bạn vào ngày mai:</p>" +
            "  <div style='background: white; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
            "    <p><strong>🐕 Thú cưng:</strong> " + petName + "</p>" +
            "    <p><strong>💉 Dịch vụ:</strong> " + serviceName + "</p>" +
            "    <p><strong>📅 Ngày hẹn:</strong> " + appointmentDate + "</p>" +
            "    <p><strong>⏰ Giờ hẹn:</strong> " + appointmentTime + "</p>" +
            "  </div>" +
            "  <p>Vui lòng đến đúng giờ để được phục vụ tốt nhất.</p>" +
            "  <p style='color: #666;'>Trân trọng,<br>Đội ngũ PetVaccine</p>" +
            "</div>" +
            "</body></html>";
    }
    
    private static String buildCancellationEmail(String customerName, String petName,
            String serviceName, String appointmentDate, String reason) {
        
        return "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'></head>" +
            "<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
            "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center;'>" +
            "  <h1 style='color: white; margin: 0;'>🐾 PetVaccine</h1>" +
            "</div>" +
            "<div style='padding: 30px; background: #f9f9f9;'>" +
            "  <h2 style='color: #e74c3c;'>Thông báo hủy lịch hẹn</h2>" +
            "  <p>Xin chào <strong>" + customerName + "</strong>,</p>" +
            "  <p>Lịch hẹn sau đây đã được hủy:</p>" +
            "  <div style='background: white; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
            "    <p><strong>🐕 Thú cưng:</strong> " + petName + "</p>" +
            "    <p><strong>💉 Dịch vụ:</strong> " + serviceName + "</p>" +
            "    <p><strong>📅 Ngày hẹn:</strong> " + appointmentDate + "</p>" +
            "    <p><strong>📝 Lý do:</strong> " + (reason != null ? reason : "Không có") + "</p>" +
            "  </div>" +
            "  <p>Nếu bạn muốn đặt lịch mới, vui lòng truy cập website của chúng tôi.</p>" +
            "  <p style='color: #666;'>Trân trọng,<br>Đội ngũ PetVaccine</p>" +
            "</div>" +
            "</body></html>";
    }
    
    private static String buildApprovalEmail(String customerName, String petName,
            String serviceName, String appointmentDate, String doctorName) {
        
        return "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'></head>" +
            "<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
            "<div style='background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); padding: 30px; text-align: center;'>" +
            "  <h1 style='color: white; margin: 0;'>✅ Lịch hẹn đã được xác nhận!</h1>" +
            "</div>" +
            "<div style='padding: 30px; background: #f9f9f9;'>" +
            "  <p>Xin chào <strong>" + customerName + "</strong>,</p>" +
            "  <p>Chúng tôi vui mừng thông báo lịch hẹn của bạn đã được <strong style='color: #11998e;'>XÁC NHẬN</strong>!</p>" +
            "  <div style='background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #11998e;'>" +
            "    <p><strong>🐕 Thú cưng:</strong> " + (petName != null ? petName : "Chưa có tên") + "</p>" +
            "    <p><strong>💉 Dịch vụ:</strong> " + (serviceName != null ? serviceName : "Chưa xác định") + "</p>" +
            "    <p><strong>📅 Ngày hẹn:</strong> " + appointmentDate + "</p>" +
            "    <p><strong>👨‍⚕️ Bác sĩ:</strong> " + (doctorName != null && !doctorName.equals("Chưa chỉ định") ? doctorName : "Sẽ được phân công khi đến") + "</p>" +
            "  </div>" +
            "  <div style='background: #e8f5e9; padding: 15px; border-radius: 8px; margin: 20px 0;'>" +
            "    <p style='margin: 0; color: #2e7d32;'><strong>📍 Địa chỉ:</strong> 123 Đường ABC, Quận XYZ, TP.HCM</p>" +
            "    <p style='margin: 10px 0 0 0; color: #2e7d32;'><strong>📞 Hotline:</strong> 1900 xxxx</p>" +
            "  </div>" +
            "  <p><strong>Lưu ý:</strong></p>" +
            "  <ul style='color: #666;'>" +
            "    <li>Vui lòng đến trước giờ hẹn 10-15 phút</li>" +
            "    <li>Mang theo sổ tiêm chủng (nếu có)</li>" +
            "    <li>Liên hệ hotline nếu cần thay đổi lịch</li>" +
            "  </ul>" +
            "  <p style='color: #666;'>Trân trọng,<br>Đội ngũ PetVaccine</p>" +
            "</div>" +
            "</body></html>";
    }
    
    private static String buildRejectionEmail(String customerName, String petName,
            String serviceName, String appointmentDate, String reason) {
        
        return "<!DOCTYPE html>" +
            "<html><head><meta charset='UTF-8'></head>" +
            "<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
            "<div style='background: linear-gradient(135deg, #eb3349 0%, #f45c43 100%); padding: 30px; text-align: center;'>" +
            "  <h1 style='color: white; margin: 0;'>❌ Lịch hẹn không được duyệt</h1>" +
            "</div>" +
            "<div style='padding: 30px; background: #f9f9f9;'>" +
            "  <p>Xin chào <strong>" + customerName + "</strong>,</p>" +
            "  <p>Rất tiếc, lịch hẹn sau đây <strong style='color: #e74c3c;'>KHÔNG ĐƯỢC DUYỆT</strong>:</p>" +
            "  <div style='background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #e74c3c;'>" +
            "    <p><strong>🐕 Thú cưng:</strong> " + (petName != null ? petName : "Chưa có tên") + "</p>" +
            "    <p><strong>💉 Dịch vụ:</strong> " + (serviceName != null ? serviceName : "Chưa xác định") + "</p>" +
            "    <p><strong>📅 Ngày hẹn:</strong> " + appointmentDate + "</p>" +
            "    <p><strong>📝 Lý do:</strong> " + (reason != null ? reason : "Lịch không phù hợp") + "</p>" +
            "  </div>" +
            "  <div style='background: #fff3e0; padding: 15px; border-radius: 8px; margin: 20px 0;'>" +
            "    <p style='margin: 0; color: #e65100;'><strong>💡 Gợi ý:</strong> Bạn có thể đặt lịch vào ngày khác hoặc liên hệ hotline để được hỗ trợ.</p>" +
            "  </div>" +
            "  <a href='#' style='display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 12px 30px; border-radius: 25px; text-decoration: none; font-weight: bold; margin: 10px 0;'>Đặt lịch mới</a>" +
            "  <p style='color: #666;'>Trân trọng,<br>Đội ngũ PetVaccine</p>" +
            "</div>" +
            "</body></html>";
    }
}
