package com.mycompany.trust;

import java.io.Serializable;

/**
 * A proposal type that defines thresholds for determining whether a Branch is a Need or a Desire
 */
public class NeedThresholdProposal extends Proposal implements Serializable {
    private double globalThresholdPercent; // Percentage of total need points threshold
    private double personalThresholdPercent; // Percentage of individual user points threshold
    private int timeLimit; // Time limit in months for branch to reach threshold
    // Removed branchId field as the proposal now applies to all branches

    /**
     * Constructor for creating a need threshold proposal with title, description, and proposer
     */
    public NeedThresholdProposal(String title, String description, String proposer) {
        super(title, description, proposer);
        this.globalThresholdPercent = 25.0; // Default value
        this.personalThresholdPercent = 15.0; // Default value
        this.timeLimit = 3; // Default value in months
    }
    
    /**
     * Constructor for creating a need threshold proposal with specific parameters
     */
    public NeedThresholdProposal(String proposer, double globalThresholdPercent, double personalThresholdPercent, int timeLimit) {
        super("Need Threshold Proposal", "Proposal for defining when a Branch is considered a Need vs a Desire", proposer);
        this.globalThresholdPercent = globalThresholdPercent;
        this.personalThresholdPercent = personalThresholdPercent;
        this.timeLimit = timeLimit;
    }
    
    /**
     * Constructor with ID for database loading
     */
    public NeedThresholdProposal(int id, String proposer, double globalThresholdPercent, double personalThresholdPercent, int timeLimit, int votes) {
        super(proposer);
        setId(id);
        this.globalThresholdPercent = globalThresholdPercent;
        this.personalThresholdPercent = personalThresholdPercent;
        this.timeLimit = timeLimit;
        setVotes(votes);
    }
    
    // Getters and setters
    public double getGlobalThresholdPercent() {
        return globalThresholdPercent;
    }

    public void setGlobalThresholdPercent(double globalThresholdPercent) {
        this.globalThresholdPercent = globalThresholdPercent;
    }

    public double getPersonalThresholdPercent() {
        return personalThresholdPercent;
    }

    public void setPersonalThresholdPercent(double personalThresholdPercent) {
        this.personalThresholdPercent = personalThresholdPercent;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }
    
    public void addVote() {
        setVotes(getVotes() + 1);
    }
    
    /**
     * Add a vote from a specific user
     * 
     * @param username The username of the voter
     */
    public void addVote(String username) {
        if (getVoters() == null) {
            setVoters(new java.util.HashSet<>());
        }
        getVoters().add(username);
    }

    @Override
    public String toString() {
        return "Need Threshold Proposal #" + getId() + 
               " | Global: " + globalThresholdPercent + "%" +
               " | Personal: " + personalThresholdPercent + "%" +
               " | Time Limit: " + timeLimit + " months" +
               " | Applies to all branches" +
               " | Votes: " + getVotes() + 
               " | Proposed by: " + getProposer();
    }
}