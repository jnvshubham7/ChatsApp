package com.example.chatsapp.Activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatsapp.databinding.ActivityOtpactivityBinding;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {

    private static final String TAG = "OTPActivity_error";
    ActivityOtpactivityBinding binding;
    FirebaseAuth auth;
    String verificationId;
    ProgressDialog dialog;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.e(TAG, "Activity created and view binding done");

        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending OTP...");
        dialog.setCancelable(false);
        dialog.show();
        Log.e(TAG, "ProgressDialog shown");

        auth = FirebaseAuth.getInstance();
        Log.e(TAG, "FirebaseAuth instance obtained");

        Objects.requireNonNull(getSupportActionBar()).hide();
        Log.e(TAG, "Action bar hidden");

        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        binding.phoneLbl.setText("Verify " + phoneNumber);
        Log.e(TAG, "Phone number set: " + phoneNumber);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(OTPActivity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        Log.e(TAG, "Verification completed");
                        Toast.makeText(OTPActivity.this, "Verified", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        dialog.dismiss();
                        Log.e(TAG, "Verification failed: " + e.getMessage());
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(OTPActivity.this, "Invalid request: " + e.getMessage(), Toast.LENGTH_LONG)
                                    .show();
                        } else if (e instanceof FirebaseAuthMissingActivityForRecaptchaException) {
                            Toast.makeText(OTPActivity.this, "reCAPTCHA verification failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(OTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        Intent intent = new Intent(OTPActivity.this, PhoneNumberActivity.class);
                        startActivity(intent);
                        Log.e(TAG, "Intent to PhoneNumberActivity started");
                    }

                    @Override
                    public void onCodeSent(@NonNull String verifyId,
                            @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verifyId, forceResendingToken);
                        Log.e(TAG, "OTP sent, verificationId: " + verifyId);
                        Toast.makeText(OTPActivity.this, "OTP Sent", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        verificationId = verifyId;
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        binding.otpView.requestFocus();
                        Log.e(TAG, "Soft input method toggled and focus requested on OTP view");
                    }
                }).build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        Log.e(TAG, "PhoneAuthProvider.verifyPhoneNumber called");

        binding.otpView.setOtpCompletionListener(otp -> {
            Log.e(TAG, "OTP entered: " + otp);
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
            Log.e(TAG, "PhoneAuthCredential obtained");

            auth.signInWithCredential(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.e(TAG, "OTP verified successfully");
                    Toast.makeText(OTPActivity.this, "OTP Verified", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(OTPActivity.this, SetupProfileActivity.class);
                    startActivity(intent);
                    finishAffinity();
                    Log.e(TAG, "Intent to SetupProfileActivity started and affinity finished");
                } else {
                    Log.e(TAG, "OTP verification failed: " + task.getException().getMessage());
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(OTPActivity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(OTPActivity.this, "Verification failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }
}
