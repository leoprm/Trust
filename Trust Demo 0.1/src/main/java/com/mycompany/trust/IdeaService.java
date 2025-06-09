package com.mycompany.trust;

import java.sql.SQLException;
import java.util.Set;

public class IdeaService {
    
    public static int submitIdea(String name, String description, String author) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            DialogFactory.showError("Idea name cannot be empty");
            return -1;
        }
        
        if (description == null || description.trim().isEmpty()) {
            DialogFactory.showError("Idea description cannot be empty");
            return -1;
        }
        
        int ideaId = DatabaseManager.createIdea(name, description, author);
        if (ideaId != -1) {
            // Idea is saved in DB, no need to add to TrustSystem.ideas manually here
            return ideaId;
        } else {
            DialogFactory.showError("Failed to submit idea. Database error occurred.");
            return -1;
        }
    }
    
    public static void associateIdeaWithNeeds(int ideaId, Set<Need> needs) throws SQLException {
        Idea idea = DatabaseManager.getIdea(ideaId);
        if (idea == null) {
            DialogFactory.showError("Invalid idea ID: " + ideaId);
            return;
        }
        
        // Idea idea = TrustSystem.ideas.get(ideaId); // Removed
        
        for (Need need : needs) {
            // Store association in database
            DatabaseManager.associateIdeaWithNeed(ideaId, need.getId());
            
            // Update in-memory model (of the locally fetched idea object)
            idea.addAssociatedNeedId(need.getId());
        }
    }
    
    public static void voteForIdea(int ideaId, User voter) throws SQLException {
        Idea idea = DatabaseManager.getIdea(ideaId);
        if (idea == null) {
            DialogFactory.showError("Invalid idea ID: " + ideaId);
            return;
        }
        
        // Idea idea = TrustSystem.ideas.get(ideaId); // Removed
        Set<String> supporters = idea.getSupporters(); // Supporters are loaded by getIdea
        
        if (supporters.contains(voter.getUsername())) {
            DialogFactory.showError("You have already voted for this idea.");
            return;
        }
        
        // Add support for the idea
        idea.addSupporter(voter.getUsername()); // Updates local idea object's supporter set
        idea.setVoteCount(idea.getSupporters().size()); // Update vote count on local idea object
        
        // Award XP to the user
        voter.addXp(10);
        
        // Update database
        DatabaseManager.updateUser(voter);
        DatabaseManager.addIdeaSupporter(ideaId, voter.getUsername()); // Adds to idea_supporters table
        DatabaseManager.updateIdea(idea); // Updates vote_count in ideas table
        
        DialogFactory.showInfo("Vote Successful", "You've successfully voted for the idea: " + idea.getName());
    }
    
    public static void updateIdeaStatus(int ideaId, String newStatus) throws SQLException {
        Idea idea = DatabaseManager.getIdea(ideaId);
        if (idea == null) {
            DialogFactory.showError("Invalid idea ID: " + ideaId);
            return;
        }
        
        // Idea idea = TrustSystem.ideas.get(ideaId); // Removed
        idea.setStatus(newStatus); // Updates local idea object
        
        // Update in database
        // Note: DatabaseManager.updateIdeaStatus(ideaId, newStatus) needs to be implemented in DatabaseManager.java
        // OR DatabaseManager.updateIdea(Idea idea) should be modified to also save the status.
        DatabaseManager.updateIdeaStatus(ideaId, newStatus);
    }
    
    public static String getIdeaDetails(int ideaId) throws SQLException { // Added throws SQLException
        Idea idea = DatabaseManager.getIdea(ideaId);
        if (idea == null) {
            return "Idea not found with ID: " + ideaId;
        }
        
        // Idea idea = TrustSystem.ideas.get(ideaId); // Removed
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(idea.getName()).append("\\n");
        details.append("Description: ").append(idea.getDescription()).append("\\n");
        details.append("Author: ").append(idea.getAuthor()).append("\\n");
        details.append("Status: ").append(idea.getStatus()).append("\\n");
        details.append("Votes: ").append(idea.getSupporters().size()).append("\\n"); // Derived from supporters
          // Add associated needs
        details.append("Associated Needs:\\n");
        if (idea.getAssociatedNeedIds() != null) {
            for (Integer needId : idea.getAssociatedNeedIds()) {
                try {
                    Need need = DatabaseManager.getNeed(needId); // getNeed can throw SQLException
                    if (need != null) {
                        details.append(" - ").append(need.getName()).append("\\n");
                    } else {
                        details.append(" - Need ID: ").append(needId).append(" (not found)\\n");
                    }
                } catch (SQLException e) {
                    System.err.println("Error loading need " + needId + " for idea details: " + e.getMessage());
                    details.append(" - Error loading need ").append(needId).append("\\n");
                }
            }
        }
        
        return details.toString();
    }
}
