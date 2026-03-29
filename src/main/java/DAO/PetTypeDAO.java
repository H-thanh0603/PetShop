package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import Context.DBContext;
import Model.PetType;

/**
 * DAO quản lý các loại thú cưng
 * Hỗ trợ mở rộng dễ dàng khi thêm loại thú cưng mới
 */
public class PetTypeDAO {

    /**
     * Lấy tất cả loại thú cưng đang active
     */
    public List<PetType> getActivePetTypes() {
        List<PetType> list = new ArrayList<>();
        String query = "SELECT * FROM pet_types WHERE is_active = 1 ORDER BY display_order";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy tất cả loại thú cưng (kể cả inactive - dùng cho admin)
     */
    public List<PetType> getAllPetTypes() {
        List<PetType> list = new ArrayList<>();
        String query = "SELECT * FROM pet_types ORDER BY display_order";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy loại thú cưng theo code (dog, cat, fish, ...)
     */
    public PetType getPetTypeByCode(String code) {
        String query = "SELECT * FROM pet_types WHERE code = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy loại thú cưng theo ID
     */
    public PetType getPetTypeById(int id) {
        String query = "SELECT * FROM pet_types WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Thêm loại thú cưng mới
     */
    public boolean addPetType(PetType petType) {
        String query = "INSERT INTO pet_types (code, name, icon, display_order, is_active) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, petType.getCode());
            ps.setString(2, petType.getName());
            ps.setString(3, petType.getIcon());
            ps.setInt(4, petType.getDisplayOrder());
            ps.setBoolean(5, petType.isActive());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật loại thú cưng
     */
    public boolean updatePetType(PetType petType) {
        String query = "UPDATE pet_types SET name = ?, icon = ?, display_order = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, petType.getName());
            ps.setString(2, petType.getIcon());
            ps.setInt(3, petType.getDisplayOrder());
            ps.setBoolean(4, petType.isActive());
            ps.setInt(5, petType.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Kích hoạt/vô hiệu hóa loại thú cưng
     */
    public boolean togglePetTypeStatus(int id, boolean isActive) {
        String query = "UPDATE pet_types SET is_active = ? WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setBoolean(1, isActive);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private PetType mapResultSet(ResultSet rs) throws Exception {
        return new PetType(
            rs.getInt("id"),
            rs.getString("code"),
            rs.getString("name"),
            rs.getString("icon"),
            rs.getInt("display_order"),
            rs.getBoolean("is_active")
        );
    }
}
