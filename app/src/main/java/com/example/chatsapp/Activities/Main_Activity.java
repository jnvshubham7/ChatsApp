package com.example.chatsapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chatsapp.Adapters.Top_Status_Adapter;
import com.example.chatsapp.Adapters.Users_Adapter;
import com.example.chatsapp.Models.Status;
import com.example.chatsapp.Models.User;
import com.example.chatsapp.Models.User_Status;
import com.example.chatsapp.R;
import com.example.chatsapp.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class Main_Activity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseDatabase database;
    private ArrayList<User> users;
    private Users_Adapter usersAdapter;
    private Top_Status_Adapter statusAdapter;
    private ArrayList<User_Status> userStatuses;
    private ProgressDialog dialog;
    private User user;

    private static final String ONESIGNAL_APP_ID = "501adfca-a519-4e24-a760-d2a878ac4b02";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeComponents();
        fetchCurrentUser();
        setupAdapters();
        fetchUsers();
        fetchStories();

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.status) {
                selectImageFromGallery();
            }
            return false;
        });
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
        database.getReference().child("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
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
                users.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    User user = snapshot1.getValue(User.class);
                    if (user != null && !user.getUid().equals(FirebaseAuth.getInstance().getUid())) {
                        users.add(user);
                    }
                }
                binding.recyclerView.hideShimmerAdapter();
                usersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void fetchStories() {
        database.getReference().child("stories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userStatuses.clear();
                    for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                        User_Status status = new User_Status();
                        status.setName(storySnapshot.child("name").getValue(String.class));
                        status.setProfileImage(storySnapshot.child("profileImage").getValue(String.class));
                        status.setLastUpdated(storySnapshot.child("lastUpdated").getValue(Long.class));

                        ArrayList<Status> statuses = new ArrayList<>();
                        for (DataSnapshot statusSnapshot : storySnapshot.child("statuses").getChildren()) {
                            Status sampleStatus = statusSnapshot.getValue(Status.class);
                            statuses.add(sampleStatus);
                        }

                        status.setStatuses(statuses);
                        userStatuses.add(status);
                    }
                    binding.statusList.hideShimmerAdapter();
                    statusAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 75);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && data.getData() != null) {
            uploadImage(data.getData());
        }
    }

    private void uploadImage(Uri imageUri) {
        dialog.show();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        Date date = new Date();
        StorageReference reference = storage.getReference().child("status").child(date.getTime() + "");

        reference.putFile(imageUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                    User_Status userStatus = new User_Status();
                    userStatus.setName(user.getName());
                    userStatus.setProfileImage(user.getProfileImage());
                    userStatus.setLastUpdated(date.getTime());

                    Map<String, Object> obj = new HashMap<>();
                    obj.put("name", userStatus.getName());
                    obj.put("profileImage", userStatus.getProfileImage());
                    obj.put("lastUpdated", userStatus.getLastUpdated());

                    Status status = new Status(uri.toString(), userStatus.getLastUpdated());

                    database.getReference()
                            .child("stories")
                            .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                            .updateChildren(obj);

                    database.getReference().child("stories")
                            .child(FirebaseAuth.getInstance().getUid())
                            .child("statuses")
                            .push()
                            .setValue(status);

                    dialog.dismiss();
                });
            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.group:
                startActivity(new Intent(Main_Activity.this, Group_Chat_Activity.class));
                return true;
                case R.id.profile:
                startActivity(new Intent(Main_Activity.this, Profile_Editing.class));
                return true;

            case R.id.search:
                Toast.makeText(this, "Search clicked.", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.settings:
                Toast.makeText(this, "Settings clicked.", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
