package com.mycompany.trust;


/**
 * Represents the Completed phase of a branch
 */
public class CompletedPhase extends PhaseBase {
    private static final long serialVersionUID = 1L;
    
    public CompletedPhase() {
        // Initialize any completed-phase-specific properties
    }
    
    @Override
    public void displayPhaseInfo() {
        System.out.println("Completed Phase");
    }
}

