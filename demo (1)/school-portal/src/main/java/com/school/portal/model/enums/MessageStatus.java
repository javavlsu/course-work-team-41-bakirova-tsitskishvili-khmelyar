package com.school.portal.model.enums;

public enum MessageStatus {
    NEW(0, "Новое"),
    READ(1, "Прочитано"),
    ARCHIVED(2, "Архив");

    private final int code;
    private final String displayName;

    MessageStatus(int code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public int getCode() { return code; }
    public String getDisplayName() { return displayName; }

    public static MessageStatus fromCode(int code) {
        for (MessageStatus status : values()) {
            if (status.code == code) return status;
        }
        return NEW;
    }
}