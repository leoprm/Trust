/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.trust;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents the Development phase of a branch
 */
public class DevelopmentPhase extends PhaseBase implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private ArrayList<String> team = new ArrayList<>();
    
    // Development-specific properties can go here
    
    public DevelopmentPhase() {
        // Initialize any development-specific properties
    }
    
    public String getPhaseName() {
        return "Development";
    }

    @Override
    public void displayPhaseInfo() {
        System.out.println("Development Phase - Team Members: " + team.size());
        if (!team.isEmpty()) {
            System.out.println("Development Team:");
            for (String member : team) {
                System.out.println("- " + member);
            }
        } else {
            System.out.println("No team members assigned yet.");
        }
    }

     /**
     * @return the team
     */
    public ArrayList<String> getTeam() {
        return team;
    }

    /**
     * @param aTeam the team to set
     */
    public void setTeam(ArrayList<String> aTeam) {
        team = aTeam;
    }
    
    public DevelopmentPhase(ArrayList<String> team) {
        this.team = team;
    }
    
    public void addTeamMember(User user){
        this.team.add(user.getUsername());
    }
}
