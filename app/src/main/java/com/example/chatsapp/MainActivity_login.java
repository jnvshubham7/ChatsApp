package com.example.chatsapp;

import static androidx.databinding.DataBindingUtil.setContentView;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.chatsapp.databinding.ActivityMainLoginBinding;


public class MainActivity_login  extends AppCompatActivity {

    private ActivityMainLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Load the LoginFragment by default
        loadFragment(new LoginFragment());
    }

    private void loadFragment(Fragment fragment) {
        // Create a new fragment and transaction
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

}
