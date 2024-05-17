package com.example.diskwizard.presentation.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.diskwizard.MainActivity;
import com.example.diskwizard.R;
import com.example.diskwizard.presentation.registration.RegistrationActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.example.diskwizard.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private TextInputEditText emailField;
    private TextInputEditText passwordField;
    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
    View backGroundView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("APP_PREFERENCES", MODE_PRIVATE);
        String themeName = prefs.getString("THEME", "Base.Theme.DiskWizard");

        // При запуске приложения проверяем состояние авторизации
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Устанавливаем тему
        switch (themeName) {
            case "Base.Theme.DiskWizard.Green":
                setTheme(R.style.Base_Theme_DiskWizard_Green);
                break;
            case "Base.Theme.DiskWizard.DeepPurple":
                setTheme(R.style.Base_Theme_DiskWizard_DeepPurple);
                break;
            case "Base.Theme.DiskWizard.LightBlue":
                setTheme(R.style.Base_Theme_DiskWizard_LightBlue);
                break;
            case "Base.Theme.DiskWizard.Orange":
                setTheme(R.style.Base_Theme_DiskWizard_Orange);
                break;
            default:
                setTheme(R.style.Base_Theme_DiskWizard);
                break;
        }

        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        backGroundView = binding.mainbackground;

        // Устанавливаем фон
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

        emailField = binding.emailField;
        passwordField = binding.passwordField;

        binding.loginButton.setOnClickListener(view12 -> onLoginClick());

        binding.textView4.setOnClickListener(view1 -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
            finish();
        });


    }

    private void onLoginClick() {
        String email = emailField.getText().toString();
        String pass = passwordField.getText().toString();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Почта/Пароль не могут быть пустыми", Toast.LENGTH_LONG).show();
            return;
        }

        String hashedPassword = hashPassword(pass);

        auth.signInWithEmailAndPassword(email, hashedPassword)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = auth.getCurrentUser();
                    usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                boolean isAdmin = dataSnapshot.child("admin").getValue(Boolean.class);

                                // Сохранение значения isAdmin в SharedPreferences
                                SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                                myEdit.putBoolean("isAdmin", isAdmin);
                                myEdit.apply();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Обработка ошибок
                        }
                    });

                    // При успешной авторизации сохраняем состояние авторизации
                    SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
                    SharedPreferences.Editor myEdit = sharedPreferences.edit();
                    myEdit.putBoolean("isLoggedIn", true);
                    myEdit.apply();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })

                .addOnFailureListener(e -> Snackbar.make(binding.textView,
                        "Authorisation Error: " + e.getMessage(),
                        Snackbar.LENGTH_SHORT).show());
    }

    // Метод для хеширования пароля с использованием SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

}