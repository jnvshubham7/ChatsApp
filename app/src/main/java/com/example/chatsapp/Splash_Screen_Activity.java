package com.example.chatsapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.chatsapp.Activities.Phone_Number_Activity;
import com.jaeger.library.StatusBarUtil;

public class Splash_Screen_Activity extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 3000;  // Increased to allow full animations

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_splash_screen);

        StatusBarUtil.setTransparent(this);

        ImageView logo = findViewById(R.id.imageView4);
        TextView appName = findViewById(R.id.textView);

        // Apply the scale, rotate, and fade-in animation to the logo
        Animation scaleRotateFadeIn = AnimationUtils.loadAnimation(this, R.anim.scale_rotate_fade_in);
        logo.startAnimation(scaleRotateFadeIn);

        // Delay the text animation slightly
        new Handler().postDelayed(() -> {
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            appName.startAnimation(fadeIn);
        }, 500);

        new Handler().postDelayed(() -> {
            // Apply the translate and fade-out animation
            Animation translateFadeOut = AnimationUtils.loadAnimation(this, R.anim.translate_fade_out);
            logo.startAnimation(translateFadeOut);
            appName.startAnimation(translateFadeOut);
            translateFadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Intent intent = new Intent(Splash_Screen_Activity.this, Phone_Number_Activity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
        }, SPLASH_DISPLAY_LENGTH);
    }
}
