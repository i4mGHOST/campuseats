package com.pawsitiveconnect;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ProcessPaymentController {

    @FXML private Label applicationDetailsLabel;
    @FXML private Label summaryAmountLabel;
    @FXML private TextField payerNameField;
    @FXML private TextField amountField;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private Label statusLabel;

    private int currentApplicationId = 0;
    private Runnable onSuccessCallback;

    @FXML
    public void initialize() {
        paymentMethodCombo.setItems(FXCollections.observableArrayList(
                "Credit Card", "Bank Transfer", "Cash", "Debit Card", "PayPal"
        ));
        paymentMethodCombo.getSelectionModel().selectFirst();
    }

    public void setApplicationData(int appId, String applicantName, Runnable onSuccessCallback) {
        this.currentApplicationId = appId;
        this.applicationDetailsLabel.setText("App #APP-" + String.format("%04d", appId) + " | " + applicantName);
        this.payerNameField.setText(applicantName);
        this.amountField.setText("150.00");
        if (summaryAmountLabel != null) {
            this.summaryAmountLabel.setText("$150.00");
        }
        this.onSuccessCallback = onSuccessCallback;

        // Auto-update summary label when amount field changes
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (summaryAmountLabel != null && newVal != null && !newVal.isBlank()) {
                summaryAmountLabel.setText("$" + newVal.trim());
            }
        });
    }

    @FXML
    public void handleProcessPayment(ActionEvent event) {
        String payer = payerNameField.getText().trim();
        String amountStr = amountField.getText().trim();
        String method = paymentMethodCombo.getValue();

        if (payer.isEmpty() || amountStr.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: #D93025;");
            statusLabel.setText("Please fill in all required fields.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            sendPaymentToBackend(payer, amount, method);
        } catch (NumberFormatException e) {
            statusLabel.setStyle("-fx-text-fill: #D93025;");
            statusLabel.setText("Invalid amount format. Please enter a valid number.");
        }
    }

    private void sendPaymentToBackend(String payer, double amount, String method) {
        statusLabel.setStyle("-fx-text-fill: #B8860B;");
        statusLabel.setText("Processing payment settlement...");

        String jsonPayload = String.format(
                "{\"application_id\": %d, \"payer_name\": \"%s\", \"amount\": %.2f, \"payment_method\": \"%s\"}",
                currentApplicationId, payer.replace("\"", "\\\""), amount, method
        );

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/transactions/process"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        javafx.application.Platform.runLater(() -> {
                            if (response.statusCode() == 200 || response.statusCode() == 201) {
                                if (onSuccessCallback != null) onSuccessCallback.run();
                                closeDialog();
                            } else {
                                statusLabel.setStyle("-fx-text-fill: #D93025;");
                                statusLabel.setText("Server response: " + response.body());
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        javafx.application.Platform.runLater(() -> {
                            statusLabel.setStyle("-fx-text-fill: #D93025;");
                            statusLabel.setText("Could not connect to payment gateway.");
                        });
                        return null;
                    });

        } catch (Exception e) {
            statusLabel.setStyle("-fx-text-fill: #D93025;");
            statusLabel.setText("Failed to dispatch request.");
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) payerNameField.getScene().getWindow();
        stage.close();
    }
}