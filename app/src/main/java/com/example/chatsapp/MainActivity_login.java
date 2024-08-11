package com.example.chatsapp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.chatsapp.ViewPagerAdapter;
import com.example.chatsapp.databinding.ActivityMainLoginBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity_login extends AppCompatActivity {

    private ActivityMainLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up ViewPager2 with the FragmentStateAdapter
        binding.viewPager.setAdapter(new ViewPagerAdapter(this));

        // Attach TabLayout with ViewPager2
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                (tab, position) -> tab.setText(position == 0 ? "Login" : "Sign Up")).attach();
    }
}
