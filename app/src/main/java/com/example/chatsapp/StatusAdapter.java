//package com.example.chatsapp;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//import com.bumptech.glide.Glide;
//import java.util.ArrayList;
//
//public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.StatusViewHolder> {
//
//    private Context context;
//    private ArrayList<Status> statuses;
//
//    public StatusAdapter(Context context, ArrayList<Status> statuses) {
//        this.context = context;
//        this.statuses = statuses;
//    }
//
//    @NonNull
//    @Override
//    public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.item_status, parent, false);
//        return new StatusViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull StatusViewHolder holder, int position) {
//        Status status = statuses.get(position);
//        holder.nameTextView.setText(status.getName());
//        holder.updatedTimeTextView.setText(status.getUpdatedTime());
//        Glide.with(context).load(status.getProfileImage()).into(holder.profileImageView);
//    }
//
//    @Override
//    public int getItemCount() {
//        return statuses.size();
//    }
//
//    static class StatusViewHolder extends RecyclerView.ViewHolder {
//
//        ImageView profileImageView;
//        TextView nameTextView;
//        TextView updatedTimeTextView;
//
//        public StatusViewHolder(@NonNull View itemView) {
//            super(itemView);
//            profileImageView = itemView.findViewById(R.id.profileImageView);
//            nameTextView = itemView.findViewById(R.id.nameTextView);
//            updatedTimeTextView = itemView.findViewById(R.id.updatedTimeTextView);
//        }
//    }
//}
//
