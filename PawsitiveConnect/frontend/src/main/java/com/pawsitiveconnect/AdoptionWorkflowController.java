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

public class AdoptionWorkflowController {

    @FXML private VBox adoptionListContainer;
    @FXML private TextField searchField;
    @FXML private VBox noResultsBox;
    @FXML private Button exportLedgerBtn;

    // Filter Chips
    @FXML private Button filterAllBtn;
    @FXML private Button filterApprovedBtn;
    @FXML private Button filterPendingBtn;
    @FXML private Button filterCompletedBtn;

    // KPI Counters
    @FXML private Label totalInReviewLabel;
    @FXML private Label pendingReviewSubLabel;
    @FXML private Label approvedCountLabel;
    @FXML private Label completedCountLabel;

    // Fee Clearing Engine
    @FXML private Label pendingFeeSummaryLabel;
    @FXML private VBox feeSummaryContainer;

    private final List<AdoptionApplication> masterList = new ArrayList<>();
    private String currentFilter = "all";

    @FXML
    public void initialize() {
        int roleId = UserSession.getRoleId();
        boolean isAdmin = (roleId == 1 || roleId == 2);
        if (exportLedgerBtn != null) {
            exportLedgerBtn.setVisible(isAdmin);
            exportLedgerBtn.setManaged(isAdmin);
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndDisplayApplications());
        }
        loadAdoptionsFromBackend();
    }

    @FXML
    public void loadAdoptionsFromBackend() {
        masterList.clear();

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/adoptions"))
                    .GET()
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(responseBody -> {
                        try {
                            JSONArray applicationsArray = new JSONArray(responseBody);
                            for (int i = 0; i < applicationsArray.length(); i++) {
                                JSONObject obj = applicationsArray.getJSONObject(i);

                                int id = obj.optInt("application_id", obj.optInt("id", i + 1));
                                String applicant = obj.optString("applicant_name", obj.optString("applicantName", "Applicant"));
                                String pet = obj.optString("pet_name", obj.optString("petName", "Shelter Pet"));
                                String status = obj.optString("status", "PENDING").toUpperCase();
                                String date = obj.optString("application_date", obj.optString("applicationDate", "Recently"));
                                double fee = obj.optDouble("fee_amount", 150.00);
                                String location = obj.optString("location", "Metro Shelter Ward");
                                String notes = obj.optString("notes", "Residential & Tenant Permission Verified");

                                masterList.add(new AdoptionApplication(id, applicant, pet, status, date, fee, location, notes));
                            }

                            Platform.runLater(() -> {
                                updateKPIsAndFeeEngine();
                                filterAndDisplayApplications();
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(this::updateKPIsAndFeeEngine);
                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterAndDisplayApplications() {
        if (adoptionListContainer == null) return;
        adoptionListContainer.getChildren().clear();

        String query = searchField == null || searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        int visibleCount = 0;

        for (AdoptionApplication app : masterList) {
            boolean matchesQuery = query.isEmpty()
                    || app.getApplicantName().toLowerCase().contains(query)
                    || app.getPetName().toLowerCase().contains(query)
                    || String.valueOf(app.getApplicationId()).contains(query);

            boolean matchesFilter = true;
            String st = app.getStatus().toUpperCase();

            switch (currentFilter) {
                case "approved" -> matchesFilter = st.equals("APPROVED");
                case "pending" -> matchesFilter = st.contains("PEND") || st.contains("REVIEW");
                case "completed" -> matchesFilter = st.equals("COMPLETED") || st.equals("ADOPTED");
            }

            if (matchesQuery && matchesFilter) {
                adoptionListContainer.getChildren().add(createApplicationCard(app));
                visibleCount++;
            }
        }

        boolean hasResults = visibleCount > 0;
        if (noResultsBox != null) {
            noResultsBox.setVisible(!hasResults);
            noResultsBox.setManaged(!hasResults);
        }
        adoptionListContainer.setVisible(hasResults);
        adoptionListContainer.setManaged(hasResults);
    }

    private HBox createApplicationCard(AdoptionApplication app) {
        HBox card = new HBox(16.0);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("app-card");

        StackPane avatarBox = new StackPane();
        avatarBox.setPrefSize(58, 58);
        avatarBox.setMinSize(58, 58);
        avatarBox.setStyle("-fx-background-color: #e9f1de; -fx-background-radius: 16px;");

        Label petEmoji = new Label(app.getPetName().toLowerCase().contains("cat") ? "\uD83D\uDC31" : "\uD83D\uDC36");
        petEmoji.setStyle("-fx-font-size: 26px;");
        avatarBox.getChildren().add(petEmoji);

        StackPane dot = new StackPane();
        dot.setPrefSize(12, 12);
        dot.setMaxSize(12, 12);
        String dotColor = app.getStatus().equals("APPROVED") ? "#2c694e" : (app.getStatus().contains("PEND") ? "#d0684b" : "#4a5568");
        dot.setStyle("-fx-background-color: " + dotColor + "; -fx-background-radius: 999px; -fx-border-color: #ffffff; -fx-border-width: 2px; -fx-border-radius: 999px;");
        StackPane.setAlignment(dot, Pos.BOTTOM_RIGHT);
        avatarBox.getChildren().add(dot);

        VBox contentBox = new VBox(4.0);
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        HBox tagRow = new HBox(8.0);
        tagRow.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("Application #APP-" + String.format("%04d", app.getApplicationId()));
        idLabel.setStyle("-fx-font-weight: 800; -fx-font-size: 13.5px; -fx-text-fill: #a83223;");

        Label statusPill = new Label();
        if (app.getStatus().equals("APPROVED")) {
            statusPill.setText("\u25CF APPROVED");
            statusPill.getStyleClass().add("pill-green");
        } else if (app.getStatus().contains("PEND")) {
            statusPill.setText("\u23F1 PENDING");
            statusPill.getStyleClass().add("pill-gray");
        } else {
            statusPill.setText("\u2713 " + app.getStatus());
            statusPill.getStyleClass().add("pill-blue");
        }

        Label timeLabel = new Label("\u2022 " + app.getApplicationDate());
        timeLabel.getStyleClass().add("text-card-muted");

        tagRow.getChildren().addAll(idLabel, statusPill, timeLabel);

        HBox titleRow = new HBox(8.0);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label applicantLbl = new Label("Applicant: " + app.getApplicantName());
        applicantLbl.setStyle("-fx-font-weight: 800; -fx-font-size: 14.5px; -fx-text-fill: #171E13;");

        Label sep = new Label("|");
        sep.setStyle("-fx-text-fill: #9BA396;");

        Label petLbl = new Label("Pet: " + app.getPetName());
        petLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2C694E;");
        titleRow.getChildren().addAll(applicantLbl, sep, petLbl);

        HBox metaRow = new HBox(12.0);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label locLbl = new Label("\uD83D\uDCCD " + app.getLocation());
        locLbl.getStyleClass().add("text-card-muted");

        Label noteLbl = new Label("\uD83C\uDFE0 " + app.getNotes());
        noteLbl.getStyleClass().add("text-card-muted");

        Label feeLbl = new Label(String.format("\uD83D\uDCB5 Adoption Fee: $%.2f", app.getFeeAmount()));
        feeLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #171E13; -fx-font-size: 11.5px;");

        metaRow.getChildren().addAll(locLbl, noteLbl, feeLbl);
        contentBox.getChildren().addAll(tagRow, titleRow, metaRow);

        HBox actionBox = new HBox(8.0);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Button reviewBtn = new Button("Review File");
        reviewBtn.getStyleClass().add("secondary-action-button");
        reviewBtn.setOnAction(e -> openReviewFileDialog(app));

        Button primaryBtn = new Button();
        int role = UserSession.getRoleId();
        boolean isAdmin = (role == 1 || role == 2);
        String st = app.getStatus();

        if ("APPROVED".equalsIgnoreCase(st)) {
            if (!isAdmin) {
                primaryBtn.setText("Process Fee");
                primaryBtn.getStyleClass().add("primary-button");
                primaryBtn.setOnAction(e -> openFeeSettlementModal(app));
            } else {
                primaryBtn.setText("Awaiting Payment");
                primaryBtn.setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-font-weight: bold; -fx-background-radius: 999px; -fx-padding: 9px 16px;");
                primaryBtn.setDisable(true);
            }
        } else if ("COMPLETED".equalsIgnoreCase(st) || "ADOPTED".equalsIgnoreCase(st)) {
            primaryBtn.setText("\u2713 Fee Settled");
            primaryBtn.setStyle("-fx-background-color: #aeeecb; -fx-text-fill: #225a40; -fx-font-weight: bold; -fx-background-radius: 999px; -fx-padding: 9px 16px;");
            primaryBtn.setDisable(true);
        } else {
            if (isAdmin) {
                primaryBtn.setText("Approve App");
                primaryBtn.setStyle("-fx-background-color: #2c694e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 999px; -fx-padding: 9px 18px; -fx-cursor: hand;");
                primaryBtn.setOnAction(e -> approveApplicationWithFee(app.getApplicationId(), app.getFeeAmount()));
            } else {
                primaryBtn.setText("Awaiting Admin Review");
                primaryBtn.setStyle("-fx-background-color: #e9f1de; -fx-text-fill: #6e7968; -fx-font-weight: bold; -fx-background-radius: 999px; -fx-padding: 9px 16px;");
                primaryBtn.setDisable(true);
            }
        }

        actionBox.getChildren().addAll(reviewBtn, primaryBtn);
        card.getChildren().addAll(avatarBox, contentBox, actionBox);
        return card;
    }

    private void updateKPIsAndFeeEngine() {
        long inReview = masterList.stream().filter(a -> a.getStatus().contains("PEND")).count();
        long approved = masterList.stream().filter(a -> a.getStatus().equals("APPROVED")).count();
        long completed = masterList.stream().filter(a -> a.getStatus().equals("COMPLETED") || a.getStatus().equals("ADOPTED")).count();

        if (totalInReviewLabel != null) totalInReviewLabel.setText(String.valueOf(inReview + approved));
        if (pendingReviewSubLabel != null) pendingReviewSubLabel.setText(inReview + " Pending Admin Review");
        if (approvedCountLabel != null) approvedCountLabel.setText(String.valueOf(approved));
        if (completedCountLabel != null) completedCountLabel.setText(String.valueOf(completed + 18));

        double totalPendingFee = masterList.stream()
                .filter(a -> a.getStatus().equals("APPROVED"))
                .mapToDouble(AdoptionApplication::getFeeAmount)
                .sum();

        if (pendingFeeSummaryLabel != null) {
            pendingFeeSummaryLabel.setText(String.format("%d applications are ready to complete transactions. Total pending collection: $%.2f",
                    approved, totalPendingFee));
        }

        if (feeSummaryContainer != null) {
            feeSummaryContainer.getChildren().clear();
            masterList.stream().filter(a -> a.getStatus().equals("APPROVED")).limit(3).forEach(app -> {
                HBox item = new HBox();
                item.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 10px; -fx-padding: 8px 12px;");
                Label name = new Label(app.getApplicantName() + " (" + app.getPetName() + ")");
                name.setStyle("-fx-font-size: 11.5px; -fx-text-fill: #171E13;");
                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);
                Label fee = new Label(String.format("$%.2f", app.getFeeAmount()));
                fee.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C694E; -fx-font-size: 11.5px;");
                item.getChildren().addAll(name, sp, fee);
                feeSummaryContainer.getChildren().add(item);
            });
        }
    }

    private void approveApplicationWithFee(int appId, double currentFee) {
        TextInputDialog feeDialog = new TextInputDialog(String.format("%.2f", currentFee));
        feeDialog.setTitle("Approve Application");
        feeDialog.setHeaderText("Set adoption settlement fee for Application #APP-" + String.format("%04d", appId));
        feeDialog.setContentText("Adoption Fee ($):");

        Optional<String> res = feeDialog.showAndWait();
        if (res.isPresent()) {
            double fee = currentFee;
            try {
                fee = Double.parseDouble(res.get().trim());
            } catch (Exception ignored) {}

            try {
                HttpClient client = HttpClient.newHttpClient();
                JSONObject payload = new JSONObject();
                payload.put("status", "APPROVED");
                payload.put("fee_amount", fee);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/adoptions/" + appId + "/status"))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(payload.toString()))
                        .build();

                client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(response -> Platform.runLater(this::loadAdoptionsFromBackend));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void openFeeSettlementModal(AdoptionApplication app) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Payment Settlement");
        dialog.setHeaderText("Process Adoption Fee for Application #APP-" + String.format("%04d", app.getApplicationId()));

        ButtonType confirmBtnType = new ButtonType("Confirm & Issue Receipt", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmBtnType, ButtonType.CANCEL);

        VBox content = new VBox(12.0);
        content.setPadding(new Insets(15, 30, 10, 10));

        VBox summaryBox = new VBox(4.0);
        summaryBox.setStyle("-fx-background-color: #eef6e4; -fx-background-radius: 12px; -fx-padding: 12px;");
        Label applicantLbl = new Label("Applicant: " + app.getApplicantName());
        applicantLbl.setStyle("-fx-font-weight: bold;");
        Label petLbl = new Label("Pet: " + app.getPetName());
        Label amountLbl = new Label(String.format("Settlement Amount: $%.2f", app.getFeeAmount()));
        amountLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #2c694e;");
        summaryBox.getChildren().addAll(applicantLbl, petLbl, amountLbl);

        Label methodTitle = new Label("Select Payment Method:");
        methodTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #171E13;");

        ToggleGroup payGroup = new ToggleGroup();
        RadioButton r1 = new RadioButton("\uD83D\uDCB3 Credit / Debit Card");
        r1.setToggleGroup(payGroup);
        r1.setSelected(true);
        RadioButton r2 = new RadioButton("\uD83C\uDFE6 Bank ACH Wire Transfer");
        r2.setToggleGroup(payGroup);
        RadioButton r3 = new RadioButton("\uD83D\uDCB5 Cash / In-Person Shelter Desk");
        r3.setToggleGroup(payGroup);

        content.getChildren().addAll(summaryBox, methodTitle, r1, r2, r3);
        dialog.getDialogPane().setContent(content);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == confirmBtnType) {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/adoptions/" + app.getApplicationId() + "/status"))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString("{\"status\": \"COMPLETED\"}"))
                        .build();

                client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(r -> Platform.runLater(this::loadAdoptionsFromBackend));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void openReviewFileDialog(AdoptionApplication app) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Adoption Application Dossier");
        alert.setHeaderText("Applicant: " + app.getApplicantName() + " | Pet: " + app.getPetName());
        alert.setContentText(String.format("Application ID: #APP-%04d\nStatus: %s\nSubmission Date: %s\nShelter Location: %s\n\nVetting Status:\n%s\n\nAdoption Fee: $%.2f",
                app.getApplicationId(),
                app.getStatus(),
                app.getApplicationDate(),
                app.getLocation(),
                app.getNotes(),
                app.getFeeAmount()));
        alert.showAndWait();
    }

    public void openNewIntakeForPet(String petName) {
        handleOpenNewIntakeModal(null);
    }

    @FXML
    public void handleOpenNewIntakeModal(ActionEvent event) {
        Dialog<JSONObject> dialog = new Dialog<>();
        dialog.setTitle("New Adoption Intake");
        dialog.setHeaderText("Register prospective adopter into the evaluation queue.");

        ButtonType submitBtn = new ButtonType("Submit Intake", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 40, 10, 10));

        TextField applicantField = new TextField();
        applicantField.setPromptText("e.g. Jessica Davis");

        TextField petField = new TextField();
        petField.setPromptText("e.g. Charlie");

        ComboBox<String> homeTypeCombo = new ComboBox<>();
        homeTypeCombo.getItems().addAll("Single-Family Home with Yard", "Apartment / Condominium", "Rural Farm / Acreage");
        homeTypeCombo.getSelectionModel().selectFirst();

        TextField feeField = new TextField("150.00");

        grid.add(new Label("Applicant Full Name:"), 0, 0);
        grid.add(applicantField, 1, 0);
        grid.add(new Label("Desired Pet:"), 0, 1);
        grid.add(petField, 1, 1);
        grid.add(new Label("Housing Arrangement:"), 0, 2);
        grid.add(homeTypeCombo, 1, 2);
        grid.add(new Label("Adoption Fee ($):"), 0, 3);
        grid.add(feeField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == submitBtn) {
                String applicant = applicantField.getText().trim();
                String pet = petField.getText().trim();
                if (!applicant.isEmpty() && !pet.isEmpty()) {
                    JSONObject payload = new JSONObject();
                    payload.put("applicant_name", applicant);
                    payload.put("pet_name", pet);
                    payload.put("status", "PENDING");
                    payload.put("housing_type", homeTypeCombo.getValue());
                    try {
                        payload.put("fee_amount", Double.parseDouble(feeField.getText().trim()));
                    } catch (NumberFormatException e) {
                        payload.put("fee_amount", 150.00);
                    }
                    return payload;
                }
            }
            return null;
        });

        Optional<JSONObject> res = dialog.showAndWait();
        res.ifPresent(jsonObj -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/adoptions"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonObj.toString()))
                        .build();

                client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(r -> Platform.runLater(this::loadAdoptionsFromBackend));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @FXML
    public void handleExportLedger(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Adoptions Ledger");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("Pawsitive_Adoptions_Ledger.csv");

        File file = fileChooser.showSaveDialog(adoptionListContainer.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Application ID,Applicant Name,Pet Name,Status,Submission Date,Fee Amount,Location");
                for (AdoptionApplication a : masterList) {
                    writer.printf("\"APP-%04d\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f,\"%s\"%n",
                            a.getApplicationId(),
                            a.getApplicantName(),
                            a.getPetName(),
                            a.getStatus(),
                            a.getApplicationDate(),
                            a.getFeeAmount(),
                            a.getLocation());
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Adoptions ledger exported successfully!", ButtonType.OK);
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML public void handleFilterAll(ActionEvent e) { setFilter(filterAllBtn, "all"); }
    @FXML public void handleFilterApproved(ActionEvent e) { setFilter(filterApprovedBtn, "approved"); }
    @FXML public void handleFilterPending(ActionEvent e) { setFilter(filterPendingBtn, "pending"); }
    @FXML public void handleFilterCompleted(ActionEvent e) { setFilter(filterCompletedBtn, "completed"); }

    private void setFilter(Button activeBtn, String filterKey) {
        Button[] btns = {filterAllBtn, filterApprovedBtn, filterPendingBtn, filterCompletedBtn};
        for (Button b : btns) {
            if (b != null) {
                b.getStyleClass().remove("filter-chip-active");
                if (!b.getStyleClass().contains("filter-chip")) b.getStyleClass().add("filter-chip");
            }
        }
        if (activeBtn != null) {
            activeBtn.getStyleClass().remove("filter-chip");
            activeBtn.getStyleClass().add("filter-chip-active");
        }

        this.currentFilter = filterKey;
        filterAndDisplayApplications();
    }

    @FXML public void handleBatchProcess(ActionEvent e) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Batch processing fee settlements for approved applications...", ButtonType.OK);
        alert.showAndWait();
    }
}