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
            Idea idea = new Idea(name, description, author);
            idea.setId(ideaId);
            TrustSystem.ideas.put(ideaId, idea);
            return ideaId;
        } else {
            DialogFactory.showError("Failed to submit idea. Database error occurred.");
            return -1;
        }
    }
    
    public static void associateIdeaWithNeeds(int ideaId, Set<Need> needs) throws SQLException {
        if (!TrustSystem.ideas.containsKey(ideaId)) {
            DialogFactory.showError("Invalid idea ID");
            return;
        }
        
        Idea idea = TrustSystem.ideas.get(ideaId);
        
        for (Need need : needs) {
            // Store association in database
            DatabaseManager.associateIdeaWithNeed(ideaId, need.getId());
            
            // Update in-memory model
            idea.addAssociatedNeedId(need.getId());
        }
    }
    
    public static void voteForIdea(int ideaId, User voter) throws SQLException {
        if (!TrustSystem.ideas.containsKey(ideaId)) {
            DialogFactory.showError("Invalid idea ID.");
            return;
        }
        
        Idea idea = TrustSystem.ideas.get(ideaId);
        Set<String> supporters = idea.getSupporters();
        
        if (supporters.contains(voter.getUsername())) {
            DialogFactory.showError("You have already voted for this idea.");
            return;
        }
        
        // Add support for the idea
        idea.addSupporter(voter.getUsername());
        
        // Award XP to the user
        voter.addXp(10);
        
        // Update database
        DatabaseManager.updateUser(voter);
        DatabaseManager.addIdeaSupporter(ideaId, voter.getUsername());
        
        DialogFactory.showInfo("Vote Successful", "You've successfully voted for the idea: " + idea.getName());
    }
    
    public static void updateIdeaStatus(int ideaId, String newStatus) throws SQLException {
        if (!TrustSystem.ideas.containsKey(ideaId)) {
            DialogFactory.showError("Invalid idea ID.");
            return;
        }
        
        Idea idea = TrustSystem.ideas.get(ideaId);
        idea.setStatus(newStatus);
        
        // Update in database
        DatabaseManager.updateIdeaStatus(ideaId, newStatus);
    }
    
    public static String getIdeaDetails(int ideaId) {
        if (!TrustSystem.ideas.containsKey(ideaId)) {
            return "Idea not found";
        }
        
        Idea idea = TrustSystem.ideas.get(ideaId);
        StringBuilder details = new StringBuilder();
        details.append("Name: ").append(idea.getName()).append("\n");
        details.append("Description: ").append(idea.getDescription()).append("\n");
        details.append("Author: ").append(idea.getAuthor()).append("\n");
        details.append("Status: ").append(idea.getStatus()).append("\n");
        details.append("Votes: ").append(idea.getSupporters().size()).append("\n");
        
        // Add associated needs
        details.append("Associated Needs:\n");
        for (Integer needId : idea.getAssociatedNeedIds()) {
            if (TrustSystem.needs.containsKey(needId)) {
                details.append(" - ").append(TrustSystem.needs.get(needId).getName()).append("\n");
            }
        }
        
        return details.toString();
    }
}
