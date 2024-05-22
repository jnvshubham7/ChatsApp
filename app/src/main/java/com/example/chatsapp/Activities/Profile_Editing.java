package com.example.chatsapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.chatsapp.Models.User;
import com.example.chatsapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile_Editing extends AppCompatActivity {

    private CircleImageView profileImageView;
    private EditText nameEditText;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private ProgressDialog progressDialog;
    private Uri selectedImageUri;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_editing);

        profileImageView = findViewById(R.id.profileImageView);
        nameEditText = findViewById(R.id.nameEditText);
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating profile...");
        progressDialog.setCancelable(false);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fetchCurrentUser();
    }

    private void fetchCurrentUser() {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getUid());
        database.getReference().child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
                if (currentUser != null) {
                    nameEditText.setText(currentUser.getName());
                    // Load profile image using a library like Glide or Picasso
                     Glide.with(Profile_Editing.this).load(currentUser.getProfileImage()).into(profileImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Profile_Editing.this, "Failed to fetch profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void selectProfileImage(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            profileImageView.setImageURI(selectedImageUri);
        }
    }

    public void saveProfile(View view) {
        final String name = nameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            nameEditText.setError("Name is required");
            nameEditText.requestFocus();
            return;
        }

        progressDialog.show();
        if (selectedImageUri != null) {
            uploadProfileImage(name);
        } else {
            updateProfile(name, null);
        }
    }

    private void uploadProfileImage(final String name) {
        StorageReference reference = storage.getReference().child("profile_images").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        reference.putFile(selectedImageUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                reference.getDownloadUrl().addOnSuccessListener(uri -> {
                    updateProfile(name, uri.toString());
                });
            } else {
                progressDialog.dismiss();
                Toast.makeText(Profile_Editing.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile(String name, @Nullable String imageUrl) {
        String uid = Objects.requireNonNull(FirebaseAuth.getInstance().getUid());
        database.getReference().child("users").child(uid).child("name").setValue(name);
        if (imageUrl != null) {
            database.getReference().child("users").child(uid).child("profileImage").setValue(imageUrl);
        }
        progressDialog.dismiss();
        Toast.makeText(Profile_Editing.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
    }
}
