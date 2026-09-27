package com.petshop.repository;

import com.petshop.model.CustomerSupportKnowledge;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface CustomerSupportKnowledgeRepository extends JpaRepository<CustomerSupportKnowledge, Integer> {

    List<CustomerSupportKnowledge> findByIsActiveTrueOrderByIdAsc();

    List<CustomerSupportKnowledge> findAllByOrderByIdDesc();

    @Transactional
    default boolean create(CustomerSupportKnowledge item) {
        try {
            save(item);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(CustomerSupportKnowledgeRepository.class)
                    .error("Error creating knowledge base item", e);
            return false;
        }
    }

    default List<CustomerSupportKnowledge> getAllActive() {
        return findByIsActiveTrueOrderByIdAsc();
    }

    default List<CustomerSupportKnowledge> getAll() {
        return findAllByOrderByIdDesc();
    }

    default CustomerSupportKnowledge getById(int id) {
        return findById(id).orElse(null);
    }

    @Transactional
    default boolean update(CustomerSupportKnowledge item) {
        try {
            save(item);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(CustomerSupportKnowledgeRepository.class)
                    .error("Error updating knowledge base item id={}", item.getId(), e);
            return false;
        }
    }

    @Transactional
    default boolean delete(int id) {
        try {
            deleteById(id);
            return true;
        } catch (DataAccessException e) {
            LoggerFactory.getLogger(CustomerSupportKnowledgeRepository.class)
                    .error("Error deleting knowledge base item id={}", id, e);
            return false;
        }
    }
}
