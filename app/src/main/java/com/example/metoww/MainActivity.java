package com.example.metoww;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;
    private Fragment homeFragment, calendarFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        homeFragment = new HomeFragment();
        calendarFragment = new CalendarFragment();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 프로그래밍 방식으로 BottomNavigationView 항목 추가
        Menu menu = bottomNavigationView.getMenu();
        menu.add(Menu.NONE, R.id.nav_home, Menu.NONE, "Home").setIcon(R.drawable.ic_home);
        menu.add(Menu.NONE, R.id.nav_calendar, Menu.NONE, "Calendar").setIcon(R.drawable.ic_calendar);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_home:
                        loadFragment(homeFragment);
                        return true;
                    case R.id.nav_calendar:
                        loadFragment(calendarFragment);
                        return true;
                }
                return false;
            }
        });

        // 기본 프래그먼트 로드
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }
    }

    private void loadFragment(Fragment fragment) {
        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }
}
