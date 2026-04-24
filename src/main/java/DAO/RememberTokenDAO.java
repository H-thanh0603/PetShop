package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import Context.DBContext;
import Util.PasswordUtil;

/**
 * Manages secure remember-me tokens.
 * Tokens are stored as BCrypt hashes; plain tokens are only in cookies.
 */
public class RememberTokenDAO {

    /**
     * Store a hashed token for the user with a 7-day expiry.
     */
    public boolean saveToken(int userId, String plainToken) {
        String hash = PasswordUtil.hashPassword(plainToken);
        String sql = "INSERT INTO remember_tokens (user_id, token_hash, expires_at) " +
                     "VALUES (?, ?, DATE_ADD(NOW(), INTERVAL 7 DAY))";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, hash);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Find a non-expired token record that matches the plain token.
     * Returns the token record id if found, -1 otherwise.
     * Also returns the userId via out-param pattern using int[1].
     */
    public int findMatchingToken(String plainToken, int[] outUserId) {
        String sql = "SELECT id, user_id, token_hash FROM remember_tokens WHERE expires_at > NOW()";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String storedHash = rs.getString("token_hash");
                if (PasswordUtil.verifyPassword(plainToken, storedHash)) {
                    outUserId[0] = rs.getInt("user_id");
                    return rs.getInt("id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Delete a specific token record (used after token rotation).
     */
    public void deleteToken(int tokenId) {
        String sql = "DELETE FROM remember_tokens WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tokenId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete all tokens for a user (on logout).
     */
    public void deleteAllTokensForUser(int userId) {
        String sql = "DELETE FROM remember_tokens WHERE user_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Clean up expired tokens (can be called periodically).
     */
    public void deleteExpiredTokens() {
        String sql = "DELETE FROM remember_tokens WHERE expires_at <= NOW()";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
