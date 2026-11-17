package com.pawsitiveconnect;

public class RescueDispatch {
    private int id;
    private String reporterName;
    private String contactNumber;
    private String animalDescription;
    private String locationCoordinates;
    private String urgencyLevel;
    private String status;

    public RescueDispatch(int id, String reporterName, String contactNumber, String animalDescription,
                          String locationCoordinates, String urgencyLevel, String status) {
        this.id = id;
        this.reporterName = reporterName != null ? reporterName : "Anonymous";
        this.contactNumber = contactNumber != null ? contactNumber : "N/A";
        this.animalDescription = animalDescription;
        this.locationCoordinates = locationCoordinates;
        this.urgencyLevel = urgencyLevel != null ? urgencyLevel : "Medium";
        this.status = status != null ? status : "REPORTED";
    }

    // Getters
    public int getId() { return id; }
    public String getReporterName() { return reporterName; }
    public String getContactNumber() { return contactNumber; }
    public String getAnimalDescription() { return animalDescription; }
    public String getLocationCoordinates() { return locationCoordinates; }
    public String getUrgencyLevel() { return urgencyLevel; }
    public String getStatus() { return status; }

    // Setters for live state updates
    public void setStatus(String status) { this.status = status; }
    public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }
}