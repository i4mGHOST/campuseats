package com.pawsitiveconnect;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Button togglePasswordBtn;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private CheckBox termsCheckbox;
    @FXML private Label statusLabel;

    private boolean isPasswordVisible = false;

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList(
                "Adopter (Normal User)",
                "Volunteer / Foster Parent",
                "Shelter Staff / Vet Lead",
                "System Admin"
        ));
        roleComboBox.getSelectionModel().selectFirst();

        // Bind visibility for password field
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    public void handleTogglePassword(ActionEvent event) {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordBtn.setText("🙈 Hide");
        } else {
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            togglePasswordBtn.setText("👁 Show");
        }
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        String selectedRole = roleComboBox.getValue();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: #D93025;");
            statusLabel.setText("Please fill out all required fields.");
            return;
        }

        if (!termsCheckbox.isSelected()) {
            statusLabel.setStyle("-fx-text-fill: #D93025;");
            statusLabel.setText("You must accept the Animal Welfare Pledge to continue.");
            return;
        }

        statusLabel.setStyle("-fx-text-fill: #B8860B;");
        statusLabel.setText("Registering account...");

        // Map UI string to role_id in PostgreSQL:
        // 1 = System Admin, 2 = Shelter Staff, 3 = Volunteer, 4 = Adopter
        int roleId = 4;
        if ("System Admin".equals(selectedRole)) {
            roleId = 1;
        } else if ("Shelter Staff / Vet Lead".equals(selectedRole)) {
            roleId = 2;
        } else if ("Volunteer / Foster Parent".equals(selectedRole)) {
            roleId = 3;
        }

        String jsonPayload = String.format(
                "{\"full_name\":\"%s\", \"email\":\"%s\", \"password\":\"%s\", \"phone\":\"%s\", \"role_id\":%d}",
                escapeJson(fullName),
                escapeJson(email),
                escapeJson(password),
                escapeJson(phone),
                roleId
        );

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.statusCode() == 201) {
                            statusLabel.setStyle("-fx-text-fill: #2D6A4F;");
                            statusLabel.setText("Registration successful! You can now log in.");
                            fullNameField.clear();
                            emailField.clear();
                            phoneField.clear();
                            passwordField.clear();
                            roleComboBox.getSelectionModel().selectFirst();
                        } else {
                            statusLabel.setStyle("-fx-text-fill: #D93025;");
                            statusLabel.setText("Registration failed: " + response.body());
                        }
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        statusLabel.setStyle("-fx-text-fill: #D93025;");
                        statusLabel.setText("Error: Cannot connect to server at localhost:8080.");
                    });
                    return null;
                });
    }

    @FXML
    public void handleGoToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) fullNameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1080, 700));
            stage.setTitle("Pawsitive Connect - Login");
        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setStyle("-fx-text-fill: #D93025;");
            statusLabel.setText("Error loading LoginView.fxml");
        }
    }

    private String escapeJson(String input) {
        return input.replace("\"", "\\\"");
    }
}