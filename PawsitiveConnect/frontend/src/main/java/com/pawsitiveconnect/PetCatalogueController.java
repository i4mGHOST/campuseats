package com.pawsitiveconnect;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PetCatalogueController {

    @FXML private Label registryCountLabel;
    @FXML private TextField searchField;
    @FXML private Button clearSearchBtn;

    // Filter Buttons
    @FXML private Button filterAllBtn;
    @FXML private Button filterDogsBtn;
    @FXML private Button filterCatsBtn;
    @FXML private Button filterAvailableBtn;
    @FXML private Button filterMedicalBtn;
    @FXML private Button filterPendingBtn;

    // Quick Ledger Count Badges
    @FXML private Label readyBadge;
    @FXML private Label pendingBadge;
    @FXML private Label medicalBadge;

    @FXML private FlowPane petGridPane;
    @FXML private VBox noResultsBox;

    private final List<Pet> allPets = new ArrayList<>();
    private String currentFilter = "all";

    @FXML
    public void initialize() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (clearSearchBtn != null) clearSearchBtn.setVisible(!newVal.trim().isEmpty());
                filterAndDisplayPets();
            });
        }
        loadPetsFromBackend();
    }

    public void loadPetsFromBackend() {
        allPets.clear();
        if (registryCountLabel != null) {
            registryCountLabel.setText("SHELTER REGISTRY • SYNCING...");
        }

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/pets"))
                    .GET()
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(responseBody -> {
                        try {
                            JSONArray petsArray = new JSONArray(responseBody);
                            for (int i = 0; i < petsArray.length(); i++) {
                                JSONObject petObj = petsArray.getJSONObject(i);

                                int id = petObj.optInt("id", petObj.optInt("pet_id", i + 1));
                                String name = petObj.optString("name", "Unnamed");
                                String species = petObj.optString("species", "Dog");
                                String breed = petObj.optString("breed", "Mixed");
                                int age = petObj.optInt("age", 1);
                                String status = petObj.optString("status", "Available");
                                String imageUrl = petObj.optString("image_url", null);

                                allPets.add(new Pet(id, name, species, breed, age, status, imageUrl));
                            }

                            Platform.runLater(() -> {
                                updateLedgerBadges();
                                filterAndDisplayPets();
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            if (registryCountLabel != null) {
                                registryCountLabel.setText("SHELTER REGISTRY • OFFLINE");
                            }
                        });
                        return null;
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterAndDisplayPets() {
        if (petGridPane == null) return;
        petGridPane.getChildren().clear();

        String query = searchField == null || searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        int visibleCount = 0;

        for (Pet pet : allPets) {
            boolean matchesQuery = query.isEmpty()
                    || pet.getName().toLowerCase().contains(query)
                    || pet.getBreed().toLowerCase().contains(query);

            boolean matchesFilter = true;
            String speciesLower = pet.getSpecies().toLowerCase();
            String statusLower = pet.getStatus().toLowerCase();

            switch (currentFilter) {
                case "dog" -> matchesFilter = speciesLower.contains("dog");
                case "cat" -> matchesFilter = speciesLower.contains("cat");
                case "available" -> matchesFilter = statusLower.contains("available");
                case "medical" -> matchesFilter = statusLower.contains("medic") || statusLower.contains("recov");
                case "pending" -> matchesFilter = statusLower.contains("pend");
            }

            if (matchesQuery && matchesFilter) {
                petGridPane.getChildren().add(createPetCard(pet));
                visibleCount++;
            }
        }

        boolean hasResults = visibleCount > 0;
        if (noResultsBox != null) {
            noResultsBox.setVisible(!hasResults);
            noResultsBox.setManaged(!hasResults);
        }
        petGridPane.setVisible(hasResults);
        petGridPane.setManaged(hasResults);

        if (registryCountLabel != null) {
            registryCountLabel.setText("SHELTER REGISTRY • " + allPets.size() + " RESIDENTS");
        }
    }

    /**
     * Resolves images from web URLs, explicit filenames, or auto-matches by pet name
     */
    private Image loadPetImage(Pet pet) {
        String explicitUrl = pet.getImageUrl();

        // 1. Check if database has an explicit URL or file path
        if (explicitUrl != null && !explicitUrl.isBlank()) {
            explicitUrl = explicitUrl.trim();
            if (explicitUrl.startsWith("http://") || explicitUrl.startsWith("https://")) {
                try {
                    return new Image(explicitUrl, 310, 170, false, true, true);
                } catch (Exception ignored) {}
            }

            String[] candidatePaths = {
                    explicitUrl,
                    explicitUrl.startsWith("/") ? explicitUrl : "/" + explicitUrl,
                    "/assets/" + explicitUrl,
                    "/com/pawsitiveconnect/assets/" + explicitUrl
            };

            for (String path : candidatePaths) {
                try {
                    URL res = getClass().getResource(path);
                    if (res != null) {
                        return new Image(res.toExternalForm(), 310, 170, false, true, true);
                    }
                } catch (Exception ignored) {}
            }
        }

        // 2. Auto-match by pet name & ID inside /assets/ folder
        String cleanName = pet.getName().trim().toLowerCase().replaceAll("\\s+", "_");
        String idStr = String.valueOf(pet.getId());

        String[] autoFilenames = {
                cleanName + ".jpg", cleanName + ".png", cleanName + ".jpeg",
                pet.getName().trim() + ".jpg", pet.getName().trim() + ".png",
                idStr + ".jpg", idStr + ".png"
        };

        for (String filename : autoFilenames) {
            String[] candidatePaths = {
                    "/assets/" + filename,
                    "/com/pawsitiveconnect/assets/" + filename,
                    "/" + filename
            };
            for (String path : candidatePaths) {
                try {
                    URL res = getClass().getResource(path);
                    if (res != null) {
                        return new Image(res.toExternalForm(), 295, 170, false, true, true);
                    }
                } catch (Exception ignored) {}
            }
        }

        return null;
    }

    private VBox createPetCard(Pet pet) {
        VBox card = new VBox(10.0);
        card.setPrefWidth(310.0);
        card.setMaxWidth(310.0);
        card.getStyleClass().add("pet-card");

        // Media Area with rounded image clipping
        StackPane mediaArea = new StackPane();
        mediaArea.setPrefHeight(170.0);
        mediaArea.setMaxHeight(170.0);
        mediaArea.setStyle("-fx-background-color: #e9f1de; -fx-background-radius: 14px;");

        Rectangle clip = new Rectangle(295, 170);
        clip.setArcWidth(28);
        clip.setArcHeight(28);
        mediaArea.setClip(clip);

        Image petImage = loadPetImage(pet);
        if (petImage != null && !petImage.isError()) {
            ImageView imageView = new ImageView(petImage);
            imageView.setFitWidth(295);
            imageView.setFitHeight(170);
            imageView.setPreserveRatio(false);
            mediaArea.getChildren().add(imageView);
        } else {
            Label placeholderEmoji = new Label(pet.getSpecies().equalsIgnoreCase("cat") ? "🐱" : "🐶");
            placeholderEmoji.setStyle("-fx-font-size: 54px;");
            mediaArea.getChildren().add(placeholderEmoji);
        }

        // Species Pill
        Label speciesPill = new Label(pet.getSpecies().equalsIgnoreCase("cat") ? "\uD83D\uDC31 Cat" : "\uD83D\uDC36 Dog");
        speciesPill.setStyle("-fx-background-color: rgba(244, 252, 234, 0.92); -fx-text-fill: #171E13; -fx-font-weight: bold; -fx-font-size: 10.5px; -fx-background-radius: 999px; -fx-padding: 3px 8px;");
        StackPane.setAlignment(speciesPill, Pos.TOP_LEFT);
        StackPane.setMargin(speciesPill, new Insets(10));

        // Status Pill
        Label statusPill = new Label();
        String stLower = pet.getStatus().toLowerCase();
        if (stLower.contains("available")) {
            statusPill.setText("\u25CF Available");
            statusPill.getStyleClass().add("pill-green");
        } else if (stLower.contains("medic") || stLower.contains("recov")) {
            statusPill.setText("\u25CF In Medical Care");
            statusPill.getStyleClass().add("pill-red");
        } else {
            statusPill.setText("\u25CF " + pet.getStatus());
            statusPill.getStyleClass().add("pill-gray");
        }
        StackPane.setAlignment(statusPill, Pos.TOP_RIGHT);
        StackPane.setMargin(statusPill, new Insets(10));

        mediaArea.getChildren().addAll(speciesPill, statusPill);

        // Name & Age Row
        HBox titleRow = new HBox(8.0);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        VBox nameBox = new VBox(2.0);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        Label nameLabel = new Label(pet.getName());
        nameLabel.getStyleClass().add("text-item-title");
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 800;");

        Label tagLabel = new Label("Tag ID: #PC-" + pet.getSpecies().toUpperCase().charAt(0) + String.format("%02d", pet.getId()));
        tagLabel.getStyleClass().add("text-card-muted");
        nameBox.getChildren().addAll(nameLabel, tagLabel);

        Label ageBadge = new Label(pet.getAge() + " Years");
        ageBadge.getStyleClass().add("pill-gray");

        titleRow.getChildren().addAll(nameBox, ageBadge);

        // Specs Info Box
        VBox specBox = new VBox(4.0);
        specBox.getStyleClass().add("pet-spec-box");

        HBox breedRow = new HBox();
        Label breedTitle = new Label("Breed");
        breedTitle.getStyleClass().add("text-card-muted");
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        Label breedValue = new Label(pet.getBreed());
        breedValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #171E13; -fx-font-size: 11.5px;");
        breedRow.getChildren().addAll(breedTitle, spacer1, breedValue);

        HBox statusRow = new HBox();
        Label statusTitle = new Label("Status");
        statusTitle.getStyleClass().add("text-card-muted");
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        Label statusValue = new Label(pet.getStatus());
        statusValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #2C694E; -fx-font-size: 11.5px;");
        statusRow.getChildren().addAll(statusTitle, spacer2, statusValue);

        specBox.getChildren().addAll(breedRow, statusRow);

        // Action Buttons Row
        HBox actionRow = new HBox(8.0);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        Button viewProfileBtn = new Button("View Profile");
        viewProfileBtn.getStyleClass().add("pet-action-button");
        HBox.setHgrow(viewProfileBtn, Priority.ALWAYS);
        viewProfileBtn.setMaxWidth(Double.MAX_VALUE);
        viewProfileBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Pet Dossier • " + pet.getName());
            alert.setHeaderText(pet.getName() + " (" + pet.getSpecies() + " - " + pet.getBreed() + ")");
            alert.setContentText(String.format("Tag ID: #PC-%s%02d\nAge: %d Years\nStatus: %s\n\nHealth Check: Fully vaccinated, microchipped & spayed/neutered.\nTemperament: Friendly, gentle companion suited for home adoption.",
                    pet.getSpecies().toUpperCase().substring(0, 1), pet.getId(), pet.getAge(), pet.getStatus()));
            alert.showAndWait();
        });

        Button adoptBtn = new Button("\u2764");
        adoptBtn.getStyleClass().add("pet-heart-button");
        adoptBtn.setTooltip(new Tooltip("Start Adoption Process"));
        adoptBtn.setOnAction(e -> {
            if (MainLayoutController.getInstance() != null) {
                Object ctrl = MainLayoutController.getInstance().loadView("AdoptionWorkflowView.fxml");
                if (ctrl instanceof AdoptionWorkflowController awc) {
                    awc.openNewIntakeForPet(pet.getName());
                }
            }
        });

        actionRow.getChildren().addAll(viewProfileBtn, adoptBtn);
        card.getChildren().addAll(mediaArea, titleRow, specBox, actionRow);
        return card;
    }

    private void updateLedgerBadges() {
        long ready = allPets.stream().filter(p -> p.getStatus().toLowerCase().contains("available")).count();
        long pending = allPets.stream().filter(p -> p.getStatus().toLowerCase().contains("pending")).count();
        long medical = allPets.stream().filter(p -> p.getStatus().toLowerCase().contains("medic") || p.getStatus().toLowerCase().contains("recov")).count();

        if (readyBadge != null) readyBadge.setText("\u2713 " + ready + " Ready");
        if (pendingBadge != null) pendingBadge.setText("\u23F1 " + pending + " Pending");
        if (medicalBadge != null) medicalBadge.setText("\u271A " + medical + " Critical");
    }

    @FXML public void handleFilterAll(ActionEvent event) { setActiveFilter(filterAllBtn, "all"); }
    @FXML public void handleFilterDogs(ActionEvent event) { setActiveFilter(filterDogsBtn, "dog"); }
    @FXML public void handleFilterCats(ActionEvent event) { setActiveFilter(filterCatsBtn, "cat"); }
    @FXML public void handleFilterAvailable(ActionEvent event) { setActiveFilter(filterAvailableBtn, "available"); }
    @FXML public void handleFilterMedical(ActionEvent event) { setActiveFilter(filterMedicalBtn, "medical"); }
    @FXML public void handleFilterPending(ActionEvent event) { setActiveFilter(filterPendingBtn, "pending"); }

    private void setActiveFilter(Button activeBtn, String filterKey) {
        Button[] buttons = {filterAllBtn, filterDogsBtn, filterCatsBtn, filterAvailableBtn, filterMedicalBtn, filterPendingBtn};
        for (Button btn : buttons) {
            if (btn != null) {
                btn.getStyleClass().remove("filter-pill-active");
                if (!btn.getStyleClass().contains("filter-pill")) btn.getStyleClass().add("filter-pill");
            }
        }
        if (activeBtn != null) {
            activeBtn.getStyleClass().remove("filter-pill");
            activeBtn.getStyleClass().add("filter-pill-active");
        }

        this.currentFilter = filterKey;
        filterAndDisplayPets();
    }

    @FXML
    public void handleClearSearch(ActionEvent event) {
        if (searchField != null) searchField.clear();
        if (clearSearchBtn != null) clearSearchBtn.setVisible(false);
    }

    @FXML
    public void handleResetFilters(ActionEvent event) {
        if (searchField != null) searchField.clear();
        setActiveFilter(filterAllBtn, "all");
    }

    @FXML
    public void handleOpenAddPetModal(ActionEvent event) {
        Dialog<JSONObject> dialog = new Dialog<>();
        dialog.setTitle("New Pet Intake");
        dialog.setHeaderText("Add an animal record to the Pawsitive Connect registry.");

        ButtonType saveButtonType = new ButtonType("Save Record", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameInput = new TextField();
        nameInput.setPromptText("e.g. Copper");

        ComboBox<String> speciesCombo = new ComboBox<>();
        speciesCombo.getItems().addAll("Dog", "Cat", "Other Companion");
        speciesCombo.getSelectionModel().selectFirst();

        TextField breedInput = new TextField();
        breedInput.setPromptText("e.g. Golden Retriever");

        TextField ageInput = new TextField();
        ageInput.setPromptText("e.g. 2");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Available", "In Medical Care", "Pending Adoption");
        statusCombo.getSelectionModel().selectFirst();

        TextField imageInput = new TextField();
        imageInput.setPromptText("e.g. copper.jpg or https://...");

        grid.add(new Label("Animal Name:"), 0, 0);
        grid.add(nameInput, 1, 0);
        grid.add(new Label("Species:"), 0, 1);
        grid.add(speciesCombo, 1, 1);
        grid.add(new Label("Breed:"), 0, 2);
        grid.add(breedInput, 1, 2);
        grid.add(new Label("Age (Years):"), 0, 3);
        grid.add(ageInput, 1, 3);
        grid.add(new Label("Intake Status:"), 0, 4);
        grid.add(statusCombo, 1, 4);
        grid.add(new Label("Image File / URL:"), 0, 5);
        grid.add(imageInput, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameInput.getText().trim();
                String breed = breedInput.getText().trim();
                int age = 1;
                try {
                    age = Integer.parseInt(ageInput.getText().trim());
                } catch (NumberFormatException ignored) {}

                if (!name.isEmpty() && !breed.isEmpty()) {
                    JSONObject obj = new JSONObject();
                    obj.put("name", name);
                    obj.put("species", speciesCombo.getValue());
                    obj.put("breed", breed);
                    obj.put("age", age);
                    obj.put("status", statusCombo.getValue());
                    obj.put("image_url", imageInput.getText().trim());
                    return obj;
                }
            }
            return null;
        });

        Optional<JSONObject> result = dialog.showAndWait();
        result.ifPresent(jsonObj -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/api/pets"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonObj.toString()))
                        .build();

                client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                        .thenAccept(res -> Platform.runLater(this::loadPetsFromBackend));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }
}