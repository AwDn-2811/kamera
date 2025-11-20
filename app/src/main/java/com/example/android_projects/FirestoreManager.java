package com.example.android_projects;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirestoreManager {

    private static final String TAG = "FirestoreManager";
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    // Singleton Instance
    private static FirestoreManager instance;

    private FirestoreManager() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized FirestoreManager getInstance() {
        if (instance == null) {
            instance = new FirestoreManager();
        }
        return instance;
    }

    // Interface untuk Callback agar Activity tahu kapan data selesai diambil
    public interface FirestoreCallback {
        void onSuccess(Object result);
        void onFailure(Exception e);
    }

    // ==========================================
    // 1. COLLECTION: USERS
    // ==========================================

    /**
     * Menyimpan atau memperbarui data user.
     * Menggunakan Document ID = UID dari Firebase Auth (Manual ID).
     */
    public void saveUser(String fullName, String phone, String email, FirestoreCallback callback) {
        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullName);
        user.put("email", email);
        user.put("phoneNumber", phone);
        user.put("updatedAt", Timestamp.now());
        // role bisa diset default 'user'
        user.put("role", "user");

        // Gunakan .set() dengan SetOptions.merge() agar field lain tidak hilang
        db.collection("users").document(userId)
                .set(user, SetOptions.merge())
                .addOnSuccessListener(aVoid -> callback.onSuccess("User saved"))
                .addOnFailureListener(callback::onFailure);
    }

    public void getUserProfile(String userId, FirestoreCallback callback) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(callback::onSuccess) // Mengembalikan DocumentSnapshot
                .addOnFailureListener(callback::onFailure);
    }

    // ==========================================
    // 2. COLLECTION: CAMERAS (Auto-Generated ID)
    // ==========================================

    /**
     * Menambahkan kamera baru.
     * Menggunakan .add() -> Firestore otomatis membuat ID unik.
     */
    public void addCamera(String name, String brand, String desc, double price,
                          double lat, double lng, String address, String imageUrl, FirestoreCallback callback) {

        Map<String, Object> camera = new HashMap<>();
        camera.put("name", name);
        camera.put("brand", brand);
        camera.put("description", desc);
        camera.put("pricePerDay", price);
        camera.put("imageUrl", imageUrl);
        camera.put("location", new GeoPoint(lat, lng)); // Penting untuk Maps
        camera.put("address", address);
        camera.put("isAvailable", true);
        camera.put("ratingAverage", 0.0);
        camera.put("reviewCount", 0);
        camera.put("createdAt", Timestamp.now());

        db.collection("cameras")
                .add(camera)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Camera added with ID: " + documentReference.getId());
                    callback.onSuccess(documentReference.getId());
                })
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Mengambil semua kamera yang tersedia (isAvailable == true).
     */
    public void getAvailableCameras(FirestoreCallback callback) {
        db.collection("cameras")
                .whereEqualTo("isAvailable", true)
                .get()
                .addOnSuccessListener(callback::onSuccess) // Mengembalikan QuerySnapshot
                .addOnFailureListener(callback::onFailure);
    }

    // ==========================================
    // 3. COLLECTION: BOOKINGS
    // ==========================================

    /**
     * Membuat booking baru.
     * Melakukan denormalisasi (menyimpan nama kamera) agar query history cepat.
     */
    public void createBooking(String cameraId, String cameraName, String cameraImage,
                              Date startDate, Date endDate, double totalPrice, FirestoreCallback callback) {

        if (auth.getCurrentUser() == null) return;

        Map<String, Object> booking = new HashMap<>();
        booking.put("userId", auth.getCurrentUser().getUid());
        booking.put("cameraId", cameraId);

        // Denormalisasi: Simpan info kamera di sini
        booking.put("cameraName", cameraName);
        booking.put("cameraImageUrl", cameraImage);

        booking.put("startDate", new Timestamp(startDate));
        booking.put("endDate", new Timestamp(endDate));
        booking.put("totalPrice", totalPrice);
        booking.put("status", "pending_payment");
        booking.put("createdAt", Timestamp.now());

        db.collection("bookings")
                .add(booking)
                .addOnSuccessListener(documentReference -> callback.onSuccess(documentReference.getId()))
                .addOnFailureListener(callback::onFailure);
    }

    /**
     * Mengambil riwayat sewa milik user yang sedang login.
     * Diurutkan berdasarkan tanggal pembuatan terbaru.
     */
    public void getMyRentalHistory(FirestoreCallback callback) {
        String myUid = auth.getCurrentUser().getUid();

        db.collection("bookings")
                .whereEqualTo("userId", myUid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    // ==========================================
    // 4. COLLECTION: REVIEWS
    // ==========================================

    public void addReview(String cameraId, float rating, String comment, String userName, FirestoreCallback callback) {
        Map<String, Object> review = new HashMap<>();
        review.put("cameraId", cameraId);
        review.put("userId", auth.getCurrentUser().getUid());
        review.put("userName", userName); // Denormalisasi nama user
        review.put("rating", rating);
        review.put("comment", comment);
        review.put("timestamp", Timestamp.now());

        db.collection("reviews")
                .add(review)
                .addOnSuccessListener(ref -> callback.onSuccess("Review Added"))
                .addOnFailureListener(callback::onFailure);

        // Catatan: Idealnya di sini juga menjalankan Cloud Function
        // atau Transaction untuk mengupdate ratingAverage di collection 'cameras'
    }

    public void getCameraReviews(String cameraId, FirestoreCallback callback) {
        db.collection("reviews")
                .whereEqualTo("cameraId", cameraId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onFailure);
    }

    // ==========================================
    // 5. COLLECTION: FEEDBACKS
    // ==========================================

    public void sendFeedback(String message, String category, FirestoreCallback callback) {
        Map<String, Object> feedback = new HashMap<>();
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "anonymous";

        feedback.put("userId", uid);
        feedback.put("message", message);
        feedback.put("category", category);
        feedback.put("createdAt", Timestamp.now());

        db.collection("feedbacks")
                .add(feedback)
                .addOnSuccessListener(ref -> callback.onSuccess("Feedback Sent"))
                .addOnFailureListener(callback::onFailure);
    }
}