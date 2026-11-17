package com.pawsitiveconnect;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RescueDispatchController {

    @FXML private VBox dispatchListContainer;
    @FXML private TextField searchField;
    @FXML private Label streamCountBadge;
    @FXML private VBox noResultsBox;

    // Bento Counts
    @FXML private Label criticalCountLabel;
    @FXML private Label highPriorityLabel;
    @FXML private Label unitsDeployedLabel;
    @FXML private Label pendingCountLabel;

    private final List<RescueDispatch> masterIncidents = new ArrayList<>();

    @FXML
    public void initialize() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAndDisplayIncidents());
        }
        loadDispatchesFromBackend();
    }

    @FXML
    public void loadDispatchesFromBackend() {
        masterIncidents.clear();
        if (streamCountBadge != null) {
            streamCountBadge.setText("Syncing incidents...");
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/rescue"))
                    .GET()
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(responseBody -> {
                        try {
                            JSONArray incidentsArray = new JSONArray(responseBody);
                            for (int i = 0; i < incidentsArray.length(); i++) {
                                JSONObject obj = incidentsArray.getJSONObject(i);

                                int id = obj.optInt("id", i + 1);
                                String reporter = obj.optString("reporter_name", obj.optString("reporterName", "Hotline Caller"));
                                String contact = obj.optString("contact_number", obj.optString("contactNumber", "N/A"));
                                String desc = obj.optString("animal_description", obj.optString("animalDescription", "Animal in distress"));
                                String loc = obj.optString("location_coordinates", obj.optString("locationCoordinates", "40.7128, -74.0060"));
                                String urgency = obj.optString("urgency_level", obj.optString("urgencyLevel", "Medium"));
                                String status = obj.optString("status", "REPORTED");

                                masterIncidents.add(new RescueDispatch(id, reporter, contact, desc, loc, urgency, status));
                            }

                            Platform.runLater(() -> {
                                updateCounts();
                                filterAndDisplayIncidents();
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            if (streamCountBadge != null) {
                                streamCountBadge.setText("Offline (Local Cache Active)");
                            }
                        });
                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterAndDisplayIncidents() {
        if (dispatchListContainer == null) return;
        dispatchListContainer.getChildren().clear();

        String query = searchField == null || searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        int visibleCount = 0;

        for (RescueDispatch incident : masterIncidents) {
            boolean matches = query.isEmpty()
                    || incident.getAnimalDescription().toLowerCase().contains(query)
                    || incident.getLocationCoordinates().toLowerCase().contains(query)
                    || incident.getUrgencyLevel().toLowerCase().contains(query)
                    || String.valueOf(incident.getId()).contains(query);

            if (matches) {
                dispatchListContainer.getChildren().add(createIncidentCard(incident));
                visibleCount++;
            }
        }

        boolean hasResults = visibleCount > 0;
        if (noResultsBox != null) {
            noResultsBox.setVisible(!hasResults);
            noResultsBox.setManaged(!hasResults);
        }
        dispatchListContainer.setVisible(hasResults);
        dispatchListContainer.setManaged(hasResults);

        if (streamCountBadge != null) {
            streamCountBadge.setText("Showing " + visibleCount + " recorded events");
        }
    }

    private HBox createIncidentCard(RescueDispatch incident) {
        HBox card = new HBox(16.0);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("incident-card");

        // 1. Left Icon Container
        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(52, 52);
        iconBox.setMinSize(52, 52);
        iconBox.setStyle("-fx-background-color: #ffdad4; -fx-background-radius: 16px;");

        String descLower = incident.getAnimalDescription().toLowerCase();
        String emoji = "\uD83D\uDC3E"; // 🐾
        if (descLower.contains("dog") || descLower.contains("puppy")) emoji = "\uD83D\uDC36"; // 🐶
        else if (descLower.contains("cat") || descLower.contains("kitten")) emoji = "\uD83D\uDC31"; // 🐱
        else if (descLower.contains("hawk") || descLower.contains("bird")) emoji = "\uD83E\uDD85"; // 🦅
        else if (descLower.contains("horse")) emoji = "\uD83D\uDC34"; // 🐴

        Label iconLabel = new Label(emoji);
        iconLabel.setStyle("-fx-font-size: 24px;");
        iconBox.getChildren().add(iconLabel);

        // 2. Middle Content VBox
        VBox contentBox = new VBox(4.0);
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        HBox tagRow = new HBox(8.0);
        tagRow.setAlignment(Pos.CENTER_LEFT);

        Label idLabel = new Label("Incident #RD-2026-" + String.format("%03d", incident.getId()));
        idLabel.setStyle("-fx-font-weight: 800; -fx-font-size: 15px; -fx-text-fill: #171E13;");

        Label urgencyPill = new Label(incident.getUrgencyLevel() + " Urgency");
        if (incident.getUrgencyLevel().equalsIgnoreCase("Critical")) {
            urgencyPill.getStyleClass().add("urgency-critical");
        } else if (incident.getUrgencyLevel().equalsIgnoreCase("High")) {
            urgencyPill.getStyleClass().add("urgency-high");
        } else {
            urgencyPill.getStyleClass().add("urgency-medium");
        }

        Label statusPill = new Label(incident.getStatus());
        if (incident.getStatus().equalsIgnoreCase("DISPATCHED")) {
            statusPill.setText("\u25CF DISPATCHED");
            statusPill.getStyleClass().add("pill-green");
        } else {
            statusPill.setText("\u23F1 REPORTED");
            statusPill.getStyleClass().add("pill-gray");
        }

        tagRow.getChildren().addAll(idLabel, urgencyPill, statusPill);

        HBox descRow = new HBox(4.0);
        Label reportPrefix = new Label("Report:");
        reportPrefix.setStyle("-fx-font-weight: bold; -fx-text-fill: #58413D; -fx-font-size: 13px;");
        Label descText = new Label(incident.getAnimalDescription());
        descText.setStyle("-fx-text-fill: #171E13; -fx-font-size: 13px;");
        descRow.getChildren().addAll(reportPrefix, descText);

        HBox metaRow = new HBox(8.0);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        HBox locChip = new HBox(4.0);
        locChip.setAlignment(Pos.CENTER_LEFT);
        locChip.setStyle("-fx-background-color: #eef6e4; -fx-background-radius: 999px; -fx-padding: 2px 10px;");
        Label pin = new Label("\uD83D\uDCCD"); // 📍
        Label locLabel = new Label(incident.getLocationCoordinates());
        locLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #171E13;");
        locChip.getChildren().addAll(pin, locLabel);

        Label dot = new Label("\u2022"); // •
        Label timeLabel = new Label(incident.getStatus().equalsIgnoreCase("DISPATCHED") ? "Unit En Route (ETA 4m)" : "Reported Just Now");
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #2C694E; -fx-font-weight: bold;");
        metaRow.getChildren().addAll(locChip, dot, timeLabel);
        contentBox.getChildren().addAll(tagRow, descRow, metaRow);

        // 3. Right Action Box: Mini Map Preview + Role-Based Action Button
        HBox actionBox = new HBox(12.0);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        VBox miniMap = new VBox(2.0);
        miniMap.getStyleClass().add("map-preview-box");
        Label mapIcon = new Label("\uD83D\uDCCD");
        mapIcon.setStyle("-fx-font-size: 18px;");
        Label gpsLabel = new Label("GPS TELEMETRY");
        gpsLabel.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #2C694E;");
        miniMap.getChildren().addAll(mapIcon, gpsLabel);
        miniMap.setOnMouseClicked(e -> openMapModal(incident.getLocationCoordinates(), "Incident #RD-2026-" + String.format("%03d", incident.getId()) + ": " + incident.getAnimalDescription()));

        int roleId = UserSession.getRoleId();
        boolean isAdmin = (roleId == 1 || roleId == 2);

        if (incident.getStatus().equalsIgnoreCase("REPORTED")) {
            if (isAdmin) {
                Button dispatchBtn = new Button("Assign Unit");
                dispatchBtn.getStyleClass().add("primary-button");
                dispatchBtn.setOnAction(e -> openAssignModal(incident));
                actionBox.getChildren().addAll(miniMap, dispatchBtn);
            } else {
                Label userStatusLbl = new Label("\u23F1 Dispatch In Review");
                userStatusLbl.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-padding: 8 14; -fx-background-radius: 999px; -fx-font-weight: bold; -fx-font-size: 11.5px;");
                actionBox.getChildren().addAll(miniMap, userStatusLbl);
            }
        } else {
            Button mapBtn = new Button("View Map");
            mapBtn.setStyle("-fx-background-color: #2c694e; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 999px; -fx-padding: 9px 18px; -fx-cursor: hand;");
            mapBtn.setOnAction(e -> openMapModal(incident.getLocationCoordinates(), "Live Tracking: Incident #RD-2026-" + String.format("%03d", incident.getId())));
            actionBox.getChildren().addAll(miniMap, mapBtn);
        }

        card.getChildren().addAll(iconBox, contentBox, actionBox);
        return card;
    }

    private void updateCounts() {
        long critical = masterIncidents.stream().filter(i -> i.getUrgencyLevel().equalsIgnoreCase("Critical")).count();
        long high = masterIncidents.stream().filter(i -> i.getUrgencyLevel().equalsIgnoreCase("High")).count();
        long dispatched = masterIncidents.stream().filter(i -> i.getStatus().equalsIgnoreCase("DISPATCHED")).count();
        long pending = masterIncidents.stream().filter(i -> i.getStatus().equalsIgnoreCase("REPORTED")).count();

        if (criticalCountLabel != null) criticalCountLabel.setText(String.valueOf(critical));
        if (highPriorityLabel != null) highPriorityLabel.setText(String.valueOf(high));
        if (unitsDeployedLabel != null) unitsDeployedLabel.setText(String.valueOf(dispatched));
        if (pendingCountLabel != null) pendingCountLabel.setText(String.valueOf(pending));
    }

    private void openAssignModal(RescueDispatch incident) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Field Deployment");
        dialog.setHeaderText("Assign Response Unit to Incident #RD-2026-" + String.format("%03d", incident.getId()) + "\n" + incident.getAnimalDescription());

        ButtonType confirmBtnType = new ButtonType("Confirm Dispatch", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmBtnType, ButtonType.CANCEL);

        VBox content = new VBox(10.0);
        content.setPadding(new Insets(15, 20, 10, 10));

        Label title = new Label("Select Available Mobile Unit:");
        title.setStyle("-fx-font-weight: bold; -fx-text-fill: #171E13;");

        ToggleGroup unitGroup = new ToggleGroup();

        RadioButton u1 = new RadioButton("Unit 03 - K9 Emergency Transport\nOfficer M. Hayes • 1.2 miles away [Ready]");
        u1.setToggleGroup(unitGroup);
        u1.setSelected(true);
        u1.getStyleClass().add("unit-option-card");
        u1.setMaxWidth(Double.MAX_VALUE);

        RadioButton u2 = new RadioButton("Unit 07 - Large Animal & Equine Rig\nOfficer D. Chen • 3.8 miles away [Ready]");
        u2.setToggleGroup(unitGroup);
        u2.getStyleClass().add("unit-option-card");
        u2.setMaxWidth(Double.MAX_VALUE);

        RadioButton u3 = new RadioButton("Unit 12 - Rapid Urban Medic\nDr. E. Thorne • 0.8 miles away [Ready]");
        u3.setToggleGroup(unitGroup);
        u3.getStyleClass().add("unit-option-card");
        u3.setMaxWidth(Double.MAX_VALUE);

        content.getChildren().addAll(title, u1, u2, u3);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> {
            if (btn == confirmBtnType) {
                RadioButton selected = (RadioButton) unitGroup.getSelectedToggle();
                return selected != null ? selected.getText().split("\n")[0] : "Unit Assigned";
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(unitName -> {
            incident.setStatus("DISPATCHED");
            updateCounts();
            filterAndDisplayIncidents();

            try {
                HttpClient client = HttpClient.newHttpClient();
                JSONObject updatePayload = new JSONObject();
                updatePayload.put("status", "DISPATCHED");
                updatePayload.put("assigned_unit", unitName);

                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/rescue/" + incident.getId() + "/assign"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(updatePayload.toString()))
                        .build();

                client.sendAsync(req, HttpResponse.BodyHandlers.ofString());
            } catch (Exception ignored) {}
        });
    }

    private void openMapModal(String coordinates, String title) {
        Alert mapDialog = new Alert(Alert.AlertType.INFORMATION);
        mapDialog.setTitle("Field GPS Telemetry");
        mapDialog.setHeaderText(title);

        VBox box = new VBox(12.0);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(15));

        StackPane mapGraphic = new StackPane();
        mapGraphic.setPrefSize(380, 160);
        mapGraphic.setStyle("-fx-background-color: #e9f1de; -fx-background-radius: 14px;");

        VBox innerPin = new VBox(4.0);
        innerPin.setAlignment(Pos.CENTER);
        Label pinLabel = new Label("📍");
        pinLabel.setStyle("-fx-font-size: 32px;");
        Label coordsLabel = new Label(coordinates);
        coordsLabel.setStyle("-fx-background-color: #ffffff; -fx-padding: 4px 12px; -fx-background-radius: 999px; -fx-font-weight: bold;");
        innerPin.getChildren().addAll(pinLabel, coordsLabel);
        mapGraphic.getChildren().add(innerPin);

        Label satLabel = new Label("\uD83D\uDCE1 Live GPS Satellite sync active • Coordinate Locked");
        satLabel.setStyle("-fx-text-fill: #2C694E; -fx-font-weight: bold; -fx-font-size: 11.5px;");

        box.getChildren().addAll(mapGraphic, satLabel);
        mapDialog.getDialogPane().setContent(box);
        mapDialog.showAndWait();
    }

    @FXML
    public void handleOpenNewEmergencyModal(ActionEvent event) {
        Dialog<JSONObject> dialog = new Dialog<>();
        dialog.setTitle("Fast Intake Hotline");
        dialog.setHeaderText("Create a new emergency field dispatch report.");

        ButtonType submitBtnType = new ButtonType("Submit & Queue Dispatch", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 40, 10, 10));

        TextField descField = new TextField();
        descField.setPromptText("e.g. Stray dog stuck in canal, visible injury");

        ComboBox<String> urgencyCombo = new ComboBox<>();
        urgencyCombo.getItems().addAll("Critical", "High", "Medium");
        urgencyCombo.getSelectionModel().select("High");

        TextField locField = new TextField();
        locField.setPromptText("e.g. 40.7306, -73.9352");

        TextField reporterField = new TextField();
        reporterField.setPromptText("e.g. Jane Smith");

        TextField phoneField = new TextField();
        phoneField.setPromptText("e.g. +1 (555) 019-2831");

        grid.add(new Label("Incident Summary:"), 0, 0);
        grid.add(descField, 1, 0);
        grid.add(new Label("Urgency Level:"), 0, 1);
        grid.add(urgencyCombo, 1, 1);
        grid.add(new Label("Location / Coordinates:"), 0, 2);
        grid.add(locField, 1, 2);
        grid.add(new Label("Caller Name:"), 0, 3);
        grid.add(reporterField, 1, 3);
        grid.add(new Label("Contact Phone:"), 0, 4);
        grid.add(phoneField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == submitBtnType) {
                String desc = descField.getText().trim();
                String loc = locField.getText().trim();
                if (!desc.isEmpty() && !loc.isEmpty()) {
                    JSONObject payload = new JSONObject();
                    payload.put("animal_description", desc);
                    payload.put("urgency_level", urgencyCombo.getValue());
                    payload.put("location_coordinates", loc);
                    payload.put("reporter_name", reporterField.getText().trim().isEmpty() ? "Hotline Caller" : reporterField.getText().trim());
                    payload.put("contact_number", phoneField.getText().trim().isEmpty() ? "N/A" : phoneField.getText().trim());
                    payload.put("status", "REPORTED");
                    return payload;
                }
            }
            return null;
        });

        Optional<JSONObject> result = dialog.showAndWait();
        result.ifPresent(jsonObj -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/rescue"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonObj.toString()))
                        .build();

                client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(res -> Platform.runLater(this::loadDispatchesFromBackend));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}