package com.example.android_projects;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Disarankan nama kelas diawali huruf Kapital (RegistrationActivity), tapi ini saya biarkan sesuai aslinya
public class registration extends AppCompatActivity {

    // Deklarasi Variabel
    EditText firstname_txt, lastname_txt, email_txt, number_txt, password;
    Spinner gender_spn;
    Button registration_btn, login_page_btn;

    // Default DOB (bisa diganti DatePicker nanti)
    String dob_txt = "1/1/1900";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inisialisasi Firebase (Penting agar tidak Crash)
        FirebaseApp.initializeApp(this);

        setContentView(R.layout.activity_registration);

        setupViews();
    }

    private void setupViews() {
        firstname_txt = findViewById(R.id.firstname_txt);
        lastname_txt = findViewById(R.id.lastname_txt);
        email_txt = findViewById(R.id.email_txt);
        password = findViewById(R.id.password);
        gender_spn = findViewById(R.id.gender_spn);
        number_txt = findViewById(R.id.number_txt);

        registration_btn = findViewById(R.id.registration_btn);
        login_page_btn = findViewById(R.id.login_page_btn);

        // Setup Spinner dengan Layout Bawaan Android (Lebih Aman)
        String[] genderChoice = {"Select Your Gender", "Female", "Male"};
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item, // Menggunakan layout default android
                genderChoice
        );
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender_spn.setAdapter(genderAdapter);

        // Listener Tombol Register
        registration_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkInformation()) {
                    writeToDatabase();
                }
            }
        });

        // Listener Tombol Login
        login_page_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(registration.this, login.class);
                startActivity(intent);
                finish(); // Opsional: agar user tidak bisa back ke halaman register
            }
        });
    }

    // Helper untuk cek format email
    boolean isEmailValid(CharSequence email) {
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    private boolean checkInformation() {
        if (firstname_txt.getText().toString().trim().isEmpty()) {
            firstname_txt.setError("First Name is Required");
            firstname_txt.requestFocus();
            return false;
        }

        if (lastname_txt.getText().toString().trim().isEmpty()) {
            lastname_txt.setError("Last Name is Required");
            lastname_txt.requestFocus();
            return false;
        }

        if (email_txt.getText().toString().trim().isEmpty()) {
            email_txt.setError("Email is Required");
            return false;
        } else if (!isEmailValid(email_txt.getText().toString())) {
            email_txt.setError("Please Enter a Valid Email!");
            return false;
        }

        if (number_txt.getText().toString().trim().isEmpty()) {
            number_txt.setError("Phone Number is Required");
            return false;
        }

        // Validasi Spinner (Agar user tidak memilih "Select Your Gender")
        if (gender_spn.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select your gender!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.getText().toString().isEmpty() || password.length() < 6) {
            password.setError("Password must be at least 6 characters");
            return false;
        }

        return true;
    }

    private void writeToDatabase() {
        // Tampilkan indikator loading (bisa pakai Toast dulu untuk sederhana)
        Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show();
        registration_btn.setEnabled(false); // Cegah klik ganda

        String uuid = UUID.randomUUID().toString();

        Map<String, Object> user = new HashMap<>();
        user.put("userID", uuid); // Simpan ID juga di dalam objek biar mudah dicari
        user.put("firstName", firstname_txt.getText().toString().trim());
        user.put("lastName", lastname_txt.getText().toString().trim());
        user.put("dob", dob_txt);
        user.put("gender", gender_spn.getSelectedItem().toString());
        user.put("email", email_txt.getText().toString().trim());
        user.put("password", password.getText().toString()); // CATATAN: Password disimpan plain text (Tidak aman untuk production)
        user.put("phoneNumber", number_txt.getText().toString().trim());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users");

        // LOGIKA PENTING: Gunakan Listener untuk memastikan data tersimpan
        myRef.child(uuid).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    // 1. Simpan sesi lokal
                    SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userID", uuid);
                    editor.putString("userName", firstname_txt.getText().toString()); // Opsional
                    editor.apply();

                    // 2. Beri notifikasi sukses
                    Toast.makeText(registration.this, "Registration Successful!", Toast.LENGTH_SHORT).show();

                    // 3. Pindah Halaman
                    Intent intent = new Intent(registration.this, login.class);
                    startActivity(intent);
                    finish(); // Tutup halaman register
                })
                .addOnFailureListener(e -> {
                    // Jika gagal (misal tidak ada internet)
                    registration_btn.setEnabled(true);
                    Toast.makeText(registration.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}