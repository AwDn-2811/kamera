package com.example.android_projects;

import android.os.Bundle;
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

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.contains("status_code=200")) {
                    saveBookingToFirebase();
                    return true;
                }

                view.loadUrl(url);
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

        finish();
    }
}
