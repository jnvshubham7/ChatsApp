package com.example.chatsapp;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.chatsapp.Fragments.MainFragment;
import com.example.chatsapp.Fragments.StatusFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new MainFragment(); // Replace with your actual fragments
        } else {
            return new StatusFragment(); // Replace with your actual fragments
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Number of fragments
    }
}
