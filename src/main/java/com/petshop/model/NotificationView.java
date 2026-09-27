package com.petshop.model;

import java.sql.Timestamp;

public interface NotificationView {
    Integer getId();
    Integer getUserId();
    String getTitle();
    String getMessage();
    String getType();
    String getLink();
    Boolean getRead();
    Timestamp getCreatedAt();
}
