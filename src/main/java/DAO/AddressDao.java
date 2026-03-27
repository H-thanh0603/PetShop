package DAO;

import Context.DBContext;
import Model.Address;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AddressDao {

    public List<Address> getAddressesByUserId(int userId) {
        List<Address> list = new ArrayList<>();
        String sql = "SELECT * FROM addresses WHERE user_id = ? ORDER BY `default` DESC, created_at DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Address a = new Address();
                a.setId(rs.getInt("id"));
                a.setUserId(rs.getInt("user_id"));
                a.setDefaultt(rs.getBoolean("default"));
                a.setAddress(rs.getString("address"));
                a.setCreateAt(rs.getTimestamp("created_at"));
                a.setProvince(rs.getString("province"));
                a.setDistrict(rs.getString("district"));
                a.setWard(rs.getString("ward"));
                list.add(a);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void setDefaultAddress(int userId, int addressId) {
        Connection conn = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;

        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            String sql1 = "UPDATE addresses SET `default` = 0 WHERE user_id = ?";
            ps1 = conn.prepareStatement(sql1);
            ps1.setInt(1, userId);
            ps1.executeUpdate();

            String sql2 = "UPDATE addresses SET `default` = 1 WHERE id = ? AND user_id = ?";
            ps2 = conn.prepareStatement(sql2);
            ps2.setInt(1, addressId);
            ps2.setInt(2, userId);
            ps2.executeUpdate();

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (ps1 != null) ps1.close();
                if (ps2 != null) ps2.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addAddress(int userId, boolean defaultt, Timestamp createdAt,
                           String address, String province, String district, String ward) {

        String resetSql = "UPDATE addresses SET `default` = 0 WHERE user_id = ?";
        String insertSql = "INSERT INTO addresses (user_id, `default`, created_at, address, province, district, ward) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement psReset = null;
        PreparedStatement psInsert = null;

        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            if (defaultt) {
                psReset = conn.prepareStatement(resetSql);
                psReset.setInt(1, userId);
                psReset.executeUpdate();
            }

            psInsert = conn.prepareStatement(insertSql);
            psInsert.setInt(1, userId);
            psInsert.setBoolean(2, defaultt);
            psInsert.setTimestamp(3, createdAt);
            psInsert.setString(4, address);
            psInsert.setString(5, province);
            psInsert.setString(6, district);
            psInsert.setString(7, ward);
            psInsert.executeUpdate();

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (psReset != null) psReset.close();
                if (psInsert != null) psInsert.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean hasAnyAddress(int userId) {
        String sql = "SELECT id FROM addresses WHERE user_id = ? LIMIT 1";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateAddress(int id, int userId, boolean isDefault, Timestamp updatedAt,
                              String address, String province, String district, String ward) {

        String resetSql = "UPDATE addresses SET `default` = 0 WHERE user_id = ?";
        String updateSql = "UPDATE addresses "
                + "SET `default` = ?, created_at = ?, address = ?, province = ?, district = ?, ward = ? "
                + "WHERE id = ? AND user_id = ?";

        Connection conn = null;
        PreparedStatement psReset = null;
        PreparedStatement psUpdate = null;

        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            if (isDefault) {
                psReset = conn.prepareStatement(resetSql);
                psReset.setInt(1, userId);
                psReset.executeUpdate();
            }

            psUpdate = conn.prepareStatement(updateSql);
            psUpdate.setBoolean(1, isDefault);
            psUpdate.setTimestamp(2, updatedAt);
            psUpdate.setString(3, address);
            psUpdate.setString(4, province);
            psUpdate.setString(5, district);
            psUpdate.setString(6, ward);
            psUpdate.setInt(7, id);
            psUpdate.setInt(8, userId);
            psUpdate.executeUpdate();

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (psReset != null) psReset.close();
                if (psUpdate != null) psUpdate.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Address getAddressById(int id, int userId) {
        String sql = "SELECT * FROM addresses WHERE id = ? AND user_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, userId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Model.Address a = new Model.Address();
                a.setId(rs.getInt("id"));
                a.setUserId(rs.getInt("user_id"));
                a.setDefaultt(rs.getBoolean("default"));
                a.setAddress(rs.getString("address"));
                a.setCreateAt(rs.getTimestamp("created_at"));
                a.setProvince(rs.getString("province"));
                a.setDistrict(rs.getString("district"));
                a.setWard(rs.getString("ward"));
                return a;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}