package services.ai;

import Model.Product;
import org.junit.jupiter.api.Test;
import services.ai.common.AppEventBus;
import services.ai.common.Cards;
import services.ai.common.Fence;
import services.ai.common.MemoryService;
import services.ai.merchant.ChangeLedger;
import services.ai.merchant.MerchantTools;
import services.ai.merchant.PetShopMerchantBackend;
import services.ai.merchant.StagedChange;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Offline tests for the full commerce-agents port: skills, guardrails,
 * ledger lifecycle, memory filter, fencing, events, merchant gates.
 * No network, no DB.
 */
public class CommerceFullPortTest {

    @Test
    public void allTenSkillsLoad() {
        var shopping = SkillLoader.loadRole("shopping");
        var merchant = SkillLoader.loadRole("merchant");
        assertEquals(5, shopping.size());
        assertEquals(5, merchant.size());
        for (var s : shopping) assertFalse(s.body().isBlank(), s.name());
        for (var s : merchant) assertFalse(s.body().isBlank(), s.name());
        String prompt = SkillLoader.renderForPrompt(shopping);
        assertTrue(prompt.contains("search-discovery"));
        assertEquals(2, SkillLoader.roleSkillNames().size());
    }

    @Test
    public void ledgerStagesAndTransitions() {
        ChangeLedger ledger = new ChangeLedger();
        StagedChange c = ledger.stage(StagedChange.Kind.INVENTORY_ACTION, "restock",
                List.of(new StagedChange.Item("7", "stock", "3", "50")), "admin:1", List.of());
        assertEquals(StagedChange.Status.STAGED, c.getStatus());
        assertTrue(c.getChangeId().startsWith("chg-"));
        assertEquals(1, ledger.pending().size());

        ledger.apply(c.getChangeId(), "admin:1");
        assertEquals(StagedChange.Status.APPLIED, c.getStatus());
        assertTrue(ledger.pending().isEmpty());

        assertThrows(ChangeLedger.ChangeNotApplicable.class,
                () -> ledger.apply(c.getChangeId(), "admin:1"));
        assertThrows(ChangeLedger.ChangeNotApplicable.class,
                () -> ledger.discard("chg-9999", "admin:1"));
    }

    @Test
    public void guardrailsBlockOversizedMoves() {
        // Price move > 20%: 100 -> 200 grounding both sides.
        var violations = ChangeLedger.checkGuardrails(StagedChange.Kind.PRICE_UPDATE,
                List.of(new StagedChange.Item("1", "price", "100000", "200000")));
        assertFalse(violations.isEmpty());

        // Compliant 10% move passes.
        assertTrue(ChangeLedger.checkGuardrails(StagedChange.Kind.PRICE_UPDATE,
                List.of(new StagedChange.Item("1", "price", "100000", "110000"))).isEmpty());

        // Ungrounded price blocks the move (no silent default).
        assertFalse(ChangeLedger.checkGuardrails(StagedChange.Kind.PRICE_UPDATE,
                List.of(new StagedChange.Item("1", "price", "", "110000"))).isEmpty());

        // Restock over cap.
        assertFalse(ChangeLedger.checkGuardrails(StagedChange.Kind.INVENTORY_ACTION,
                List.of(new StagedChange.Item("1", "stock", "0", "99999"))).isEmpty());

        // Protected field.
        assertFalse(ChangeLedger.checkGuardrails(StagedChange.Kind.LISTING_UPDATE,
                List.of(new StagedChange.Item("1", "currency", "VND", "USD"))).isEmpty());

        // price/stock via listing update must route to their own tools.
        assertFalse(ChangeLedger.checkGuardrails(StagedChange.Kind.LISTING_UPDATE,
                List.of(new StagedChange.Item("1", "price", "100", "110"))).isEmpty());

        // Duplicate target+field in one change.
        assertFalse(ChangeLedger.checkGuardrails(StagedChange.Kind.INVENTORY_ACTION,
                List.of(new StagedChange.Item("1", "stock", "0", "10"),
                        new StagedChange.Item("1", "stock", "0", "10"))).isEmpty());

        // Promotion depth over 50%.
        assertFalse(ChangeLedger.checkGuardrails(StagedChange.Kind.PROMOTION,
                List.of(new StagedChange.Item("1", "price", "100000", "10000"))).isEmpty());
    }

    @Test
    public void ledgerStageRefusesGuardrailBreaks() {
        ChangeLedger ledger = new ChangeLedger();
        assertThrows(ChangeLedger.GuardrailViolation.class, () -> ledger.stage(
                StagedChange.Kind.PRICE_UPDATE, "too big",
                List.of(new StagedChange.Item("1", "price", "100000", "500000")), "admin:1", List.of()));
    }

    @Test
    public void memoryWriteFilter() {
        assertTrue(MemoryService.validateFact("pet", "owns a 2-year-old British Shorthair cat", "preference"));
        assertFalse(MemoryService.validateFact("k", "", "preference"));
        assertFalse(MemoryService.validateFact("x".repeat(65), "v", "preference"));
        assertFalse(MemoryService.validateFact("k", "x".repeat(201), "preference"));
        assertFalse(MemoryService.validateFact("k", "v", "unknown-category"));
        assertFalse(MemoryService.validateFact("password", "my password is 123", "context"));
        assertFalse(MemoryService.validateFact("contact", "email me at a@b.com", "context"));
    }

    @Test
    public void fenceSanitizesAndCaps() {
        assertFalse(Fence.sanitize("a\u0000b").contains("\u0000"));
        String fenced = Fence.fence("products", "hello");
        assertTrue(fenced.contains("[fenced:products]"));
        assertTrue(Fence.fence("x", "y".repeat(20000)).length() <= Fence.DEFAULT_MAX_CHARS + 100);
        assertEquals(4, Fence.sanitizeChips(List.of("a", "b", "c", "d", "e")).size());
    }

    @Test
    public void appEventsQueueAndDrain() {
        AppEventBus.publish("test-session", "order_completed", "orderId=42");
        var events = AppEventBus.drain("test-session");
        assertEquals(1, events.size());
        assertEquals("order_completed", events.get(0).type());
        assertTrue(AppEventBus.drain("test-session").isEmpty());
    }

    @Test
    public void merchantStageRequiresProvenance() {
        MerchantTools tools = new MerchantTools(new PetShopMerchantBackend(), "test");
        // Unknown listing id (never returned by a tool this session) is held.
        String held = tools.execute("stage_price_update",
                "{\"items\":[{\"productId\":424242,\"newPrice\":\"100000\"}]}");
        assertTrue(held.contains("provenance") || held.contains("not returned"));
        assertTrue(tools.execute("no_such_tool", "{}").contains("error"));
    }

    @Test
    public void cardsBuiltFromServerRecords() {
        Product p = new Product(7, "Pate mèo", "img.jpg", new BigDecimal("50000"), 10, "ngon");
        var card = Cards.productCard(p);
        assertEquals(7, card.get("id").getAsInt());
        assertTrue(card.get("url").getAsString().contains("7"));
        var checkout = Cards.checkoutCard(2, "100000", "/cart");
        assertEquals("checkout", checkout.get("type").getAsString());
    }
}
