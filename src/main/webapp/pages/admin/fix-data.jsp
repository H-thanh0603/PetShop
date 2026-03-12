<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="Context.DBContext" %>
<%@ page import="java.sql.*" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Fix Vietnamese Data</title>
</head>
<body>
<%
    // Vietnamese strings with Unicode escape
    String QUAN_TRI_VIEN = "Qu\u1EA3n tr\u1ECB vi\u00EAn";
    String NGUYEN_VAN_THANH = "Nguy\u1EC5n V\u0103n Th\u00E0nh";
    String TRAN_THI_BINH = "Tr\u1EA7n Th\u1ECB B\u00ECnh";
    String LE_VAN_CUONG = "L\u00EA V\u0103n C\u01B0\u1EDDng";
    String PHAM_THI_DUNG = "Ph\u1EA1m Th\u1ECB Dung";
    String HOANG_VAN_EM = "Ho\u00E0ng V\u0103n Em";
    
    String CHO = "Ch\u00F3";
    String MEO = "M\u00E8o";
    String DUC = "\u0110\u1EF1c";
    String CAI = "C\u00E1i";
    String TAT_CA = "T\u1EA5t c\u1EA3";
    String DUOI_5KG = "D\u01B0\u1EDBi 5kg";
    String TREN_5KG = "Tr\u00EAn 5kg";
    String PHONG_TIEU_CHUAN = "Ph\u00F2ng Ti\u00EAu chu\u1EA9n - 150.000\u0111/ng\u00E0y";
    String PHONG_VIP = "Ph\u00F2ng VIP - 250.000\u0111/ng\u00E0y";
    String SANG = "S\u00E1ng (9h-12h)";
    String CHIEU = "Chi\u1EC1u (14h-17h)";
    String TOI = "T\u1ED1i (17h-19h)";
    
    try {
        DBContext db = new DBContext();
        Connection conn = db.getConnection();
        
        out.println("<h2>Xóa dữ liệu cũ...</h2>");
        conn.prepareStatement("DELETE FROM vaccination_records").executeUpdate();
        conn.prepareStatement("DELETE FROM cart").executeUpdate();
        conn.prepareStatement("DELETE FROM reviews").executeUpdate();
        conn.prepareStatement("DELETE FROM appointments").executeUpdate();
        conn.prepareStatement("DELETE FROM hotel_bookings").executeUpdate();
        conn.prepareStatement("DELETE FROM spa_bookings").executeUpdate();
        conn.prepareStatement("DELETE FROM pets").executeUpdate();
        conn.prepareStatement("DELETE FROM vaccines").executeUpdate();
        
        out.println("<h2>Thêm dữ liệu mới...</h2>");
        
        // Fix Users
        out.println("<p>Fixing users...</p>");
        PreparedStatement ps = conn.prepareStatement("UPDATE users SET fullname = ? WHERE id = ?");
        ps.setString(1, QUAN_TRI_VIEN); ps.setInt(2, 1); ps.executeUpdate();
        ps.setString(1, NGUYEN_VAN_THANH); ps.setInt(2, 2); ps.executeUpdate();
        ps.setString(1, TRAN_THI_BINH); ps.setInt(2, 5); ps.executeUpdate();
        ps.setString(1, LE_VAN_CUONG); ps.setInt(2, 6); ps.executeUpdate();
        ps.setString(1, PHAM_THI_DUNG); ps.setInt(2, 7); ps.executeUpdate();
        ps.setString(1, HOANG_VAN_EM); ps.setInt(2, 8); ps.executeUpdate();
        
        // Add Vaccines
        out.println("<p>Adding vaccines...</p>");
        ps = conn.prepareStatement("INSERT INTO vaccines (name, description, target_species, manufacturer, price, doses_required, interval_days, min_age_weeks, stock_quantity, is_active) VALUES (?, ?, ?, 'Nobivac', ?, 2, 21, 8, 100, 1)");
        String[][] vaccines = {
            {"Vaccine 5 b\u1EC7nh ch\u00F3", "Ph\u00F2ng Care, Parvo, Vi\u00EAm gan", CHO, "250000"},
            {"Vaccine 7 b\u1EC7nh ch\u00F3", "Ph\u00F2ng 5 b\u1EC7nh + Lepto + Corona", CHO, "350000"},
            {"Vaccine d\u1EA1i", "Ph\u00F2ng b\u1EC7nh d\u1EA1i cho ch\u00F3 m\u00E8o", TAT_CA, "150000"},
            {"Vaccine 4 b\u1EC7nh m\u00E8o", "Ph\u00F2ng Vi\u00EAm m\u0169i, Calici", MEO, "280000"},
            {"Vaccine FeLV", "Ph\u00F2ng virus g\u00E2y b\u1EA1ch c\u1EA7u", MEO, "320000"}
        };
        for (String[] v : vaccines) {
            ps.setString(1, v[0]); ps.setString(2, v[1]); ps.setString(3, v[2]); ps.setInt(4, Integer.parseInt(v[3]));
            ps.executeUpdate();
        }

        // Add Pets
        out.println("<p>Adding pets...</p>");
        ps = conn.prepareStatement("INSERT INTO pets (user_id, name, species, breed, gender, birth_date, weight, color, notes) VALUES (?, ?, ?, ?, ?, '2023-01-01', ?, 'Mixed', ?)");
        Object[][] pets = {
            {2, "Lucky", CHO, "Golden Retriever", DUC, 25.5, "R\u1EA5t th\u00E2n thi\u1EC7n"},
            {2, "Miu Miu", MEO, "M\u00E8o Anh l\u00F4ng ng\u1EAFn", CAI, 4.2, "Nh\u00E1t ng\u01B0\u1EDDi"},
            {2, "B\u00F4ng", CHO, "Poodle", CAI, 5.8, "\u0110\u00E3 tri\u1EC7t s\u1EA3n"},
            {5, "Rex", CHO, "German Shepherd", DUC, 32.0, "\u0110\u00E3 hu\u1EA5n luy\u1EC7n"},
            {5, "Kitty", MEO, "M\u00E8o Xi\u00EAm", CAI, 3.8, "N\u0103ng \u0111\u1ED9ng"},
            {5, "Buddy", CHO, "Labrador", DUC, 28.0, "Th\u00EDch b\u01A1i l\u1ED9i"},
            {6, "Nala", MEO, "Maine Coon", CAI, 6.5, "K\u00EDch th\u01B0\u1EDBc l\u1EDBn"},
            {6, "Max", CHO, "Husky", DUC, 22.0, "C\u1EA7n v\u1EADn \u0111\u1ED9ng nhi\u1EC1u"},
            {6, "Luna", MEO, "Ragdoll", CAI, 4.0, "R\u1EA5t hi\u1EC1n l\u00E0nh"},
            {2, "Coco", CHO, "Corgi", CAI, 11.0, "Th\u00F4ng minh"}
        };
        for (Object[] p : pets) {
            ps.setInt(1, (Integer)p[0]); ps.setString(2, (String)p[1]); ps.setString(3, (String)p[2]);
            ps.setString(4, (String)p[3]); ps.setString(5, (String)p[4]); ps.setDouble(6, (Double)p[5]);
            ps.setString(7, (String)p[6]); ps.executeUpdate();
        }
        
        // Add Appointments
        out.println("<p>Adding appointments...</p>");
        ps = conn.prepareStatement("INSERT INTO appointments (user_id, customer_name, phone, pet_name, pet_type, service_id, doctor_id, booking_date, note, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        Object[][] appts = {
            {2, NGUYEN_VAN_THANH, "0901234567", "Lucky", CHO, 3, 1, "2026-01-20", "Ti\u00EAm vaccine d\u1EA1i \u0111\u1ECBnh k\u1EF3", "Confirmed"},
            {2, NGUYEN_VAN_THANH, "0901234567", "Miu Miu", MEO, 1, 2, "2026-01-21", "Kh\u00E1m t\u1ED5ng qu\u00E1t", "Pending"},
            {2, NGUYEN_VAN_THANH, "0901234567", "B\u00F4ng", CHO, 4, 3, "2026-01-22", "T\u1EAFm spa v\u00E0 c\u1EAFt t\u1EC9a l\u00F4ng", "Confirmed"},
            {5, TRAN_THI_BINH, "0902345678", "Rex", CHO, 1, 1, "2026-01-23", "Kh\u00E1m da, nghi ng\u1EDD n\u1EA5m", "Pending"},
            {5, TRAN_THI_BINH, "0902345678", "Kitty", MEO, 2, 2, "2026-01-24", "Ph\u1EABu thu\u1EADt tri\u1EC7t s\u1EA3n", "Confirmed"},
            {5, TRAN_THI_BINH, "0902345678", "Buddy", CHO, 3, 1, "2026-01-25", "Ti\u00EAm vaccine 7 b\u1EC7nh", "Pending"},
            {6, LE_VAN_CUONG, "0903456789", "Nala", MEO, 1, 3, "2026-01-26", "Kh\u00E1m m\u1EAFt", "Pending"},
            {6, LE_VAN_CUONG, "0903456789", "Max", CHO, 4, 1, "2026-01-27", "Spa full combo", "Confirmed"},
            {6, LE_VAN_CUONG, "0903456789", "Luna", MEO, 3, 2, "2026-01-28", "Ti\u00EAm vaccine FeLV", "Pending"},
            {2, NGUYEN_VAN_THANH, "0901234567", "Coco", CHO, 5, 3, "2026-01-29", "G\u1EEDi kh\u00E1ch s\u1EA1n 3 ng\u00E0y", "Confirmed"}
        };
        for (Object[] a : appts) {
            ps.setInt(1, (Integer)a[0]); ps.setString(2, (String)a[1]); ps.setString(3, (String)a[2]);
            ps.setString(4, (String)a[3]); ps.setString(5, (String)a[4]); ps.setInt(6, (Integer)a[5]);
            ps.setInt(7, (Integer)a[6]); ps.setString(8, (String)a[7]); ps.setString(9, (String)a[8]);
            ps.setString(10, (String)a[9]); ps.executeUpdate();
        }

        // Add Hotel Bookings
        out.println("<p>Adding hotel_bookings...</p>");
        ps = conn.prepareStatement("INSERT INTO hotel_bookings (user_id, customer_name, phone, pet_name, pet_type, pet_weight, room_type, check_in_date, check_out_date, extra_services, note, total_nights, total_price, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        Object[][] hotels = {
            {2, NGUYEN_VAN_THANH, "0901234567", "Miu Miu", MEO, DUOI_5KG, PHONG_TIEU_CHUAN, "2026-01-25", "2026-01-28", "Ch\u1EBF \u0111\u1ED9 \u0103n cao c\u1EA5p", "B\u00E9 nh\u00E1t ng\u01B0\u1EDDi", 3, 600000, "Confirmed"},
            {2, NGUYEN_VAN_THANH, "0901234567", "B\u00F4ng", CHO, TREN_5KG, PHONG_VIP, "2026-02-01", "2026-02-05", "Vui ch\u01A1i th\u00EAm 30 ph\u00FAt", null, 4, 1200000, "Pending"},
            {5, TRAN_THI_BINH, "0902345678", "Kitty", MEO, DUOI_5KG, PHONG_TIEU_CHUAN, "2026-02-10", "2026-02-12", null, "C\u1EA7n ph\u00F2ng y\u00EAn t\u0129nh", 2, 300000, "Pending"},
            {5, TRAN_THI_BINH, "0902345678", "Rex", CHO, TREN_5KG, PHONG_VIP, "2026-02-15", "2026-02-20", "T\u1EAFm spa khi tr\u1EA3 ph\u00F2ng", "Ch\u00F3 l\u1EDBn c\u1EA7n ph\u00F2ng r\u1ED9ng", 5, 1400000, "Confirmed"},
            {6, LE_VAN_CUONG, "0903456789", "Luna", MEO, DUOI_5KG, PHONG_TIEU_CHUAN, "2026-02-20", "2026-02-22", "C\u1EADp nh\u1EADt video h\u00E0ng ng\u00E0y", null, 2, 340000, "Pending"},
            {6, LE_VAN_CUONG, "0903456789", "Nala", MEO, TREN_5KG, PHONG_VIP, "2026-02-25", "2026-02-28", "Ch\u1EBF \u0111\u1ED9 \u0103n cao c\u1EA5p", "Maine Coon", 3, 900000, "Confirmed"},
            {2, NGUYEN_VAN_THANH, "0901234567", "Coco", CHO, TREN_5KG, PHONG_VIP, "2026-03-01", "2026-03-03", null, "\u0110i c\u00F4ng t\u00E1c", 2, 500000, "Pending"},
            {5, TRAN_THI_BINH, "0902345678", "Buddy", CHO, TREN_5KG, PHONG_VIP, "2026-03-05", "2026-03-10", "Vui ch\u01A1i, T\u1EAFm spa", "Labrador th\u00EDch n\u01B0\u1EDBc", 5, 1550000, "Pending"},
            {6, LE_VAN_CUONG, "0903456789", "Max", CHO, TREN_5KG, PHONG_VIP, "2026-03-15", "2026-03-18", "Ch\u1EBF \u0111\u1ED9 \u0103n cao c\u1EA5p, Video", "Husky c\u1EA7n v\u1EADn \u0111\u1ED9ng", 3, 1050000, "Confirmed"},
            {2, NGUYEN_VAN_THANH, "0901234567", "Lucky", CHO, TREN_5KG, PHONG_VIP, "2026-03-20", "2026-03-25", "T\u1EAFm spa khi tr\u1EA3 ph\u00F2ng", "Golden th\u00E2n thi\u1EC7n", 5, 1400000, "Pending"}
        };
        for (Object[] h : hotels) {
            ps.setInt(1, (Integer)h[0]); ps.setString(2, (String)h[1]); ps.setString(3, (String)h[2]);
            ps.setString(4, (String)h[3]); ps.setString(5, (String)h[4]); ps.setString(6, (String)h[5]);
            ps.setString(7, (String)h[6]); ps.setString(8, (String)h[7]); ps.setString(9, (String)h[8]);
            ps.setString(10, (String)h[9]); ps.setString(11, (String)h[10]); ps.setInt(12, (Integer)h[11]);
            ps.setInt(13, (Integer)h[12]); ps.setString(14, (String)h[13]); ps.executeUpdate();
        }
        
        // Add Spa Bookings
        out.println("<p>Adding spa_bookings...</p>");
        ps = conn.prepareStatement("INSERT INTO spa_bookings (user_id, customer_name, phone, pet_name, pet_type, spa_package, booking_date, preferred_time, note, price, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        Object[][] spas = {
            {2, NGUYEN_VAN_THANH, "0901234567", "Lucky", CHO, "C\u1EAFt t\u1EC9a to\u00E0n th\u00E2n (> 5kg) - 500.000\u0111", "2026-01-20", SANG, "C\u1EAFt ki\u1EC3u Teddy bear", 500000, "Confirmed"},
            {2, NGUYEN_VAN_THANH, "0901234567", "B\u00F4ng", CHO, "T\u1EAFm v\u1EC7 sinh (< 5kg) - 150.000\u0111", "2026-01-22", CHIEU, null, 150000, "Pending"},
            {2, NGUYEN_VAN_THANH, "0901234567", "Miu Miu", MEO, "T\u1EAFm v\u1EC7 sinh (< 5kg) - 150.000\u0111", "2026-01-25", SANG, "M\u00E8o s\u1EE3 n\u01B0\u1EDBc", 150000, "Pending"},
            {5, TRAN_THI_BINH, "0902345678", "Rex", CHO, "C\u1EAFt t\u1EC9a to\u00E0n th\u00E2n (> 5kg) - 500.000\u0111", "2026-01-26", SANG, "C\u1EAFt ng\u1EAFn d\u1EC5 ch\u0103m s\u00F3c", 500000, "Confirmed"},
            {5, TRAN_THI_BINH, "0902345678", "Buddy", CHO, "T\u1EAFm v\u1EC7 sinh (> 5kg) - 250.000\u0111", "2026-01-28", TOI, "Labrador th\u00EDch n\u01B0\u1EDBc", 250000, "Pending"},
            {5, TRAN_THI_BINH, "0902345678", "Kitty", MEO, "T\u1EAFm v\u1EC7 sinh (< 5kg) - 150.000\u0111", "2026-01-30", CHIEU, null, 150000, "Confirmed"},
            {6, LE_VAN_CUONG, "0903456789", "Max", CHO, "Combo VIP (T\u1EAFm + C\u1EAFt t\u1EC9a + Nhu\u1ED9m)", "2026-02-01", SANG, "Nhu\u1ED9m xanh nh\u1EA1t \u1EDF tai", 800000, "Pending"},
            {6, LE_VAN_CUONG, "0903456789", "Nala", MEO, "C\u1EAFt t\u1EC9a to\u00E0n th\u00E2n (> 5kg) - 500.000\u0111", "2026-02-03", CHIEU, "Maine Coon l\u00F4ng d\u00E0i", 500000, "Confirmed"},
            {6, LE_VAN_CUONG, "0903456789", "Luna", MEO, "T\u1EAFm v\u1EC7 sinh (< 5kg) - 150.000\u0111", "2026-02-05", SANG, "Ragdoll hi\u1EC1n l\u00E0nh", 150000, "Pending"},
            {2, NGUYEN_VAN_THANH, "0901234567", "Coco", CHO, "C\u1EAFt t\u1EC9a to\u00E0n th\u00E2n (< 5kg) - 350.000\u0111", "2026-02-08", TOI, "Ki\u1EC3u Corgi truy\u1EC1n th\u1ED1ng", 350000, "Confirmed"}
        };
        for (Object[] s : spas) {
            ps.setInt(1, (Integer)s[0]); ps.setString(2, (String)s[1]); ps.setString(3, (String)s[2]);
            ps.setString(4, (String)s[3]); ps.setString(5, (String)s[4]); ps.setString(6, (String)s[5]);
            ps.setString(7, (String)s[6]); ps.setString(8, (String)s[7]); ps.setString(9, (String)s[8]);
            ps.setInt(10, (Integer)s[9]); ps.setString(11, (String)s[10]); ps.executeUpdate();
        }
        
        conn.close();
        out.println("<h3 style='color:green'>✓ Hoàn thành!</h3>");
        out.println("<p><a href='" + request.getContextPath() + "/admin/dashboard'>Quay về Dashboard</a></p>");
        
    } catch (Exception e) {
        out.println("<h3 style='color:red'>Lỗi: " + e.getMessage() + "</h3>");
        e.printStackTrace(new java.io.PrintWriter(out));
    }
%>
</body>
</html>
