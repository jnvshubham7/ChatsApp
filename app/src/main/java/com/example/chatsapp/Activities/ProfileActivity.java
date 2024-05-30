package com.example.chatsapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.chatsapp.FullImageActivity;
import com.example.chatsapp.R;
import com.example.chatsapp.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String name = getIntent().getStringExtra("name");
        String image = getIntent().getStringExtra("image");

        binding.name.setText(name);
        Glide.with(this).load(image).placeholder(R.drawable.avatar).into(binding.profileImage);

        binding.profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, FullImageActivity.class);
            intent.putExtra("imageUrl", image);
            startActivity(intent);
        });
    }
}
