package com.mycompany.trust;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for all proposal types in the system
 */
public abstract class Proposal {
    private int id;
    private String proposer;
    private Set<String> voters;
    private int votes;

    /**
     * Constructor for creating a proposal with just a proposer (used by some child classes)
     */
    public Proposal(String proposer) {
        this.proposer = proposer;
        this.voters = new HashSet<>();
        this.votes = 0;
    }

    /**
     * Constructor for creating a proposal with title, description, and proposer
     */
    public Proposal(String title, String description, String proposer) {
        this.proposer = proposer;
        this.voters = new HashSet<>();
        this.votes = 0;
    }

    /**
     * Add a vote from a user
     */
    public void addVote(String username) {
        if (!voters.contains(username)) {
            voters.add(username);
            votes++;
        }
    }

    /**
     * Check if a user has already voted for this proposal
     */
    public boolean hasVoted(String username) {
        return voters.contains(username);
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProposer() {
        return proposer;
    }

    public void setProposer(String proposer) {
        this.proposer = proposer;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }
    
    public Set<String> getVoters() {
        return voters;
    }

    public void setVoters(Set<String> voters) {
        this.voters = voters;
    }
}
