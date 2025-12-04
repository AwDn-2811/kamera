package com.example.android_projects;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class addCameraActivity extends AppCompatActivity {

    private static final String TAG = "AddCameraActivity";
    private static final int PICK_IMAGE_REQUEST = 200;

    // UI
    ImageView camera_preview;
    EditText input_brand, input_model, input_type, input_resolution, input_price, input_location;
    Button btn_choose, btn_save;

    Uri imageUri = null;
    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initCloudinary();
        setContentView(R.layout.activity_add_camera);

        camera_preview   = findViewById(R.id.camera_preview);
        input_brand      = findViewById(R.id.input_brand);
        input_model      = findViewById(R.id.input_model);
        input_type       = findViewById(R.id.input_type);
        input_resolution = findViewById(R.id.input_resolution);
        input_price      = findViewById(R.id.input_price);
        input_location   = findViewById(R.id.input_location);

        btn_choose = findViewById(R.id.btn_choose_image);
        btn_save   = findViewById(R.id.btn_save_camera);

        progressDialog = new CustomProgressDialog(this);

        btn_choose.setOnClickListener(v -> chooseImage());
        btn_save.setOnClickListener(v -> saveCamera());
    }

    private void initCloudinary() {
        try {
            MediaManager.get();
        } catch (Exception e) {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", "dskqwokji");
            config.put("api_key",    "868821764693376");
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
                Log.e(TAG, "Load gambar gagal", e);
                Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean validateInput() {
        if (imageUri == null) {
            Toast.makeText(this, "Pilih gambar terlebih dahulu!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (input_brand.getText().toString().trim().isEmpty()) { input_brand.setError("Brand wajib diisi"); return false; }
        if (input_model.getText().toString().trim().isEmpty()) { input_model.setError("Model wajib diisi"); return false; }
        if (input_type.getText().toString().trim().isEmpty()) { input_type.setError("Tipe wajib diisi"); return false; }
        if (input_resolution.getText().toString().trim().isEmpty()) { input_resolution.setError("Resolusi wajib diisi"); return false; }
        if (input_price.getText().toString().trim().isEmpty()) { input_price.setError("Harga wajib diisi"); return false; }
        if (input_location.getText().toString().trim().isEmpty()) { input_location.setError("Lokasi wajib diisi"); return false; }
        return true;
    }

    private void saveCamera() {
        if (!validateInput()) return;

        progressDialog.show("Mengunggah gambar...");

        MediaManager.get()
                .upload(imageUri)
                .option("folder", "camera_rentals")
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}

                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        saveCameraDataToRealtimeDb(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        progressDialog.dismiss();
                        Toast.makeText(addCameraActivity.this,
                                "Gagal upload gambar: " + error.getDescription(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    // ==================================================
    // SAVE CAMERA DATA (FINAL STRUCTURE MATCH)
    // ==================================================
    private void saveCameraDataToRealtimeDb(String imageUrl) {

        String brand      = input_brand.getText().toString().trim();
        String model      = input_model.getText().toString().trim();
        String type       = input_type.getText().toString().trim();
        String resolution = input_resolution.getText().toString().trim();
        String address    = input_location.getText().toString().trim();

        double priceValue;
        try { priceValue = Double.parseDouble(input_price.getText().toString().trim()); }
        catch (Exception e) { priceValue = 0; }

        // ambil user ID owner
        SharedPreferences sp = getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String ownerId = sp.getString("userId", null);

        if (ownerId == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Session habis, login ulang!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, login.class));
            finish();
            return;
        }

        // FINAL DATA sesuai Firebase structure
        Map<String, Object> cam = new HashMap<>();

        cam.put("brand", brand);
        cam.put("model", model);
        cam.put("type", type);
        cam.put("resolution", resolution);

        // 🔥 DESKRIPSI SESUAI CAMERA INFORMATION
        cam.put("description", type + " / " + resolution);

        cam.put("pricePerDay", priceValue);
        cam.put("address", address);

        // lokasi string (sesuai struktur database lo)
        cam.put("location", address);

        cam.put("imageUrl", imageUrl);
        cam.put("ownerId", ownerId);

        cam.put("isAvailable", true);
        cam.put("ratingAverage", 0);
        cam.put("reviewCount", 0);
        cam.put("createdAt", ServerValue.TIMESTAMP);

        // push ke Firebase
        DatabaseReference camerasRef =
                FirebaseDatabase.getInstance().getReference("cameras");

        String cameraId = camerasRef.push().getKey();

        camerasRef.child(cameraId)
                .setValue(cam)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(addCameraActivity.this,
                            "Kamera berhasil disimpan!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(addCameraActivity.this,
                            "Gagal menyimpan data: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
