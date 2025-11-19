package com.example.android_projects;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AvailableCameraAdapter extends RecyclerView.Adapter<AvailableCameraAdapter.CameraHolder> {

    Context context;
    ArrayList<AvailableCamera> list;

    public AvailableCameraAdapter(Context context, ArrayList<AvailableCamera> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public CameraHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.camera_available_item, parent, false);
        return new CameraHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CameraHolder holder, int position) {

        AvailableCamera cam = list.get(position);

        holder.name.setText(cam.getBrand() + " " + cam.getModel());
        holder.type.setText(cam.getType());
        holder.price.setText("Rp " + cam.getPrice() + " / hari");

        // === PENTING: Load image dari Cloudinary URL ===
        if (cam.getImageUrl() != null && !cam.getImageUrl().isEmpty()) {
            Picasso.get().load(cam.getImageUrl()).into(holder.img);
        }

        holder.bind(cam);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class CameraHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name, type, price;
        ImageView img;
        AvailableCamera data;

        public CameraHolder(@NonNull View itemView) {
            super(itemView);

            img = itemView.findViewById(R.id.cam_img);
            name = itemView.findViewById(R.id.cam_name);
            type = itemView.findViewById(R.id.cam_type);
            price = itemView.findViewById(R.id.cam_price);

            itemView.setOnClickListener(this);
        }

        public void bind(AvailableCamera cam) {
            this.data = cam;
        }

        @Override
        public void onClick(View view) {
            Intent i = new Intent(view.getContext(), CameraInformation.class);
            i.putExtra("cameraID", data.getId());
            view.getContext().startActivity(i);
        }
    }
}
