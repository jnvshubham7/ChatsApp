package com.example.chatsapp.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatsapp.Adapters.Top_Status_Adapter;
import com.example.chatsapp.Models.Status;
import com.example.chatsapp.Models.User;
import com.example.chatsapp.Models.User_Status;
import com.example.chatsapp.R;
import com.example.chatsapp.databinding.ActivityStatus2Binding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class status_activity extends AppCompatActivity {

    private static final String TAG = "status_activity";
    private static final int REQUEST_CODE_GALLERY = 75;
//    private static final int REQUEST_CODE_CAMERA = 76;

    private static final int REQUEST_CODE_CAMERA = 1;


    ActivityStatus2Binding binding;

    private FirebaseDatabase database;
    private ArrayList<User> users;
    private Top_Status_Adapter statusAdapter;
    private ArrayList<User_Status> userStatuses;
    private ProgressDialog dialog;
    private User currentUser;
    private Uri imageUri;

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
        setupAddStatusButton(); // Initialize the FloatingActionButton
        setupAddStatusButtonCam();
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
        binding.statusList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
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
                    sortUserStatusesByLastUpdatedTime();
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

    private void sortUserStatusesByLastUpdatedTime() {
        Collections.sort(userStatuses, new Comparator<User_Status>() {
            @Override
            public int compare(User_Status o1, User_Status o2) {
                Status lastStatus1 = o1.getStatuses().get(o1.getStatuses().size() - 1);
                Status lastStatus2 = o2.getStatuses().get(o2.getStatuses().size() - 1);
                return Long.compare(lastStatus2.getTimeStamp(), lastStatus1.getTimeStamp());
            }
        });
    }

    private void setupAddStatusButton() {
        FloatingActionButton addStatusButton = findViewById(R.id.addStatus);
        addStatusButton.setOnClickListener(v -> selectImageFromGallery());
    }

    private void selectImageFromGallery() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_GALLERY);
//        } else {
            openGallery();
//        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }

    private void setupAddStatusButtonCam() {
        FloatingActionButton addStatusButtonCam = findViewById(R.id.addStatus_camera);
        addStatusButtonCam.setOnClickListener(v -> openCamera());
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_CAMERA);
            launchCamera();
        } else {
            launchCamera();
        }
    }


    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(this, "com.example.chatsapp.fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, REQUEST_CODE_CAMERA);
            }
        } else {
            Log.e(TAG, "No camera app available");
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                launchCamera();
            } else {
                Log.e(TAG, "Camera or storage permission not granted");
            }
        }
    }



    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            Log.e(TAG, "Error creating image file", e);
        }
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uploadImage(data.getData());
        } else if (requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK) {
            uploadImage(imageUri);
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
