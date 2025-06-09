package com.mycompany.trust;

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;

public class UIStyleManager {
    // UI Style Constants
    public static final String BUTTON_STYLE = "-fx-background-color: #2e2e2e; -fx-text-fill: #D9D9D9; -fx-border-color: #979797; " +
                                             "-fx-border-radius: 4px; -fx-background-radius: 4px; -fx-border-width: 1px;";
    public static final String BUTTON_HOVER_STYLE = "-fx-background-color: #3e3e3e; -fx-text-fill: #FFFFFF; -fx-border-color: #b0b0b0; " +
                                                   "-fx-border-radius: 4px; -fx-background-radius: 4px; -fx-border-width: 1px;";
    public static final String BUTTON_FOCUSED_STYLE = "-fx-background-color: #3e3e3e; -fx-text-fill: #FFFFFF; -fx-border-color: #4fc3f7; " +
                                                     "-fx-border-radius: 4px; -fx-background-radius: 4px; -fx-border-width: 2px;";
    public static final String DIALOG_STYLE = "-fx-background-color: #2e2e2e;";
    
    // Updated styles for better readability - light background options
    public static final String TEXT_FIELD_STYLE_LIGHT = "-fx-background-color: #B2B2B2; -fx-text-fill: #353535;";
    public static final String TEXT_AREA_STYLE_LIGHT = "-fx-control-inner-background: #B2B2B2; -fx-text-fill: #353535;";
    
    // Dark background options with white text
    public static final String TEXT_FIELD_STYLE_DARK = "-fx-background-color: #3A3A3A; -fx-text-fill: #FFFFFF;";
    public static final String TEXT_AREA_STYLE_DARK = "-fx-control-inner-background: #3A3A3A; -fx-text-fill: #FFFFFF;";
    
    // Use dark style by default for consistency
    public static final String TEXT_FIELD_STYLE = TEXT_FIELD_STYLE_DARK;
    public static final String TEXT_AREA_STYLE = TEXT_AREA_STYLE_DARK;
    
    public static final String LABEL_STYLE = "-fx-text-fill: #D9D9D9;";
    public static final String LABEL_BOLD_STYLE = "-fx-text-fill: #D9D9D9; -fx-font-weight: bold;";
    
    // Enhanced ComboBox styling
    public static final String COMBO_BOX_STYLE = "-fx-background-color: #3A3A3A; -fx-text-fill: white; -fx-mark-color: white;";
    public static final String COMBO_BOX_POPUP_STYLE = "-fx-background-color: #2e2e2e; -fx-background-radius: 0; -fx-background-insets: 0;";
    public static final String COMBO_BOX_CELL_STYLE = "-fx-text-fill: white; -fx-background-color: #3A3A3A;";
    public static final String COMBO_BOX_CELL_HOVER_STYLE = "-fx-text-fill: white; -fx-background-color: #4e4e4e;";
    
    // Helper method to create menu buttons with consistent styling
    public static Button createMenuButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(40);
        button.setStyle(BUTTON_STYLE);
                
        // Add hover effect
        button.setOnMouseEntered(_ -> 
            button.setStyle(BUTTON_HOVER_STYLE));
        button.setOnMouseExited(_ -> 
            button.setStyle(BUTTON_STYLE));
                    
        // Add focus indicator for keyboard navigation
        button.focusedProperty().addListener((_, _, newVal) -> {
            if (newVal) {
                button.setStyle(BUTTON_FOCUSED_STYLE);
            } else {
                button.setStyle(BUTTON_STYLE);
            }
        });
        
        // Set the action
        button.setOnAction(action);
        
        return button;
    }
    
    // Helper method to enhance dialogs with keyboard navigation
    public static void enhanceDialogWithKeyboardNavigation(Dialog<?> dialog) {
        // Apply dialog styling
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(DIALOG_STYLE);
        
        // Make sure dialog header text is white
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
        
        // Apply styling to all text fields in the dialog
        for (Node node : dialogPane.lookupAll(".text-field")) {
            if (node instanceof TextField) {
                node.setStyle(TEXT_FIELD_STYLE_DARK);
            }
        }
        
        // Apply styling to all text areas in the dialog
        for (Node node : dialogPane.lookupAll(".text-area")) {
            if (node instanceof TextArea) {
                ((TextArea) node).setStyle(TEXT_AREA_STYLE_DARK);
            }
        }
        
        // Apply styling to all combo boxes in the dialog
        for (Node node : dialogPane.lookupAll(".combo-box")) {
            if (node instanceof ComboBox) {
                applyDarkThemeComboBoxStyle((ComboBox<?>) node);
            }
        }
        
        dialog.getDialogPane().getScene().getWindow().addEventFilter(
                javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                Node focusedNode = dialog.getDialogPane().getScene().getFocusOwner();
                
                if (focusedNode instanceof ComboBox) {
                    // Don't process TAB when ComboBox is showing 
                    ComboBox<?> comboBox = (ComboBox<?>) focusedNode;
                    if (comboBox.isShowing()) {
                        return;
                    }
                }
                
                // Don't process TAB in TextAreas where Tab is useful
                if (focusedNode instanceof TextArea && !event.isControlDown()) {
                    return;
                }
            }
            
            // Handle Enter key to activate buttons
            if (event.getCode() == KeyCode.ENTER && dialog.getDialogPane().getScene().getFocusOwner() instanceof Button) {
                Button button = (Button) dialog.getDialogPane().getScene().getFocusOwner();
                button.fire();
                event.consume();
            }
        });
        
        // Style all buttons
        dialog.getDialogPane().lookupAll(".button").forEach(node -> {
            if (node instanceof Button) {
                Button button = (Button) node;
                button.setStyle(BUTTON_STYLE);
                
                // Add hover effect
                button.setOnMouseEntered(_ -> 
                    button.setStyle(BUTTON_HOVER_STYLE));
                button.setOnMouseExited(_ -> 
                    button.setStyle(BUTTON_STYLE));
                
                // Add focus indicator
                button.focusedProperty().addListener((_, _, newVal) -> {
                    if (newVal) {
                        button.setStyle(BUTTON_FOCUSED_STYLE);
                    } else {
                        button.setStyle(BUTTON_STYLE);
                    }
                });
            }
        });
        
        // Set initial focus on the appropriate control
        dialog.setOnShown(_ -> {
            // Find first input control
            Node firstField = findFirstFocusableControl(dialog);
            
            // Set focus on found control
            if (firstField != null) {
                firstField.requestFocus();
            }
        });
    }
    
    private static Node findFirstFocusableControl(Dialog<?> dialog) {
        // Try to find the first editable TextField
        for (Node node : dialog.getDialogPane().lookupAll(".text-field")) {
            if (node instanceof TextField && node.isVisible() && !((TextField) node).isDisabled()) {
                return node;
            }
        }
        
        // If no TextField found, try ComboBox
        for (Node node : dialog.getDialogPane().lookupAll(".combo-box")) {
            if (node instanceof ComboBox && node.isVisible() && !((ComboBox<?>) node).isDisabled()) {
                return node;
            }
        }
        
        // If no ComboBox found, try first button
        for (Node node : dialog.getDialogPane().lookupAll(".button")) {
            if (node instanceof Button && node.isVisible() && !((Button) node).isDisabled()) {
                return node;
            }
        }
        
        return null;
    }
    
    /**
     * Applies appropriate styling to a TextField component in a dark theme UI
     * @param textField The TextField to style
     */
    public static void applyDarkThemeTextFieldStyle(TextField textField) {
        textField.setStyle(TEXT_FIELD_STYLE_DARK);
    }
    
    /**
     * Applies appropriate styling to a TextArea component in a dark theme UI
     * @param textArea The TextArea to style
     */
    public static void applyDarkThemeTextAreaStyle(TextArea textArea) {
        textArea.setStyle(TEXT_AREA_STYLE_DARK);
    }
    
    /**
     * Applies appropriate styling to a ComboBox component in a dark theme UI
     * @param comboBox The ComboBox to style
     */
    public static <T> void applyDarkThemeComboBoxStyle(ComboBox<T> comboBox) {
        // Apply base style to the combo box itself
        comboBox.setStyle(COMBO_BOX_STYLE);
        
        // Make sure the text inside the ComboBox has correct style
        if (comboBox.getButtonCell() != null) {
            comboBox.getButtonCell().setStyle(COMBO_BOX_CELL_STYLE);
        }
        
        // Style the combo box list cells - using proper generic typing
        comboBox.setCellFactory(new Callback<ListView<T>, ListCell<T>>() {
            @Override
            public ListCell<T> call(ListView<T> lv) {
                return new ListCell<T>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                            setBackground(null);
                        } else {
                            setText(item.toString());
                            setStyle(COMBO_BOX_CELL_STYLE);
                        }
                        
                        // Add hover effect for better UX
                        setOnMouseEntered(_ -> {
                            if (!empty && item != null) {
                                setStyle(COMBO_BOX_CELL_HOVER_STYLE);
                            }
                        });
                        
                        setOnMouseExited(_ -> {
                            if (!empty && item != null) {
                                setStyle(COMBO_BOX_CELL_STYLE);
                            }
                        });
                    }
                };
            }
        });
        
        // Add CSS to make the dropdown popup match the dark theme
        // This needs to be done after the ComboBox is shown
        comboBox.setOnShown(_ -> {
            // Get the popup and apply style
            if (comboBox.getSkin() != null) {
                Node popup = comboBox.lookup(".combo-box-popup");
                if (popup != null) {
                    popup.setStyle(COMBO_BOX_POPUP_STYLE);
                }
                
                // Style the list view inside the popup
                Node listView = comboBox.lookup(".combo-box-popup .list-view");
                if (listView != null) {
                    listView.setStyle("-fx-background-color: #2e2e2e;");
                }
                
                // Style the scroll bars
                for (Node scrollBar : comboBox.lookupAll(".combo-box-popup .scroll-bar")) {
                    if (scrollBar != null) {
                        scrollBar.setStyle("-fx-background-color: #2e2e2e; -fx-background: #2e2e2e;");
                    }
                }
            }
        });
    }
    
    public static void styleDialogButtons(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();
        for (ButtonType buttonType : dialogPane.getButtonTypes()) {
            Button button = (Button) dialogPane.lookupButton(buttonType);
            if (button != null) {
                button.setStyle(BUTTON_STYLE);
                button.setOnMouseEntered(e -> button.setStyle(BUTTON_HOVER_STYLE));
                button.setOnMouseExited(e -> button.setStyle(BUTTON_STYLE));
                button.focusedProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal) {
                        button.setStyle(BUTTON_FOCUSED_STYLE);
                    } else {
                        button.setStyle(BUTTON_STYLE);
                    }
                });
            }
        }
    }
    
    public static void styleTextField(TextField textField) {
        textField.setStyle(TEXT_FIELD_STYLE);
    }
    
    public static void styleTextArea(TextArea textArea) {
        textArea.setStyle(TEXT_AREA_STYLE);
    }
    
    public static void styleDialog(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle(DIALOG_STYLE);
    }
    
    /**
     * Styles a button as a secondary action button (e.g., for proposal voting)
     */
    public static void styleSecondaryButton(Button button) {
        button.setStyle("-fx-background-color: #444a6d; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-border-radius: 5px; -fx-border-color: #7a88c3; -fx-border-width: 1.5px;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #5a5f87; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-border-radius: 5px; -fx-border-color: #a3b1e6; -fx-border-width: 2px;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #444a6d; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-border-radius: 5px; -fx-border-color: #7a88c3; -fx-border-width: 1.5px;"));
        button.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                button.setStyle("-fx-background-color: #5a5f87; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-border-radius: 5px; -fx-border-color: #a3b1e6; -fx-border-width: 2px;");
            } else {
                button.setStyle("-fx-background-color: #444a6d; -fx-text-fill: #fff; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-border-radius: 5px; -fx-border-color: #7a88c3; -fx-border-width: 1.5px;");
            }
        });
    }
}
