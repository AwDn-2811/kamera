package com.example.android_projects;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Imports untuk Firestore
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;

public class HomePage extends AppCompatActivity {
    private static final String TAG = "HomePage";

    private EditText edt_pickupLocation;
    private TextView tf_startDate, tf_endDate, tf_warning;
    private Button btn_search;

    // Menu Navigasi
    private LinearLayout menu_addCamera;
    private LinearLayout menu_feedback;
    private LinearLayout menu_home;
    private LinearLayout menu_aboutus;
    private LinearLayout menu_account;

    private RecyclerView recycler_layout;

    // Adapter & List untuk Kamera
    private HomeCameraAdapter cameraAdapter;
    private ArrayList<HomeCameraCard> listOfCameras; // Menggunakan HomeCameraCard

    // Inisialisasi Firestore
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        setupView();
    }

    private void setupView() {
        edt_pickupLocation = findViewById(R.id.edt_pickupLocation);
        tf_startDate = findViewById(R.id.tf_pickupDate);
        tf_endDate = findViewById(R.id.tf_dropoffDate);
        btn_search = findViewById(R.id.btn_search);
        tf_warning = findViewById(R.id.tf_warning);

        // Inisialisasi Menu
        menu_addCamera = findViewById(R.id.menu_addCar);
        menu_feedback = findViewById(R.id.menu_feedback);
        menu_home = findViewById(R.id.menu_home);
        menu_aboutus = findViewById(R.id.menu_aboutus);
        menu_account = findViewById(R.id.menu_account);

        recycler_layout = findViewById(R.id.recycler_layout);

        // Load Data Kamera Terdekat
        getNearCameras();

        // Setup Date Pickers
        setupDatePickers();

        // Tombol Cari
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSearch();
            }
        });

        // Navigasi Menu
        menu_addCamera.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), addCameraActivity.class);
            startActivity(i);
        });

        menu_feedback.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), feedback.class);
            startActivity(i);
        });

        menu_aboutus.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), AboutUs.class);
            startActivity(i);
        });

        menu_account.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), Profile.class);
            startActivity(i);
        });
    }

    private void setupDatePickers() {
        final Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        // ... (Logika DatePickerDialog tidak berubah)
        tf_startDate.setOnClickListener(view -> {
            DatePickerDialog dialog = new DatePickerDialog(HomePage.this, (view1, year1, month1, dayOfMonth) -> {
                month1 = month1 + 1;
                String date = dayOfMonth + "/" + month1 + "/" + year1;
                tf_startDate.setText(date);
            }, year, month, day);
            dialog.show();
        });

        tf_endDate.setOnClickListener(view -> {
            DatePickerDialog dialog = new DatePickerDialog(HomePage.this, (view1, year1, month1, dayOfMonth) -> {
                month1 = month1 + 1;
                String date = dayOfMonth + "/" + month1 + "/" + year1;
                tf_endDate.setText(date);
            }, year, month, day);
            dialog.show();
        });
    }

    private void handleSearch() {
        if (edt_pickupLocation.getText().toString().isEmpty()) {
            tf_warning.setText("Lokasi pengambilan harus diisi");
        } else if (tf_startDate.getText().toString().isEmpty()) {
            tf_warning.setText("Tanggal mulai sewa harus diisi");
        } else if (tf_endDate.getText().toString().isEmpty()) {
            tf_warning.setText("Tanggal selesai sewa harus diisi");
        } else {
            String location = edt_pickupLocation.getText().toString();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");

            try {
                LocalDate startDate = LocalDate.parse(tf_startDate.getText().toString(), formatter);
                LocalDate endDate = LocalDate.parse(tf_endDate.getText().toString(), formatter);

                if (startDate.isBefore(LocalDate.now())) {
                    tf_warning.setText("Tanggal sewa tidak boleh di masa lalu");
                } else if (startDate.isAfter(endDate)) {
                    tf_warning.setText("Tanggal selesai harus setelah tanggal mulai");
                } else {
                    // Pindah ke halaman List Kamera
                    Intent i = new Intent(getApplicationContext(), CamerasAvailable.class);
                    i.putExtra("location", location);
                    i.putExtra("startDate", startDate.toString());
                    i.putExtra("endDate", endDate.toString());

                    // Simpan preferensi tanggal
                    SharedPreferences sharedPreferences = getSharedPreferences("dates", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("fromDate", startDate.toString());
                    editor.putString("toDate", endDate.toString());
                    editor.apply();

                    startActivity(i);
                }
            } catch (Exception e) {
                // Logika ini menangkap error format tanggal (java.time)
                Log.e(TAG, "Error parsing date: " + e.getMessage());
                tf_warning.setText("Format tanggal salah");
            }
        }
    }

    /**
     * Mengambil daftar kamera dari Firestore dan memfilter berdasarkan lokasi (simulasi).
     * Gambar dimuat menggunakan URL Cloudinary yang tersimpan.
     */
    private void getNearCameras() {
        recycler_layout.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recycler_layout.setLayoutManager(layoutManager);

        listOfCameras = new ArrayList<>();
        // Inisialisasi adapter dengan list kosong
        cameraAdapter = new HomeCameraAdapter(this, listOfCameras);
        recycler_layout.setAdapter(cameraAdapter);

        // 1. Ambil Lokasi User (untuk simulasi jarak)
        LocationHelper locationHelper = new LocationHelper(this);
        // Diasumsikan getLocation mengembalikan "lat,long" atau string lokasi
        String myLocation = locationHelper.getLocation();

        // 2. Query Firestore Collection 'cameras'
        db.collection("cameras")
                // Opsional: Batasi jumlah data yang diambil
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listOfCameras.clear();
                    int count = 0;

                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {

                        // Ambil data yang dibutuhkan dari Firestore Document
                        String brand = document.getString("brand");
                        String model = document.getString("model");
                        // Asumsi field harga di Firestore adalah 'pricePerDay' (double)
                        Number price = document.getDouble("pricePerDay");
                        String imageUrl = document.getString("imageUrl"); // URL Cloudinary

                        // Ambil lokasi (jika menggunakan Double biasa)
                        // Double camLatitude = document.getDouble("latitude");
                        // Double camLongitude = document.getDouble("longitude");

                        // --- LOGIC FILTER JARAK (Disarankan menggunakan Geohashing/Cloud Function untuk performa) ---
                        // Untuk demonstrasi, kita hanya memproses 6 item yang available
                        Boolean isAvailable = document.getBoolean("isAvailable");

                        if (isAvailable != null && isAvailable && count < 6) {

                            // Logika Hitung Jarak Sederhana (dipertahankan dari kode lama, tapi harus lebih aman)
                            // Skip penghitungan jarak yang kompleks untuk fokus pada migrasi data

                            final HomeCameraCard camModel = new HomeCameraCard();
                            camModel.id = document.getId();
                            camModel.name = brand + " " + model;

                            // Format Harga
                            camModel.price = "Rp " + (price != null ? price.intValue() : 0) + " /Hari";

                            // SET URL CLOUDINARY
                            // Adapter Anda (HomeCameraAdapter) sekarang HARUS memuat gambar
                            // secara asinkron dari URL ini menggunakan Glide/Picasso.
                            camModel.setImageUrl(imageUrl);

                            listOfCameras.add(camModel);
                            count++;
                        }
                    }

                    // Update tampilan setelah semua data diproses
                    if (listOfCameras.isEmpty()) {
                        Toast.makeText(HomePage.this, "Tidak ada kamera ditemukan.", Toast.LENGTH_SHORT).show();
                    }
                    cameraAdapter.notifyDataSetChanged();

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading cameras from Firestore: ", e);
                    Toast.makeText(HomePage.this, "Gagal memuat data kamera: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // FUNGSI getCameraImage DIHAPUS KARENA SUDAH TIDAK MENGGUNAKAN FIREBASE STORAGE
}