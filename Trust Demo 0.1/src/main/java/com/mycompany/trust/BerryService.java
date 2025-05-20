package com.mycompany.trust;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    
    public static void convertToBerries(User user, int points) throws SQLException {
        // Check if user has enough points
        if (points > user.getPoints()) {
            DialogFactory.showError("You don't have enough points. You have " + user.getPoints() + " points remaining.");
            return;
        }
        
        // Calculate conversion
        double conversionRate = SystemParameters.getConversionPercentage() / 100.0;
        int berriesAmount = (int) Math.round(points * conversionRate);
        
        if (berriesAmount <= 0) {
            DialogFactory.showError("The conversion would result in 0 berries. Please convert more points.");
            return;
        }
        
        // Deduct points from user
        user.setPoints(user.getPoints() - points);
        DatabaseManager.updateUser(user);
        
        // Set expiration date
        LocalDateTime expirationDate = LocalDateTime.now()
                .plusMonths(SystemParameters.getBerryValidityTime());
        
        // Create and save berry
        Berry berry = new Berry(user.getUsername(), berriesAmount, "point_conversion", expirationDate);
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
            
            DialogFactory.showInfo("Points Converted", 
                "Successfully converted " + points + " points to " + berriesAmount + " berries.");
        } else {
            DialogFactory.showError("Failed to convert points. Database error occurred.");
        }
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
        int conversionTotal = 0;
        int otherTotal = 0;
        
        for (Berry berry : TrustSystem.userBerries.get(username)) {
            if (berry.getExpirationDate().isBefore(LocalDateTime.now())) {
                continue; // Skip expired berries
            }
            
            switch (berry.getSource()) {
                case "level_up":
                    levelUpTotal += berry.getAmount();
                    break;
                case "point_conversion":
                    conversionTotal += berry.getAmount();
                    break;
                default:
                    otherTotal += berry.getAmount();
                    break;
            }
        }
        
        // Add to report
        report.append("Berries from Level-ups: ").append(levelUpTotal).append("\n");
        report.append("Berries from Point Conversion: ").append(conversionTotal).append("\n");
        report.append("Berries from Other Sources: ").append(otherTotal).append("\n\n");
        
        report.append("Total Available Berries: ").append(levelUpTotal + conversionTotal + otherTotal).append("\n\n");
        
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
}
