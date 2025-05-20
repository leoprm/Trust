package com.mycompany.trust;

public class SystemParameters {
    // Active Leveling Parameters
    private static double xpIncreasePercentage = 30.0; // Default value
    private static double xpThreshold = 40.0;       // Default value

    // Active Berry Earning Parameter
    private static int initialLevelOneBerryEarning = 100; // Default value

    // Active Berry Validity Parameter
    private static int berryValidityTime = 12; // Default value
    
    // Active Berry Conversion Parameters
    private static double conversionPercentage = 10.0; // Default value (10%)
    private static int conversionPeriod = 3; // Default value (3 months)
    
    // Need classification parameters
    private static double globalNeedThresholdPercent = 25.0; // Default: 25% threshold for global needs
    private static double personalNeedThresholdPercent = 15.0; // Default: 15% threshold for personal needs
    private static int needTimeLimit = 3; // Default: 3 months time limit for branches to meet threshold
    
    // Getters and setters
    public static double getXpIncreasePercentage() {
        return xpIncreasePercentage;
    }
    
    public static void setXpIncreasePercentage(double value) {
        xpIncreasePercentage = value;
    }
    
    public static double getXpThreshold() {
        return xpThreshold;
    }
    
    public static void setXpThreshold(double value) {
        xpThreshold = value;
    }
    
    public static int getInitialLevelOneBerryEarning() {
        return initialLevelOneBerryEarning;
    }
    
    public static void setInitialLevelOneBerryEarning(int value) {
        initialLevelOneBerryEarning = value;
    }
    
    public static int getBerryValidityTime() {
        return berryValidityTime;
    }
    
    public static void setBerryValidityTime(int value) {
        berryValidityTime = value;
    }
    
    public static double getConversionPercentage() {
        return conversionPercentage;
    }
    
    public static void setConversionPercentage(double value) {
        conversionPercentage = value;
    }
    
    public static int getConversionPeriod() {
        return conversionPeriod;
    }
    
    public static void setConversionPeriod(int value) {
        conversionPeriod = value;
    }
    
    public static double getGlobalNeedThresholdPercent() {
        return globalNeedThresholdPercent;
    }
    
    public static void setGlobalNeedThresholdPercent(double value) {
        globalNeedThresholdPercent = value;
    }
    
    public static double getPersonalNeedThresholdPercent() {
        return personalNeedThresholdPercent;
    }
    
    public static void setPersonalNeedThresholdPercent(double value) {
        personalNeedThresholdPercent = value;
    }
    
    public static int getNeedTimeLimit() {
        return needTimeLimit;
    }
    
    public static void setNeedTimeLimit(int value) {
        needTimeLimit = value;
    }
    
    // Helper method for XP calculation
    public static int calculateXpThreshold(int level) {
        return (int) ((initialLevelOneBerryEarning * 1 + (xpThreshold / 100)) * 
                     Math.pow(1 + (xpIncreasePercentage / 100), level - 1));
    }
    
    /**
     * Calculates the monthly berry earning for a user based on their level
     * 
     * @param level The user's current level
     * @return The number of berries the user earns monthly
     */
    public static int calculateMonthlyBerryEarning(int level) {
        // Base monthly berry earning for level 1
        int baseEarning = initialLevelOneBerryEarning;
        
        if (level <= 1) {
            return baseEarning;
        }
        
        // Each level increases earnings by 10% of base earning
        double levelMultiplier = 1.0 + ((level - 1) * 0.1);
        
        // Calculate and round to the nearest integer
        return (int) Math.round(baseEarning * levelMultiplier);
    }
    
    /**
     * Determines if a branch is a Need based on the points allocated to it
     * 
     * @param branchPointsPercent Percentage of total points dedicated to this branch
     * @return true if it meets the global threshold for Need status
     */
    public static boolean isGlobalNeed(double branchPointsPercent) {
        return branchPointsPercent >= globalNeedThresholdPercent;
    }
    
    /**
     * Determines if a branch is a personal Need for a user
     * 
     * @param userPointsPercent Percentage of user's points dedicated to this branch
     * @return true if it meets the personal threshold for Need status
     */
    public static boolean isPersonalNeed(double userPointsPercent) {
        return userPointsPercent >= personalNeedThresholdPercent;
    }
    
    // Removed obsolete getActive*Proposal methods.
    // Determining the active proposal and updating parameters should be handled
    // elsewhere (e.g., in ProposalService or TrustSystem.updateSystemParametersFromActiveProposals)
    // by querying the database based on votes/status.
}
