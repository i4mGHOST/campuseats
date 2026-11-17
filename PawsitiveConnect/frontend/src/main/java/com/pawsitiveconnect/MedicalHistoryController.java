package com.pawsitiveconnect;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Optional;

public class MedicalHistoryController {

    @FXML private TableView<MedicalRecord> medicalTable;
    @FXML private TableColumn<MedicalRecord, String> petNameCol;
    @FXML private TableColumn<MedicalRecord, String> treatmentCol;
    @FXML private TableColumn<MedicalRecord, String> dateCol;
    @FXML private TableColumn<MedicalRecord, String> vetCol;
    @FXML private TableColumn<MedicalRecord, String> notesCol;
    @FXML private TableColumn<MedicalRecord, Void> actionsCol;

    @FXML private TextField searchField;
    @FXML private Label recordCountBadge;
    @FXML private Label footerCountLabel;
    @FXML private Label criticalCountLabel;
    @FXML private Label pendingBoostersLabel;
    @FXML private Button addMedicalEntryBtn;

    private final ObservableList<MedicalRecord> masterList = FXCollections.observableArrayList();
    private FilteredList<MedicalRecord> filteredList;

    @FXML
    public void initialize() {
        int roleId = UserSession.getRoleId();
        boolean isAdmin = (roleId == 1 || roleId == 2);

        if (addMedicalEntryBtn != null) {
            addMedicalEntryBtn.setVisible(isAdmin);
            addMedicalEntryBtn.setManaged(isAdmin);
        }

        setupColumnCellFactories();

        filteredList = new FilteredList<>(masterList, p -> true);
        medicalTable.setItems(filteredList);

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredList.setPredicate(record -> {
                    if (newVal == null || newVal.trim().isEmpty()) return true;
                    String query = newVal.trim().toLowerCase();
                    return record.getPetName().toLowerCase().contains(query)
                            || record.getTreatment().toLowerCase().contains(query)
                            || record.getVeterinarian().toLowerCase().contains(query);
                });
                updateCounts();
            });
        }

        loadMedicalDataFromBackend();
    }

    private void setupColumnCellFactories() {
        // 1. Pet Name Cell
        petNameCol.setCellValueFactory(new PropertyValueFactory<>("petName"));
        petNameCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String petName, boolean empty) {
                super.updateItem(petName, empty);
                if (empty || petName == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(10);
                    box.setAlignment(Pos.CENTER_LEFT);

                    String initial = petName.substring(0, 1).toUpperCase();
                    StackPane avatar = new StackPane();
                    avatar.setPrefSize(34, 34);
                    avatar.setMinSize(34, 34);
                    avatar.setStyle("-fx-background-color: #e3ebd9; -fx-background-radius: 999px;");

                    Label initialLabel = new Label(initial);
                    initialLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #171E13; -fx-font-size: 12px;");
                    avatar.getChildren().add(initialLabel);

                    VBox textBox = new VBox(1);
                    Label nameLbl = new Label(petName);
                    nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #171E13; -fx-font-size: 13px;");

                    Label subLbl = new Label("Intake Patient");
                    subLbl.setStyle("-fx-text-fill: #6E7968; -fx-font-size: 10.5px;");

                    textBox.getChildren().addAll(nameLbl, subLbl);
                    box.getChildren().addAll(avatar, textBox);
                    setGraphic(box);
                }
            }
        });

        // 2. Treatment / Vaccine Cell
        treatmentCol.setCellValueFactory(new PropertyValueFactory<>("treatment"));
        treatmentCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String treatment, boolean empty) {
                super.updateItem(treatment, empty);
                if (empty || treatment == null) {
                    setGraphic(null);
                } else {
                    Label pill = new Label("\u25CF " + treatment);
                    String lower = treatment.toLowerCase();
                    if (lower.contains("splint") || lower.contains("x-ray") || lower.contains("surg") || lower.contains("fract")) {
                        pill.getStyleClass().add("pill-red");
                    } else if (lower.contains("deworm") || lower.contains("rabies") || lower.contains("vaccine")) {
                        pill.getStyleClass().add("pill-green");
                    } else {
                        pill.getStyleClass().add("pill-blue");
                    }
                    setGraphic(pill);
                }
            }
        });

        // 3. Date Administered Cell
        dateCol.setCellValueFactory(new PropertyValueFactory<>("dateAdministered"));
        dateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setGraphic(null);
                } else {
                    VBox box = new VBox(1);
                    box.setAlignment(Pos.CENTER_LEFT);
                    Label dateLbl = new Label(date);
                    dateLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #171E13;");
                    Label shiftLbl = new Label("Clinical Shift");
                    shiftLbl.setStyle("-fx-text-fill: #6E7968; -fx-font-size: 10.5px;");
                    box.getChildren().addAll(dateLbl, shiftLbl);
                    setGraphic(box);
                }
            }
        });

        // 4. Veterinarian Cell
        vetCol.setCellValueFactory(new PropertyValueFactory<>("veterinarian"));
        vetCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String vet, boolean empty) {
                super.updateItem(vet, empty);
                if (empty || vet == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8);
                    box.setAlignment(Pos.CENTER_LEFT);

                    StackPane badge = new StackPane();
                    badge.setPrefSize(26, 26);
                    badge.setStyle("-fx-background-color: #d8e2ff; -fx-background-radius: 999px;");
                    Label initialLbl = new Label("VT");
                    initialLbl.setStyle("-fx-font-size: 9.5px; -fx-font-weight: bold; -fx-text-fill: #004494;");
                    badge.getChildren().add(initialLbl);

                    VBox info = new VBox(1);
                    Label vetLbl = new Label(vet);
                    vetLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #171E13; -fx-font-size: 12px;");
                    Label titleLbl = new Label("Attending Vet");
                    titleLbl.setStyle("-fx-text-fill: #6E7968; -fx-font-size: 10.5px;");

                    info.getChildren().addAll(vetLbl, titleLbl);
                    box.getChildren().addAll(badge, info);
                    setGraphic(box);
                }
            }
        });

        // 5. Notes Cell
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));
        notesCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String notes, boolean empty) {
                super.updateItem(notes, empty);
                if (empty || notes == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label(notes.isBlank() ? "Routine examination completed." : notes);
                    lbl.setStyle("-fx-text-fill: #171E13; -fx-font-size: 12px;");
                    lbl.setWrapText(true);
                    setGraphic(lbl);
                }
            }
        });

        // 6. Action Buttons Cell with clean Vector Document Icon (replaces scrambled "📄")
        actionsCol.setCellFactory(col -> new TableCell<>() {
            private final SVGPath docIcon = new SVGPath();
            private final Button reportBtn = new Button();
            private final HBox btnBox = new HBox(4, reportBtn);

            {
                docIcon.setContent("M14 2H6c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z");
                docIcon.setFill(Color.web("#2C694E"));
                docIcon.setScaleX(0.7);
                docIcon.setScaleY(0.7);

                reportBtn.setGraphic(docIcon);
                reportBtn.setStyle("-fx-background-color: #e9f1de; -fx-background-radius: 999px; -fx-cursor: hand; -fx-padding: 6 10;");
                reportBtn.setTooltip(new Tooltip("View Clinical Summary"));

                btnBox.setAlignment(Pos.CENTER);

                reportBtn.setOnAction(e -> {
                    MedicalRecord rec = getTableView().getItems().get(getIndex());
                    showClinicalSummary(rec);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnBox);
            }
        });
    }

    @FXML
    public void loadMedicalDataFromBackend() {
        masterList.clear();
        if (recordCountBadge != null) recordCountBadge.setText("Syncing...");

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/medical"))
                    .GET()
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(responseBody -> {
                        try {
                            JSONArray recordsArray = new JSONArray(responseBody);
                            ObservableList<MedicalRecord> loaded = FXCollections.observableArrayList();

                            for (int i = 0; i < recordsArray.length(); i++) {
                                JSONObject obj = recordsArray.getJSONObject(i);

                                int id = obj.optInt("id", i + 1);
                                String rawDate = obj.optString("date_administered", "2026-09-20");
                                String cleanDate = rawDate.length() > 10 ? rawDate.substring(0, 10) : rawDate;

                                MedicalRecord record = new MedicalRecord(
                                        id,
                                        obj.optString("pet_name", "Unknown Pet"),
                                        obj.optString("treatment", "General Checkup"),
                                        cleanDate,
                                        obj.optString("veterinarian", "Dr. Jenkins"),
                                        obj.has("notes") && !obj.isNull("notes") ? obj.getString("notes") : ""
                                );
                                loaded.add(record);
                            }

                            Platform.runLater(() -> {
                                masterList.setAll(loaded);
                                updateCounts();
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            if (recordCountBadge != null) recordCountBadge.setText("Offline");
                        });
                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateCounts() {
        int total = filteredList.size();
        if (recordCountBadge != null) recordCountBadge.setText(total + " Records");
        if (footerCountLabel != null) footerCountLabel.setText("Showing " + total + " of " + masterList.size() + " registered protocols");

        long criticalCount = masterList.stream()
                .filter(r -> r.getTreatment().toLowerCase().contains("splint") || r.getTreatment().toLowerCase().contains("surg"))
                .count();
        if (criticalCountLabel != null) criticalCountLabel.setText(String.format("%02d Cases", criticalCount));

        long boosterCount = masterList.stream()
                .filter(r -> r.getTreatment().toLowerCase().contains("vaccine") || r.getTreatment().toLowerCase().contains("deworm"))
                .count();
        if (pendingBoostersLabel != null) pendingBoostersLabel.setText(String.format("%02d Due", boosterCount));
    }

    @FXML
    public void handleOpenAddEntryModal(ActionEvent event) {
        Dialog<JSONObject> dialog = new Dialog<>();
        dialog.setTitle("New Medical Intervention");
        dialog.setHeaderText("Log veterinary protocol to shelter medical ledger.");

        ButtonType saveBtnType = new ButtonType("Save Record", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 40, 10, 10));

        TextField petNameField = new TextField();
        petNameField.setPromptText("e.g. Milo, Bella");

        TextField treatmentField = new TextField();
        treatmentField.setPromptText("e.g. Rabies Booster, Splint Check");

        DatePicker datePicker = new DatePicker(LocalDate.now());

        ComboBox<String> vetCombo = new ComboBox<>();
        vetCombo.getItems().addAll("Dr. Sarah Jenkins", "Dr. Mark Sloan", "Dr. Emily Chen");
        vetCombo.getSelectionModel().selectFirst();

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Clinical observations, next appointment...");
        notesArea.setPrefRowCount(3);

        grid.add(new Label("Pet Name:"), 0, 0);
        grid.add(petNameField, 1, 0);
        grid.add(new Label("Treatment / Vaccine:"), 0, 1);
        grid.add(treatmentField, 1, 1);
        grid.add(new Label("Date Administered:"), 0, 2);
        grid.add(datePicker, 1, 2);
        grid.add(new Label("Veterinarian:"), 0, 3);
        grid.add(vetCombo, 1, 3);
        grid.add(new Label("Clinical Notes:"), 0, 4);
        grid.add(notesArea, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveBtnType) {
                String petName = petNameField.getText().trim();
                String treatment = treatmentField.getText().trim();
                String date = datePicker.getValue() != null ? datePicker.getValue().toString() : LocalDate.now().toString();

                if (!petName.isEmpty() && !treatment.isEmpty()) {
                    JSONObject payload = new JSONObject();
                    payload.put("pet_name", petName);
                    payload.put("treatment", treatment);
                    payload.put("date_administered", date);
                    payload.put("veterinarian", vetCombo.getValue());
                    payload.put("notes", notesArea.getText().trim());
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
                        .uri(URI.create("http://localhost:8080/api/medical"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonObj.toString()))
                        .build();

                client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(res -> Platform.runLater(this::loadMedicalDataFromBackend));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    @FXML
    public void handleExportCsv(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Medical Records to CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("Pawsitive_Medical_Ledger.csv");

        File file = fileChooser.showSaveDialog(searchField.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Pet Name,Treatment,Date Administered,Veterinarian,Notes");
                for (MedicalRecord rec : filteredList) {
                    writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                            rec.getPetName(),
                            rec.getTreatment(),
                            rec.getDateAdministered(),
                            rec.getVeterinarian(),
                            rec.getNotes().replace("\"", "\"\"")
                    );
                }
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Medical records successfully exported!", ButtonType.OK);
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void handleResetFilter(ActionEvent event) {
        if (searchField != null) searchField.clear();
    }

    private void showClinicalSummary(MedicalRecord record) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Clinical Record: " + record.getPetName());
        alert.setHeaderText(record.getTreatment() + " (" + record.getDateAdministered() + ")");
        alert.setContentText(String.format("Attending Veterinarian: %s\n\nClinical Notes:\n%s",
                record.getVeterinarian(),
                record.getNotes().isBlank() ? "No additional notes recorded." : record.getNotes()));
        alert.showAndWait();
    }

    @FXML public void handleAuditLogs(ActionEvent e) { System.out.println("Opening Audit Logs..."); }
    @FXML public void handleDispensePharmacy(ActionEvent e) { System.out.println("Opening Pharmacy Dispatch..."); }
}