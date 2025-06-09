package com.mycompany.trust;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class for all proposal types in the system
 */
public abstract class Proposal {
    private int id;
    private String title;
    private String description;
    private String proposer;
    private String status;
    private Set<String> voters;
    private int votes;
    private Date creationDate;    /**
     * Constructor for creating a proposal with just a proposer (used by some child classes)
     */
    public Proposal(String proposer) {
        this.proposer = proposer;
        this.voters = new HashSet<>();
        this.votes = 0;
        this.status = "PENDING";
        this.creationDate = new Date();
    }/**
     * Constructor for creating a proposal with title, description, and proposer
     */
    public Proposal(String title, String description, String proposer) {
        this.title = title;
        this.description = description;
        this.proposer = proposer;
        this.voters = new HashSet<>();
        this.votes = 0;
        this.status = "PENDING";
        this.creationDate = new Date();
    }
    /**
     * Constructor for loading a proposal with a specific creation date
     */
    public Proposal(String title, String description, String proposer, Date creationDate) {
        this.title = title;
        this.description = description;
        this.proposer = proposer;
        this.voters = new HashSet<>();
        this.votes = 0;
        this.status = "PENDING";
        this.creationDate = creationDate != null ? creationDate : new Date();
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
    }    public void setVoters(Set<String> voters) {
        this.voters = voters;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    // Add these methods for UI compatibility
    public String getProposalType() {
        return this.getClass().getSimpleName();
    }
    public String getAuthorUsername() {
        return proposer;
    }
}
