package com.example.android_projects;

// Pastikan import ini tidak merah. Jika merah, cek bagian 'Dependencies' di bawah.
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
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

        // 3. Hubungkan dengan layout XML
        // PENTING: Pastikan nama file XML Anda adalah 'activity_add_camera.xml'
        setContentView(R.layout.activity_add_camera);

        // 4. Binding ID (Menghubungkan variabel Java dengan elemen XML)
        // ID ini harus SAMA PERSIS dengan android:id di file XML
        camera_preview = findViewById(R.id.camera_preview);
        input_brand = findViewById(R.id.input_brand);
        input_model = findViewById(R.id.input_model);
        input_type = findViewById(R.id.input_type);
        input_resolution = findViewById(R.id.input_resolution);
        input_price = findViewById(R.id.input_price);
        input_location = findViewById(R.id.input_location);

        btn_choose = findViewById(R.id.btn_choose_image);
        btn_save = findViewById(R.id.btn_save_camera);

        // 5. Set Listener Tombol
        btn_choose.setOnClickListener(v -> chooseImage());
        btn_save.setOnClickListener(v -> saveCamera());
    }

    private void initCloudinary() {
        // Menggunakan Map<String, Object> agar lebih spesifik dan tidak merah/warning
        Map<String, Object> config = new HashMap<>();
        config.put("cloud_name", "dskqwokji");
        config.put("api_key", "868821764693376");
        config.put("api_secret", "lEgo-9HFqAICSHAvYj64WkG46Ew");
        // Catatan Keamanan: Sebaiknya api_secret jangan ditaruh di sini untuk produksi.

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
        if (input_type.getText().toString().trim().isEmpty()) {
            input_type.setError("Sensor wajib diisi");
            return false;
        }
        if (input_resolution.getText().toString().trim().isEmpty()) {
            input_resolution.setError("Resolusi wajib diisi");
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
                    @Override
                    public void onStart(String requestId) {
                        // Opsional: Tampilkan loading bar
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // Opsional: Update progress bar
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        // Ambil URL gambar yang sudah diupload
                        String imageUrl = resultData.get("secure_url").toString();
                        // Simpan data teks ke Firebase
                        saveCameraDataToFirebase(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(addCameraActivity.this,
                                "Upload Gagal: " + error.getDescription(),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    private void saveCameraDataToFirebase(String imageUrl) {
        String id = UUID.randomUUID().toString();

        Map<String, Object> cam = new HashMap<>();
        cam.put("brand", input_brand.getText().toString().trim());
        cam.put("model", input_model.getText().toString().trim());
        cam.put("type", input_type.getText().toString().trim());
        cam.put("resolution", input_resolution.getText().toString().trim());
        cam.put("price", input_price.getText().toString().trim());
        cam.put("location", input_location.getText().toString().trim());
        cam.put("imageUrl", imageUrl);

        FirebaseDatabase.getInstance().getReference("cameras")
                .child(id)
                .setValue(cam)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(addCameraActivity.this, "Kamera berhasil disimpan!", Toast.LENGTH_SHORT).show();
                    finish(); // Tutup activity setelah sukses
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(addCameraActivity.this, "Gagal menyimpan ke database: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}