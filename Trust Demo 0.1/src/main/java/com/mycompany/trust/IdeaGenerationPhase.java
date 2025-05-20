/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.trust;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the Idea Generation phase of a branch
 */
public class IdeaGenerationPhase extends PhaseBase implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private ArrayList<String> team = new ArrayList<>();
    
    public String getPhaseName() {
        return "Idea Generation";
    }

    @Override
    public void displayPhaseInfo() {
        System.out.println("Idea Generation Phase");
    }

    /**
     * @return the team
     */
    @Override
    public ArrayList<String> getTeam() {
        return team;
    }

    /**
     * @param aTeam the team to set
     */
    public void setTeam(ArrayList<String> aTeam) {
        team = aTeam;
    }

    public IdeaGenerationPhase() {
        // Initialize any generation-specific properties
    }
    
    public IdeaGenerationPhase(ArrayList<String> team) {
        this.team = team;
    }
    
    public void addTeamMember(User user){
        this.team.add(user.getUsername());
    }
}
