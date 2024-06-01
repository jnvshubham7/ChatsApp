package com.example.chatsapp.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatsapp.Activities.PhoneNumberActivity;
import com.example.chatsapp.Activities.ProfileEditing;
import com.example.chatsapp.Adapters.TopStatusAdapter;
import com.example.chatsapp.Adapters.UsersAdapter;
import com.example.chatsapp.Models.User;
import com.example.chatsapp.Models.UserStatus;
import com.example.chatsapp.R;
import com.example.chatsapp.databinding.FragmentMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ChatsFragment extends Fragment {

    private static final String TAG = "MainFragment";
    private static final int REQUEST_CODE_NOTIFICATION = 100;

    private FragmentMainBinding binding;
    private FirebaseDatabase database;
    private ArrayList<User> users;
    private UsersAdapter usersAdapter;
    private TopStatusAdapter statusAdapter;
    private ArrayList<UserStatus> userStatuses;
    private ProgressDialog dialog;
    private User currentUser;

    private RecyclerView recyclerView;
    private UsersAdapter chatAdapter;
    private List<User> userList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        setHasOptionsMenu(true); // To handle menu in fragment

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        chatAdapter = new UsersAdapter(getContext(), (ArrayList<User>) userList);
        recyclerView.setAdapter(chatAdapter);

        // Initialize database here
        database = FirebaseDatabase.getInstance();
        fetchUsers();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeComponents();
        fetchCurrentUser();
        setupAdapters();
        fetchUsers();
        retrieveFCMToken();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        }
    }

    private void initializeComponents() {
        dialog = new ProgressDialog(getContext());
        dialog.setMessage("Uploading Image...");
        dialog.setCancelable(false);

        // Initialize database here if not already initialized
        if (database == null) {
            database = FirebaseDatabase.getInstance();
        }

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
        usersAdapter = new UsersAdapter(getContext(), users);
        statusAdapter = new TopStatusAdapter(getContext(), userStatuses);

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

    private void fetchLastMsgTime(User user, List<User> tempUsers, long totalUsers) {
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
    public void onResume() {
        super.onResume();
        updatePresenceStatus("Online");

        // Access the ActionBar from the hosting activity
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("ChatsApp");
            }
        }
    }

    @Override
    public void onPause() {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_NOTIFICATION);
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.topmenu, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchUsers(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchUsers(newText);
                return false;
            }
        });
    }

    private void searchUsers(String query) {
        List<User> filteredUsers = new ArrayList<>();
        for (User user : users) {
            if (user.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredUsers.add(user);
            }
        }
        usersAdapter.updateUsers(filteredUsers);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == R.id.search) {
//            return true;
//        } else
//
     if (item.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getContext(), PhoneNumberActivity.class));
            return true;
        } else if (item.getItemId() == R.id.profile) {
            startActivity(new Intent(getContext(), ProfileEditing.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
