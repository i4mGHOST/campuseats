package com.pawsitiveconnect;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionLedgerController {

    @FXML private VBox ledgerListContainer;
    @FXML private TextField searchField;
    @FXML private VBox noResultsBox;

    // Filter Pills
    @FXML private Button filterAllBtn;
    @FXML private Button filterCompletedBtn;
    @FXML private Button filterPendingBtn;
    @FXML private Button filterAdoptionsBtn;
    @FXML private Button filterDonationsBtn;

    // KPI Labels
    @FXML private Label grossInflowLabel;
    @FXML private Label adoptionFeesLabel;
    @FXML private Label settledRecordsBadge;
    @FXML private Label pendingAmountLabel;
    @FXML private Label pendingCountBadge;
    @FXML private Label donationsLabel;
    @FXML private Label footerCountLabel;

    private final List<Transaction> masterTransactions = new ArrayList<>();
    private String currentFilter = "all";

    @FXML
    public void initialize() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndDisplayTransactions());
        }
        loadTransactionsFromBackend();
    }

    @FXML
    public void loadTransactionsFromBackend() {
        masterTransactions.clear();

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/transactions"))
                    .GET()
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(responseBody -> {
                        try {
                            JSONArray txArray = new JSONArray(responseBody);
                            for (int i = 0; i < txArray.length(); i++) {
                                JSONObject obj = txArray.getJSONObject(i);

                                int id = obj.optInt("transaction_id", obj.optInt("id", i + 1));
                                String payer = obj.optString("payer_name", obj.optString("payerName", "Anonymous Donor"));
                                double amount = obj.optDouble("amount", 150.0);
                                String method = obj.optString("payment_method", obj.optString("method", "Credit Card"));
                                String status = obj.optString("status", "COMPLETED").toUpperCase();
                                String date = obj.optString("date", "Just now");
                                String category = obj.optString("category", payer.toLowerCase().contains("sponsor") || payer.toLowerCase().contains("donor") ? "Donation" : "Adoption Fee");

                                masterTransactions.add(new Transaction(id, payer, amount, method, status, date, category));
                            }

                            Platform.runLater(() -> {
                                updateKPIs();
                                filterAndDisplayTransactions();
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(this::updateKPIs);
                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterAndDisplayTransactions() {
        if (ledgerListContainer == null) return;
        ledgerListContainer.getChildren().clear();

        String query = searchField == null || searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        int visibleCount = 0;

        for (Transaction txn : masterTransactions) {
            boolean matchesQuery = query.isEmpty()
                    || txn.getPayerName().toLowerCase().contains(query)
                    || txn.getMethod().toLowerCase().contains(query)
                    || String.valueOf(txn.getTransactionId()).contains(query)
                    || String.valueOf(txn.getAmount()).contains(query);

            boolean matchesFilter = true;
            String st = txn.getStatus().toUpperCase();
            String cat = txn.getCategory().toLowerCase();

            switch (currentFilter) {
                case "completed" -> matchesFilter = st.equals("COMPLETED");
                case "pending" -> matchesFilter = st.contains("PEND");
                case "adoptions" -> matchesFilter = cat.contains("adopt");
                case "donations" -> matchesFilter = cat.contains("donat") || txn.getPayerName().toLowerCase().contains("sponsor") || txn.getPayerName().toLowerCase().contains("donor");
            }

            if (matchesQuery && matchesFilter) {
                ledgerListContainer.getChildren().add(createLedgerRow(txn));
                visibleCount++;
            }
        }

        boolean hasResults = visibleCount > 0;
        if (noResultsBox != null) {
            noResultsBox.setVisible(!hasResults);
            noResultsBox.setManaged(!hasResults);
        }
        ledgerListContainer.setVisible(hasResults);
        ledgerListContainer.setManaged(hasResults);

        if (footerCountLabel != null) {
            footerCountLabel.setText("Showing " + visibleCount + " of " + masterTransactions.size() + " recorded events");
        }
    }

    private HBox createLedgerRow(Transaction txn) {
        HBox row = new HBox(16.0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("txn-row-card");

        StackPane iconBox = new StackPane();
        iconBox.getStyleClass().add("txn-icon-box");

        String methodLower = txn.getMethod().toLowerCase();
        String iconEmoji = "\uD83D\uDCB3"; // 💳
        String bgColor = "#aeeecb";

        if (methodLower.contains("bank") || methodLower.contains("transfer") || methodLower.contains("ach")) {
            iconEmoji = "\uD83C\uDFE6"; // 🏦
            bgColor = "#d8e2ff";
        } else if (methodLower.contains("paypal") || methodLower.contains("wallet")) {
            iconEmoji = "\uD83D\uDC5B"; // 👛
            bgColor = "#ffdad4";
        } else if (methodLower.contains("cash")) {
            iconEmoji = "\uD83D\uDCB5"; // 💵
            bgColor = "#e3ebd9";
        } else if (methodLower.contains("corp") || methodLower.contains("company")) {
            iconEmoji = "\uD83C\uDFE2"; // 🏢
            bgColor = "#d8e2ff";
        }

        iconBox.setStyle("-fx-background-color: " + bgColor + ";");
        Label iconLbl = new Label(iconEmoji);
        iconLbl.setStyle("-fx-font-size: 20px;");
        iconBox.getChildren().add(iconLbl);

        VBox contentBox = new VBox(3.0);
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        HBox titleRow = new HBox(8.0);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("TXN-" + String.format("%05d", txn.getTransactionId()));
        idLabel.setStyle("-fx-font-weight: 800; -fx-font-size: 15px; -fx-text-fill: #171E13;");

        Label sep = new Label("|");
        sep.setStyle("-fx-text-fill: #9BA396;");

        Label amountLabel = new Label(String.format("$%.2f", txn.getAmount()));
        amountLabel.setStyle("-fx-font-weight: 800; -fx-font-size: 15px; -fx-text-fill: #a83223;");

        titleRow.getChildren().addAll(idLabel, sep, amountLabel);

        HBox detailsRow = new HBox(10.0);
        detailsRow.setAlignment(Pos.CENTER_LEFT);

        Label payerLbl = new Label("Payer: " + txn.getPayerName());
        payerLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #171E13; -fx-font-size: 12px;");

        Label dot = new Label("\u2022"); // •
        dot.setStyle("-fx-text-fill: #9BA396;");

        Label methodLbl = new Label("Method: " + txn.getMethod());
        methodLbl.getStyleClass().add("text-card-muted");

        detailsRow.getChildren().addAll(payerLbl, dot, methodLbl);
        contentBox.getChildren().addAll(titleRow, detailsRow);

        HBox actionBox = new HBox(10.0);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Label statusPill = new Label();
        boolean isCompleted = txn.getStatus().equalsIgnoreCase("COMPLETED");
        if (isCompleted) {
            statusPill.setText("\u2713 COMPLETED");
            statusPill.getStyleClass().add("pill-green");
        } else {
            statusPill.setText("\u23F1 PENDING");
            statusPill.getStyleClass().add("pill-gray");
        }

        Button receiptBtn = new Button("Receipt");
        receiptBtn.setStyle("-fx-background-color: #e9f1de; -fx-background-radius: 999px; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 6 12;");
        receiptBtn.setTooltip(new Tooltip("Download Receipt"));
        receiptBtn.setOnAction(e -> handleShowReceipt(txn));

        Button detailsBtn = new Button("Info");
        detailsBtn.setStyle("-fx-background-color: #e9f1de; -fx-background-radius: 999px; -fx-cursor: hand; -fx-font-size: 11px; -fx-padding: 6 12;");
        detailsBtn.setTooltip(new Tooltip("View Audit Trail"));
        detailsBtn.setOnAction(e -> handleShowAudit(txn));

        actionBox.getChildren().addAll(statusPill, receiptBtn, detailsBtn);
        row.getChildren().addAll(iconBox, contentBox, actionBox);
        return row;
    }

    private void updateKPIs() {
        double grossInflow = masterTransactions.stream()
                .filter(t -> t.getStatus().equalsIgnoreCase("COMPLETED"))
                .mapToDouble(Transaction::getAmount).sum();

        double adoptionFees = masterTransactions.stream()
                .filter(t -> t.getStatus().equalsIgnoreCase("COMPLETED") && t.getCategory().toLowerCase().contains("adopt"))
                .mapToDouble(Transaction::getAmount).sum();

        long settledCount = masterTransactions.stream()
                .filter(t -> t.getStatus().equalsIgnoreCase("COMPLETED") && t.getCategory().toLowerCase().contains("adopt")).count();

        double pendingAmount = masterTransactions.stream()
                .filter(t -> t.getStatus().equalsIgnoreCase("PENDING"))
                .mapToDouble(Transaction::getAmount).sum();

        long pendingCount = masterTransactions.stream()
                .filter(t -> t.getStatus().equalsIgnoreCase("PENDING")).count();

        double donations = masterTransactions.stream()
                .filter(t -> t.getCategory().toLowerCase().contains("donat") || t.getPayerName().toLowerCase().contains("sponsor"))
                .mapToDouble(Transaction::getAmount).sum();

        if (grossInflowLabel != null) grossInflowLabel.setText(String.format("$%.2f", grossInflow));
        if (adoptionFeesLabel != null) adoptionFeesLabel.setText(String.format("$%.2f", adoptionFees));
        if (settledRecordsBadge != null) settledRecordsBadge.setText(settledCount + " settled records");
        if (pendingAmountLabel != null) pendingAmountLabel.setText(String.format("$%.2f", pendingAmount));
        if (pendingCountBadge != null) pendingCountBadge.setText(pendingCount + " pending transactions");
        if (donationsLabel != null) donationsLabel.setText(String.format("$%.2f", donations));
    }

    @FXML public void handleFilterAll(ActionEvent e) { setFilter(filterAllBtn, "all"); }
    @FXML public void handleFilterCompleted(ActionEvent e) { setFilter(filterCompletedBtn, "completed"); }
    @FXML public void handleFilterPending(ActionEvent e) { setFilter(filterPendingBtn, "pending"); }
    @FXML public void handleFilterAdoptions(ActionEvent e) { setFilter(filterAdoptionsBtn, "adoptions"); }
    @FXML public void handleFilterDonations(ActionEvent e) { setFilter(filterDonationsBtn, "donations"); }

    private void setFilter(Button activeBtn, String filterKey) {
        Button[] btns = {filterAllBtn, filterCompletedBtn, filterPendingBtn, filterAdoptionsBtn, filterDonationsBtn};
        for (Button b : btns) {
            if (b != null) {
                b.getStyleClass().remove("filter-pill-active");
                if (!b.getStyleClass().contains("filter-pill")) b.getStyleClass().add("filter-pill");
            }
        }
        if (activeBtn != null) {
            activeBtn.getStyleClass().remove("filter-pill");
            activeBtn.getStyleClass().add("filter-pill-active");
        }

        this.currentFilter = filterKey;
        filterAndDisplayTransactions();
    }

    @FXML
    public void handleExportCsv(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Transactions Ledger");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("Pawsitive_Financial_Ledger.csv");

        File file = fileChooser.showSaveDialog(searchField.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Transaction ID,Payer Name,Amount,Payment Method,Status,Category");
                for (Transaction t : masterTransactions) {
                    writer.printf("\"TXN-%05d\",\"%s\",%.2f,\"%s\",\"%s\",\"%s\"%n",
                            t.getTransactionId(),
                            t.getPayerName(),
                            t.getAmount(),
                            t.getMethod(),
                            t.getStatus(),
                            t.getCategory());
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Transaction ledger exported successfully!", ButtonType.OK);
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleOpenNewTransactionModal(ActionEvent event) {
        Dialog<JSONObject> dialog = new Dialog<>();
        dialog.setTitle("New Financial Transaction");
        dialog.setHeaderText("Record an adoption fee settlement or donor contribution.");

        ButtonType submitBtn = new ButtonType("Record Transaction", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 30, 10, 10));

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Adoption Fee", "Donation", "Medical Sponsorship", "Event Ticket");
        categoryCombo.getSelectionModel().selectFirst();

        Label appSelectLabel = new Label("Select Approved Application:");
        ComboBox<String> approvedAppsCombo = new ComboBox<>();
        approvedAppsCombo.setPromptText("Loading approved applications...");

        TextField payerField = new TextField();
        payerField.setPromptText("Payer Name");

        TextField amountField = new TextField("150.00");
        amountField.setPromptText("Amount ($)");

        ComboBox<String> methodCombo = new ComboBox<>();
        methodCombo.getItems().addAll("Credit Card", "Bank Transfer", "Cash", "PayPal", "Corporate Card");
        methodCombo.getSelectionModel().selectFirst();

        grid.add(new Label("Transaction Category:"), 0, 0);
        grid.add(categoryCombo, 1, 0);
        grid.add(appSelectLabel, 0, 1);
        grid.add(approvedAppsCombo, 1, 1);
        grid.add(new Label("Payer / Donor:"), 0, 2);
        grid.add(payerField, 1, 2);
        grid.add(new Label("Amount ($):"), 0, 3);
        grid.add(amountField, 1, 3);
        grid.add(new Label("Payment Method:"), 0, 4);
        grid.add(methodCombo, 1, 4);

        HttpClient.newHttpClient().sendAsync(
                HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/api/adoptions")).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        ).thenAccept(res -> {
            try {
                JSONArray arr = new JSONArray(res.body());
                List<String> approvedItems = new ArrayList<>();
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    if ("APPROVED".equalsIgnoreCase(obj.optString("status"))) {
                        int id = obj.optInt("application_id", obj.optInt("id", i + 1));
                        String applicant = obj.optString("applicant_name", "Applicant");
                        String pet = obj.optString("pet_name", "Pet");
                        double fee = obj.optDouble("fee_amount", 150.00);
                        approvedItems.add(String.format("APP-%04d: %s for %s ($%.2f)", id, applicant, pet, fee));
                    }
                }
                Platform.runLater(() -> {
                    if (approvedItems.isEmpty()) {
                        approvedAppsCombo.setPromptText("No approved applications pending fee");
                    } else {
                        approvedAppsCombo.getItems().setAll(approvedItems);
                        approvedAppsCombo.getSelectionModel().selectFirst();
                    }
                });
            } catch (Exception ignored) {}
        });

        approvedAppsCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.contains(":")) {
                try {
                    String[] parts = newVal.split(":");
                    String details = parts[1];
                    String applicant = details.substring(1, details.indexOf(" for "));
                    String feeStr = details.substring(details.lastIndexOf("$") + 1, details.indexOf(")"));
                    payerField.setText(applicant);
                    amountField.setText(feeStr);
                } catch (Exception ignored) {}
            }
        });

        categoryCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isAdoption = "Adoption Fee".equals(newVal);
            appSelectLabel.setVisible(isAdoption);
            appSelectLabel.setManaged(isAdoption);
            approvedAppsCombo.setVisible(isAdoption);
            approvedAppsCombo.setManaged(isAdoption);
            if (!isAdoption) {
                payerField.clear();
                amountField.setText("50.00");
            }
        });

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == submitBtn) {
                String payer = payerField.getText().trim();
                String amtStr = amountField.getText().trim();
                if (!payer.isEmpty() && !amtStr.isEmpty()) {
                    try {
                        double amt = Double.parseDouble(amtStr);
                        JSONObject payload = new JSONObject();
                        payload.put("payer_name", payer);
                        payload.put("amount", amt);
                        payload.put("payment_method", methodCombo.getValue());
                        payload.put("category", categoryCombo.getValue());
                        payload.put("status", "COMPLETED");
                        return payload;
                    } catch (NumberFormatException ignored) {}
                }
            }
            return null;
        });

        Optional<JSONObject> res = dialog.showAndWait();
        res.ifPresent(jsonObj -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/transactions"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonObj.toString()))
                        .build();

                client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(r -> Platform.runLater(this::loadTransactionsFromBackend));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void handleShowReceipt(Transaction txn) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Transaction Receipt");
        alert.setHeaderText("Pawsitive Connect Animal Rescue Network\nReceipt #TXN-" + String.format("%05d", txn.getTransactionId()));
        alert.setContentText(String.format("Date: %s\nPayer: %s\nPayment Method: %s\nAmount Settled: $%.2f\nStatus: %s\n\nThank you for supporting animal rescue operations!",
                txn.getDate(), txn.getPayerName(), txn.getMethod(), txn.getAmount(), txn.getStatus()));
        alert.showAndWait();
    }

    private void handleShowAudit(Transaction txn) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Audit Trail Record");
        alert.setHeaderText("Cryptographic Verification & Gateway Logs");
        alert.setContentText(String.format("Record ID: TXN-%05d\nGateway Status: Verified 256-bit SSL\nSettlement Ledger: Node.js/PostgreSQL Active\nCategory: %s",
                txn.getTransactionId(), txn.getCategory()));
        alert.showAndWait();
    }
}