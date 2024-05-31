package com.example.chatsapp.Activities;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.chatsapp.Fragments.MainFragment;
import com.example.chatsapp.Fragments.StatusFragment;
import com.example.chatsapp.R;

public class Main_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new StatusFragment())
                    .commit();
        }
    }

}
