package com.example.android_projects;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ReviewCameraAdapter extends RecyclerView.Adapter<ReviewCameraAdapter.ViewHolder> {

    Context context;
    ArrayList<ReviewCamera> list;

    public ReviewCameraAdapter(Context context, ArrayList<ReviewCamera> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_review_camera, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReviewCamera item = list.get(position);

        holder.name.setText(item.userName);
        holder.comment.setText(item.comment);
        holder.ratingBar.setRating(item.rating);

        // Load image dari Cloudinary URL
        if (item.userPhoto != null && !item.userPhoto.isEmpty()) {
            Picasso.get().load(item.userPhoto).into(holder.photo);
        } else {
            holder.photo.setImageResource(R.drawable.ic_user); // fallback icon
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name, comment;
        RatingBar ratingBar;
        ImageView photo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.review_user_name);
            comment = itemView.findViewById(R.id.review_comment);
            ratingBar = itemView.findViewById(R.id.review_rating);
            photo = itemView.findViewById(R.id.review_user_photo);
        }
    }
}
