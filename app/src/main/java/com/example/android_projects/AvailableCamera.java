package com.example.android_projects;

public class AvailableCamera {

    private String id;
    private String name;
    private String brand;
    private String description;
    private String address;
    private String imageUrl;
    private long pricePerDay;

    public AvailableCamera() {
        // required for Firebase
    }

    // GETTERS
    public String getId() { return id; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getDescription() { return description; }
    public String getAddress() { return address; }
    public String getImageUrl() { return imageUrl; }
    public long getPricePerDay() { return pricePerDay; }

    // SETTERS
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setDescription(String description) { this.description = description; }
    public void setAddress(String address) { this.address = address; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setPricePerDay(long pricePerDay) { this.pricePerDay = pricePerDay; }
}
