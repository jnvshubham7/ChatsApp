package com.example.chatsapp.Activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.chatsapp.Adapters.ViewPagerAdapter;
import com.example.chatsapp.Fragments.ChatsFragment;
import com.example.chatsapp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Chats");
                            tab.setIcon(R.drawable.ic_chats);
                            break;
                        case 1:
                            tab.setText("Status");
                            tab.setIcon(R.drawable.ic_status);
                            break;
                    }
                }).attach();

        tabLayout.setBackgroundColor(getResources().getColor(R.color.tab_background, getTheme()));
        tabLayout.setTabTextColors(
                getResources().getColor(R.color.tab_text_color, getTheme()),
                getResources().getColor(R.color.secondaryTextColor, getTheme())
        );
    }

    @Override
    public void onBackPressed() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem == 0) {
            ChatsFragment chatsFragment = (ChatsFragment) viewPagerAdapter.createFragment(0);
            if (chatsFragment != null && chatsFragment.isSearchActive()) {
                chatsFragment.closeSearch();
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }
}
