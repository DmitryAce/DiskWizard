package com.example.diskwizard;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.diskwizard.presentation.about.AboutFragment;
import com.example.diskwizard.presentation.about.app.AppAboutFragment;
import com.example.diskwizard.presentation.about.developer.DeveloperAboutFragment;
import com.example.diskwizard.presentation.home.HomeFragment;
import com.google.android.material.navigation.NavigationBarView;

import com.example.diskwizard.presentation.disk.list.DiskListFragment;
import com.example.diskwizard.presentation.profile.ProfileFragment;


public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userName = getIntent().getStringExtra("UserName"); // Получим имя пользователя из Intent

        NavigationBarView navBar = findViewById(R.id.bottomNavigationView);
        navBar.setOnItemSelectedListener(this);
        navBar.setSelectedItemId(R.id.home);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Fragment fragment = new Fragment();

        if (itemId == R.id.home) fragment = new HomeFragment();
        else if (itemId == R.id.search) fragment = new DiskListFragment(userName);
        else if (itemId == R.id.profile) fragment = new ProfileFragment(userName);
        else if (itemId == R.id.info) fragment = new AboutFragment(this::onAppAboutClick, this::onDeveloperAboutClick);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentView, fragment)
                .setReorderingAllowed(true)
                .addToBackStack("backstack")
                .commit();

        return true;
    }

    private void onAppAboutClick() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentView, new AppAboutFragment())
                .addToBackStack("backstack")
                .commit();
    }

    private void onDeveloperAboutClick() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentView, new DeveloperAboutFragment())
                .addToBackStack("backstack")
                .commit();
    }

}