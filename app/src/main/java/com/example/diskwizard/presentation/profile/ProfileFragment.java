package com.example.diskwizard.presentation.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.diskwizard.MainActivity;
import com.example.diskwizard.R;
import com.example.diskwizard.databinding.FragmentProfileBinding;
import com.example.diskwizard.presentation.login.LoginActivity;
import com.example.diskwizard.presentation.registration.RegistrationActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.UploadTask;

public class ProfileFragment extends Fragment {

    public FragmentProfileBinding binding;
    private static final int REQUEST_CODE_IMAGE = 101;
    private StorageReference storageReference;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String name = user.getDisplayName();
    private FirebaseAuth mAuth;

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }

    private DatabaseReference usersRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        storageReference = FirebaseStorage.getInstance().getReference();
        binding.UserName.setText(name);

        // Настройка обработчика событий для кнопки exit
        binding.exit.setOnClickListener(view1 -> exit());

        // Аватарка
        binding.changeAVA.setOnClickListener(view1 -> chooseImage());
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference fileRef = storageReference.child(userId + "/pfp.jpg");
        loadNewImage(fileRef);

        //Изменения аккаунта
        binding.changePass.setOnClickListener(view1 -> changeAccPass());
        binding.changeName.setOnClickListener(view1 -> changeAccName());

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //Изменение статуса админа
        if (currentUser != null && currentUser.getDisplayName() != null && currentUser.getDisplayName().equals("DmitryAce")) {
            binding.textView31.setVisibility(View.VISIBLE);
            binding.adminNameBlock.setVisibility(View.VISIBLE);
            binding.changeAdmin.setVisibility(View.VISIBLE);
            binding.textView30.setVisibility(View.GONE);
            binding.newName.setVisibility(View.GONE);
            binding.changeName.setVisibility(View.GONE);
        } else {
            binding.textView31.setVisibility(View.GONE);
            binding.adminNameBlock.setVisibility(View.GONE);
            binding.changeAdmin.setVisibility(View.GONE);
            binding.textView30.setVisibility(View.VISIBLE);
            binding.newName.setVisibility(View.VISIBLE);
            binding.changeName.setVisibility(View.VISIBLE);
        }

        binding.changeAdmin.setOnClickListener(view1 -> changeAdminState());

        return view;
    }

    public void exit(){
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    // IMAGE
    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            uploadImage(imageUri, userId);
        }
    }

    private void uploadImage(Uri imageUri, String userId) {
        StorageReference fileRef = storageReference.child(userId + "/pfp.jpg");

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(getContext(), "Image uploaded", Toast.LENGTH_SHORT).show();
                    loadNewImage(fileRef);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error uploading image", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadNewImage(StorageReference fileRef) {

        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this)
                    .load(uri)
                    .into(binding.imageView);
        });
    }

    // Img COPY\DELET

    public void copyAndDeleteImage(String oldName, String newName) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference avatarsRef = storage.getReference().child("avatars");

        StorageReference oldImageRef = avatarsRef.child(oldName);

        oldImageRef.getMetadata().addOnSuccessListener(storageMetadata -> {
            String mimeType = storageMetadata.getContentType();
            String ext = "";

            switch (mimeType) {
                case "image/jpeg":
                    ext = "jpeg";
                    break;
                case "image/png":
                    ext = "png";
                    break;
                case "image/jpg":
                    ext = "jpg";
                    break;
                // Добавьте здесь другие типы MIME, если это необходимо
            }

            if (!ext.isEmpty()) {
                StorageReference newImageRef = avatarsRef.child(newName + "." + ext);

                oldImageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
                    newImageRef.putBytes(bytes).addOnSuccessListener(taskSnapshot -> {
                        // Image copied successfully
                        // Now delete the old image
                        oldImageRef.delete().addOnSuccessListener(aVoid -> {
                            // Old image deleted successfully
                        }).addOnFailureListener(e -> {
                            // Handle any errors
                        });
                    }).addOnFailureListener(e -> {
                        // Handle any errors
                    });
                }).addOnFailureListener(e -> {
                    // Handle any errors
                });
            }
        }).addOnFailureListener(e -> {
            // Handle any errors
        });
    }

    // ACCOUNT SETTINGS
    public void changeAccPass() {
        final EditText current_password = binding.curPass;
        final EditText new_password = binding.newPass;
        final EditText repeat_password = binding.repPass;

        if (TextUtils.isEmpty(current_password.getText().toString())) {
            Snackbar.make(binding.textView30, "Укажите текущий пароль", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(new_password.getText().toString())) {
            Snackbar.make(binding.textView30,
                    "Введите новый пароль",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(repeat_password.getText().toString())) {
            Snackbar.make(binding.textView30,
                    "Повторите пароль",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (new_password.getText().toString().length() < 5) {
            Snackbar.make(binding.textView30,
                    "Пароль должен быть не менее 5 символов", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (!new_password.getText().toString().equals(repeat_password.getText().toString())) {
            Snackbar.make(binding.textView30,
                    "Пароли не совпадают",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Получение текущего пользователя Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Проверка правильности старого пароля
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), current_password.getText().toString());
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Пароль успешно подтвержден, теперь можно изменить пароль
                        changeUserPassword(name, current_password.getText().toString(), new_password.getText().toString());
                    } else {
                        Snackbar.make(binding.textView30,
                                "Текущий пароль неверный",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    // Метод для изменения пароля пользователя в Firebase
    private void changeUserPassword(String name, String currentPassword, String newPassword) {
        FirebaseAuth.getInstance().getCurrentUser().updatePassword(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Пароль успешно изменен в Firebase Authentication, обновляем в Realtime Database
                        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

                        // Находим пользователя с заданным именем в базе данных и обновляем его пароль
                        usersRef.orderByChild("name").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                    // Обновляем поле пароля для пользователя
                                    userSnapshot.getRef().child("pass").setValue(newPassword);
                                }
                                Snackbar.make(binding.textView30, "Пароль успешно изменен", Snackbar.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Snackbar.make(binding.textView30, "Ошибка при изменении пароля в Realtime Database: " + databaseError.getMessage(), Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Snackbar.make(binding.textView30, "Ошибка при изменении пароля в Firebase Authentication: " + task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    public void changeAccName() {
        final EditText newName = binding.newName;

        if (TextUtils.isEmpty(newName.getText().toString())) {
            Snackbar.make(binding.textView30, "Введите новое имя", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Получение текущего пользователя Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName.getText().toString())
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Имя пользователя успешно изменено в Firebase Authentication и отображается,
                            // теперь обновляем в Realtime Database
                            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
                            usersRef.orderByChild("email").equalTo(user.getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                        // Обновляем поле имени пользователя в Realtime Database
                                        userSnapshot.getRef().child("name").setValue(newName.getText().toString());
                                    }
                                    Snackbar.make(binding.textView30, "Имя пользователя успешно изменено", Snackbar.LENGTH_SHORT).show();
                                    binding.UserName.setText(newName.getText().toString());

                                    setName(newName.getText().toString());
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Snackbar.make(binding.textView30, "Ошибка при изменении имени пользователя в Realtime Database: " + databaseError.getMessage(), Snackbar.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Snackbar.make(binding.textView30, "Ошибка при изменении имени пользователя: " + task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Snackbar.make(binding.textView30, "Пользователь не авторизован", Snackbar.LENGTH_SHORT).show();
        }
    }

    // Change AdminState
    public void changeAdminState() {
        final EditText nameEditText = binding.adminName;
        final String adminName = nameEditText.getText().toString().trim();

        if (TextUtils.isEmpty(adminName)) {
            Snackbar.make(binding.textView30, "Введите имя", Snackbar.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String name = snapshot.child("name").getValue(String.class);
                    if (name != null && name.equals(nameEditText.getText().toString())) {
                        boolean adminStatus = snapshot.child("admin").getValue(Boolean.class);
                        snapshot.getRef().child("admin").setValue(!adminStatus);
                        Snackbar.make(binding.textView30, "Статус изменен", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                }
                Snackbar.make(binding.textView30, "Пользователь не найден", Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обработка ошибок
            }
        });
    }
}
