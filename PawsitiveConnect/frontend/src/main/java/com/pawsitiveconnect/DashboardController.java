package com.pawsitiveconnect;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class DashboardController {

    @FXML
    public void initialize() {
        // Ready for telemetry sync
    }

    @FXML
    public void handleViewPets(ActionEvent event) {
        if (MainLayoutController.getInstance() != null) {
            MainLayoutController.getInstance().showPets(event);
        }
    }

    @FXML
    public void handleReviewQueue(ActionEvent event) {
        if (MainLayoutController.getInstance() != null) {
            MainLayoutController.getInstance().showAdoptions(event);
        }
    }

    @FXML
    public void handleViewMedicalLedger(ActionEvent event) {
        if (MainLayoutController.getInstance() != null) {
            MainLayoutController.getInstance().showMedical(event);
        }
    }

    @FXML
    public void handleLiveGps(ActionEvent event) {
        if (MainLayoutController.getInstance() != null) {
            MainLayoutController.getInstance().showRescue(event);
        }
    }

    @FXML
    public void handleEmergencyRescue(ActionEvent event) {
        // Switches to Rescue Dispatch and immediately triggers the report dialog
        if (MainLayoutController.getInstance() != null) {
            Object controller = MainLayoutController.getInstance().loadView("RescueDispatchView.fxml");
            if (controller instanceof RescueDispatchController rdc) {
                rdc.handleOpenNewEmergencyModal(event);
            }
        }
    }

    @FXML
    public void handleLogIntake(ActionEvent event) {
        // Switches to Pet Catalogue and immediately triggers the Add Pet dialog
        if (MainLayoutController.getInstance() != null) {
            Object controller = MainLayoutController.getInstance().loadView("PetCatalogueView.fxml");
            if (controller instanceof PetCatalogueController pcc) {
                pcc.handleOpenAddPetModal(event);
            }
        }
    }

    @FXML
    public void handleScheduleVet(ActionEvent event) {
        // Switches to Medical view and immediately triggers the Add Entry dialog
        if (MainLayoutController.getInstance() != null) {
            Object controller = MainLayoutController.getInstance().loadView("MedicalHistoryView.fxml");
            if (controller instanceof MedicalHistoryController mhc) {
                mhc.handleOpenAddEntryModal(event);
            }
        }
    }

    @FXML
    public void handleContactDriver(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Field Dispatch Radio");
        alert.setHeaderText("Van Unit 2 Dispatch Contact");
        alert.setContentText("Driver: Officer M. Hayes\nVehicle: Unit 02 (Ford Transit Animal Spec)\nDirect Radio Channel: 154.2 MHz\nCell Hotline: +1 (555) 019-8821\nStatus: En Route to Oakwood Ridge Park (ETA 6m)");
        alert.showAndWait();
    }
}