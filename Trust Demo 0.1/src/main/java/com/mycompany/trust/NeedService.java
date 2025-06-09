package com.mycompany.trust;

import java.sql.SQLException;
import java.util.Map;

public class NeedService {
    
    public static void createNeed(String needName) throws SQLException {
        if (needName == null || needName.trim().isEmpty()) {
            DialogFactory.showError("Need name cannot be empty");
            return;
        }
          int needId = DatabaseManager.createNeed(needName);
        if (needId != -1) {
            DialogFactory.showInfo("Need Created", "Need '" + needName + "' was created with ID: " + needId);
        } else {
            DialogFactory.showError("Failed to create need. Database error occurred.");
        }
    }
    
    public static void allocatePointsToNeed(User user, Need need, int points, boolean affected, String location) throws SQLException {
        // Check if user has enough points
        if (points > user.getPoints()) {
            DialogFactory.showError("You don't have enough points. You have " + user.getPoints() + " points remaining.");
            return;
        }
        
        // Get existing points for this need
        int existingPoints = need.getSupporters().getOrDefault(user.getUsername(), 0);
        
        // Deduct points from user
        user.setPoints(user.getPoints() - points);
        DatabaseManager.updateUser(user);
        
        // Add points to need
        need.addSupporter(user.getUsername(), points);
        DatabaseManager.addNeedSupporter(need.getId(), user.getUsername(), points);
        
        // Add user as affected if checked - FIXED: removed the existingPoints == 0 condition
        if (affected) {
            need.addAffectedUser(user.getUsername(), location);
            DatabaseManager.addNeedAffectedUser(need.getId(), user.getUsername(), location);
        }
        
        // Show success message
        if (existingPoints > 0) {
            DialogFactory.showInfo("Points Allocated", "Added " + points + " points to your existing allocation of " + 
                    existingPoints + " points for '" + need.getName() + "'.");
        } else {
            DialogFactory.showInfo("Points Allocated", "Allocated " + points + " points to '" + need.getName() + "'.");
        }
    }
    
    public static int calculateTotalPoints(Need need) {
        return need.getSupporters().values().stream().mapToInt(Integer::intValue).sum();
    }
      public static int getTotalNeedPoints(int id) {
        try {
            Need need = DatabaseManager.getNeed(id);
            if (need == null) {
                return 0;
            }
            
            Map<String, Integer> supporters = need.getSupporters();
            int totalPoints = 0;
            for (int points : supporters.values()) {
                totalPoints += points;
            }
            return totalPoints;
        } catch (SQLException e) {
            System.err.println("Error loading need " + id + ": " + e.getMessage());
            return 0;
        }
    }
}
