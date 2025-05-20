package com.mycompany.trust;

/**
 *
 * @author leo
 */
public class BerryEarningProposal extends Proposal {
    private int initialLevelOneBerryEarning;

    /**
     * Constructor for creating a berry earning proposal with title, description, and proposer
     */
    public BerryEarningProposal(String title, String description, String proposer) {
        super(title, description, proposer);
        this.initialLevelOneBerryEarning = 100; // Default value
    }
    
    /**
     * Constructor for creating a berry earning proposal with specific parameters
     */
    public BerryEarningProposal(String proposer, int initialLevelOneBerryEarning) {
        super("Berry Earning Proposal", "Proposal for adjusting the monthly berry earnings for Level 1 users", proposer);
        this.initialLevelOneBerryEarning = initialLevelOneBerryEarning;
    }

    // Getters and setters specific to BerryEarningProposal
    public int getInitialLevelOneBerryEarning() {
        return initialLevelOneBerryEarning;
    }

    public void setInitialLevelOneBerryEarning(int initialLevelOneBerryEarning) {
        this.initialLevelOneBerryEarning = initialLevelOneBerryEarning;
    }
    
    public void addVote() {
        setVotes(getVotes() + 1);
    }

    @Override
    public String toString() {
        return "Berry Earning Proposal #" + getId() + 
               " | Initial Level 1 Berry Earning: " + initialLevelOneBerryEarning + 
               " | Votes: " + getVotes() + 
               " | Proposed by: " + getProposer();
    }
}

