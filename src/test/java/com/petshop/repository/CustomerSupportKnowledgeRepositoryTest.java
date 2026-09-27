package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import com.petshop.model.CustomerSupportKnowledge;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerSupportKnowledgeRepositoryTest {

    @Autowired
    private CustomerSupportKnowledgeRepository repository;

    private CustomerSupportKnowledge item(String title, boolean active) {
        CustomerSupportKnowledge knowledge = new CustomerSupportKnowledge();
        knowledge.setTitle(title);
        knowledge.setCategory("FAQ");
        knowledge.setContent("content");
        knowledge.setActive(active);
        return knowledge;
    }

    @Test
    void createThenGetById() {
        assertTrue(repository.create(item("How to order", true)));
        assertEquals(1, repository.getAllActive().stream()
                .filter(k -> "How to order".equals(k.getTitle())).count());
    }

    @Test
    void getAllActiveExcludesInactive() {
        repository.create(item("Active item", true));
        repository.create(item("Inactive item", false));
        assertTrue(repository.getAllActive().stream().noneMatch(k -> "Inactive item".equals(k.getTitle())));
        assertTrue(repository.getAll().stream().anyMatch(k -> "Inactive item".equals(k.getTitle())));
    }

    @Test
    void updateThenDelete() {
        CustomerSupportKnowledge knowledge = item("To change", true);
        repository.create(knowledge);
        CustomerSupportKnowledge saved = repository.getAll().stream()
                .filter(k -> "To change".equals(k.getTitle())).findFirst().orElseThrow();
        saved.setTitle("Changed");
        assertTrue(repository.update(saved));
        assertNotNull(repository.getById(saved.getId()));
        assertEquals("Changed", repository.getById(saved.getId()).getTitle());
        assertTrue(repository.delete(saved.getId()));
        assertNull(repository.getById(saved.getId()));
    }

    @Test
    void createWithNullTitleReturnsFalseOnConstraintViolation() {
        // Contract pin (Review Focus): NOT NULL violation -> false, not throw.
        CustomerSupportKnowledge bad = item("x", true);
        bad.setTitle(null);
        assertEquals(false, repository.create(bad));
    }
}
