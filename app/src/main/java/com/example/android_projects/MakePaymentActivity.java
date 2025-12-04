package com.example.android_projects;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

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

        btn_confirm.setOnClickListener(v -> startPayment());
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

            Long priceLong = snapshot.child("pricePerDay").getValue(Long.class);
            if (priceLong != null) {
                price = String.valueOf(priceLong);
            }
            location = snapshot.child("location").getValue(String.class);
            imageUrl = snapshot.child("imageUrl").getValue(String.class); // URL CLOUDINARY

            txt_price.setText("Harga: Rp " + price + " / hari");
            txt_location.setText("Lokasi: " + location);

            // ⚡ MUAT GAMBAR DARI CLOUDINARY
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get().load(imageUrl).into(camera_img);
            }

        });
    }

    private void startPayment() {

        if (price == null || price.isEmpty()) {
            Toast.makeText(this, "Harga tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("order_id", "ORDER-" + System.currentTimeMillis());
            body.put("gross_amount", Integer.parseInt(price));

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "http://192.168.1.27:3000/create-transaction";

            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST, url, body,
                    response -> {

                        try {
                            String snapUrl = response.getString("redirect_url");

                            Intent i = new Intent(MakePaymentActivity.this, WebViewPayment.class);
                            i.putExtra("snapUrl", snapUrl);
                            i.putExtra("cameraID", cameraID);
                            i.putExtra("fromDate", fromDate);
                            i.putExtra("toDate", toDate);
                            i.putExtra("price", price);
                            i.putExtra("location", location);
                            i.putExtra("imageUrl", imageUrl);
                            startActivity(i);

                        } catch (Exception e) {
                            Toast.makeText(this, "Gagal parsing Snap URL", Toast.LENGTH_SHORT).show();
                        }

                    },
                    error -> Toast.makeText(this, "Gagal koneksi server Midtrans", Toast.LENGTH_SHORT).show()
            );

            queue.add(req);

        } catch (Exception e) {
            Toast.makeText(this, "Error membuat transaksi", Toast.LENGTH_SHORT).show();
        }
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