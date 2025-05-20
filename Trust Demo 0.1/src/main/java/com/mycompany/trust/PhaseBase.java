package com.mycompany.trust;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class for all phases that provides common functionality
 */
public abstract class PhaseBase implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Team members (usernames)
    protected transient ArrayList<String> team = new ArrayList<>();
    
    // Phase status flags
    private boolean completed = false;
    private boolean validated = false;
    
    // XP award tracking
    private boolean baseXpAwarded = false;
    private boolean bonusXpAwarded = false;
    
    // Community satisfaction index (0.0 to 100.0), -1 means unset
    private double satisfactionIndex = -1.0;
    
    // Team openings for this phase
    private int teamOpenings = 0;
    
    // --- NEW: Associated Fields of Expertise --- (Transient: Loaded from DB)
    private transient Set<Integer> associatedExpertiseIds = new HashSet<>();
    
    public ArrayList<String> getTeam() {
        if (this.team == null) this.team = new ArrayList<>(); // Defend against null if not transient
        return team;
    }
    
    public void setTeam(ArrayList<String> team) {
        this.team = team;
    }
    
    public void addTeamMember(User user) {
        if (this.team == null) this.team = new ArrayList<>();
        if (!this.team.contains(user.getUsername())) {
            this.team.add(user.getUsername());
        }
    }
    
    public int getTeamOpenings() {
        return teamOpenings;
    }
    
    public void setTeamOpenings(int teamOpenings) {
        this.teamOpenings = teamOpenings;
    }
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    
    public boolean isValidated() {
        return validated;
    }
    
    public void setValidated(boolean validated) {
        this.validated = validated;
    }
    
    public boolean isBaseXpAwarded() {
        return baseXpAwarded;
    }
    
    public void setBaseXpAwarded(boolean baseXpAwarded) {
        this.baseXpAwarded = baseXpAwarded;
    }
    
    public boolean isBonusXpAwarded() {
        return bonusXpAwarded;
    }
    
    public void setBonusXpAwarded(boolean bonusXpAwarded) {
        this.bonusXpAwarded = bonusXpAwarded;
    }
    
    public double getSatisfactionIndex() {
        return satisfactionIndex;
    }
    
    public void setSatisfactionIndex(double satisfactionIndex) {
        this.satisfactionIndex = satisfactionIndex;
    }
    
    // --- Getters/Setters for Expertise --- 
    public Set<Integer> getAssociatedExpertiseIds() {
        if (associatedExpertiseIds == null) {
            associatedExpertiseIds = new HashSet<>(); // Ensure not null
        }
        return associatedExpertiseIds;
    }
    
    public void setAssociatedExpertiseIds(Set<Integer> associatedExpertiseIds) {
        this.associatedExpertiseIds = (associatedExpertiseIds != null) ? associatedExpertiseIds : new HashSet<>();
    }
    
    public abstract void displayPhaseInfo();
}
