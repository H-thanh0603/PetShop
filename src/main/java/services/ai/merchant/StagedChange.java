package services.ai.merchant;

import java.util.List;

/** Staged-change model port (merchant_agent/types.py + changes.py lifecycle). */
public final class StagedChange {
    public enum Kind { LISTING_UPDATE, PRICE_UPDATE, INVENTORY_ACTION, PROMOTION, CAMPAIGN }
    public enum Status { STAGED, APPLIED, DISCARDED }

    public record Item(String target, String field, String before, String after) {}

    private final String changeId;
    private final Kind kind;
    private Status status;
    private final String summary;
    private final List<Item> items;
    private final String createdBy;
    private final long createdAt;
    private final List<String> guardrailNotes;
    private String approvedBy;
    private Long approvedAt;
    private String appliedBy;
    private Long appliedAt;
    private String discardedBy;
    private Long discardedAt;

    public StagedChange(String changeId, Kind kind, String summary, List<Item> items,
                        String createdBy, List<String> notes) {
        this.changeId = changeId;
        this.kind = kind;
        this.status = Status.STAGED;
        this.summary = summary == null || summary.length() <= 200 ? summary
                : summary.substring(0, 200);
        this.items = List.copyOf(items);
        this.createdBy = createdBy;
        this.createdAt = System.currentTimeMillis();
        this.guardrailNotes = notes == null ? List.of() : List.copyOf(notes);
    }

    public String getChangeId() { return changeId; }
    public Kind getKind() { return kind; }
    public Status getStatus() { return status; }
    public void setStatus(Status s) { this.status = s; }
    public String getSummary() { return summary; }
    public List<Item> getItems() { return items; }
    public String getCreatedBy() { return createdBy; }
    public long getCreatedAt() { return createdAt; }
    public List<String> getGuardrailNotes() { return guardrailNotes; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String b) { this.approvedBy = b; }
    public Long getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Long t) { this.approvedAt = t; }
    public String getAppliedBy() { return appliedBy; }
    public void setAppliedBy(String b) { this.appliedBy = b; }
    public Long getAppliedAt() { return appliedAt; }
    public void setAppliedAt(Long t) { this.appliedAt = t; }
    public String getDiscardedBy() { return discardedBy; }
    public void setDiscardedBy(String b) { this.discardedBy = b; }
    public Long getDiscardedAt() { return discardedAt; }
    public void setDiscardedAt(Long t) { this.discardedAt = t; }
}
