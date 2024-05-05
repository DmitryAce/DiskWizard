package com.example.diskwizard.presentation.profile;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private static final int REQUEST_CODE = 72;
    private static final int REQUEST_CODE_CAMERA = 73;
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

        changeThemeHendler();
        return view;
    }

    private void changeThemeHendler() {
        Glide.with(this)
                .load(R.drawable.background)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Включаем кэширование
                .apply(new RequestOptions().centerCrop())
                .into(binding.CardimageView1);

        Glide.with(this)
                .load(R.drawable.backgrounddeepgreen)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Включаем кэширование
                .apply(new RequestOptions().centerCrop())
                .into(binding.CardimageView2);

        Glide.with(this)
                .load(R.drawable.backgrounddeeppurple)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Включаем кэширование
                .apply(new RequestOptions().centerCrop())
                .into(binding.CardimageView3);

        Glide.with(this)
                .load(R.drawable.backgroundskies)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Включаем кэширование
                .apply(new RequestOptions().centerCrop())
                .into(binding.CardimageView4);
        Glide.with(this)
                .load(R.drawable.backgroundorange)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Включаем кэширование
                .apply(new RequestOptions().centerCrop())
                .into(binding.CardimageView5);

        binding.themeCard1.setOnClickListener(v -> new AlertDialog.Builder(getContext())
                .setTitle("Смена темы")
                .setMessage("После смены темы приложение будет перезапущено.")
                .setPositiveButton("Ok", (dialog, which) -> {
                    // Сохраняем выбранную тему в SharedPreferences
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE).edit();
                    editor.putString("THEME", "Base_Theme_DiskWizard");
                    editor.apply();

                    Toast.makeText(getContext(), "Тема изменена", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                })
                .setNegativeButton("Отмена", null)
                .show());

        binding.themeCard2.setOnClickListener(v -> new AlertDialog.Builder(getContext())
                .setTitle("Смена темы")
                .setMessage("После смены темы приложение будет перезапущено.")
                .setPositiveButton("Ok", (dialog, which) -> {
                    // Сохраняем выбранную тему в SharedPreferences
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE).edit();
                    editor.putString("THEME", "Base.Theme.DiskWizard.Green");
                    editor.apply();

                    Toast.makeText(getContext(), "Тема изменена", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                })
                .setNegativeButton("Отмена", null)
                .show());

        binding.themeCard3.setOnClickListener(v -> new AlertDialog.Builder(getContext())
                .setTitle("Смена темы")
                .setMessage("После смены темы приложение будет перезапущено.")
                .setPositiveButton("Ok", (dialog, which) -> {
                    // Сохраняем выбранную тему в SharedPreferences
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE).edit();
                    editor.putString("THEME", "Base.Theme.DiskWizard.DeepPurple");
                    editor.apply();

                    Toast.makeText(getContext(), "Тема изменена", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                })
                .setNegativeButton("Отмена", null)
                .show());

        binding.themeCard4.setOnClickListener(v -> new AlertDialog.Builder(getContext())
                .setTitle("Смена темы")
                .setMessage("После смены темы приложение будет перезапущено.")
                .setPositiveButton("Ok", (dialog, which) -> {
                    // Сохраняем выбранную тему в SharedPreferences
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE).edit();
                    editor.putString("THEME", "Base.Theme.DiskWizard.LightBlue");
                    editor.apply();

                    Toast.makeText(getContext(), "Тема изменена", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                })
                .setNegativeButton("Отмена", null)
                .show());

        binding.themeCard5.setOnClickListener(v -> new AlertDialog.Builder(getContext())
                .setTitle("Смена темы")
                .setMessage("После смены темы приложение будет перезапущено.")
                .setPositiveButton("Ok", (dialog, which) -> {
                    // Сохраняем выбранную тему в SharedPreferences
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE).edit();
                    editor.putString("THEME", "Base.Theme.DiskWizard.Orange");
                    editor.apply();

                    Toast.makeText(getContext(), "Тема изменена", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getContext(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                })
                .setNegativeButton("Отмена", null)
                .show());
    }

    public void exit(){
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    // IMAGE
    // Создадим экземпляр ActivityResultLauncher
    ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Uri imageUri = data.getData();
                    if (imageUri != null) {
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        uploadImage(imageUri, userId);
                    } else {
                        Bundle extras = data.getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        // Convert bitmap to Uri
                        imageUri = getImageUri(getContext(), imageBitmap);
                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        uploadImage(imageUri, userId);
                    }
                }
            }
    );

    private void chooseImage() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Intent chooser = Intent.createChooser(galleryIntent, "Выберите приложение");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { cameraIntent });

        // Запустим ActivityResultLauncher
        mGetContent.launch(chooser);
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
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
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Включаем кэширование
                    .apply(new RequestOptions().centerCrop())
                    .into(binding.imageView);
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

                        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MySharedPref", getActivity().MODE_PRIVATE);
                        SharedPreferences.Editor myEdit = sharedPreferences.edit();
                        myEdit.putBoolean("isAdmin", !adminStatus);

                        myEdit.apply();
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
