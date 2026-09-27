package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import com.petshop.model.PetType;
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
class PetTypeRepositoryTest {

    @Autowired
    private PetTypeRepository repository;

    private PetType petType(String code) {
        PetType petType = new PetType();
        petType.setCode(code + "_" + Math.abs(System.nanoTime() % 100000));
        petType.setName("Name " + code);
        petType.setIcon("icon");
        petType.setDisplayOrder(1);
        petType.setActive(true);
        return petType;
    }

    @Test
    void addThenGetByCode() {
        PetType petType = petType("dog");
        assertTrue(repository.addPetType(petType));
        PetType found = repository.getPetTypeByCode(petType.getCode());
        assertNotNull(found);
        assertEquals(petType.getCode(), found.getCode());
    }

    @Test
    void getActiveExcludesInactiveAfterToggle() {
        PetType petType = petType("cat");
        repository.addPetType(petType);
        PetType saved = repository.getPetTypeByCode(petType.getCode());
        assertTrue(repository.togglePetTypeStatus(saved.getId(), false));
        assertNull(repository.getPetTypeByCode(saved.getCode()) == null ? null
                : repository.getActivePetTypes().stream()
                        .filter(p -> p.getId() == saved.getId()).findFirst().orElse(null));
    }

    @Test
    void updatePetTypeChangesName() {
        PetType petType = petType("bird");
        repository.addPetType(petType);
        PetType saved = repository.getPetTypeByCode(petType.getCode());
        saved.setName("Chim Canh Cut");
        assertTrue(repository.updatePetType(saved));
        assertEquals("Chim Canh Cut", repository.getPetTypeById(saved.getId()).getName());
    }
}
