package com.example.chatsapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatsapp.Models.User;
import com.example.chatsapp.databinding.ActivitySetupProfileBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

@SuppressWarnings("deprecation")
public class Setup_Profile_Activity extends AppCompatActivity {

    private static final String TAG = "Setup_Profile_Activity";
    ActivitySetupProfileBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri selectedImage;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetupProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Updating profile...");
        dialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        Objects.requireNonNull(getSupportActionBar()).hide();

        binding.imageView.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 45);
        });

        binding.continueBtn.setOnClickListener(v -> {
            String name = binding.nameBox.getText().toString();

            if(name.isEmpty()) {
                binding.nameBox.setError("Please type a name");
                return;
            }

            dialog.show();

            FirebaseApp.initializeApp(this);
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        String fcmToken = task.getResult();
                        Log.d(TAG, "FCM Registration Token: " + fcmToken);

                        if(selectedImage != null) {
                            StorageReference reference = storage.getReference().child("Profiles").child(Objects.requireNonNull(auth.getUid()));
                            reference.putFile(selectedImage).addOnCompleteListener(task1 -> {
                                if(task1.isSuccessful()) {
                                    reference.getDownloadUrl().addOnSuccessListener(uri -> {
                                        String imageUrl = uri.toString();

                                        String uid = auth.getUid();
                                        String phone = Objects.requireNonNull(auth.getCurrentUser()).getPhoneNumber();
                                        String name1 = binding.nameBox.getText().toString();

                                        User user = new User(uid, name1, phone, imageUrl, fcmToken);

                                        database.getReference()
                                                .child("users")
                                                .child(uid)
                                                .setValue(user)
                                                .addOnSuccessListener(aVoid -> {
                                                    dialog.dismiss();
                                                    Intent intent = new Intent(Setup_Profile_Activity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                });
                                    });
                                }
                            });
                        } else {
                            String uid = auth.getUid();
                            String phone = Objects.requireNonNull(auth.getCurrentUser()).getPhoneNumber();

                            User user = new User(uid, name, phone, "No Image", fcmToken);

                            assert uid != null;
                            database.getReference()
                                    .child("users")
                                    .child(uid)
                                    .setValue(user)
                                    .addOnSuccessListener(aVoid -> {
                                        dialog.dismiss();
                                        Intent intent = new Intent(Setup_Profile_Activity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    });
                        }
                    });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null) {
            if(data.getData() != null) {
                Uri uri = data.getData(); // filepath
                FirebaseStorage storage = FirebaseStorage.getInstance();
                long time = new Date().getTime();
                StorageReference reference = storage.getReference().child("Profiles").child(time+"");
                reference.putFile(uri).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        reference.getDownloadUrl().addOnSuccessListener(uri1 -> {
                            String filePath = uri1.toString();
                            HashMap<String, Object> obj = new HashMap<>();
                            obj.put("image", filePath);
                            database.getReference().child("users")
                                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                    .updateChildren(obj).addOnSuccessListener(aVoid -> {

                                    });
                        });
                    }
                });

                binding.imageView.setImageURI(data.getData());
                selectedImage = data.getData();
            }
        }
    }
}
