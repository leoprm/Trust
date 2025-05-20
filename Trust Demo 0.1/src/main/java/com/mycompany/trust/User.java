/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.trust;

import java.util.*;

public class User {
    private int id; // Add a numeric ID field
    private String username;
    private String displayName;
    private String password;
    private int level;
    private int xp;
    private int points;
    private int totalBerriesEarned;
    private List<Berry> berries;

    // --- NEW: Certified Fields of Expertise --- (Loaded from DB)
    private Set<Integer> certifiedExpertiseIds = new HashSet<>();

    // Constructor with just username and displayName
    public User(String username, String displayName) {
        this.username = username;
        this.displayName = displayName;
        this.level = 1;
        this.xp = 0;
        this.points = 50; // Starting points
        this.totalBerriesEarned = 0;
        this.berries = new ArrayList<>();
        this.certifiedExpertiseIds = new HashSet<>(); // Initialize
    }

    // Constructor with more parameters (used in database loading)
    public User(String username, String password, int level, int xp, int points, int totalBerriesEarned) {
        this.username = username;
        this.password = password;
        this.level = level;
        this.xp = xp;
        this.points = points;
        this.totalBerriesEarned = totalBerriesEarned;
        this.berries = new ArrayList<>();
        this.certifiedExpertiseIds = new HashSet<>(); // Initialize
    }

    // --- Getters/Setters for Expertise ---
    public Set<Integer> getCertifiedExpertiseIds() {
         if (certifiedExpertiseIds == null) {
              certifiedExpertiseIds = new HashSet<>(); // Ensure not null
         }
        return certifiedExpertiseIds;
    }

    public void setCertifiedExpertiseIds(Set<Integer> certifiedExpertiseIds) {
        this.certifiedExpertiseIds = (certifiedExpertiseIds != null) ? certifiedExpertiseIds : new HashSet<>();
    }

    // Add these ID getter and setter methods
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : username;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean checkPassword(String password) {
        // Hash the provided password before comparing with stored hash
        String hashedPassword = PasswordHasher.hashPassword(password);
        return this.password.equals(hashedPassword);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public void addXp(int amount) {
        this.xp += amount;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void addPoints(int points) {
        this.points += points;
    }

    public List<Berry> getBerries() {
        return berries;
    }

    public int getTotalBerriesEarned() {
        return totalBerriesEarned;
    }

    public void setTotalBerriesEarned(int totalBerriesEarned) {
        this.totalBerriesEarned = totalBerriesEarned;
    }

    public void earnMonthlyBerries() {
        int berryAmount = SystemParameters.getInitialLevelOneBerryEarning() * this.level;
        this.totalBerriesEarned += berryAmount;
        Berry newBerry = new Berry(this.username, berryAmount, "monthly_distribution", 
                    java.time.LocalDateTime.now().plusMonths(SystemParameters.getBerryValidityTime()));
        this.berries.add(newBerry);
    }

    @Override
    public String toString() {
        return username + " (Level " + level + ", XP: " + xp + ", Points: " + points + ")";
    }
}

