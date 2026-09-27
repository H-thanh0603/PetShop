package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import com.petshop.model.Address;
import java.sql.Timestamp;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AddressRepositoryTest {

    @Autowired
    private AddressRepository repository;

    @Autowired
    private javax.sql.DataSource dataSource;

    private int seedUser() throws Exception {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            long stamp = System.nanoTime();
            stmt.executeUpdate("INSERT INTO users (username, password) VALUES ('addruser_" + stamp + "', 'x')",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            try (java.sql.ResultSet keys = stmt.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }

    @Test
    void addThenListAddresses() throws Exception {
        int userId = seedUser();
        assertTrue(repository.addAddress(userId, true, now(), "123 Street", "HCM", "Q1", "Ward A"));
        assertTrue(repository.addAddress(userId, false, now(), "456 Street", "HCM", "Q2", "Ward B"));
        List<Address> list = repository.getAddressesByUserId(userId);
        assertEquals(2, list.size());
        // Default first (is_default DESC).
        assertTrue(list.get(0).isDefaultt());
    }

    @Test
    void setDefaultSwitchesAtomically() throws Exception {
        int userId = seedUser();
        repository.addAddress(userId, true, now(), "A", "P", "D", "W");
        repository.addAddress(userId, false, now(), "B", "P", "D", "W");
        int secondId = repository.getAddressesByUserId(userId).stream()
                .filter(a -> !a.isDefaultt()).findFirst().orElseThrow().getId();
        assertTrue(repository.setDefaultAddress(userId, secondId));
        List<Address> list = repository.getAddressesByUserId(userId);
        assertEquals(1, list.stream().filter(Address::isDefaultt).count());
        assertEquals(secondId, repository.getDefaultAddressByUserId(userId).getId());
    }

    @Test
    void setDefaultMissingAddressReturnsFalseAndKeepsOldDefault() throws Exception {
        int userId = seedUser();
        repository.addAddress(userId, true, now(), "A", "P", "D", "W");
        int oldDefault = repository.getDefaultAddressByUserId(userId).getId();
        assertFalse(repository.setDefaultAddress(userId, -999));
        // Rollback pin: the old default survives the failed switch.
        assertNotNull(repository.getDefaultAddressByUserId(userId));
        assertEquals(oldDefault, repository.getDefaultAddressByUserId(userId).getId());
    }

    @Test
    void addAddressWithBadUserReturnsFalseAndPersistsNothing() {
        // FK violation on insert -> false; no partial rows.
        assertEquals(false, repository.addAddress(-999, true, now(), "X", "P", "D", "W"));
        assertTrue(repository.getAddressesByUserId(-999).isEmpty());
    }

    @Test
    void updateAndDeleteAddress() throws Exception {
        int userId = seedUser();
        repository.addAddress(userId, false, now(), "A", "P", "D", "W");
        int id = repository.getAddressesByUserId(userId).get(0).getId();
        assertTrue(repository.updateAddress(id, userId, true, now(), "A2", "P2", "D2", "W2"));
        assertEquals("A2", repository.getAddressById(id, userId).getAddress());
        assertTrue(repository.isDefaultAddress(id, userId));
        assertTrue(repository.deleteAddress(id, userId));
        assertNull(repository.getAddressById(id, userId));
        assertFalse(repository.hasAnyAddress(userId));
    }

    @Test
    void setNewestAddressAsDefault() throws Exception {
        int userId = seedUser();
        repository.addAddress(userId, false, new Timestamp(System.currentTimeMillis() - 60000), "Old", "P", "D", "W");
        repository.addAddress(userId, false, now(), "New", "P", "D", "W");
        repository.setNewestAddressAsDefault(userId);
        assertEquals("New", repository.getDefaultAddressByUserId(userId).getAddress());
    }
}
