package DAO;

import Context.DBContext;
import Model.Address;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AddressDao {
    public void insertAddress(int userId, String address, boolean defaultt, Timestamp created_at) {
        String sql = "INSERT INTO user_addresses(user_id, address, defaultt, created_at) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, address);
            ps.setBoolean(3, defaultt);
            ps.setTimestamp(4, created_at);

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setDefaultAddress(int userId, int addressId) {
        String resetSql = "UPDATE addresses SET defaultt = 0 WHERE user_id = ?";
        String setDefaultSql = "UPDATE addresses SET defaultt = 1 WHERE id = ? AND user_id = ?";
        try (Connection conn = DBContext.getConnection();) {
            conn.setAutoCommit(false);
            try (PreparedStatement psReset = conn.prepareStatement(resetSql)) {
                psReset.setInt(1, userId);
                psReset.executeUpdate();
            }
            try (PreparedStatement psSet = conn.prepareStatement(setDefaultSql)) {
                psSet.setInt(1, addressId);
                psSet.setInt(2, userId);
                psSet.executeUpdate();
            }
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Address> getAddressesByUser(int userId) {
        List<Address> list = new ArrayList<>();
        String sql = "SELECT * FROM addresses WHERE user_id = ?";

        try (Connection con = DBContext.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Address addr = new Address();
                addr.setId(rs.getInt("id"));
                addr.setUserId(rs.getInt("user_id"));
                addr.setDefaultt(rs.getBoolean("defaultt"));
                addr.setCreateAt(rs.getTimestamp("created_at"));
                addr.setAddress(rs.getString("address"));
                addr.setProvince(rs.getString("province"));
                addr.setDistrict(rs.getString("district"));
                addr.setWard(rs.getString("ward"));
                list.add(addr);
                ps.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
    public void addAddress(int userId, boolean defaultt, Timestamp created_at,  String address, String province, String district, String ward) {
        String insertSql = "INSERT INTO addresses (user_id, defaultt, created_at, address, province, district, ward) VALUES (?,?,?,?,?,?,?)";
        String resetSql = "UPDATE addresses SET defaultt = 0 WHERE user_id = ?";
        try (Connection conn = DBContext.getConnection();) {
            conn.setAutoCommit(false);

            if (defaultt) {
                try (PreparedStatement psReset = conn.prepareStatement(resetSql)) {
                    psReset.setInt(1, userId);
                    psReset.executeUpdate();
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, userId);
                ps.setBoolean(2, defaultt);
                ps.setTimestamp(3, created_at);
                ps.setString(4, address);
                ps.setString(5, province);
                ps.setString(6, district);
                ps.setString(7, ward);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
