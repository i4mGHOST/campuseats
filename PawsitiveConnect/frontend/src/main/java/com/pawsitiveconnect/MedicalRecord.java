package com.pawsitiveconnect;

public class MedicalRecord {
    private int id;
    private String petName;
    private String treatment;
    private String dateAdministered;
    private String veterinarian;
    private String notes;

    // Backward-compatible 5-parameter constructor
    public MedicalRecord(String petName, String treatment, String dateAdministered, String veterinarian, String notes) {
        this(0, petName, treatment, dateAdministered, veterinarian, notes);
    }

    // Extended constructor with ID
    public MedicalRecord(int id, String petName, String treatment, String dateAdministered, String veterinarian, String notes) {
        this.id = id;
        this.petName = petName;
        this.treatment = treatment;
        this.dateAdministered = dateAdministered;
        this.veterinarian = veterinarian;
        this.notes = notes;
    }

    // Getters
    public int getId() { return id; }
    public String getPetName() { return petName; }
    public String getTreatment() { return treatment; }
    public String getDateAdministered() { return dateAdministered; }
    public String getVeterinarian() { return veterinarian; }
    public String getNotes() { return notes != null ? notes : ""; }

    public void setNotes(String notes) { this.notes = notes; }
}