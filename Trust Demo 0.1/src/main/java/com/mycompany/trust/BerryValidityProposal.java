package com.mycompany.trust;

import java.util.Date;

public class BerryValidityProposal extends Proposal {
    private int validityMonths;

    /**
     * Constructor for creating a berry validity proposal with title, description, and proposer
     */
    public BerryValidityProposal(String title, String description, String proposer) {
        super(title, description, proposer);
        this.validityMonths = 12; // Default value
    }
    
    /**
     * Constructor for creating a berry validity proposal with specific parameters
     */
    public BerryValidityProposal(String proposer, int validityMonths) {
        super("Berry Validity Proposal", "Proposal for adjusting how long berries remain valid before expiring", proposer);
        this.validityMonths = validityMonths;
    }

    /**
     * Constructor for loading from database with creation date
     */
    public BerryValidityProposal(String proposer, int validityMonths, Date creationDate) {
        super("Berry Validity Proposal", "Proposal for adjusting how long berries remain valid before expiring", proposer, creationDate);
        this.validityMonths = validityMonths;
    }

    // Getters and setters specific to BerryValidityProposal
    public int getMonths() {
        return validityMonths;
    }
    
    // Add this method to match the call in TrustSystem.java
    public int getValidityMonths() {
        return validityMonths;
    }

    public void setValidityMonths(int validityMonths) {
        this.validityMonths = validityMonths;
    }
    
    // Add this method for UI compatibility
    public int getValidityDays() {
        return validityMonths * 30;
    }

    public void addVote() {
        setVotes(getVotes() + 1);
    }

    @Override
    public String toString() {
        return "Berry Validity Proposal #" + getId() + 
               " | Validity Period: " + validityMonths + " months" +
               " | Votes: " + getVotes() + 
               " | Proposed by: " + getProposer();
    }
}
