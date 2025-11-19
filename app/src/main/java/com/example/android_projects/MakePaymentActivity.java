package com.example.android_projects;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class MakePaymentActivity extends AppCompatActivity {

    ImageView camera_img;
    TextView txt_price, txt_dates, txt_location;
    Button btn_confirm;

    String cameraID = "";
    String userID = "";
    String fromDate = "";
    String toDate = "";
    String price = "";
    String location = "";
    String imageUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_payment);

        cameraID = getIntent().getStringExtra("cameraID");

        SharedPreferences shared = getSharedPreferences("dates", MODE_PRIVATE);
        fromDate = shared.getString("fromDate", "-");
        toDate = shared.getString("toDate", "-");

        SharedPreferences sharedUser = getSharedPreferences("UserData", MODE_PRIVATE);
        userID = sharedUser.getString("userId", "");

        initViews();
        loadCameraData();

        btn_confirm.setOnClickListener(v -> confirmBooking());
    }

    private void initViews() {
        camera_img = findViewById(R.id.camera_img);
        txt_price = findViewById(R.id.txt_price);
        txt_dates = findViewById(R.id.txt_dates);
        txt_location = findViewById(R.id.txt_location);
        btn_confirm = findViewById(R.id.btn_confirm);

        txt_dates.setText("Tanggal: " + fromDate + " - " + toDate);
    }

    private void loadCameraData() {
        DatabaseReference ref =
                FirebaseDatabase.getInstance().getReference("cameras").child(cameraID);

        ref.get().addOnSuccessListener(snapshot -> {

            price = snapshot.child("price").getValue(String.class);
            location = snapshot.child("location").getValue(String.class);
            imageUrl = snapshot.child("imageUrl").getValue(String.class); // URL CLOUDINARY

            txt_price.setText("Harga: " + price + " / hari");
            txt_location.setText("Lokasi: " + location);

            // ⚡ MUAT GAMBAR DARI CLOUDINARY
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get().load(imageUrl).into(camera_img);
            }

        });
    }

    private void confirmBooking() {

        DatabaseReference bookingRef =
                FirebaseDatabase.getInstance().getReference("bookings").push();

        bookingRef.child("cameraID").setValue(cameraID);
        bookingRef.child("userID").setValue(userID);
        bookingRef.child("fromDate").setValue(fromDate);
        bookingRef.child("toDate").setValue(toDate);
        bookingRef.child("paymentMethod").setValue("On-Site");
        bookingRef.child("location").setValue(location);
        bookingRef.child("pricePerDay").setValue(price);
        bookingRef.child("imageUrl").setValue(imageUrl); // opsional

        Toast.makeText(this, "Pemesanan berhasil! Bayar di tempat.", Toast.LENGTH_LONG).show();

        finish();
    }
}
