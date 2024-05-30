package com.example.chatsapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatsapp.Adapters.Top_Status_Adapter;
import com.example.chatsapp.Models.Status;
import com.example.chatsapp.Models.User;
import com.example.chatsapp.Models.User_Status;
import com.example.chatsapp.R;
import com.example.chatsapp.databinding.ActivityStatus2Binding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class status_activity extends AppCompatActivity {

    private static final String TAG = "status_activity";

    ActivityStatus2Binding binding;

    private FirebaseDatabase database;
    private ArrayList<User> users;
    private Top_Status_Adapter statusAdapter;
    private ArrayList<User_Status> userStatuses;
    private ProgressDialog dialog;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStatus2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        initializeComponents();
        fetchCurrentUser();
        setupAdapters();
        fetchStories();
    }

    private void initializeComponents() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image...");
        dialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();
        users = new ArrayList<>();
        userStatuses = new ArrayList<>();
    }

    private void fetchCurrentUser() {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getUid());
        database.getReference().child("users").child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentUser = snapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error fetching current user", error.toException());
                    }
                });
    }

    private void setupAdapters() {
        statusAdapter = new Top_Status_Adapter(this, userStatuses);
        binding.statusList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.statusList.setAdapter(statusAdapter);
        binding.statusList.showShimmerAdapter();
    }

    private void fetchStories() {
        database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userStatuses.clear();
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User_Status userStatus = new User_Status();
                        userStatus.setName(Objects.requireNonNull(userSnapshot.child("name").getValue()).toString());
                        userStatus.setProfileImage(Objects.requireNonNull(userSnapshot.child("profileImage").getValue()).toString());
                        userStatus.setLastUpdated(Long.parseLong(Objects.requireNonNull(userSnapshot.child("lastUpdated").getValue()).toString()));

                        ArrayList<Status> statuses = new ArrayList<>();
                        for (DataSnapshot statusSnapshot : userSnapshot.child("statuses").getChildren()) {
                            Status status = statusSnapshot.getValue(Status.class);
                            statuses.add(status);
                        }
                        userStatus.setStatuses(statuses);
                        userStatuses.add(userStatus);
                    }
                    binding.statusList.hideShimmerAdapter();
                    statusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching stories", error.toException());
            }
        });
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 75);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 75 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uploadImage(data.getData());
        }
    }

    private void uploadImage(Uri imageUri) {
        dialog.show();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference reference = storage.getReference().child("status").child(new Date().getTime() + "");

        reference.putFile(imageUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveStatusToDatabase(uri);
                    dialog.dismiss();
                });
            } else {
                dialog.dismiss();
                Log.e(TAG, "Failed to upload image", task.getException());
            }
        });
    }

    private void saveStatusToDatabase(Uri uri) {
        Date date = new Date();
        User_Status userStatus = new User_Status();
        userStatus.setName(currentUser.getName());
        userStatus.setProfileImage(currentUser.getProfileImage());
        userStatus.setLastUpdated(date.getTime());

        Map<String, Object> statusData = new HashMap<>();
        statusData.put("name", userStatus.getName());
        statusData.put("profileImage", userStatus.getProfileImage());
        statusData.put("lastUpdated", userStatus.getLastUpdated());

        Status status = new Status(uri.toString(), userStatus.getLastUpdated());

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            database.getReference().child("stories").child(uid).updateChildren(statusData);
            database.getReference().child("stories").child(uid).child("statuses").push().setValue(status);
        }
    }
}
