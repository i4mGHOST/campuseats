package com.pawsitiveconnect;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainLayoutController {

    private static MainLayoutController instance;

    @FXML private Label userEmailLabel;
    @FXML private Label userRoleLabel;
    @FXML private TextField searchField;
    @FXML private StackPane contentArea;

    // Navigation Buttons
    @FXML private Button navDashboard;
    @FXML private Button navPets;
    @FXML private Button navMedical;
    @FXML private Button navRescue;
    @FXML private Button navAdoptions;
    @FXML private Button navLedger;

    private List<Button> navButtons;

    public static MainLayoutController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        instance = this;

        // Display user email
        String email = UserSession.getEmail();
        userEmailLabel.setText(email != null ? email : "volunteer@pawsitive.com");

        // Role title
        int roleId = UserSession.getRoleId();
        switch (roleId) {
            case 1 -> userRoleLabel.setText("System Admin");
            case 2 -> userRoleLabel.setText("Shelter Lead");
            case 3 -> userRoleLabel.setText("Foster Volunteer");
            default -> userRoleLabel.setText("Adopter");
        }

        navButtons = Arrays.asList(navDashboard, navPets, navMedical, navRescue, navAdoptions, navLedger);

        // Global Keyword Search Handler
        if (searchField != null) {
            searchField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    handleGlobalSearch(searchField.getText().trim());
                }
            });
        }

        // Default to Dashboard
        showDashboard(null);
    }

    /**
     * Navigates across the entire application based on contextual keywords
     */
    private void handleGlobalSearch(String query) {
        if (query.isEmpty()) return;
        String q = query.toLowerCase();

        if (q.contains("dash") || q.contains("home") || q.contains("stat") || q.contains("overview")) {
            showDashboard(null);
        } else if (q.contains("pet") || q.contains("dog") || q.contains("cat") || q.contains("breed") || q.contains("catalogue")) {
            showPets(null);
        } else if (q.contains("vet") || q.contains("medic") || q.contains("vaccin") || q.contains("surg") || q.contains("welfare") || q.contains("doctor")) {
            showMedical(null);
        } else if (q.contains("rescue") || q.contains("dispatch") || q.contains("emergen") || q.contains("urgent") || q.contains("gps") || q.contains("incident")) {
            showRescue(null);
        } else if (q.contains("adopt") || q.contains("applic") || q.contains("workflow") || q.contains("queue") || q.contains("foster")) {
            showAdoptions(null);
        } else if (q.contains("ledger") || q.contains("transact") || q.contains("fee") || q.contains("pay") || q.contains("donat") || q.contains("money")) {
            showTransactionLedger(null);
        } else {
            // Default fallback: search inside Pet Catalogue
            showPets(null);
        }
    }

    private void setActiveNavButton(Button activeBtn) {
        for (Button btn : navButtons) {
            if (btn == null) continue;
            if (btn == activeBtn) {
                btn.getStyleClass().remove("nav-button");
                if (!btn.getStyleClass().contains("nav-button-active")) {
                    btn.getStyleClass().add("nav-button-active");
                }
            } else {
                btn.getStyleClass().remove("nav-button-active");
                if (!btn.getStyleClass().contains("nav-button")) {
                    btn.getStyleClass().add("nav-button");
                }
            }
        }
    }

    @FXML
    public void showDashboard(ActionEvent event) {
        setActiveNavButton(navDashboard);
        loadView("DashboardView.fxml");
    }

    @FXML
    public void showPets(ActionEvent event) {
        setActiveNavButton(navPets);
        loadView("PetCatalogueView.fxml");
    }

    @FXML
    public void showMedical(ActionEvent event) {
        setActiveNavButton(navMedical);
        loadView("MedicalHistoryView.fxml");
    }

    @FXML
    public void showRescue(ActionEvent event) {
        setActiveNavButton(navRescue);
        loadView("RescueDispatchView.fxml");
    }

    @FXML
    public void showAdoptions(ActionEvent event) {
        setActiveNavButton(navAdoptions);
        loadView("AdoptionWorkflowView.fxml");
    }

    @FXML
    public void showTransactionLedger(ActionEvent event) {
        setActiveNavButton(navLedger);
        loadView("TransactionLedgerView.fxml");
    }

    public Object loadView(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not load view: " + fxmlFileName);
            return null;
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        UserSession.clearSession();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userEmailLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 1080, 700));
            stage.setTitle("Pawsitive Connect - Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}