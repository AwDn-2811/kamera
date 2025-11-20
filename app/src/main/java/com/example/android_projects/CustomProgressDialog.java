package com.example.android_projects;

import android.app.ProgressDialog;
import android.content.Context;

// Kelas ini mensimulasikan custom dialog untuk indikator loading
// Menggunakan ProgressDialog deprecated, tapi lebih cepat diimplementasikan
// untuk contoh ini. Dalam produksi, gunakan ProgressBar atau AlertDialog custom.
public class CustomProgressDialog {

    private final ProgressDialog dialog;

    public CustomProgressDialog(Context context) {
        dialog = new ProgressDialog(context);
        dialog.setCancelable(false); // Tidak bisa dibatalkan user
        dialog.setTitle("Mohon Tunggu");
    }

    public void show(String message) {
        dialog.setMessage(message);
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}