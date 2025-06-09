package com.mycompany.trust;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BerryService {
    
    public static void distributeBerriesForLevel(User user, int level) throws SQLException {
        // Calculate number of berries based on level
        int berriesAmount = calculateBerryDistribution(level);
        
        // Set expiration date
        LocalDateTime expirationDate = LocalDateTime.now()
                .plusMonths(SystemParameters.getBerryValidityTime());
        
        // Create and save berry
        Berry berry = new Berry(user.getUsername(), berriesAmount, "level_up", expirationDate);
        int berryId = DatabaseManager.saveBerry(berry);
        
        if (berryId != -1) {
            berry.setId(berryId);
            
            // Add to user's berries
            if (TrustSystem.userBerries.containsKey(user.getUsername())) {
                TrustSystem.userBerries.get(user.getUsername()).add(berry);
            } else {
                List<Berry> berries = new ArrayList<>();
                berries.add(berry);
                TrustSystem.userBerries.put(user.getUsername(), berries);
            }
            
            DialogFactory.showInfo("Berries Earned", 
                "Congratulations! You've earned " + berriesAmount + " berries for reaching level " + level + ".");
        } else {
            DialogFactory.showError("Failed to distribute berries. Database error occurred.");
        }
    }
    
    private static int calculateBerryDistribution(int level) {
        // Use system parameter for initial level 1 berry earning
        int initialEarning = SystemParameters.getInitialLevelOneBerryEarning();
        
        // Simple formula: initialEarning * level
    return initialEarning * level;
    }
    
    public static int getUserTotalBerries(String username) {
        if (!TrustSystem.userBerries.containsKey(username)) {
            return 0;
        }
        
        int total = 0;
        for (Berry berry : TrustSystem.userBerries.get(username)) {
            // Only count non-expired berries
            if (berry.getExpirationDate().isAfter(LocalDateTime.now())) {
                total += berry.getAmount();
            }
        }
        return total;
    }
    
    public static void checkAndRemoveExpiredBerries() throws SQLException {
        LocalDateTime now = LocalDateTime.now();
        List<Berry> expiredBerries = new ArrayList<>();
        
        // Check all berries for expiration
        for (List<Berry> userBerryList : TrustSystem.userBerries.values()) {
            for (Berry berry : userBerryList) {
                if (berry.getExpirationDate().isBefore(now)) {
                    expiredBerries.add(berry);
                    // Mark as expired in database
                    DatabaseManager.markBerryExpired(berry.getId());
                }
            }
            // Remove expired berries from user's list
            userBerryList.removeAll(expiredBerries);
        }
    }
    
    public static String generateBerryUsageReport(String username) {
        if (!TrustSystem.userBerries.containsKey(username)) {
            return "No berries found for this user.";
        }
        
        StringBuilder report = new StringBuilder();
        report.append("Berry Usage Report for ").append(username).append("\n\n");
          // Group berries by source
        int levelUpTotal = 0;
        int otherTotal = 0;
        
        for (Berry berry : TrustSystem.userBerries.get(username)) {
            if (berry.getExpirationDate().isBefore(LocalDateTime.now())) {
                continue; // Skip expired berries
            }
            
            switch (berry.getSource()) {
                case "level_up":
                    levelUpTotal += berry.getAmount();
                    break;
                default:
                    otherTotal += berry.getAmount();
                    break;
            }
        }
        
        // Add to report
        report.append("Berries from Level-ups: ").append(levelUpTotal).append("\n");
        report.append("Berries from Other Sources: ").append(otherTotal).append("\n\n");
        
        report.append("Total Available Berries: ").append(levelUpTotal + otherTotal).append("\n\n");
        
        // Add expiry information
        report.append("Upcoming Expirations:\n");
        for (Berry berry : TrustSystem.userBerries.get(username)) {
            if (berry.getExpirationDate().isAfter(LocalDateTime.now())) {
                report.append(berry.getAmount())
                      .append(" berries will expire on ")
                      .append(berry.getExpirationDate().toLocalDate())
                      .append("\n");
            }
        }
        
        return report.toString();
    }
      /**
     * Process monthly berry earnings for all users based on their current level
     */
    public static void processMonthlyBerryEarnings() throws SQLException {
        List<User> allUsers = DatabaseManager.getAllUsersList();
        int distributedCount = 0;
        int errorCount = 0;
        
        for (User user : allUsers) {
            try {
                distributeMonthlyBerries(user, user.getLevel());
                distributedCount++;
            } catch (SQLException e) {
                errorCount++;
                System.err.println("Failed to distribute monthly berries for user: " + user.getUsername() + " - " + e.getMessage());
            }
        }
          // Show summary dialog
        String message = "Monthly Berry Distribution Complete!\n\n" +
                        "Successfully distributed to " + distributedCount + " users.\n";
        if (errorCount > 0) {
            message += "Failed for " + errorCount + " users (check logs for details).";
            DialogFactory.showInfo("Monthly Distribution", message);
        } else {
            DialogFactory.showInfo("Monthly Distribution", message);
        }
    }
    
    /**
     * Distribute monthly berries to a specific user based on their level
     */
    public static void distributeMonthlyBerries(User user, int level) throws SQLException {
        // Calculate number of berries based on level (same as level-up distribution)
        int berriesAmount = calculateBerryDistribution(level);
        
        // Set expiration date
        LocalDateTime expirationDate = LocalDateTime.now()
                .plusMonths(SystemParameters.getBerryValidityTime());
        
        // Create and save berry with "monthly" source
        Berry berry = new Berry(user.getUsername(), berriesAmount, "monthly", expirationDate);
        int berryId = DatabaseManager.saveBerry(berry);
        
        if (berryId != -1) {
            berry.setId(berryId);
            
            // Add to user's berries
            if (TrustSystem.userBerries.containsKey(user.getUsername())) {
                TrustSystem.userBerries.get(user.getUsername()).add(berry);
            } else {
                List<Berry> berries = new ArrayList<>();
                berries.add(berry);
                TrustSystem.userBerries.put(user.getUsername(), berries);
            }
        } else {
            throw new SQLException("Failed to save berry to database for user: " + user.getUsername());
        }
    }
    
    /**
     * Loads all berries for all users from the database.
     * @return Map of username to list of Berry objects
     */
    public static Map<String, List<Berry>> loadAllUserBerries() throws java.sql.SQLException {
        return DatabaseManager.loadAllBerries();
    }
}
