/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.trust;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author leo
 */

public class Idea {
    private static int idCounter;
    private int id;
    private String name;
    private String description;
    private String author;
    private Set<String> supporters;
    private int voteCount;
    private Set<Integer> associatedNeedIds; // Store associated need IDs
    private Set<Integer> branches; // Add this field to store branch IDs
    private String status;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the voteCount
     */
    public int getVoteCount() {
        return voteCount;
    }

    /**
     * @param voteCount the voteCount to set
     */
    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    /**
     * @return the supporters
     */
    public Set<String> getSupporters() {
        return supporters;
    }

    /**
     * @param aSupporters the supporters to set
     */
    public void setSupporters(Set<String> aSupporters) {
        supporters = aSupporters;
    }
    
    /**
     * @return the associated need IDs
     */
    public Set<Integer> getAssociatedNeedIds() {
        return associatedNeedIds;
    }
    
    /**
     * @param needIds the need IDs to set
     */
    public void setAssociatedNeedIds(Set<Integer> needIds) {
        this.associatedNeedIds = needIds;
    }
    
    /**
     * Add an associated need ID
     * @param needId the need ID to add
     */
    public void addAssociatedNeedId(int needId) {
        this.associatedNeedIds.add(needId);
    }

    /**
     * @return the branches
     */
    public Set<Integer> getBranches() {
        if (branches == null) {
            branches = new HashSet<>();
        }
        return branches;
    }

    public void setBranches(Set<Integer> branches) {
        this.branches = branches;
    }
    /**
     * @return the status of the idea
     */
    // Get the status of the idea
    public String getStatus() {
        return status;
    }
    /**
     * Set the status of the idea
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }
    /**
     * Add a branch ID
     * @param branchId the branch ID to add
     */
    public void addBranch(int branchId) {
        if (branches == null) {
            branches = new HashSet<>();
        }
        branches.add(branchId);
    }

    /**
     * Check if a branch ID exists
     * @param branchId the branch ID to check
     * @return true if the branch ID exists, false otherwise
     */
    public boolean hasBranch(int branchId) {
        return branches != null && branches.contains(branchId);
    }

    public Idea() {
        this.id = ++Idea.idCounter;
        this.supporters = new HashSet<>();
        this.associatedNeedIds = new HashSet<>();
        this.branches = new HashSet<>();
    }

    public Idea(String name, String description, String author) {
        this.id = ++Idea.idCounter;
        this.name = name;
        this.description = description;
        this.author = author;
        this.voteCount = 0;
        this.supporters = new HashSet<>();
        this.associatedNeedIds = new HashSet<>();
        this.branches = new HashSet<>();
    }


    @Override
    public String toString() {
        return "ID: " + getId() +
                ", Name: " + getName() +
                ", Idea: " + getDescription() +
                ", Author: " + getAuthor() +
                ", Votes: " + getVoteCount();
    }
    
    /**
     *
     * @param supporter
     */
    public void addSupporter(String supporter)
    {
        supporters.add(supporter);
    }
}

