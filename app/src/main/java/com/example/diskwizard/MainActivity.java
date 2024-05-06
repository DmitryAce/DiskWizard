package com.example.diskwizard;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.diskwizard.databinding.ActivityMainBinding;
import com.example.diskwizard.presentation.about.AboutFragment;
import com.example.diskwizard.presentation.about.app.AppAboutFragment;
import com.example.diskwizard.presentation.about.developer.DeveloperAboutFragment;
import com.example.diskwizard.presentation.home.HomeFragment;
import com.google.android.material.navigation.NavigationBarView;
import com.example.diskwizard.presentation.disk.list.DiskListFragment;
import com.example.diskwizard.presentation.profile.ProfileFragment;


public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    public ActivityMainBinding binding;
    View backGroundView;

    SharedPreferences prefs;
    String themeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        prefs = getSharedPreferences("APP_PREFERENCES", MODE_PRIVATE);
        themeName = prefs.getString("THEME", "Base.Theme.DiskWizard");

        NavigationBarView navBar = binding.bottomNavigationView;
        navBar.setOnItemSelectedListener(this);
        backGroundView = binding.mainbackground;


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

        // Устанавливаем тему
        switch (themeName) {
            case "Base.Theme.DiskWizard.Green":
                setTheme(R.style.Base_Theme_DiskWizard_Green);
                backGroundView.setBackgroundResource(R.drawable.backgrounddeepgreen);
                break;
            case "Base.Theme.DiskWizard.DeepPurple":
                setTheme(R.style.Base_Theme_DiskWizard_DeepPurple);
                backGroundView.setBackgroundResource(R.drawable.backgrounddeeppurple);
                break;
            case "Base.Theme.DiskWizard.LightBlue":
                setTheme(R.style.Base_Theme_DiskWizard_LightBlue);
                backGroundView.setBackgroundResource(R.drawable.backgroundskies);
                break;
            case "Base.Theme.DiskWizard.Orange":
                setTheme(R.style.Base_Theme_DiskWizard_Orange);
                backGroundView.setBackgroundResource(R.drawable.backgroundorange);
                break;
            default:
                setTheme(R.style.Base_Theme_DiskWizard);
                backGroundView.setBackgroundResource(R.drawable.background);
                break;
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

        switch (themeName) {
            case "Base.Theme.DiskWizard.Green":
                backGroundView.setBackgroundResource(R.drawable.backgrounddeepgreen);
                break;
            case "Base.Theme.DiskWizard.DeepPurple":
                backGroundView.setBackgroundResource(R.drawable.backgrounddeeppurple);
                break;
            case "Base.Theme.DiskWizard.LightBlue":
                backGroundView.setBackgroundResource(R.drawable.backgroundskies);
                break;
            case "Base.Theme.DiskWizard.Orange":
                backGroundView.setBackgroundResource(R.drawable.backgroundorange);
                break;
            default:
                backGroundView.setBackgroundResource(R.drawable.background);
                break;
        }

        return true;
    }


    private void onAppAboutClick() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentView, new AppAboutFragment())
                .addToBackStack("backstack")
                .commit();
        switch (themeName) {
            case "Base.Theme.DiskWizard.Green":
                backGroundView.setBackgroundResource(R.drawable.backgrounddeepgreen);
                break;
            case "Base.Theme.DiskWizard.DeepPurple":
                backGroundView.setBackgroundResource(R.drawable.backgrounddeeppurple);
                break;
            case "Base.Theme.DiskWizard.LightBlue":
                backGroundView.setBackgroundResource(R.drawable.backgroundskies);
                break;
            case "Base.Theme.DiskWizard.Orange":
                backGroundView.setBackgroundResource(R.drawable.backgroundorange);
                break;
            default:
                backGroundView.setBackgroundResource(R.drawable.background);
                break;
        }
    }

    private void onDeveloperAboutClick() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentView, new DeveloperAboutFragment())
                .addToBackStack("backstack")
                .commit();
        backGroundView.setBackgroundResource(R.drawable.backgroundauthor);
    }

}