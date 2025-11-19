package com.example.android_projects;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RentalHistoryAdapter extends RecyclerView.Adapter<RentalHistoryAdapter.HistoryHolder> {

    Context context;
    ArrayList<RentalHistory> list;

    public RentalHistoryAdapter(Context context, ArrayList<RentalHistory> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public HistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_rental_history, parent, false);
        return new HistoryHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryHolder holder, int position) {

        RentalHistory item = list.get(position);

        holder.txt_title.setText(item.title);
        holder.txt_date.setText(item.fromDate + " - " + item.toDate);
        holder.txt_status.setText("Status: " + item.status);

        // ===== LOAD IMAGE DARI CLOUDINARY URL =====
        if (item.imageUrl != null && !item.imageUrl.isEmpty()) {
            Picasso.get().load(item.imageUrl)
                    .into(holder.img_camera);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class HistoryHolder extends RecyclerView.ViewHolder {

        ImageView img_camera;
        TextView txt_title, txt_date, txt_status;

        public HistoryHolder(@NonNull View itemView) {
            super(itemView);

            img_camera = itemView.findViewById(R.id.img_history_camera);
            txt_title = itemView.findViewById(R.id.txt_history_title);
            txt_date = itemView.findViewById(R.id.txt_history_date);
            txt_status = itemView.findViewById(R.id.txt_history_status);
        }
    }
}
