package com.example.chatsapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatsapp.FullImageActivity;
import com.example.chatsapp.Models.Message;
import com.example.chatsapp.R;
import com.example.chatsapp.databinding.DeleteDialogBinding;
import com.example.chatsapp.databinding.ItemReceiveBinding;
import com.example.chatsapp.databinding.ItemSentBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    ArrayList<Message> messages;

    final int ITEM_SENT = 1;
    final int ITEM_RECEIVE = 2;

    String senderRoom;
    String receiverRoom;

    public MessagesAdapter(Context context, ArrayList<Message> messages, String senderRoom, String receiverRoom) {
        this.context = context;
        this.messages = messages;
        this.senderRoom = senderRoom;
        this.receiverRoom = receiverRoom;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (Objects.requireNonNull(FirebaseAuth.getInstance().getUid()).equals(message.getSenderId())) {
            return ITEM_SENT;
        } else {
            return ITEM_RECEIVE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());

        if (holder instanceof SentViewHolder) {
            SentViewHolder viewHolder = (SentViewHolder) holder;

            if (message.getMessage().equals("photo")) {
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.binding.image);

                viewHolder.binding.image.setOnClickListener(v -> {
                    Intent intent = new Intent(context, FullImageActivity.class);
                    intent.putExtra("imageUrl", message.getImageUrl());
                    context.startActivity(intent);
                });
            } else {
                viewHolder.binding.message.setText(message.getMessage());
                viewHolder.binding.image.setVisibility(View.GONE);
                viewHolder.binding.message.setVisibility(View.VISIBLE);
            }
            viewHolder.binding.timestamp.setText(sdf.format(new Date(message.getTimestamp())));

            // Set status icon
            if (message.isRead()) {
                viewHolder.binding.statusIcon.setImageResource(R.drawable.ic_double_check_blue);
            } else {
                viewHolder.binding.statusIcon.setImageResource(R.drawable.ic_double_check);
            }

            viewHolder.itemView.setOnLongClickListener(v -> {
                showDeleteDialog(message);
                return false;
            });

        } else if (holder instanceof ReceiverViewHolder) {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;

            if (message.getMessage().equals("photo")) {
                viewHolder.binding.image.setVisibility(View.VISIBLE);
                viewHolder.binding.message.setVisibility(View.GONE);
                Glide.with(context)
                        .load(message.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(viewHolder.binding.image);

                viewHolder.binding.image.setOnClickListener(v -> {
                    Intent intent = new Intent(context, FullImageActivity.class);
                    intent.putExtra("imageUrl", message.getImageUrl());
                    context.startActivity(intent);
                });
            } else {
                viewHolder.binding.message.setText(message.getMessage());
                viewHolder.binding.image.setVisibility(View.GONE);
                viewHolder.binding.message.setVisibility(View.VISIBLE);
            }
            viewHolder.binding.timestamp.setText(sdf.format(new Date(message.getTimestamp())));

            viewHolder.itemView.setOnLongClickListener(v -> {
                showDeleteDialog(message);
                return false;
            });
        }
    }

    private void showDeleteDialog(Message message) {
        View view = LayoutInflater.from(context).inflate(R.layout.delete_dialog, null);
        DeleteDialogBinding binding = DeleteDialogBinding.bind(view);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Delete Message")
                .setView(binding.getRoot())
                .create();

        binding.everyone.setOnClickListener(v1 -> {
            message.setMessage("This message is removed.");
            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);

            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(receiverRoom)
                    .child("messages")
                    .child(message.getMessageId()).setValue(message);
            dialog.dismiss();
        });

        binding.delete.setOnClickListener(v12 -> {
            FirebaseDatabase.getInstance().getReference()
                    .child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .child(message.getMessageId()).setValue(null);
            dialog.dismiss();
        });

        binding.cancel.setOnClickListener(v13 -> dialog.dismiss());

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class SentViewHolder extends RecyclerView.ViewHolder {

        ItemSentBinding binding;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSentBinding.bind(itemView);
        }
    }

    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {

        ItemReceiveBinding binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveBinding.bind(itemView);
        }
    }
}
