package Model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderLogTest {

    @Test
    void keepsActorActionStatusAndNoteForAuditTimeline() {
        OrderLog log = new OrderLog();
        log.setOrderId(15);
        log.setActorType("ADMIN");
        log.setActorId(3);
        log.setAction("UPDATE_PAYMENT_VERIFICATION");
        log.setOldStatus("PENDING");
        log.setNewStatus("VERIFIED");
        log.setNote("Admin confirmed bank transfer");

        assertEquals(15, log.getOrderId());
        assertEquals("ADMIN", log.getActorType());
        assertEquals(3, log.getActorId());
        assertEquals("UPDATE_PAYMENT_VERIFICATION", log.getAction());
        assertEquals("PENDING", log.getOldStatus());
        assertEquals("VERIFIED", log.getNewStatus());
        assertEquals("Admin confirmed bank transfer", log.getNote());
    }
}
