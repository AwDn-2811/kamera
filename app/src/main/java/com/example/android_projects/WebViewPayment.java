package com.example.android_projects;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class WebViewPayment extends AppCompatActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        String url = getIntent().getStringExtra("snapUrl");

        Log.d("MIDTRANS", "Snap URL = " + url);

        if (url == null || url.isEmpty()) {
            Log.e("MIDTRANS", "Snap URL kosong! Tidak bisa memuat pembayaran");
            return;
        }

        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String requestUrl) {

                Log.d("MIDTRANS", "URL sekarang: " + requestUrl);

                // ✔️ Midtrans sandbox redirect ke example.com jika berhasil
                if (requestUrl.contains("example.com")) {
                    saveBookingToFirebase();
                    return true;
                }

                // ✔️ Kalau ada kode sukses tambahan
                if (requestUrl.contains("status_code=200") ||
                        requestUrl.contains("status=success")) {

                    saveBookingToFirebase();
                    return true;
                }

                view.loadUrl(requestUrl);
                return true;
            }
        });

        webView.loadUrl(url);
    }

    private void saveBookingToFirebase() {

        String cameraID = getIntent().getStringExtra("cameraID");
        String fromDate = getIntent().getStringExtra("fromDate");
        String toDate = getIntent().getStringExtra("toDate");
        String price = getIntent().getStringExtra("price");
        String location = getIntent().getStringExtra("location");
        String imageUrl = getIntent().getStringExtra("imageUrl");

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("bookings")
                .push();

        ref.child("cameraID").setValue(cameraID);
        ref.child("fromDate").setValue(fromDate);
        ref.child("toDate").setValue(toDate);
        ref.child("price").setValue(price);
        ref.child("location").setValue(location);
        ref.child("paymentMethod").setValue("Midtrans");
        ref.child("status").setValue("Paid");
        ref.child("imageUrl").setValue(imageUrl);

        Log.d("MIDTRANS", "Booking berhasil disimpan ke Firebase");

        finish();
    }
}
