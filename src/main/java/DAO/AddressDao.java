package DAO;

import Context.DBContext;
import Model.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AddressDao {

    private static final Logger logger = LoggerFactory.getLogger(AddressDao.class);

    public List<Address> getAddressesByUserId(int userId) {
        List<Address> list = new ArrayList<>();
        String sql = "SELECT * FROM addresses WHERE user_id = ? ORDER BY is_default DESC, created_at DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Address a = new Address();
                    a.setId(rs.getInt("id"));
                    a.setUserId(rs.getInt("user_id"));
                    a.setDefaultt(rs.getBoolean("is_default"));
                    a.setAddress(rs.getString("address"));
                    a.setCreateAt(rs.getTimestamp("created_at"));
                    a.setProvince(rs.getString("province"));
                    a.setDistrict(rs.getString("district"));
                    a.setWard(rs.getString("ward"));
                    list.add(a);
                }
            }

        } catch (Exception e) {
            logger.error("Error fetching addresses for user id={}", userId, e);
        }
        return list;
    }

    public boolean setDefaultAddress(int userId, int addressId) {
        String sql1 = "UPDATE addresses SET is_default = 0 WHERE user_id = ?";
        String sql2 = "UPDATE addresses SET is_default = 1 WHERE id = ? AND user_id = ?";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps1 = conn.prepareStatement(sql1)) {
                    ps1.setInt(1, userId);
                    ps1.executeUpdate();
                }
                try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                    ps2.setInt(1, addressId);
                    ps2.setInt(2, userId);
                    int updated = ps2.executeUpdate();
                    if (updated == 0) {
                        conn.rollback();
                        return false;
                    }
                }
                conn.commit();
                return true;
            } catch (Exception e) {
                logger.error("Error setting default address id={} for user id={}", addressId, userId, e);
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            logger.error("Error in setDefaultAddress transaction for user id={}", userId, e);
        }
        return false;
    }

    public boolean addAddress(int userId, boolean defaultt, Timestamp createdAt,
                           String address, String province, String district, String ward) {

        String resetSql = "UPDATE addresses SET is_default = 0 WHERE user_id = ?";
        String insertSql = "INSERT INTO addresses (user_id, is_default, created_at, address, province, district, ward) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (defaultt) {
                    try (PreparedStatement psReset = conn.prepareStatement(resetSql)) {
                        psReset.setInt(1, userId);
                        psReset.executeUpdate();
                    }
                }
                try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                    psInsert.setInt(1, userId);
                    psInsert.setBoolean(2, defaultt);
                    psInsert.setTimestamp(3, createdAt);
                    psInsert.setString(4, address);
                    psInsert.setString(5, province);
                    psInsert.setString(6, district);
                    psInsert.setString(7, ward);
                    int inserted = psInsert.executeUpdate();
                    if (inserted == 0) {
                        conn.rollback();
                        return false;
                    }
                }
                conn.commit();
                return true;
            } catch (Exception e) {
                logger.error("Error adding address for user id={}", userId, e);
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            logger.error("Error in addAddress transaction for user id={}", userId, e);
        }
        return false;
    }

    public boolean hasAnyAddress(int userId) {
        String sql = "SELECT id FROM addresses WHERE user_id = ? LIMIT 1";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (Exception e) {
            logger.error("Error checking address existence for user id={}", userId, e);
        }
        return false;
    }

    public boolean updateAddress(int id, int userId, boolean isDefault, Timestamp updatedAt,
                              String address, String province, String district, String ward) {

        String resetSql = "UPDATE addresses SET is_default = 0 WHERE user_id = ?";
        String updateSql = "UPDATE addresses "
                + "SET is_default = ?, created_at = ?, address = ?, province = ?, district = ?, ward = ? "
                + "WHERE id = ? AND user_id = ?";

        try (Connection conn = DBContext.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (isDefault) {
                    try (PreparedStatement psReset = conn.prepareStatement(resetSql)) {
                        psReset.setInt(1, userId);
                        psReset.executeUpdate();
                    }
                }
                try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                    psUpdate.setBoolean(1, isDefault);
                    psUpdate.setTimestamp(2, updatedAt);
                    psUpdate.setString(3, address);
                    psUpdate.setString(4, province);
                    psUpdate.setString(5, district);
                    psUpdate.setString(6, ward);
                    psUpdate.setInt(7, id);
                    psUpdate.setInt(8, userId);
                    int updated = psUpdate.executeUpdate();
                    if (updated == 0) {
                        conn.rollback();
                        return false;
                    }
                }
                conn.commit();
                return true;
            } catch (Exception e) {
                logger.error("Error updating address id={} for user id={}", id, userId, e);
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception e) {
            logger.error("Error in updateAddress transaction id={} for user id={}", id, userId, e);
        }
        return false;
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
                a.setDefaultt(rs.getBoolean("is_default"));
                a.setAddress(rs.getString("address"));
                a.setCreateAt(rs.getTimestamp("created_at"));
                a.setProvince(rs.getString("province"));
                a.setDistrict(rs.getString("district"));
                a.setWard(rs.getString("ward"));
                return a;
            }
        } catch (Exception e) {
            logger.error("Error fetching address by id={} for user id={}", id, userId, e);
        }

        return null;
    }
    public boolean deleteAddress(int addressId, int userId) {
        String sql = "DELETE FROM addresses WHERE id = ? AND user_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, addressId);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            logger.error("Error deleting address id={} for user id={}", addressId, userId, e);
        }
        return false;
    }
    public boolean isDefaultAddress(int addressId, int userId) {
        String sql = "SELECT is_default FROM addresses WHERE id = ? AND user_id = ?";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, addressId);
            ps.setInt(2, userId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("is_default");
            }
        } catch (Exception e) {
            logger.error("Error checking if address id={} is default for user id={}", addressId, userId, e);
        }
        return false;
    }

    public void setNewestAddressAsDefault(int userId) {
        String sql = "UPDATE addresses SET is_default = 1 " +
                "WHERE id = (" +
                "   SELECT id FROM (" +
                "       SELECT id FROM addresses WHERE user_id = ? ORDER BY created_at DESC LIMIT 1" +
                "   ) t" +
                ")";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.executeUpdate();

        } catch (Exception e) {
            logger.error("Error setting newest address as default for user id={}", userId, e);
        }
    }
    public Address getDefaultAddressByUserId(int userId) {
        String sql = "SELECT * FROM addresses WHERE user_id = ? AND is_default = 1 LIMIT 1";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Address a = new Address();
                a.setId(rs.getInt("id"));
                a.setUserId(rs.getInt("user_id"));
                a.setDefaultt(rs.getBoolean("is_default"));
                a.setAddress(rs.getString("address"));
                a.setCreateAt(rs.getTimestamp("created_at"));
                a.setProvince(rs.getString("province"));
                a.setDistrict(rs.getString("district"));
                a.setWard(rs.getString("ward"));
                return a;
            }
        } catch (Exception e) {
            logger.error("Error fetching default address for user id={}", userId, e);
        }

        return null;
    }
}
