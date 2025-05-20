package com.mycompany.trust;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import java.util.Optional;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SplitPane;

public class TrustSystem extends Application {

    public static Map<Integer, LevelProposal> levelProposals = new HashMap<>();
    public static Map<Integer, BerryEarningProposal> berryEarningProposals = new HashMap<>();
    public static Map<Integer, BerryValidityProposal> berryValidityProposals = new HashMap<>();
    public static Map<Integer, BerryConversionProposal> berryConversionProposals = new HashMap<>();
    public static Map<Integer, NeedThresholdProposal> needThresholdProposals = new HashMap<>();
    // Data collections
    public static Map<String, User> users = new HashMap<>();
    public static Map<Integer, Need> needs = new HashMap<>();
    public static Map<Integer, Idea> ideas = new HashMap<>();
    public static Map<Integer, Branch> branches = new HashMap<>();
    public static Map<String, List<Berry>> userBerries = new HashMap<>();
    public static Map<Integer, FieldOfExpertise> fieldsOfExpertise = new HashMap<>();
    // Obsolete specific proposal maps removed.
    
    // Add static references to main UI components
    private static BorderPane mainLayout;
    private static Stage primaryStage;
    
    // Initialize the system
    public static void initialize() {
        try {
            // Initialize database
            DatabaseConnection.initializeDataSource();
            
            // Load data from database
            loadUsersFromDatabase();
            loadNeedsFromDatabase();
            loadIdeasFromDatabase();
            loadBranchesFromDatabase();
            loadBerriesFromDatabase();
            loadProposalsFromDatabase();
            loadFieldsOfExpertiseFromDatabase(); // NEW
                 
            // Check for and remove expired berries
            BerryService.checkAndRemoveExpiredBerries();
                 
        } catch (SQLException e) {
            DialogFactory.showError("Database initialization error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    // Redundant loadAllData method removed.
    // Load data methods
    private static void loadUsersFromDatabase() throws SQLException {
        users = DatabaseManager.loadAllUsers();
    }
    
    private static void loadNeedsFromDatabase() throws SQLException {
        needs = DatabaseManager.loadAllNeeds();
    }
    
    private static void loadIdeasFromDatabase() throws SQLException {
        ideas = DatabaseManager.loadAllIdeas();
    }
    
    private static void loadBranchesFromDatabase() throws SQLException {
        branches = DatabaseManager.loadAllBranches();
    }
    
    private static void loadBerriesFromDatabase() throws SQLException {
        userBerries = DatabaseManager.loadAllBerries();
    }
    
    private static void loadProposalsFromDatabase() throws SQLException {
        // Load proposals into their specific maps using the new DB methods
        levelProposals = DatabaseManager.loadAllLevelProposals();
        berryEarningProposals = DatabaseManager.loadAllBerryEarningProposals();
        berryValidityProposals = DatabaseManager.loadAllBerryValidityProposals();
        berryConversionProposals = DatabaseManager.loadAllBerryConversionProposals();
        needThresholdProposals = DatabaseManager.loadAllNeedThresholdProposals();

        // Initialize system parameters from active proposals
        updateSystemParametersFromActiveProposals();
    }
    
    private static void updateSystemParametersFromActiveProposals() {
        // Find the active proposals (highest vote count) and update system parameters

        // Level parameters
        LevelProposal activeLevelProposal = findActiveProposal(levelProposals);
        if (activeLevelProposal != null) {
            SystemParameters.setXpIncreasePercentage(activeLevelProposal.getXpIncreasePercentage());
            SystemParameters.setXpThreshold(activeLevelProposal.getXpThreshold());
            System.out.println("System Parameters Updated from Level Proposal #" + activeLevelProposal.getId());
        } else {
            System.out.println("No active Level Proposal found. Using default parameters.");
        }

        // Berry earning parameter
        BerryEarningProposal activeBerryEarningProposal = findActiveProposal(berryEarningProposals);
        if (activeBerryEarningProposal != null) {
            SystemParameters.setInitialLevelOneBerryEarning(activeBerryEarningProposal.getInitialLevelOneBerryEarning());
            System.out.println("System Parameters Updated from Berry Earning Proposal #" + activeBerryEarningProposal.getId());
        } else {
            System.out.println("No active Berry Earning Proposal found. Using default parameters.");
        }

        // Berry validity parameter
        BerryValidityProposal activeBerryValidityProposal = findActiveProposal(berryValidityProposals);
        if (activeBerryValidityProposal != null) {
            SystemParameters.setBerryValidityTime(activeBerryValidityProposal.getValidityMonths());
            System.out.println("System Parameters Updated from Berry Validity Proposal #" + activeBerryValidityProposal.getId());
        } else {
            System.out.println("No active Berry Validity Proposal found. Using default parameters.");
        }

        // Berry conversion parameters
        BerryConversionProposal activeBerryConversionProposal = findActiveProposal(berryConversionProposals);
        if (activeBerryConversionProposal != null) {
            SystemParameters.setConversionPercentage(activeBerryConversionProposal.getConversionPercentage());
            SystemParameters.setConversionPeriod(activeBerryConversionProposal.getConversionPeriod());
            System.out.println("System Parameters Updated from Berry Conversion Proposal #" + activeBerryConversionProposal.getId());
        } else {
            System.out.println("No active Berry Conversion Proposal found. Using default parameters.");
        }
        
        // Need threshold parameters
        NeedThresholdProposal activeNeedThresholdProposal = findActiveProposal(needThresholdProposals);
        if (activeNeedThresholdProposal != null) {
            SystemParameters.setGlobalNeedThresholdPercent(activeNeedThresholdProposal.getGlobalThresholdPercent());
            SystemParameters.setPersonalNeedThresholdPercent(activeNeedThresholdProposal.getPersonalThresholdPercent());
            SystemParameters.setNeedTimeLimit(activeNeedThresholdProposal.getTimeLimit());
            
            // Log the update
            System.out.println("System Parameters Updated from Need Threshold Proposal #" + activeNeedThresholdProposal.getId());
            
        } else {
            System.out.println("No active Need Threshold Proposal found. Using default parameters.");
        }
    }
    
    // Helper method to find the active proposal (e.g., highest votes) from a map
    private static <T extends Proposal> T findActiveProposal(Map<Integer, T> proposals) {
        if (proposals == null || proposals.isEmpty()) {
            return null;
        }
        // Return the one with the most votes
        return proposals.values().stream()
                .max(Comparator.comparingInt(Proposal::getVotes))
                .orElse(null);
    }
    
    // User registration method
    public static boolean registerUser(String username, String password, String displayName) {
        if (users.containsKey(username)) {
            return false;
        }
        
        try {
            User newUser = new User(username, displayName);
            newUser.setPassword(password);
            
            // Save to database
            int userId = DatabaseManager.saveUser(newUser);
            
            if (userId != -1) {
                newUser.setId(userId);
                users.put(username, newUser);
                return true;
            }
        } catch (SQLException e) {
            DialogFactory.showError("Error registering user: " + e.getMessage());
        }
        
        return false;
    }
    
    // Check for level up
    public static boolean checkForLevelUp(User user) throws SQLException {
        int currentXp = user.getXp();
        int currentLevel = user.getLevel();
        int xpThreshold = SystemParameters.calculateXpThreshold(currentLevel);
        
        if (currentXp >= xpThreshold) {
            // Level up the user
            user.setLevel(currentLevel + 1);
            user.setPoints(user.getPoints() + 10); // Bonus points for leveling up
            
            // Update in database
            DatabaseManager.updateUser(user);
            
            // Return true to indicate level up occurred
            return true;
        }
        
        return false;
    }
    
    // Main UI entry points 
    @Override
    public void start(Stage primaryStage) {
        startMainSystem(primaryStage);
    }

    public static void startMainSystem(Stage primaryStage) {
        try {
            TrustSystem.primaryStage = primaryStage; // Store reference to the primary stage
            
            // Create the main menu scene
            mainLayout = new BorderPane(); // Use the class field instead of local variable
            mainLayout.setPrefSize(800, 600);   
            mainLayout.setStyle("-fx-background-color: #2e2e2e;");
            
            // Create a welcome label
            Label welcomeLabel = new Label("Welcome, " + SessionManager.getCurrentUser().getDisplayName() + "!");
            welcomeLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-size: 24px; -fx-padding: 20px;");
            mainLayout.setTop(welcomeLabel);
            
            // Create a sidebar with navigation buttons
            VBox sidebar = new VBox(10);
            sidebar.setPadding(new Insets(20));
            sidebar.setStyle("-fx-background-color: #1e1e1e;");
            sidebar.setPrefWidth(200);
            
            // Create menu buttons
            Button needsButton = UIStyleManager.createMenuButton("Needs", _ -> handleShowNeeds());
            Button ideasButton = UIStyleManager.createMenuButton("Ideas", _ -> handleShowIdeas());
            Button branchesButton = UIStyleManager.createMenuButton("Branches", _ -> handleShowBranches());
            Button traceButton = UIStyleManager.createMenuButton("Trace", _ -> handleShowTrace());
            Button proposalsButton = UIStyleManager.createMenuButton("Proposals", _ -> handleShowProposals());
            Button jobsButton = UIStyleManager.createMenuButton("Jobs", _ -> handleShowJobs()); // NEW Jobs button
            Button notificationsButton = UIStyleManager.createMenuButton("Notifications", _ -> handleShowNotifications()); // Existing notifications
            Button berriesButton = UIStyleManager.createMenuButton("Berries", _ -> handleShowBerries());
            Button profileButton = UIStyleManager.createMenuButton("My Profile", _ -> handleShowProfile());
            Button logoutButton = UIStyleManager.createMenuButton("Logout", _ -> SessionManager.handleLogout());
            
            // Add buttons to sidebar
            sidebar.getChildren().addAll(
                needsButton, ideasButton, branchesButton, traceButton,
                proposalsButton, jobsButton, notificationsButton, // Added Jobs button
                 berriesButton, profileButton,
                logoutButton
            );
            mainLayout.setLeft(sidebar);
            
            // Show the dashboard content
            showDashboard();
            
            // Create the scene and set it on the primary stage
            Scene scene = new Scene(mainLayout);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Trust System - Main");
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (Exception e) {
            DialogFactory.showError("Error starting main system: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleShowNotifications() {
    try {
        // Get current user
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            DialogFactory.showError("You must be logged in to view notifications.");
            return;
        }
        
        // Create a BorderPane for notifications layout
        BorderPane notificationsLayout = new BorderPane();
        notificationsLayout.setPadding(new Insets(20));
        notificationsLayout.setStyle("-fx-background-color: #2e2e2e;");
        
        // Create title label
        Label titleLabel = new Label("Your Notifications");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #d9d9d9; -fx-font-weight: bold;");
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        
        // Create container for notifications
        VBox notificationsContainer = new VBox(15);
        notificationsContainer.setPadding(new Insets(20, 0, 0, 0));
        
        // Collect branches related to needs and ideas the user has supported
        List<Branch> relevantBranches = new ArrayList<>();
        Map<Integer, Branch.Phase> lastSeenPhases = new HashMap<>();
        
        // Check supported or affected needs
        for (Need need : needs.values()) {
            if (need.getSupporters().containsKey(currentUser.getUsername()) || 
                need.getAffectedUsers().containsKey(currentUser.getUsername())) {
                
                // Find branches associated with this need
                for (Branch branch : branches.values()) {
                    if (branch.getNeeds().contains(need.getId())) {
                        relevantBranches.add(branch);
                        // For now, assume last seen phase is one behind current (we'll add proper tracking later)
                        Branch.Phase[] phases = Branch.Phase.values();
                        for (int i = 1; i < phases.length; i++) {
                            if (phases[i] == branch.getCurrentPhase()) {
                                lastSeenPhases.put(branch.getId(), phases[i-1]);
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        // Check supported ideas
        for (Idea idea : ideas.values()) {
            // Check if user voted for this idea
            if (idea.getSupporters().contains(currentUser.getUsername())) {
                // Find branches associated with this idea
                for (Branch branch : branches.values()) {
                    if (branch.getIdeaId() == idea.getId() && !relevantBranches.contains(branch)) {
                        relevantBranches.add(branch);
                        // For now, assume last seen phase is one behind current (we'll add proper tracking later)
                        Branch.Phase[] phases = Branch.Phase.values();
                        for (int i = 1; i < phases.length; i++) {
                            if (phases[i] == branch.getCurrentPhase()) {
                                lastSeenPhases.put(branch.getId(), phases[i-1]);
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        // If there are no notifications, show a message
        if (relevantBranches.isEmpty()) {
            Label emptyLabel = new Label("You don't have any notifications.");
            emptyLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-size: 16px;");
            notificationsContainer.getChildren().add(emptyLabel);
        } else {
            // Create notification cards for each branch
            for (Branch branch : relevantBranches) {
                // Create notification card
                VBox notificationCard = createNotificationCard(branch, lastSeenPhases.get(branch.getId()));
                notificationsContainer.getChildren().add(notificationCard);
            }
        }
        
        // Create scrollable view for notifications
        ScrollPane scrollPane = new ScrollPane(notificationsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2e2e2e; -fx-background-color: #2e2e2e;");
        
        // Add components to layout
        notificationsLayout.setTop(titleLabel);
        notificationsLayout.setCenter(scrollPane);
        
        // Set the notifications content in the main window
        setMainContent(notificationsLayout, "Notifications");
        
    } catch (Exception e) {
        e.printStackTrace();
        DialogFactory.showError("Error displaying notifications: " + e.getMessage());
    }
}

private static VBox createNotificationCard(Branch branch, Branch.Phase lastSeenPhase) {
    // Create card container
    VBox card = new VBox(10);
    card.setPadding(new Insets(15));
    card.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 5;");
    
    // Branch name and phase info
    String phaseName = branch.getCurrentPhase().toString();
    String ideaName = "N/A";
    if (branch.getIdeaId() > 0 && ideas.containsKey(branch.getIdeaId())) {
        ideaName = ideas.get(branch.getIdeaId()).getName();
    }
    
    // Create header with branch name
    Label nameLabel = new Label(branch.getName());
    nameLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #ffffff; -fx-font-weight: bold;");
    
    // Create labels for idea and phase
    Label ideaLabel = new Label("Associated Idea: " + ideaName);
    ideaLabel.setStyle("-fx-text-fill: #d9d9d9;");
    
    Label phaseChangeLabel = new Label("Phase changed from " + 
                                       (lastSeenPhase != null ? lastSeenPhase.toString() : "previous phase") + 
                                       " to " + phaseName);
    phaseChangeLabel.setStyle("-fx-text-fill: #d9d9d9;");
    
    // Add phase details if applicable
    VBox phaseDetails = new VBox(5);
    phaseDetails.setPadding(new Insets(10, 0, 10, 0));
    
    // Add satisfaction rating UI if there's a previous phase
    if (lastSeenPhase != null) {
        User currentUser = SessionManager.getCurrentUser();
        Rating existingRating = null;

        // Check if the user has already rated this specific phase
        try {
            existingRating = DatabaseManager.getRatingByUserForBranchPhase(
                    currentUser.getUsername(),
                    branch.getId(),
                    lastSeenPhase.name() // Check against the phase being rated
            );
        } catch (SQLException e) {
            System.err.println("Error checking for existing rating: " + e.getMessage());
            // Handle error appropriately, maybe disable rating for safety
        }

        // If already rated, show the previous rating
        if (existingRating != null) {
            Label alreadyRatedLabel = new Label(
                    "You already rated the " + lastSeenPhase.toString() +
                    " phase: " + existingRating.getRatingValue() + "%");
            alreadyRatedLabel.setStyle("-fx-text-fill: #76ff76;"); // Green text for confirmation
            phaseDetails.getChildren().add(alreadyRatedLabel);

            // --- Auxiliary Code Trigger ---
            // You could add a button here (maybe only visible to admins?)
            // that calls a method to delete the existing rating, allowing a re-rate.
            /*
            if (currentUser.isAdmin()) { // Assuming an isAdmin() check exists
                 Button allowReRateButton = UIStyleManager.createMenuButton("Allow Re-rate (Admin)", _ -> {
                     try {
                         DatabaseManager.deleteRating(existingRating.getId()); // Need to create deleteRating method
                         // Refresh the notification view or this specific card
                         handleShowNotifications(); // Simplest refresh
                     } catch (SQLException ex) {
                         DialogFactory.showError("Error deleting rating: " + ex.getMessage());
                     }
                 });
                 phaseDetails.getChildren().add(allowReRateButton);
            }
            */
            // --- End Auxiliary Code ---

        } else {
            // Not rated yet, show the rating UI
            Label rateLabel = new Label("Rate your satisfaction with the " + lastSeenPhase.toString() + " phase:");
            rateLabel.setStyle("-fx-text-fill: #d9d9d9;");

            Slider satisfactionSlider = new Slider(0, 100, 50);
            satisfactionSlider.setShowTickLabels(true);
            satisfactionSlider.setShowTickMarks(true);
            satisfactionSlider.setMajorTickUnit(25);
            satisfactionSlider.setMinorTickCount(5);
            satisfactionSlider.setBlockIncrement(10);
            
            // Create labels for min/max values
            HBox sliderLabels = new HBox();
            sliderLabels.setAlignment(Pos.CENTER);
            
            Label minLabel = new Label("0%");
            minLabel.setStyle("-fx-text-fill: #d9d9d9;");
            Label maxLabel = new Label("100%");
            maxLabel.setStyle("-fx-text-fill: #d9d9d9;");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            sliderLabels.getChildren().addAll(minLabel, spacer, maxLabel);
            
            // Create submit button
            Button submitRatingButton = UIStyleManager.createMenuButton("Submit Rating", _ -> {
                double ratingValue = satisfactionSlider.getValue();
                // Pass the phaseDetails VBox to update it after submission
                handleSubmitSatisfactionRating(branch, lastSeenPhase, ratingValue, phaseDetails);
            });
            submitRatingButton.setPrefWidth(150);
            
            phaseDetails.getChildren().addAll(rateLabel, satisfactionSlider, sliderLabels, submitRatingButton);
        }
    }
    
    // Create details button
    Button viewDetailsButton = UIStyleManager.createMenuButton("View Branch Details", _ -> {
        // Select this branch in the branches view
        handleShowBranches();
        
        // Need to find and select this branch in the branches list view
        // This functionality would be added in the handleShowBranches method
    });
    viewDetailsButton.setPrefWidth(180);
    
    // Add all components to the card
    card.getChildren().addAll(nameLabel, ideaLabel, phaseChangeLabel, phaseDetails, viewDetailsButton);
    
    return card;
}

// Modified handleSubmitSatisfactionRating to accept phaseDetails VBox
private static void handleSubmitSatisfactionRating(Branch branch, Branch.Phase phase, double rating, VBox phaseDetailsContainer) {
    try {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            DialogFactory.showError("Error: Not logged in.");
            return;
        }

        // Double-check if already rated before saving
        Rating existingRatingCheck = DatabaseManager.getRatingByUserForBranchPhase(
                currentUser.getUsername(),
                branch.getId(),
                phase.name()
        );
        if (existingRatingCheck != null) {
            DialogFactory.showError("Already Rated: You have already submitted a rating for this phase.");
            // Update UI to reflect it's already rated, similar to createNotificationCard
             phaseDetailsContainer.getChildren().clear();
             Label alreadyRatedLabel = new Label(
                     "Already rated: " + existingRatingCheck.getRatingValue() + "%");
             alreadyRatedLabel.setStyle("-fx-text-fill: #76ff76;");
             phaseDetailsContainer.getChildren().add(alreadyRatedLabel);
            return;
        }

        // Create a Rating object
        String raterUsername = currentUser.getUsername();
        int branchId = branch.getId();
        String phaseType = phase.name();
        int ratingValue = (int) Math.round(rating);
        String comment = ""; // Add a comment field later if needed

        Rating newRating = new Rating(raterUsername, branchId, phaseType, ratingValue, comment);

        // Save the Rating object using the correct method
        DatabaseManager.saveRating(newRating);

         // Update the UI to show the rating has been submitted
        phaseDetailsContainer.getChildren().clear(); // Clear the slider/button
        Label thankYouLabel = new Label("Thank you for your rating of " + ratingValue + "%");
        thankYouLabel.setStyle("-fx-text-fill: #76ff76;");
        phaseDetailsContainer.getChildren().add(thankYouLabel);

        // --- Optional: Decide if you still need the code below --- 
        // If the ratings table is the primary source, you might remove this.
        /*
        // Get the phase object
        PhaseBase phaseObj = null;
        switch (phase) {
            case GENERATION: phaseObj = branch.getGeneration(); break;
            case INVESTIGATION: phaseObj = branch.getInvestigation(); break;
            case DEVELOPMENT: phaseObj = branch.getDevelopment(); break;
            case PRODUCTION: phaseObj = branch.getProduction(); break;
            case DISTRIBUTION: phaseObj = branch.getDistribution(); break;
            case MAINTENANCE: phaseObj = branch.getMaintenance(); break;
            case RECYCLING: phaseObj = branch.getRecycling(); break;
            default: break; // No specific object for others yet
        }
        if (phaseObj != null) {
            phaseObj.setSatisfactionIndex(rating);
            DatabaseManager.savePhase(branch.getId(), phase, phaseObj);
        }
        */
        // --- End of Optional Section ---

        DialogFactory.showInfo("Rating Submitted",
                "Your satisfaction rating of " + ratingValue + "% for the " +
                phase.toString() + " phase has been recorded.");

    } catch (SQLException e) {
        DialogFactory.showError("Error saving satisfaction rating: " + e.getMessage());
        e.printStackTrace(); // Print stack trace for debugging
    } catch (Exception e) { // Catch other potential errors
        DialogFactory.showError("An unexpected error occurred: " + e.getMessage());
        e.printStackTrace();
    }
}
    
    // New method to display the dashboard content
    private static void showDashboard() {
        // Create content area with welcome message
        VBox contentArea = new VBox(15);
        contentArea.setPadding(new Insets(20));
        contentArea.setAlignment(Pos.CENTER);
        
        Label titleLabel = new Label("Trust System Dashboard");
        titleLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-size: 22px; -fx-font-weight: bold;");
        
        Label subtitleLabel = new Label("Select an option from the menu to get started");
        subtitleLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 16px;");
        
        // Add user stats
        User currentUser = SessionManager.getCurrentUser();
        VBox statsBox = new VBox(5);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(20));
        statsBox.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 5px;");
        
        Label levelLabel = new Label("Level: " + currentUser.getLevel());
        levelLabel.setStyle("-fx-text-fill: #d9d9d9;");
        
        Label xpLabel = new Label("XP: " + currentUser.getXp() + "/" + 
                SystemParameters.calculateXpThreshold(currentUser.getLevel()));
        xpLabel.setStyle("-fx-text-fill: #d9d9d9;");
        
        Label pointsLabel = new Label("Points: " + currentUser.getPoints());
        pointsLabel.setStyle("-fx-text-fill: #d9d9d9;");
        
        int berryCount = BerryService.getUserTotalBerries(currentUser.getUsername());
        Label berriesLabel = new Label("Berries: " + berryCount);
        berriesLabel.setStyle("-fx-text-fill: #d9d9d9;");
        
        statsBox.getChildren().addAll(levelLabel, xpLabel, pointsLabel, berriesLabel);
        
        contentArea.getChildren().addAll(titleLabel, subtitleLabel, statsBox);
        
        // Set the dashboard in the center of the main layout
        mainLayout.setCenter(contentArea);
        
        // Update the window title to reflect we're on the dashboard
        primaryStage.setTitle("Trust System - Dashboard");
    }
    
    // New method to update the content in the main window
    private static void setMainContent(Node content, String title) {
        mainLayout.setCenter(content);
        primaryStage.setTitle("Trust System - " + title);
    }
    
    // Example of UI handlers calling service methods
    private static void handleCreateNeed(String needName) {
        try {
            NeedService.createNeed(needName);
        } catch (SQLException e) {
            DialogFactory.showError("Error creating need: " + e.getMessage());
        }
    }
    
    private static void handleAllocatePoints(User user, Need need, int points, boolean affected, String location) {
        try {
            NeedService.allocatePointsToNeed(user, need, points, affected, location);
        } catch (SQLException e) {
            DialogFactory.showError("Error allocating points: " + e.getMessage());
        }
    }
    
    private static void handleSubmitIdea(String name, String description, String author, Set<Need> associatedNeeds) {
        try {
            int ideaId = IdeaService.submitIdea(name, description, author);
            if (ideaId != -1 && associatedNeeds != null && !associatedNeeds.isEmpty()) {
                IdeaService.associateIdeaWithNeeds(ideaId, associatedNeeds);
            }
        } catch (SQLException e) {
            DialogFactory.showError("Error submitting idea: " + e.getMessage());
        }
    }
    
    private static void handleVoteForIdea(int ideaId, User voter) {
        try {
            IdeaService.voteForIdea(ideaId, voter);
        } catch (SQLException e) {
            DialogFactory.showError("Error voting for idea: " + e.getMessage());
        }
    }
    
    private static void handleCreateBranch(String name, String description, int parentId, Integer ideaId) {
        try {
            BranchService.createBranch(name, description, parentId, ideaId);
        } catch (SQLException e) {
            DialogFactory.showError("Error creating branch: " + e.getMessage());
        }
    }
    
    private static void handleCreateLevelProposal(String author, double xpIncrease, double xpThreshold) {
        try {
            ProposalService.createLevelProposal( author, xpIncrease, xpThreshold);
        } catch (SQLException e) {
            DialogFactory.showError("Error creating level proposal: " + e.getMessage());
        }
    }
    
    private static void handleVoteForProposal(String proposalType, int proposalId, User voter) {
        try {
            ProposalService.voteForProposal(proposalType, proposalId, voter);
        } catch (SQLException e) {
            DialogFactory.showError("Error voting for proposal: " + e.getMessage());
        }
    }
    
    private static void handleConvertToBerries(User user, int points) {
        try {
            BerryService.convertToBerries(user, points);
        } catch (SQLException e) {
            DialogFactory.showError("Error converting to berries: " + e.getMessage());
        }
    }
    
    private static void handleAssociateBranchWithIdea(int branchId, int ideaId) {
        try {
            BranchService.associateBranchWithIdea(branchId, ideaId);
        } catch (SQLException e) {
            DialogFactory.showError("Error associating branch with idea: " + e.getMessage());
        }
    }
    
    // Add placeholder methods for menu items
    private static void handleShowNeeds() {
        try {
            // Create a BorderPane for needs management
            BorderPane needsLayout = new BorderPane();
            needsLayout.setStyle("-fx-background-color: #2e2e2e;");
            
            // Create header
            Label headerLabel = new Label("Needs Management");
            headerLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-size: 24px; -fx-padding: 20px;");
            needsLayout.setTop(headerLabel);
            
            // Create ListView for needs
            ListView<Need> needsListView = new ListView<>();
            needsListView.setStyle("-fx-background-color: #3e3e3e; -fx-control-inner-background: #3e3e3e;");
            
            // Populate the list with needs
            for (Need need : needs.values()) {
                needsListView.getItems().add(need);
            }
            
            // Custom cell factory for displaying needs
            needsListView.setCellFactory(_ -> new ListCell<Need>() {
                @Override
                protected void updateItem(Need need, boolean empty) {
                    super.updateItem(need, empty);
                    if (empty || need == null) {
                        setText(null);
                    } else {
                        setText(need.getName());
                        setStyle("-fx-text-fill: #d9d9d9;");
                    }
                }
            });
            
            // Create need details panel
            VBox detailsPanel = new VBox(10);
            detailsPanel.setPadding(new Insets(20));
            detailsPanel.setStyle("-fx-background-color: #3e3e3e;");
            
            Label detailsTitleLabel = new Label("Need Details");
            detailsTitleLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-size: 18px; -fx-font-weight: bold;");
            
            Label nameLabel = new Label("Name: ");
            nameLabel.setStyle("-fx-text-fill: #d9d9d9;");
            
            Label pointsLabel = new Label("Total Points: ");
            pointsLabel.setStyle("-fx-text-fill: #d9d9d9;");
            
            Label supportersLabel = new Label("Supporters: ");
            supportersLabel.setStyle("-fx-text-fill: #d9d9d9;");
            
            Label affectedLabel = new Label("Affected Users: ");
            affectedLabel.setStyle("-fx-text-fill: #d9d9d9;");
            
            detailsPanel.getChildren().addAll(
                detailsTitleLabel, nameLabel, pointsLabel, supportersLabel, affectedLabel
            );
            
            // Set up selection change listener to update the details panel
            needsListView.getSelectionModel().selectedItemProperty().addListener((_, __, newVal) -> {
                if (newVal != null) {
                    nameLabel.setText("Name: " + newVal.getName());
                    
                    int totalPoints = NeedService.calculateTotalPoints(newVal);
                    pointsLabel.setText("Total Points: " + totalPoints);
                    
                    supportersLabel.setText("Supporters: " + newVal.getSupporters().size());
                    affectedLabel.setText("Affected Users: " + newVal.getAffectedUsers().size());
                } else {
                    nameLabel.setText("Name: ");
                    pointsLabel.setText("Total Points: 0");
                    supportersLabel.setText("Supporters: 0");
                    affectedLabel.setText("Affected Users: 0");
                }
            });
            
            // Create action buttons panel
            VBox actionsPanel = new VBox(10);
            actionsPanel.setPadding(new Insets(20));
            actionsPanel.setStyle("-fx-background-color: #1e1e1e;");
            actionsPanel.setPrefWidth(200);
            actionsPanel.setAlignment(Pos.TOP_CENTER);
            
            Button createNeedButton = UIStyleManager.createMenuButton("Create Need", _ -> {
                TextInputDialog dialog = new TextInputDialog();
                // Apply styling to the dialog
                UIStyleManager.enhanceDialogWithKeyboardNavigation(dialog);
                dialog.setTitle("Create Need");
                dialog.setHeaderText("Enter the name for the new need:");
                dialog.setContentText("Name:");
                
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent() && !result.get().trim().isEmpty()) {
                    handleCreateNeed(result.get().trim());
                    
                    // Refresh the needs list
                    needsListView.getItems().clear();
                    for (Need need : needs.values()) {
                        needsListView.getItems().add(need);
                    }
                }
            });
            
            Button supportNeedButton = UIStyleManager.createMenuButton("Support Need", _ -> {
                Need selectedNeed = needsListView.getSelectionModel().getSelectedItem();
                if (selectedNeed == null) {
                    DialogFactory.showInfo("Selection Required", "Please select a need first.");
                    return;
                }
                
                User currentUser = SessionManager.getCurrentUser();
                
                // Create dialog for supporting a need
                Dialog<Map<String, Object>> dialog = new Dialog<>();
                UIStyleManager.enhanceDialogWithKeyboardNavigation(dialog); // Apply styling
                dialog.setTitle("Support Need");
                dialog.setHeaderText("Support Need: " + selectedNeed.getName());
                
                // Set the button types
                ButtonType supportButtonType = new ButtonType("Support", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(supportButtonType, ButtonType.CANCEL);
                
                // Create the form grid
                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));
                
                // Create points field
                TextField pointsField = new TextField();
                pointsField.setPromptText("Points");
                grid.add(new Label("Points:"), 0, 0);
                grid.add(pointsField, 1, 0);
                
                // Add grid to dialog
                dialog.getDialogPane().setContent(grid);
                
                // Request focus on points field
                Platform.runLater(() -> pointsField.requestFocus());
                
                // Set result converter
                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == supportButtonType) {
                        try {
                            Map<String, Object> result = new HashMap<>();
                            result.put("points", Integer.parseInt(pointsField.getText().trim()));
                            return result;
                        } catch (NumberFormatException ex) {
                            DialogFactory.showInfo("Invalid Input", "Please enter a valid number for points.");
                            return null;
                        }
                    }
                    return null;
                });
                
                // Process the result
                Optional<Map<String, Object>> result = dialog.showAndWait();
                result.ifPresent(data -> {
                    int points = (int) data.get("points");
                    
                    // Call the service method to allocate points (as supporter)
                    handleAllocatePoints(currentUser, selectedNeed, points, false, "");
                    
                    // Refresh the needs list and update selection
                    Need updatedNeed = needs.get(selectedNeed.getId());
                    
                    // Trigger update of the details panel
                    needsListView.getSelectionModel().clearSelection();
                    needsListView.getSelectionModel().select(updatedNeed);
                });
            });
            
            Button markAsAffectedButton = UIStyleManager.createMenuButton("Mark as Affected", _ -> {
                Need selectedNeed = needsListView.getSelectionModel().getSelectedItem();
                if (selectedNeed == null) {
                    DialogFactory.showInfo("Selection Required", "Please select a need first.");
                    return;
                }
                
                User currentUser = SessionManager.getCurrentUser();
                
                // Create dialog for marking as affected
                Dialog<Map<String, Object>> dialog = new Dialog<>();
                UIStyleManager.enhanceDialogWithKeyboardNavigation(dialog); // Apply styling
                dialog.setTitle("Mark as Affected");
                dialog.setHeaderText("Mark as Affected by: " + selectedNeed.getName());
                
                // Set the button types
                ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
                
                // Create the form grid
                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));
                
                // Points field (fixed at 5 for affected users)
                TextField pointsField = new TextField("5");
                pointsField.setPromptText("Points to contribute");
                pointsField.setDisable(true); // Fixed value for affected users
                
                // Location field
                TextField locationField = new TextField();
                locationField.setPromptText("Your location");
                
                // Create labels with white text styling
                Label availablePointsLabel = new Label("Available points: " + currentUser.getPoints());
                availablePointsLabel.setStyle("-fx-text-fill: white;");
                
                Label pointsLabel2 = new Label("Points (fixed):");
                pointsLabel2.setStyle("-fx-text-fill: white;");
                
                Label locationLabel = new Label("Location:");
                locationLabel.setStyle("-fx-text-fill: white;");
                
                // Add fields to grid with styled labels
                grid.add(availablePointsLabel, 0, 0, 2, 1);
                grid.add(pointsLabel2, 0, 1);
                grid.add(pointsField, 1, 1);
                grid.add(locationLabel, 0, 2);
                grid.add(locationField, 1, 2);
                
                // Style the dialog
                DialogPane dialogPane = dialog.getDialogPane();
                dialogPane.setContent(grid);
                dialogPane.getStyleClass().add("dialog");
                dialogPane.setStyle("-fx-background-color: #3e3e3e;");
                
                // Style the header text to be white
                dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
                
                // Set result converter
                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == confirmButtonType) {
                        int points = 5; // Fixed points for affected users
                        
                        if (points > currentUser.getPoints()) {
                            DialogFactory.showInfo("Insufficient Points", 
                                "You need 5 points to mark yourself as affected. You only have " + 
                                currentUser.getPoints() + " points available.");
                            return null;
                        }
                        
                        Map<String, Object> result = new HashMap<>();
                        result.put("points", points);
                        result.put("location", locationField.getText().trim());
                        
                        return result;
                    }
                    return null;
                });
                
                // Process the result
                Optional<Map<String, Object>> result = dialog.showAndWait();
                result.ifPresent(data -> {
                    int points = (int) data.get("points");
                    String location = (String) data.get("location");
                    
                    // Call the service method to allocate points (as affected)
                    handleAllocatePoints(currentUser, selectedNeed, points, true, location);
                    
                    // Refresh the needs list and update selection
                    Need updatedNeed = needs.get(selectedNeed.getId());
                    
                    // Trigger update of the details panel
                    needsListView.getSelectionModel().clearSelection();
                    needsListView.getSelectionModel().select(updatedNeed);
                });
            });
            
            // Replace the Close button with a Back button that returns to the dashboard
            Button backButton = UIStyleManager.createMenuButton("Back to Dashboard", _ -> showDashboard());
            
            // Add buttons to the actions panel
            actionsPanel.getChildren().addAll(
                createNeedButton, supportNeedButton, markAsAffectedButton, backButton
            );
            
            // Create a split pane to contain the list and details
            BorderPane contentPane = new BorderPane();
            contentPane.setCenter(needsListView);
            contentPane.setBottom(detailsPanel);
            
            // Add all components to the needs layout
            needsLayout.setCenter(contentPane);
            needsLayout.setRight(actionsPanel);
            
            // Set the needs content in the main window
            setMainContent(needsLayout, "Needs Management");
            
        } catch (Exception e) {
            e.printStackTrace();
            DialogFactory.showError("Error displaying needs view: " + e.getMessage());
        }
    }
    
    private static void handleShowIdeas() {
        try {
            // Create a BorderPane for ideas management
            BorderPane ideasLayout = new BorderPane();
            ideasLayout.setStyle("-fx-background-color: #2e2e2e;");
            
            // Create header
            Label headerLabel = new Label("Ideas Management");
            headerLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-size: 24px; -fx-padding: 20px;");
            ideasLayout.setTop(headerLabel);
            
            // Create ListView for ideas
            ListView<Idea> ideasListView = new ListView<>();
            ideasListView.setStyle("-fx-background-color: #3e3e3e; -fx-control-inner-background: #3e3e3e;");
            
            // Populate the list with ideas
            for (Idea idea : ideas.values()) {
                ideasListView.getItems().add(idea);
            }
            
            // Custom cell factory for displaying ideas
            ideasListView.setCellFactory(_ -> new ListCell<Idea>() {
                @Override
                protected void updateItem(Idea idea, boolean empty) {
                    super.updateItem(idea, empty);
                    if (empty || idea == null) {
                        setText(null);
                    } else {
                        setText(idea.getName() + " (by " + idea.getAuthor() + ")");
                        setStyle("-fx-text-fill: #d9d9d9;");
                    }
                }
            });
            
            // Create idea details panel
            VBox detailsPanel = new VBox(10);
            detailsPanel.setPadding(new Insets(20));
            detailsPanel.setStyle("-fx-background-color: #3e3e3e;");
            
            Label detailsTitleLabel = new Label("Idea Details");
            detailsTitleLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-size: 18px; -fx-font-weight: bold;");
            
            Label nameLabel = new Label("Name: ");
            nameLabel.setStyle("-fx-text-fill: #d9d9d9;");
            
            Label authorLabel = new Label("Author: ");
            authorLabel.setStyle("-fx-text-fill: #d9d9d9;");
            
            Label descriptionLabel = new Label("Description: ");
            descriptionLabel.setStyle("-fx-text-fill: #d9d9d9;");
            
            Label votesLabel = new Label("Votes: ");
            votesLabel.setStyle("-fx-text-fill: #d9d9d9;");
            
            Label linkedNeedsLabel = new Label("Linked Needs: ");
            linkedNeedsLabel.setStyle("-fx-text-fill: #d9d9d9;");
            
            detailsPanel.getChildren().addAll(
                detailsTitleLabel, nameLabel, authorLabel, descriptionLabel, votesLabel, linkedNeedsLabel
            );
            
            // Set up selection change listener to update the details panel
            ideasListView.getSelectionModel().selectedItemProperty().addListener((_, __, newVal) -> {
                if (newVal != null) {
                    nameLabel.setText("Name: " + newVal.getName());
                    authorLabel.setText("Author: " + newVal.getAuthor());
                    descriptionLabel.setText("Description: " + newVal.getDescription());
                    // Show votes as the number of supporters, not voteCount
                    votesLabel.setText("Votes: " + newVal.getSupporters().size());
                    // Get linked needs information
                    StringBuilder linkedNeeds = new StringBuilder();
                    for (Integer needId : newVal.getAssociatedNeedIds()) {
                        if (needs.containsKey(needId)) {
                            if (linkedNeeds.length() > 0) {
                                linkedNeeds.append(", ");
                            }
                            linkedNeeds.append(needs.get(needId).getName());
                        }
                    }
                    if (linkedNeeds.length() == 0) {
                        linkedNeeds.append("None");
                    }
                    linkedNeedsLabel.setText("Linked Needs: " + linkedNeeds.toString());
                } else {
                    nameLabel.setText("Name: ");
                    authorLabel.setText("Author: ");
                    descriptionLabel.setText("Description: ");
                    votesLabel.setText("Votes: 0");
                    linkedNeedsLabel.setText("Linked Needs: ");
                }
            });
            
            // Create action buttons panel
            VBox actionsPanel = new VBox(10);
            actionsPanel.setPadding(new Insets(20));
            actionsPanel.setStyle("-fx-background-color: #1e1e1e;");
            actionsPanel.setPrefWidth(200);
            actionsPanel.setAlignment(Pos.TOP_CENTER);
            
            // Add "Submit Idea" button
            Button submitIdeaButton = UIStyleManager.createMenuButton("Submit Idea", _ -> {
                // Create dialog for submitting a new idea
                Dialog<Map<String, Object>> dialog = new Dialog<>();
                UIStyleManager.enhanceDialogWithKeyboardNavigation(dialog); // Apply styling
                dialog.setTitle("Submit New Idea");
                dialog.setHeaderText("Enter details for your new idea");
                
                // Create grid pane for form layout
                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));
                
                TextField nameField = new TextField();
                nameField.setPromptText("Idea Name");
                
                TextArea descriptionArea = new TextArea();
                descriptionArea.setPromptText("Idea Description");
                descriptionArea.setPrefRowCount(5);
                
                // Create a ListView to select associated needs
                ListView<Need> needsSelectionView = new ListView<>();
                needsSelectionView.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
                needsSelectionView.setPrefHeight(150);
                
                // Populate the needs selection list
                for (Need need : needs.values()) {
                    needsSelectionView.getItems().add(need);
                }
                
                // Custom cell factory for needs
                needsSelectionView.setCellFactory(_ -> new ListCell<Need>() {
                    @Override
                    protected void updateItem(Need need, boolean empty) {
                        super.updateItem(need, empty);
                        if (empty || need == null) {
                            setText(null);
                        } else {
                            setText(need.getName());
                        }
                    }
                });
                
                // Add fields to the grid
                grid.add(new Label("Name:"), 0, 0);
                grid.add(nameField, 1, 0);
                grid.add(new Label("Description:"), 0, 1);
                grid.add(descriptionArea, 1, 1);
                grid.add(new Label("Associated Needs:"), 0, 2);
                // Define button types
                ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);
                
                dialog.getDialogPane().setContent(grid);
                
                // Request focus on name field
                Platform.runLater(() -> nameField.requestFocus());
                
                // Set result converter to gather form data
                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == submitButtonType) {
                        // Validate required fields
                        if (nameField.getText().trim().isEmpty() || descriptionArea.getText().trim().isEmpty()) {
                            DialogFactory.showError("Name and Description are required.");
                            return null;
                        }
                        
                        Map<String, Object> result = new HashMap<>();
                        result.put("name", nameField.getText().trim());
                        result.put("description", descriptionArea.getText().trim());
                        result.put("selectedNeeds", new HashSet<>(needsSelectionView.getSelectionModel().getSelectedItems()));
                        return result;
                    }
                    return null;
                });
                
                // Process the result
                Optional<Map<String, Object>> result = dialog.showAndWait();
                result.ifPresent(data -> {
                    String name = (String) data.get("name");
                    String description = (String) data.get("description");
                    // Corrected unchecked cast
                    @SuppressWarnings("unchecked") // Suppress warning as we know the type from dialog creation
                    Set<Need> selectedNeeds = (Set<Need>) data.get("selectedNeeds"); 
                    String author = SessionManager.getCurrentUser().getUsername();
                    
                    // Submit the idea
                    handleSubmitIdea(name, description, author, selectedNeeds);
                    
                    // Refresh the ideas list
                    ideasListView.getItems().clear();
                    for (Idea idea : ideas.values()) {
                        ideasListView.getItems().add(idea);
                    }
                });
            });
            
            // Add "Vote for Idea" button
            Button voteButton = UIStyleManager.createMenuButton("Vote for Idea", _ -> {
                Idea selectedIdea = ideasListView.getSelectionModel().getSelectedItem();
                if (selectedIdea == null) {
                    DialogFactory.showInfo("Selection Required", "Please select an idea to vote for.");
                    return;
                }
                
                // Check if the user is eligible to vote (supporter or affected by any linked need)
                User currentUser = SessionManager.getCurrentUser();
                boolean isEligible = false;
                
                for (Integer needId : selectedIdea.getAssociatedNeedIds()) {
                    if (needs.containsKey(needId)) {
                        Need need = needs.get(needId);
                        if (need.getSupporters().containsKey(currentUser.getUsername()) ||
                            need.getAffectedUsers().containsKey(currentUser.getUsername())) {
                            isEligible = true;
                            break;
                        }
                    }
                }
                
                if (!isEligible) {
                    DialogFactory.showInfo("Not Eligible", 
                        "You must be a supporter or affected by at least one of the linked needs to vote for this idea.");
                    return;
                }
                
                // Handle vote
                handleVoteForIdea(selectedIdea.getId(), currentUser);
                
                // Refresh the ideas list and update selection
                Idea updatedIdea = ideas.get(selectedIdea.getId());
                ideasListView.getItems().clear();
                for (Idea idea : ideas.values()) {
                    ideasListView.getItems().add(idea);
                }
                
                // Reselect the idea to update the details
                ideasListView.getSelectionModel().select(updatedIdea);
            });
            
            // Add "Back to Dashboard" button
            Button backButton = UIStyleManager.createMenuButton("Back to Dashboard", _ -> showDashboard());
            
            // Add buttons to the actions panel
            actionsPanel.getChildren().addAll(submitIdeaButton, voteButton, backButton);
            
            // Create a BorderPane to contain the list and details
            BorderPane contentPane = new BorderPane();
            contentPane.setCenter(ideasListView);
            contentPane.setBottom(detailsPanel);
            
            // Add all components to the ideas layout
            ideasLayout.setCenter(contentPane);
            ideasLayout.setRight(actionsPanel);
            
            // Set the ideas content in the main window
            setMainContent(ideasLayout, "Ideas Management");
            
        } catch (Exception e) {
            e.printStackTrace();
            DialogFactory.showError("Error displaying ideas view: " + e.getMessage());
        }
    }
    
    private static void handleShowBranches() {
        try {
            // Force reload branches directly from database before displaying them
            try {
                System.out.println("Directly loading branches from database before displaying UI...");
                branches = DatabaseManager.loadAllBranches();
                
                // Debug: List all loaded branches and their names
                System.out.println("Loaded " + branches.size() + " branches:");
                for (Branch branch : branches.values()) {
                    System.out.println("  Branch ID: " + branch.getId() + 
                                      ", Name: \"" + branch.getName() + "\"" +
                                      ", Description: \"" + branch.getDescription() + "\"");
                }
            } catch (SQLException ex) {
                System.err.println("Error loading branches: " + ex.getMessage());
                ex.printStackTrace();
            }
            
            // Create a BorderPane for branches management
            BorderPane branchesLayout = new BorderPane();
            branchesLayout.setStyle("-fx-background-color: #2e2e2e;");
            
            // Create header
            Label headerLabel = new Label("Branches Management");
            headerLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-size: 24px; -fx-padding: 20px;");
            branchesLayout.setTop(headerLabel);
            
            // Create main content area with split panes
            BorderPane contentPane = new BorderPane();
            
            // Create ListView for branches
            ListView<Branch> branchesListView = new ListView<>();
            branchesListView.setStyle("-fx-background-color: #3e3e3e; -fx-control-inner-background: #3e3e3e;");
            
            // Populate the list with branches
            for (Branch branch : branches.values()) {
                branchesListView.getItems().add(branch);
            }
            
            // Custom cell factory for displaying branches
            branchesListView.setCellFactory(_ -> new ListCell<Branch>() {
                @Override
                protected void updateItem(Branch branch, boolean empty) {
                    super.updateItem(branch, empty);
                    if (empty || branch == null) {
                        setText(null);
                    } else {
                        String branchName = branch.getName();
                        if (branchName == null || branchName.trim().isEmpty()) {
                            branchName = "Branch #" + branch.getId(); // Fallback name
                        }
                        
                        String ideaInfo = "";
                        if (branch.getIdeaId() > 0 && ideas.containsKey(branch.getIdeaId())) {
                            ideaInfo = " - Idea: " + ideas.get(branch.getIdeaId()).getName();
                        }
                        setText(branchName + " (Phase: " + branch.getCurrentPhase() + ")" + ideaInfo);
                        setStyle("-fx-text-fill: #d9d9d9;");
                    }
                }
            });
            
            // Branch details panel
            VBox detailsPanel = new VBox(10);
            detailsPanel.setPadding(new Insets(20));
            detailsPanel.setStyle("-fx-background-color: #3e3e3e;");
            
            Label detailsTitleLabel = new Label("Branch Details");
            detailsTitleLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-size: 18px; -fx-font-weight: bold;");
            
            Label nameLabel = new Label("Name: ");
            nameLabel.setStyle("-fx-text-fill: #d9d9d9;");
            
            Label phaseLabel = new Label("Current Phase: ");
            phaseLabel.setStyle("-fx-text-fill: #d9d9d9;");
            
            Label descriptionLabel = new Label("Description: ");
            descriptionLabel.setStyle("-fx-text-fill: #d9d9d9;");
            
            Label ideaLabel = new Label("Associated Idea: ");
            ideaLabel.setStyle("-fx-text-fill: #d9d9d9;");
            
            Label teamSizeLabel = new Label("Team Size: ");
            teamSizeLabel.setStyle("-fx-text-fill: #d9d9d9;");
            
            Label openingsLabel = new Label("Team Openings: ");
            openingsLabel.setStyle("-fx-text-fill: #d9d9d9;");
            
            Label candidatesLabel = new Label("Candidates: ");
            candidatesLabel.setStyle("-fx-text-fill: #d9d9d9;");
            
            detailsPanel.getChildren().addAll(
                detailsTitleLabel, nameLabel, phaseLabel, descriptionLabel, 
                ideaLabel, teamSizeLabel, openingsLabel, candidatesLabel
            );
            
            // Set up selection change listener to update the details panel
            branchesListView.getSelectionModel().selectedItemProperty().addListener((_, __, newVal) -> {
                if (newVal != null) {
                    nameLabel.setText("Name: " + newVal.getName());
                    phaseLabel.setText("Current Phase: " + newVal.getCurrentPhase());
                    descriptionLabel.setText("Description: " + newVal.getDescription());
                    
                    String ideaInfo = "None";
                    if (newVal.getIdeaId() > 0 && ideas.containsKey(newVal.getIdeaId())) {
                        ideaInfo = ideas.get(newVal.getIdeaId()).getName();
                    }
                    ideaLabel.setText("Associated Idea: " + ideaInfo);
                    
                    ArrayList<String> team = newVal.getTeam();
                    teamSizeLabel.setText("Team Size: " + (team != null ? team.size() : 0));
                    openingsLabel.setText("Team Openings: " + newVal.getTeamOpenings());
                    
                    ArrayList<String> candidates = newVal.getCandidates();
                    candidatesLabel.setText("Candidates: " + (candidates != null ? candidates.size() : 0));
                } else {
                    nameLabel.setText("Name: ");
                    phaseLabel.setText("Current Phase: ");
                    descriptionLabel.setText("Description: ");
                    ideaLabel.setText("Associated Idea: ");
                    teamSizeLabel.setText("Team Size: 0");
                    openingsLabel.setText("Team Openings: 0");
                    candidatesLabel.setText("Candidates: 0");
                }
            });
            
            // Create action buttons panel
            VBox actionsPanel = new VBox(10);
            actionsPanel.setPadding(new Insets(20));
            actionsPanel.setStyle("-fx-background-color: #1e1e1e;");
            actionsPanel.setPrefWidth(200);
            actionsPanel.setAlignment(Pos.TOP_CENTER);
            
            // 1. Create Branch button
            Button createBranchButton = UIStyleManager.createMenuButton("Create Branch", _ -> {
                // Create dialog for creating a new branch
                Dialog<Map<String, Object>> dialog = new Dialog<>();
                dialog.setTitle("Create Branch");
                dialog.setHeaderText("Enter details for the new branch");
                
                ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
                
                // Create form content
                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));
                
                TextField nameField = new TextField();
                nameField.setPromptText("Branch Name");
                
                TextArea descriptionArea = new TextArea();
                descriptionArea.setPromptText("Branch Description");
                descriptionArea.setPrefRowCount(3);
                
                // Create parent branch dropdown
                ComboBox<Branch> parentComboBox = new ComboBox<>();
                parentComboBox.setPromptText("Parent Branch (Optional)");
                
                // Add a "None" option
                parentComboBox.getItems().add(null);
                
                // Add all branches
                for (Branch branch : branches.values()) {
                    parentComboBox.getItems().add(branch);
                }
                
                // Set cell factory for the combo box
                parentComboBox.setCellFactory(_ -> new ListCell<Branch>() {
                    @Override
                    protected void updateItem(Branch branch, boolean empty) {
                        super.updateItem(branch, empty);
                        if (branch == null) {
                            setText("None (Root Branch)");
                        } else if (!empty) {
                            setText(branch.getName());
                        } else {
                            setText("");
                        }
                    }
                });
                
                // Set button cell factory for selected value display
                parentComboBox.setButtonCell(new ListCell<Branch>() {
                    @Override
                    protected void updateItem(Branch branch, boolean empty) {
                        super.updateItem(branch, empty);
                        if (branch == null) {
                            setText("None (Root Branch)");
                        } else if (!empty) {
                            setText(branch.getName());
                        } else {
                            setText("");
                        }
                    }
                });
                
                // Create idea selection ListView
                ComboBox<Idea> ideaComboBox = new ComboBox<>();
                ideaComboBox.setPromptText("Associated Idea");
                
                // Populate the idea combo box
                for (Idea idea : ideas.values()) {
                    ideaComboBox.getItems().add(idea);
                }
                
                // Set cell factory for the combo box
                ideaComboBox.setCellFactory(_ -> new ListCell<Idea>() {
                    @Override
                    protected void updateItem(Idea idea, boolean empty) {
                        super.updateItem(idea, empty);
                        if (empty || idea == null) {
                            setText("");
                        } else {
                            setText(idea.getName());
                        }
                    }
                });
                
                // Set button cell factory for selected value display
                ideaComboBox.setButtonCell(new ListCell<Idea>() {
                    @Override
                    protected void updateItem(Idea idea, boolean empty) {
                        super.updateItem(idea, empty);
                        if (empty || idea == null) {
                            setText("");
                        } else {
                            setText(idea.getName());
                        }
                    }
                });
                
                // Add fields to the grid
                grid.add(new Label("Name:"), 0, 0);
                grid.add(nameField, 1, 0);
                grid.add(new Label("Description:"), 0, 1);
                grid.add(descriptionArea, 1, 1);
                grid.add(new Label("Parent Branch:"), 0, 2);
                grid.add(parentComboBox, 1, 2);
                grid.add(new Label("Associated Idea:"), 0, 3);
                grid.add(ideaComboBox, 1, 3);
                
                dialog.getDialogPane().setContent(grid);
                
                // Apply styling
                dialog.getDialogPane().setStyle("-fx-background-color: #3e3e3e;");
                dialog.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #3e3e3e;");
                dialog.getDialogPane().lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
                
                // Request focus on name field
                Platform.runLater(() -> nameField.requestFocus());
                
                // Set result converter to gather form data
                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == createButtonType) {
                        // Validate required fields
                        if (nameField.getText().trim().isEmpty()) {
                            DialogFactory.showError("Branch name is required.");
                            return null;
                        }
                        
                        Map<String, Object> result = new HashMap<>();
                        result.put("name", nameField.getText().trim());
                        result.put("description", descriptionArea.getText().trim());
                        result.put("parentBranch", parentComboBox.getValue());
                        result.put("idea", ideaComboBox.getValue());
                        return result;
                    }
                    return null;
                });
                
                // Process the result
                Optional<Map<String, Object>> result = dialog.showAndWait();
                result.ifPresent(data -> {
                    String name = (String) data.get("name");
                    String description = (String) data.get("description");
                    Branch parentBranch = (Branch) data.get("parentBranch");
                    Idea associatedIdea = (Idea) data.get("idea");
                    
                    int parentId = parentBranch != null ? parentBranch.getId() : 0;
                    
                    // Create the branch
                    try {
                        int branchId = BranchService.createBranch(name, description, parentId, associatedIdea != null ? associatedIdea.getId() : -1);
                        if (branchId == -1) {
                            DialogFactory.showError("Failed to create branch.");
                            return;
                        }
                        
                        // Refresh the branches list
                        refreshBranchesList(branchesListView);
                        
                    } catch (SQLException ex) {
                        DialogFactory.showError("Error creating branch: " + ex.getMessage());
                    }
                });
            });
            
            // 2. Set Team Openings button
            Button setOpeningsButton = UIStyleManager.createMenuButton("Set Team Openings", _ -> {
                Branch selectedBranch = branchesListView.getSelectionModel().getSelectedItem();
                if (selectedBranch == null) {
                    DialogFactory.showInfo("Selection Required", "Please select a branch first.");
                    return;
                }
                
                // Load latest expertise requirements from the database before showing dialog
                try {
                    selectedBranch.loadExpertiseRequirements();
                } catch (SQLException ex) {
                    DialogFactory.showError("Error loading expertise requirements: " + ex.getMessage());
                    return;
                }

                // Create a custom dialog for setting team openings with expertise requirements
                Dialog<Map<Integer, Integer>> dialog = new Dialog<>();
                dialog.setTitle("Set Team Openings");
                dialog.setHeaderText("Set team openings for " + selectedBranch.getName());
                
                // Set the button types
                ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
                
                // Create the content grid
                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));
                
                // Create an ObservableList to store expertise requirements (for TableView)
                javafx.collections.ObservableList<ExpertiseRequirement> expertiseRequirements = 
                    javafx.collections.FXCollections.observableArrayList();
                
                // Create a TableView to display expertise requirements
                javafx.scene.control.TableView<ExpertiseRequirement> requirementsTable = new javafx.scene.control.TableView<>();
                requirementsTable.setPrefHeight(200);
                requirementsTable.setEditable(true);
                
                // Create and configure columns
                javafx.scene.control.TableColumn<ExpertiseRequirement, String> expertiseCol = 
                    new javafx.scene.control.TableColumn<>("Field of Expertise");
                expertiseCol.setPrefWidth(220);
                expertiseCol.setCellValueFactory(cellData -> {
                    Integer expertiseId = cellData.getValue().getExpertiseId();
                    if (expertiseId == -1) {
                        return new javafx.beans.property.SimpleStringProperty("None (General)");
                    } else if (TrustSystem.fieldsOfExpertise.containsKey(expertiseId)) {
                        return new javafx.beans.property.SimpleStringProperty(
                            TrustSystem.fieldsOfExpertise.get(expertiseId).getName());
                    } else {
                        return new javafx.beans.property.SimpleStringProperty("Unknown (" + expertiseId + ")");
                    }
                });
                
                javafx.scene.control.TableColumn<ExpertiseRequirement, Integer> countCol = 
                    new javafx.scene.control.TableColumn<>("Openings");
                countCol.setPrefWidth(80);
                countCol.setCellValueFactory(cellData -> 
                    new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getCount()).asObject());
                
                // Set up editable cells for the count column
                countCol.setCellFactory(_ -> new javafx.scene.control.TableCell<ExpertiseRequirement, Integer>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            javafx.scene.control.TextField textField = new javafx.scene.control.TextField(item.toString());
                            textField.focusedProperty().addListener((_, __, isNowFocused) -> {
                                if (!isNowFocused) {
                                    try {
                                        int value = Integer.parseInt(textField.getText());
                                        if (value < 0) value = 0;
                                        ExpertiseRequirement requirement = getTableView().getItems().get(getIndex());
                                        requirement.setCount(value);
                                        getTableView().refresh();
                                    } catch (NumberFormatException e) {
                                        textField.setText(item.toString());
                                    }
                                }
                            });
                            setGraphic(textField);
                            setText(null);
                        }
                    }
                });
                
                requirementsTable.getColumns().addAll(expertiseCol, countCol);
                requirementsTable.setItems(expertiseRequirements);
                
                // Create buttons for adding/removing expertise requirements
                Button addButton = new Button("Add Expertise");
                addButton.setOnAction(_ -> {
                    // Create a dialog to select expertise
                    Dialog<Integer> expertiseDialog = new Dialog<>();
                    expertiseDialog.setTitle("Add Expertise Requirement");
                    expertiseDialog.setHeaderText("Select a field of expertise");
                    
                    ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
                    expertiseDialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);
                    
                    // Create a ComboBox for expertise selection
                    javafx.scene.control.ComboBox<FieldOfExpertise> expertiseComboBox = 
                        new javafx.scene.control.ComboBox<>();
                    
                    // Add "None" option (general opening with no specific expertise)
                    FieldOfExpertise noneOption = new FieldOfExpertise(-1, "None (General)", "No specific expertise required", null);
                    expertiseComboBox.getItems().add(noneOption);
                    
                    // Add all fields of expertise
                    List<FieldOfExpertise> allExpertiseFields = new ArrayList<>(TrustSystem.fieldsOfExpertise.values());
                    Collections.sort(allExpertiseFields, Comparator.comparing(FieldOfExpertise::getName));
                    expertiseComboBox.getItems().addAll(allExpertiseFields);
                    
                    // Setup combo box display
                    expertiseComboBox.setCellFactory(_ -> new ListCell<FieldOfExpertise>() {
                        @Override
                        protected void updateItem(FieldOfExpertise item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                setText(item.getName());
                            }
                        }
                    });
                    expertiseComboBox.setButtonCell(new ListCell<FieldOfExpertise>() {
                        @Override
                        protected void updateItem(FieldOfExpertise item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                setText(item.getName());
                            }
                        }
                    });
                    
                    // Add a TextField for number of openings
                    TextField countField = new TextField("1");
                    countField.setPromptText("Number of openings");
                    
                    // Create a grid for the dialog content
                    GridPane expertiseGrid = new GridPane();
                    expertiseGrid.setHgap(10);
                    expertiseGrid.setVgap(10);
                    expertiseGrid.setPadding(new Insets(20, 150, 10, 10));
                    expertiseGrid.add(new Label("Field of Expertise:"), 0, 0);
                    expertiseGrid.add(expertiseComboBox, 1, 0);
                    expertiseGrid.add(new Label("Number of Openings:"), 0, 1);
                    expertiseGrid.add(countField, 1, 1);
                    
                    expertiseDialog.getDialogPane().setContent(expertiseGrid);
                    UIStyleManager.enhanceDialogWithKeyboardNavigation(expertiseDialog);
                    
                    // Set the result converter
                    expertiseDialog.setResultConverter(dialogButton -> {
                        if (dialogButton == selectButtonType) {
                            FieldOfExpertise selected = expertiseComboBox.getValue();
                            if (selected != null) {
                                try {
                                    int count = Integer.parseInt(countField.getText());
                                    if (count > 0) {
                                        // Check if this expertise is already in the list
                                        boolean alreadyExists = false;
                                        for (ExpertiseRequirement req : expertiseRequirements) {
                                            if (req.getExpertiseId() == selected.getId()) {
                                                req.setCount(req.getCount() + count);
                                                alreadyExists = true;
                                                break;
                                            }
                                        }
                                        
                                        if (!alreadyExists) {
                                            // Add new requirement
                                            expertiseRequirements.add(new ExpertiseRequirement(selected.getId(), count));
                                        }
                                        
                                        requirementsTable.refresh();
                                    }
                                } catch (NumberFormatException a) {
                                    // Invalid number, do nothing
                                }
                            }
                        }
                        return null;
                    });
                    
                    expertiseDialog.showAndWait();
                });
                
                Button removeButton = new Button("Remove Selected");
                removeButton.setOnAction(_ -> {
                    ExpertiseRequirement selected = requirementsTable.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        expertiseRequirements.remove(selected);
                    }
                });
                
                // Show the current expertise requirements if any
                try {
                    Map<Integer, Integer> currentRequirements = selectedBranch.getExpertiseRequirements();
                    if (currentRequirements != null) {
                        for (Map.Entry<Integer, Integer> entry : currentRequirements.entrySet()) {
                            if (entry.getValue() > 0) {
                                expertiseRequirements.add(new ExpertiseRequirement(entry.getKey(), entry.getValue()));
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Error loading current expertise requirements: " + ex.getMessage());
                }
                
                // Label for total openings
                Label totalOpeningsLabel = new Label("Total Openings: 0");
                
                // Update total openings when the requirements change
                javafx.collections.ListChangeListener<ExpertiseRequirement> listener = _ -> {
                    int total = 0;
                    for (ExpertiseRequirement req : expertiseRequirements) {
                        total += req.getCount();
                    }
                    totalOpeningsLabel.setText("Total Openings: " + total);
                };
                expertiseRequirements.addListener(listener);
                
                // Trigger the listener to initialize the total
                listener.onChanged(null);
                
                // Create a horizontal box for buttons
                HBox buttonsBox = new HBox(10, addButton, removeButton);
                
                // Add components to the grid
                grid.add(requirementsTable, 0, 0);
                grid.add(buttonsBox, 0, 1);
                grid.add(totalOpeningsLabel, 0, 2);
                
                dialog.getDialogPane().setContent(grid);
                
                // Apply styling
                dialog.getDialogPane().setStyle("-fx-background-color: #3e3e3e;");
                dialog.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #3e3e3e;");
                dialog.getDialogPane().lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
                
                // Set the result converter
                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == saveButtonType) {
                        Map<Integer, Integer> result = new HashMap<>();
                        for (ExpertiseRequirement req : expertiseRequirements) {
                            if (req.getCount() > 0) {
                                result.put(req.getExpertiseId(), req.getCount());
                            }
                        }
                        return result;
                    }
                    return null;
                });
                
                // Show the dialog and process the result
                Optional<Map<Integer, Integer>> result = dialog.showAndWait();
                result.ifPresent(requirements -> {
                    try {
                        // Set the expertise requirements
                        selectedBranch.setExpertiseRequirements(requirements);
                        selectedBranch.saveExpertiseRequirements();
                        
                        // Calculate the total openings
                        int totalOpenings = 0;
                        for (Integer count : requirements.values()) {
                            totalOpenings += count;
                        }
                        
                        // Update the branch's team openings count
                        selectedBranch.setTeamOpenings(totalOpenings);
                        
                        // Refresh the branches list
                        refreshBranchesList(branchesListView);
                        
                        // Update the selection to refresh details
                        branchesListView.getSelectionModel().clearAndSelect(
                                branchesListView.getItems().indexOf(selectedBranch));
                        
                    } catch (SQLException ex) {
                        DialogFactory.showError("Error setting team openings: " + ex.getMessage());
                    }
                });
            });
            
            // 3. Apply to Join Team button
            Button applyToJoinButton = UIStyleManager.createMenuButton("Apply to Join Team", _ -> {
                Branch selectedBranch = branchesListView.getSelectionModel().getSelectedItem();
                if (selectedBranch == null) {
                    DialogFactory.showInfo("Selection Required", "Please select a branch first.");
                    return;
                }
                
                User currentUser = SessionManager.getCurrentUser();
                
                // Check if branch has openings
                if (selectedBranch.getTeamOpenings() <= 0) {
                    DialogFactory.showInfo("No Openings", "This branch does not have any team openings.");
                    return;
                }
                
                // Check if user is already in team or candidates
                if (selectedBranch.getTeam() != null && selectedBranch.getTeam().contains(currentUser.getUsername())) {
                    DialogFactory.showInfo("Already in Team", "You are already a member of this branch's team.");
                    return;
                }
                
                if (selectedBranch.getCandidates() != null && selectedBranch.getCandidates().contains(currentUser.getUsername())) {
                    DialogFactory.showInfo("Already Applied", "You have already applied to join this branch's team.");
                    return;
                }
                
                // Apply to join
                try {
                    selectedBranch.addCandidate(currentUser.getUsername());
                    DialogFactory.showInfo("Application Submitted", "You have applied to join the team for " + selectedBranch.getName());
                    
                    // Refresh the branches list
                    refreshBranchesList(branchesListView);
                    
                    // Update the selection to refresh details
                    branchesListView.getSelectionModel().clearAndSelect(
                            branchesListView.getItems().indexOf(selectedBranch));
                    
                } catch (SQLException ex) {
                    DialogFactory.showError("Error applying to join team: " + ex.getMessage());
                }
            });
            
            // 4. Select Team Members button
            Button selectTeamButton = UIStyleManager.createMenuButton("Select Team Members", _ -> {
                Branch selectedBranch = branchesListView.getSelectionModel().getSelectedItem();
                if (selectedBranch == null) {
                    DialogFactory.showInfo("Selection Required", "Please select a branch first.");
                    return;
                }
                
                // Check if there are any candidates
                if (selectedBranch.getCandidates() == null || selectedBranch.getCandidates().isEmpty()) {
                    DialogFactory.showInfo("No Candidates", "There are no candidates to select from.");
                    return;
                }
                
                // Confirm selection process
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Select Team Members");
                confirmDialog.setHeaderText("Select Team Members for " + selectedBranch.getName());
                confirmDialog.setContentText("This will randomly select members from the candidates pool based on " +
                                             "the number of openings. Proceed?");
                
                // Apply styling
                DialogPane dialogPane = confirmDialog.getDialogPane();
                dialogPane.setStyle("-fx-background-color: #3e3e3e;");
                dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #3e3e3e;");
                dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
                dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
                
                Optional<ButtonType> result = confirmDialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    try {
                        // Run the candidate selection process
                        selectedBranch.candidateSelector();
                        
                        DialogFactory.showInfo("Team Selection Complete", 
                                "Team members have been selected from the candidates pool.");
                        
                        // Refresh the branches list
                        refreshBranchesList(branchesListView);
                        
                        // Update the selection to refresh details
                        branchesListView.getSelectionModel().clearAndSelect(
                                branchesListView.getItems().indexOf(selectedBranch));
                        
                    } catch (SQLException ex) {
                        DialogFactory.showError("Error selecting team members: " + ex.getMessage());
                    }
                }
            });
            
            // 5. Advance Phase button
            Button advancePhaseButton = UIStyleManager.createMenuButton("Advance Phase", _ -> {
                Branch selectedBranch = branchesListView.getSelectionModel().getSelectedItem();
                if (selectedBranch == null) {
                    DialogFactory.showInfo("Selection Required", "Please select a branch first.");
                    return;
                }
                
                // Confirm phase advancement
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Advance Phase");
                confirmDialog.setHeaderText("Advance Phase for " + selectedBranch.getName());
                
                String currentPhase = selectedBranch.getCurrentPhase().toString();
                String nextPhase = getNextPhase(selectedBranch.getCurrentPhase());
                
                confirmDialog.setContentText("Are you sure you want to advance from " + 
                                             currentPhase + " to " + nextPhase + "?");
                
                // Apply styling
                DialogPane dialogPane = confirmDialog.getDialogPane();
                dialogPane.setStyle("-fx-background-color: #3e3e3e;");
                dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #3e3e3e;");
                dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
                dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
                
                Optional<ButtonType> result = confirmDialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    try {
                        selectedBranch.advancePhase();
                        
                        DialogFactory.showInfo("Phase Advanced", 
                                "Branch has been advanced to the " + selectedBranch.getCurrentPhase() + " phase.");
                        
                        // Refresh the branches list
                        refreshBranchesList(branchesListView);
                        
                        // Update the selection to refresh details
                        branchesListView.getSelectionModel().clearAndSelect(
                                branchesListView.getItems().indexOf(selectedBranch));
                        
                    } catch (SQLException ex) {
                        DialogFactory.showError("Error advancing phase: " + ex.getMessage());
                    }
                }
            });
            
            // 6. Back to Dashboard button
            Button backButton = UIStyleManager.createMenuButton("Back to Dashboard", _ -> showDashboard());
            
            // Add buttons to the actions panel
            actionsPanel.getChildren().addAll(
                createBranchButton, 
                setOpeningsButton, 
                applyToJoinButton, 
                selectTeamButton, 
                advancePhaseButton, 
                backButton
            );
            
            // Add all components to the layout
            contentPane.setCenter(branchesListView);
            contentPane.setBottom(detailsPanel);
            
            branchesLayout.setCenter(contentPane);
            branchesLayout.setRight(actionsPanel);
            
            // Set the branches content in the main window
            setMainContent(branchesLayout, "Branches Management");
            
        } catch (Exception e) {
            e.printStackTrace();
            DialogFactory.showError("Error displaying branches view: " + e.getMessage());
        }
    }
    
    // Helper method to refresh the branches list
    private static void refreshBranchesList(ListView<Branch> branchesListView) {
        Branch selectedBranch = branchesListView.getSelectionModel().getSelectedItem();
        
        branchesListView.getItems().clear();
        
        // Reload branches from database to ensure we have fresh data
        try {
            branches = DatabaseManager.loadAllBranches();
            
            // Debug: Print branch names to console
            System.out.println("Loaded branches:");
            for (Branch branch : branches.values()) {
                System.out.println("  Branch ID: " + branch.getId() + ", Name: " + branch.getName());
            }
        } catch (SQLException e) {
            DialogFactory.showError("Error refreshing branches: " + e.getMessage());
        }
        
        // Populate list with branches
        for (Branch branch : branches.values()) {
            branchesListView.getItems().add(branch);
        }
        
        if (selectedBranch != null) {
            // Try to find the previously selected branch by ID
            for (Branch branch : branchesListView.getItems()) {
                if (branch.getId() == selectedBranch.getId()) {
                    branchesListView.getSelectionModel().select(branch);
                    break;
                }
            }
        }
    }
    
    // Helper method to get the next phase name
    private static String getNextPhase(Branch.Phase currentPhase) {
        Branch.Phase[] phases = Branch.Phase.values();
        for (int i = 0; i < phases.length - 1; i++) {
            if (phases[i] == currentPhase) {
                return phases[i + 1].toString();
            }
        }
        return "COMPLETED"; // Default to completed if we can't find the next phase
    }
    
    private static void handleShowProposals() {
        try {
            // Create a BorderPane for proposals management
            BorderPane proposalsLayout = new BorderPane();
            proposalsLayout.setStyle("-fx-background-color: #2e2e2e;");

            // Create header
            Label headerLabel = new Label("Proposals Management");
            headerLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-size: 24px; -fx-padding: 20px;");
            proposalsLayout.setTop(headerLabel);

            // Create TabPane to separate different proposal types
            javafx.scene.control.TabPane tabPane = new javafx.scene.control.TabPane();
            tabPane.setTabClosingPolicy(javafx.scene.control.TabPane.TabClosingPolicy.UNAVAILABLE);
            tabPane.setStyle("-fx-background-color: #2e2e2e;");

            // Create tabs for each proposal type using the specific maps and service methods
            javafx.scene.control.Tab levelTab = createProposalTab("Level", "LEVEL", levelProposals,
                () -> createLevelProposalDialog(),
                (proposal) -> "XP Inc: " + ((LevelProposal)proposal).getXpIncreasePercentage() + "%, XP Thresh: " + ((LevelProposal)proposal).getXpThreshold());

            javafx.scene.control.Tab berryEarningTab = createProposalTab("Berry Earning", "BERRY_EARNING", berryEarningProposals,
                () -> createBerryEarningProposalDialog(),
                (proposal) -> "Initial Lvl 1 Earning: " + ((BerryEarningProposal)proposal).getInitialLevelOneBerryEarning());

            javafx.scene.control.Tab berryValidityTab = createProposalTab("Berry Validity", "BERRY_VALIDITY", berryValidityProposals,
                () -> createBerryValidityProposalDialog(),
                (proposal) -> "Validity Months: " + ((BerryValidityProposal)proposal).getValidityMonths());

            javafx.scene.control.Tab berryConversionTab = createProposalTab("Berry Conversion", "BERRY_CONVERSION", berryConversionProposals,
                () -> createBerryConversionProposalDialog(),
                (proposal) -> "Conv %: " + ((BerryConversionProposal)proposal).getConversionPercentage() + ", Period: " + ((BerryConversionProposal)proposal).getConversionPeriod() + " months");
                
            javafx.scene.control.Tab needThresholdTab = createProposalTab("Need Threshold", "NEED_THRESHOLD", needThresholdProposals,
                () -> createNeedThresholdProposalDialog(),
                (proposal) -> "Global: " + ((NeedThresholdProposal)proposal).getGlobalThresholdPercent() + "%, Personal: " + 
                              ((NeedThresholdProposal)proposal).getPersonalThresholdPercent() + "%, Time: " + 
                              ((NeedThresholdProposal)proposal).getTimeLimit() + " months");

            // Add tabs to TabPane
            tabPane.getTabs().addAll(levelTab, berryEarningTab, berryValidityTab, berryConversionTab, needThresholdTab);

            // Create bottom panel with back button
            VBox bottomBox = new VBox(15);
            bottomBox.setPadding(new Insets(15));
            bottomBox.setAlignment(Pos.CENTER);

            Button backButton = UIStyleManager.createMenuButton("Back to Dashboard", _ -> showDashboard());
            bottomBox.getChildren().add(backButton);

            // Set TabPane in the center
            proposalsLayout.setCenter(tabPane);
            proposalsLayout.setBottom(bottomBox);

            // Set the proposals content in the main window
            setMainContent(proposalsLayout, "Proposals Management");

        } catch (Exception e) {
            e.printStackTrace();
            DialogFactory.showError("Error displaying proposals view: " + e.getMessage());
        }
    }

    // Generic method to create a proposal tab
    private static <T extends Proposal> javafx.scene.control.Tab createProposalTab(
            String tabTitle, String proposalType, Map<Integer, T> proposalMap,
            Runnable createDialogAction, java.util.function.Function<T, String> detailsFormatter) {

        javafx.scene.control.Tab tab = new javafx.scene.control.Tab(tabTitle);
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: #2e2e2e;");

        ListView<T> proposalListView = new ListView<>();
        proposalListView.setStyle("-fx-background-color: #3e3e3e; -fx-control-inner-background: #3e3e3e;");
        proposalListView.setPrefHeight(300);

        // Populate proposals
        proposalListView.getItems().addAll(proposalMap.values());

        // Custom cell factory
        proposalListView.setCellFactory(_ -> new ListCell<T>() {
            @Override
            protected void updateItem(T proposal, boolean empty) {
                super.updateItem(proposal, empty);
                if (empty || proposal == null) {
                    setText(null);
                } else {
                    String details = detailsFormatter.apply(proposal);
                    setText("ID: " + proposal.getId() + " | " + details + " | Votes: " + proposal.getVotes() + " | By: " + proposal.getProposer());
                    setStyle("-fx-text-fill: #d9d9d9;");
                }
            }
        });

        // Buttons container
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button createButton = UIStyleManager.createMenuButton("Create Proposal", _ -> {
            createDialogAction.run();
            // Refresh list after potential creation
            proposalListView.getItems().clear();
            proposalListView.getItems().addAll(proposalMap.values());
        });

        Button voteButton = UIStyleManager.createMenuButton("Vote for Proposal", _ -> {
            T selected = proposalListView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                DialogFactory.showInfo("Selection Required", "Please select a proposal to vote for.");
                return;
            }
            User currentUser = SessionManager.getCurrentUser();
            handleVoteForProposal(proposalType, selected.getId(), currentUser);
            // Refresh list after voting
            proposalListView.getItems().clear();
            proposalListView.getItems().addAll(proposalMap.values());
            // Reselect if possible
            proposalListView.getSelectionModel().select(selected);
        });

        buttonBox.getChildren().addAll(createButton, voteButton);
        content.getChildren().addAll(proposalListView, buttonBox);
        tab.setContent(content);
        return tab;
    }

    // --- Dialog Creation Methods for Proposals ---

    private static void createLevelProposalDialog() {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Create Level Proposal");
        dialog.setHeaderText("Enter proposal details");
        
        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField xpIncreaseField = new TextField();
        xpIncreaseField.setPromptText("XP Increase (%)");
        xpIncreaseField.setStyle(UIStyleManager.TEXT_FIELD_STYLE_DARK);
        
        TextField xpThresholdField = new TextField();
        xpThresholdField.setPromptText("XP Threshold");
        xpThresholdField.setStyle(UIStyleManager.TEXT_FIELD_STYLE_DARK);

        // Style labels
        Label increaseLabel = new Label("XP Increase (%):");
        increaseLabel.setStyle(UIStyleManager.LABEL_STYLE);
        
        Label thresholdLabel = new Label("XP Threshold:");
        thresholdLabel.setStyle(UIStyleManager.LABEL_STYLE);

        grid.add(increaseLabel, 0, 0);
        grid.add(xpIncreaseField, 1, 0);
        grid.add(thresholdLabel, 0, 1);
        grid.add(xpThresholdField, 1, 1);

        // Apply UIStyleManager enhancement instead of manual styling
        dialog.getDialogPane().setContent(grid);
        UIStyleManager.enhanceDialogWithKeyboardNavigation(dialog);

        Platform.runLater(() -> xpIncreaseField.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                try {
                    Map<String, Object> result = new HashMap<>();
                    result.put("xpIncrease", Double.parseDouble(xpIncreaseField.getText().trim()));
                    result.put("xpThreshold", Double.parseDouble(xpThresholdField.getText().trim()));
                    return result;
                } catch (NumberFormatException ex) {
                    DialogFactory.showError("Invalid number format.");
                    return null;
                }
            }
            return null;
        });

        Optional<Map<String, Object>> result = dialog.showAndWait();
        result.ifPresent(data -> {
            double xpIncrease = (double) data.get("xpIncrease");
            double xpThreshold = (double) data.get("xpThreshold");
            String proposer = SessionManager.getCurrentUser().getUsername();
            handleCreateLevelProposal(proposer, xpIncrease, xpThreshold);
        });
    }

    private static void createBerryEarningProposalDialog() {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Create Berry Earning Proposal");
        dialog.setHeaderText("Enter proposal details");

        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField initialEarningField = new TextField();
        initialEarningField.setPromptText("Initial Level 1 Earning");
        initialEarningField.setStyle(UIStyleManager.TEXT_FIELD_STYLE_DARK);

        Label earningLabel = new Label("Initial Lvl 1 Earning:");
        earningLabel.setStyle(UIStyleManager.LABEL_STYLE);

        grid.add(earningLabel, 0, 0);
        grid.add(initialEarningField, 1, 0);

        // Apply UIStyleManager enhancement instead of manual styling
        dialog.getDialogPane().setContent(grid);
        UIStyleManager.enhanceDialogWithKeyboardNavigation(dialog);

        Platform.runLater(() -> initialEarningField.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                try {
                    Map<String, Object> result = new HashMap<>();
                    result.put("initialEarning", Integer.parseInt(initialEarningField.getText().trim()));
                    return result;
                } catch (NumberFormatException ex) {
                    DialogFactory.showError("Invalid number format.");
                    return null;
                }
            }
            return null;
        });

        Optional<Map<String, Object>> result = dialog.showAndWait();
        result.ifPresent(data -> {
            int initialEarning = (int) data.get("initialEarning");
            String proposer = SessionManager.getCurrentUser().getUsername();
            try {
                ProposalService.createBerryEarningProposal(proposer, initialEarning);
            } catch (SQLException e) {
                DialogFactory.showError("Error creating proposal: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static void createBerryValidityProposalDialog() {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Create Berry Validity Proposal");
        dialog.setHeaderText("Enter proposal details");

        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField monthsField = new TextField();
        monthsField.setPromptText("Validity (Months)");
        monthsField.setStyle(UIStyleManager.TEXT_FIELD_STYLE_DARK);

        Label monthsLabel = new Label("Validity (Months):");
        monthsLabel.setStyle(UIStyleManager.LABEL_STYLE);

        grid.add(monthsLabel, 0, 0);
        grid.add(monthsField, 1, 0);

        // Apply UIStyleManager enhancement instead of manual styling
        dialog.getDialogPane().setContent(grid);
        UIStyleManager.enhanceDialogWithKeyboardNavigation(dialog);

        Platform.runLater(() -> monthsField.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                try {
                    Map<String, Object> result = new HashMap<>();
                    result.put("months", Integer.parseInt(monthsField.getText().trim()));
                    return result;
                } catch (NumberFormatException ex) {
                    DialogFactory.showError("Invalid number format.");
                    return null;
                }
            }
            return null;
        });

        Optional<Map<String, Object>> result = dialog.showAndWait();
        result.ifPresent(data -> {
            int months = (int) data.get("months");
            String proposer = SessionManager.getCurrentUser().getUsername();
            try {
                ProposalService.createBerryValidityProposal(proposer, months);
            } catch (SQLException e) {
                DialogFactory.showError("Error creating proposal: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static void createBerryConversionProposalDialog() {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Create Berry Conversion Proposal");
        dialog.setHeaderText("Enter proposal details");

        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField percentageField = new TextField();
        percentageField.setPromptText("Conversion Percentage (%)");
        percentageField.setStyle(UIStyleManager.TEXT_FIELD_STYLE_DARK);
        
        TextField periodField = new TextField();
        periodField.setPromptText("Conversion Period (Months)");
        periodField.setStyle(UIStyleManager.TEXT_FIELD_STYLE_DARK);

        Label percentageLabel = new Label("Conversion (%):");
        percentageLabel.setStyle(UIStyleManager.LABEL_STYLE);
        
        Label periodLabel = new Label("Period (Months):");
        periodLabel.setStyle(UIStyleManager.LABEL_STYLE);

        grid.add(percentageLabel, 0, 0);
        grid.add(percentageField, 1, 0);
        grid.add(periodLabel, 0, 1);
        grid.add(periodField, 1, 1);

        // Apply UIStyleManager enhancement instead of manual styling
        dialog.getDialogPane().setContent(grid);
        UIStyleManager.enhanceDialogWithKeyboardNavigation(dialog);

        Platform.runLater(() -> percentageField.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                try {
                    Map<String, Object> result = new HashMap<>();
                    result.put("percentage", Double.parseDouble(percentageField.getText().trim()));
                    result.put("period", Integer.parseInt(periodField.getText().trim()));
                    return result;
                } catch (NumberFormatException ex) {
                    DialogFactory.showError("Invalid number format.");
                    return null;
                }
            }
            return null;
        });

        Optional<Map<String, Object>> result = dialog.showAndWait();
        result.ifPresent(data -> {
            double percentage = (double) data.get("percentage");
            int period = (int) data.get("period");
            String proposer = SessionManager.getCurrentUser().getUsername();
            try {
                ProposalService.createBerryConversionProposal(proposer, percentage, period);
            } catch (SQLException e) {
                DialogFactory.showError("Error creating proposal: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static void createNeedThresholdProposalDialog() {
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Create Need Threshold Proposal");
        dialog.setHeaderText("Enter Need Threshold proposal details");

        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Create form fields
        TextField globalThresholdField = new TextField();
        globalThresholdField.setPromptText("Global Threshold (%)");
        globalThresholdField.setStyle(UIStyleManager.TEXT_FIELD_STYLE_DARK);
        
        TextField personalThresholdField = new TextField();
        personalThresholdField.setPromptText("Personal Threshold (%)");
        personalThresholdField.setStyle(UIStyleManager.TEXT_FIELD_STYLE_DARK);
        
        TextField timeLimitField = new TextField();
        timeLimitField.setPromptText("Time Limit (months)");
        timeLimitField.setStyle(UIStyleManager.TEXT_FIELD_STYLE_DARK);
        
        // Branch selection removed as Need Threshold Proposals now apply to all branches

        // Style labels
        Label globalThresholdLabel = new Label("Global Threshold (%):");
        globalThresholdLabel.setStyle(UIStyleManager.LABEL_STYLE);
        
        Label personalThresholdLabel = new Label("Personal Threshold (%):");
        personalThresholdLabel.setStyle(UIStyleManager.LABEL_STYLE);
        
        Label timeLimitLabel = new Label("Time Limit (months):");
        timeLimitLabel.setStyle(UIStyleManager.LABEL_STYLE);

        // Add fields to grid
        grid.add(globalThresholdLabel, 0, 0);
        grid.add(globalThresholdField, 1, 0);
        grid.add(personalThresholdLabel, 0, 1);
        grid.add(personalThresholdField, 1, 1);
        grid.add(timeLimitLabel, 0, 2);
        grid.add(timeLimitField, 1, 2);

        // Apply UIStyleManager enhancement for consistent dialog styling
        dialog.getDialogPane().setContent(grid);
        UIStyleManager.enhanceDialogWithKeyboardNavigation(dialog);

        Platform.runLater(() -> globalThresholdField.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                try {
                    Map<String, Object> result = new HashMap<>();
                    
                    // Validate and parse inputs
                    double globalThreshold = Double.parseDouble(globalThresholdField.getText().trim());
                    double personalThreshold = Double.parseDouble(personalThresholdField.getText().trim());
                    int timeLimit = Integer.parseInt(timeLimitField.getText().trim());
                    
                    if (globalThreshold < 0 || globalThreshold > 100 || personalThreshold < 0 || personalThreshold > 100) {
                        DialogFactory.showError("Threshold percentages must be between 0 and 100");
                        return null;
                    }
                    
                    if (timeLimit <= 0) {
                        DialogFactory.showError("Time limit must be a positive number of months");
                        return null;
                    }
                    
                    // Add validated values to result
                    result.put("globalThreshold", globalThreshold);
                    result.put("personalThreshold", personalThreshold);
                    result.put("timeLimit", timeLimit);
                    
                    return result;
                } catch (NumberFormatException ex) {
                    DialogFactory.showError("Please enter valid numbers for all fields");
                }
            }
            return null;
        });

        Optional<Map<String, Object>> result = dialog.showAndWait();
        result.ifPresent(data -> {
            double globalThreshold = (double) data.get("globalThreshold");
            double personalThreshold = (double) data.get("personalThreshold");
            int timeLimit = (int) data.get("timeLimit");
            
            String proposer = SessionManager.getCurrentUser().getUsername();
            
            try {
                ProposalService.createNeedThresholdProposal(proposer, globalThreshold, personalThreshold, timeLimit);
            } catch (SQLException e) {
                DialogFactory.showError("Error creating Need Threshold proposal: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static void handleShowBerries() {
    try {
        // Create a BorderPane for berries management
        BorderPane berriesLayout = new BorderPane();
        berriesLayout.setStyle("-fx-background-color: #2e2e2e;");
        
        // Create header
        Label headerLabel = new Label("Berries Management");
        headerLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-size: 24px; -fx-padding: 20px;");
        berriesLayout.setTop(headerLabel);
        
        // Create main content area
        VBox contentPane = new VBox(15);
        contentPane.setPadding(new Insets(20));
        contentPane.setAlignment(Pos.CENTER);
        
        // Get current user's information
        User currentUser = SessionManager.getCurrentUser();
        String username = currentUser.getUsername();
        
        // Display user's berry information
        VBox berryInfoBox = new VBox(10);
        berryInfoBox.setAlignment(Pos.CENTER);
        berryInfoBox.setPadding(new Insets(20));
        berryInfoBox.setMaxWidth(500);
        berryInfoBox.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 5px;");
        
        // Get user's total berries
        int totalBerries = BerryService.getUserTotalBerries(username);
        
        // Add information labels
        Label totalBerriesLabel = new Label("Your Total Berries: " + totalBerries);
        totalBerriesLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-size: 18px;");
        
        Label totalEarnedLabel = new Label("Total Berries Earned: " + currentUser.getTotalBerriesEarned());
        totalEarnedLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-size: 16px;");
        
        Label monthlyRateLabel = new Label("Monthly Earning Rate: " + 
                SystemParameters.calculateMonthlyBerryEarning(currentUser.getLevel()) + " berries");
        monthlyRateLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-size: 16px;");
        
        Label validityLabel = new Label("Berry Validity Period: " + 
                SystemParameters.getBerryValidityTime() + " months");
        validityLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-size: 16px;");
        
        berryInfoBox.getChildren().addAll(
            totalBerriesLabel, 
            totalEarnedLabel, 
            monthlyRateLabel,
            validityLabel
        );
        
        // Display list of user's berries if available
        ListView<Berry> berriesListView = new ListView<>();
        berriesListView.setMaxHeight(300);
        berriesListView.setStyle("-fx-background-color: #3e3e3e; -fx-control-inner-background: #3e3e3e;");
        
        // Populate berry list
        List<Berry> userBerryList = userBerries.getOrDefault(username, new ArrayList<>());
        if (userBerryList != null && !userBerryList.isEmpty()) {
            for (Berry berry : userBerryList) {
                berriesListView.getItems().add(berry);
            }
            
            // Custom cell factory for displaying berries
            berriesListView.setCellFactory(_ -> new ListCell<Berry>() {
                @Override
                protected void updateItem(Berry berry, boolean empty) {
                    super.updateItem(berry, empty);
                    if (empty || berry == null) {
                        setText(null);
                    } else {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        setText(berry.getAmount() + " berries - Source: " + berry.getSource() + 
                              " (Expires: " + berry.getExpirationDate().format(formatter) + ")");
                        setStyle("-fx-text-fill: #d9d9d9;");
                    }
                }
            });
            
            Label berriesListLabel = new Label("Your Active Berries:");
            berriesListLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-size: 16px; -fx-padding: 10 0 5 0;");
            
            contentPane.getChildren().addAll(berryInfoBox, berriesListLabel, berriesListView);
        } else {
            Label noBerriesLabel = new Label("You don't have any berries at the moment.");
            noBerriesLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 16px; -fx-padding: 20 0 0 0;");
            
            contentPane.getChildren().addAll(berryInfoBox, noBerriesLabel);
        }
        
        // Create action buttons panel
        VBox actionsPanel = new VBox(10);
        actionsPanel.setPadding(new Insets(20));
        actionsPanel.setStyle("-fx-background-color: #1e1e1e;");
        actionsPanel.setPrefWidth(200);
        actionsPanel.setAlignment(Pos.TOP_CENTER);
        
        // Add admin section header if user has appropriate permissions
        Label adminSectionLabel = new Label("Admin Functions");
        adminSectionLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Button for processing monthly berry distribution
        Button distributeBerryButton = UIStyleManager.createMenuButton("Process Monthly Distribution", _ -> {
            // Confirm process with alert dialog
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Process Monthly Distribution");
            confirmDialog.setHeaderText("Process Monthly Berry Distribution");
            confirmDialog.setContentText("Are you sure you want to process the monthly berry distribution for all users?");
            
            // Apply styling
            DialogPane dialogPane = confirmDialog.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #3e3e3e;");
            dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #3e3e3e;");
            dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
            dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
            
            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    // Process the monthly berry distribution
                    processMonthlyBerryDistribution();
                    
                    // Refresh the view to show updated berries
                    handleShowBerries();
                } catch (SQLException ex) {
                    DialogFactory.showError("Error processing monthly distribution: " + ex.getMessage());
                }
            }
        });
        
        // Back to Dashboard button
        Button backButton = UIStyleManager.createMenuButton("Back to Dashboard", _ -> showDashboard());
        
        // Add buttons to actions panel
        actionsPanel.getChildren().addAll(adminSectionLabel, distributeBerryButton, backButton);
        
        // Set the content in the berries layout
        berriesLayout.setCenter(contentPane);
        berriesLayout.setRight(actionsPanel);
        
        // Set the berries content in the main window
        setMainContent(berriesLayout, "Berries Management");
    } catch (Exception e) {
        e.printStackTrace();
        DialogFactory.showError("Error displaying berries view: " + e.getMessage());
    }
}

    private static void handleShowProfile() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            DialogFactory.showError("No user logged in.");
            return;
        }

        VBox profileLayout = new VBox(15);
        profileLayout.setPadding(new Insets(20));
        profileLayout.setStyle("-fx-background-color: #3e3e3e;");

        Label titleLabel = new Label("My Profile");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");
        profileLayout.getChildren().add(titleLabel);

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(10);
        detailsGrid.setVgap(10);
        detailsGrid.setPadding(new Insets(10));

        // Define styles for labels and values
        String labelStyle = "-fx-text-fill: #b2b2b2; -fx-font-size: 14px;";
        String valueStyle = "-fx-text-fill: #d9d9d9; -fx-font-size: 14px;"; // Basic value style

        int rowIndex = 0;
        // Row: Username
        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle(labelStyle);
        Label usernameValue = new Label(currentUser.getUsername());
        usernameValue.setStyle(valueStyle);
        detailsGrid.add(usernameLabel, 0, rowIndex);
        detailsGrid.add(usernameValue, 1, rowIndex++);

        // Row: Display Name
        Label displayNameLabel = new Label("Display Name:");
        displayNameLabel.setStyle(labelStyle);
        TextField displayNameField = new TextField(currentUser.getDisplayName());
        displayNameField.setStyle("-fx-control-inner-background: #555; -fx-text-fill: white; -fx-border-color: #777;"); // Style the text field
        detailsGrid.add(displayNameLabel, 0, rowIndex);
        detailsGrid.add(displayNameField, 1, rowIndex++);

        // Row: Level
        Label levelLabel = new Label("Level:");
        levelLabel.setStyle(labelStyle);
        Label levelValue = new Label(String.valueOf(currentUser.getLevel()));
        levelValue.setStyle(valueStyle);
        detailsGrid.add(levelLabel, 0, rowIndex);
        detailsGrid.add(levelValue, 1, rowIndex++);

        // Row: XP (with progress bar)
        Label xpLabel = new Label("XP:");
        xpLabel.setStyle(labelStyle);
        int currentXP = currentUser.getXp();
        int xpThreshold = SystemParameters.calculateXpThreshold(currentUser.getLevel());
        double xpProgress = (xpThreshold > 0) ? (double)currentXP / xpThreshold : 0.0;
        ProgressBar xpProgressBar = new ProgressBar(xpProgress);
        xpProgressBar.setMaxWidth(Double.MAX_VALUE);
        xpProgressBar.setStyle("-fx-accent: #76ff76;"); // Green progress
        Label xpText = new Label(currentXP + " / " + xpThreshold);
        xpText.setStyle(valueStyle);
        HBox xpBox = new HBox(10, xpProgressBar, xpText);
        xpBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(xpProgressBar, Priority.ALWAYS);
        detailsGrid.add(xpLabel, 0, rowIndex);
        detailsGrid.add(xpBox, 1, rowIndex++); // Add HBox instead of simple Label

        // Row: Points
        Label pointsLabel = new Label("Points:");
        pointsLabel.setStyle(labelStyle);
        Label pointsValue = new Label(String.valueOf(currentUser.getPoints()));
        pointsValue.setStyle(valueStyle);
        detailsGrid.add(pointsLabel, 0, rowIndex);
        detailsGrid.add(pointsValue, 1, rowIndex++);

        // Row: Current Berries
        Label berriesLabel = new Label("Current Berries:"); // Renamed label
        berriesLabel.setStyle(labelStyle);
        // Call BerryService to get the current total
        int currentBerries = BerryService.getUserTotalBerries(currentUser.getUsername());
        Label berriesValue = new Label(String.valueOf(currentBerries)); 
        berriesValue.setStyle(valueStyle);
        detailsGrid.add(berriesLabel, 0, rowIndex);
        detailsGrid.add(berriesValue, 1, rowIndex++);

        // --- Add Certified Expertise Section ---
        Label expertiseTitleLabel = new Label("Certified Expertise:");
        expertiseTitleLabel.setStyle(labelStyle); // Use the standard label style
        detailsGrid.add(expertiseTitleLabel, 0, rowIndex);

        ListView<String> expertiseListView = new ListView<>();
        expertiseListView.setPrefHeight(100); // Adjust height as needed
        expertiseListView.setStyle("-fx-control-inner-background: #555; -fx-text-fill: white; -fx-border-color: #777;");
        Set<Integer> certifiedIds = currentUser.getCertifiedExpertiseIds();
        List<String> expertiseNames = new ArrayList<>();
        if (certifiedIds != null && !certifiedIds.isEmpty()) {
            for (int id : certifiedIds) {
                FieldOfExpertise foe = TrustSystem.fieldsOfExpertise.get(id);
                if (foe != null) {
                    expertiseNames.add(foe.getName());
                } else {
                    expertiseNames.add("Unknown Expertise ID: " + id);
                }
            }
            Collections.sort(expertiseNames);
        } 

        if (expertiseNames.isEmpty()) {
            expertiseListView.getItems().add("None");
            expertiseListView.setMouseTransparent(true); // Make it non-interactive if empty
            expertiseListView.setFocusTraversable(false);
        } else {
            expertiseListView.setItems(FXCollections.observableArrayList(expertiseNames));
        }
        detailsGrid.add(expertiseListView, 1, rowIndex++); // Add to grid, increment row index
        // --- End Certified Expertise Section ---

        profileLayout.getChildren().add(detailsGrid);

        Button saveButton = UIStyleManager.createMenuButton("Save Changes", _ -> {
            currentUser.setDisplayName(displayNameField.getText());
            try {
                DatabaseManager.updateUser(currentUser);
                DialogFactory.showInfo("Success", "Profile updated.");
                // Update welcome label if needed
                if (mainLayout != null && mainLayout.getTop() instanceof Label) {
                   ((Label)mainLayout.getTop()).setText("Welcome, " + currentUser.getDisplayName() + "!");
                }
            } catch (SQLException ex) {
                DialogFactory.showError("Database error updating profile: " + ex.getMessage());
            }
        });
        profileLayout.getChildren().add(saveButton);

        setMainContent(profileLayout, "My Profile");
    }
    
    /**
     * Authenticates a user and sets up their session
     */
    public static User authenticateAndSetupSession(String username, String password) {
        User user = SessionManager.authenticateUser(username, password);
        if (user != null) {
            SessionManager.setCurrentUser(user, username, password);
        }
        return user;
    }
    
    /**
     * Checks if an idea belongs to the given user
     */
    public static boolean isIdeaAuthor(int ideaId, String username) {
        if (!ideas.containsKey(ideaId)) {
            return false;
        }
        return ideas.get(ideaId).getAuthor().equals(username);
    }
    
    /**
     * Handles user gaining XP and checks for level-up
     */
    public static void processUserXpGain(User user, int xpAmount) throws SQLException {
        if (user == null) return;
        
        user.addXp(xpAmount);
        DatabaseManager.updateUser(user);
        
        // Check if user leveled up
        checkForLevelUp(user);
    }
    
    public static void startConsoleSystem() {
        try {
            // Initialize database and load data
            DatabaseConnection.initializeDataSource();
            DatabaseManager.loadAllData();
            BerryService.checkAndRemoveExpiredBerries();
            
            // Rest of console system implementation would go here
            // (Scanner setup, menu loop, etc.)
            
        } catch (SQLException e) {
            System.out.println("Failed to load data from database: " + e.getMessage());
            System.exit(1);
        }
    }
    
    // Process monthly berry distribution
    public static void processMonthlyBerryDistribution() throws SQLException {
        // First ensure we have the latest proposals
        loadProposalsFromDatabase();
        
        // Update system parameters from active proposals
        updateSystemParametersFromActiveProposals();
        
        // Process berry distribution for all users
        for (User user : users.values()) {
            // Calculate berry distribution based on user level
            int level = user.getLevel();
            BerryService.distributeBerriesForLevel(user, level);
        }
        
        DialogFactory.showInfo("Monthly Distribution Complete", 
            "Monthly Berry distribution completed for all users.");
    }

    // --- NEW: Trace Tab Handler --- 
    private static void handleShowTrace() {
        try {
            BorderPane traceLayout = new BorderPane();
            traceLayout.setPadding(new Insets(15));
            traceLayout.setStyle("-fx-background-color: #2e2e2e;");

            Label headerLabel = new Label("Trace - Fields of Expertise Management");
            headerLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-size: 24px; -fx-padding: 0 0 15 0;");
            traceLayout.setTop(headerLabel);

            // --- Main Content Split Pane ---
            SplitPane mainSplit = new SplitPane();
            mainSplit.setStyle("-fx-background-color: #2e2e2e;");

            // --- Left Side: Fields of Expertise List & Actions ---
            VBox foePanel = new VBox(10);
            foePanel.setPadding(new Insets(10));
            foePanel.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 5;");

            Label foeListLabel = new Label("Fields of Expertise:");
            foeListLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-weight: bold;");

            ListView<FieldOfExpertise> foeListView = new ListView<>();
            foeListView.setStyle("-fx-control-inner-background: #4e4e4e; -fx-text-fill: #d9d9d9;");
            foeListView.setPrefHeight(300);
            ObservableList<FieldOfExpertise> foeItems = FXCollections.observableArrayList(TrustSystem.fieldsOfExpertise.values());
            FXCollections.sort(foeItems, Comparator.comparing(FieldOfExpertise::getName)); // Sort alphabetically
            foeListView.setItems(foeItems);

            // Details Area (simple for now)
            TextArea foeDescriptionArea = new TextArea();
            foeDescriptionArea.setEditable(false);
            foeDescriptionArea.setWrapText(true);
            foeDescriptionArea.setPrefRowCount(3);
            foeDescriptionArea.setStyle("-fx-control-inner-background: #5e5e5e; -fx-text-fill: #d9d9d9;");

            foeListView.getSelectionModel().selectedItemProperty().addListener((_, __, newVal) -> {
                // Update description area when selection changes
                if (newVal != null) {
                    foeDescriptionArea.setText(newVal.getDescription() != null ? newVal.getDescription() : "");
                } else {
                    foeDescriptionArea.setText("");
                }
            });

            // Action Buttons for FoE
            HBox foeActionBox = new HBox(10);
            foeActionBox.setAlignment(Pos.CENTER_LEFT);
            Button createFoeButton = UIStyleManager.createMenuButton("Create", _ -> handleCreateFoE(foeListView));
            Button editFoeButton = UIStyleManager.createMenuButton("Edit", _ -> handleEditFoE(foeListView.getSelectionModel().getSelectedItem(), foeListView));
            Button deleteFoeButton = UIStyleManager.createMenuButton("Delete", _ -> handleDeleteFoE(foeListView.getSelectionModel().getSelectedItem(), foeListView));
            foeActionBox.getChildren().addAll(createFoeButton, editFoeButton, deleteFoeButton);

            foePanel.getChildren().addAll(foeListLabel, foeListView, foeDescriptionArea, foeActionBox);

            // --- Right Side: User Certification ---
            VBox assignmentPanel = new VBox(10);
            assignmentPanel.setPadding(new Insets(10));
            assignmentPanel.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 5;");

            // User Certification Selection
            HBox userSelector = new HBox(10);
            userSelector.setAlignment(Pos.CENTER_LEFT);
            Label selectUserLabel = new Label("User:");
            selectUserLabel.setStyle("-fx-text-fill: #d9d9d9;");
            ComboBox<User> userComboBox = new ComboBox<>();
            userComboBox.setPromptText("Select User");
            userComboBox.getItems().addAll(TrustSystem.users.values());
            userSelector.getChildren().addAll(selectUserLabel, userComboBox);

            // Expertise Certified for User List
            Label userExpertiseLabel = new Label("Certified Expertise for Selected User:");
            userExpertiseLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-weight: bold;");
            ListView<FieldOfExpertise> userExpertiseListView = new ListView<>();
            userExpertiseListView.setStyle("-fx-control-inner-background: #4e4e4e; -fx-text-fill: #d9d9d9;");
            userExpertiseListView.setPrefHeight(150);

            // Action Buttons for User Certification
            HBox userActionBox = new HBox(10);
            userActionBox.setAlignment(Pos.CENTER_LEFT);
            Button addUserExpertiseButton = UIStyleManager.createMenuButton("Certify", _ -> handleCertifyUserExpertise(userComboBox.getValue(), userExpertiseListView));
            Button removeUserExpertiseButton = UIStyleManager.createMenuButton("Revoke", _ -> handleRevokeUserExpertise(userComboBox.getValue(), userExpertiseListView.getSelectionModel().getSelectedItem(), userExpertiseListView));
            userActionBox.getChildren().addAll(addUserExpertiseButton, removeUserExpertiseButton);

            // Populate user expertise list when user is selected
            userComboBox.valueProperty().addListener((_, __, newVal) -> {
                populateUserExpertiseList(newVal, userExpertiseListView);
            });

            assignmentPanel.getChildren().addAll(
                userSelector,
                userExpertiseLabel,
                userExpertiseListView,
                userActionBox
            );

            // Add panels to split pane
            mainSplit.getItems().addAll(foePanel, assignmentPanel);
            mainSplit.setDividerPositions(0.5); // Adjust initial split

            traceLayout.setCenter(mainSplit);

            // --- Bottom: Back Button ---
            HBox bottomBar = new HBox();
            bottomBar.setPadding(new Insets(15, 0, 0, 0));
            bottomBar.setAlignment(Pos.CENTER);
            Button backButton = UIStyleManager.createMenuButton("Back to Dashboard", _ -> showDashboard());
            bottomBar.getChildren().add(backButton);
            traceLayout.setBottom(bottomBar);

            setMainContent(traceLayout, "Trace - Expertise");

        } catch (Exception e) {
            DialogFactory.showError("Error displaying Trace tab: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- NEW Loader --- 
    private static void loadFieldsOfExpertiseFromDatabase() throws SQLException {
        fieldsOfExpertise = DatabaseManager.loadAllFieldsOfExpertise();
        System.out.println("Loaded " + fieldsOfExpertise.size() + " Fields of Expertise.");
    }

    // --- Helper methods for Trace Tab --- 

    private static void refreshFoEListView(ListView<FieldOfExpertise> listView) {
         try {
             TrustSystem.fieldsOfExpertise = DatabaseManager.loadAllFieldsOfExpertise(); // Reload from DB
             ObservableList<FieldOfExpertise> items = FXCollections.observableArrayList(TrustSystem.fieldsOfExpertise.values());
             FXCollections.sort(items, Comparator.comparing(FieldOfExpertise::getName));
             listView.setItems(items);
         } catch (SQLException ex) {
              DialogFactory.showError("Error reloading Fields of Expertise: " + ex.getMessage());
         }
    }

    private static void handleCreateFoE(ListView<FieldOfExpertise> listView) {
        Dialog<FieldOfExpertise> dialog = new Dialog<>();
        dialog.setTitle("Create Field of Expertise");
        dialog.setHeaderText("Enter details for the new Field of Expertise");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Name (Unique)");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        descriptionArea.setPrefRowCount(4);

        // --- Added Parent FoE ComboBox ---
        ComboBox<FieldOfExpertise> parentComboBox = new ComboBox<>();
        parentComboBox.setPromptText("Parent Expertise (Optional)");
        populateParentFoEComboBox(parentComboBox, null); // Populate with all FoEs
        // --- End Added Parent FoE ComboBox ---

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1); grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Parent:"), 0, 2); grid.add(parentComboBox, 1, 2); // Added ComboBox to grid
        dialogPane.setContent(grid);

        // Style dialog
        dialogPane.setStyle("-fx-background-color: #3e3e3e;");
        dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #3e3e3e;");
        grid.getChildren().filtered(node -> node instanceof Label).forEach(node -> node.setStyle("-fx-text-fill: white;"));

        Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String name = nameField.getText().trim();
                String description = descriptionArea.getText().trim();
                FieldOfExpertise selectedParent = parentComboBox.getValue(); // Get selected parent
                Integer parentId = (selectedParent != null) ? selectedParent.getId() : null; // Get parent ID or null

                if (name.isEmpty()) {
                    DialogFactory.showError("Name cannot be empty.");
                    return null;
                }
                // Pass parentId to constructor
                return new FieldOfExpertise(name, description, parentId);
            }
            return null;
        });

        Optional<FieldOfExpertise> result = dialog.showAndWait();
        result.ifPresent(foe -> {
            try {
                // Pass parentId to DatabaseManager method
                // Use correct method name createFieldOfExpertise instead of saveFieldOfExpertise
                int id = DatabaseManager.createFieldOfExpertise(foe.getName(), foe.getDescription(), foe.getParentId());
                if (id != -1) {
                    foe.setId(id);
                    refreshFoEListView(listView);
                    DialogFactory.showInfo("Success", "Field of Expertise created.");
                } else {
                    DialogFactory.showError("Failed to create Field of Expertise (likely duplicate name).");
                }
            } catch (SQLException e) {
                DialogFactory.showError("Database error creating FoE: " + e.getMessage());
            }
        });
    }

    private static void handleEditFoE(FieldOfExpertise foe, ListView<FieldOfExpertise> listView) {
        if (foe == null) {
            DialogFactory.showInfo("Selection Required", "Please select a Field of Expertise to edit.");
            return;
        }

        Dialog<FieldOfExpertise> dialog = new Dialog<>();
        dialog.setTitle("Edit Field of Expertise");
        dialog.setHeaderText("Edit details for " + foe.getName());
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField(foe.getName());
        TextArea descriptionArea = new TextArea(foe.getDescription());
        descriptionArea.setPrefRowCount(4);

        ComboBox<FieldOfExpertise> parentComboBox = new ComboBox<>();
        parentComboBox.setPromptText("Parent Expertise (Optional)");
        
        // Find current parent for selection
        FieldOfExpertise currentParent = null;
        if (foe.getParentId() != null) {
            currentParent = fieldsOfExpertise.get(foe.getParentId());
        }
        
        populateParentFoEComboBox(parentComboBox, foe); // Exclude this FoE and its descendants
        parentComboBox.setValue(currentParent);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1); grid.add(descriptionArea, 1, 1);
        grid.add(new Label("Parent:"), 0, 2); grid.add(parentComboBox, 1, 2);
        dialogPane.setContent(grid);

        // Style dialog
        dialogPane.setStyle("-fx-background-color: #3e3e3e;");
        dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #3e3e3e;");
        grid.getChildren().filtered(node -> node instanceof Label).forEach(node -> node.setStyle("-fx-text-fill: white;"));

        Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String name = nameField.getText().trim();
                String description = descriptionArea.getText().trim();
                FieldOfExpertise selectedParent = parentComboBox.getValue();
                Integer parentId = (selectedParent != null) ? selectedParent.getId() : null;

                if (name.isEmpty()) {
                    DialogFactory.showError("Name cannot be empty.");
                    return null;
                }

                // Create updated FoE
                FieldOfExpertise updatedFoE = new FieldOfExpertise(foe.getId(), name, description, parentId);
                return updatedFoE;
            }
            return null;
        });

        Optional<FieldOfExpertise> result = dialog.showAndWait();
        result.ifPresent(updatedFoE -> {
            try {
                DatabaseManager.updateFieldOfExpertise(updatedFoE);
                refreshFoEListView(listView);
                DialogFactory.showInfo("Success", "Field of Expertise updated successfully.");
            } catch (SQLException ex) {
                DialogFactory.showError("Error updating Field of Expertise: " + ex.getMessage());
            }
        });
    }

    private static void handleDeleteFoE(FieldOfExpertise foe, ListView<FieldOfExpertise> listView) {
        if (foe == null) {
            DialogFactory.showInfo("Selection Required", "Please select a Field of Expertise to delete.");
            return;
        }

        // Check for children - Fix Integer comparison
        boolean hasChildren = false;
        for (FieldOfExpertise candidate : fieldsOfExpertise.values()) {
            Integer candidateParentId = candidate.getParentId();
            Integer foeId = foe.getId();
            if (candidateParentId != null && foeId != null && candidateParentId.equals(foeId)) {
                hasChildren = true;
                break;
            }
        }

        if (hasChildren) {
            // Use a version of DialogFactory.showError that accepts multiple parameters
            // or split it into two calls
            DialogFactory.showError("Cannot Delete Parent");
            DialogFactory.showInfo("Child Dependencies", 
                "This Field of Expertise has child specializations that depend on it. " +
                "Delete all child specializations first or reassign them to another parent.");
            return;
        }

        // Confirm deletion
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Field of Expertise: " + foe.getName());
        confirmDialog.setContentText("Are you sure? This action cannot be undone. " +
                                     "All associations with users and phases will also be removed.");

        // Style dialog
        DialogPane dialogPane = confirmDialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #3e3e3e;");
        dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #3e3e3e;");
        dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete from database and refresh
                DatabaseManager.deleteFieldOfExpertise(foe.getId());
                refreshFoEListView(listView);
                DialogFactory.showInfo("Success", "Field of Expertise deleted successfully.");
            } catch (SQLException ex) {
                DialogFactory.showError("Error deleting Field of Expertise: " + ex.getMessage());
            }
        }
    }

    private static void populateParentFoEComboBox(ComboBox<FieldOfExpertise> comboBox, FieldOfExpertise exclude) {
        comboBox.getItems().clear();
        comboBox.getItems().add(null); // Null option for "no parent"
        
        // Add all FoEs except the one to exclude (and its descendants)
        for (FieldOfExpertise foe : fieldsOfExpertise.values()) {
            if (exclude == null || !isDescendantOrSelf(foe, exclude.getId())) {
                comboBox.getItems().add(foe);
            }
        }
        
        // Set cell factory for display
        comboBox.setCellFactory(_ -> new ListCell<FieldOfExpertise>() {
            @Override
            protected void updateItem(FieldOfExpertise item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("(No Parent - Root Level)");
                } else {
                    setText(item.getName());
                }
            }
        });
        
        // Do the same for the button cell
        comboBox.setButtonCell(new ListCell<FieldOfExpertise>() {
            @Override
            protected void updateItem(FieldOfExpertise item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("(No Parent - Root Level)");
                } else {
                    setText(item.getName());
                }
            }
        });
    }
    
    // Helper method to check if a FoE is descendant of another
    private static boolean isDescendantOrSelf(FieldOfExpertise candidate, Integer ancestorId) {
        // Fix the Integer comparison using proper null checks
        if (ancestorId != null && candidate.getId() == ancestorId) {
            return true;
        }
        
        Integer parentId = candidate.getParentId();
        if (parentId == null) {
            return false;
        }
        
        FieldOfExpertise parent = fieldsOfExpertise.get(parentId);
        if (parent == null) {
            return false;
        }
        
        return isDescendantOrSelf(parent, ancestorId);
    }

    private static void populateUserExpertiseList(User user, ListView<FieldOfExpertise> listView) {
        listView.getItems().clear();
        
        if (user == null) {
            return;
        }
        
        Set<Integer> certifiedIds = user.getCertifiedExpertiseIds();
        if (certifiedIds != null && !certifiedIds.isEmpty()) {
            List<FieldOfExpertise> userExpertises = new ArrayList<>();
            for (Integer id : certifiedIds) {
                FieldOfExpertise foe = fieldsOfExpertise.get(id);
                if (foe != null) {
                    userExpertises.add(foe);
                }
            }
            
            // Sort by name
            userExpertises.sort(Comparator.comparing(FieldOfExpertise::getName));
            listView.getItems().addAll(userExpertises);
        }
    }
    
    private static void handleCertifyUserExpertise(User user, ListView<FieldOfExpertise> listView) {
        if (user == null) {
            DialogFactory.showInfo("Selection Required", "Please select a user to certify.");
            return;
        }
        
        // Create dialog to select expertise
        Dialog<FieldOfExpertise> dialog = new Dialog<>();
        dialog.setTitle("Certify User Expertise");
        dialog.setHeaderText("Select expertise to certify for " + user.getDisplayName());
        
        // Set button types
        ButtonType certifyButtonType = new ButtonType("Certify", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(certifyButtonType, ButtonType.CANCEL);
        
        // Create expertise list view
        ListView<FieldOfExpertise> foeSelectionView = new ListView<>();
        foeSelectionView.setPrefHeight(300);
        foeSelectionView.setStyle("-fx-control-inner-background: #4e4e4e;");
        
        // Get current user expertise IDs to exclude - final copy for lambda
        final Set<Integer> userExpertiseIds = user.getCertifiedExpertiseIds() != null ?
                new HashSet<>(user.getCertifiedExpertiseIds()) : new HashSet<>();
        
        // Add all expertise not already certified for the user
        List<FieldOfExpertise> availableFoEs = new ArrayList<>();
        for (FieldOfExpertise foe : fieldsOfExpertise.values()) {
            if (!userExpertiseIds.contains(foe.getId())) {
                availableFoEs.add(foe);
            }
        }
        
        // Sort by name
        availableFoEs.sort(Comparator.comparing(FieldOfExpertise::getName));
        foeSelectionView.getItems().addAll(availableFoEs);
        
        // Create selection dialog content
        VBox dialogContent = new VBox(10);
        dialogContent.setPadding(new Insets(10));
        dialogContent.getChildren().add(foeSelectionView);
        
        // If no available expertise, show a message
        if (availableFoEs.isEmpty()) {
            Label noFoELabel = new Label("User is already certified in all expertise areas.");
            noFoELabel.setStyle("-fx-text-fill: #d9d9d9;");
            dialogContent.getChildren().add(noFoELabel);
            
            // Disable the OK button since there's nothing to select
            dialog.getDialogPane().lookupButton(certifyButtonType).setDisable(true);
        }
        
        dialog.getDialogPane().setContent(dialogContent);
        
        // Style dialog
        dialog.getDialogPane().setStyle("-fx-background-color: #3e3e3e;");
        dialog.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #3e3e3e;");
        dialog.getDialogPane().lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
        
        // Set result converter
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == certifyButtonType) {
                return foeSelectionView.getSelectionModel().getSelectedItem();
            }
            return null;
        });
        
        // Show dialog and process result
        Optional<FieldOfExpertise> result = dialog.showAndWait();
        result.ifPresent(foe -> {
            try {
                // Certify user for expertise in database
                DatabaseManager.certifyUserExpertise(user.getUsername(), foe.getId());
                
                // Update user's certified expertise set
                Set<Integer> updatedExpertiseIds = new HashSet<>(userExpertiseIds); // Create new set from the final copy
                updatedExpertiseIds.add(foe.getId());
                user.setCertifiedExpertiseIds(updatedExpertiseIds);
                
                // Refresh the user expertise list
                populateUserExpertiseList(user, listView);
                
                DialogFactory.showInfo("Success", user.getDisplayName() + " certified in " + foe.getName());
            } catch (SQLException ex) {
                DialogFactory.showError("Error certifying user expertise: " + ex.getMessage());
            }
        });
    }
    
    private static void handleRevokeUserExpertise(User user, FieldOfExpertise expertise, ListView<FieldOfExpertise> listView) {
        if (user == null || expertise == null) {
            DialogFactory.showInfo("Selection Required", "Please select both a user and expertise to revoke.");
            return;
        }
        
        // Confirm revocation
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Revoke");
        confirmDialog.setHeaderText("Revoke " + expertise.getName() + " from " + user.getDisplayName());
        confirmDialog.setContentText("This will revoke the user's certification in this expertise area. Continue?");
        
        // Style dialog
        DialogPane dialogPane = confirmDialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #3e3e3e;");
        dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #3e3e3e;");
        dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Revoke user's expertise in database
                DatabaseManager.revokeUserExpertise(user.getUsername(), expertise.getId());
                
                // Update user's certified expertise set
                Set<Integer> currentUserExpertiseIds = user.getCertifiedExpertiseIds();
                if (currentUserExpertiseIds != null) {
                    currentUserExpertiseIds.remove(expertise.getId());
                    user.setCertifiedExpertiseIds(currentUserExpertiseIds);
                }
                
                // Refresh the user expertise list
                populateUserExpertiseList(user, listView);
                
                DialogFactory.showInfo("Success", expertise.getName() + " certification revoked from " + user.getDisplayName());
            } catch (SQLException ex) {
                DialogFactory.showError("Error revoking user expertise: " + ex.getMessage());
            }
        }
    }

    // --- Implementation of handleShowJobs method ---
    private static void handleShowJobs() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            DialogFactory.showError("You must be logged in to view jobs.");
            return;
        }

        Set<Integer> userExpertiseIds = currentUser.getCertifiedExpertiseIds();
        if (userExpertiseIds == null || userExpertiseIds.isEmpty()) {
            DialogFactory.showInfo("No Expertise", "You have no certified expertise. Certify expertise in the Trace tab to see relevant jobs.");
            // Show an empty view instead of just a dialog
            BorderPane emptyLayout = new BorderPane();
            emptyLayout.setPadding(new Insets(20));
            emptyLayout.setStyle("-fx-background-color: #2e2e2e;");
            Label msgLabel = new Label("You have no certified expertise. Certify expertise in the Trace tab to see relevant jobs.");
            msgLabel.setStyle(UIStyleManager.LABEL_STYLE);
            emptyLayout.setCenter(msgLabel);
            Button backButton = UIStyleManager.createMenuButton("Back to Dashboard", _ -> showDashboard());
            HBox bottomBar = new HBox(backButton);
            bottomBar.setAlignment(Pos.CENTER);
            bottomBar.setPadding(new Insets(15));
            emptyLayout.setBottom(bottomBar);
            setMainContent(emptyLayout, "Jobs");
            return;
        }

        BorderPane jobsLayout = new BorderPane();
        jobsLayout.setPadding(new Insets(20));
        jobsLayout.setStyle("-fx-background-color: #2e2e2e;");

        Label titleLabel = new Label("Job Openings Matching Your Expertise");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #d9d9d9; -fx-font-weight: bold;");
        BorderPane.setAlignment(titleLabel, Pos.CENTER);
        jobsLayout.setTop(titleLabel);

        ListView<JobOpening> jobsListView = new ListView<>();
        jobsListView.setStyle("-fx-control-inner-background: #3e3e3e; -fx-text-fill: white; -fx-border-color: #555;");

        List<JobOpening> jobOpenings = findMatchingJobOpenings(currentUser, userExpertiseIds);

        if (jobOpenings.isEmpty()) {
            jobsListView.setPlaceholder(new Label("No job openings currently match your certified expertise."));
        } else {
            jobsListView.setItems(FXCollections.observableArrayList(jobOpenings));
            jobsListView.setCellFactory(_ -> new ListCell<JobOpening>() {
                @Override
                protected void updateItem(JobOpening item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText("Branch: " + item.branch().getName() + 
                                " | Phase: " + item.phase().name() + 
                                " | Requires: " + item.requiredExpertise().getName());
                        setStyle("-fx-text-fill: #d9d9d9;");
                    }
                }
            });
        }

        jobsLayout.setCenter(jobsListView);

        // --- Action Buttons ---
        HBox bottomBar = new HBox(10);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setPadding(new Insets(10, 0, 0, 0));

        Button applyButton = UIStyleManager.createMenuButton("Apply for Selected Job", _ -> {
            JobOpening selectedOpening = jobsListView.getSelectionModel().getSelectedItem();
            if (selectedOpening == null) {
                DialogFactory.showInfo("Selection Required", "Please select a job opening to apply for.");
                return;
            }
            handleApplyForJobOpening(currentUser, selectedOpening, jobsListView);
        });

        Button backButton2 = UIStyleManager.createMenuButton("Back to Dashboard", _ -> showDashboard());

        bottomBar.getChildren().addAll(applyButton, backButton2);
        bottomBar.setStyle("-fx-background-color: #3e3e3e; -fx-padding: 10; -fx-border-color: #555; -fx-border-width: 0 0 1 0;"); 
        jobsLayout.setBottom(bottomBar);

        setMainContent(jobsLayout, "Jobs");
    }

    // Helper class to represent a job opening
    private static class JobOpening {
        private final Branch branch;
        private final Branch.Phase phase;
        private final FieldOfExpertise requiredExpertise;
        
        public JobOpening(Branch branch, Branch.Phase phase, FieldOfExpertise requiredExpertise) {
            this.branch = branch;
            this.phase = phase;
            this.requiredExpertise = requiredExpertise;
        }
        
        public Branch branch() { return branch; }
        public Branch.Phase phase() { return phase; }
        public FieldOfExpertise requiredExpertise() { return requiredExpertise; }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            JobOpening that = (JobOpening) obj;
            return Objects.equals(branch, that.branch) && 
                   phase == that.phase && 
                   Objects.equals(requiredExpertise, that.requiredExpertise);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(branch, phase, requiredExpertise);
        }
    }

    // Helper method to find job openings matching user expertise
    private static List<JobOpening> findMatchingJobOpenings(User user, Set<Integer> userExpertiseIds) {
        List<JobOpening> openings = new ArrayList<>();
        String username = user.getUsername();

        for (Branch branch : branches.values()) {
            
                // Skip if user is already a candidate or team member for this branch
                if ((branch.getCandidates() != null && branch.getCandidates().contains(username)) || 
                    (branch.getTeam() != null && branch.getTeam().contains(username))) {
                    continue;
                }

                // Check if branch has openings
                if (branch.getTeamOpenings() <= 0) {
                    continue;
                }

                // Check each phase of the branch for required expertise
                for (Branch.Phase phase : Branch.Phase.values()) {
                    if (phase == Branch.Phase.COMPLETED) continue; // Skip completed phase

                    try {
                        Set<Integer> requiredIds = DatabaseManager.getExpertiseIdsForPhase(branch.getId(), phase.name());
                        if (requiredIds != null && !requiredIds.isEmpty()) {
                            // First check for general openings (expertise ID -1)
                            if (requiredIds.contains(-1)) {
                                // General opening - everyone qualifies
                                FieldOfExpertise generalExpertise = fieldsOfExpertise.get(-1);
                                if (generalExpertise == null) {
                                    generalExpertise = new FieldOfExpertise(-1, "None (General)", "No specific expertise required", null);
                                }
                                openings.add(new JobOpening(branch, phase, generalExpertise));
                                continue; // Move to next phase after adding general opening
                            }
                            
                            // Process specific expertise matches
                            Set<Integer> matchingIds = new HashSet<>(userExpertiseIds);
                            matchingIds.retainAll(requiredIds);

                            // If there's a match, create a JobOpening entry for each matching expertise
                            for (int expertiseId : matchingIds) {
                                FieldOfExpertise foe = fieldsOfExpertise.get(expertiseId);
                                if (foe != null) {
                                    openings.add(new JobOpening(branch, phase, foe));
                                }
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Error fetching expertise for branch " + branch.getId() + " phase " + phase.name() + ": " + e.getMessage());
                        // Optionally show an error to the user, but continue checking other branches/phases
                    }
                }
        }
        
        // Sort openings for consistent display (e.g., by branch name then phase)
        openings.sort(Comparator.comparing((JobOpening jo) -> jo.branch().getName())
                          .thenComparing(jo -> jo.phase().ordinal()));
        return openings;
    }

    // Helper method to handle applying for a job (becoming a candidate)
    // This method is called when the user clicks the "Apply for Selected Job" button
    // It uses the selected job opening to apply for the job
    // Note: This method is similar to the one above but uses the JobOpening record
    // instead of the Branch directly
    private static void handleApplyForJobOpening(User user, JobOpening jobOpening, ListView<JobOpening> jobsListView) {
        try {
            // Apply for the job through the branch associated with the opening
            jobOpening.branch().addCandidate(user.getUsername()); 
            DialogFactory.showInfo("Application Submitted", "You have applied to join the team for branch: " + 
                                  jobOpening.branch().getName());
        } catch (SQLException e) {
            DialogFactory.showInfo("Already Applied", e.getMessage());
            return;
        }
    
        // Refresh the job list view - moved outside the try-catch since these operations don't throw SQLException
        List<JobOpening> updatedOpenings = findMatchingJobOpenings(user, user.getCertifiedExpertiseIds());
        jobsListView.setItems(FXCollections.observableArrayList(updatedOpenings));
        if (updatedOpenings.isEmpty()) {
            jobsListView.setPlaceholder(new Label("No job openings currently match your certified expertise."));
        }
    }
}
