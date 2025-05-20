/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.trust;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author leo
 */
public class Need {
    private static int idCounter;
    private int id;
    private String name;
    private Map<String, Integer> supporters;
    private Map<String, String> affectedUsers; // username -> location

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
     * @return the supporters
     */
    public Map<String, Integer> getSupporters() {
        return supporters;
    }

    /**
     * @param supporters the supporters to set
     */
    public void setSupporters(Map<String, Integer> supporters) {
        this.supporters = supporters;
    }

    public Map<String, String> getAffectedUsers() {
        return affectedUsers;
    }

    public void setAffectedUsers(Map<String, String> affectedUsers) {
        this.affectedUsers = affectedUsers;
    }

    public Need() {
        this.id = ++Need.idCounter;
        this.supporters = new HashMap<>();
        this.affectedUsers = new HashMap<>();
    }
    
    public Need(String name) {
        this.id = ++Need.idCounter;
        this.name = name;
        this.supporters = new HashMap<>();
        this.affectedUsers = new HashMap<>();
    }
    
    public void addSupporter(String username, int points) {
        supporters.put(username, supporters.getOrDefault(username, 0) + points);
    }
    
    public void addAffectedUser(String username, String location) {
        affectedUsers.put(username, location);
    }
    
    @Override
    public String toString() {
        return "Need #" + id + ": " + name;
    }
}
