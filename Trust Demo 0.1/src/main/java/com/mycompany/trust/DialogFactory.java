package com.mycompany.trust;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class DialogFactory {
    
    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        
        // Make the dialog resizable
        alert.setResizable(true);
        
        // Set a larger preferred size
        alert.getDialogPane().setPrefWidth(500);
        alert.getDialogPane().setPrefHeight(200);
        
        // Create a TextArea for copyable text with proper color contrast
        TextArea textArea = new TextArea(message);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle(UIStyleManager.TEXT_AREA_STYLE_DARK + "; -fx-text-fill: #ff6b6b;");
        
        // Style the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(UIStyleManager.DIALOG_STYLE);
        
        // Fix styling for header text if present
        if (dialogPane.lookup(".header-panel") != null) {
            dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #3e3e3e;");
            if (dialogPane.lookup(".header-panel .label") != null) {
                dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
            }
        }
        
        // Make sure content text is white
        if (dialogPane.lookup(".content.label") != null) {
            dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
        }
        
        // Add the TextArea to the dialog content
        VBox contentPane = new VBox(10);
        contentPane.getChildren().addAll(
                new Label("An error occurred:"),
                textArea
        );
        contentPane.setPadding(new Insets(10));
        
        // Style labels with visible text
        contentPane.getChildren().filtered(node -> node instanceof Label)
            .forEach(node -> ((Label) node).setStyle(UIStyleManager.LABEL_BOLD_STYLE));
        
        alert.getDialogPane().setContent(contentPane);
        
        // Style buttons
        dialogPane.lookupAll(".button").forEach(node -> {
            if (node instanceof Button) {
                Button button = (Button) node;
                button.setStyle(UIStyleManager.BUTTON_STYLE);
                
                // Add hover effect
                button.setOnMouseEntered(event -> 
                    button.setStyle(UIStyleManager.BUTTON_HOVER_STYLE));
                button.setOnMouseExited(event -> 
                    button.setStyle(UIStyleManager.BUTTON_STYLE));
            }
        });
        
        UIStyleManager.enhanceDialogWithKeyboardNavigation(alert);
        
        alert.showAndWait();
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        
        // Make the dialog resizable
        alert.setResizable(true);
        
        // Set a larger preferred size
        alert.getDialogPane().setPrefWidth(450);
        alert.getDialogPane().setPrefHeight(180);
        
        // Create a TextArea for copyable text with proper color contrast
        TextArea textArea = new TextArea(message);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle(UIStyleManager.TEXT_AREA_STYLE_DARK + "; -fx-text-fill: #76ff76;");
        
        // Style the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(UIStyleManager.DIALOG_STYLE);
        
        // Fix styling for header text if present
        if (dialogPane.lookup(".header-panel") != null) {
            dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #3e3e3e;");
            if (dialogPane.lookup(".header-panel .label") != null) {
                dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
            }
        }
        
        // Make sure content text is white
        if (dialogPane.lookup(".content.label") != null) {
            dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
        }
        
        // Add the TextArea to the dialog content
        VBox contentPane = new VBox(10);
        contentPane.getChildren().addAll(
                new Label(title),
                textArea
        );
        contentPane.setPadding(new Insets(10));
        
        // Style labels with visible text
        contentPane.getChildren().filtered(node -> node instanceof Label)
            .forEach(node -> ((Label) node).setStyle(UIStyleManager.LABEL_BOLD_STYLE));
        
        alert.getDialogPane().setContent(contentPane);
        
        // Style buttons
        dialogPane.lookupAll(".button").forEach(node -> {
            if (node instanceof Button) {
                Button button = (Button) node;
                button.setStyle(UIStyleManager.BUTTON_STYLE);
                
                // Add hover effect
                button.setOnMouseEntered(event -> 
                    button.setStyle(UIStyleManager.BUTTON_HOVER_STYLE));
                button.setOnMouseExited(event -> 
                    button.setStyle(UIStyleManager.BUTTON_STYLE));
            }
        });
        
        UIStyleManager.enhanceDialogWithKeyboardNavigation(alert);
        
        alert.showAndWait();
    }

    public static void showConfirmation(String title, String message, Runnable onConfirm) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        
        // Style the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(UIStyleManager.DIALOG_STYLE);
        
        // Use the UIStyleManager to enhance the dialog
        UIStyleManager.enhanceDialogWithKeyboardNavigation(alert);
        
        // Make sure content text is white
        Label contentLabel = new Label(message);
        contentLabel.setStyle(UIStyleManager.LABEL_STYLE);
        contentLabel.setWrapText(true);
        
        // Set the content
        alert.getDialogPane().setContent(contentLabel);
        
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                onConfirm.run();
            }
        });
    }

    public static void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setResizable(true);
        alert.getDialogPane().setPrefWidth(500);
        alert.getDialogPane().setPrefHeight(200);

        // Create a TextArea for copyable text with green color for success
        TextArea textArea = new TextArea(message);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle(UIStyleManager.TEXT_AREA_STYLE_DARK + "; -fx-text-fill: #4caf50;");

        // Style the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(UIStyleManager.DIALOG_STYLE);

        VBox contentPane = new VBox(10);
        contentPane.getChildren().addAll(
                new Label("Success:"),
                textArea
        );
        contentPane.setPadding(new Insets(10));
        dialogPane.setContent(contentPane);

        alert.showAndWait();
    }

    // Custom input dialogs will use the enhanceDialogWithKeyboardNavigation method
    // from UIStyleManager to ensure all text is visible
}
