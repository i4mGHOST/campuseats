package com.pawsitiveconnect;

public class Pet {
    private int id;
    private String name;
    private String species;
    private String breed;
    private int age;
    private String status;
    private String imageUrl;

    // Original 6-parameter constructor (backward compatible)
    public Pet(int id, String name, String species, String breed, int age, String status) {
        this(id, name, species, breed, age, status, null);
    }

    // Extended constructor with imageUrl
    public Pet(int id, String name, String species, String breed, int age, String status, String imageUrl) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.age = age;
        this.status = status;
        this.imageUrl = imageUrl;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getSpecies() { return species; }
    public String getBreed() { return breed; }
    public int getAge() { return age; }
    public String getStatus() { return status; }
    public String getImageUrl() { return imageUrl; }
}