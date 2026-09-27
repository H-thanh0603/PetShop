package com.petshop.repository;

import com.petshop.model.RememberToken;
import com.petshop.util.PasswordUtil;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RememberTokenRepository extends JpaRepository<RememberToken, Integer> {

    List<RememberToken> findByExpiresAtAfter(LocalDateTime now);

    List<RememberToken> findByUserId(Integer userId);

    @Transactional
    default boolean saveToken(int userId, String plainToken) {
        try {
            RememberToken token = new RememberToken();
            token.setUserId(userId);
            token.setTokenHash(PasswordUtil.hashPassword(plainToken));
            token.setExpiresAt(LocalDateTime.now().plusDays(7));
            save(token);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(RememberTokenRepository.class)
                    .error("Error saving remember token for user id={}", userId, e);
            return false;
        }
    }

    default int findMatchingToken(String plainToken, int[] outUserId) {
        for (RememberToken token : findByExpiresAtAfter(LocalDateTime.now())) {
            if (PasswordUtil.verifyPassword(plainToken, token.getTokenHash())) {
                outUserId[0] = token.getUserId();
                return token.getId();
            }
        }
        return -1;
    }

    @Transactional
    default void deleteToken(int tokenId) {
        try {
            deleteById(tokenId);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(RememberTokenRepository.class)
                    .error("Error deleting remember token id={}", tokenId, e);
        }
    }

    @Modifying
    @Transactional
    @Query("DELETE FROM RememberToken t WHERE t.userId = :userId")
    void deleteAllTokensForUser(@Param("userId") int userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RememberToken t WHERE t.expiresAt <= :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}
