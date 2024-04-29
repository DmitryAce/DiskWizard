package com.example.diskwizard.presentation.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.diskwizard.MainActivity;
import com.example.diskwizard.R;
import com.example.diskwizard.databinding.FragmentProfileBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;
    private static final int REQUEST_CODE_IMAGE = 101;
    private StorageReference storageReference;

    String name;

    public ProfileFragment(String name) {
        this.name = name;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        storageReference = FirebaseStorage.getInstance().getReference();
        binding.changeAVA.setOnClickListener(view1 -> chooseImage());
        StorageReference fileRef = storageReference.child("avatars/" + name + ".jpg");
        binding.UserName.setText(name);
        loadNewImage(fileRef);
        return view;
    }

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
            uploadImage(imageUri);
        }
    }

    private void uploadImage(Uri imageUri) {
        StorageReference fileRef = storageReference.child("avatars/" + name + ".jpg");

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

}
