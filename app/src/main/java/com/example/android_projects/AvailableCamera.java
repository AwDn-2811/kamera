package com.example.android_projects;

public class AvailableCamera {

    public String id;
    public String brand;
    public String model;
    public String type;
    public String resolution;
    public String price;
    public String location;

    private String imageUrl;   // *** penting untuk Cloudinary ***

    public AvailableCamera() {
    }

    // =================== GETTERS ===================
    public String getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getType() {
        return type;
    }

    public String getResolution() {
        return resolution;
    }

    public String getPrice() {
        return price;
    }

    public String getLocation() {
        return location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // =================== SETTERS ===================
    public void setId(String id) {
        this.id = id;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
