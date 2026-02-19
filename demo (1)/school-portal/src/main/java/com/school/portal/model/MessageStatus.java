package com.school.portal.model;

public enum MessageStatus {
    NEW("Новое"),
    READ("Прочитано"),
    ARCHIVED("Архив");

    private final String displayName;

    MessageStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}