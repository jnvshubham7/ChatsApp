package com.example.chatsapp.Fragments;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatsapp.Adapters.TopStatusAdapter;
import com.example.chatsapp.Models.Status;
import com.example.chatsapp.Models.User;
import com.example.chatsapp.Models.UserStatus;
import com.example.chatsapp.databinding.FragmentStatusBinding;
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

public class StatusFragment extends Fragment {

    private static final String TAG = "StatusFragment";
    private static final int REQUEST_CODE_GALLERY = 75;
    private static final int REQUEST_CODE_CAMERA = 1;

    private FragmentStatusBinding binding;
    private FirebaseDatabase database;
    private ArrayList<User> users;
    private TopStatusAdapter statusAdapter;
    private ArrayList<UserStatus> userStatuses;
    private ProgressDialog dialog;
    private User currentUser;
    private Uri imageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatusBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeComponents();
        fetchCurrentUser();
        setupAdapters();
        fetchStories();
        setupAddStatusButton();
        setupAddStatusButtonCam();
    }

    private void initializeComponents() {
        dialog = new ProgressDialog(getContext());
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
        statusAdapter = new TopStatusAdapter(getContext(), userStatuses);
        binding.statusList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
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
                        UserStatus userStatus = new UserStatus();
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
        Collections.sort(userStatuses, new Comparator<UserStatus>() {
            @Override
            public int compare(UserStatus o1, UserStatus o2) {
                Status lastStatus1 = o1.getStatuses().get(o1.getStatuses().size() - 1);
                Status lastStatus2 = o2.getStatuses().get(o2.getStatuses().size() - 1);
                return Long.compare(lastStatus2.getTimeStamp(), lastStatus1.getTimeStamp());
            }
        });
    }

    private void setupAddStatusButton() {
        FloatingActionButton addStatusButton = binding.addStatus;
        addStatusButton.setOnClickListener(v -> selectImageFromGallery());
    }

    private void selectImageFromGallery() {
        openGallery();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_GALLERY);
    }

    private void setupAddStatusButtonCam() {
        FloatingActionButton addStatusButtonCam = binding.addStatusCamera;
        addStatusButtonCam.setOnClickListener(v -> openCamera());
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_CAMERA);
            launchCamera();
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                imageUri = FileProvider.getUriForFile(getContext(), "com.example.chatsapp.fileprovider", photoFile);
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
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            Log.e(TAG, "Error creating image file", e);
        }
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GALLERY && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            uploadImage(data.getData());
        } else if (requestCode == REQUEST_CODE_CAMERA && resultCode == getActivity().RESULT_OK) {
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
        UserStatus userStatus = new UserStatus();
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
