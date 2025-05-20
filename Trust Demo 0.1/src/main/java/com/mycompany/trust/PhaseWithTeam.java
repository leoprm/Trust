package com.mycompany.trust;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Base class for all phases that have a team
 */
public abstract class PhaseWithTeam implements Serializable {
    // Add serialVersionUID to ensure version compatibility
    private static final long serialVersionUID = 1L;
    
    protected ArrayList<String> team;
    
    public PhaseWithTeam() {
        this.team = new ArrayList<>();
    }
    
    public ArrayList<String> getTeam() {
        return team;
    }
    
    public void setTeam(ArrayList<String> team) {
        this.team = team;
    }
    
    public void addTeamMember(User user) {
        if (!team.contains(user.getUsername())) {
            team.add(user.getUsername());
        }
    }
    
    public abstract void displayPhaseInfo();
}
