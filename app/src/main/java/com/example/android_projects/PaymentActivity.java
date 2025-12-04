package com.example.android_projects;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentActivity extends AppCompatActivity {

    WebView webPayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        webPayment = findViewById(R.id.webview_payment);

        String url = getIntent().getStringExtra("payment_url");

        WebSettings settings = webPayment.getSettings();
        settings.setJavaScriptEnabled(true);

        webPayment.setWebViewClient(new WebViewClient());
        webPayment.loadUrl(url);
    }
}
