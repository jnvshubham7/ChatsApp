package com.example.chatsapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatsapp.Activities.Chat_Activity;
import com.example.chatsapp.Models.Message;
import com.example.chatsapp.Models.User;
import com.example.chatsapp.R;
import com.example.chatsapp.databinding.RowConversationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Users_Adapter extends RecyclerView.Adapter<Users_Adapter.UsersViewHolder> {

    Context context;
    ArrayList<User> users;

    User user;

    public Users_Adapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);
        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
        User user = users.get(position);

        String senderId = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderId + user.getUid();

        FirebaseDatabase.getInstance().getReference()
                .child("chats")
                .child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String lastMsg = snapshot.child("lastMsg").getValue(String.class);
                            Long time_date = snapshot.child("lastMsgTime").getValue(Long.class);

                            if (time_date == null) {
                                holder.binding.msgTime.setText("");
                            } else {
                                // Get current time
                                long currentTime = System.currentTimeMillis();

                                // Calculate time difference in milliseconds
                                long timeDifference = currentTime - time_date;

                                // Format for time
                                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
                                // Format for date
                                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                                if (timeDifference < 24 * 60 * 60 * 1000) { // within 24 hours
                                    String formattedTime = timeFormat.format(new Date(time_date));
                                    holder.binding.msgTime.setText(formattedTime);
                                } else {
                                    // Get calendar instance
                                    Calendar messageCalendar = Calendar.getInstance();
                                    messageCalendar.setTimeInMillis(time_date);
                                    int messageDayOfYear = messageCalendar.get(Calendar.DAY_OF_YEAR);

                                    Calendar currentCalendar = Calendar.getInstance();
                                    currentCalendar.setTimeInMillis(currentTime);
                                    int currentDayOfYear = currentCalendar.get(Calendar.DAY_OF_YEAR);

                                    if (messageDayOfYear == currentDayOfYear - 1) {
                                        holder.binding.msgTime.setText("Yesterday");
                                    } else {
                                        String formattedDate = dateFormat.format(new Date(time_date));
                                        holder.binding.msgTime.setText(formattedDate);
                                    }
                                }
                            }

                            user.setLastMsgTime(time_date != null ? time_date : 0); // Set 0 if time_date is null

                            Log.d("time_date_error", user.getTimeAndDate() + "");

                            holder.binding.lastMsg.setText(lastMsg);

                            // Count unread messages
                            long unreadCount = 0;
                            for (DataSnapshot messageSnapshot : snapshot.child("messages").getChildren()) {
                                Message message = messageSnapshot.getValue(Message.class);
                                if (message != null && !message.isRead() && !message.getSenderId().equals(senderId)) {
                                    unreadCount++;
                                }
                            }

                            if (unreadCount > 0) {
                                SpannableString spannableString = new SpannableString(lastMsg);
                                assert lastMsg != null;
                                spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, lastMsg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), 0, lastMsg.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                holder.binding.lastMsg.setText(spannableString);

                                holder.binding.msgTime.setTextColor(Color.GREEN);

                                holder.binding.unreadCount.setVisibility(View.VISIBLE);
                                holder.binding.unreadCount.setText(String.valueOf(unreadCount));
                            } else {
                                holder.binding.lastMsg.setText(lastMsg);
                                holder.binding.lastMsg.setTypeface(Typeface.DEFAULT);

                                holder.binding.msgTime.setTextColor(holder.binding.msgTime.getTextColors());


                                holder.binding.lastMsg.setTextColor(holder.binding.lastMsg.getTextColors());


                                holder.binding.unreadCount.setVisibility(View.GONE);
                            }
                        }
                        else {
                            holder.binding.lastMsg.setText("Tap to chat");
                            holder.binding.msgTime.setText("");
                            holder.binding.unreadCount.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.binding.username.setText(user.getName());

        Glide.with(context).load(user.getProfileImage())
                .placeholder(R.drawable.avatar)
                .into(holder.binding.profile);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, Chat_Activity.class);
            intent.putExtra("name", user.getName());
            intent.putExtra("image", user.getProfileImage());
            intent.putExtra("uid", user.getUid());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        RowConversationBinding binding;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConversationBinding.bind(itemView);
        }
    }
}