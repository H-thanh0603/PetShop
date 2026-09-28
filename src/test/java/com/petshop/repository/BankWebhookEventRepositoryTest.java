package com.petshop.repository;

import com.petshop.config.JpaTestConfig;
import com.petshop.model.BankWebhookEvent;
import java.math.BigDecimal;
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
class BankWebhookEventRepositoryTest {

    @Autowired
    private BankWebhookEventRepository repository;

    @Test
    void saveThenFindByProviderTransactionId() {
        String txnId = "TXN-" + System.nanoTime();
        int id = repository.save(BankWebhookEvent.Status.UNMATCHED, txnId,
                BigDecimal.valueOf(250), "content here", "VCB", null, "{}");
        assertTrue(id > 0);
        BankWebhookEvent found = repository.findByProviderTransactionId(txnId);
        assertNotNull(found);
        assertEquals("UNMATCHED", found.getStatus());
        assertEquals(0, BigDecimal.valueOf(250).compareTo(found.getAmount()));
        assertNull(repository.findByProviderTransactionId("NO-SUCH-" + System.nanoTime()));
    }
}
