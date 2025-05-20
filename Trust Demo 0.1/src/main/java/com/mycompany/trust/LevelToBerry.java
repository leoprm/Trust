package com.mycompany.trust;

/**
 * Utility class to calculate berry earnings based on user level
 * @author leo
 */
class LevelToBerry {
    
    /**
     * Calculates the berry amount a user should receive based on their level
     * @param level User's current level
     * @return Amount of berries earned
     */
    public static int LevelToBerries(int level) {
        // Get the initial berry earning for level 1 from system parameters
        int total = SystemParameters.getInitialLevelOneBerryEarning();
        
        // Apply compounding increase based on active level proposal parameters
        if (SystemParameters.getXpIncreasePercentage() > 0) {
            // Calculate the total berries based on the level and increase percentage
            for(int i = 0; i < level; i++) {
                total += total * (SystemParameters.getXpIncreasePercentage() / 100);
            }
        }
        
        return total;
    }
}
