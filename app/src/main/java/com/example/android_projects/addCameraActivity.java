package com.example.android_projects;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.android_projects.R; // Pastikan ini mengarah ke file R yang benar
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class addCameraActivity extends AppCompatActivity {

    private static final String TAG = "AddCameraActivity";
    private static final int PICK_IMAGE_REQUEST = 200;

    // Variabel UI
    ImageView camera_preview;
    EditText input_brand, input_model, input_type, input_resolution, input_price, input_location;
    Button btn_choose, btn_save;

    // Variabel Data
    Uri imageUri = null;
    private CustomProgressDialog progressDialog; // Menggunakan CustomProgressDialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inisialisasi Cloudinary (Dilakukan di onCreate untuk memastikan ketersediaan)
        initCloudinary();

        setContentView(R.layout.activity_add_camera);

        // 2. Binding Views
        camera_preview = findViewById(R.id.camera_preview);
        input_brand = findViewById(R.id.input_brand);
        input_model = findViewById(R.id.input_model);
        input_type = findViewById(R.id.input_type);
        input_resolution = findViewById(R.id.input_resolution);
        input_price = findViewById(R.id.input_price);
        input_location = findViewById(R.id.input_location);

        btn_choose = findViewById(R.id.btn_choose_image);
        btn_save = findViewById(R.id.btn_save_camera);

        progressDialog = new CustomProgressDialog(this);

        // 3. Set Listener
        btn_choose.setOnClickListener(v -> chooseImage());
        btn_save.setOnClickListener(v -> saveCamera());
    }

    private void initCloudinary() {
        // Cek agar tidak double init
        try {
            MediaManager.get();
        } catch (Exception e) {
            Map<String, Object> config = new HashMap<>();
            // Gunakan konfigurasi Cloudinary Anda
            config.put("cloud_name", "dskqwokji");
            config.put("api_key", "868821764693376");
            config.put("api_secret", "lEgo-9HFqAICSHAvYj64WkG46Ew");
            MediaManager.init(this, config);
        }
    }

    private void chooseImage() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE_REQUEST && data != null) {
            imageUri = data.getData();
            try {
                Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                camera_preview.setImageBitmap(bmp);
            } catch (IOException e) {
                Log.e(TAG, "Gagal memuat gambar", e);
                Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateInput() {
        if (imageUri == null) {
            Toast.makeText(this, "Pilih gambar kamera dulu!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (input_brand.getText().toString().trim().isEmpty()) {
            input_brand.setError("Brand wajib diisi");
            return false;
        }
        if (input_model.getText().toString().trim().isEmpty()) {
            input_model.setError("Model wajib diisi");
            return false;
        }
        if (input_price.getText().toString().trim().isEmpty()) {
            input_price.setError("Harga wajib diisi");
            return false;
        }
        if (input_location.getText().toString().trim().isEmpty()) {
            input_location.setError("Lokasi wajib diisi");
            return false;
        }
        return true;
    }

    // ===========================================
    // MAIN LOGIC: UPLOAD IMAGE THEN SAVE DATA
    // ===========================================

    private void saveCamera() {
        if (!validateInput()) {
            return;
        }

        progressDialog.show("Mengunggah gambar...");

        // 1. Upload Gambar ke Cloudinary
        MediaManager.get().upload(imageUri)
                .option("folder", "camera_rentals") // Folder di Cloudinary
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Upload Started: " + requestId);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // Opsi: Update progress bar
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        Log.d(TAG, "Upload Success: " + imageUrl);

                        // 2. Simpan Data ke Firestore setelah upload sukses
                        saveCameraDataToFirestore(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        progressDialog.dismiss();
                        Log.e(TAG, "Upload Error: " + error.getDescription());
                        Toast.makeText(addCameraActivity.this, "Gagal mengunggah gambar: " + error.getDescription(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        // Not implemented
                    }
                }).dispatch();
    }

    // === FUNGSI PENYIMPANAN BARU (MENGGUNAKAN FIRESTORE) ===
    private void saveCameraDataToFirestore(String imageUrl) {

        // Ambil data dari input field
        String brand = input_brand.getText().toString().trim();
        String model = input_model.getText().toString().trim();
        String type = input_type.getText().toString().trim();
        String resolution = input_resolution.getText().toString().trim();
        String address = input_location.getText().toString().trim();

        double priceValue = 0;
        try {
            priceValue = Double.parseDouble(input_price.getText().toString().trim());
        } catch (NumberFormatException e) {
            Log.e(TAG, "Harga tidak valid", e);
        }

        // Dapatkan OwnerId (penting!)
        String ownerId = "";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            ownerId = user.getUid();
        } else {
            progressDialog.dismiss();
            Toast.makeText(this, "Anda harus login untuk menambahkan kamera.", Toast.LENGTH_SHORT).show();
            return;
        }


        // 4. Siapkan Data Map (Sesuai Skema Firestore: Collection 'cameras')
        Map<String, Object> cam = new HashMap<>();
        cam.put("name", brand + " " + model); // Gabungkan brand & model
        cam.put("brand", brand);
        cam.put("description", type + " / " + resolution); // Gabungkan type & resolution menjadi deskripsi
        cam.put("pricePerDay", priceValue);
        cam.put("address", address);
        cam.put("imageUrl", imageUrl);
        cam.put("ownerId", ownerId);
        cam.put("isAvailable", true); // Status default
        cam.put("ratingAverage", 0.0);
        cam.put("reviewCount", 0);
        cam.put("createdAt", Timestamp.now());

        // **CATATAN PENTING UNTUK GEOLOCATION**
        // Karena tidak ada input Lat/Lng, kita gunakan nilai default/dummy.
        // Anda harus mengimplementasikan API Geocoding atau Location Picker
        // untuk mendapatkan nilai Lat/Lng yang akurat.
        cam.put("location", new GeoPoint(-6.200000, 106.816666));


        // 5. Simpan ke Collection "cameras" (Menggunakan .add() untuk Auto-ID)
        FirebaseFirestore.getInstance().collection("cameras")
                .add(cam)
                .addOnSuccessListener(documentReference -> {
                    progressDialog.dismiss();
                    Toast.makeText(addCameraActivity.this, "Kamera berhasil disimpan dengan ID: " + documentReference.getId(), Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Log.e(TAG, "Error saving to Firestore", e);
                    Toast.makeText(addCameraActivity.this, "Gagal menyimpan data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}