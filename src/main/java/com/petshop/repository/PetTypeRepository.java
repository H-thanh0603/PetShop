package com.petshop.repository;

import com.petshop.model.PetType;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface PetTypeRepository extends JpaRepository<PetType, Integer> {

    List<PetType> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<PetType> findAllByOrderByDisplayOrderAsc();

    PetType findByCode(String code);

    @Transactional
    default List<PetType> getActivePetTypes() {
        try {
            return findByIsActiveTrueOrderByDisplayOrderAsc();
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(PetTypeRepository.class).error("DB error", e);
            return List.of();
        }
    }

    default List<PetType> getAllPetTypes() {
        return findAllByOrderByDisplayOrderAsc();
    }

    default PetType getPetTypeByCode(String code) {
        try {
            return findByCode(code);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(PetTypeRepository.class).error("DB error", e);
            return null;
        }
    }

    default PetType getPetTypeById(int id) {
        try {
            return findById(id).orElse(null);
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(PetTypeRepository.class).error("DB error", e);
            return null;
        }
    }

    @Transactional
    default boolean addPetType(PetType petType) {
        try {
            save(petType);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(PetTypeRepository.class).error("DB error", e);
            return false;
        }
    }

    @Transactional
    default boolean updatePetType(PetType petType) {
        return addPetType(petType);
    }

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE PetType p SET p.isActive = :active WHERE p.id = :id")
    int setActive(@Param("id") int id, @Param("active") boolean active);

    @Transactional
    default boolean togglePetTypeStatus(int id, boolean isActive) {
        try {
            return setActive(id, isActive) > 0;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(PetTypeRepository.class).error("DB error", e);
            return false;
        }
    }
}
