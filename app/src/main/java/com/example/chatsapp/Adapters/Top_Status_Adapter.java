package com.example.chatsapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatsapp.Activities.status_activity;
import com.example.chatsapp.Models.Status;
import com.example.chatsapp.Models.User_Status;
import com.example.chatsapp.R;
import com.example.chatsapp.databinding.ItemStatusBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class Top_Status_Adapter extends RecyclerView.Adapter<Top_Status_Adapter.TopStatusViewHolder> {

    Context context;
    ArrayList<User_Status> userStatuses;

    public Top_Status_Adapter(Context context, ArrayList<User_Status> userStatuses) {
        this.context = context;
        this.userStatuses = userStatuses;
       // sortUserStatusesByLastUpdatedTime();  // Sort the user statuses when initializing the adapter
    }

    @NonNull
    @Override
    public TopStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_status, parent, false);
        return new TopStatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopStatusViewHolder holder, int position) {
        User_Status userStatus = userStatuses.get(position);
        Status lastStatus = userStatus.getStatuses().get(userStatus.getStatuses().size() - 1);

        // Load the image using Glide
        Glide.with(context).load(lastStatus.getImageUrl()).into(holder.binding.image);

        // Set the portion count
        holder.binding.circularStatusView.setPortionsCount(userStatus.getStatuses().size());

        // Set the username
        holder.binding.username.setText(userStatus.getName());

        if (lastStatus.getTimeStamp() == 0) {
            holder.binding.lastUpdatedTime.setText("");
        } else {
            // Get current time
            long currentTime = System.currentTimeMillis();
            long lastUpdatedTimeStamp = lastStatus.getTimeStamp();

            // Calculate time difference in milliseconds
            long timeDifference = currentTime - lastUpdatedTimeStamp;

            // Format for time
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            // Format for date
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            if (timeDifference < 24 * 60 * 60 * 1000) { // within 24 hours
                String formattedTime = timeFormat.format(new Date(lastUpdatedTimeStamp));
                holder.binding.lastUpdatedTime.setText(formattedTime);
            } else {
                // Get calendar instance
                Calendar messageCalendar = Calendar.getInstance();
                messageCalendar.setTimeInMillis(lastUpdatedTimeStamp);
                int messageDayOfYear = messageCalendar.get(Calendar.DAY_OF_YEAR);

                Calendar currentCalendar = Calendar.getInstance();
                currentCalendar.setTimeInMillis(currentTime);
                int currentDayOfYear = currentCalendar.get(Calendar.DAY_OF_YEAR);

                if (messageDayOfYear == currentDayOfYear - 1) {
                    holder.binding.lastUpdatedTime.setText("Yesterday");
                } else {
                    String formattedDate = dateFormat.format(new Date(lastUpdatedTimeStamp));
                    holder.binding.lastUpdatedTime.setText(formattedDate);
                }
            }
        }


        holder.binding.statusItemLayout.setOnClickListener(v -> {
            ArrayList<MyStory> myStories = new ArrayList<>();
            for (Status status : userStatus.getStatuses()) {
                myStories.add(new MyStory(status.getImageUrl()));
            }

            new StoryView.Builder(((status_activity) context).getSupportFragmentManager())
                    .setStoriesList(myStories) // Required
                    .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                    .setTitleText(userStatus.getName()) // Default is Hidden
                    .setSubtitleText("") // Default is Hidden
                    .setTitleLogoUrl(userStatus.getProfileImage()) // Default is Hidden
                    .setStoryClickListeners(new StoryClickListeners() {
                        @Override
                        public void onDescriptionClickListener(int position1) {
                            // your action
                        }

                        @Override
                        public void onTitleIconClickListener(int position1) {
                            // your action
                        }
                    }) // Optional Listeners
                    .build() // Must be called before calling show method
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return userStatuses.size();
    }



    public static class TopStatusViewHolder extends RecyclerView.ViewHolder {

        ItemStatusBinding binding;

        public TopStatusViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemStatusBinding.bind(itemView);
        }
    }
}
