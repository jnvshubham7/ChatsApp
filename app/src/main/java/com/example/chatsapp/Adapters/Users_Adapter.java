package com.example.chatsapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class Users_Adapter extends RecyclerView.Adapter<Users_Adapter.UsersViewHolder> {

    Context context;
    ArrayList<User> users;

    public Users_Adapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
        fetchAndSortUsers();
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
                            long time_date = snapshot.child("lastMsgTime").getValue(Long.class);

                            @SuppressLint("SimpleDateFormat")
                            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
                            String formattedTime = timeFormat.format(new Date(time_date));

                            @SuppressLint("SimpleDateFormat")
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                            String formattedDate = dateFormat.format(new Date(time_date));

                            String timeAndDate = formattedTime + " - " + formattedDate;

                            holder.binding.msgTime.setText(timeAndDate);
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
                                holder.binding.unreadCount.setVisibility(View.VISIBLE);
                                holder.binding.unreadCount.setText(String.valueOf(unreadCount));
                            } else {
                                holder.binding.unreadCount.setVisibility(View.GONE);
                            }

                            // Update user object
                            user.setLastMsgTime(time_date);
                            user.setUnreadCount(unreadCount);

                            // Sort users and notify adapter
                            sortUsers();
                            notifyDataSetChanged();

                        } else {
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

    private void fetchAndSortUsers() {
        // Here you can fetch the user list from the database and call sortUsers()
        // For example:
        FirebaseDatabase.getInstance().getReference().child("users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        users.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            users.add(user);
                        }
                        sortUsers();
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
    }

    private void sortUsers() {
        Collections.sort(users, new Comparator<User>() {
            @Override
            public int compare(User u1, User u2) {
                // First, compare by unread count
                int unreadCompare = Long.compare(u2.getUnreadCount(), u1.getUnreadCount());
                if (unreadCompare != 0) {
                    return unreadCompare;
                }
                // If unread counts are equal, compare by last message time
                return Long.compare(u2.getLastMsgTime(), u1.getLastMsgTime());
            }
        });
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        RowConversationBinding binding;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConversationBinding.bind(itemView);
        }
    }
}
