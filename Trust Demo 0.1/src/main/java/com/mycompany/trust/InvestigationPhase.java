/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.trust;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents the Investigation phase of a branch
 */
public class InvestigationPhase extends PhaseBase implements Serializable {
    private static final long serialVersionUID = 1L; // Add a unique serial version UID
    
    private ArrayList<String> team = new ArrayList<>();
    
    public String getPhaseName() {
        return "Investigation";
    }

    @Override
    public void displayPhaseInfo() {
        System.out.println("Investigation Phase - Team Members: " + team.size());
        if (!team.isEmpty()) {
            System.out.println("Investigation Team:");
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
    
    public InvestigationPhase() {
        // Initialize any investigation-specific properties
    }
    
    public InvestigationPhase(ArrayList<String> team) {
        this.team = team;
    }
    
    public void addTeamMember(User user){
        this.team.add(user.getUsername());
    }
    
    public void addTeamMember(String user){
        this.team.add(user);
    }
}
