package com.example.chatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class FullImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        ImageView imageView = findViewById(R.id.fullImageView);

        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("imageUrl");

        if (imageUrl != null) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(imageView);
        }
    }
}
