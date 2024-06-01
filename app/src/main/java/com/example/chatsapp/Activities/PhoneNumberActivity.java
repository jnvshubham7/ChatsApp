package com.example.chatsapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatsapp.R;
import com.example.chatsapp.databinding.ActivityPhoneNumberBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.hbb20.CountryCodePicker;

import java.util.Objects;

public class PhoneNumberActivity extends AppCompatActivity {

    ActivityPhoneNumberBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Register the EditText with CountryCodePicker
        binding.ccp.registerCarrierNumberEditText(binding.phoneBox);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(PhoneNumberActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        Objects.requireNonNull(getSupportActionBar()).hide();

        binding.phoneBox.requestFocus();

        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the full phone number including the country code
                String fullPhoneNumber = binding.ccp.getFullNumberWithPlus();

                // Validate the phone number
                if (binding.ccp.isValidFullNumber()) {
                    Intent intent = new Intent(PhoneNumberActivity.this, OTPActivity.class);
                    intent.putExtra("phoneNumber", fullPhoneNumber);
                    startActivity(intent);
                } else {
                    binding.phoneBox.setError("Invalid phone number");
                }
            }
        });
    }
}
