package services.ai.merchant;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Staged-change guardrails + lifecycle port (merchant_agent/changes.py).
 * Guardrails run at stage time and again at apply time, against the config
 * in force at apply time. Pure logic — persistence lives in
 * {@link MerchantChangeDAO}.
 */
public class ChangeLedger {
    public static class GuardrailViolation extends RuntimeException {
        private final List<String> violations;
        public GuardrailViolation(List<String> violations) {
            super(String.join("; ", violations));
            this.violations = List.copyOf(violations);
        }
        public List<String> getViolations() { return violations; }
    }

    public static class ChangeNotApplicable extends RuntimeException {
        public ChangeNotApplicable(String msg) { super(msg); }
    }

    private final Map<String, StagedChange> changes = new LinkedHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger();

    /** Operator-readable messages for every guardrail broken; empty = may proceed. */
    public static List<String> checkGuardrails(StagedChange.Kind kind, List<StagedChange.Item> items) {
        List<String> violations = new ArrayList<>();
        if (items.size() > MerchantConfig.maxItemsPerChange()) {
            violations.add("change touches " + items.size() + " items, limit is "
                    + MerchantConfig.maxItemsPerChange() + " per change; split into separate changes");
        }
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (StagedChange.Item item : items) {
            String field = item.field() == null ? "" : item.field().toLowerCase();
            String target = item.target() == null ? "" : item.target();
            if (!seen.add(target + "|" + field)) {
                violations.add("'" + item.field() + "' on " + target
                        + " appears more than once in this change — stage one line per item");
            }
            if (MerchantConfig.protectedFields().contains(field)) {
                violations.add("field '" + item.field() + "' on " + target
                        + " is protected and cannot be changed by the assistant");
            }
            if (kind == StagedChange.Kind.LISTING_UPDATE
                    && MerchantConfig.listingUpdateBlockedFields().contains(field)) {
                violations.add("'" + item.field()
                        + "' cannot change through a listing update — stage it as a price update "
                        + "or inventory action so its own limits apply");
            }
            if (MerchantConfig.priceBearingFields().contains(field)
                    || kind == StagedChange.Kind.PROMOTION) {
                Double before = asPrice(item.before());
                Double after = asPrice(item.after());
                if (after == null) {
                    violations.add("price for " + target + " must be a positive amount");
                } else if (before == null) {
                    violations.add("price for " + target + " has no grounded current price — "
                            + "the movement cap cannot be checked");
                } else {
                    double deltaPct = Math.abs(after - before) / before * 100;
                    if (kind == StagedChange.Kind.PROMOTION
                            && deltaPct > MerchantConfig.maxPromotionDiscountPct()) {
                        violations.add("promotion move of " + Math.round(deltaPct) + "% on " + target
                                + " exceeds the " + Math.round(MerchantConfig.maxPromotionDiscountPct())
                                + "% promotion limit");
                    } else if (kind != StagedChange.Kind.PROMOTION
                            && deltaPct > MerchantConfig.maxPriceDeltaPct()) {
                        violations.add("price move of " + Math.round(deltaPct) + "% on " + target
                                + " exceeds the " + Math.round(MerchantConfig.maxPriceDeltaPct())
                                + "% per-change limit");
                    }
                }
            }
            if (kind == StagedChange.Kind.INVENTORY_ACTION) {
                int added = asQuantity(item.after()) - asQuantity(item.before());
                if (added > MerchantConfig.maxRestockQuantity()) {
                    violations.add("restock of " + added + " units on " + target + " exceeds the "
                            + MerchantConfig.maxRestockQuantity() + "-unit per-change limit");
                }
            }
            if (kind == StagedChange.Kind.CAMPAIGN && "budget".equals(field)) {
                Double budget = asPrice(item.after());
                if (budget != null && budget > MerchantConfig.maxCampaignBudget()) {
                    violations.add("campaign budget exceeds the per-change limit");
                }
            }
        }
        return violations;
    }

    public StagedChange stage(StagedChange.Kind kind, String summary,
                              List<StagedChange.Item> items, String actor, List<String> notes) {
        List<String> violations = checkGuardrails(kind, items);
        if (!violations.isEmpty()) throw new GuardrailViolation(violations);
        String id = String.format("chg-%04d", sequence.incrementAndGet());
        StagedChange change = new StagedChange(id, kind, summary, items, actor, notes);
        changes.put(id, change);
        return change;
    }

    public StagedChange get(String changeId) { return changes.get(changeId); }

    /**
     * Re-inserts a change hydrated from durable storage (restart recovery).
     * Advances the id sequence past any hydrated id so new ids never collide.
     */
    public void reattach(StagedChange change) {
        changes.put(change.getChangeId(), change);
        try {
            String num = change.getChangeId().replaceAll("\\D+", "");
            if (!num.isEmpty()) {
                sequence.updateAndGet(cur -> Math.max(cur, Integer.parseInt(num)));
            }
        } catch (Exception ignored) {}
    }

    public List<StagedChange> pending() {
        return changes.values().stream()
                .filter(c -> c.getStatus() == StagedChange.Status.STAGED).toList();
    }

    public StagedChange apply(String changeId, String actor) {
        StagedChange change = requireStaged(changeId, "apply");
        List<String> violations = checkGuardrails(change.getKind(), change.getItems());
        if (!violations.isEmpty()) throw new GuardrailViolation(violations);
        change.setStatus(StagedChange.Status.APPLIED);
        change.setAppliedBy(actor);
        change.setAppliedAt(System.currentTimeMillis());
        return change;
    }

    public StagedChange discard(String changeId, String actor) {
        StagedChange change = requireStaged(changeId, "discard");
        change.setStatus(StagedChange.Status.DISCARDED);
        change.setDiscardedBy(actor);
        change.setDiscardedAt(System.currentTimeMillis());
        return change;
    }

    StagedChange requireStaged(String changeId, String action) {
        StagedChange change = changes.get(changeId);
        if (change == null) throw new ChangeNotApplicable("no change '" + changeId + "' to " + action);
        if (change.getStatus() != StagedChange.Status.STAGED) {
            throw new ChangeNotApplicable("change " + changeId + " is " + change.getStatus()
                    + ", not staged — nothing to " + action);
        }
        return change;
    }

    static Double asPrice(String value) {
        try {
            double d = Double.parseDouble(value.trim());
            return d > 0 ? d : null;
        } catch (Exception e) {
            return null;
        }
    }

    static int asQuantity(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
