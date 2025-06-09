package com.mycompany.trust;

/**
 * Represents a requirement for a specific field of expertise
 * in a team opening.
 */
public class ExpertiseRequirement {
    private int expertiseId; // -1 represents "None" (general opening)
    private int count;
    private String description; // Added description field

    /**
     * Creates a new expertise requirement
     *
     * @param expertiseId The field of expertise ID (-1 for general/none)
     * @param count The number of openings required for this expertise
     */
    public ExpertiseRequirement(int expertiseId, int count) {
        this.expertiseId = expertiseId;
        this.count = count;
        // Initialize description to null or a default value if appropriate
        this.description = null;
    }

    // Constructor including description
    public ExpertiseRequirement(int expertiseId, int count, String description) {
        this.expertiseId = expertiseId;
        this.count = count;
        this.description = description;
    }

    /**
     * Gets the expertise ID
     *
     * @return The expertise ID (-1 for general/none)
     */
    public int getExpertiseId() {
        return expertiseId;
    }

    /**
     * Sets the expertise ID
     *
     * @param expertiseId The expertise ID (-1 for general/none)
     */
    public void setExpertiseId(int expertiseId) {
        this.expertiseId = expertiseId;
    }

    /**
     * Gets the count of openings required for this expertise
     *
     * @return The count of openings
     */
    public int getCount() {
        return count;
    }

    /**
     * Sets the count of openings required for this expertise
     *
     * @param count The count of openings
     */
    public void setCount(int count) {
        this.count = count;
    }

    // Getter and Setter for description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}