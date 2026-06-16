package Model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class AiChatMessage {
    private int id;
    private int sessionId;
    private String senderType; // USER, AI, ADMIN, SYSTEM
    private String message;
    private String intent;
    private BigDecimal confidence;
    private boolean needAdminSupport;
    private String suggestedAdminNote;
    private Timestamp createdAt;

    public AiChatMessage() {}

    public AiChatMessage(int id, int sessionId, String senderType, String message, String intent, BigDecimal confidence, boolean needAdminSupport, String suggestedAdminNote, Timestamp createdAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.senderType = senderType;
        this.message = message;
        this.intent = intent;
        this.confidence = confidence;
        this.needAdminSupport = needAdminSupport;
        this.suggestedAdminNote = suggestedAdminNote;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }

    public boolean isNeedAdminSupport() { return needAdminSupport; }
    public void setNeedAdminSupport(boolean needAdminSupport) { this.needAdminSupport = needAdminSupport; }

    public String getSuggestedAdminNote() { return suggestedAdminNote; }
    public void setSuggestedAdminNote(String suggestedAdminNote) { this.suggestedAdminNote = suggestedAdminNote; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
