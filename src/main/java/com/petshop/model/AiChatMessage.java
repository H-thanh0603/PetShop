package com.petshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "ai_chat_messages")
public class AiChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "session_id", nullable = false)
    private int sessionId;
    @Column(name = "sender_type", nullable = false, length = 20)
    private String senderType; // USER, AI, ADMIN, SYSTEM
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
    @Column(name = "intent", length = 50)
    private String intent;
    @Column(name = "confidence", precision = 4, scale = 2)
    private BigDecimal confidence;
    @Column(name = "need_admin_support")
    private boolean needAdminSupport;
    @Column(name = "suggested_admin_note", columnDefinition = "TEXT")
    private String suggestedAdminNote;
    @Column(name = "created_at", insertable = false, updatable = false)
    private Timestamp createdAt;
    @Column(name = "is_read", insertable = false, updatable = false)
    private boolean read;

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
