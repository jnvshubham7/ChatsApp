package com.example.chatsapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.chatsapp.Adapters.Messages_Adapter;
import com.example.chatsapp.Models.Message;
import com.example.chatsapp.R;
import com.example.chatsapp.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class Chat_Activity extends AppCompatActivity {

    private static final String TAG = "Chat_Activity";
    private ActivityChatBinding binding;
    private Messages_Adapter adapter;
    private ArrayList<Message> messages;
    private String senderRoom, receiverRoom;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private ProgressDialog dialog;
    private String senderUid;
    private String receiverUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initViews();
        initFirebase();
        initChat();
        loadMessages();
        setupSendButton();
        setupCameraButton();
        setupTypingIndicator();
    }

    private void initViews() {
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        binding.name.setText(getIntent().getStringExtra("name"));
        Glide.with(this).load(getIntent().getStringExtra("image"))
                .placeholder(R.drawable.avatar)
                .into(binding.profile);

        binding.imageView2.setOnClickListener(v -> finish());
    }

    private void initFirebase() {
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading image...");
        dialog.setCancelable(false);

        receiverUid = getIntent().getStringExtra("uid");
        senderUid = Objects.requireNonNull(FirebaseAuth.getInstance().getUid());

        database.getReference().child("presence").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.getValue(String.class);
                if (status != null && !status.isEmpty()) {
                    binding.status.setVisibility(status.equals("Offline") ? View.GONE : View.VISIBLE);
                    binding.status.setText(status);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

//        // Initialize Firebase
//        FirebaseApp.initializeApp(this);
//
//        // Get the FCM registration token
//        FirebaseMessaging.getInstance().getToken()
//                .addOnCompleteListener(new OnCompleteListener<String>() {
//                    @Override
//                    public void onComplete(@NonNull Task<String> task) {
//                        if (!task.isSuccessful()) {
//                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
//                            return;
//                        }
//
//                        // Get new FCM registration token
//                        String token = task.getResult();
//                        Log.d(TAG, "FCM Registration Token: " + token);
//
//                        // Save or send the token as needed
//                        sendTokenToServer(token);
//                    }
//                });
    }

//    private void sendTokenToServer(String token) {
//
//
//        database.getReference().child("users").child(senderUid).child("fcmToken").setValue(token);
//    }

    private void initChat() {
        messages = new ArrayList<>();
        adapter = new Messages_Adapter(this, messages, senderRoom, receiverRoom);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private void loadMessages() {
        database.getReference().child("chats").child(senderRoom).child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            Message message = snapshot1.getValue(Message.class);
                            if (message != null) {
                                message.setMessageId(snapshot1.getKey());
                                messages.add(message);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle database error
                    }
                });
    }

    private void setupSendButton() {
        binding.sendBtn.setOnClickListener(v -> {
            String messageTxt = binding.messageBox.getText().toString();
            if (!messageTxt.trim().isEmpty()) {
                sendMessage(messageTxt);
            }
        });
    }

    private void sendMessage(String messageTxt) {
        Date date = new Date();
        Message message = new Message(messageTxt, senderUid, date.getTime());
        binding.messageBox.setText("");

        String randomKey = database.getReference().push().getKey();

        HashMap<String, Object> lastMsgObj = new HashMap<>();
        lastMsgObj.put("lastMsg", message.getMessage());
        lastMsgObj.put("lastMsgTime", date.getTime());

        database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
        database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

        if (randomKey != null) {
            database.getReference().child("chats").child(senderRoom).child("messages").child(randomKey).setValue(message)
                    .addOnSuccessListener(aVoid -> database.getReference().child("chats").child(receiverRoom).child("messages").child(randomKey).setValue(message));
        }
    }

    private void setupCameraButton() {
        binding.camera.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 25);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 25 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uploadImage(data.getData());
        }
    }

    private void uploadImage(Uri selectedImage) {
        Calendar calendar = Calendar.getInstance();
        StorageReference reference = storage.getReference().child("chats").child(String.valueOf(calendar.getTimeInMillis()));
        dialog.show();
        reference.putFile(selectedImage).addOnCompleteListener(task -> {
            dialog.dismiss();
            if (task.isSuccessful()) {
                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String filePath = uri.toString();
                    sendMessageWithImage(filePath);
                });
            }
        });
    }

    private void sendMessageWithImage(String filePath) {
        Date date = new Date();
        Message message = new Message("photo", senderUid, date.getTime());
        message.setImageUrl(filePath);
        binding.messageBox.setText("");

        String randomKey = database.getReference().push().getKey();

        HashMap<String, Object> lastMsgObj = new HashMap<>();
        lastMsgObj.put("lastMsg", message.getMessage());
        lastMsgObj.put("lastMsgTime", date.getTime());

        database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
        database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);

        if (randomKey != null) {
            database.getReference().child("chats").child(senderRoom).child("messages").child(randomKey).setValue(message)
                    .addOnSuccessListener(aVoid -> database.getReference().child("chats").child(receiverRoom).child("messages").child(randomKey).setValue(message));
        }
    }

    private void setupTypingIndicator() {
        final Handler handler = new Handler();
        binding.messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                database.getReference().child("presence").child(senderUid).setValue("typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(userStoppedTyping, 1000);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }

            final Runnable userStoppedTyping = () -> database.getReference().child("presence").child(senderUid).setValue("Online");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        database.getReference().child("presence").child(senderUid).setValue("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        database.getReference().child("presence").child(senderUid).setValue("Offline");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
