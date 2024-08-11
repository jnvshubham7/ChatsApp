package com.example.chatsapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
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

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Profile");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

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

    @Override
    public boolean onSupportNavigateUp() {
        finish(); // Close this activity and return to the previous one
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish(); // Close this activity and return to the previous one
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
