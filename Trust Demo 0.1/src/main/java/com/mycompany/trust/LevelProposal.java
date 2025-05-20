package com.mycompany.trust;

public class LevelProposal extends Proposal {
    private double xpIncreasePercentage;
    private double xpThreshold;

    /**
     * Constructor for creating a level proposal with title, description, and proposer
     */
    public LevelProposal(String title, String description, String proposer) {
        super(title, description, proposer);
        this.xpIncreasePercentage = 30.0; // Default value
        this.xpThreshold = 40.0; // Default value
    }
    
    /**
     * Constructor for creating a level proposal with specific parameters
     */
    public LevelProposal(String proposer, double xpIncreasePercentage, double xpThreshold) {
        super("Level System Proposal", "Proposal for adjusting the XP requirements for leveling up", proposer);
        this.xpIncreasePercentage = xpIncreasePercentage;
        this.xpThreshold = xpThreshold;
    }

    // Getters and setters specific to LevelProposal
    public double getXpIncreasePercentage() {
        return xpIncreasePercentage;
    }

    public void setXpIncreasePercentage(double xpIncreasePercentage) {
        this.xpIncreasePercentage = xpIncreasePercentage;
    }

    public double getXpThreshold() {
        return xpThreshold;
    }

    public void setXpThreshold(double xpThreshold) {
        this.xpThreshold = xpThreshold;
    }

    @Override
    public String toString() {
        return "Level Proposal #" + getId() + 
               " | XP Increase: " + xpIncreasePercentage + 
               "% | XP Threshold: " + xpThreshold + 
               "% | Votes: " + getVotes() + 
               " | Proposed by: " + getProposer();
    }
}

