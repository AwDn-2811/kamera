package com.example.android_projects;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class addCameraActivity extends AppCompatActivity {

    // Variabel UI
    ImageView camera_preview;
    EditText input_brand, input_model, input_type, input_resolution, input_price, input_location;
    Button btn_choose, btn_save;

    // Variabel Data
    Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Inisialisasi Firebase
        FirebaseApp.initializeApp(this);

        // 2. Inisialisasi Cloudinary (Cek agar tidak double init)
        try {
            MediaManager.get();
        } catch (Exception e) {
            initCloudinary();
        }

        setContentView(R.layout.activity_add_camera);

        // 3. Binding Views
        camera_preview = findViewById(R.id.camera_preview);
        input_brand = findViewById(R.id.input_brand);
        input_model = findViewById(R.id.input_model);
        input_type = findViewById(R.id.input_type);
        input_resolution = findViewById(R.id.input_resolution);
        input_price = findViewById(R.id.input_price);
        input_location = findViewById(R.id.input_location);

        btn_choose = findViewById(R.id.btn_choose_image);
        btn_save = findViewById(R.id.btn_save_camera);

        // 4. Set Listener
        btn_choose.setOnClickListener(v -> chooseImage());
        btn_save.setOnClickListener(v -> saveCamera());
    }

    private void initCloudinary() {
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", "dskqwokji");
        config.put("api_key", "868821764693376");
        config.put("api_secret", "lEgo-9HFqAICSHAvYj64WkG46Ew");
        MediaManager.init(this, config);
    }

    private void chooseImage() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 200 && data != null) {
            imageUri = data.getData();
            try {
                Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                camera_preview.setImageBitmap(bmp);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Validasi Input (Menggunakan logika kode utama yang lebih lengkap)
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

    private void saveCamera() {
        if (!validateInput()) return;

        Toast.makeText(this, "Sedang mengupload gambar...", Toast.LENGTH_SHORT).show();

        // Upload ke Cloudinary
        MediaManager.get().upload(imageUri)
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        // Ambil URL gambar
                        String imageUrl = resultData.get("secure_url").toString();

                        // Panggil fungsi simpan ke Firestore (GABUNGAN BARU)
                        saveCameraDataToFirestore(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(addCameraActivity.this,
                                "Upload Gambar Gagal: " + error.getDescription(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    // === FUNGSI PENYIMPANAN BARU (MENGGUNAKAN FIRESTORE) ===
    private void saveCameraDataToFirestore(String imageUrl) {
        // 1. Inisialisasi Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 2. Ambil User ID (Pemilik Kamera)
        String ownerId = "";
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            ownerId = user.getUid();
        }

        // 3. Konversi Harga ke Double (Angka)
        double priceValue = 0;
        try {
            priceValue = Double.parseDouble(input_price.getText().toString().trim());
        } catch (NumberFormatException e) {
            priceValue = 0;
        }

        // 4. Siapkan Data Map
        Map<String, Object> cam = new HashMap<>();
        cam.put("brand", input_brand.getText().toString().trim());
        cam.put("model", input_model.getText().toString().trim());
        cam.put("type", input_type.getText().toString().trim());
        cam.put("resolution", input_resolution.getText().toString().trim());
        cam.put("price", priceValue); // Disimpan sebagai Angka
        cam.put("location", input_location.getText().toString().trim());
        cam.put("imageUrl", imageUrl);
        cam.put("ownerID", ownerId);
        cam.put("isAvailable", true); // Status default

        // 5. Simpan ke Collection "cameras"
        db.collection("cameras")
                .add(cam)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(addCameraActivity.this, "Kamera berhasil disimpan!", Toast.LENGTH_SHORT).show();
                    finish(); // Tutup activity setelah sukses
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(addCameraActivity.this, "Gagal menyimpan ke database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}