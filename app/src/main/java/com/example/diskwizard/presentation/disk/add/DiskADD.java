package com.example.diskwizard.presentation.disk.add;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.diskwizard.MainActivity;
import com.example.diskwizard.R;
import com.example.diskwizard.databinding.ActivityDiskAddBinding;
import com.example.diskwizard.domain.model.Disk;
import com.example.diskwizard.presentation.disk.list.DiskListFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class DiskADD extends AppCompatActivity {
    ActivityDiskAddBinding binding;
    private static final int REQUEST_CODE_IMAGE = 102;
    StorageReference Storage;
    DatabaseReference db;
    Uri localImgURI;

    boolean diskEmpty = true;
    View backGroundView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("APP_PREFERENCES", MODE_PRIVATE);
        String themeName = prefs.getString("THEME", "Base.Theme.DiskWizard"); // Значение по умолчанию - ваша текущая тема

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
        
        binding = ActivityDiskAddBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view); // Set the content view
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

        Button buttonBack = binding.backToListDisk;
        Button buttonAdd = binding.addDiskButton;

        db = FirebaseDatabase.getInstance().getReference();
        Storage = FirebaseStorage.getInstance().getReference();

        buttonBack.setOnClickListener(view1 -> goToDiskList());
        buttonAdd.setOnClickListener(view1 -> onAddDiskClick());
        RelativeLayout rellay = binding.relmain;

        rellay.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rellay.getWindowVisibleDisplayFrame(r);
            int screenHeight = rellay.getRootView().getHeight();

            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) {
                hideViews();
            } else {
                showViews();
            }
        });

        FloatingActionButton addImg = binding.setDiskImg;
        addImg.setOnClickListener(v -> chooseImage());

    }

    private void onAddDiskClick() {
        String diskName = binding.diskName.getText().toString();
        String smallDescription = binding.SmallDescriptionText.getText().toString();
        String description = binding.DescriptionText.getText().toString();

        if (diskName.isEmpty() || smallDescription.isEmpty() || description.isEmpty() || diskEmpty) {
            // Поля пустые, показать ошибку
            Toast.makeText(this, "Заполните все поля и выберите изображение", Toast.LENGTH_SHORT).show();
        } else {
            // Поля заполнены, добавить в Firebase
            DatabaseReference disksRef = db.child("Disks");

            String id = disksRef.push().getKey(); // Генерация уникального ключа
            Disk disk = new Disk(id, diskName, smallDescription, description);
            disksRef.child(id).setValue(disk);

            // Сохранить изображение в Storage
            uploadImage(localImgURI, id);
        }
    }

    private void goToDiskList() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("fragmentToLoad", "search");
        startActivity(intent);
        finish();
    }

    private void exit() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("fragmentToLoad", "search");
        intent.putExtra("toast", "Диск успешно добавлен");
        startActivity(intent);
        finish();
    }

    private void hideViews() {
        // Скрыть нужные вам элементы интерфейса
        binding.backToListDisk.setVisibility(View.GONE);
        binding.addDiskButton.setVisibility(View.GONE);
    }

    private void showViews() {
        // Показать скрытые элементы интерфейса
        binding.backToListDisk.setVisibility(View.VISIBLE);
        binding.addDiskButton.setVisibility(View.VISIBLE);
    }

    // Обработка изображения

    // Создайте экземпляр ActivityResultLauncher
    ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    localImgURI  = data.getData();
                    if (localImgURI == null) {
                        Bundle extras = data.getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        // Convert bitmap to Uri
                        localImgURI = getImageUri(this, imageBitmap);
                    }
                    diskEmpty = false;
                    Glide.with(this)
                            .load(localImgURI)
                            .apply(new RequestOptions().centerCrop())
                            .into(binding.imageView);
                }
            }
    );

    private void chooseImage() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Intent chooser = Intent.createChooser(galleryIntent, "Выберите приложение");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { cameraIntent });

        // Запустите ActivityResultLauncher
        mGetContent.launch(chooser);
    }


    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    private void uploadImage(Uri imageUri, String diskId) {
        StorageReference fileRef = Storage.child("Disks/"+diskId+".jpg");

        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show();
                    exit();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show();
                });
    }


}
