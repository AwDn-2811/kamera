package com.example.android_projects;

public class BookingCamera {
    public String userID;
    public String fromDate;
    public String toDate;
    public String paymentMethod;
    public double totalPrice;

    public BookingCamera() {
        // Required empty constructor for Firebase
    }

    public BookingCamera(String userID, String fromDate, String toDate, String paymentMethod, double totalPrice) {
        this.userID = userID;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.paymentMethod = paymentMethod;
        this.totalPrice = totalPrice;
    }
}
