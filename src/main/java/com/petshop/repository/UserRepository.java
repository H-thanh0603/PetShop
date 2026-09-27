package com.petshop.repository;

import com.petshop.model.StatusBooleanConverter;
import com.petshop.model.User;
import com.petshop.util.PasswordUtil;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, Integer> {

    User findByUsername(String username);

    User findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    @Query("SELECT u FROM User u WHERE u.email = :key OR u.username = :key")
    List<User> findByEmailOrUsername(@Param("key") String key);

    User findByResetToken(String token);

    User findByVerificationToken(String token);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    int countByRole(@Param("role") String role);

    List<User> findAllByOrderByIdDesc();

    List<User> findByRoleOrderByIdDesc(String role);

    @Query(value = "SELECT u.*, "
            + "(SELECT COUNT(*) FROM orders WHERE user_id = u.id) AS order_count, "
            + "(SELECT SUM(total_amount) FROM orders WHERE user_id = u.id AND status != 'Cancelled') AS total_spent "
            + "FROM users u ORDER BY u.id DESC",
            nativeQuery = true)
    List<Object[]> findAllWithStatsRaw();

    @Query(value = "SELECT u.*, "
            + "(SELECT COUNT(*) FROM orders WHERE user_id = u.id) AS order_count, "
            + "(SELECT SUM(total_amount) FROM orders WHERE user_id = u.id AND status != 'Cancelled') AS total_spent "
            + "FROM users u WHERE 1=1 "
            + "AND (:keyword IS NULL OR u.fullname LIKE CONCAT('%', :keyword, '%') OR u.email LIKE CONCAT('%', :keyword, '%') OR u.phone LIKE CONCAT('%', :keyword, '%') OR u.username LIKE CONCAT('%', :keyword, '%')) "
            + "AND (:role IS NULL OR u.role = :role) "
            + "ORDER BY u.id DESC",
            nativeQuery = true)
    List<Object[]> searchUsersRaw(@Param("keyword") String keyword, @Param("role") String role);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE User u SET u.discountUsed = true WHERE u.id = :userId")
    int markDiscountAsUsedInternal(@Param("userId") int userId);

    // --- contract-preserving default methods (same names/signatures as the DAO) ---

    @Transactional
    default User login(String username, String password) {
        try {
            User user = findByUsername(username);
            if (user != null && PasswordUtil.verifyPassword(password, user.getPassword())) {
                return user;
            }
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
        }
        return null;
    }

    @Transactional
    default User loginByEmail(String email, String password) {
        try {
            User user = findByEmail(email);
            if (user != null && PasswordUtil.verifyPassword(password, user.getPassword())) {
                return user;
            }
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
        }
        return null;
    }

    @Transactional
    default User loginByEmailOrUsername(String emailOrUsername, String password) {
        try {
            for (User user : findByEmailOrUsername(emailOrUsername)) {
                if (PasswordUtil.verifyPassword(password, user.getPassword())) {
                    return user;
                }
            }
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
        }
        return null;
    }

    default boolean checkUsernameExists(String username) {
        try {
            return existsByUsername(username);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return false;
        }
    }

    @Transactional
    default boolean register(String username, String password, String fullname, String email) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(PasswordUtil.hashPassword(password));
            user.setFullname(fullname);
            user.setEmail(email);
            user.setRole("user");
            user.setStatus(true); // match DB default 'active' (old INSERT omitted status)
            save(user);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return false;
        }
    }

    default int countUsers() {
        try {
            return (int) count();
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return 0;
        }
    }

    default User getUserById(int id) {
        try {
            return findById(id).orElse(null);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return null;
        }
    }

    @Transactional
    default boolean markDiscountAsUsed(int userId) {
        try {
            return markDiscountAsUsedInternal(userId) > 0;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return false;
        }
    }

    default String getEmailByUserId(int userId) {
        User user = getUserById(userId);
        return user != null ? user.getEmail() : null;
    }

    default User getUserByEmail(String email) {
        try {
            return findByEmail(email);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return null;
        }
    }

    default boolean checkEmailExists(String email) {
        return getUserByEmail(email) != null;
    }

    default boolean checkPhoneExists(String phone) {
        try {
            return existsByPhone(phone);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return false;
        }
    }

    @Transactional
    default boolean updatePassword(String email, String newPassword) {
        try {
            User user = findByEmail(email);
            if (user == null) {
                return false;
            }
            user.setPassword(PasswordUtil.hashPassword(newPassword));
            save(user);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return false;
        }
    }

    @Transactional
    default boolean saveResetToken(String email, String token) {
        try {
            User user = findByEmail(email);
            if (user == null) {
                return false;
            }
            user.setResetToken(token);
            user.setResetTokenExpiry(new Timestamp(System.currentTimeMillis() + 30 * 60 * 1000));
            save(user);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return false;
        }
    }

    default User getUserByResetToken(String token) {
        try {
            User user = findByResetToken(token);
            if (user != null && user.getResetTokenExpiry() != null
                    && user.getResetTokenExpiry().after(new Timestamp(System.currentTimeMillis()))) {
                return user;
            }
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
        }
        return null;
    }

    @Transactional
    default boolean clearResetToken(String email) {
        try {
            User user = findByEmail(email);
            if (user == null) {
                return false;
            }
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            save(user);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return false;
        }
    }

    default java.util.List<User> getAllUsers() {
        try {
            return findAllByOrderByIdDesc();
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return java.util.List.of();
        }
    }

    default java.util.List<User> getUsersByRole(String role) {
        try {
            return findByRoleOrderByIdDesc(role);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return java.util.List.of();
        }
    }

    @Transactional
    default boolean updateUserRole(int userId, String role) {
        try {
            User user = findById(userId).orElse(null);
            if (user == null) {
                return false;
            }
            user.setRole(role);
            save(user);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return false;
        }
    }

    @Transactional
    default boolean deleteUser(int userId) {
        try {
            deleteById(userId);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return false;
        }
    }

    @Transactional
    default boolean deactivateUser(int userId) {
        // Preserved verbatim: old code wrote literal 0 into the VARCHAR status.
        return updateStatusRaw(userId, "0");
    }

    default int countUsersByRole(String role) {
        try {
            return countByRole(role);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return 0;
        }
    }

    default User getUserFullById(int id) {
        return getUserById(id);
    }

    @Transactional
    default boolean updateUser(int userId, String fullname, String email, String phone, String address) {
        try {
            User user = findById(userId).orElse(null);
            if (user == null) {
                return false;
            }
            user.setFullname(fullname);
            user.setEmail(email);
            user.setPhone(phone);
            user.setAddress(address);
            save(user);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return false;
        }
    }

    default java.util.List<User> getAllUsersWithStats() {
        try {
            java.util.List<User> list = new java.util.ArrayList<>();
            for (Object[] row : findAllWithStatsRaw()) {
                list.add(mapStatsRow(row));
            }
            return list;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return java.util.List.of();
        }
    }

    default java.util.List<User> searchUsers(String keyword, String role) {
        try {
            java.util.List<User> list = new java.util.ArrayList<>();
            String kw = (keyword == null || keyword.isEmpty()) ? null : keyword;
            String rl = (role == null || role.isEmpty()) ? null : role;
            for (Object[] row : searchUsersRaw(kw, rl)) {
                list.add(mapStatsRow(row));
            }
            return list;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return java.util.List.of();
        }
    }

    default User mapStatsRow(Object[] row) {
        // SELECT u.* + order_count + total_spent; column order follows DESCRIBE users.
        User user = new User();
        user.setId(((Number) row[0]).intValue());
        user.setUsername((String) row[1]);
        user.setPassword((String) row[2]);
        user.setFullname((String) row[3]);
        user.setEmail((String) row[4]);
        user.setRole((String) row[5]);
        user.setStatus(new StatusBooleanConverter().convertToEntityAttribute((String) row[6]));
        user.setPhone((String) row[7]);
        user.setAddress((String) row[8]);
        user.setDiscountUsed(row[9] != null && ((Number) row[9]).intValue() != 0);
        user.setCreatedAt((Timestamp) row[17]);
        if (row.length > 18 && row[18] != null) {
            user.setOrderCount(((Number) row[18]).intValue());
        }
        if (row.length > 19 && row[19] != null) {
            user.setTotalSpent(new java.math.BigDecimal(row[19].toString()));
        }
        return user;
    }

    default int countNewUsersThisWeek() {
        try {
            return countNewUsersThisWeekNative();
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return 0;
        }
    }

    @Query(value = "SELECT COUNT(*) FROM users WHERE created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)",
            nativeQuery = true)
    int countNewUsersThisWeekNative();

    @Transactional
    default boolean updateUserStatus(int userId, String status) {
        // Preserved verbatim: old code wrote the raw string into VARCHAR status.
        return updateStatusRaw(userId, status);
    }

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE users SET status = :status WHERE id = :userId", nativeQuery = true)
    int updateStatusNative(@Param("userId") int userId, @Param("status") String status);

    @Transactional
    default boolean updateStatusRaw(int userId, String status) {
        try {
            return updateStatusNative(userId, status) > 0;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return false;
        }
    }

    @Transactional
    default boolean resetUserPassword(int userId, String newPassword) {
        if (!PasswordUtil.isStrongPassword(newPassword)) {
            return false;
        }
        try {
            User user = findById(userId).orElse(null);
            if (user == null) {
                return false;
            }
            user.setPassword(PasswordUtil.hashPassword(newPassword));
            save(user);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return false;
        }
    }

    @Transactional
    default boolean addUser(String username, String password, String fullname, String email, String phone, String role) {
        if (!PasswordUtil.isStrongPassword(password)) {
            return false;
        }
        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(PasswordUtil.hashPassword(password));
            user.setFullname(fullname);
            user.setEmail(email);
            user.setPhone(phone);
            user.setRole(role);
            user.setStatus(true);
            save(user);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return false;
        }
    }

    default boolean HaveEmail(String email) {
        try {
            return countByEmail(email) == 0;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return false;
        }
    }

    @Query("SELECT COUNT(u) FROM User u WHERE u.email = :email")
    int countByEmail(@Param("email") String email);

    @Transactional
    default void insertUser(String name, String email) {
        User user = new User();
        user.setUsername(buildUniqueUsername(name, email));
        user.setEmail(email);
        user.setFullname(name);
        user.setRole("user");
        user.setStatus(true);
        user.setPassword(PasswordUtil.hashPassword(UUID.randomUUID().toString()));
        save(user);
    }

    default String buildUniqueUsername(String name, String email) {
        String base = null;
        if (email != null && email.contains("@")) {
            base = email.substring(0, email.indexOf('@'));
        }
        if (base == null || base.isBlank()) {
            base = name != null ? name : "user";
        }
        base = base.toLowerCase()
                .replaceAll("[^a-z0-9_]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
        if (base.isBlank()) {
            base = "user";
        }
        String candidate = base;
        int suffix = 1;
        while (checkUsernameExists(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }

    @Transactional
    default void updateProfile(int id, String fullname, String phone) {
        try {
            User user = findById(id).orElse(null);
            if (user == null) {
                return;
            }
            user.setFullname(fullname);
            user.setPhone(phone);
            save(user);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
        }
    }

    @Transactional
    default boolean updateProfileAndEmail(int id, String fullname, String phone, String email) {
        try {
            User user = findById(id).orElse(null);
            if (user == null) {
                return false;
            }
            user.setFullname(fullname);
            user.setPhone(phone);
            user.setEmail(email);
            save(user);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return false;
        }
    }

    default boolean isEmailTakenByAnotherUser(String email, int userId) {
        try {
            User user = findByEmail(email);
            return user != null && user.getId() != userId;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return false;
        }
    }

    default boolean isPhoneTakenByAnotherUser(String phone, int userId) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        try {
            return existsByPhoneAndIdNot(phone, userId);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return false;
        }
    }

    boolean existsByPhoneAndIdNot(String phone, int userId);

    @Transactional
    default void migratePasswordsToBCrypt() {
        try {
            for (User user : findAll()) {
                String password = user.getPassword();
                if (password == null || password.isEmpty() || password.equals("null") || password.startsWith("$2")) {
                    continue;
                }
                user.setPassword(PasswordUtil.hashPassword(password));
                save(user);
            }
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
        }
    }

    default int getFailedLoginAttempts(String emailOrUsername) {
        try {
            List<User> users = findByEmailOrUsername(emailOrUsername);
            return users.isEmpty() ? 0 : users.get(0).getFailedLoginAttempts();
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return 0;
        }
    }

    default Timestamp getLockedUntil(String emailOrUsername) {
        try {
            List<User> users = findByEmailOrUsername(emailOrUsername);
            return users.isEmpty() ? null : users.get(0).getLockedUntil();
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return null;
        }
    }

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE users SET failed_login_attempts = failed_login_attempts + 1 WHERE email = :key OR username = :key",
            nativeQuery = true)
    int incrementFailedAttemptsNative(@Param("key") String key);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE users SET locked_until = DATE_ADD(NOW(), INTERVAL :minutes MINUTE) WHERE email = :key OR username = :key",
            nativeQuery = true)
    int lockAccountNative(@Param("key") String key, @Param("minutes") int minutes);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE users SET failed_login_attempts = 0, locked_until = NULL WHERE email = :key OR username = :key",
            nativeQuery = true)
    int resetFailedAttemptsNative(@Param("key") String key);

    @Transactional
    default void incrementFailedAttempts(String emailOrUsername) {
        try {
            incrementFailedAttemptsNative(emailOrUsername);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
        }
    }

    @Transactional
    default void lockAccount(String emailOrUsername, int lockoutMinutes) {
        try {
            lockAccountNative(emailOrUsername, lockoutMinutes);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
        }
    }

    @Transactional
    default void resetFailedAttempts(String emailOrUsername) {
        try {
            resetFailedAttemptsNative(emailOrUsername);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
        }
    }

    default boolean isAccountLocked(String emailOrUsername) {
        Timestamp lockedUntil = getLockedUntil(emailOrUsername);
        return lockedUntil != null && lockedUntil.after(new Timestamp(System.currentTimeMillis()));
    }

    @Transactional
    default void saveVerificationToken(int userId, String token, Timestamp expiry) {
        try {
            User user = findById(userId).orElse(null);
            if (user == null) {
                return;
            }
            user.setVerificationToken(token);
            user.setVerificationTokenExpiry(expiry);
            user.setEmailVerified(false);
            save(user);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
        }
    }

    default User getUserByVerificationToken(String token) {
        try {
            User user = findByVerificationToken(token);
            if (user != null && user.getVerificationTokenExpiry() != null
                    && user.getVerificationTokenExpiry().after(new Timestamp(System.currentTimeMillis()))) {
                return user;
            }
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
        }
        return null;
    }

    default User getUserByExpiredVerificationToken(String token) {
        try {
            User user = findByVerificationToken(token);
            if (user != null && (user.getVerificationTokenExpiry() == null
                    || !user.getVerificationTokenExpiry().after(new Timestamp(System.currentTimeMillis())))) {
                return user;
            }
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
        }
        return null;
    }

    @Transactional
    default boolean markEmailVerified(int userId) {
        try {
            User user = findById(userId).orElse(null);
            if (user == null) {
                return false;
            }
            user.setEmailVerified(true);
            user.setVerificationToken(null);
            user.setVerificationTokenExpiry(null);
            save(user);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return false;
        }
    }

    default boolean isEmailVerified(String email) {
        try {
            User user = findByEmail(email);
            return user != null && user.isEmailVerified();
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(UserRepository.class).error("DB error", e);
            return false;
        }
    }
}
