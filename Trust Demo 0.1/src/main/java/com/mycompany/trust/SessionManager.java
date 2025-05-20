package com.mycompany.trust;

import javafx.stage.Stage;
import javafx.stage.Window;

public class SessionManager {
    private static User currentUser = null;
    private static String currentUsername = null;
    private static String currentPassword = null;
    
    public static void setCurrentUser(User user, String username, String password) {
        currentUser = user;
        currentUsername = username;
        currentPassword = password;
    }
    
    public static User getCurrentUser() {
        return currentUser;
    }
    
    public static String getCurrentUsername() {
        return currentUsername;
    }
    
    public static boolean isUserLoggedIn() {
        return currentUser != null;
    }
    
    public static void logout() {
        currentUser = null;
        currentUsername = null;
        currentPassword = null;
    }
    
    public static void handleLogout() {
        Stage currentStage = (Stage) getActiveWindow();
        logout();
        DialogFactory.showInfo("Logged Out", "You have been logged out successfully.");
        
        // Create new login window
        WindowBuilder loginWindow = new WindowBuilder();
        loginWindow.start(currentStage);
    }
    
    public static void handleExit() {
        logout();
        DatabaseConnection.closeDataSource();
        System.exit(0);
    }
    
    public static Window getActiveWindow() {
        // Get focused window
        for (Window window : Window.getWindows()) {
            if (window.isFocused()) {
                return window;
            }
        }
        return null;
    }
    
    public static User authenticateUser(String username, String password) {
        if (!TrustSystem.users.containsKey(username)) {
            return null;
        }
        
        User user = TrustSystem.users.get(username);
        if (!user.checkPassword(password)) {
            return null;
        }
        
        return user;
    }
}
