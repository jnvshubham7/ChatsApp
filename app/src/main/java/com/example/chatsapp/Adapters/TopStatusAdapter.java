package com.example.chatsapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatsapp.Models.Status;
import com.example.chatsapp.Models.User_Status;
import com.example.chatsapp.R;
import com.example.chatsapp.databinding.ItemStatusBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import omari.hamza.storyview.StoryView;
import omari.hamza.storyview.callback.StoryClickListeners;
import omari.hamza.storyview.model.MyStory;

public class TopStatusAdapter extends RecyclerView.Adapter<TopStatusAdapter.TopStatusViewHolder> {

    private Context context;
    private ArrayList<User_Status> userStatuses;

    public TopStatusAdapter(Context context, ArrayList<User_Status> userStatuses) {
        this.context = context;
        this.userStatuses = userStatuses;
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

        // Set the last updated time
        setLastUpdatedTime(holder, lastStatus);

        holder.binding.statusItemLayout.setOnClickListener(v -> showStoryView(userStatus));
    }

    private void setLastUpdatedTime(@NonNull TopStatusViewHolder holder, Status lastStatus) {
        if (lastStatus.getTimeStamp() == 0) {
            holder.binding.lastUpdatedTime.setText("");
        } else {
            long currentTime = System.currentTimeMillis();
            long lastUpdatedTimeStamp = lastStatus.getTimeStamp();
            long timeDifference = currentTime - lastUpdatedTimeStamp;

            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            if (timeDifference < 24 * 60 * 60 * 1000) { // within 24 hours
                holder.binding.lastUpdatedTime.setText(timeFormat.format(new Date(lastUpdatedTimeStamp)));
            } else {
                Calendar messageCalendar = Calendar.getInstance();
                messageCalendar.setTimeInMillis(lastUpdatedTimeStamp);
                int messageDayOfYear = messageCalendar.get(Calendar.DAY_OF_YEAR);

                Calendar currentCalendar = Calendar.getInstance();
                currentCalendar.setTimeInMillis(currentTime);
                int currentDayOfYear = currentCalendar.get(Calendar.DAY_OF_YEAR);

                if (messageDayOfYear == currentDayOfYear - 1) {
                    holder.binding.lastUpdatedTime.setText("Yesterday");
                } else {
                    holder.binding.lastUpdatedTime.setText(dateFormat.format(new Date(lastUpdatedTimeStamp)));
                }
            }
        }
    }

    private void showStoryView(User_Status userStatus) {
        ArrayList<MyStory> myStories = new ArrayList<>();
        for (Status status : userStatus.getStatuses()) {
            myStories.add(new MyStory(status.getImageUrl()));
        }

        new StoryView.Builder(((FragmentActivity) context).getSupportFragmentManager())
                .setStoriesList(myStories)
                .setStoryDuration(5000)
                .setTitleText(userStatus.getName())
                .setSubtitleText("")
                .setTitleLogoUrl(userStatus.getProfileImage())
                .setStoryClickListeners(new StoryClickListeners() {
                    @Override
                    public void onDescriptionClickListener(int position) {
                        // Action on description click
                    }

                    @Override
                    public void onTitleIconClickListener(int position) {
                        // Action on title icon click
                    }
                })
                .build()
                .show();
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
