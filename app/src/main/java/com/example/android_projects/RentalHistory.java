package com.example.android_projects;

public class RentalHistory {

    public String id;          // ID kamera
    public String title;       // Kombinasi brand + model
    public String fromDate;    // Tanggal mulai sewa
    public String toDate;      // Tanggal selesai sewa
    public String status;      // ongoing / finished / cancelled

    public String imageUrl;    // URL gambar dari Cloudinary

    public RentalHistory() {
    }
}
