package com.example.diskwizard;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.diskwizard.databinding.ActivityMainBinding;
import com.example.diskwizard.databinding.FragmentProfileBinding;
import com.example.diskwizard.presentation.about.AboutFragment;
import com.example.diskwizard.presentation.about.app.AppAboutFragment;
import com.example.diskwizard.presentation.about.developer.DeveloperAboutFragment;
import com.example.diskwizard.presentation.home.HomeFragment;
import com.example.diskwizard.presentation.login.LoginActivity;
import com.example.diskwizard.presentation.registration.RegistrationActivity;
import com.google.android.material.navigation.NavigationBarView;

import com.example.diskwizard.presentation.disk.list.DiskListFragment;
import com.example.diskwizard.presentation.profile.ProfileFragment;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    String userName;

    public ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        super.onCreate(savedInstanceState);
        setContentView(view);

        NavigationBarView navBar = findViewById(R.id.bottomNavigationView);
        navBar.setOnItemSelectedListener(this);


        String fragmentToLoad = getIntent().getStringExtra("fragmentToLoad");
        if (fragmentToLoad != null && fragmentToLoad.equals("search")) {
            navBar.setSelectedItemId(R.id.search);
        } else {
            navBar.setSelectedItemId(R.id.home);
        }

        String message = getIntent().getStringExtra("toast");
        if (message != null && message.equals("Диск успешно добавлен")) {
            Toast.makeText(this, "Диск успешно добавлен", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        Fragment fragment = new Fragment();

        if (itemId == R.id.home) {
            fragment = new HomeFragment();
        }
        else if (itemId == R.id.search) {
            fragment = new DiskListFragment();
        }
        else if (itemId == R.id.profile) {
            fragment = new ProfileFragment();
        }
        else if (itemId == R.id.info) {
            fragment = new AboutFragment(this::onAppAboutClick, this::onDeveloperAboutClick);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in, R.anim.slide_out) // Добавляем анимацию
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