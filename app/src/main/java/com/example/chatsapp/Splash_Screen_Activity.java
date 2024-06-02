package com.example.chatsapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.chatsapp.Activities.PhoneNumberActivity;
import com.jaeger.library.StatusBarUtil;

public class Splash_Screen_Activity extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 3000;  // Display duration

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash_screen);

        StatusBarUtil.setTransparent(this);

        ImageView logo = findViewById(R.id.textViewC);
        TextView appName = findViewById(R.id.textView);

        // Wait for the splash display duration, then start the next activity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(Splash_Screen_Activity.this, PhoneNumberActivity.class);
            startActivity(intent);
            finish();
        }, SPLASH_DISPLAY_LENGTH);
    }
}
