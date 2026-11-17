package com.pawsitiveconnect;

public class AdoptionApplication {
    private int applicationId;
    private String applicantName;
    private String petName;
    private String status;
    private String applicationDate;
    private double feeAmount;
    private String location;
    private String notes;

    // Backward-compatible 5-parameter constructor
    public AdoptionApplication(int applicationId, String applicantName, String petName, String status, String applicationDate) {
        this(applicationId, applicantName, petName, status, applicationDate, 150.00, "Metro Shelter Kennel 04", "Standard Evaluation");
    }

    // Extended constructor
    public AdoptionApplication(int applicationId, String applicantName, String petName, String status,
                               String applicationDate, double feeAmount, String location, String notes) {
        this.applicationId = applicationId;
        this.applicantName = applicantName;
        this.petName = petName;
        this.status = status;
        this.applicationDate = applicationDate != null ? applicationDate : "Just now";
        this.feeAmount = feeAmount > 0 ? feeAmount : 150.00;
        this.location = location != null ? location : "Central Shelter Facility";
        this.notes = notes != null ? notes : "Identity & Housing Checks Verified";
    }

    // Getters
    public int getApplicationId() { return applicationId; }
    public String getApplicantName() { return applicantName; }
    public String getPetName() { return petName; }
    public String getStatus() { return status; }
    public String getApplicationDate() { return applicationDate; }
    public double getFeeAmount() { return feeAmount; }
    public String getLocation() { return location; }
    public String getNotes() { return notes; }

    // Setters
    public void setStatus(String status) { this.status = status; }
}