package com.example.chatsapp.Activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.chatsapp.Adapters.ViewPagerAdapter;
import com.example.chatsapp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.viewpager2.widget.ViewPager2;

public class Main_Activity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        viewPager.setAdapter(new ViewPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Chats");
                            tab.setIcon(R.drawable.ic_chats); // Set your chat icon here
                            break;
                        case 1:
                            tab.setText("Status");
                            tab.setIcon(R.drawable.ic_status); // Set your status icon here
                            break;
                    }
                }).attach();

        // Set colors for light and dark mode
        tabLayout.setBackgroundColor(getResources().getColor(R.color.tab_background, getTheme()));
        tabLayout.setTabTextColors(
                getResources().getColor(R.color.tab_text_color, getTheme()),
                getResources().getColor(R.color.tab_text_color_selected, getTheme())


        );
    }
}
