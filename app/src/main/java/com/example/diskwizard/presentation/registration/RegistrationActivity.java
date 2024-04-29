package com.example.diskwizard.presentation.registration;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.diskwizard.domain.model.User;
import com.example.diskwizard.presentation.login.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.example.diskwizard.databinding.ActivityRegistrationBinding;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity {
    ActivityRegistrationBinding binding;
    FirebaseAuth auth;
    TextInputEditText emailField;
    TextInputEditText nameField;
    TextInputEditText passwordField;
    TextInputEditText repeatpasswordField;
    FirebaseDatabase db = FirebaseDatabase.getInstance();

    DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference("Users");

        binding.regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRegistrationClick();
            }

        });

        binding.textView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void onRegistrationClick() {
        final EditText email = binding.emailField;
        final EditText name = binding.nameField;
        final EditText pass = binding.passwordField;
        final EditText repeatpass = binding.repeatpasswordField;

        if (TextUtils.isEmpty(email.getText().toString())) {
            Snackbar.make(binding.textView4, "Enter your e-mail address", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(name.getText().toString())) {
            Snackbar.make(binding.textView4,
                    "Enter your name",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (pass.getText().toString().length() < 5) {
            Snackbar.make(binding.textView4,
                    "Enter the password at least 5 characters", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (!pass.getText().toString().equals(repeatpass.getText().toString())) {
            Snackbar.make(binding.textView4,
                    "The passwords don't match",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email.getText().toString(), pass.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        User user = new User();
                        user.setEmail(email.getText().toString());
                        user.setName(name.getText().toString());
                        user.setPass(pass.getText().toString());
                        user.setAdmin(false);

                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        // Создание объекта UserProfileChangeRequest и установка имени пользователя
                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(name.getText().toString())
                                                .build();

                                        // Обновление профиля пользователя
                                        FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Snackbar.make(binding.textView4, "Успешная регистрация!", Snackbar.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                });

                        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Display the error message to the user
                        Snackbar.make(binding.textView4, "Registration failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
            }
    }