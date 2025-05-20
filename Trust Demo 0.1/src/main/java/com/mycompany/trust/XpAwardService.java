package com.mycompany.trust;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Service for awarding XP to users based on phase completion and impact.
 */
public class XpAwardService {

    /**
     * Awards base XP to all team members of a completed and validated phase.
     * @param branch The branch containing the phase.
     * @param phase The phase object (must extend PhaseBase).
     * @param userMap Map of username to User objects.
     * @param baseXpAmount The base XP amount to distribute.
     * @throws SQLException if persistence fails.
     */
    public static void awardBaseXp(Branch branch, PhaseBase phase, Map<String, User> userMap, int baseXpAmount) throws SQLException {
        if (!phase.isCompleted()) {
            System.out.println("Phase not completed. No base XP awarded.");
            return;
        }
        if (phase.isValidated() == false) {
            System.out.println("Phase not validated. No base XP awarded.");
            return;
        }
        if (phase.isBaseXpAwarded()) {
            System.out.println("Base XP already awarded for this phase.");
            return;
        }

        ArrayList<String> team = phase.getTeam();
        if (team.isEmpty()) {
            System.out.println("No team members found for phase.");
            return;
        }

        double xpPerMember = (double) baseXpAmount / team.size();

        for (String username : team) {
            User user = userMap.get(username);
            if (user != null) {
                user.addXp((int) xpPerMember);
                DatabaseManager.updateUser(user);
                System.out.println("Awarded base XP: " + xpPerMember + " to user: " + username);
            } else {
                System.out.println("User not found: " + username);
            }
        }

        phase.setBaseXpAwarded(true);
        // Save updated phase back to branch
        branch.saveCurrentPhase();
        System.out.println("Base XP awarded and phase updated.");
    }

    /**
     * Awards bonus XP to all team members of a phase based on satisfaction index or other metric.
     * @param branch The branch containing the phase.
     * @param phase The phase object (must extend PhaseBase).
     * @param userMap Map of username to User objects.
     * @param maxBonusXp The maximum bonus XP possible.
     * @param triggerMetricValue The metric value (e.g., satisfaction index 0-100).
     * @throws SQLException if persistence fails.
     */
    public static void awardBonusXp(Branch branch, PhaseBase phase, Map<String, User> userMap, int maxBonusXp, double triggerMetricValue) throws SQLException {
        if (phase.isBonusXpAwarded()) {
            System.out.println("Bonus XP already awarded for this phase.");
            return;
        }

        if (triggerMetricValue < 0) {
            System.out.println("Invalid trigger metric value. No bonus XP awarded.");
            return;
        }

        double scaledBonusXp = maxBonusXp * (triggerMetricValue / 100.0);

        ArrayList<String> team = phase.getTeam();
        if (team.isEmpty()) {
            System.out.println("No team members found for phase.");
            return;
        }

        double xpPerMember = scaledBonusXp / team.size();

        for (String username : team) {
            User user = userMap.get(username);
            if (user != null) {
                user.addXp((int) xpPerMember);
                DatabaseManager.updateUser(user);
                System.out.println("Awarded bonus XP: " + xpPerMember + " to user: " + username);
            } else {
                System.out.println("User not found: " + username);
            }
        }

        phase.setBonusXpAwarded(true);
        // Save updated phase back to branch
        branch.saveCurrentPhase();
        System.out.println("Bonus XP awarded and phase updated.");
    }
}