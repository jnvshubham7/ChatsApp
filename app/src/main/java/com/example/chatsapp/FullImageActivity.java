package com.example.chatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.Objects;

public class FullImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_image);

        Objects.requireNonNull(getSupportActionBar()).hide();


        PhotoView photoView = findViewById(R.id.fullImageView);
        ImageView backButton = findViewById(R.id.backButton);

        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("imageUrl");

        if (imageUrl != null) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(photoView);
        }

        photoView.setOnClickListener(v -> onBackPressed());

        backButton.setOnClickListener(v -> onBackPressed());


    }
}
