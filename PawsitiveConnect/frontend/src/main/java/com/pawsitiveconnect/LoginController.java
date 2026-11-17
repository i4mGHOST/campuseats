package com.pawsitiveconnect;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private Button togglePasswordBtn;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Label statusLabel;

    private boolean isPasswordVisible = false;

    @FXML
    public void initialize() {
        // Keeps the hidden PasswordField and visible TextField in sync
        if (passwordTextField != null && passwordField != null) {
            passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        }
    }

    /**
     * Toggles between masked PasswordField and plain-text TextField
     */
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
    public void handleLogin(ActionEvent event) {
        String email = emailField.getText().trim();
        // Since fields are bound bidirectionally, passwordField always has current text
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: #D93025;");
            statusLabel.setText("Please enter both email and password.");
            return;
        }

        statusLabel.setStyle("-fx-text-fill: #B8860B;");
        statusLabel.setText("Authenticating with server...");

        // Safely construct JSON using JSONObject so quotes/symbols in passwords won't break it
        JSONObject requestBody = new JSONObject();
        requestBody.put("email", email);
        requestBody.put("password", password);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    Platform.runLater(() -> {
                        if (response.statusCode() == 200) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response.body());
                                String token = jsonResponse.getString("token");

                                JSONObject userObj = jsonResponse.getJSONObject("user");
                                int roleId = userObj.getInt("role_id");

                                // 1. Save credentials to your existing UserSession
                                UserSession.setSession(token, email, roleId);

                                // 2. Navigate to your existing Main Dashboard
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("MainLayout.fxml"));
                                Parent root = loader.load();

                                Stage stage = (Stage) emailField.getScene().getWindow();

                                // Standard widescreen dashboard dimensions
                                Scene scene = new Scene(root, 1180, 760);
                                stage.setScene(scene);
                                stage.setTitle("Pawsitive Connect - Dashboard");
                                stage.centerOnScreen();

                            } catch (Exception e) {
                                statusLabel.setStyle("-fx-text-fill: #D93025;");
                                statusLabel.setText("Error loading dashboard view.");
                                e.printStackTrace();
                            }
                        } else {
                            statusLabel.setStyle("-fx-text-fill: #D93025;");
                            statusLabel.setText("Invalid credentials. Please try again.");
                        }
                    });
                })
                .exceptionally(e -> {
                    Platform.runLater(() -> {
                        statusLabel.setStyle("-fx-text-fill: #D93025;");
                        statusLabel.setText("Error: Cannot connect to backend server at localhost:8080.");
                    });
                    return null;
                });
    }

    @FXML
    public void handleForgotPassword(ActionEvent event) {
        statusLabel.setStyle("-fx-text-fill: #2D6A4F;");
        statusLabel.setText("Contact your shelter coordinator to reset credentials.");
    }

    @FXML
    public void handleGoToRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RegisterView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            // Matches modern registration card size
            stage.setScene(new Scene(root, 1080, 700));
            stage.setTitle("Pawsitive Connect - Register");
            stage.centerOnScreen();
        } catch (IOException e) {
            statusLabel.setStyle("-fx-text-fill: #D93025;");
            statusLabel.setText("Error opening registration page.");
            e.printStackTrace();
        }
    }
}