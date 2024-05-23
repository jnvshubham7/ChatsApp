package com.example.chatsapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatsapp.Adapters.Top_Status_Adapter;
import com.example.chatsapp.Adapters.Users_Adapter;
import com.example.chatsapp.Models.Status;
import com.example.chatsapp.Models.User;
import com.example.chatsapp.Models.User_Status;
import com.example.chatsapp.R;
import com.example.chatsapp.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Main_Activity extends AppCompatActivity {

    private static final String TAG = "Main_Activity";
    private static final int REQUEST_CODE_NOTIFICATION = 100;

    private ActivityMainBinding binding;
    private FirebaseDatabase database;
    private ArrayList<User> users;
    private Users_Adapter usersAdapter;
    private Top_Status_Adapter statusAdapter;
    private ArrayList<User_Status> userStatuses;
    private ProgressDialog dialog;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        initializeComponents();
        fetchCurrentUser();
        setupAdapters();
        fetchUsers();
        fetchStories();
        setupBottomNavigationView();
        retrieveFCMToken();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        }
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
        usersAdapter = new Users_Adapter(this, users);
        statusAdapter = new Top_Status_Adapter(this, userStatuses);

        binding.statusList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.statusList.setAdapter(statusAdapter);

        binding.recyclerView.setAdapter(usersAdapter);
        binding.recyclerView.showShimmerAdapter();
        binding.statusList.showShimmerAdapter();
    }

    private void fetchUsers() {
        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    users.clear();
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null && !user.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                            users.add(user);
                        }
                    }
                    binding.recyclerView.hideShimmerAdapter();
                    usersAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching users", error.toException());
            }
        });
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

    private void setupBottomNavigationView() {
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.status) {
                selectImageFromGallery();
                return true;
            }
            return false;
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

    private void retrieveFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                return;
            }

            String token = task.getResult();
            Log.d(TAG, "Token: " + token);
            saveTokenToDatabase(token);
        });
    }

    private void saveTokenToDatabase(String token) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("token").setValue(token);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePresenceStatus("Online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        updatePresenceStatus("Offline");
    }

    private void updatePresenceStatus(String status) {
        String currentId = FirebaseAuth.getInstance().getUid();
        if (currentId != null) {
            database.getReference().child("presence").child(currentId).setValue(status);
        }
    }

    private void requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_NOTIFICATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted");
            } else {
                Log.d(TAG, "Notification permission denied");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.profile) {
            startActivity(new Intent(Main_Activity.this, Profile_Editing.class));
        } else if (item.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(Main_Activity.this, Phone_Number_Activity.class));
            finishAffinity();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
