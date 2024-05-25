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
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.chatsapp.Adapters.Messages_Adapter;
import com.example.chatsapp.Models.Message;
import com.example.chatsapp.R;
import com.example.chatsapp.databinding.ActivityChatBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Chat_Activity extends AppCompatActivity {


    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.messageBox.getWindowToken(), 0);
    }

    private static final String TAG = "Chat_Activity_Error";
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

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseMessaging.getInstance().setDeliveryMetricsExportToBigQuery(true);

        binding.messageBox.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.messageBox.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                binding.messageBox.requestFocus();
                hideKeyboard();
            }
        });



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
    }

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

                        // Ensure smooth scroll only if there are messages
                        if (!messages.isEmpty()) {
                            binding.recyclerView.smoothScrollToPosition(messages.size() - 1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle database error
                    }
                });
    }

    private void setupSendButton() {
        binding.sendBtn.setOnClickListener(v -> {
            binding.messageBox.requestFocus();
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
                    .addOnSuccessListener(aVoid -> {
                        database.getReference().child("chats").child(receiverRoom).child("messages").child(randomKey).setValue(message)
                                .addOnSuccessListener(aVoid1 -> {
                                    sendNotification(message.getMessage());
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(Chat_Activity.this, "Failed to send message to receiver: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Failed to send message to receiver: ", e);
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Chat_Activity.this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to send message: ", e);
                    });
        } else {
            Toast.makeText(Chat_Activity.this, "Failed to generate message key", Toast.LENGTH_SHORT).show();
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
                }).addOnFailureListener(e -> {
                    Toast.makeText(Chat_Activity.this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to get download URL: ", e);
                });
            } else {
                Toast.makeText(Chat_Activity.this, "Image upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Image upload failed: ", task.getException());
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
                    .addOnSuccessListener(aVoid -> {
                        database.getReference().child("chats").child(receiverRoom).child("messages").child(randomKey).setValue(message)
                                .addOnSuccessListener(aVoid1 -> {
                                    sendNotification("photo");
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(Chat_Activity.this, "Failed to send image to receiver: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "Failed to send image to receiver: ", e);
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Chat_Activity.this, "Failed to send image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to send image: ", e);
                    });
        } else {
            Toast.makeText(Chat_Activity.this, "Failed to generate message key", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupTypingIndicator() {
        final Handler handler = new Handler();
        binding.messageBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No implementation needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                database.getReference().child("presence").child(senderUid).setValue("Typing...");
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> database.getReference().child("presence").child(senderUid).setValue("Online"), 1000);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No implementation needed
            }
        });
    }

    private void sendNotification(String message) {
        RequestQueue queue = Volley.newRequestQueue(this);

        String url = "https://fcm.googleapis.com/fcm/send";
        JSONObject data = new JSONObject();

        try {
            data.put("title", "New Message");
            data.put("body", message);
            data.put("sound", "default");

            JSONObject notificationData = new JSONObject();
            notificationData.put("to", "/topics/" + receiverUid);
            notificationData.put("data", data);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, notificationData,
                    response -> {
                        // Handle response
                    }, error -> {
                // Handle error
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "key=YOUR_SERVER_KEY");
                    return headers;
                }
            };

            queue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "sendNotification: ", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        database.getReference().child("presence").child(senderUid).setValue("Online");
        if (!messages.isEmpty()) {
            binding.recyclerView.scrollToPosition(messages.size() - 1);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        database.getReference().child("presence").child(senderUid).setValue("Offline");
    }
}
