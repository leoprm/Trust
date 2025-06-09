package com.mycompany.trust;

import java.sql.SQLException;
import java.util.*;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ListCell;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ButtonBar;
import javafx.scene.Node;
import javafx.beans.property.BooleanProperty;
import javafx.collections.FXCollections;

import javafx.application.Platform;

import java.time.LocalDateTime;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import javafx.application.HostServices;

public class TrustSystem extends Application {

    public static Map<Integer, LevelProposal> levelProposals = new HashMap<>();
    public static Map<Integer, BerryEarningProposal> berryEarningProposals = new HashMap<>();
    public static Map<Integer, BerryValidityProposal> berryValidityProposals = new HashMap<>();
    public static Map<Integer, BerryConversionProposal> berryConversionProposals = new HashMap<>();
    public static Map<Integer, NeedThresholdProposal> needThresholdProposals = new HashMap<>();
    public static Map<String, List<Berry>> userBerries = new HashMap<>();
    public static Map<Integer, FieldOfExpertise> fieldsOfExpertise = new HashMap<>();

    // Fields of Expertise Manager UI components
    private static TreeView<FieldOfExpertise> currentFieldsTreeView;
    private static VBox currentFieldDetailsArea;
    private static FieldOfExpertise selectedFieldForDetails;

    private static BorderPane mainLayout;
    private static Stage primaryStage;
    private static HostServices appHostServices;

 @Override
    public void start(Stage primaryStage) {
        appHostServices = getHostServices();
        startMainSystem(primaryStage);
    }

    public static void startMainSystem(Stage primaryStage) {
        try {
            // Initialize the system (load data from database)
            initialize();
            
            TrustSystem.primaryStage = primaryStage;

            mainLayout = new BorderPane();
            mainLayout.setPrefSize(800, 600);
            mainLayout.setStyle("-fx-background-color: #2e2e2e;");

            Label welcomeLabel = new Label("Welcome, " + SessionManager.getCurrentUser().getDisplayName() + "!");
            welcomeLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-size: 24px; -fx-padding: 20px;");
            mainLayout.setTop(welcomeLabel);

            VBox sidebar = new VBox(10);
            sidebar.setPadding(new Insets(20));
            sidebar.setStyle("-fx-background-color: #1e1e1e;");
            sidebar.setPrefWidth(200);

            Button needsButton = UIStyleManager.createMenuButton("Needs", _ -> handleShowNeeds());
            Button ideasButton = UIStyleManager.createMenuButton("Ideas", _ -> handleShowIdeas());
            Button branchesButton = UIStyleManager.createMenuButton("Branches", _ -> handleShowBranches());
            Button traceButton = UIStyleManager.createMenuButton("Trace", _ -> handleShowTrace());
            Button proposalsButton = UIStyleManager.createMenuButton("Proposals", _ -> handleShowProposals());
            Button jobsButton = UIStyleManager.createMenuButton("Jobs", _ -> handleShowJobs());
            Button notificationsButton = UIStyleManager.createMenuButton("Notifications", _ -> handleShowNotifications());
            Button berriesButton = UIStyleManager.createMenuButton("Berries", _ -> handleShowBerries());
            Button profileButton = UIStyleManager.createMenuButton("My Profile", _ -> handleShowProfile());
            Button logoutButton = UIStyleManager.createMenuButton("Logout", _ -> SessionManager.handleLogout());

            sidebar.getChildren().addAll(
                needsButton, ideasButton, branchesButton, traceButton,
                proposalsButton, jobsButton, notificationsButton,
                berriesButton, profileButton, logoutButton
            );
            mainLayout.setLeft(sidebar);

            showDashboard();

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

    private static void showDashboard() {
        VBox contentArea = new VBox(15);
        contentArea.setPadding(new Insets(20));
        contentArea.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Trust System Dashboard");
        titleLabel.setStyle("-fx-text-fill: #d9d9d9; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label subtitleLabel = new Label("Select an option from the menu to get started");
        subtitleLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 16px;");

        User currentUser = SessionManager.getCurrentUser();
        VBox statsBox = new VBox(5);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(20));
        statsBox.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 5px;");

        Label levelLabel = new Label("Level: " + currentUser.getLevel());
        levelLabel.setStyle("-fx-text-fill: #d9d9d9;");

        Label xpLabel = new Label("XP: " + currentUser.getXp() +
                "/" + SystemParameters.calculateXpThreshold(currentUser.getLevel()));
        xpLabel.setStyle("-fx-text-fill: #d9d9d9;");

        Label pointsLabel = new Label("Points: " + currentUser.getPoints());
        pointsLabel.setStyle("-fx-text-fill: #d9d9d9;");

        // Load user berries from database to ensure accurate berry count in dashboard
        try {
            List<Berry> berries = DatabaseManager.getUserBerries(currentUser.getUsername());
            userBerries.put(currentUser.getUsername(), berries);
        } catch (SQLException e) {
            System.err.println("Error loading berries for dashboard: " + e.getMessage());
        }

        int berryCount = BerryService.getUserTotalBerries(currentUser.getUsername());
        Label berriesLabel = new Label("Berries: " + berryCount);
        berriesLabel.setStyle("-fx-text-fill: #d9d9d9;");

        statsBox.getChildren().addAll(levelLabel, xpLabel, pointsLabel, berriesLabel);

        contentArea.getChildren().addAll(titleLabel, subtitleLabel, statsBox);

        mainLayout.setCenter(contentArea);
        primaryStage.setTitle("Trust System - Dashboard");
    }

    private static void setMainContent(Node content, String title) {
        mainLayout.setCenter(content);
        primaryStage.setTitle("Trust System - " + title);
    }

    public static void initialize() {
        try {
            DatabaseConnection.initializeDataSource();
            loadBerriesFromDatabase();
            loadProposalsFromDatabase();
            loadFieldsOfExpertiseFromDatabase();
            BerryService.checkAndRemoveExpiredBerries();
        } catch (SQLException e) {
            DialogFactory.showError("Database initialization error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void loadBerriesFromDatabase() throws SQLException {
        userBerries = BerryService.loadAllUserBerries();
    }

    private static void loadProposalsFromDatabase() throws SQLException {
        levelProposals = DatabaseManager.loadAllLevelProposals();
        berryEarningProposals = DatabaseManager.loadAllBerryEarningProposals();
        berryValidityProposals = DatabaseManager.loadAllBerryValidityProposals();
        berryConversionProposals = DatabaseManager.loadAllBerryConversionProposals();
        needThresholdProposals = DatabaseManager.loadAllNeedThresholdProposals();
        updateSystemParametersFromActiveProposals();
    }

    private static void loadFieldsOfExpertiseFromDatabase() throws SQLException {
        fieldsOfExpertise = DatabaseManager.loadAllFieldsOfExpertise();
    }

    public static boolean registerUser(String username, String password, String displayName) {
        try {
            User existingUser = DatabaseManager.getUser(username);
            if (existingUser != null) {
                DialogFactory.showInfo("Registration Failed", "Username already exists.");
                return false;
            }

            User newUser = new User(username, displayName);
            newUser.setPassword(password);

            int userId = DatabaseManager.saveUser(newUser);

            if (userId != -1) {
                newUser.setId(userId);
                return true;
            }
        } catch (SQLException e) {
            DialogFactory.showError("Error registering user: " + e.getMessage());
        }

        return false;
    }

    public static boolean isIdeaAuthor(int ideaId, String username) {
        try {
            Idea idea = DatabaseManager.getIdea(ideaId);
            return idea != null && idea.getAuthor().equals(username);
        } catch (SQLException e) {
            System.err.println("Error checking idea author: " + e.getMessage());
            return false;
        }
    }

    public static void processMonthlyBerryDistribution() throws SQLException {
        loadProposalsFromDatabase();

        List<User> allUsers = DatabaseManager.getAllUsersList();
        for (User user : allUsers) {
            int level = user.getLevel();
            BerryService.distributeBerriesForLevel(user, level);
        }

        DialogFactory.showInfo("Monthly Distribution Complete",
            "Monthly Berry distribution completed for all users.");
    }

    private static void handleShowNeeds() {
        try {
            // Load all needs from DB
            Map<Integer, Need> needs = DatabaseManager.loadAllNeeds();
            User currentUser = SessionManager.getCurrentUser();
            
            VBox content = new VBox(20);
            content.setPadding(new Insets(30));
            content.setStyle("-fx-background-color: #2e2e2e;");

            // Header section with title and stats
            VBox header = new VBox(10);
            Label title = new Label("🎯 Needs Hub");
            title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 28px; -fx-font-weight: bold;");
            
            Label subtitle = new Label("Identify, support, and track community needs");
            subtitle.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 14px; -fx-font-style: italic;");

            // Calculate and show remaining points
            int totalAssigned = 0;
            int needsWithPoints = 0;
            for (Need n : needs.values()) {
                int userPoints = n.getSupporters().getOrDefault(currentUser.getUsername(), 0);
                totalAssigned += userPoints;
                if (userPoints > 0) {
                    needsWithPoints++;
                }
            }
            int pointsLeft = 100 - totalAssigned;
            
            // Stats bar
            HBox statsBar = new HBox(30);
            statsBar.setAlignment(Pos.CENTER_LEFT);
            statsBar.setPadding(new Insets(15));
            statsBar.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 10px;");
            
            Label totalNeedsLabel = new Label("📊 Total Needs: " + needs.size());
            totalNeedsLabel.setStyle("-fx-text-fill: #76ff76; -fx-font-size: 14px; -fx-font-weight: bold;");
            
            Label supportedLabel = new Label("💰 Supported: " + needsWithPoints);
            supportedLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 14px; -fx-font-weight: bold;");
            
            Label pointsLeftLabel = new Label("⚡ Points Left: " + pointsLeft + "/100");
            String pointsColor = pointsLeft > 50 ? "#76ff76" : pointsLeft > 20 ? "#ffaa00" : "#ff6666";
            pointsLeftLabel.setStyle("-fx-text-fill: " + pointsColor + "; -fx-font-size: 14px; -fx-font-weight: bold;");
            
            statsBar.getChildren().addAll(totalNeedsLabel, supportedLabel, pointsLeftLabel);
            header.getChildren().addAll(title, subtitle, statsBar);
            content.getChildren().add(header);

            // Enhanced Needs List with custom cells
            ListView<Need> needsList = new ListView<>();
            needsList.setItems(FXCollections.observableArrayList(needs.values()));
            needsList.setCellFactory(_ -> new ListCell<>() {
                @Override
                protected void updateItem(Need need, boolean empty) {
                    super.updateItem(need, empty);
                    if (empty || need == null) {
                        setGraphic(null);
                        setText(null);
                        setStyle("-fx-background-color: transparent;");
                    } else {
                        VBox cardContent = new VBox(10);
                        cardContent.setPadding(new Insets(15));
                        cardContent.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 8px; -fx-border-color: #555; -fx-border-radius: 8px;");
                        
                        // Header row with name and user's points
                        HBox headerRow = new HBox(10);
                        headerRow.setAlignment(Pos.CENTER_LEFT);
                        
                        Label nameLabel = new Label(need.getName());
                        nameLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold;");
                        
                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);
                        
                        int userPoints = need.getSupporters().getOrDefault(currentUser.getUsername(), 0);
                        if (userPoints > 0) {
                            Label pointsLabel = new Label(userPoints + " pts");
                            pointsLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 12px; -fx-font-weight: bold; " +
                                               "-fx-background-color: #ffd70022; -fx-padding: 4 8; -fx-background-radius: 12px;");
                            headerRow.getChildren().addAll(nameLabel, spacer, pointsLabel);
                        } else {
                            headerRow.getChildren().addAll(nameLabel, spacer);
                        }
                        
                        // ID and total support info
                        HBox infoRow = new HBox(20);
                        infoRow.setAlignment(Pos.CENTER_LEFT);
                        
                        Label idLabel = new Label("🆔 ID: " + need.getId());
                        idLabel.setStyle("-fx-text-fill: #87ceeb; -fx-font-size: 12px;");
                        
                        int totalSupport = need.getSupporters().values().stream().mapToInt(Integer::intValue).sum();
                        Label totalSupportLabel = new Label("💪 Total Support: " + totalSupport + " pts");
                        totalSupportLabel.setStyle("-fx-text-fill: #76ff76; -fx-font-size: 12px;");
                        
                        int supporterCount = need.getSupporters().size();
                        Label supportersLabel = new Label("👥 " + supporterCount + " supporters");
                        supportersLabel.setStyle("-fx-text-fill: #ff9999; -fx-font-size: 12px;");
                        
                        // Add affected indicator if user has points and is affected
                        if (userPoints > 0 && need.getAffectedUsers().containsKey(currentUser.getUsername())) {
                            Label affectedIndicator = new Label("📍 Affected");
                            affectedIndicator.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 11px; -fx-font-weight: bold;");
                            infoRow.getChildren().addAll(idLabel, totalSupportLabel, supportersLabel, affectedIndicator);
                        } else {
                            infoRow.getChildren().addAll(idLabel, totalSupportLabel, supportersLabel);
                        }
                        
                        cardContent.getChildren().addAll(headerRow, infoRow);
                        
                        // Add hover effect
                        cardContent.setOnMouseEntered(_ -> 
                            cardContent.setStyle("-fx-background-color: #4a4a4a; -fx-background-radius: 8px; -fx-border-color: #777; -fx-border-radius: 8px;"));
                        cardContent.setOnMouseExited(_ -> 
                            cardContent.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 8px; -fx-border-color: #555; -fx-border-radius: 8px;"));
                        
                        setGraphic(cardContent);
                        setText(null);
                        setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                    }
                }
            });
            
            needsList.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
            needsList.setPrefHeight(400);
            content.getChildren().add(needsList);

            // Enhanced action buttons
            HBox actions = new HBox(15);
            actions.setAlignment(Pos.CENTER);
            actions.setPadding(new Insets(20, 0, 0, 0));
            
            Button createNeedBtn = createStyledButton("✨ Create Need", "#00aa00");
            Button assignPointsBtn = createStyledButton("💰 Assign Points", "#aa7700");
            Button markAffectedBtn = createStyledButton("📍 Mark Affected", "#aa0077");
            
            actions.getChildren().addAll(createNeedBtn, assignPointsBtn, markAffectedBtn);
            content.getChildren().add(actions);

            // Create Need
            createNeedBtn.setOnAction(_ -> {
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Create New Need");
                dialog.setHeaderText("Enter the details for the new Need:");
                
                VBox formContent = new VBox(10);
                formContent.setPadding(new Insets(10));
                
                TextField nameField = new TextField();
                nameField.setPromptText("Need name (e.g., 'Clean Water Access', 'Public Transportation')");
                
                TextArea descField = new TextArea();
                descField.setPromptText("Describe the need in detail...");
                descField.setPrefRowCount(3);
                
                formContent.getChildren().addAll(
                    new Label("Need Name:"),
                    nameField,
                    new Label("Description (Optional):"),
                    descField
                );
                
                dialog.getDialogPane().setContent(formContent);
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                UIStyleManager.enhanceDialogWithKeyboardNavigation(dialog);
                
                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    String name = nameField.getText().trim();
                    
                    if (name.isEmpty()) {
                        DialogFactory.showError("Need name cannot be empty.");
                        return;
                    }
                    
                    try {
                        int needId = DatabaseManager.createNeed(name);
                        if (needId > 0) {
                            DialogFactory.showInfo("Need Created", "Need '" + name + "' created successfully with ID: " + needId);
                            handleShowNeeds(); // Refresh the list
                        } else {
                            DialogFactory.showError("Failed to create need.");
                        }
                    } catch (SQLException ex) {
                        DialogFactory.showError("Error creating need: " + ex.getMessage());
                    }
                }
            });

            // Assign Points
            assignPointsBtn.setOnAction(_ -> {
                Need selectedNeed = needsList.getSelectionModel().getSelectedItem();
                if (selectedNeed == null) {
                    DialogFactory.showError("Please select a Need to assign points to.");
                    return;
                }

                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Assign Points to Need");
                dialog.setHeaderText("Assign points to: " + selectedNeed.getName());
                dialog.getDialogPane().setStyle("-fx-background-color: #2e2e2e;");

                VBox formContent = new VBox(15);
                formContent.setPadding(new Insets(15));

                TextField pointsField = new TextField();
                pointsField.setPromptText("Enter points to assign");
                pointsField.setStyle("-fx-background-color: #3e3e3e; -fx-text-fill: #fff;");
                
                Label pointsInfoLabel = new Label("You can assign points to support this need.");
                pointsInfoLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 12px.");

                formContent.getChildren().addAll(
                    new Label("Points:"), // Consider styling this label for consistency
                    pointsField,
                    pointsInfoLabel
                );
                
                dialog.getDialogPane().setContent(formContent);
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                UIStyleManager.enhanceDialogWithKeyboardNavigation(dialog);
                
                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    try {
                        int points = Integer.parseInt(pointsField.getText().trim());
                        if (points < 0) {
                            DialogFactory.showError("Points must be non-negative.");
                            return;
                        }
                        
                        // Get current user's points for this need
                        int currentUserPoints = selectedNeed.getSupporters().getOrDefault(currentUser.getUsername(), 0);
                        // Get total points assigned to this need
                        int totalAssignedNow = selectedNeed.getSupporters().values().stream()
                                .mapToInt(Integer::intValue).sum();
                        // Check if total would exceed 100
                        int newTotal = totalAssignedNow - currentUserPoints + points;
                        if (newTotal > 100) {
                            DialogFactory.showError("You cannot assign more than 100 points in total. You have " + (100 - (totalAssignedNow - currentUserPoints)) + " points available.");
                            return;
                        }

                        // Update points in database
                        DatabaseManager.addNeedSupporter(selectedNeed.getId(), currentUser.getUsername(), points);
                        
                        String message = points == 0 ? 
                            "Removed your support from '" + selectedNeed.getName() + "'." :
                            "Assigned " + points + " points to '" + selectedNeed.getName() + "'.";
                        DialogFactory.showInfo("Points Updated", message);
                        handleShowNeeds(); // Refresh the list
                    } catch (NumberFormatException ex) {
                        DialogFactory.showError("Please enter a valid number.");
                    } catch (SQLException ex) {
                        DialogFactory.showError("Error assigning points: " + ex.getMessage());
                    }
                }
            });

            // Mark as Affected
            markAffectedBtn.setOnAction(_ -> {
                Need selected = needsList.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    DialogFactory.showError("Please select a Need to mark yourself as affected.");
                    return;
                }
                
                // Only allow if user has assigned points to this Need
                int userPointsForNeed = selected.getSupporters().getOrDefault(currentUser.getUsername(), 0);
                if (userPointsForNeed <= 0) {
                    DialogFactory.showError("You must assign points to this Need before you can mark yourself as affected by it.");
                    return;
                }
                
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.setTitle("Mark as Affected");
                dialog.setHeaderText("Mark yourself as affected by '" + selected.getName() + "'");
                
                VBox formContent = new VBox(15);
                formContent.setPadding(new Insets(15));
                
                Label infoLabel = new Label(String.format(
                    "You are about to mark yourself as affected by this need.\n" +
                    "Your current support: %d points", userPointsForNeed
                ));
                infoLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 12px;");
                
                TextField locationField = new TextField();
                locationField.setPromptText("Enter your location (e.g., 'Downtown', 'North District', 'Main St')");
                
                TextArea impactField = new TextArea();
                impactField.setPromptText("Describe how this need affects you (optional)...");
                impactField.setPrefRowCount(3);
                
                Label privacyLabel = new Label("🔒 Your location information will be used to understand the geographic distribution of this need.");
                privacyLabel.setStyle("-fx-text-fill: #87ceeb; -fx-font-size: 11px; -fx-font-style: italic;");
                privacyLabel.setWrapText(true);
                
                formContent.getChildren().addAll(
                    infoLabel,
                    new Label("Your Location:"),
                    locationField,
                    new Label("Impact Description (Optional):"),
                    impactField,
                    privacyLabel
                );
                
                dialog.getDialogPane().setContent(formContent);
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
                UIStyleManager.enhanceDialogWithKeyboardNavigation(dialog);
                
                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    String location = locationField.getText().trim();
                    if (location.isEmpty()) {
                        DialogFactory.showError("Please enter your location.");
                        return;
                    }
                    
                    try {
                        DatabaseManager.addNeedAffectedUser(selected.getId(), currentUser.getUsername(), location);
                        DialogFactory.showInfo("Marked as Affected", 
                            "You are now marked as affected by '" + selected.getName() + "' in location: " + location);
                        handleShowNeeds(); // Refresh the list
                    } catch (SQLException ex) {
                        DialogFactory.showError("Error marking as affected: " + ex.getMessage());
                    }
                }
            });

            setMainContent(content, "Needs Management");
        } catch (SQLException e) {
            DialogFactory.showError("Error loading needs: " + e.getMessage());
        }
    }

    private static void handleShowIdeas() {
        try {
            // Load all ideas from DB
            Map<Integer, Idea> ideas = DatabaseManager.loadAllIdeas();
            // Load all needs for association selection
            Map<Integer, Need> availableNeeds = DatabaseManager.loadAllNeeds();
            User currentUser = SessionManager.getCurrentUser();
            
            VBox content = new VBox(20);
            content.setPadding(new Insets(30));
            content.setStyle("-fx-background-color: #2e2e2e;");

            // Header section with title and stats
            VBox header = new VBox(10);
            Label title = new Label("💡 Ideas Explorer");
            title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 28px; -fx-font-weight: bold;");
            
            Label subtitle = new Label("Discover, create, and collaborate on innovative ideas");
            subtitle.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 14px; -fx-font-style: italic;");
            
            // Stats bar
            HBox statsBar = new HBox(30);
            statsBar.setAlignment(Pos.CENTER_LEFT);
            statsBar.setPadding(new Insets(15));
            statsBar.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 10px;");
            
            Label totalIdeasLabel = new Label("📊 Total Ideas: " + ideas.size());
            totalIdeasLabel.setStyle("-fx-text-fill: #76ff76; -fx-font-size: 14px; -fx-font-weight: bold;");
            
            long myIdeasCount = ideas.values().stream()
                    .filter(idea -> idea.getAuthor().equals(currentUser.getUsername()))
                    .count();
            Label myIdeasLabel = new Label("✏️ My Ideas: " + myIdeasCount);
            myIdeasLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 14px; -fx-font-weight: bold;");
            
            long votedIdeasCount = ideas.values().stream()
                    .filter(idea -> idea.getSupporters().contains(currentUser.getUsername()))
                    .count();
            Label votedLabel = new Label("👍 Voted: " + votedIdeasCount);
            votedLabel.setStyle("-fx-text-fill: #87ceeb; -fx-font-size: 14px; -fx-font-weight: bold;");
            
            statsBar.getChildren().addAll(totalIdeasLabel, myIdeasLabel, votedLabel);
            header.getChildren().addAll(title, subtitle, statsBar);
            content.getChildren().add(header);

            // Enhanced Ideas List with custom cells
            ListView<Idea> ideasList = new ListView<>();
            ideasList.setItems(FXCollections.observableArrayList(ideas.values()));
            ideasList.setCellFactory(_ -> new ListCell<>() {
                @Override
                protected void updateItem(Idea idea, boolean empty) {
                    super.updateItem(idea, empty);
                    if (empty || idea == null) {
                        setGraphic(null);
                        setText(null);
                        setStyle("-fx-background-color: transparent;");
                    } else {
                        VBox cardContent = new VBox(10);
                        cardContent.setPadding(new Insets(15));
                        cardContent.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 8px; -fx-border-color: #555; -fx-border-radius: 8px;");
                        
                        // Header row with name and status
                        HBox headerRow = new HBox(10);
                        headerRow.setAlignment(Pos.CENTER_LEFT);
                        
                        Label nameLabel = new Label(idea.getName());
                        nameLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold;");
                        
                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);
                        
                        Label statusLabel = new Label(idea.getStatus() != null ? idea.getStatus() : "Draft");
                        String statusColor = getStatusColor(idea.getStatus());
                        statusLabel.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 12px; -fx-font-weight: bold; " +
                                           "-fx-background-color: " + statusColor + "22; -fx-padding: 4 8; -fx-background-radius: 12px;");
                        
                        headerRow.getChildren().addAll(nameLabel, spacer, statusLabel);
                        
                        // Description (truncated)
                        String description = idea.getDescription();
                        if (description.length() > 100) {
                            description = description.substring(0, 100) + "...";
                        }
                        Label descLabel = new Label(description);
                        descLabel.setStyle("-fx-text-fill: #d0d0d0; -fx-font-size: 13px;");
                        descLabel.setWrapText(true);
                        
                        // Info row with author, votes, and needs
                        HBox infoRow = new HBox(20);
                        infoRow.setAlignment(Pos.CENTER_LEFT);
                        
                        Label authorLabel = new Label("👤 " + idea.getAuthor());
                        authorLabel.setStyle("-fx-text-fill: #87ceeb; -fx-font-size: 12px;");
                        
                        Label votesLabel = new Label("👍 " + idea.getVoteCount());
                        votesLabel.setStyle("-fx-text-fill: #76ff76; -fx-font-size: 12px;");
                        
                        Set<Integer> associatedNeeds = idea.getAssociatedNeedIds();
                        String needsText = (associatedNeeds != null && !associatedNeeds.isEmpty()) 
                            ? "🎯 " + associatedNeeds.size() + " needs"
                            : "🎯 No needs";
                        Label needsLabel = new Label(needsText);
                        needsLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 12px;");
                        
                        // Add voting indicator if user has voted
                        if (idea.getSupporters().contains(currentUser.getUsername())) {
                            Label votedIndicator = new Label("✅ Voted");
                            votedIndicator.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 11px; -fx-font-weight: bold;");
                            infoRow.getChildren().addAll(authorLabel, votesLabel, needsLabel, votedIndicator);
                        } else {
                            infoRow.getChildren().addAll(authorLabel, votesLabel, needsLabel);
                        }
                        
                        cardContent.getChildren().addAll(headerRow, descLabel, infoRow);
                        
                        // Add hover effect
                        cardContent.setOnMouseEntered(_ -> 
                            cardContent.setStyle("-fx-background-color: #4a4a4a; -fx-background-radius: 8px; -fx-border-color: #777; -fx-border-radius: 8px;"));
                        cardContent.setOnMouseExited(_ -> 
                            cardContent.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 8px; -fx-border-color: #555; -fx-border-radius: 8px;"));
                        
                        setGraphic(cardContent);
                        setText(null);
                        setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                    }
                }
                
                private String getStatusColor(String status) {
                    if (status == null) return "#999999";
                    return switch (status.toLowerCase()) {
                        case "active", "approved", "open" -> "#76ff76";
                        case "pending", "draft" -> "#ffd700";
                        case "rejected", "closed", "denied" -> "#ff6b6b";
                        case "completed", "implemented" -> "#32cd32";
                        case "expired" -> "#b2b2b2";
                        default -> "#87ceeb";
                    };
                }
            });
            
            ideasList.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
            ideasList.setPrefHeight(400);
            content.getChildren().add(ideasList);

            // Enhanced action buttons
            HBox actions = new HBox(15);
            actions.setAlignment(Pos.CENTER);
            actions.setPadding(new Insets(20, 0, 0, 0));
            
            Button createIdeaBtn = createStyledButton("✨ Create Idea", "#00aa00");
            Button associateNeedsBtn = createStyledButton("🔗 Associate Needs", "#0077aa");
            Button voteBtn = createStyledButton("👍 Vote", "#aa7700");
            Button viewDetailsBtn = createStyledButton("📋 Details", "#7700aa");
            
            actions.getChildren().addAll(createIdeaBtn, associateNeedsBtn, voteBtn, viewDetailsBtn);
            content.getChildren().add(actions);

            // Create Idea
            createIdeaBtn.setOnAction(_ -> {
                showElegantCreateIdeaDialog(currentUser);
            });

            // Associate with Needs
            associateNeedsBtn.setOnAction(_ -> {
                Idea selected = ideasList.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    DialogFactory.showError("Select an Idea to associate with Needs.");
                    return;
                }
                showElegantAssociateNeedsDialog(selected, availableNeeds);
            });

            // Vote for Idea
            voteBtn.setOnAction(_ -> {
                Idea selected = ideasList.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    DialogFactory.showError("Select an Idea to vote for.");
                    return;
                }
                
                try {
                    User user = SessionManager.getCurrentUser();
                    IdeaService.voteForIdea(selected.getId(), user);
                    handleShowIdeas(); // Refresh the list
                } catch (SQLException ex) {
                    DialogFactory.showError("Error voting for idea: " + ex.getMessage());
                }
            });

            // View Details
            viewDetailsBtn.setOnAction(_ -> {
                Idea selected = ideasList.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    DialogFactory.showError("Select an Idea to view details.");
                    return;
                }
                showElegantIdeaDetails(selected);
            });

            setMainContent(content, "Ideas Management");
        } catch (SQLException e) {
            DialogFactory.showError("Error loading ideas: " + e.getMessage());
        }
    }

    private static void handleShowBranches() {
        try {
            // Load all branches from DB
            Map<Integer, Branch> branches = DatabaseManager.loadAllBranches();
            // Load all ideas for branch association
            Map<Integer, Idea> availableIdeas = DatabaseManager.loadAllIdeas();
            
            VBox content = new VBox(20);
            content.setPadding(new Insets(30));
            content.setStyle("-fx-background-color: #2e2e2e;");

            // Header section with title and stats
            VBox header = new VBox(10);
            Label title = new Label("🌳 Branches Explorer");
            title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 28px; -fx-font-weight: bold;");
            
            Label subtitle = new Label("Manage project branches and track development phases");
            subtitle.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 14px; -fx-font-style: italic;");
            
            // Stats bar
            HBox statsBar = new HBox(30);
            statsBar.setAlignment(Pos.CENTER_LEFT);
            statsBar.setPadding(new Insets(15));
            statsBar.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 10px;");
            
            Label totalBranchesLabel = new Label("📊 Total Branches: " + branches.size());
            totalBranchesLabel.setStyle("-fx-text-fill: #76ff76; -fx-font-size: 14px; -fx-font-weight: bold;");
            
            long rootBranchesCount = branches.values().stream()
                    .filter(branch -> branch.getParentId() == 0)
                    .count();
            Label rootBranchesLabel = new Label("🌱 Root Branches: " + rootBranchesCount);
            rootBranchesLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 14px; -fx-font-weight: bold;");
            
            long subBranchesCount = branches.size() - rootBranchesCount;
            Label subBranchesLabel = new Label("🌿 Sub-branches: " + subBranchesCount);
            subBranchesLabel.setStyle("-fx-text-fill: #87ceeb; -fx-font-size: 14px; -fx-font-weight: bold;");
            
            statsBar.getChildren().addAll(totalBranchesLabel, rootBranchesLabel, subBranchesLabel);
            header.getChildren().addAll(title, subtitle, statsBar);
            content.getChildren().add(header);

            // Enhanced Branches List with custom cells
            ListView<Branch> branchesList = new ListView<>();
            branchesList.setItems(FXCollections.observableArrayList(branches.values()));
            branchesList.setCellFactory(_ -> new ListCell<>() {
                @Override
                protected void updateItem(Branch branch, boolean empty) {
                    super.updateItem(branch, empty);
                    if (empty || branch == null) {
                        setGraphic(null);
                        setText(null);
                        setStyle("-fx-background-color: transparent;");
                    } else {
                        VBox cardContent = new VBox(10);
                        cardContent.setPadding(new Insets(15));
                        cardContent.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 8px; -fx-border-color: #555; -fx-border-radius: 8px;");
                        
                        // Header row with name and phase
                        HBox headerRow = new HBox(10);
                        headerRow.setAlignment(Pos.CENTER_LEFT);
                        
                        // Add hierarchy indicator
                        String hierarchyIndicator = branch.getParentId() == 0 ? "🌱" : "🌿";
                        Label nameLabel = new Label(hierarchyIndicator + " " + branch.getName());
                        nameLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold;");
                        
                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);
                        
                        Label phaseLabel = new Label(branch.getCurrentPhase() != null ? branch.getCurrentPhase().toString() : "GENERATION");
                        String phaseColor = getPhaseColor(branch.getCurrentPhase());
                        phaseLabel.setStyle("-fx-text-fill: " + phaseColor + "; -fx-font-size: 12px; -fx-font-weight: bold; " +
                                           "-fx-background-color: " + phaseColor + "22; -fx-padding: 4 8; -fx-background-radius: 12px;");
                        
                        headerRow.getChildren().addAll(nameLabel, spacer, phaseLabel);
                        
                        // Description (truncated)
                        String description = branch.getDescription() != null ? branch.getDescription() : "No description";
                        if (description.length() > 100) {
                            description = description.substring(0, 100) + "...";
                        }
                        Label descLabel = new Label(description);
                        descLabel.setStyle("-fx-text-fill: #d0d0d0; -fx-font-size: 13px;");
                        descLabel.setWrapText(true);
                        
                        // Info row with idea, parent, and children
                        HBox infoRow = new HBox(20);
                        infoRow.setAlignment(Pos.CENTER_LEFT);
                        
                        // Associated idea info
                        String ideaText = "💡 No Idea";
                        if (branch.getIdeaId() > 0 && availableIdeas.containsKey(branch.getIdeaId())) {
                            ideaText = "💡 " + availableIdeas.get(branch.getIdeaId()).getName();
                        }
                        Label ideaLabel = new Label(ideaText);
                        ideaLabel.setStyle("-fx-text-fill: #87ceeb; -fx-font-size: 12px;");
                        
                        // Parent info
                        String parentText = branch.getParentId() == 0 ? "🌱 Root" : "🌿 Sub-branch";
                        Label parentLabel = new Label(parentText);
                        parentLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 12px;");
                        
                        // Children count
                        long childrenCount = branches.values().stream()
                                .filter(b -> b.getParentId() == branch.getId())
                                .count();
                        Label childrenLabel = new Label("👥 " + childrenCount + " children");
                        childrenLabel.setStyle("-fx-text-fill: #76ff76; -fx-font-size: 12px;");
                        
                        infoRow.getChildren().addAll(ideaLabel, parentLabel, childrenLabel);
                        
                        cardContent.getChildren().addAll(headerRow, descLabel, infoRow);
                        
                        // Add hover effect
                        cardContent.setOnMouseEntered(_ -> 
                            cardContent.setStyle("-fx-background-color: #4a4a4a; -fx-background-radius: 8px; -fx-border-color: #777; -fx-border-radius: 8px;"));
                        cardContent.setOnMouseExited(_ -> 
                            cardContent.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 8px; -fx-border-color: #555; -fx-border-radius: 8px;"));
                        
                        setGraphic(cardContent);
                        setText(null);
                        setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                    }
                }
                
                private String getPhaseColor(Branch.Phase phase) {
                    if (phase == null) return "#999999";
                    return switch (phase) {
                        case GENERATION -> "#ff9500";
                        case INVESTIGATION -> "#0099ff";
                        case DEVELOPMENT -> "#ffd700";
                        case PRODUCTION -> "#00ff00";
                        case DISTRIBUTION -> "#87ceeb";
                        case MAINTENANCE -> "#ff6b6b";
                        case RECYCLING -> "#9370db";
                        case COMPLETED -> "#32cd32";
                        default -> "#999999";
                    };
                }
            });
            
            branchesList.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
            branchesList.setPrefHeight(400);
            content.getChildren().add(branchesList);

            // Enhanced action buttons
            HBox actions = new HBox(15);
            actions.setAlignment(Pos.CENTER);
            actions.setPadding(new Insets(20, 0, 0, 0));
            
            Button createRootBtn = createStyledButton("🌱 Create Root Branch", "#00aa00");
            Button createSubBtn = createStyledButton("🌿 Create Sub-branch", "#0077aa");
            Button associateIdeaBtn = createStyledButton("💡 Associate Idea", "#aa7700");
            Button viewDetailsBtn = createStyledButton("📋 Details", "#7700aa");
            Button teamOpeningsBtn = createStyledButton("👥 Team Openings", "#aa4400");
            
            actions.getChildren().addAll(createRootBtn, createSubBtn, associateIdeaBtn, viewDetailsBtn, teamOpeningsBtn);
            content.getChildren().add(actions);

            // Create Root Branch
            createRootBtn.setOnAction(_ -> {
                showElegantCreateBranchDialog(null, availableIdeas);
            });

            // Create Sub-branch
            createSubBtn.setOnAction(_ -> {
                Branch selected = branchesList.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    DialogFactory.showError("Select a Branch to create a sub-branch for.");
                    return;
                }
                showElegantCreateBranchDialog(selected, availableIdeas);
            });

            // Associate with Idea
            associateIdeaBtn.setOnAction(_ -> {
                Branch selected = branchesList.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    DialogFactory.showError("Select a Branch to associate with an Idea.");
                    return;
                }
                showElegantAssociateIdeaDialog(selected, availableIdeas);
            });

            // View Details
            viewDetailsBtn.setOnAction(_ -> {
                Branch selected = branchesList.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    DialogFactory.showError("Select a Branch to view details.");
                    return;
                }
                showElegantBranchDetails(selected, branches, availableIdeas);
            });

            // Manage Team Openings
            teamOpeningsBtn.setOnAction(_ -> {
                Branch selected = branchesList.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    DialogFactory.showError("Select a Branch to manage team openings.");
                    return;
                }
                showTeamOpeningsDialog(selected);
            });

            setMainContent(content, "Branches Management");
        } catch (SQLException e) {
            DialogFactory.showError("Error loading branches: " + e.getMessage());
        }
    }

    private static void handleShowTrace() {
        try {
            // Load all fields of expertise from database
            fieldsOfExpertise = DatabaseManager.loadAllFieldsOfExpertise();
            
            VBox content = new VBox(20);
            content.setPadding(new Insets(30));
            content.setStyle("-fx-background-color: #2e2e2e;");

            // Create the main management interface
            createFieldsOfExpertiseManager(content);

            setMainContent(content, "Fields of Expertise");
        } catch (SQLException e) {
            DialogFactory.showError("Error loading Fields of Expertise: " + e.getMessage());
        }
    }

    private static void handleShowProposals() {
        try {
            // Load all proposals from database
            loadProposalsFromDatabase();
        } catch (SQLException e) {
            DialogFactory.showError("Error loading proposals: " + e.getMessage());
            return;
        }
        
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #2e2e2e;");

        // Header section
        Label title = new Label("📋 Proposals Management");
        title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 28px; -fx-font-weight: bold;");
        
        Label subtitle = new Label("View and manage system proposals");
        subtitle.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 14px; -fx-font-style: italic;");

        // Filter and action buttons
        HBox actionBar = new HBox(10);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        
        Button createProposalBtn = new Button("+ Create Proposal");
        createProposalBtn.setStyle(UIStyleManager.BUTTON_STYLE);
        createProposalBtn.setOnAction(e -> showCreateProposalDialog());
        
        ToggleButton activeOnlyToggle = new ToggleButton("Active Only");
        activeOnlyToggle.setStyle(UIStyleManager.BUTTON_STYLE);
        activeOnlyToggle.setSelected(true);
        
        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle(UIStyleManager.BUTTON_STYLE);
        refreshBtn.setOnAction(e -> refreshProposalContent());
        
        actionBar.getChildren().addAll(createProposalBtn, activeOnlyToggle, refreshBtn);

        // Create TabPane for different proposal categories
        TabPane proposalTabs = new TabPane();
        proposalTabs.setStyle("-fx-background-color: #2e2e2e; " +
                              "-fx-tab-header-background: #1e1e1e; " +
                              "-fx-tab-header-area-tab-padding: 0.3em 0.2em; " +
                              "-fx-tab-text-fill: #b2b2b2; " +
                              "-fx-selection-bar: #404040;");
        proposalTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Create tabs for each proposal type
        Tab allTab = createProposalTab("All Proposals", "all", activeOnlyToggle.selectedProperty());
        Tab levelTab = createProposalTab("Level System", "level", activeOnlyToggle.selectedProperty());
        Tab berryEarningTab = createProposalTab("Berry Earning", "berry_earning", activeOnlyToggle.selectedProperty());
        Tab berryValidityTab = createProposalTab("Berry Validity", "berry_validity", activeOnlyToggle.selectedProperty());
        Tab berryConversionTab = createProposalTab("Berry Conversion", "berry_conversion", activeOnlyToggle.selectedProperty());
        Tab needThresholdTab = createProposalTab("Need Thresholds", "need_threshold", activeOnlyToggle.selectedProperty());

        proposalTabs.getTabs().addAll(allTab, levelTab, berryEarningTab, berryValidityTab, berryConversionTab, needThresholdTab);

        // Apply additional dark styling to tabs
        proposalTabs.getStylesheets().add("data:text/css," +
            ".tab-pane .tab-header-area .tab-header-background { " +
                "-fx-background-color: #1e1e1e; " +
            "} " +
            ".tab-pane .tab { " +
                "-fx-background-color: #1e1e1e; " +
                "-fx-text-fill: #b2b2b2; " +
                "-fx-border-color: #404040; " +
            "} " +
            ".tab-pane .tab:selected { " +
                "-fx-background-color: #404040; " +
                "-fx-text-fill: #ffffff; " +
            "} " +
            ".tab-pane .tab:hover { " +
                "-fx-background-color: #333333; " +
            "}");

        // Add change listener to refresh content when filter changes
        activeOnlyToggle.selectedProperty().addListener((obs, oldVal, newVal) -> refreshProposalTabs(proposalTabs, newVal));

        content.getChildren().addAll(title, subtitle, actionBar, proposalTabs);
        setMainContent(content, "Proposals");
    }

    private static HBox createProposalStatsBar() {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setPadding(new Insets(15));
        statsBox.setStyle("-fx-background-color: #3e3e3e; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Calculate statistics
        int totalProposals = 0;
        int activeProposals = 0;
        int userVotes = 0;

        try {
            // Count all proposals
            totalProposals += levelProposals.size();
            totalProposals += berryEarningProposals.size();
            totalProposals += berryValidityProposals.size();
            totalProposals += berryConversionProposals.size();
            totalProposals += needThresholdProposals.size();

            // Count active proposals
            activeProposals = DatabaseManager.getActiveProposals().size();

            // Count user's votes (simplified - in real implementation would query user's vote history)
            userVotes = (int) (totalProposals * 0.3); // Placeholder calculation

        } catch (Exception e) {
            System.err.println("Error calculating proposal statistics: " + e.getMessage());
        }

        VBox totalBox = createStatCard("Total Proposals", String.valueOf(totalProposals), "#4CAF50");
        VBox activeBox = createStatCard("Active Proposals", String.valueOf(activeProposals), "#2196F3");
        VBox votesBox = createStatCard("Your Votes", String.valueOf(userVotes), "#FF9800");

        statsBox.getChildren().addAll(totalBox, activeBox, votesBox);
        return statsBox;
    }

    private static VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: " + color + "33; -fx-border-color: " + color + "; -fx-border-width: 1; -fx-border-radius: 5; -fx-background-radius: 5;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 12px;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 18px; -fx-font-weight: bold;");

        card.getChildren().addAll(valueLabel, titleLabel);
        return card;
    }

    private static Tab createProposalTab(String tabName, String category, BooleanProperty activeOnly) {
        Tab tab = new Tab(tabName);
        tab.setStyle("-fx-background-color: #1e1e1e; " +
                     "-fx-text-base-color: #b2b2b2; " +
                     "-fx-text-fill: #b2b2b2; " +
                     "-fx-focus-color: transparent; " +
                     "-fx-faint-focus-color: transparent;");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #2e2e2e; -fx-background-color: #2e2e2e;");
        
        VBox proposalList = new VBox(15);
        proposalList.setPadding(new Insets(20));
        
        // Load proposals based on category
        loadProposalsForCategory(proposalList, category, activeOnly.get());
        
        scrollPane.setContent(proposalList);
        tab.setContent(scrollPane);
        
        return tab;
    }

    private static void loadProposalsForCategory(VBox proposalList, String category, boolean activeOnly) {
        proposalList.getChildren().clear();
        
        try {
            switch (category) {
                case "all":
                    loadAllProposalsIntoList(proposalList, activeOnly);
                    break;
                case "level":
                    loadLevelProposalsIntoList(proposalList, activeOnly);
                    break;
                case "berry_earning":
                    loadBerryEarningProposalsIntoList(proposalList, activeOnly);
                    break;
                case "berry_validity":
                    loadBerryValidityProposalsIntoList(proposalList, activeOnly);
                    break;
                case "berry_conversion":
                    loadBerryConversionProposalsIntoList(proposalList, activeOnly);
                    break;
                case "need_threshold":
                    loadNeedThresholdProposalsIntoList(proposalList, activeOnly);
                    break;
            }
            
            if (proposalList.getChildren().isEmpty()) {
                Label emptyLabel = new Label("No proposals found in this category");
                emptyLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 14px; -fx-font-style: italic;");
                proposalList.getChildren().add(emptyLabel);
            }
            
        } catch (Exception e) {
            Label errorLabel = new Label("Error loading proposals: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #f44336; -fx-font-size: 14px;");
            proposalList.getChildren().add(errorLabel);
        }
    }

    private static void loadAllProposalsIntoList(VBox proposalList, boolean activeOnly) {
        // Load all proposal types
        loadLevelProposalsIntoList(proposalList, activeOnly);
        loadBerryEarningProposalsIntoList(proposalList, activeOnly);
        loadBerryValidityProposalsIntoList(proposalList, activeOnly);
        loadBerryConversionProposalsIntoList(proposalList, activeOnly);
        loadNeedThresholdProposalsIntoList(proposalList, activeOnly);
    }

    private static void loadLevelProposalsIntoList(VBox proposalList, boolean activeOnly) {
        for (LevelProposal proposal : levelProposals.values()) {
            if (!activeOnly || proposal.getVotes() > 0) {
                proposalList.getChildren().add(createLevelProposalCard(proposal));
            }
        }
    }

    private static void loadBerryEarningProposalsIntoList(VBox proposalList, boolean activeOnly) {
        for (BerryEarningProposal proposal : berryEarningProposals.values()) {
            if (!activeOnly || proposal.getVotes() > 0) {
                proposalList.getChildren().add(createBerryEarningProposalCard(proposal));
            }
        }
    }

    private static void loadBerryValidityProposalsIntoList(VBox proposalList, boolean activeOnly) {
        for (BerryValidityProposal proposal : berryValidityProposals.values()) {
            if (!activeOnly || proposal.getVotes() > 0) {
                proposalList.getChildren().add(createBerryValidityProposalCard(proposal));
            }
        }
    }

    private static void loadBerryConversionProposalsIntoList(VBox proposalList, boolean activeOnly) {
        for (BerryConversionProposal proposal : berryConversionProposals.values()) {
            if (!activeOnly || proposal.getVotes() > 0) {
                proposalList.getChildren().add(createBerryConversionProposalCard(proposal));
            }
        }
    }

    private static void loadNeedThresholdProposalsIntoList(VBox proposalList, boolean activeOnly) {
        for (NeedThresholdProposal proposal : needThresholdProposals.values()) {
            if (!activeOnly || proposal.getVotes() > 0) {
                proposalList.getChildren().add(createNeedThresholdProposalCard(proposal));
            }
        }
    }

    private static VBox createLevelProposalCard(LevelProposal proposal) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #3e3e3e; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-color: #4CAF50; -fx-border-width: 1;");

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("🏆 Level System Proposal #" + proposal.getId());
        titleLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label statusLabel = new Label(proposal.getVotes() > 0 ? "ACTIVE" : "INACTIVE");
        statusLabel.setStyle("-fx-text-fill: " + (proposal.getVotes() > 0 ? "#4CAF50" : "#f44336") + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        headerBox.getChildren().addAll(titleLabel, spacer, statusLabel);

        Label descLabel = new Label("XP Increase: " + proposal.getXpIncreasePercentage() + "% | XP Threshold: " + proposal.getXpThreshold());
        descLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px;");

        Label authorLabel = new Label("Proposed by: " + proposal.getProposer());
        authorLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 12px;");

        HBox voteBox = createVoteBox(proposal.getVotes(), 0, proposal.getId(), "level");

        card.getChildren().addAll(headerBox, descLabel, authorLabel, voteBox);
        return card;
    }

    private static VBox createBerryEarningProposalCard(BerryEarningProposal proposal) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #3e3e3e; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-color: #FF9800; -fx-border-width: 1;");

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("🍓 Berry Earning Proposal #" + proposal.getId());
        titleLabel.setStyle("-fx-text-fill: #FF9800; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label statusLabel = new Label(proposal.getVotes() > 0 ? "ACTIVE" : "INACTIVE");
        statusLabel.setStyle("-fx-text-fill: " + (proposal.getVotes() > 0 ? "#4CAF50" : "#f44336") + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        headerBox.getChildren().addAll(titleLabel, spacer, statusLabel);

        Label descLabel = new Label("Initial Berries: " + proposal.getInitialLevelOneBerryEarning());
        descLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px;");

        Label authorLabel = new Label("Proposed by: " + proposal.getProposer());
        authorLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 12px;");

        HBox voteBox = createVoteBox(proposal.getVotes(), 0, proposal.getId(), "berry_earning");

        card.getChildren().addAll(headerBox, descLabel, authorLabel, voteBox);
        return card;
    }

    private static VBox createBerryValidityProposalCard(BerryValidityProposal proposal) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #3e3e3e; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-color: #9C27B0; -fx-border-width: 1;");

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("⏰ Berry Validity Proposal #" + proposal.getId());
        titleLabel.setStyle("-fx-text-fill: #9C27B0; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label statusLabel = new Label(proposal.getVotes() > 0 ? "ACTIVE" : "INACTIVE");
        statusLabel.setStyle("-fx-text-fill: " + (proposal.getVotes() > 0 ? "#4CAF50" : "#f44336") + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        headerBox.getChildren().addAll(titleLabel, spacer, statusLabel);

        Label descLabel = new Label("Validity Period: " + proposal.getValidityMonths() + " months");
        descLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px;");

        Label authorLabel = new Label("Proposed by: " + proposal.getProposer());
        authorLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 12px;");

        HBox voteBox = createVoteBox(proposal.getVotes(), 0, proposal.getId(), "berry_validity");

        card.getChildren().addAll(headerBox, descLabel, authorLabel, voteBox);
        return card;
    }

    private static VBox createBerryConversionProposalCard(BerryConversionProposal proposal) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #3e3e3e; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-color: #00BCD4; -fx-border-width: 1;");

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("🔄 Berry Conversion Proposal #" + proposal.getId());
        titleLabel.setStyle("-fx-text-fill: #00BCD4; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label statusLabel = new Label(proposal.getVotes() > 0 ? "ACTIVE" : "INACTIVE");
        statusLabel.setStyle("-fx-text-fill: " + (proposal.getVotes() > 0 ? "#4CAF50" : "#f44336") + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        headerBox.getChildren().addAll(titleLabel, spacer, statusLabel);

        Label descLabel = new Label("Conversion Rate: " + proposal.getConversionPercentage() + "%");
        descLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px;");

        Label authorLabel = new Label("Proposed by: " + proposal.getProposer());
        authorLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 12px;");

        HBox voteBox = createVoteBox(proposal.getVotes(), 0, proposal.getId(), "berry_conversion");

        card.getChildren().addAll(headerBox, descLabel, authorLabel, voteBox);
        return card;
    }

    private static VBox createNeedThresholdProposalCard(NeedThresholdProposal proposal) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #3e3e3e; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-color: #F44336; -fx-border-width: 1;");

        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("📊 Need Threshold Proposal #" + proposal.getId());
        titleLabel.setStyle("-fx-text-fill: #F44336; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label statusLabel = new Label(proposal.getVotes() > 0 ? "ACTIVE" : "INACTIVE");
        statusLabel.setStyle("-fx-text-fill: " + (proposal.getVotes() > 0 ? "#4CAF50" : "#f44336") + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        headerBox.getChildren().addAll(titleLabel, spacer, statusLabel);

        Label descLabel = new Label("Global: " + proposal.getGlobalThresholdPercent() + "% | Personal: " + proposal.getPersonalThresholdPercent() + "%");
        descLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px;");

        Label authorLabel = new Label("Proposed by: " + proposal.getProposer());
        authorLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 12px;");

        HBox voteBox = createVoteBox(proposal.getVotes(), 0, proposal.getId(), "need_threshold");

        card.getChildren().addAll(headerBox, descLabel, authorLabel, voteBox);
        return card;
    }

    private static HBox createVoteBox(int votesFor, int votesAgainst, int proposalId, String proposalType) {
        HBox voteBox = new HBox(10);
        voteBox.setAlignment(Pos.CENTER_LEFT);

        Label votesLabel = new Label("👍 " + votesFor + " votes");
        votesLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 12px;");

        Button voteForBtn = new Button("Vote For");
        voteForBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 11px;");
        voteForBtn.setOnAction(event -> handleVote(proposalId, proposalType, true));

        voteBox.getChildren().addAll(votesLabel, voteForBtn);
        return voteBox;
    }

    private static void handleVote(int proposalId, String proposalType, boolean isFor) {
        try {
            // Use the actual voting system through ProposalService
            ProposalService.voteOnProposal(proposalId, SessionManager.getCurrentUsername(), isFor);
            
            // Refresh the current view
            refreshProposalContent();
            
        } catch (Exception e) {
            System.err.println("Error recording vote: " + e.getMessage());
            DialogFactory.showError("Failed to record your vote: " + e.getMessage());
        }
    }

    private static void showCreateProposalDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Create New Proposal");
        dialog.setHeaderText("Select the type of proposal to create:");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #2e2e2e;");
        dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: #ffffff;");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Button levelBtn = new Button("🏆 Level System Proposal");
        levelBtn.setStyle(UIStyleManager.BUTTON_STYLE);
        levelBtn.setOnAction(e -> {
            dialog.setResult("level");
            dialog.close();
        });

        Button berryEarningBtn = new Button("🍓 Berry Earning Proposal");
        berryEarningBtn.setStyle(UIStyleManager.BUTTON_STYLE);
        berryEarningBtn.setOnAction(e -> {
            dialog.setResult("berry_earning");
            dialog.close();
        });

        Button berryValidityBtn = new Button("⏰ Berry Validity Proposal");
        berryValidityBtn.setStyle(UIStyleManager.BUTTON_STYLE);
        berryValidityBtn.setOnAction(e -> {
            dialog.setResult("berry_validity");
            dialog.close();
        });

        Button berryConversionBtn = new Button("🔄 Berry Conversion Proposal");
        berryConversionBtn.setStyle(UIStyleManager.BUTTON_STYLE);
        berryConversionBtn.setOnAction(e -> {
            dialog.setResult("berry_conversion");
            dialog.close();
        });

        Button needThresholdBtn = new Button("📊 Need Threshold Proposal");
        needThresholdBtn.setStyle(UIStyleManager.BUTTON_STYLE);
        needThresholdBtn.setOnAction(e -> {
            dialog.setResult("need_threshold");
            dialog.close();
        });

        content.getChildren().addAll(levelBtn, berryEarningBtn, berryValidityBtn, berryConversionBtn, needThresholdBtn);
        dialogPane.setContent(content);

        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(cancelButtonType);

        dialog.showAndWait().ifPresent(proposalType -> {
            if (!proposalType.equals("Cancel")) {
                showCreateSpecificProposalDialog(proposalType);
            }
        });
    }

    private static void showCreateSpecificProposalDialog(String proposalType) {
        // This would open specific dialog for each proposal type
        // For now, show a placeholder
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Create " + proposalType + " Proposal");
        alert.setHeaderText(null);
        alert.setContentText("Create " + proposalType + " proposal dialog would open here.\n\nThis would include fields specific to " + proposalType + " proposals.");
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #2e2e2e;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #ffffff;");
        
        alert.showAndWait();
    }

    private static void refreshProposalContent() {
        // Refresh the current proposals view
        handleShowProposals();
    }

    private static void refreshProposalTabs(TabPane tabPane, boolean activeOnly) {
        for (Tab tab : tabPane.getTabs()) {
            ScrollPane scrollPane = (ScrollPane) tab.getContent();
            VBox proposalList = (VBox) scrollPane.getContent();
            
            String category = switch (tab.getText()) {
                case "All Proposals" -> "all";
                case "Level System" -> "level";
                case "Berry Earning" -> "berry_earning";
                case "Berry Validity" -> "berry_validity";
                case "Berry Conversion" -> "berry_conversion";
                case "Need Thresholds" -> "need_threshold";
                default -> "all";
            };
            
            loadProposalsForCategory(proposalList, category, activeOnly);
        }
    }

    private static void handleShowJobs() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #2e2e2e;");

        Label titleLabel = new Label("💼 Job Opportunities");
        titleLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 28px; -fx-font-weight: bold;");
        content.getChildren().add(titleLabel);

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            Label loginLabel = new Label("Please log in to see job opportunities.");
            loginLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 16px;");
            content.getChildren().add(loginLabel);
            setMainContent(content, "Jobs");
            return;
        }

        try {
            // Corrected: Use getCertifiedExpertiseIds and getExpertiseIdsForUser
            Set<Integer> userExpertiseIds = currentUser.getCertifiedExpertiseIds();
            if (userExpertiseIds == null || userExpertiseIds.isEmpty()) { 
                userExpertiseIds = DatabaseManager.getExpertiseIdsForUser(currentUser.getUsername());
                currentUser.setCertifiedExpertiseIds(userExpertiseIds); 
            }
            
            Map<Integer, Branch> branches = DatabaseManager.loadAllBranches();
            List<VBox> jobCards = new ArrayList<>();

            for (Branch branch : branches.values()) {
                // Corrected: Use newly added getAllTeamOpeningsForBranch
                List<ExpertiseRequirement> teamOpenings = DatabaseManager.getAllTeamOpeningsForBranch(branch.getId());
                if (teamOpenings != null) {
                    for (ExpertiseRequirement opening : teamOpenings) {
                        // Corrected: Check against userExpertiseIds (Set<Integer>)
                        boolean matchesExpertise = userExpertiseIds.contains(opening.getExpertiseId());

                        if (matchesExpertise) {
                            jobCards.add(createJobCard(branch, opening));
                        }
                    }
                }
            }

            if (jobCards.isEmpty()) {
                Label noJobsLabel = new Label("No job openings match your fields of expertise at the moment.");
                noJobsLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 16px; -fx-font-style: italic;");
                noJobsLabel.setAlignment(Pos.CENTER);
                content.getChildren().add(noJobsLabel);
            } else {
                ScrollPane scrollPane = new ScrollPane();
                VBox jobListVBox = new VBox(15);
                jobListVBox.setPadding(new Insets(10));
                jobListVBox.getChildren().addAll(jobCards);
                scrollPane.setContent(jobListVBox);
                scrollPane.setFitToWidth(true);
                scrollPane.setStyle("-fx-background: #2e2e2e; -fx-background-color: #2e2e2e;");
                content.getChildren().add(scrollPane);
            }

        } catch (SQLException e) {
            DialogFactory.showError("Error loading job opportunities: " + e.getMessage());
            Label errorLabel = new Label("Could not load job opportunities. Please try again later.");
            errorLabel.setStyle("-fx-text-fill: #f44336; -fx-font-size: 16px;");
            content.getChildren().add(errorLabel);
        }

        setMainContent(content, "Jobs");
    }

    private static VBox createJobCard(Branch branch, ExpertiseRequirement opening) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #3e3e3e; -fx-border-radius: 8; -fx-background-radius: 8; -fx-border-color: #0077aa; -fx-border-width: 1;");

        // Corrected: Use getExpertiseId()
        Label jobTitleLabel = new Label("Role: " + fieldsOfExpertise.get(opening.getExpertiseId()).getName());
        jobTitleLabel.setStyle("-fx-text-fill: #00AACC; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label branchLabel = new Label("Project Branch: " + branch.getName() + " (ID: " + branch.getId() + ")");
        branchLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px;");

        // Corrected: Use newly added getDescription()
        Label descriptionLabel = new Label("Description: " + (opening.getDescription() != null ? opening.getDescription() : "No description provided."));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 12px;");
        
        // Corrected: Use getCount()
        Label openingsLabel = new Label("Open Positions: " + opening.getCount());
        openingsLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 12px;");

        Button applyButton = new Button("View Details / Apply");
        // Corrected: Use BUTTON_STYLE as BUTTON_STYLE_SUCCESS is not defined
        applyButton.setStyle(UIStyleManager.BUTTON_STYLE);
        applyButton.setOnAction(e -> {
            // Placeholder for apply/view details action
            DialogFactory.showInfo("Application Info", "Viewing details for " + fieldsOfExpertise.get(opening.getExpertiseId()).getName() + " at " + branch.getName() + ".\\\\nFurther implementation needed for application process.");
        });

        card.getChildren().addAll(jobTitleLabel, branchLabel, descriptionLabel, openingsLabel, applyButton);
        return card;
    }

    private static void handleShowNotifications() {
        try {
            User currentUser = SessionManager.getCurrentUser();
            VBox content = new VBox(20);
            content.setPadding(new Insets(30));
            content.setStyle("-fx-background-color: #2e2e2e;");

            // Enhanced Header Section
            VBox header = new VBox(15);
            header.setAlignment(Pos.CENTER_LEFT);
            
            Label titleLabel = new Label("🔔 Notifications Center");
            titleLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 28px; -fx-font-weight: bold;");
            
            Label subtitle = new Label("Stay updated with the latest activities and opportunities");
            subtitle.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 14px; -fx-font-style: italic;");

            // Load notifications from the database
            List<Notification> notifications = DatabaseManager.getUserNotifications(currentUser.getUsername());
            
            // Stats bar
            HBox statsBar = new HBox(30);
            statsBar.setAlignment(Pos.CENTER_LEFT);
            statsBar.setPadding(new Insets(15));
            statsBar.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 10px;");
            
            int totalNotifications = notifications != null ? notifications.size() : 0;
            long unreadCount = notifications != null ? notifications.stream().filter(n -> !n.isRead()).count() : 0;
            long readCount = totalNotifications - unreadCount;
            
            Label totalLabel = new Label("📊 Total: " + totalNotifications);
            totalLabel.setStyle("-fx-text-fill: #76ff76; -fx-font-size: 14px; -fx-font-weight: bold;");
            
            Label unreadLabel = new Label("🔴 Unread: " + unreadCount);
            unreadLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 14px; -fx-font-weight: bold;");
            
            Label readLabel = new Label("✅ Read: " + readCount);
            readLabel.setStyle("-fx-text-fill: #87ceeb; -fx-font-size: 14px; -fx-font-weight: bold;");
            
            statsBar.getChildren().addAll(totalLabel, unreadLabel, readLabel);
            header.getChildren().addAll(titleLabel, subtitle, statsBar);
            content.getChildren().add(header);

            // Action buttons
            HBox actionBar = new HBox(15);
            actionBar.setAlignment(Pos.CENTER_LEFT);
            actionBar.setPadding(new Insets(10, 0, 20, 0));
            
            Button refreshButton = createStyledButton("🔄 Refresh", "#4CAF50");
            refreshButton.setOnAction(_ -> handleShowNotifications());
            
            Button markAllReadButton = createStyledButton("✓ Mark All Read", "#2196F3");
            markAllReadButton.setOnAction(_ -> {
                try {
                    DatabaseManager.markAllNotificationsAsRead(currentUser.getUsername());
                    DialogFactory.showInfo("Success", "All notifications marked as read!");
                    handleShowNotifications(); // Refresh the view
                } catch (SQLException ex) {
                    DialogFactory.showError("Error marking notifications as read: " + ex.getMessage());
                }
            });
            
            actionBar.getChildren().addAll(refreshButton, markAllReadButton);
            content.getChildren().add(actionBar);
            
            if (notifications == null || notifications.isEmpty()) {
                // Enhanced empty state
                VBox emptyState = new VBox(20);
                emptyState.setAlignment(Pos.CENTER);
                emptyState.setPadding(new Insets(40));
                emptyState.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 12px; -fx-border-color: #555; -fx-border-radius: 12px;");
                
                Label emptyIcon = new Label("📭");
                emptyIcon.setStyle("-fx-font-size: 48px;");
                
                Label emptyTitle = new Label("No Notifications Yet");
                emptyTitle.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 20px; -fx-font-weight: bold;");
                
                Label emptyDescription = new Label("You're all caught up! New notifications will appear here when they arrive.");
                emptyDescription.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 14px; -fx-text-alignment: center;");
                emptyDescription.setWrapText(true);
                emptyDescription.setMaxWidth(400);
                
                emptyState.getChildren().addAll(emptyIcon, emptyTitle, emptyDescription);
                content.getChildren().add(emptyState);
            } else {
                // Notifications list with enhanced cards
                VBox notificationsList = new VBox(12);
                notificationsList.setAlignment(Pos.TOP_CENTER);
                
                // Sort notifications by timestamp (newest first)
                notifications.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));
                
                for (Notification notification : notifications) {
                    VBox notificationCard = createNotificationCard(notification, currentUser);
                    notificationsList.getChildren().add(notificationCard);
                }
                
                // Wrap in scroll pane for better handling of many notifications
                ScrollPane scrollPane = new ScrollPane(notificationsList);
                scrollPane.setFitToWidth(true);
                scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                scrollPane.setStyle("-fx-background: #2e2e2e; -fx-background-color: #2e2e2e;");
                scrollPane.setPrefHeight(500);
                
                content.getChildren().add(scrollPane);
            }

            setMainContent(content, "Notifications");
        } catch (SQLException e) {
            DialogFactory.showError("Error loading notifications: " + e.getMessage());
        }
    }

    // Helper method to create enhanced notification cards
    private static VBox createNotificationCard(Notification notification, User currentUser) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setMaxWidth(800);
        card.setPrefWidth(800);
        
        // Determine card styling based on read status
        String cardStyle = notification.isRead() 
            ? "-fx-background-color: #3e3e3e; -fx-background-radius: 12px; -fx-border-color: #555; -fx-border-radius: 12px; -fx-border-width: 1px;"
            : "-fx-background-color: #4a4a4a; -fx-background-radius: 12px; -fx-border-color: #76ff76; -fx-border-radius: 12px; -fx-border-width: 2px; -fx-effect: dropshadow(gaussian, rgba(118,255,118,0.3), 8, 0, 0, 0);";
        
        card.setStyle(cardStyle);
        
        // Header with status indicator and date
        HBox headerRow = new HBox(15);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        // Status indicator
        Label statusIndicator = new Label(notification.isRead() ? "✅" : "🔴");
        statusIndicator.setStyle("-fx-font-size: 16px;");
        
        // Notification type icon (based on content)
        String typeIcon = getNotificationTypeIcon(notification.getMessage());
        Label typeLabel = new Label(typeIcon);
        typeLabel.setStyle("-fx-font-size: 18px;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Formatted date
        String formattedDate = formatNotificationDate(notification.getTimestamp());
        Label dateLabel = new Label(formattedDate);
        dateLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        headerRow.getChildren().addAll(statusIndicator, typeLabel, spacer, dateLabel);
        
        // Message content
        Label messageLabel = new Label(notification.getMessage());
        messageLabel.setStyle(notification.isRead() 
            ? "-fx-text-fill: #d0d0d0; -fx-font-size: 15px; -fx-line-spacing: 2px;"
            : "-fx-text-fill: #ffffff; -fx-font-size: 15px; -fx-font-weight: bold; -fx-line-spacing: 2px;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(750);
        
        // Action buttons row
        HBox actionsRow = new HBox(10);
        actionsRow.setAlignment(Pos.CENTER_LEFT);
        
        // Toggle read status button
        Button toggleReadButton = new Button(notification.isRead() ? "Mark Unread" : "Mark Read");
        toggleReadButton.setStyle("-fx-background-color: #666; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 15px;");
        toggleReadButton.setOnMouseEntered(_ -> toggleReadButton.setStyle("-fx-background-color: #777; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 15px;"));
        toggleReadButton.setOnMouseExited(_ -> toggleReadButton.setStyle("-fx-background-color: #666; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 15px;"));
        toggleReadButton.setOnAction(_ -> {
            try {
                if (notification.isRead()) {
                    // Mark as unread (would need a new database method)
                    DialogFactory.showInfo("Info", "Mark as unread functionality would be implemented here");
                } else {
                    DatabaseManager.markNotificationAsRead(notification.getId());
                    DialogFactory.showInfo("Success", "Notification marked as read!");
                    handleShowNotifications(); // Refresh the view
                }
            } catch (SQLException ex) {
                DialogFactory.showError("Error updating notification: " + ex.getMessage());
            }
        });
        
        actionsRow.getChildren().add(toggleReadButton);
        
        // Action URL button (if applicable)
        if (notification.getActionUrl() != null && !notification.getActionUrl().isEmpty()) {
            Button actionButton = new Button("🔗 View Details");
            actionButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 5 12; -fx-background-radius: 15px;");
            actionButton.setOnMouseEntered(_ -> actionButton.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 5 12; -fx-background-radius: 15px;"));
            actionButton.setOnMouseExited(_ -> actionButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 5 12; -fx-background-radius: 15px;"));
            actionButton.setOnAction(_ -> {
                try {
                    // Mark as read when clicking action
                    if (!notification.isRead()) {
                        DatabaseManager.markNotificationAsRead(notification.getId());
                    }
                    getAppHostServices().showDocument(notification.getActionUrl());
                } catch (SQLException ex) {
                    DialogFactory.showError("Error marking notification as read: " + ex.getMessage());
                }
            });
            actionsRow.getChildren().add(actionButton);
        }
        
        card.getChildren().addAll(headerRow, messageLabel, actionsRow);
        
        // Add hover effect
        card.setOnMouseEntered(_ -> {
            String hoverStyle = notification.isRead() 
                ? "-fx-background-color: #4a4a4a; -fx-background-radius: 12px; -fx-border-color: #777; -fx-border-radius: 12px; -fx-border-width: 1px;"
                : "-fx-background-color: #555; -fx-background-radius: 12px; -fx-border-color: #76ff76; -fx-border-radius: 12px; -fx-border-width: 2px; -fx-effect: dropshadow(gaussian, rgba(118,255,118,0.4), 10, 0, 0, 0);";
            card.setStyle(hoverStyle);
        });
        
        card.setOnMouseExited(_ -> card.setStyle(cardStyle));
        
        return card;
    }
    
    // Helper method to get notification type icon based on content
    private static String getNotificationTypeIcon(String message) {
        String lowercaseMessage = message.toLowerCase();
        if (lowercaseMessage.contains("job") || lowercaseMessage.contains("position") || lowercaseMessage.contains("opportunity")) {
            return "💼";
        } else if (lowercaseMessage.contains("berry") || lowercaseMessage.contains("berries")) {
            return "🫐";
        } else if (lowercaseMessage.contains("proposal") || lowercaseMessage.contains("vote")) {
            return "🗳️";
        } else if (lowercaseMessage.contains("level") || lowercaseMessage.contains("promoted")) {
            return "⭐";
        } else if (lowercaseMessage.contains("need") || lowercaseMessage.contains("requirement")) {
            return "❗";
        } else if (lowercaseMessage.contains("idea") || lowercaseMessage.contains("innovation")) {
            return "💡";
        } else if (lowercaseMessage.contains("branch") || lowercaseMessage.contains("department")) {
            return "🌿";
        } else {
            return "📢";
        }
    }
    
    // Helper method to format notification date in a user-friendly way
    private static String formatNotificationDate(java.sql.Timestamp timestamp) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime notificationTime = timestamp.toLocalDateTime();
        java.time.Duration duration = java.time.Duration.between(notificationTime, now);
        
        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();
        
        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " min ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        } else if (days < 7) {
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        } else {
            return notificationTime.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        }
    }

    private static void handleShowBerries() {
        try {
            // Create main content container
            VBox content = new VBox(20);
            content.setPadding(new Insets(30));
            content.setStyle("-fx-background-color: #2e2e2e;");

            // Load user berries from database
            List<Berry> berries = DatabaseManager.getUserBerries(SessionManager.getCurrentUser().getUsername());
            
            // Update in-memory map with database berries to ensure correct total calculation
            userBerries.put(SessionManager.getCurrentUser().getUsername(), berries);
            
            // Calculate statistics
            int totalBerries = BerryService.getUserTotalBerries(SessionManager.getCurrentUser().getUsername());
            long expiringBerries = berries.stream()
                .filter(berry -> !berry.isExpired() && berry.getExpirationDate() != null)
                .filter(berry -> java.time.Duration.between(java.time.LocalDateTime.now(), berry.getExpirationDate()).toDays() <= 7)
                .mapToInt(Berry::getAmount)
                .sum();
            int projectedMonthlyEarning = SystemParameters.calculateMonthlyBerryEarning(SessionManager.getCurrentUser().getLevel());

            // Create header section
            VBox headerSection = new VBox(10);
            headerSection.setPadding(new Insets(25));
            headerSection.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 10px; -fx-padding: 18px; -fx-border-color: #555; -fx-border-radius: 10px;");
            
            Label titleLabel = new Label("🫐 Berries Management");
            titleLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 24px; -fx-font-weight: bold;");
            
            Label subtitleLabel = new Label("Manage your berry collection and track earnings");
            subtitleLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 14px; -fx-font-style: italic.");

            // Stats bar
            HBox statsBar = new HBox(30);
            statsBar.setAlignment(Pos.CENTER_LEFT);
            statsBar.setPadding(new Insets(15));
            statsBar.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 10px;");
            
            Label totalBerriesLabel = new Label(String.format("🫐 Total Berries: %d", totalBerries));
            totalBerriesLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #76ff76;");

            Label expiringLabel = new Label(String.format("⏰ Expiring Soon: %d", expiringBerries));
            expiringLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #ffd700;");

            Label projectedLabel = new Label(String.format("📈 Monthly Projected: %d", projectedMonthlyEarning));
            projectedLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #87ceeb;");

            statsBar.getChildren().addAll(totalBerriesLabel, expiringLabel, projectedLabel);

            headerSection.getChildren().addAll(titleLabel, subtitleLabel, statsBar);
            content.getChildren().add(headerSection);

            // Create ScrollPane for berries list
            ScrollPane scrollPane = new ScrollPane();
            VBox berriesList = new VBox(10);
            berriesList.setPadding(new Insets(10));

            if (berries.isEmpty()) {
                Label noBerries = new Label("🍃 No berries found. Start earning berries by participating in the Trust System!");
                noBerries.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 16px; -fx-font-style: italic;");
                noBerries.setAlignment(Pos.CENTER);
                berriesList.getChildren().add(noBerries);
            } else {
                for (Berry berry : berries) {
                    VBox berryCard = createBerryCard(berry);
                    berriesList.getChildren().add(berryCard);
                }
            }

            scrollPane.setContent(berriesList);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefHeight(400);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
            content.getChildren().add(scrollPane);

            // Action buttons
            HBox actionButtons = new HBox(15);
            actionButtons.setAlignment(Pos.CENTER);
            actionButtons.setPadding(new Insets(10, 0, 0, 0));

            Button usageReportButton = new Button("Usage Report");
            usageReportButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 10 15 10 15;");
            usageReportButton.setOnMouseEntered(e -> usageReportButton.setStyle("-fx-background-color: #1976D2; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 10 15 10 15;"));
            usageReportButton.setOnMouseExited(e -> usageReportButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 10 15 10 15;"));
            usageReportButton.setOnAction(_ -> handleBerryUsageReport());

            Button refreshButton = new Button("Refresh");
            refreshButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 10 15 10 15;");
            refreshButton.setOnMouseEntered(e -> refreshButton.setStyle("-fx-background-color: #F57C00; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 10 15 10 15;"));
            refreshButton.setOnMouseExited(e -> refreshButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 10 15 10 15;"));
            refreshButton.setOnAction(_ -> handleShowBerries());

            Button monthlyDistributionButton = new Button("🗓️ Process Monthly Distribution");
            monthlyDistributionButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 10 15 10 15;");
            monthlyDistributionButton.setOnMouseEntered(e -> monthlyDistributionButton.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 10 15 10 15;"));
            monthlyDistributionButton.setOnMouseExited(e -> monthlyDistributionButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 10 15 10 15;"));
            monthlyDistributionButton.setOnAction(_ -> handleMonthlyBerryDistribution());

            actionButtons.getChildren().addAll(usageReportButton, refreshButton, monthlyDistributionButton);
            content.getChildren().add(actionButtons);

            // Set the content in the main layout
            setMainContent(content, "Berries Management");

        } catch (SQLException e) {
            System.err.println("Error loading berries: " + e.getMessage());
            DialogFactory.showError("Failed to load berries: " + e.getMessage());
        }
    }
    
    // Create a styled card for displaying berry information
    private static VBox createBerryCard(Berry berry) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        
        // Set style based on expiration status
        LocalDateTime now = LocalDateTime.now();
        String cardStyle;
        if (berry.getExpirationDate().isBefore(now)) {
            // Expired berry - red tint
            cardStyle = "-fx-background-color: #4a2a2a; -fx-background-radius: 8px; -fx-border-color: #ff6b6b; -fx-border-radius: 8px; -fx-border-width: 2px;";
        } else if (java.time.Duration.between(now, berry.getExpirationDate()).toDays() <= 7) {
            // Expiring soon - yellow tint
            cardStyle = "-fx-background-color: #4a2a2a; -fx-background-radius: 8px; -fx-border-color: #ffd700; -fx-border-radius: 8px; -fx-border-width: 2px;";
        } else {
            // Active berry - green tint
            cardStyle = "-fx-background-color: #2a4a2a; -fx-background-radius: 8px; -fx-border-color: #76ff76; -fx-border-radius: 8px; -fx-border-width: 2px;";
        }
        card.setStyle(cardStyle);
        
        // Header with amount and status
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label amountLabel = new Label("🫐 " + berry.getAmount() + " berries");
        amountLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Status indicator
        String statusText;
        String statusColor;
        if (berry.getExpirationDate().isBefore(now)) {
            statusText = "EXPIRED";
            statusColor = "#ff6b6b";
        } else if (java.time.Duration.between(now, berry.getExpirationDate()).toDays() <= 7) {
            statusText = "EXPIRING SOON";
            statusColor = "#ffd700";
        } else {
            statusText = "ACTIVE";
            statusColor = "#76ff76";
        }
        
        Label statusLabel = new Label(statusText);
        statusLabel.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 12px; -fx-font-weight: bold; " +
                           "-fx-background-color: " + statusColor + "22; -fx-padding: 4 8; -fx-background-radius: 12px;");
        
        header.getChildren().addAll(amountLabel, spacer, statusLabel);
        
        // Source information
        Label sourceLabel = new Label("Source: " + formatBerrySource(berry.getSource()));
        sourceLabel.setStyle("-fx-text-fill: #d0d0d0; -fx-font-size: 13px;");
        
        // Expiration information
        Label expirationLabel = new Label("Expires: " + formatExpirationDate(berry));
        expirationLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 12px;");
        
        card.getChildren().addAll(header, sourceLabel, expirationLabel);
        
        // Add hover effect
        card.setOnMouseEntered(_ -> {
            String hoverStyle = cardStyle.replace("-fx-background-color: #", "-fx-background-color: #");
            if (cardStyle.contains("#4a2a2a")) {
                hoverStyle = cardStyle.replace("#4a2a2a", "#5a3a3a");
            } else if (cardStyle.contains("#4a4a2a")) {
                hoverStyle = cardStyle.replace("#4a4a2a", "#5a3a3a");
            } else {
                hoverStyle = cardStyle.replace("#2a4a2a", "#3a5a3a");
            }
            card.setStyle(hoverStyle);
        });
        
        card.setOnMouseExited(_ -> card.setStyle(cardStyle));
        
        return card;
    }
    
    // Helper method to format berry source for display
    private static String formatBerrySource(String source) {
        return switch (source) {
            case "monthly_distribution" -> "Monthly Distribution";
            case "level_up" -> "Level Up Reward";
            case "system_reward" -> "System Reward";
            default -> source.replace("_", " ").substring(0, 1).toUpperCase() + 
                      source.replace("_", " ").substring(1);
        };
    }
    
    // Helper method to format expiration date with appropriate messaging
    private static String formatExpirationDate(Berry berry) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = berry.getExpirationDate();
        
        if (expiration.isBefore(now)) {
            long daysAgo = java.time.Duration.between(expiration, now).toDays();
            return "Expired " + daysAgo + " days ago";
        } else {
            long daysLeft = java.time.Duration.between(now, expiration).toDays();
            if (daysLeft == 0) {
                return "Expires today";
            } else if (daysLeft == 1) {
                return "Expires tomorrow";
            } else if (daysLeft <= 7) {
                return "Expires in " + daysLeft + " days";
            } else {
                return expiration.toLocalDate().toString();
            }
        }
    }


    
    // Method to handle berry usage report generation
    private static void handleBerryUsageReport() {
        String username = SessionManager.getCurrentUser().getUsername();
        String report = BerryService.generateBerryUsageReport(username);
        
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Berry Usage Report");
        dialog.setHeaderText("Berry Usage Report for " + username);
        
        TextArea reportArea = new TextArea(report);
        reportArea.setEditable(false);
        reportArea.setPrefRowCount(15);
        reportArea.setPrefColumnCount(50);
        reportArea.setStyle("-fx-background-color: #3e3e3e; -fx-text-fill: white; -fx-border-color: #555; -fx-font-family: monospace;");
        
        dialog.getDialogPane().setContent(reportArea);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setStyle("-fx-background-color: #2e2e2e;");
        dialog.getDialogPane().setPrefWidth(600);
        dialog.getDialogPane().setPrefHeight(500);
        
        dialog.showAndWait();
    }
    
    // Method to handle monthly berry distribution for all users
    private static void handleMonthlyBerryDistribution() {
        try {
            // Show confirmation dialog first
            Dialog<ButtonType> confirmDialog = new Dialog<>();
            confirmDialog.setTitle("Monthly Berry Distribution");
            confirmDialog.setHeaderText("Process Monthly Berry Distribution");
            
            VBox content = new VBox(15);
            content.setPadding(new Insets(20));
            content.setStyle("-fx-background-color: #2e2e2e;");
            
            Label warningLabel = new Label("⚠️ This will distribute berries to ALL users based on their current level.");
            warningLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 14px; -fx-font-weight: bold;");
            
            Label infoLabel = new Label("Each user will receive berries equal to their level multiplied by the initial berry earning parameter.");
            infoLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 12px;");
            infoLabel.setWrapText(true);
            
            Label questionLabel = new Label("Are you sure you want to continue?");
            questionLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px; -fx-font-weight: bold;");
            
            content.getChildren().addAll(warningLabel, infoLabel, questionLabel);
            
            confirmDialog.getDialogPane().setContent(content);
            confirmDialog.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
            confirmDialog.getDialogPane().setStyle("-fx-background-color: #2e2e2e;");
            
            // Style the buttons
            Button yesButton = (Button) confirmDialog.getDialogPane().lookupButton(ButtonType.YES);
            Button noButton = (Button) confirmDialog.getDialogPane().lookupButton(ButtonType.NO);
            
            yesButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
            noButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
            
            Optional<ButtonType> result = confirmDialog.showAndWait();
            
            if (result.isPresent() && result.get() == ButtonType.YES) {
                // Process the monthly distribution
                BerryService.processMonthlyBerryEarnings();
                
                // Refresh the berries view to show updated information
                handleShowBerries();
                
                DialogFactory.showInfo("Distribution Processed", "Monthly berry distribution has been successfully processed.");
            }
            
        } catch (SQLException e) {
            System.err.println("Error processing monthly berry distribution: " + e.getMessage());
            DialogFactory.showError("Failed to process monthly berry distribution: " + e.getMessage());
        }
    }
    
    // Helper method to find the active proposal (highest votes)
    private static <T extends Proposal> T findActiveProposal(Map<Integer, T> proposalsMap) {
        if (proposalsMap == null || proposalsMap.isEmpty()) {
            return null;
        }

        T activeProposal = null;
        int maxVotes = -1;

        for (T proposal : proposalsMap.values()) {
            if (proposal.getVotes() > maxVotes) {
                maxVotes = proposal.getVotes();
                activeProposal = proposal;
            } else if (proposal.getVotes() == maxVotes) {
                if (activeProposal != null && proposal.getId() < activeProposal.getId()) {
                    activeProposal = proposal;
                } else if (activeProposal == null) {
                    activeProposal = proposal;
                }
            }
        }
        return activeProposal;
    }

    public static void updateSystemParametersFromActiveProposals() {
        LevelProposal activeLevelProposal = findActiveProposal(levelProposals);
        if (activeLevelProposal != null) {
            SystemParameters.setXpIncreasePercentage(activeLevelProposal.getXpIncreasePercentage());
            SystemParameters.setXpThreshold(activeLevelProposal.getXpThreshold());
            System.out.println("Updated Level Parameters from Proposal ID: " + activeLevelProposal.getId());
        }

        BerryEarningProposal activeBerryEarningProposal = findActiveProposal(berryEarningProposals);
        if (activeBerryEarningProposal != null) {
            SystemParameters.setInitialLevelOneBerryEarning(activeBerryEarningProposal.getInitialLevelOneBerryEarning());
            System.out.println("Updated Berry Earning Parameters from Proposal ID: " + activeBerryEarningProposal.getId());
        }

        BerryValidityProposal activeBerryValidityProposal = findActiveProposal(berryValidityProposals);
        if (activeBerryValidityProposal != null) {
            SystemParameters.setBerryValidityTime(activeBerryValidityProposal.getValidityMonths());
            System.out.println("Updated Berry Validity Parameters from Proposal ID: " + activeBerryValidityProposal.getId());
        }

        BerryConversionProposal activeBerryConversionProposal = findActiveProposal(berryConversionProposals);
        if (activeBerryConversionProposal != null) {
            SystemParameters.setConversionPercentage(activeBerryConversionProposal.getConversionPercentage());
            SystemParameters.setConversionPeriod(activeBerryConversionProposal.getConversionPeriod());
            System.out.println("Updated Berry Conversion Parameters from Proposal ID: " + activeBerryConversionProposal.getId());
        }

        NeedThresholdProposal activeNeedThresholdProposal = findActiveProposal(needThresholdProposals);
        if (activeNeedThresholdProposal != null) {
            SystemParameters.setGlobalNeedThresholdPercent(activeNeedThresholdProposal.getGlobalThresholdPercent());
            SystemParameters.setPersonalNeedThresholdPercent(activeNeedThresholdProposal.getPersonalThresholdPercent());
            SystemParameters.setNeedTimeLimit(activeNeedThresholdProposal.getTimeLimit());
            System.out.println("Updated Need Threshold Parameters from Proposal ID: " + activeNeedThresholdProposal.getId());
        }
    }

    // Helper method to create styled buttons for the Ideas UI
    private static Button createStyledButton(String text, String baseColor) {
        Button button = new Button(text);
        button.setPrefWidth(150);
        button.setPrefHeight(40);
        
        // Create styled appearance with the specified color
        String buttonStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 12px; " +
            "-fx-background-radius: 8px; " +
            "-fx-border-radius: 8px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 2, 0, 0, 1);",
            baseColor
        );
        
        button.setStyle(buttonStyle);
        
        // Add hover effects
        String hoverColor = lightenColor(baseColor);
        String hoverStyle = String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 12px; " +
            "-fx-background-radius: 8px; " +
            "-fx-border-radius: 8px; " +
            "-fx-cursor: hand; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 3, 0, 0, 2);",
            hoverColor
        );
        
        button.setOnMouseEntered(_ -> button.setStyle(hoverStyle));
        button.setOnMouseExited(_ -> button.setStyle(buttonStyle));
        
        return button;
    }
    
    // Helper method to lighten a color for hover effects
    private static String lightenColor(String hexColor) {
        // Simple color lightening - increase each RGB component by  20%
        if (hexColor.startsWith("#")) {
            try {
                int r = Integer.parseInt(hexColor.substring(1, 3), 16);
                int g = Integer.parseInt(hexColor.substring(3, 5), 16);
                int b = Integer.parseInt(hexColor.substring(5, 7), 16);
                
                r = Math.min(255, (int)(r * 1.2));
                g = Math.min(255, (int)(g * 1.2));
                b = Math.min(255, (int)(b * 1.2));
                
                return String.format("#%02x%02x%02x", r, g, b);
            } catch (NumberFormatException e) {
                return hexColor; // Return original if parsing fails
            }
        }
        return hexColor;
    }
    
    // Helper method to create monthly berry revenue chart
    private static LineChart<Number, Number> createMonthlyBerryRevenueChart(String username) {
        // Create axes
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Month");
        yAxis.setLabel("Berry Revenue");
        
        // Create the line chart
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("📈 Monthly Berry Revenue");
        lineChart.setPrefHeight(300);
        lineChart.setPrefWidth(400);
        
        // Style the chart for dark theme
        lineChart.setStyle("-fx-background-color: #3e3e3e; -fx-border-color: #555; -fx-border-radius: 8px;");
        lineChart.lookup(".chart-plot-background").setStyle("-fx-background-color: #2e2e2e;");
        lineChart.setLegendVisible(false);
        
        // Create data series
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Berry Revenue");
        
        try {
            // Load user berries from database
            List<Berry> berries = DatabaseManager.getUserBerries(username);
            
            // Group berries by month and calculate revenue
            Map<YearMonth, Integer> monthlyRevenue = new LinkedHashMap<>();
            YearMonth currentMonth = YearMonth.now();
            
            // Initialize last 6 months with 0 revenue
            for (int i = 5; i >= 0; i--) {
                YearMonth month = currentMonth.minusMonths(i);
                monthlyRevenue.put(month, 0);
            }
            
            // Calculate actual revenue by month
            for (Berry berry : berries) {
                if (berry.getExpirationDate() != null) {
                    // Use creation date approximation (expiration - validity period)
                    LocalDateTime creationDate = berry.getExpirationDate().minusMonths(SystemParameters.getBerryValidityTime());
                    YearMonth berryMonth = YearMonth.from(creationDate);
                    
                    // Only include berries from the last 6 months
                    if (monthlyRevenue.containsKey(berryMonth)) {
                        monthlyRevenue.put(berryMonth, monthlyRevenue.get(berryMonth) + berry.getAmount());
                    }
                }
            }
            
            // Add data points to the series
            int monthIndex = 0;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM");
            for (Map.Entry<YearMonth, Integer> entry : monthlyRevenue.entrySet()) {
                series.getData().add(new XYChart.Data<>(monthIndex, entry.getValue()));
                monthIndex++;
            }
            
            // Customize x-axis labels to show month names
            xAxis.setTickLabelFormatter(new javafx.util.StringConverter<Number>() {
                @Override
                public String toString(Number object) {
                    int index = object.intValue();
                    YearMonth month = currentMonth.minusMonths(5 - index);
                    return month.format(formatter);
                }
                
                @Override
                public Number fromString(String string) {
                    return 0;
                }
            });
            
        } catch (SQLException e) {
            System.err.println("Error loading berry data for chart: " + e.getMessage());
            // Add dummy data if database fails
            series.getData().add(new XYChart.Data<>(0, 0));
            series.getData().add(new XYChart.Data<>(1, 0));
        }
        
        lineChart.getData().add(series);
        
        // Style the line and points
        lineChart.lookup(".chart-series-line").setStyle("-fx-stroke: #76ff76; -fx-stroke-width: 3px;");
        
        return lineChart;
    }

    // Show the My Profile tab with user info and statistics
    private static void handleShowProfile() {
        try {
            User currentUser = SessionManager.getCurrentUser();
            VBox content = new VBox(25);
            content.setPadding(new Insets(30));
            content.setStyle("-fx-background-color: #2e2e2e;");

            // Header section
            VBox header = new VBox(10);
            Label title = new Label("👤 My Profile");
            title.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 28px; -fx-font-weight: bold;");
            Label subtitle = new Label("View your account details and statistics");
            subtitle.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 14px; -fx-font-style: italic;");
            header.getChildren().addAll(title, subtitle);

            // User info section
            VBox userInfo = new VBox(8);
            userInfo.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 10px; -fx-padding: 18px; -fx-border-color: #555; -fx-border-radius: 10px;");
            Label nameLabel = new Label("Display Name: " + currentUser.getDisplayName());
            nameLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 16px; -fx-font-weight: bold;");
            Label usernameLabel = new Label("Username: " + currentUser.getUsername());
            usernameLabel.setStyle("-fx-text-fill: #87ceeb; -fx-font-size: 14px;");
            Label levelLabel = new Label("Level: " + currentUser.getLevel());
            levelLabel.setStyle("-fx-text-fill: #76ff76; -fx-font-size: 14px;");
            Label xpLabel = new Label("XP: " + currentUser.getXp() + "/" + SystemParameters.calculateXpThreshold(currentUser.getLevel()));
            xpLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 14px;");
            Label pointsLabel = new Label("Points: " + currentUser.getPoints());
            pointsLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 14px;");
            userInfo.getChildren().addAll(nameLabel, usernameLabel, levelLabel, xpLabel, pointsLabel);

            // Statistics section
            VBox statsSection = new VBox(15);
            statsSection.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 10px; -fx-padding: 18px; -fx-border-color: #555; -fx-border-radius: 10px;");
            Label statsTitle = new Label("📊 Statistics");
            statsTitle.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 18px; -fx-font-weight: bold;");

            // Add the monthly berry revenue chart
            LineChart<Number, Number> berryChart = createMonthlyBerryRevenueChart(currentUser.getUsername());
            statsSection.getChildren().addAll(statsTitle, berryChart);

            content.getChildren().addAll(header, userInfo, statsSection);
            setMainContent(content, "My Profile");
        } catch (Exception e) {
            DialogFactory.showError("Error loading profile: " + e.getMessage());
        }
    }

    /**
     * Returns the JavaFX HostServices instance for opening URLs, etc.
     */
    public static HostServices getAppHostServices() {
        return appHostServices;
    }

    // Elegant dialog for creating a new idea
    private static void showElegantCreateIdeaDialog(User currentUser) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create New Idea");
        dialog.setHeaderText("Enter the details for your new idea:");
        dialog.getDialogPane().setStyle("-fx-background-color: #2e2e2e;");

        VBox formContent = new VBox(12);
        formContent.setPadding(new Insets(15));

        TextField nameField = new TextField();
        nameField.setPromptText("Idea name (e.g., 'Solar-Powered Water Purifier')");
        nameField.setStyle("-fx-background-color: #3e3e3e; -fx-text-fill: #fff;");

        TextArea descField = new TextArea();
        descField.setPromptText("Describe your idea in detail...");
        descField.setPrefRowCount(4);
        descField.setStyle("-fx-background-color: #3e3e3e; -fx-text-fill: #fff;");

        formContent.getChildren().addAll(
            new Label("Idea Name:"),
            nameField,
            new Label("Description (Optional):"),
            descField
        );

        dialog.getDialogPane().setContent(formContent);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        UIStyleManager.enhanceDialogWithKeyboardNavigation(dialog);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String name = nameField.getText().trim();
            String description = descField.getText().trim();
            if (name.isEmpty()) {
                DialogFactory.showError("Idea name cannot be empty.");
                return;
            }
            try {
                Idea newIdea = new Idea(name, description, currentUser.getUsername());
                int ideaId = DatabaseManager.createIdea(newIdea.getName(), newIdea.getDescription(), newIdea.getAuthor());
                if (ideaId > 0) {
                    DialogFactory.showInfo("Idea Created", "Your idea was created successfully!");
                    handleShowIdeas(); // Refresh the ideas list
                } else {
                    DialogFactory.showError("Failed to create idea.");
                }
            } catch (Exception ex) {
                DialogFactory.showError("Error creating idea: " + ex.getMessage());
            }
        }
    }

    // Elegant dialog for associating needs with an idea
    private static void showElegantAssociateNeedsDialog(Idea selected, Map<Integer, Need> availableNeeds) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Associate Needs with Idea");
        dialog.setHeaderText("Select needs to associate with this idea:");
        dialog.getDialogPane().setStyle("-fx-background-color: #2e2e2e;");

        VBox formContent = new VBox(10);
        formContent.setPadding(new Insets(15));

        // List of checkboxes for all available needs
        Map<Integer, CheckBox> needCheckBoxes = new HashMap<>();
        for (Need need : availableNeeds.values()) {
            CheckBox cb = new CheckBox(need.getName());
            cb.setStyle("-fx-text-fill: #fff;");
            if (selected.getAssociatedNeedIds() != null && selected.getAssociatedNeedIds().contains(need.getId())) {
                cb.setSelected(true);
            }
            needCheckBoxes.put(need.getId(), cb);
            formContent.getChildren().add(cb);
        }

        dialog.getDialogPane().setContent(formContent);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        UIStyleManager.enhanceDialogWithKeyboardNavigation(dialog);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Set<Integer> selectedNeedIds = new HashSet<>();
            for (Map.Entry<Integer, CheckBox> entry : needCheckBoxes.entrySet()) {
                if (entry.getValue().isSelected()) {
                    selectedNeedIds.add(entry.getKey());
                }
            }
            try {
                DatabaseManager.updateIdeaNeeds(selected.getId(), selectedNeedIds);
                DialogFactory.showInfo("Needs Associated", "Needs successfully associated with the idea.");
                handleShowIdeas(); // Refresh the ideas list
            } catch (Exception ex) {
                DialogFactory.showError("Error associating needs: " + ex.getMessage());
            }
        }
    }

    // Elegant dialog to show details of an idea
    private static void showElegantIdeaDetails(Idea selected) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Idea Details");
        dialog.setHeaderText("Details for: " + selected.getName());
        dialog.getDialogPane().setStyle("-fx-background-color: #2e2e2e;");

        VBox content = new VBox(12);
        content.setPadding(new Insets(18));

        Label nameLabel = new Label("Name: " + selected.getName());
        nameLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 18px; -fx-font-weight: bold;");

        Label authorLabel = new Label("Author: " + selected.getAuthor());
        authorLabel.setStyle("-fx-text-fill: #87ceeb; -fx-font-size: 14px;");

        Label statusLabel = new Label("Status: " + (selected.getStatus() != null ? selected.getStatus() : "Draft"));
        statusLabel.setStyle("-fx-text-fill: #76ff76; -fx-font-size: 14px;");

        Label votesLabel = new Label("Votes: " + selected.getVoteCount());
        votesLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label descLabel = new Label("Description:");
        descLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 15px; -fx-font-weight: bold;");
        TextArea descArea = new TextArea(selected.getDescription());
        descArea.setEditable(false);
        descArea.setWrapText(true);
        descArea.setPrefRowCount(4);
        descArea.setStyle("-fx-background-color: #3e3e3e; -fx-text-fill: #fff;");

        // Associated needs
        Label needsLabel = new Label("Associated Needs:");
        needsLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 15px; -fx-font-weight: bold;");
        VBox needsBox = new VBox(3);
        if (selected.getAssociatedNeedIds() != null && !selected.getAssociatedNeedIds().isEmpty()) {
            for (Integer needId : selected.getAssociatedNeedIds()) {
                try {
                    Need need = DatabaseManager.getNeed(needId);
                    if (need != null) {
                        Label needLabel = new Label("• " + need.getName());
                        needLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 13px;");
                        needsBox.getChildren().add(needLabel);
                    }
                } catch (Exception ignored) {}
            }
        } else {
            Label noneLabel = new Label("(No needs associated)");
            noneLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 13px; font-style: italic;");
            needsBox.getChildren().add(noneLabel);
        }

        content.getChildren().addAll(
            nameLabel, authorLabel, statusLabel, votesLabel,
            descLabel, descArea,
            needsLabel, needsBox
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        UIStyleManager.enhanceDialogWithKeyboardNavigation(dialog);
        dialog.showAndWait();
    }

    // Elegant dialog for creating a new branch or sub-branch
    private static void showElegantCreateBranchDialog(Branch parent, Map<Integer, Idea> availableIdeas) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(parent == null ? "Create Root Branch" : "Create Sub-branch");
        dialog.setHeaderText(parent == null ? "Enter details for the new root branch:" : "Enter details for the new sub-branch:");
        dialog.getDialogPane().setStyle("-fx-background-color: #2e2e2e;");

        VBox formContent = new VBox(12);
        formContent.setPadding(new Insets(15));

        TextField nameField = new TextField();
        nameField.setPromptText("Branch name (e.g., 'Infrastructure', 'Education')");
        nameField.setStyle("-fx-background-color: #3e3e3e; -fx-text-fill: #fff;");

        TextArea descField = new TextArea();
        descField.setPromptText("Describe this branch...");
        descField.setPrefRowCount(3);
        descField.setStyle("-fx-background-color: #3e3e3e; -fx-text-fill: #fff;");

        ComboBox<Idea> ideaCombo = new ComboBox<>();
        ideaCombo.setPromptText("Associate with an idea (optional)");
        ideaCombo.setStyle("-fx-background-color: #3e3e3e; -fx-text-fill: #fff;");
        ideaCombo.getItems().add(null); // Option for no association
        for (Idea idea : availableIdeas.values()) {
            ideaCombo.getItems().add(idea);
        }
        ideaCombo.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Idea item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? "(None)" : item.getName());
            }
        });
        ideaCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Idea item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? "(None)" : item.getName());
            }
        });

        formContent.getChildren().addAll(
            new Label("Branch Name:"),
            nameField,
            new Label("Description (Optional):"),
            descField,
            new Label("Associate with Idea (Optional):"),
            ideaCombo
        );

        dialog.getDialogPane().setContent(formContent);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        UIStyleManager.enhanceDialogWithKeyboardNavigation(dialog);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String name = nameField.getText().trim();
            String description = descField.getText().trim();
            Idea selectedIdea = ideaCombo.getValue();
            int ideaId = (selectedIdea != null) ? selectedIdea.getId() : 0;
            if (name.isEmpty()) {
                DialogFactory.showError("Branch name cannot be empty.");
                return;
            }
            try {
                Branch newBranch = new Branch();
                newBranch.setName(name);
                newBranch.setDescription(description);
                newBranch.setIdeaId(ideaId);
                newBranch.setParentId(parent == null ? 0 : parent.getId());
                int branchId = DatabaseManager.createBranch(newBranch.getName(), newBranch.getDescription(), newBranch.getIdeaId(), newBranch.getParentId());
                // If branchId is greater than 0, it means creation was successful
                newBranch.setId(branchId);
                newBranch.setCurrentPhase(Branch.Phase.GENERATION); // Default phase for new branches
                DatabaseManager.updateBranchPhase(branchId, newBranch.getCurrentPhase());
                // If the branch was created successfully, show a success message
                if (branchId > 0) {
                    DialogFactory.showInfo("Branch Created", "Branch created successfully!");
                    handleShowBranches(); // Refresh the branches view
                } else {
                    DialogFactory.showError("Failed to create branch.");
                }
            } catch (Exception ex) {
                DialogFactory.showError("Error creating branch: " + ex.getMessage());
            }
        }
    }

    // Elegant dialog for associating an idea with a branch
    private static void showElegantAssociateIdeaDialog(Branch selected, Map<Integer, Idea> availableIdeas) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Associate Idea with Branch");
        dialog.setHeaderText("Select an idea to associate with this branch:");
        dialog.getDialogPane().setStyle("-fx-background-color: #2e2e2e;");

        VBox formContent = new VBox(12);
        formContent.setPadding(new Insets(15));

        ComboBox<Idea> ideaCombo = new ComboBox<>();
        ideaCombo.setPromptText("Select an idea");
        ideaCombo.setStyle("-fx-background-color: #3e3e3e; -fx-text-fill: #fff;");
        for (Idea idea : availableIdeas.values()) {
            ideaCombo.getItems().add(idea);
        }
        // Pre-select current association if any
        if (selected.getIdeaId() > 0 && availableIdeas.containsKey(selected.getIdeaId())) {
            ideaCombo.setValue(availableIdeas.get(selected.getIdeaId()));
        }
        ideaCombo.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Idea item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? "(None)" : item.getName());
            }
        });
        ideaCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Idea item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? "(None)" : item.getName());
            }
        });

        formContent.getChildren().addAll(new Label("Idea:"), ideaCombo);

        dialog.getDialogPane().setContent(formContent);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        UIStyleManager.enhanceDialogWithKeyboardNavigation(dialog);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Idea selectedIdea = ideaCombo.getValue();
            int ideaId = (selectedIdea != null) ? selectedIdea.getId() : 0;
            try {
                selected.setIdeaId(ideaId);
                DatabaseManager.updateBranchIdea(selected.getId(), ideaId);
                DialogFactory.showInfo("Idea Associated", "Idea successfully associated with the branch.");
                handleShowBranches(); // Refresh the branches view
            } catch (Exception ex) {
                DialogFactory.showError("Error associating idea: " + ex.getMessage());
            }
        }
    }

    // Elegant dialog to show details of a branch
    private static void showElegantBranchDetails(Branch selected, Map<Integer, Branch> branches, Map<Integer, Idea> availableIdeas) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Branch Details");
        dialog.setHeaderText("Details for: " + selected.getName());
        dialog.getDialogPane().setStyle("-fx-background-color: #2e2e2e;");

        VBox content = new VBox(12);
        content.setPadding(new Insets(18));

        // Name and hierarchy
        String hierarchyIndicator = selected.getParentId() == 0 ? "🌱" : "🌿";
        Label nameLabel = new Label(hierarchyIndicator + " " + selected.getName());
        nameLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Phase
        Label phaseLabel = new Label("Phase: " + (selected.getCurrentPhase() != null ? selected.getCurrentPhase().toString() : "GENERATION"));
        phaseLabel.setStyle("-fx-text-fill: #76ff76; -fx-font-size: 14px;");

        // Description
        Label descLabel = new Label("Description:");
        descLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 15px; -fx-font-weight: bold;");
        TextArea descArea = new TextArea(selected.getDescription() != null ? selected.getDescription() : "No description");
        descArea.setEditable(false);
        descArea.setWrapText(true);
        descArea.setPrefRowCount(3);
        descArea.setStyle("-fx-background-color: #3e3e3e; -fx-text-fill: #fff;");

        // Associated idea
        String ideaText = "No idea associated";
        if (selected.getIdeaId() > 0 && availableIdeas.containsKey(selected.getIdeaId())) {
            ideaText = availableIdeas.get(selected.getIdeaId()).getName();
        }
        Label ideaLabel = new Label("Associated Idea: " + ideaText);
        ideaLabel.setStyle("-fx-text-fill: #87ceeb; -fx-font-size: 14px;");

        // Parent branch
        String parentText = "Root branch";
        if (selected.getParentId() > 0 && branches.containsKey(selected.getParentId())) {
            parentText = branches.get(selected.getParentId()).getName();
        }
        Label parentLabel = new Label("Parent: " + parentText);
        parentLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 13px;");

        // Children branches
        long childrenCount = branches.values().stream().filter(b -> b.getParentId() == selected.getId()).count();
        Label childrenLabel = new Label("Children: " + childrenCount);
        childrenLabel.setStyle("-fx-text-fill: #76ff76; -fx-font-size: 13px;");

        // List sub-fields (names)
        VBox subFieldsBox = new VBox(2);
        branches.values().stream()
            .filter(b -> b.getParentId() == selected.getId())
            .sorted(Comparator.comparing(Branch::getName))
            .forEach(b -> {
                Label sub = new Label("• " + b.getName());
                sub.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 12px;");
            });
        if (subFieldsBox.getChildren().isEmpty()) {
            Label none = new Label("(No sub-branches)");
            none.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 12px; font-style: italic;");
            subFieldsBox.getChildren().add(none);
        }

        content.getChildren().addAll(nameLabel, phaseLabel, descLabel, descArea, ideaLabel, parentLabel, childrenLabel, subFieldsBox);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        UIStyleManager.enhanceDialogWithKeyboardNavigation(dialog);
        dialog.showAndWait();
    }

    // Elegant dialog to show details of a field
    private static void showElegantFieldDetails(FieldOfExpertise selected) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Field Details");
        dialog.setHeaderText("Details for: " + selected.getName());
        dialog.getDialogPane().setStyle("-fx-background-color: #2e2e2e;");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #3e3e3e;");

        // Name and hierarchy
        String hierarchyIndicator = selected.getParentId() == 0 ? "🎓" : "📖";
        Label nameLabel = new Label(hierarchyIndicator + " " + selected.getName());
        nameLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Description
        Label descLabel = new Label("Description:");
        descLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 15px; -fx-font-weight: bold;");
        TextArea descArea = new TextArea(selected.getDescription() != null ? selected.getDescription() : "No description");
        descArea.setEditable(false);
        descArea.setWrapText(true);
        descArea.setPrefRowCount(3);
        descArea.setStyle("-fx-background-color: #3e3e3e; -fx-text-fill: #fff;");

        // Parent field
        String parentText = "Root field";
        if (selected.getParentId() != null && selected.getParentId() > 0) {
            FieldOfExpertise parent = fieldsOfExpertise.get(selected.getParentId());
            parentText = (parent != null) ? parent.getName() : ("ID: " + selected.getParentId());
        }
        Label parentLabel = new Label("Parent: " + parentText);
        parentLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 13px;");

        // Sub-fields
        long childrenCount = fieldsOfExpertise.values().stream().filter(f -> f.getParentId() != null && f.getParentId().equals(selected.getId())).count();
        Label childrenLabel = new Label("Sub-fields: " + childrenCount);
        childrenLabel.setStyle("-fx-text-fill: #76ff76; -fx-font-size: 13px;");

        // List sub-fields (names)
        VBox subFieldsBox = new VBox(2);
        fieldsOfExpertise.values().stream()
            .filter(f -> f.getParentId() != null && f.getParentId().equals(selected.getId()))
            .sorted(Comparator.comparing(FieldOfExpertise::getName))
            .forEach(f -> {
                Label sub = new Label("• " + f.getName());
                sub.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 12px;");
            });
        if (subFieldsBox.getChildren().isEmpty()) {
            Label none = new Label("(No sub-fields)");
            none.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 12px; font-style: italic;");
            subFieldsBox.getChildren().add(none);
        }

        content.getChildren().addAll(nameLabel, descLabel, descArea, parentLabel, childrenLabel, subFieldsBox);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private static void showElegantCreateFieldDialog(FieldOfExpertise parent) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(parent == null ? "Create New Field of Expertise" : "Create Sub-field for " + parent.getName());
        UIStyleManager.styleDialog(dialog);

        VBox dialogContent = new VBox(15); // Changed 'content' to 'dialogContent' for consistency
        dialogContent.setPadding(new Insets(20));
        dialogContent.setStyle("-fx-background-color: #3e3e3e;");

        Label titleLabel = new Label(parent == null ? "Enter New Field Details" : "Enter Sub-field Details for: " + parent.getName());
        titleLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 18px; -fx-font-weight: bold;");

        TextField nameField = new TextField();
        nameField.setPromptText("Field Name (e.g., Java Programming)");
        UIStyleManager.styleTextField(nameField);

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Detailed description of the field...");
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(4);
        UIStyleManager.styleTextArea(descriptionArea);

        Label parentFieldLabel = new Label("Parent Field (optional):");
        parentFieldLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 14px;");
        
        ComboBox<FieldOfExpertise> parentFieldComboBox = new ComboBox<>();
        UIStyleManager.applyDarkThemeComboBoxStyle(parentFieldComboBox);
        parentFieldComboBox.setPromptText("Select Parent Field");

        List<FieldOfExpertise> possibleParents = new ArrayList<>();
        possibleParents.add(null); // Represents "None (Root Field)"

        // Fix: 'selected' is not defined. For creating a new field, all existing fields are potential parents.
        // No exclusion logic based on the (non-existent) new field's descendants is needed here.
        Set<Integer> exclusionIds = Collections.emptySet();

        for (FieldOfExpertise foe : fieldsOfExpertise.values()) {
            // The condition '!exclusionIds.contains(foe.getId())' will always be true if exclusionIds is empty.
            // This correctly adds all fields as possible parents.
            if (!exclusionIds.contains(foe.getId())) {
                possibleParents.add(foe);
            }
        }
        parentFieldComboBox.setItems(FXCollections.observableArrayList(possibleParents));

        parentFieldComboBox.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(FieldOfExpertise item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("None (Root Field)");
                } else {
                    setText(item.getName());
                }
            }
        });
        parentFieldComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(FieldOfExpertise item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("None (Root Field)");
                } else {
                    setText(item.getName());
                }
            }
        });
        
        // Fix: Initialize parentFieldComboBox based on the 'parent' parameter, not undefined 'selected'.
        if (parent == null) {
            parentFieldComboBox.setValue(null); // Default to "None (Root Field)"
        } else {
            // If creating a sub-field, pre-select the parent.
            // Ensure 'parent' itself is a valid choice (it should be, as exclusionIds is empty).
            parentFieldComboBox.setValue(parent);
        }

        dialogContent.getChildren().addAll(
                titleLabel,
                new Label("Field Name:"), nameField,
                new Label("Description:"), descriptionArea,
                parentFieldLabel, parentFieldComboBox
        );

        dialog.getDialogPane().setContent(dialogContent);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        UIStyleManager.styleDialogButtons(dialog);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText(parent == null ? "Create Field" : "Create Sub-field");
        okButton.setOnAction(_ -> {
            String newName = nameField.getText().trim();
            String newDescription = descriptionArea.getText().trim();
            FieldOfExpertise selectedParentInComboBox = parentFieldComboBox.getValue();

            if (newName.isEmpty()) {
                DialogFactory.showError("Field Name cannot be empty.");
                return;
            }

            Integer newParentId = (selectedParentInComboBox == null) ? null : selectedParentInComboBox.getId();
            
            // Fix: Create a new FieldOfExpertise object instead of updating 'selected'.
            // FieldOfExpertise newField = new FieldOfExpertise();
            // newField.setName(newName);
            // newField.setDescription(newDescription);
            // newField.setParentId(newParentId);

            try {
                // Assuming a method like createFieldOfExpertise exists or is handled by saveFieldOfExpertise
                // For simplicity, let's assume DatabaseManager.createFieldOfExpertise(name, description, parentId) returns new ID
                // Or adapt to how new fields are actually persisted.
                // If DatabaseManager.saveFieldOfExpertise handles both create and update,
                // then newField.setId(0) or similar might be needed.
                // Based on other create methods (e.g., createIdea, createBranch), a specific create method is likely.
                // Let's assume a createFieldOfExpertise method:
                int newFieldId = DatabaseManager.createFieldOfExpertise(newName, newDescription, newParentId);

                if (newFieldId > 0) {
                    DialogFactory.showSuccess("Field of Expertise '" + newName + "' created successfully!");
                    handleShowTrace(); // Refresh the UI
                    dialog.close(); 
                } else {
                    DialogFactory.showError("Failed to create field of expertise.");
                }
            } catch (SQLException ex) {
                DialogFactory.showError("Database error creating field: " + ex.getMessage());
            } catch (Exception ex) {
                DialogFactory.showError("An unexpected error occurred: " + ex.getMessage());
                ex.printStackTrace(); // For debugging
            }
        });
        
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText("✖️ Cancel");

        dialog.showAndWait();
    }

    // Helper method to get all descendant IDs of a field, including itself.
    private static Set<Integer> getDescendantIdsRecursive(FieldOfExpertise field, Map<Integer, FieldOfExpertise> allFields) {
        Set<Integer> descendantIds = new HashSet<>();
        if (field == null || field.getId() == 0) { // Ensure field and field.getId() are not null
            return descendantIds;
        }

        Queue<Integer> queue = new LinkedList<>();
        queue.add(field.getId());
        descendantIds.add(field.getId());

        while (!queue.isEmpty()) {
            Integer currentId = queue.poll();
            for (FieldOfExpertise potentialChild : allFields.values()) {
                if (potentialChild.getParentId() != null && potentialChild.getParentId().equals(currentId)) {
                    if (descendantIds.add(potentialChild.getId())) {
                        queue.add(potentialChild.getId());
                    }
                }
            }
        }
        return descendantIds;
    }

    private static void showElegantEditFieldDialog(FieldOfExpertise selectedField) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Field of Expertise: " + selectedField.getName());
        UIStyleManager.styleDialog(dialog);

        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setStyle("-fx-background-color: #3e3e3e;");

        Label titleLabel = new Label("✏️ Edit '" + selectedField.getName() + "'");
        titleLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 22px; -fx-font-weight: bold;");

        TextField nameField = new TextField(selectedField.getName());
        nameField.setPromptText("Field Name");
        UIStyleManager.styleTextField(nameField);

        TextArea descriptionArea = new TextArea(selectedField.getDescription());
        descriptionArea.setPromptText("Field Description");
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(4);
        UIStyleManager.styleTextArea(descriptionArea);

        Label parentFieldLabel = new Label("Parent Field (optional):");
        parentFieldLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-size: 14px;");
        
        ComboBox<FieldOfExpertise> parentFieldComboBox = new ComboBox<>();
        UIStyleManager.applyDarkThemeComboBoxStyle(parentFieldComboBox);
        parentFieldComboBox.setPromptText("Select Parent Field");

        List<FieldOfExpertise> possibleParents = new ArrayList<>();
        possibleParents.add(null); // Represents "None (Root Field)"

        Set<Integer> exclusionIds = getDescendantIdsRecursive(selectedField, fieldsOfExpertise);

        for (FieldOfExpertise foe : fieldsOfExpertise.values()) {
            if (!exclusionIds.contains(foe.getId())) {
                possibleParents.add(foe);
            }
        }
        parentFieldComboBox.setItems(FXCollections.observableArrayList(possibleParents));

        parentFieldComboBox.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(FieldOfExpertise item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("None (Root Field)");
                } else {
                    setText(item.getName());
                }
            }
        });
        parentFieldComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(FieldOfExpertise item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("None (Root Field)");
                } else {
                    setText(item.getName());
                }
            }
        });
        
        if (selectedField.getParentId() == null || selectedField.getParentId() == 0) {
            parentFieldComboBox.setValue(null);
        } else {
            FieldOfExpertise currentParent = fieldsOfExpertise.get(selectedField.getParentId());
            if (currentParent != null && !exclusionIds.contains(currentParent.getId())) {
                parentFieldComboBox.setValue(currentParent);
            } else {
                 parentFieldComboBox.setValue(null); 
            }
        }

        dialogContent.getChildren().addAll(
                titleLabel,
                new Label("Field Name:"), nameField,
                new Label("Description:"), descriptionArea,
                parentFieldLabel, parentFieldComboBox
        );

        dialog.getDialogPane().setContent(dialogContent);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        UIStyleManager.styleDialogButtons(dialog);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("💾 Save Changes");
        okButton.setOnAction(_ -> {
            String newName = nameField.getText().trim();
            String newDescription = descriptionArea.getText().trim();
            FieldOfExpertise selectedParent = parentFieldComboBox.getValue();

            if (newName.isEmpty()) {
                DialogFactory.showError("Field Name cannot be empty.");
                return;
            }

            Integer newParentId = (selectedParent == null) ? null : selectedParent.getId();
            
            // Update the original selectedField object
            selectedField.setName(newName);
            selectedField.setDescription(newDescription);
            selectedField.setParentId(newParentId);

            try {
                DatabaseManager.updateFieldOfExpertise(selectedField);
                DialogFactory.showSuccess("Field of Expertise '" + newName + "' updated successfully!");
                // The handleShowTrace method will reload from DB and refresh the UI including the local map
                handleShowTrace(); 
                dialog.close(); 
            } catch (SQLException e) {
                DialogFactory.showError("Database error while updating field: " + e.getMessage());
            } catch (Exception e) {
                DialogFactory.showError("An unexpected error occurred: " + e.getMessage());
                e.printStackTrace(); // For debugging
            }
        });
        
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText("✖️ Cancel");

        dialog.showAndWait();
    }

    private static void showElegantManageUsersDialog(FieldOfExpertise selected) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Manage Users in Field");
        dialog.setHeaderText("Manage user certifications for: " + selected.getName());
        UIStyleManager.styleDialog(dialog);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #2e2e2e;");

        // Load users with certification in this field
        List<User> users = DatabaseManager.getUsersByField(selected.getId());

        // Users list view
        ListView<User> usersList = new ListView<>();
        usersList.setItems(FXCollections.observableArrayList(users));
        usersList.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(user.getDisplayName() + " (" + user.getUsername() + ")");
                    setStyle("-fx-text-fill: #ffffff; -fx-font-size: 14px;");
                }
            }
        });
        usersList.setStyle("-fx-background-color: #3e3e3e; -fx-border-color: #555; -fx-border-radius: 8px;");
        usersList.setPrefHeight(200);

        // Add/Remove buttons
        HBox buttonsBox = new HBox(10);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button addButton = new Button("➕ Add User");
        addButton.setStyle("-fx-background-color: #00aa00; -fx-text-fill: white; -fx-font-weight: bold;");
        addButton.setOnAction(_ -> {
            Dialog<ButtonType> addDialog = new Dialog<>();
            addDialog.setTitle("Add User to Field");
            addDialog.setHeaderText("Enter username to add:");
            UIStyleManager.styleDialog(addDialog);

            TextField usernameField = new TextField();
            usernameField.setPromptText("Username");
            usernameField.setStyle("-fx-background-color: #3e3e3e; -fx-text-fill: #fff;");

            addDialog.getDialogPane().setContent(usernameField);
            addDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            UIStyleManager.enhanceDialogWithKeyboardNavigation(addDialog);

            Optional<ButtonType> result = addDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                String username = usernameField.getText().trim();
                if (username.isEmpty()) {
                    DialogFactory.showError("Username cannot be empty.");
                    return;
                }
                try {
                    // Check if user exists
                    User userToAdd = DatabaseManager.getUser(username);
                    if (userToAdd == null) {
                        DialogFactory.showError("User not found.");
                        return;
                    }
                    // Add certification
                    DatabaseManager.addUserCertification(userToAdd.getUsername(), selected.getId());
                    DialogFactory.showInfo("User Added", "User '" + userToAdd.getDisplayName() + "' added to the field.");
                    // Refresh users list
                    usersList.getItems().add(userToAdd);
                } catch (Exception ex) {
                    DialogFactory.showError("Error adding user: " + ex.getMessage());
                }
            }
        });

        Button removeButton = new Button("🗑️ Remove User");
        removeButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        removeButton.setOnAction(_ -> {
            User selectedUser = usersList.getSelectionModel().getSelectedItem();
            if (selectedUser == null) {
                DialogFactory.showError("Select a user to remove.");
                return;
            }
            try {
                // Remove certification
                DatabaseManager.removeUserCertification(selectedUser.getUsername(), selected.getId());
                DialogFactory.showInfo("User Removed", "User '" + selectedUser.getDisplayName() + "' removed from the field.");
                // Refresh users list
                usersList.getItems().remove(selectedUser);
            } catch (Exception ex) {
                DialogFactory.showError("Error removing user: " + ex.getMessage());
            }
        });

        buttonsBox.getChildren().addAll(addButton, removeButton);

        content.getChildren().addAll(usersList, buttonsBox);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private static void showTeamOpeningsDialog(Branch selectedBranch) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Manage Team Openings for: " + selectedBranch.getName());
        UIStyleManager.styleDialog(dialog);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #3e3e3e;");

        Label titleLabel = new Label("Configure Expertise for Branch: " + selectedBranch.getName());
        titleLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 18px; -fx-font-weight: bold;");

        ComboBox<Branch.Phase> phaseComboBox = new ComboBox<>();
        phaseComboBox.setItems(FXCollections.observableArrayList(Branch.Phase.values()));
        phaseComboBox.setPromptText("Select Phase");
        UIStyleManager.applyDarkThemeComboBoxStyle(phaseComboBox); // Corrected method name

        VBox expertiseVBox = new VBox(10);
        expertiseVBox.setPadding(new Insets(10));
        expertiseVBox.setStyle("-fx-border-color: #555555; -fx-border-width: 1; -fx-border-radius: 5;");
        ScrollPane scrollPane = new ScrollPane(expertiseVBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(250);
        // UIStyleManager.styleScrollPane(scrollPane); // Assuming this might not exist yet

        Map<Integer, Integer> currentRequirements = new HashMap<>();

        phaseComboBox.setOnAction(_ -> {
                                             Branch.Phase selectedPhase = phaseComboBox.getValue();
            if (selectedPhase == null) return;

            expertiseVBox.getChildren().clear();
            currentRequirements.clear();

            try {
                Map<Integer, Integer> loadedReqs = DatabaseManager.loadBranchExpertiseRequirements(selectedBranch.getId(), selectedPhase.name());
                currentRequirements.putAll(loadedReqs);
            } catch (SQLException e) {
                DialogFactory.showError("Error loading expertise requirements: " + e.getMessage());
            }

            if (fieldsOfExpertise == null) {
                DialogFactory.showError("Fields of Expertise not loaded.");
                return;
            }

            fieldsOfExpertise.values().forEach(field -> {
                HBox fieldRow = new HBox(10);
                fieldRow.setAlignment(Pos.CENTER_LEFT);
                Label fieldNameLabel = new Label(field.getName());
                fieldNameLabel.setStyle("-fx-text-fill: #cccccc;");
                fieldNameLabel.setPrefWidth(200);

                Spinner<Integer> levelSpinner = new Spinner<>(0, 10, currentRequirements.getOrDefault(field.getId(), 0));
                levelSpinner.setPrefWidth(80);
                // UIStyleManager.styleSpinner(levelSpinner); // Assuming this might not exist yet

                levelSpinner.valueProperty().addListener((_, _, newValue) -> {
                    if (newValue > 0) {
                        currentRequirements.put(field.getId(), newValue);
                    } else {
                        currentRequirements.remove(field.getId());
                    }
                });

                fieldRow.getChildren().addAll(fieldNameLabel, levelSpinner);
                expertiseVBox.getChildren().add(fieldRow);
            });
        });

        content.getChildren().addAll(titleLabel, new Label("Select Phase:"), phaseComboBox, new Label("Expertise Requirements:"), scrollPane);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        UIStyleManager.styleDialogButtons(dialog);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Save Requirements");
        okButton.setOnAction(_ -> {
            Branch.Phase selectedPhase = phaseComboBox.getValue();
            if (selectedPhase == null) {
                DialogFactory.showError("Please select a phase.");
                return;
            }

            try {
                DatabaseManager.saveBranchExpertiseRequirements(selectedBranch.getId(), selectedPhase.name(), currentRequirements);
                DialogFactory.showInfo("Success", "Team expertise requirements for phase '" + selectedPhase.name() + "' saved successfully!");
                dialog.close();
            } catch (SQLException ex) {
                DialogFactory.showError("Database error while saving requirements: " + ex.getMessage());
            }
        });

        dialog.showAndWait();
    }

    /**
     * Creates the comprehensive Fields of Expertise Manager UI
     * @param content The main content VBox to add the manager to
     */
    private static void createFieldsOfExpertiseManager(VBox content) {
        // Main container for the manager
        VBox managerContainer = new VBox(20);
        managerContainer.setPadding(new Insets(20));
        managerContainer.setStyle("-fx-background-color: #3e3e3e; -fx-background-radius: 10px; -fx-border-color: #555; -fx-border-radius: 10px;");
        
        // Create main layout with TreeView and action panel
        HBox mainLayout = new HBox(20);
        
        // Left side: TreeView for hierarchical display
        VBox treeSection = createFieldsTreeView();
        
        // Right side: Action panel and details
        VBox actionPanel = createFieldsActionPanel();
        
        mainLayout.getChildren().addAll(treeSection, actionPanel);
        HBox.setHgrow(treeSection, Priority.ALWAYS);
        
        managerContainer.getChildren().add(mainLayout);
        content.getChildren().add(managerContainer);
    }
    
    /**
     * Creates the TreeView section for displaying fields hierarchically
     * @return VBox containing the TreeView and related controls
     */
    private static VBox createFieldsTreeView() {
        VBox treeSection = new VBox(10);
        
        Label treeLabel = new Label("🌲 Fields of Expertiese Hierarchy View");
        treeLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Create TreeView for fields
        TreeView<FieldOfExpertise> fieldsTreeView = new TreeView<>();
        fieldsTreeView.setStyle("-fx-background-color: #2e2e2e; -fx-border-color: #555; -fx-border-radius: 5px;");
        fieldsTreeView.setPrefHeight(400);
        fieldsTreeView.setPrefWidth(350);
        
        // Build the tree structure
        TreeItem<FieldOfExpertise> rootItem = buildFieldsTree();
        fieldsTreeView.setRoot(rootItem);
        fieldsTreeView.setShowRoot(false);
        
        // Set custom cell factory for better display
        fieldsTreeView.setCellFactory(_ -> new TreeCell<FieldOfExpertise>() {
            @Override
            protected void updateItem(FieldOfExpertise field, boolean empty) {
                super.updateItem(field, empty);
                if (empty || field == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String icon = field.getParentId() == null || field.getParentId() == 0 ? "🎓" : "📖";
                    setText(icon + " " + field.getName());
                    setStyle("-fx-text-fill: #000000; -fx-font-size: 14px;");
                }
            }
        });
        
        // Add selection handler
        fieldsTreeView.getSelectionModel().selectedItemProperty().addListener((_, _, newSelection) -> {
            if (newSelection != null && newSelection.getValue() != null) {
                selectedFieldForDetails = newSelection.getValue();
                updateFieldDetailsPanel();
            }
        });
        
        // Store reference for refreshing
        currentFieldsTreeView = fieldsTreeView;
        
        // Search functionality
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search fields...");
        searchField.setStyle("-fx-background-color: #2e2e2e; -fx-text-fill: #ffffff; -fx-border-color: #555; -fx-border-radius: 5px;");
        searchField.setPrefWidth(250);
        
        Button clearSearchBtn = createStyledButton("Clear", "#f44336");
        clearSearchBtn.setPrefWidth(80);
        clearSearchBtn.setPrefHeight(30);
        
        searchField.textProperty().addListener((_, _, newText) -> {
            filterFieldsTree(newText);
        });
        
        clearSearchBtn.setOnAction(_ -> {
            searchField.clear();
            filterFieldsTree("");
        });
        
        searchBox.getChildren().addAll(searchField, clearSearchBtn);
        
        treeSection.getChildren().addAll(treeLabel, searchBox, fieldsTreeView);
        return treeSection;
    }
    
    /**
     * Creates the action panel with buttons and field details
     * @return VBox containing action buttons and details panel
     */
    private static VBox createFieldsActionPanel() {
        VBox actionPanel = new VBox(15);
        actionPanel.setPrefWidth(300);
        
        // Action buttons section
        Label actionsLabel = new Label("🛠️ Actions");
        actionsLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        VBox buttonContainer = new VBox(10);
        
        // Create Root Field button
        Button createRootBtn = createStyledButton("➕ Create Root Field", "#4caf50");
        createRootBtn.setPrefWidth(250);
        createRootBtn.setOnAction(_ -> showElegantCreateFieldDialog(null));
        
        // Create Sub-field button
        Button createSubBtn = createStyledButton("🌿 Create Sub-field", "#2196f3");
        createSubBtn.setPrefWidth(250);
        createSubBtn.setOnAction(_ -> {
            if (selectedFieldForDetails != null) {
                showElegantCreateFieldDialog(selectedFieldForDetails);
            } else {
                DialogFactory.showError("Please select a parent field first.");
            }
        });
        
        // Edit Field button
        Button editBtn = createStyledButton("✏️ Edit Field", "#ff9800");
        editBtn.setPrefWidth(250);
        editBtn.setOnAction(_ -> {
            if (selectedFieldForDetails != null) {
                showElegantEditFieldDialog(selectedFieldForDetails);
            } else {
                DialogFactory.showError("Please select a field to edit.");
            }
        });
        
        // Delete Field button
        Button deleteBtn = createStyledButton("🗑️ Delete Field", "#f44336");
        deleteBtn.setPrefWidth(250);
        deleteBtn.setOnAction(_ -> {
            if (selectedFieldForDetails != null) {
                showDeleteFieldConfirmation(selectedFieldForDetails);
            } else {
                DialogFactory.showError("Please select a field to delete.");
            }
        });
        
        // View Details button
        Button detailsBtn = createStyledButton("📋 View Details", "#9c27b0");
        detailsBtn.setPrefWidth(250);
        detailsBtn.setOnAction(_ -> {
            if (selectedFieldForDetails != null) {
                showElegantFieldDetails(selectedFieldForDetails);
            } else {
                DialogFactory.showError("Please select a field to view details.");
            }
        });
        
        // Manage Users button
        Button manageUsersBtn = createStyledButton("👥 Manage Users", "#607d8b");
        manageUsersBtn.setPrefWidth(250);
        manageUsersBtn.setOnAction(_ -> {
            if (selectedFieldForDetails != null) {
                showElegantManageUsersDialog(selectedFieldForDetails);
            } else {
                DialogFactory.showError("Please select a field to manage users.");
            }
        });
        
        // Refresh button
        Button refreshBtn = createStyledButton("🔄 Refresh", "#795548");
        refreshBtn.setPrefWidth(250);
        refreshBtn.setOnAction(_ -> refreshFieldsOfExpertise());
        
        buttonContainer.getChildren().addAll(
            createRootBtn, createSubBtn, editBtn, deleteBtn, detailsBtn, manageUsersBtn, refreshBtn
        );
        
        // Field details section
        Label detailsLabel = new Label("📝 Selected Field Details");
        detailsLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Details display area
        currentFieldDetailsArea = createFieldDetailsArea();
        
        actionPanel.getChildren().addAll(actionsLabel, buttonContainer, detailsLabel, currentFieldDetailsArea);
        return actionPanel;
    }
    
    /**
     * Creates the field details display area
     * @return VBox containing field details
     */
    private static VBox createFieldDetailsArea() {
        VBox detailsArea = new VBox(8);
        detailsArea.setPadding(new Insets(15));
        detailsArea.setStyle("-fx-background-color: #2e2e2e; -fx-border-color: #555; -fx-border-radius: 5px;");
        detailsArea.setPrefHeight(200);
        
        Label noSelectionLabel = new Label("Select a field to view details");
        noSelectionLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-style: italic;");
        detailsArea.getChildren().add(noSelectionLabel);
        
        return detailsArea;
    }
    
    /**
     * Builds the tree structure for fields of expertise
     * @return Root TreeItem for the tree
     */
    private static TreeItem<FieldOfExpertise> buildFieldsTree() {
        TreeItem<FieldOfExpertise> rootItem = new TreeItem<>();
        Map<Integer, TreeItem<FieldOfExpertise>> itemMap = new HashMap<>();
        
        // Create tree items for all fields
        for (FieldOfExpertise field : fieldsOfExpertise.values()) {
            TreeItem<FieldOfExpertise> item = new TreeItem<>(field);
            item.setExpanded(true);
            itemMap.put(field.getId(), item);
        }
        
        // Build the hierarchy
        for (FieldOfExpertise field : fieldsOfExpertise.values()) {
            TreeItem<FieldOfExpertise> item = itemMap.get(field.getId());
            
            if (field.getParentId() == null || field.getParentId() == 0) {
                // Root field
                rootItem.getChildren().add(item);
            } else {
                // Sub-field
                TreeItem<FieldOfExpertise> parentItem = itemMap.get(field.getParentId());
                if (parentItem != null) {
                    parentItem.getChildren().add(item);
                } else {
                    // Parent not found, treat as root
                    rootItem.getChildren().add(item);
                }
            }
        }
        
        // Sort children by name
        sortTreeItems(rootItem);
        
        return rootItem;
    }
    
    /**
     * Recursively sorts tree items by field name
     * @param item The tree item to sort
     */
    private static void sortTreeItems(TreeItem<FieldOfExpertise> item) {
        if (item.getChildren().isEmpty()) return;
        
        item.getChildren().sort((a, b) -> {
            if (a.getValue() == null && b.getValue() == null) return 0;
            if (a.getValue() == null) return -1;
            if (b.getValue() == null) return 1;
            return a.getValue().getName().compareToIgnoreCase(b.getValue().getName());
        });
        
        for (TreeItem<FieldOfExpertise> child : item.getChildren()) {
            sortTreeItems(child);
        }
    }
    
    /**
     * Filters the fields tree based on search text
     * @param searchText The text to search for
     */
    private static void filterFieldsTree(String searchText) {
        if (currentFieldsTreeView == null) return;
        
        if (searchText == null || searchText.trim().isEmpty()) {
            // Show all fields
            currentFieldsTreeView.setRoot(buildFieldsTree());
        } else {
            // Filter fields
            TreeItem<FieldOfExpertise> filteredRoot = buildFilteredTree(searchText.toLowerCase());
            currentFieldsTreeView.setRoot(filteredRoot);
        }
        
        currentFieldsTreeView.setShowRoot(false);
    }
    
    /**
     * Builds a filtered tree based on search text
     * @param searchText The search text (lowercase)
     * @return Filtered tree root
     */
    private static TreeItem<FieldOfExpertise> buildFilteredTree(String searchText) {
        TreeItem<FieldOfExpertise> rootItem = new TreeItem<>();
        
        for (FieldOfExpertise field : fieldsOfExpertise.values()) {
            if (field.getName().toLowerCase().contains(searchText) || 
                (field.getDescription() != null && field.getDescription().toLowerCase().contains(searchText))) {
                
                TreeItem<FieldOfExpertise> item = new TreeItem<>(field);
                item.setExpanded(true);
                rootItem.getChildren().add(item);
            }
        }
        
        // Sort filtered results
        rootItem.getChildren().sort((a, b) -> 
            a.getValue().getName().compareToIgnoreCase(b.getValue().getName()));
        
        return rootItem;
    }
    
    /**
     * Updates the field details panel with the selected field information
     */
    private static void updateFieldDetailsPanel() {
        if (currentFieldDetailsArea == null || selectedFieldForDetails == null) return;
        
        currentFieldDetailsArea.getChildren().clear();
        
        FieldOfExpertise field = selectedFieldForDetails;
        
        // Field name
        Label nameLabel = new Label("📌 " + field.getName());
        nameLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Field type
        String typeText = (field.getParentId() == null || field.getParentId() == 0) ? "Root Field" : "Sub-field";
        Label typeLabel = new Label("Type: " + typeText);
        typeLabel.setStyle("-fx-text-fill: #87ceeb; -fx-font-size: 12px;");
        
        // Parent field
        Label parentLabel = null;
        if (field.getParentId() != null && field.getParentId() > 0) {
            FieldOfExpertise parent = fieldsOfExpertise.get(field.getParentId());
            String parentName = (parent != null) ? parent.getName() : "Unknown";
            parentLabel = new Label("Parent: " + parentName);
            parentLabel.setStyle("-fx-text-fill: #ffab40; -fx-font-size: 12px;");
        }
        
        // Sub-fields count
        long subFieldsCount = fieldsOfExpertise.values().stream()
            .filter(f -> f.getParentId() != null && f.getParentId().equals(field.getId()))
            .count();
        Label subFieldsLabel = new Label("Sub-fields: " + subFieldsCount);
        subFieldsLabel.setStyle("-fx-text-fill: #76ff76; -fx-font-size: 12px;");
        
        // Users count
        Label usersLabel = new Label("Certified Users: Loading...");
        usersLabel.setStyle("-fx-text-fill: #ff7043; -fx-font-size: 12px;");
        
        // Load users count asynchronously
        loadUserCountForField(field.getId(), usersLabel);
        
        // Description
        String desc = field.getDescription();
        if (desc != null && !desc.trim().isEmpty()) {
            Label descHeader = new Label("Description:");
            descHeader.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 12px; -fx-font-weight: bold;");
            
            Label descLabel = new Label(desc.length() > 100 ? desc.substring(0, 100) + "..." : desc);
            descLabel.setStyle("-fx-text-fill: #d0d0d0; -fx-font-size: 11px;");
            descLabel.setWrapText(true);
            
            currentFieldDetailsArea.getChildren().addAll(nameLabel, typeLabel);
            if (parentLabel != null) currentFieldDetailsArea.getChildren().add(parentLabel);
            currentFieldDetailsArea.getChildren().addAll(subFieldsLabel, usersLabel, descHeader, descLabel);
        } else {
            currentFieldDetailsArea.getChildren().addAll(nameLabel, typeLabel);
            if (parentLabel != null) currentFieldDetailsArea.getChildren().add(parentLabel);
            currentFieldDetailsArea.getChildren().addAll(subFieldsLabel, usersLabel);
        }
    }
    
    /**
     * Loads the user count for a field asynchronously
     * @param fieldId The field ID
     * @param usersLabel The label to update
     */
    private static void loadUserCountForField(int fieldId, Label usersLabel) {
        // Run in background thread to avoid blocking UI
        new Thread(() -> {
            try {
                List<User> users = DatabaseManager.getUsersByField(fieldId);
                Platform.runLater(() -> {
                    usersLabel.setText("Certified Users: " + users.size());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    usersLabel.setText("Certified Users: Error loading");
                });
            }
        }).start();
    }
    
    /**
     * Shows confirmation dialog for deleting a field
     * @param field The field to delete
     */
    private static void showDeleteFieldConfirmation(FieldOfExpertise field) {
        // Check if field has sub-fields
        boolean hasSubFields = fieldsOfExpertise.values().stream()
            .anyMatch(f -> f.getParentId() != null && f.getParentId().equals(field.getId()));
        
        Dialog<ButtonType> confirmDialog = new Dialog<>();
        confirmDialog.setTitle("Delete Field of Expertise");
        confirmDialog.setHeaderText("Confirm Deletion");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #2e2e2e;");
        
        Label warningLabel = new Label("⚠️ Are you sure you want to delete this field?");
        warningLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label fieldLabel = new Label("Field: " + field.getName());
        fieldLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px;");
        
        if (hasSubFields) {
            Label subFieldsWarning = new Label("⚠️ This field has sub-fields that will also be deleted!");
            subFieldsWarning.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 12px; -fx-font-weight: bold;");
            content.getChildren().add(subFieldsWarning);
        }
        
        Label usersWarning = new Label("⚠️ This will remove certifications for all users in this field!");
        usersWarning.setStyle("-fx-text-fill: #ff9800; -fx-font-size: 12px;");
        
        content.getChildren().addAll(warningLabel, fieldLabel, usersWarning);
        
        confirmDialog.getDialogPane().setContent(content);
        confirmDialog.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);
        confirmDialog.getDialogPane().setStyle("-fx-background-color: #2e2e2e;");
        
        // Style buttons
        Button yesButton = (Button) confirmDialog.getDialogPane().lookupButton(ButtonType.YES);
        Button noButton = (Button) confirmDialog.getDialogPane().lookupButton(ButtonType.NO);
        
        yesButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        noButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.YES) {
            deleteFieldOfExpertise(field);
        }
    }
    
    /**
     * Deletes a field of expertise and all its sub-fields
     * @param field The field to delete
     */
    private static void deleteFieldOfExpertise(FieldOfExpertise field) {
        try {
            // Delete the field and all its descendants
            DatabaseManager.deleteFieldOfExpertise(field.getId());
            
            DialogFactory.showSuccess("Field '" + field.getName() + "' deleted successfully!");
            refreshFieldsOfExpertise();
            
        } catch (SQLException e) {
            DialogFactory.showError("Failed to delete field: " + e.getMessage());
        } catch (Exception e) {
            DialogFactory.showError("An unexpected error occurred: " + e.getMessage());
        }
    }
    
    /**
     * Refreshes the fields of expertise data and UI
     */
    private static void refreshFieldsOfExpertise() {
        try {
            // Reload from database
            fieldsOfExpertise = DatabaseManager.loadAllFieldsOfExpertise();
            
            // Refresh the tree view
            if (currentFieldsTreeView != null) {
                currentFieldsTreeView.setRoot(buildFieldsTree());
                currentFieldsTreeView.setShowRoot(false);
            }
            
            // Clear selection and details
            selectedFieldForDetails = null;
            if (currentFieldDetailsArea != null) {
                currentFieldDetailsArea.getChildren().clear();
                Label noSelectionLabel = new Label("Select a field to view details");
                noSelectionLabel.setStyle("-fx-text-fill: #b2b2b2; -fx-font-style: italic;");
                currentFieldDetailsArea.getChildren().add(noSelectionLabel);
            }
            
            // Refresh the entire trace tab to update statistics
            handleShowTrace();
            
        } catch (SQLException e) {
            DialogFactory.showError("Failed to refresh fields: " + e.getMessage());
        }
    }
}
