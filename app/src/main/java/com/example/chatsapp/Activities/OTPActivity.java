package com.example.chatsapp.Activities;

import static android.content.ContentValues.TAG;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OTP_Activity extends AppCompatActivity {

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
        dialog = new ProgressDialog(this);
        dialog.setMessage("Sending OTP...");
        dialog.setCancelable(false);
        dialog.show();

        auth = FirebaseAuth.getInstance();

        Objects.requireNonNull(getSupportActionBar()).hide();

        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        binding.phoneLbl.setText("Verify " + phoneNumber);

//        Toast.makeText(this, phoneNumber, Toast.LENGTH_SHORT).show();

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(OTP_Activity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        Toast.makeText(OTP_Activity.this, "Verified", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        dialog.dismiss();
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            Toast.makeText(OTP_Activity.this, "Invalid request: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        } else if (e instanceof FirebaseAuthMissingActivityForRecaptchaException) {
                            // reCAPTCHA verification requires an activity to be provided
                            Toast.makeText(OTP_Activity.this, "reCAPTCHA verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            // Other errors
                            Toast.makeText(OTP_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        Intent intent = new Intent(OTP_Activity.this, Phone_Number_Activity.class);
                        startActivity(intent);
                        Log.e("VerificationFailed", "Verification failed with error: " + e.getMessage());
                    }

                    @Override
                    public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verifyId, forceResendingToken);
                        Toast.makeText(OTP_Activity.this, "OTP Sent", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        verificationId = verifyId;
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        binding.otpView.requestFocus();
                    }
                }).build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        binding.otpView.setOtpCompletionListener(otp -> {
//            Toast.makeText(OTP_Activity.this, otp, Toast.LENGTH_SHORT).show();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

            auth.signInWithCredential(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(OTP_Activity.this, "OTP Verified", Toast.LENGTH_SHORT).show();


                    Intent intent = new Intent(OTP_Activity.this, Setup_Profile_Activity.class);
                    startActivity(intent);
                    finishAffinity();
                } else {
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(OTP_Activity.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(OTP_Activity.this, "Verification failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }
}
