package com.example.android_projects;

import android.graphics.Bitmap;

public class HomeCameraCard extends AvailableCamera {

    public String id;        // ID kamera
    public Bitmap image;     // Gambar kamera
    public String brand;     // Brand + Model (Sony A7 III)
    public String price;     // Rp150000 / hari
    public String name;

    public HomeCameraCard() {
    }

    public String getID() {
        return id;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getBrand() {
        return brand;
    }

    public String getPrice() {
        return price;
    }
}
