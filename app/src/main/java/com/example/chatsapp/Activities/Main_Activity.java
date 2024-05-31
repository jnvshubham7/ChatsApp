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
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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



        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && !actionBar.isShowing()) {
            // If the action bar is hidden, remove the top margin from RecyclerView
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.recyclerView.getLayoutParams();
            params.topMargin = 0;
            binding.recyclerView.setLayoutParams(params);
        }

        initializeComponents();
        fetchCurrentUser();
        setupAdapters();
        fetchUsers();
//        fetchStories();
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

        binding.recyclerView.setAdapter(usersAdapter);
        binding.recyclerView.showShimmerAdapter();

    }

    public void fetchUsers() {
        database.getReference().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<User> tempUsers = new ArrayList<>();
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null && !user.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                            fetchLastMsgTime(user, tempUsers, snapshot.getChildrenCount());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error fetching users", error.toException());
            }
        });
    }

    private void fetchLastMsgTime(User user, List<User> tempUserzs, long totalUsers) {
        String senderId = FirebaseAuth.getInstance().getUid();
        String senderRoom = senderId + user.getUid();

        database.getReference().child("chats").child(senderRoom)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Long lastMsgTime = snapshot.child("lastMsgTime").getValue(Long.class);
                            user.setLastMsgTime(lastMsgTime != null ? lastMsgTime : 0);
                        } else {
                            user.setLastMsgTime(0);
                        }

                        tempUsers.add(user);

                        if (tempUsers.size() == totalUsers - 1) {
                            users.clear();
                            users.addAll(tempUsers);
                            sortUsersByLastMsgTime();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error fetching last message time", error.toException());
                    }
                });
    }

    private void sortUsersByLastMsgTime() {
        Collections.sort(users, (u1, u2) -> Long.compare(u2.getLastMsgTime(), u1.getLastMsgTime()));

        binding.recyclerView.hideShimmerAdapter();
        usersAdapter.notifyDataSetChanged();
    }


    private void setupBottomNavigationView() {
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Intent intent;
            switch (item.getItemId()) {
                case R.id.chats:
                    intent = new Intent(this, Main_Activity.class);
                    startActivity(intent);
                    return true;

                case R.id.status:
                    intent = new Intent(this, status_activity.class);
                    startActivity(intent);
                    return true;

                default:
                    return true;
            }
        });
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
        fetchUsers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        updatePresenceStatus("Offline");
        fetchUsers();
    }

    private void updatePresenceStatus(String status) {
        String currentId = FirebaseAuth.getInstance().getUid();
        if (currentId != null) {
            database.getReference().child("presence").child(currentId).setValue(status);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION);
            }
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
