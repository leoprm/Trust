/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.trust;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author leo
*/
public class Berry {
    private static int idCounter;
    private int id;
    private LocalDate creationTime;
    private int validMonths;
    private String username;
    private int amount;
    private String source;
    private LocalDateTime expirationDate;
    private boolean expired;

    /**
     * @return the idCounter
     */
    public static int getIdCounter() {
        return idCounter;
    }

    /**
     * @param aIdCounter the idCounter to set
     */
    public static void setIdCounter(int aIdCounter) {
        idCounter = aIdCounter;
    }

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
     * @return the creationTime
     */
    public LocalDate getCreationTime() {
        return creationTime;
    }

    /**
     * @param creationTime the creationTime to set
     */
    public void setCreationTime(LocalDate creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * @return the validMonths
     */
    public int getValidMonths() {
        return validMonths;
    }

    /**
     * @param validMonths the validMonths to set
     */
    public void setValidMonths(int validMonths) {
        this.validMonths = validMonths;
    }
    
    public Berry()
    {
        this.id = ++Berry.idCounter;
        this.creationTime = LocalDate.now();
        this.validMonths = 12;
    }
    
    
    public Berry(int validMonths)
    {
        this.id = ++Berry.idCounter;
        this.creationTime = LocalDate.now();
        this.validMonths = validMonths;
    }    /**
     * Constructor for creating a berry
     * @param username The username of the berry owner
     * @param amount The amount of berries
     * @param source The source of the berries (e.g., "monthly_distribution", "level_up")
     * @param expirationDate When the berries expire
     */
    public Berry(String username, int amount, String source, LocalDateTime expirationDate) {
        this.username = username;
        this.amount = amount;
        this.source = source;
        this.expirationDate = expirationDate;
        this.expired = false;
    }
    
    // Getters and setters
    public String getUsername() {
        return username;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public String getSource() {
        return source;
    }
    
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }
    
    public boolean isExpired() {
        return expired;
    }
    
    public void setExpired(boolean expired) {
        this.expired = expired;
    }
    
    @Override
    public String toString() {
        return amount + " berries (Source: " + source + ", Expires: " + expirationDate + ")";
    }
}
