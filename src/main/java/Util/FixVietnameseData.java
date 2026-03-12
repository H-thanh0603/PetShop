package Util;

import Context.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * Utility để fix encoding tiếng Việt trong database
 * Chạy file này một lần để update dữ liệu
 */
public class FixVietnameseData {
    
    public static void main(String[] args) {
        try {
            DBContext db = new DBContext();
            Connection conn = db.getConnection();
            
            System.out.println("=== Bắt đầu fix encoding tiếng Việt ===");
            
            // 1. Fix Users
            System.out.println("Fixing users...");
            updateData(conn, "UPDATE users SET fullname = ? WHERE id = ?", "Quản trị viên", 1);
            updateData(conn, "UPDATE users SET fullname = ? WHERE id = ?", "Nguyễn Văn Thành", 2);
            updateData(conn, "UPDATE users SET fullname = ? WHERE id = ?", "Trần Thị Bình", 5);
            updateData(conn, "UPDATE users SET fullname = ? WHERE id = ?", "Lê Văn Cường", 6);
            updateData(conn, "UPDATE users SET fullname = ? WHERE id = ?", "Phạm Thị Dung", 7);
            updateData(conn, "UPDATE users SET fullname = ? WHERE id = ?", "Hoàng Văn Em", 8);
            
            // 2. Fix Vaccines
            System.out.println("Fixing vaccines...");
            String[] vaccineUpdates = {
                "UPDATE vaccines SET name = 'Vaccine 5 bệnh chó', description = 'Phòng Care, Parvo, Viêm gan, Ho cũi, Phó cúm' WHERE id = 1",
                "UPDATE vaccines SET name = 'Vaccine 7 bệnh chó', description = 'Phòng 5 bệnh + Lepto + Corona' WHERE id = 2",
                "UPDATE vaccines SET name = 'Vaccine dại', description = 'Phòng bệnh dại cho chó mèo', target_species = 'Tất cả' WHERE id = 3",
                "UPDATE vaccines SET name = 'Vaccine 4 bệnh mèo', description = 'Phòng Viêm mũi, Calici, Panleukopenia', target_species = 'Mèo' WHERE id = 4",
                "UPDATE vaccines SET name = 'Vaccine FeLV', description = 'Phòng virus gây bạch cầu ở mèo', target_species = 'Mèo' WHERE id = 5",
                "UPDATE vaccines SET name = 'Vaccine Kennel Cough', description = 'Phòng ho cũi cho chó', target_species = 'Chó' WHERE id = 6",
                "UPDATE vaccines SET name = 'Vaccine Lepto', description = 'Phòng bệnh Leptospirosis', target_species = 'Chó' WHERE id = 7",
                "UPDATE vaccines SET name = 'Vaccine Bordetella', description = 'Phòng viêm phế quản truyền nhiễm', target_species = 'Chó' WHERE id = 8",
                "UPDATE vaccines SET name = 'Vaccine Corona chó', description = 'Phòng virus Corona ở chó', target_species = 'Chó' WHERE id = 9",
                "UPDATE vaccines SET name = 'Vaccine cúm chó', description = 'Phòng virus cúm H3N8/H3N2', target_species = 'Chó' WHERE id = 10"
            };
            for (String sql : vaccineUpdates) {
                executeUpdate(conn, sql);
            }
            
            // 3. Fix Pets
            System.out.println("Fixing pets...");
            String[] petUpdates = {
                "UPDATE pets SET species = 'Chó', gender = 'Đực', notes = 'Rất thân thiện' WHERE name = 'Lucky'",
                "UPDATE pets SET species = 'Mèo', gender = 'Cái', notes = 'Nhát người' WHERE name = 'Miu Miu'",
                "UPDATE pets SET species = 'Chó', gender = 'Cái', notes = 'Đã triệt sản' WHERE name = 'Bong'",
                "UPDATE pets SET species = 'Chó', gender = 'Đực', notes = 'Đã huấn luyện' WHERE name = 'Rex'",
                "UPDATE pets SET species = 'Mèo', gender = 'Cái', notes = 'Năng động' WHERE name = 'Kitty'",
                "UPDATE pets SET species = 'Chó', gender = 'Đực', notes = 'Thích bơi lội' WHERE name = 'Buddy'",
                "UPDATE pets SET species = 'Mèo', gender = 'Cái', notes = 'Kích thước lớn' WHERE name = 'Nala'",
                "UPDATE pets SET species = 'Chó', gender = 'Đực', notes = 'Cần vận động nhiều' WHERE name = 'Max'",
                "UPDATE pets SET species = 'Mèo', gender = 'Cái', notes = 'Rất hiền lành' WHERE name = 'Luna'",
                "UPDATE pets SET species = 'Chó', gender = 'Cái', notes = 'Thông minh' WHERE name = 'Coco'"
            };
            for (String sql : petUpdates) {
                executeUpdate(conn, sql);
            }
            
            // 4. Fix Appointments
            System.out.println("Fixing appointments...");
            String[] appointmentUpdates = {
                "UPDATE appointments SET customer_name = 'Nguyễn Văn Thành', pet_type = 'Chó', note = 'Tiêm vaccine dại định kỳ' WHERE user_id = 2 AND pet_name = 'Lucky'",
                "UPDATE appointments SET customer_name = 'Nguyễn Văn Thành', pet_type = 'Mèo', note = 'Khám tổng quát' WHERE user_id = 2 AND pet_name = 'Miu Miu'",
                "UPDATE appointments SET customer_name = 'Nguyễn Văn Thành', pet_type = 'Chó', note = 'Tắm spa và cắt tỉa lông' WHERE user_id = 2 AND pet_name = 'Bong'",
                "UPDATE appointments SET customer_name = 'Trần Thị Bình', pet_type = 'Chó', note = 'Khám da, nghi ngờ nấm' WHERE user_id = 5 AND pet_name = 'Rex'",
                "UPDATE appointments SET customer_name = 'Trần Thị Bình', pet_type = 'Mèo', note = 'Phẫu thuật triệt sản' WHERE user_id = 5 AND pet_name = 'Kitty'",
                "UPDATE appointments SET customer_name = 'Trần Thị Bình', pet_type = 'Chó', note = 'Tiêm vaccine 7 bệnh' WHERE user_id = 5 AND pet_name = 'Buddy'",
                "UPDATE appointments SET customer_name = 'Lê Văn Cường', pet_type = 'Mèo', note = 'Khám mắt' WHERE user_id = 6 AND pet_name = 'Nala'",
                "UPDATE appointments SET customer_name = 'Lê Văn Cường', pet_type = 'Chó', note = 'Spa full combo' WHERE user_id = 6 AND pet_name = 'Max'",
                "UPDATE appointments SET customer_name = 'Lê Văn Cường', pet_type = 'Mèo', note = 'Tiêm vaccine FeLV' WHERE user_id = 6 AND pet_name = 'Luna'",
                "UPDATE appointments SET customer_name = 'Nguyễn Văn Thành', pet_type = 'Chó', note = 'Gửi khách sạn 3 ngày' WHERE user_id = 2 AND pet_name = 'Coco'"
            };
            for (String sql : appointmentUpdates) {
                executeUpdate(conn, sql);
            }
            
            // 5. Fix Hotel Bookings
            System.out.println("Fixing hotel_bookings...");
            executeUpdate(conn, "UPDATE hotel_bookings SET customer_name = 'Nguyễn Văn Thành', pet_type = 'Mèo', pet_weight = 'Dưới 5kg', room_type = 'Phòng Tiêu chuẩn', extra_services = 'Chế độ ăn cao cấp', note = 'Bé nhát người' WHERE user_id = 2 AND pet_name = 'Miu Miu'");
            executeUpdate(conn, "UPDATE hotel_bookings SET customer_name = 'Nguyễn Văn Thành', pet_type = 'Chó', pet_weight = 'Trên 5kg', room_type = 'Phòng VIP', extra_services = 'Vui chơi thêm 30 phút' WHERE user_id = 2 AND pet_name = 'Bong'");
            executeUpdate(conn, "UPDATE hotel_bookings SET customer_name = 'Trần Thị Bình', pet_type = 'Mèo', pet_weight = 'Dưới 5kg', room_type = 'Phòng Tiêu chuẩn', note = 'Cần phòng yên tĩnh' WHERE user_id = 5 AND pet_name = 'Kitty'");
            executeUpdate(conn, "UPDATE hotel_bookings SET customer_name = 'Trần Thị Bình', pet_type = 'Chó', pet_weight = 'Trên 5kg', room_type = 'Phòng VIP', extra_services = 'Tắm spa khi trả phòng', note = 'Chó lớn cần phòng rộng' WHERE user_id = 5 AND pet_name = 'Rex'");
            executeUpdate(conn, "UPDATE hotel_bookings SET customer_name = 'Lê Văn Cường', pet_type = 'Mèo', pet_weight = 'Dưới 5kg', room_type = 'Phòng Tiêu chuẩn', extra_services = 'Cập nhật video hàng ngày' WHERE user_id = 6 AND pet_name = 'Luna'");
            executeUpdate(conn, "UPDATE hotel_bookings SET customer_name = 'Lê Văn Cường', pet_type = 'Mèo', pet_weight = 'Trên 5kg', room_type = 'Phòng VIP', extra_services = 'Chế độ ăn cao cấp' WHERE user_id = 6 AND pet_name = 'Nala'");
            executeUpdate(conn, "UPDATE hotel_bookings SET customer_name = 'Nguyễn Văn Thành', pet_type = 'Chó', pet_weight = 'Trên 5kg', room_type = 'Phòng VIP', note = 'Đi công tác' WHERE user_id = 2 AND pet_name = 'Coco'");
            executeUpdate(conn, "UPDATE hotel_bookings SET customer_name = 'Trần Thị Bình', pet_type = 'Chó', pet_weight = 'Trên 5kg', room_type = 'Phòng VIP', extra_services = 'Vui chơi thêm 30 phút, Tắm spa khi trả phòng', note = 'Labrador thích nước' WHERE user_id = 5 AND pet_name = 'Buddy'");
            executeUpdate(conn, "UPDATE hotel_bookings SET customer_name = 'Lê Văn Cường', pet_type = 'Chó', pet_weight = 'Trên 5kg', room_type = 'Phòng VIP', extra_services = 'Chế độ ăn cao cấp, Cập nhật video hàng ngày', note = 'Husky cần vận động' WHERE user_id = 6 AND pet_name = 'Max'");
            executeUpdate(conn, "UPDATE hotel_bookings SET customer_name = 'Nguyễn Văn Thành', pet_type = 'Chó', pet_weight = 'Trên 5kg', room_type = 'Phòng VIP', extra_services = 'Tắm spa khi trả phòng', note = 'Golden thân thiện' WHERE user_id = 2 AND pet_name = 'Lucky'");
            
            // 6. Fix Spa Bookings
            System.out.println("Fixing spa_bookings...");
            executeUpdate(conn, "UPDATE spa_bookings SET customer_name = 'Nguyễn Văn Thành', pet_type = 'Chó', spa_package = 'Cắt tỉa toàn thân (> 5kg) - 500.000đ', preferred_time = 'Sáng (9h-12h)', note = 'Cắt kiểu Teddy bear' WHERE user_id = 2 AND pet_name = 'Lucky'");
            executeUpdate(conn, "UPDATE spa_bookings SET customer_name = 'Nguyễn Văn Thành', pet_type = 'Chó', spa_package = 'Tắm vệ sinh (< 5kg) - 150.000đ', preferred_time = 'Chiều (14h-17h)' WHERE user_id = 2 AND pet_name = 'Bong'");
            executeUpdate(conn, "UPDATE spa_bookings SET customer_name = 'Nguyễn Văn Thành', pet_type = 'Mèo', spa_package = 'Tắm vệ sinh (< 5kg) - 150.000đ', preferred_time = 'Sáng (9h-12h)', note = 'Mèo sợ nước' WHERE user_id = 2 AND pet_name = 'Miu Miu'");
            executeUpdate(conn, "UPDATE spa_bookings SET customer_name = 'Trần Thị Bình', pet_type = 'Chó', spa_package = 'Cắt tỉa toàn thân (> 5kg) - 500.000đ', preferred_time = 'Sáng (9h-12h)', note = 'Cắt ngắn dễ chăm sóc' WHERE user_id = 5 AND pet_name = 'Rex'");
            executeUpdate(conn, "UPDATE spa_bookings SET customer_name = 'Trần Thị Bình', pet_type = 'Chó', spa_package = 'Tắm vệ sinh (> 5kg) - 250.000đ', preferred_time = 'Tối (17h-19h)', note = 'Labrador thích nước' WHERE user_id = 5 AND pet_name = 'Buddy'");
            executeUpdate(conn, "UPDATE spa_bookings SET customer_name = 'Trần Thị Bình', pet_type = 'Mèo', spa_package = 'Tắm vệ sinh (< 5kg) - 150.000đ', preferred_time = 'Chiều (14h-17h)' WHERE user_id = 5 AND pet_name = 'Kitty'");
            executeUpdate(conn, "UPDATE spa_bookings SET customer_name = 'Lê Văn Cường', pet_type = 'Chó', spa_package = 'Combo VIP (Tắm + Cắt tỉa + Nhuộm)', preferred_time = 'Sáng (9h-12h)', note = 'Nhuộm xanh nhạt ở tai' WHERE user_id = 6 AND pet_name = 'Max'");
            executeUpdate(conn, "UPDATE spa_bookings SET customer_name = 'Lê Văn Cường', pet_type = 'Mèo', spa_package = 'Cắt tỉa toàn thân (> 5kg) - 500.000đ', preferred_time = 'Chiều (14h-17h)', note = 'Maine Coon lông dài' WHERE user_id = 6 AND pet_name = 'Nala'");
            executeUpdate(conn, "UPDATE spa_bookings SET customer_name = 'Lê Văn Cường', pet_type = 'Mèo', spa_package = 'Tắm vệ sinh (< 5kg) - 150.000đ', preferred_time = 'Sáng (9h-12h)', note = 'Ragdoll hiền lành' WHERE user_id = 6 AND pet_name = 'Luna'");
            executeUpdate(conn, "UPDATE spa_bookings SET customer_name = 'Nguyễn Văn Thành', pet_type = 'Chó', spa_package = 'Cắt tỉa toàn thân (< 5kg) - 350.000đ', preferred_time = 'Tối (17h-19h)', note = 'Kiểu Corgi truyền thống' WHERE user_id = 2 AND pet_name = 'Coco'");
            
            // 7. Fix Vaccination Records notes
            System.out.println("Fixing vaccination_records...");
            executeUpdate(conn, "UPDATE vaccination_records SET notes = 'Mũi 1 vaccine 5 bệnh' WHERE notes LIKE '%Mui 1 vaccine 5 benh%'");
            executeUpdate(conn, "UPDATE vaccination_records SET notes = 'Vaccine dại' WHERE notes LIKE '%Vaccine dai%'");
            executeUpdate(conn, "UPDATE vaccination_records SET notes = 'Mũi 1 vaccine 4 bệnh mèo' WHERE notes LIKE '%Mui 1 vaccine 4 benh meo%'");
            executeUpdate(conn, "UPDATE vaccination_records SET notes = 'Mũi 1 vaccine 7 bệnh' WHERE notes LIKE '%Mui 1 vaccine 7 benh%'");
            executeUpdate(conn, "UPDATE vaccination_records SET notes = 'Mũi 1 vaccine FeLV' WHERE notes LIKE '%Mui 1 vaccine FeLV%'");
            
            conn.close();
            System.out.println("=== Hoàn thành fix encoding tiếng Việt! ===");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void updateData(Connection conn, String sql, String value, int id) throws Exception {
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, value);
        ps.setInt(2, id);
        ps.executeUpdate();
        ps.close();
    }
    
    private static void executeUpdate(Connection conn, String sql) throws Exception {
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.executeUpdate();
        ps.close();
    }
}
