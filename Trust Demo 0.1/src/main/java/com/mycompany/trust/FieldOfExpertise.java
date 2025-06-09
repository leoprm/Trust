package com.mycompany.trust;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a Field of Expertise within the system.
 */
public class FieldOfExpertise implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String description;
    private Integer parentId; // Added parentId field (use Integer to allow null)
    private boolean active = true; // Add active field with default value true

    // Default constructor (needed for some frameworks/serialization)
    public FieldOfExpertise() { }

    // Constructor for creating new ones (DB generates ID)
    public FieldOfExpertise(String name, String description, Integer parentId) { // Added parentId
        this.name = name;
        this.description = description;
        this.parentId = parentId;
    }

    // Constructor for loading from DB
    public FieldOfExpertise(int id, String name, String description, Integer parentId) { // Added parentId
        this.id = id;
        this.name = name;
        this.description = description;
        this.parentId = parentId;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getParentId() { return parentId; } // Added getter

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setParentId(Integer parentId) { this.parentId = parentId; } // Added setter

    @Override
    public String toString() {
        return name != null ? name : "FieldOfExpertise [id=" + id + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldOfExpertise that = (FieldOfExpertise) o;
        return id == that.id; // Equality based on ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    /**
     * Checks if this field of expertise is active
     * @return true if active, false if inactive
     */
    public boolean isActive() {
        return this.active;
    }
    
    /**
     * Sets the active status of this field of expertise
     * @param active true to activate, false to deactivate
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}