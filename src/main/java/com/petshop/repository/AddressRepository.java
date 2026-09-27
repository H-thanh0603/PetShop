package com.petshop.repository;

import com.petshop.model.Address;
import java.sql.Timestamp;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AddressRepository extends JpaRepository<Address, Integer> {

    @Query("SELECT a FROM Address a WHERE a.userId = :userId ORDER BY a.defaultt DESC, a.createAt DESC")
    List<Address> findByUserIdOrdered(@Param("userId") int userId);

    Address findByIdAndUserId(int id, int userId);

    @Query("SELECT a FROM Address a WHERE a.userId = :userId AND a.defaultt = true")
    List<Address> findDefaultByUserId(@Param("userId") int userId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Address a SET a.defaultt = false WHERE a.userId = :userId")
    void clearDefaults(@Param("userId") int userId);

    private static void rollbackOnly() {
        try {
            org.springframework.transaction.interceptor.TransactionAspectSupport
                    .currentTransactionStatus().setRollbackOnly();
        } catch (Exception ignored) {
            // No ambient transaction (should not happen for these methods).
        }
    }

    private static void discardFailedState() {
        // A failed flush poisons the persistence context for subsequent reads
        // in the same transaction; detach everything (rows already flushed stay
        // visible; rollbackOnly guarantees nothing commits).
        try {
            ProductRepositoryHolder.entityManager().clear();
        } catch (Exception ignored) {
            // No EntityManager available (should not happen under Spring).
        }
    }

    @Transactional
    default List<Address> getAddressesByUserId(int userId) {
        try {
            return findByUserIdOrdered(userId);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(AddressRepository.class)
                    .error("Error fetching addresses for user id={}", userId, e);
            return List.of();
        }
    }

    @Transactional
    default boolean setDefaultAddress(int userId, int addressId) {
        try {
            // Existence checked BEFORE any write: the false path writes nothing,
            // so no rollback is needed (matches old rollback+false net effect).
            Address address = findByIdAndUserId(addressId, userId);
            if (address == null) {
                return false;
            }
            clearDefaults(userId);
            address.setDefaultt(true);
            save(address);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(AddressRepository.class)
                    .error("Error setting default address id={} for user id={}", addressId, userId, e);
            rollbackOnly();
            return false;
        }
    }

    @Transactional
    default boolean addAddress(int userId, boolean defaultt, Timestamp createdAt,
                               String address, String province, String district, String ward) {
        try {
            if (defaultt) {
                clearDefaults(userId);
            }
            Address row = new Address();
            row.setUserId(userId);
            row.setDefaultt(defaultt);
            row.setCreateAt(createdAt);
            row.setAddress(address);
            row.setProvince(province);
            row.setDistrict(district);
            row.setWard(ward);
            save(row);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(AddressRepository.class)
                    .error("Error adding address for user id={}", userId, e);
            rollbackOnly();
            discardFailedState();
            return false;
        }
    }

    @Transactional
    default boolean hasAnyAddress(int userId) {
        try {
            return !findByUserIdOrdered(userId).isEmpty();
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(AddressRepository.class)
                    .error("Error checking address existence for user id={}", userId, e);
            return false;
        }
    }

    @Transactional
    default boolean updateAddress(int id, int userId, boolean isDefault, Timestamp updatedAt,
                                  String address, String province, String district, String ward) {
        try {
            Address row = findByIdAndUserId(id, userId);
            if (row == null) {
                return false;
            }
            if (isDefault) {
                clearDefaults(userId);
            }
            row.setDefaultt(isDefault);
            row.setCreateAt(updatedAt);
            row.setAddress(address);
            row.setProvince(province);
            row.setDistrict(district);
            row.setWard(ward);
            save(row);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(AddressRepository.class)
                    .error("Error updating address id={} for user id={}", id, userId, e);
            rollbackOnly();
            discardFailedState();
            return false;
        }
    }

    @Transactional
    default Address getAddressById(int id, int userId) {
        try {
            return findByIdAndUserId(id, userId);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(AddressRepository.class)
                    .error("Error fetching address by id={} for user id={}", id, userId, e);
            return null;
        }
    }

    @Transactional
    default boolean deleteAddress(int addressId, int userId) {
        try {
            Address row = findByIdAndUserId(addressId, userId);
            if (row == null) {
                return false;
            }
            delete(row);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(AddressRepository.class)
                    .error("Error deleting address id={} for user id={}", addressId, userId, e);
            return false;
        }
    }

    @Transactional
    default boolean isDefaultAddress(int addressId, int userId) {
        try {
            Address row = findByIdAndUserId(addressId, userId);
            return row != null && row.isDefaultt();
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(AddressRepository.class)
                    .error("Error checking if address id={} is default for user id={}", addressId, userId, e);
            return false;
        }
    }

    @Transactional
    default void setNewestAddressAsDefault(int userId) {
        try {
            List<Address> rows = findByUserIdOrdered(userId);
            Address newest = null;
            for (Address row : rows) {
                if (newest == null || (row.getCreateAt() != null && newest.getCreateAt() != null
                        && row.getCreateAt().after(newest.getCreateAt()))) {
                    newest = row;
                }
            }
            if (newest != null) {
                clearDefaults(userId);
                newest.setDefaultt(true);
                save(newest);
            }
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(AddressRepository.class)
                    .error("Error setting newest address as default for user id={}", userId, e);
        }
    }

    @Transactional
    default Address getDefaultAddressByUserId(int userId) {
        try {
            List<Address> rows = findDefaultByUserId(userId);
            return rows.isEmpty() ? null : rows.get(0);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(AddressRepository.class)
                    .error("Error fetching default address for user id={}", userId, e);
            return null;
        }
    }
}
