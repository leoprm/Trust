package com.mycompany.trust;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;

import java.sql.SQLException;

public class WindowBuilder extends Application {
    private Stage primaryStage;
    private TextField userNameTxt;
    private PasswordField passwordTxt;
    private Label messageLabel;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Trust Login");

        Pane pane = new Pane();
        pane.setPrefSize(649, 316);
        pane.setStyle("-fx-background-color: #1e1e1e;");

        Button loginBtn = new Button("Login");
        loginBtn.setLayoutX(342.04);
        loginBtn.setLayoutY(184.13);
        loginBtn.setPrefWidth(105.81);
        loginBtn.setPrefHeight(28.00);
        loginBtn.setDisable(false);
        loginBtn.setFont(Font.font("System", 14));
        loginBtn.setStyle(UIStyleManager.BUTTON_STYLE);
        loginBtn.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> { loginBtn.setStyle(UIStyleManager.BUTTON_HOVER_STYLE); });
        loginBtn.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> { loginBtn.setStyle(UIStyleManager.BUTTON_STYLE); });
        loginBtn.setOnAction(e -> handleLogin());
        pane.getChildren().add(loginBtn);

        Button createUserBtn = new Button("Create User");
        createUserBtn.setLayoutX(206.05);
        createUserBtn.setLayoutY(185.13);
        createUserBtn.setPrefWidth(105.81);
        createUserBtn.setPrefHeight(28.00);
        createUserBtn.setDisable(false);
        createUserBtn.setFont(Font.font("System", 14));
        createUserBtn.setStyle(UIStyleManager.BUTTON_STYLE);
        createUserBtn.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> { createUserBtn.setStyle(UIStyleManager.BUTTON_HOVER_STYLE); });
        createUserBtn.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> { createUserBtn.setStyle(UIStyleManager.BUTTON_STYLE); });
        createUserBtn.setOnAction(e -> handleCreateUser());
        pane.getChildren().add(createUserBtn);

        userNameTxt = new TextField("");
        userNameTxt.setLayoutX(207.05);
        userNameTxt.setLayoutY(101.63);
        userNameTxt.setPrefWidth(240.00);
        userNameTxt.setPrefHeight(21.00);
        userNameTxt.setPromptText("User Name");
        userNameTxt.setFont(Font.font("System", 14));
        userNameTxt.setStyle(UIStyleManager.TEXT_FIELD_STYLE_DARK);
        userNameTxt.setOnAction(e -> handleLogin());
        pane.getChildren().add(userNameTxt);

        passwordTxt = new PasswordField();
        passwordTxt.setText("");
        passwordTxt.setLayoutX(207.05);
        passwordTxt.setLayoutY(134.29);
        passwordTxt.setPrefWidth(240.00);
        passwordTxt.setPrefHeight(21.00);
        passwordTxt.setPromptText("Password");
        passwordTxt.setFont(Font.font("System", 14));
        passwordTxt.setStyle(UIStyleManager.TEXT_FIELD_STYLE_DARK);
        passwordTxt.setOnAction(e -> handleLogin());
        pane.getChildren().add(passwordTxt);

        messageLabel = new Label("");
        messageLabel.setLayoutX(207.05);
        messageLabel.setLayoutY(220.00);
        messageLabel.setPrefWidth(240.00);
        messageLabel.setPrefHeight(21.00);
        messageLabel.setFont(Font.font("System", 14));
        messageLabel.setStyle("-fx-text-fill: #FF0000;");
        pane.getChildren().add(messageLabel);

        Scene scene = new Scene(pane, 649, 316);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleLogin() {
        String username = userNameTxt.getText();
        String password = passwordTxt.getText();
        
        try {
            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter both username and password");
                return;
            }

            if (!TrustSystem.users.containsKey(username)) {
                messageLabel.setText("User does not exist");
                return;
            }

            if (!TrustSystem.users.get(username).checkPassword(password)) {
                messageLabel.setText("Incorrect password");
                return;
            }

            // Login successful - store user credentials in session
            SessionManager.setCurrentUser(TrustSystem.users.get(username), username, password);
            
            messageLabel.setText("");
            messageLabel.setStyle("-fx-text-fill: #00FF00;");
            messageLabel.setText("Login successful!");
            
            // Start the main TrustSystem
            TrustSystem.startMainSystem(primaryStage);
        } catch (Exception e) {
            messageLabel.setText("Error during login: " + e.getMessage());
        }
    }

    private void handleCreateUser() {
        String username = userNameTxt.getText();
        String password = passwordTxt.getText();
        
        try {
            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter both username and password");
                return;
            }

            if (TrustSystem.users.containsKey(username)) {
                messageLabel.setText("User already exists");
                return;
            }

            // Create user in the database
            DatabaseManager.createUser(username, password);
            
            // Reload user data from the database to ensure it's properly registered in the system
            DatabaseManager.loadUsers();
            
            messageLabel.setStyle("-fx-text-fill: #00FF00;");
            messageLabel.setText("User created successfully!");
            
            // Clear the fields
            userNameTxt.clear();
            passwordTxt.clear();
        } catch (SQLException e) {
            messageLabel.setText("Error creating user: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            // Initialize database connection pool first
            DatabaseConnection.initializeDataSource();
            
            // Then load all data before showing the login window
            DatabaseManager.loadAllData();
            launch(args);
        } catch (SQLException e) {
            System.err.println("Failed to load data: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for more detailed error info
            System.exit(1);
        } finally {
            // Make sure to close the connection pool when the application exits
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                DatabaseConnection.closeDataSource();
            }));
        }
    }
}