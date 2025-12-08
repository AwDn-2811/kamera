package com.example.android_projects;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class WebViewPayment extends AppCompatActivity {

    WebView webView;
    boolean alreadySaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        String url = getIntent().getStringExtra("snapUrl");

        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String loadedUrl) {
                Log.d("MIDTRANS", "Loaded URL: " + loadedUrl);

                // cek hanya setelah halaman Sukses benar tampil
                if (isSuccessUrl(loadedUrl) && !alreadySaved) {
                    alreadySaved = true;

                    // kasih waktu user lihat notifikasi sukses midtrans dulu
                    webView.postDelayed(() -> {

                        saveBookingToFirebase(); // simpan booking
                        goToHistory(); // pindah activity

                    }, 1500);
                }
            }
        });

        webView.loadUrl(url);
    }

    private boolean isSuccessUrl(String url) {
        if (url == null) return false;

        return url.contains("simulator.sandbox.midtrans.com")
                || url.contains("status")
                || url.contains("paid")
                || url.contains("success")
                || url.contains("status_code=200")
                || url.contains("transaction");
    }

    private void saveBookingToFirebase() {

        String cameraID = getIntent().getStringExtra("cameraID");
        String fromDate = getIntent().getStringExtra("fromDate");
        String toDate = getIntent().getStringExtra("toDate");
        String price = getIntent().getStringExtra("price");
        String location = getIntent().getStringExtra("location");
        String imageUrl = getIntent().getStringExtra("imageUrl");

        String userID = getSharedPreferences("UserData", MODE_PRIVATE)
                .getString("userId", "");

        String bookingID = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userID)
                .child("rentals")
                .push()
                .getKey();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userID)
                .child("rentals")
                .child(bookingID);

        ref.child("cameraID").setValue(cameraID);
        ref.child("fromDate").setValue(fromDate);
        ref.child("toDate").setValue(toDate);
        ref.child("price").setValue(price);
        ref.child("location").setValue(location);
        ref.child("paymentMethod").setValue("Midtrans");
        ref.child("status").setValue("Paid");
        ref.child("imageUrl").setValue(imageUrl);

        Log.d("MIDTRANS", "Booking berhasil disimpan");

        // berpindah Activity
        Intent intent = new Intent(WebViewPayment.this, RentalHistoryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        finish();
    }


    private void goToHistory() {
        Toast.makeText(this, "Pembayaran berhasil!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, RentalHistoryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        finish();
    }
}
