package com.mycompany.trust;

import java.sql.Timestamp;

/**
 * Represents a rating given by a user for a specific phase in a branch.
 */
public class Rating {
    private int id;
    private String raterUsername; // User who gave the rating
    // private String ratedUsername; // Removed
    private int branchId;
    private String phaseType; // e.g., DEVELOPMENT, PRODUCTION
    private int ratingValue; // e.g., 1-5 stars or 0-100 scale
    private String comment;
    private Timestamp timestamp;

    // Updated Constructor (without ratedUsername)
    public Rating(String raterUsername, /* String ratedUsername, */ int branchId, String phaseType, int ratingValue, String comment) {
        this.raterUsername = raterUsername;
        // this.ratedUsername = ratedUsername; // Removed
        this.branchId = branchId;
        this.phaseType = phaseType;
        this.ratingValue = ratingValue;
        this.comment = comment;
        // Timestamp will be set by the database or upon saving
    }

    // Getters (removed getRatedUsername)
    public int getId() { return id; }
    public String getRaterUsername() { return raterUsername; }
    // public String getRatedUsername() { return ratedUsername; } // Removed
    public int getBranchId() { return branchId; }
    public String getPhaseType() { return phaseType; }
    public int getRatingValue() { return ratingValue; }
    public String getComment() { return comment; }
    public Timestamp getTimestamp() { return timestamp; }

    // Setters (essential ones)
    public void setId(int id) { this.id = id; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    // Updated toString (without ratedUsername)
    @Override
    public String toString() {
        return "Rating{" +
               "id=" + id +
               ", rater='" + raterUsername + "'" +
               // ", rated='" + ratedUsername + "'" + // Removed
               ", branchId=" + branchId +
               ", phase='" + phaseType + "'" +
               ", value=" + ratingValue +
               ", comment='" + comment + "'" +
               ", timestamp=" + timestamp +
               '}';
    }
} 