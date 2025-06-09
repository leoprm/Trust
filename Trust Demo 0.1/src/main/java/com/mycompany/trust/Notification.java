package com.mycompany.trust;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a notification/job alert message for a user.
 */
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id; // Database ID
    private String username; // User receiving the notification
    private String message;
    private Timestamp timestamp;
    private boolean isRead;
    private Integer relatedBranchId; // Optional: Link to a branch
    private java.time.LocalDateTime date;
    private String actionUrl;

    // Constructor for creating new notifications
    public Notification(String username, String message, Integer relatedBranchId) {
        this.username = username;
        this.message = message;
        this.timestamp = Timestamp.from(Instant.now());
        this.isRead = false;
        this.relatedBranchId = relatedBranchId;
        this.date = java.time.LocalDateTime.now();
    }

    // Constructor for loading from database
    public Notification(int id, String username, String message, Timestamp timestamp, boolean isRead, Integer relatedBranchId) {
        this.id = id;
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.relatedBranchId = relatedBranchId;
        this.date = timestamp.toLocalDateTime();
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getMessage() { return message; }
    public Timestamp getTimestamp() { return timestamp; }
    public boolean isRead() { return isRead; }
    public Integer getRelatedBranchId() { return relatedBranchId; }
    /**
     * Returns the date/time of the notification as a LocalDateTime.
     */
    public java.time.LocalDateTime getDate() { return date; }
    public String getActionUrl() {
        return actionUrl;
    }

    // Setters (primarily for marking as read)
    public void setRead(boolean read) { isRead = read; }
    public void setDate(java.time.LocalDateTime date) { this.date = date; }
    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    @Override
    public String toString() {
        // Simple representation, adjust if needed for ListView display
        return timestamp.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " - " + message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}