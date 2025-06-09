package com.mycompany.trust;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class UserService {

    public static User getUser(String username) throws SQLException {
        return DatabaseManager.getUser(username);
    }

    public static boolean userExists(String username) throws SQLException {
        return DatabaseManager.userExists(username);
    }

    public static void createUser(String username, String password) throws SQLException {
        // Consider adding validation or more complex logic here if needed
        DatabaseManager.createUser(username, password);
    }

    public static void updateUser(User user) throws SQLException {
        // Consider adding validation or pre-update logic here
        DatabaseManager.updateUser(user);
    }

    public static void awardXpToUser(User user, int xpAmount) throws SQLException {
        if (user == null) {
            System.err.println("Cannot award XP: user is null.");
            return;
        }
        if (xpAmount <= 0) {
            System.out.println("No XP awarded (amount <= 0): " + xpAmount + " to user " + user.getUsername());
            return;
        }
        user.setXp(user.getXp() + xpAmount);
        // Potentially add level-up logic here if not handled in User.setXp()
        DatabaseManager.updateUser(user);
        System.out.println("Awarded " + xpAmount + " XP to " + user.getUsername() + ". New XP: " + user.getXp());
    }

    public static List<User> getAllUsersList() throws SQLException {
        // Assuming DatabaseManager.getAllUsersList() exists or can be created.
        // If DatabaseManager.loadAllUsers() returns a Map<String, User>, convert it.
        Map<String, User> userMap = DatabaseManager.loadAllUsers();
        return new java.util.ArrayList<>(userMap.values());
    }

    // Placeholder for deleteUser if needed in the future
    /*
    public static void deleteUser(String username) throws SQLException {
        // Implementation would depend on whether it's a soft delete (mark as inactive)
        // or a hard delete (remove from database).
        // E.g., DatabaseManager.deleteUser(username);
        System.out.println("User deletion functionality not yet fully implemented.");
    }
    */
}
