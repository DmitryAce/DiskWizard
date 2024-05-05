package com.example.diskwizard.presentation.disk.list;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.content.SharedPreferences;
import com.example.diskwizard.databinding.FragmentDiskListBinding;
import com.example.diskwizard.domain.service.disk.FirebaseDiskService;

import com.example.diskwizard.R;
import com.example.diskwizard.domain.model.Disk;
import com.example.diskwizard.domain.service.disk.DiskService;
import com.example.diskwizard.presentation.disk.add.DiskADD;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DiskListFragment extends Fragment {

    private final DiskService diskService = new FirebaseDiskService();
    public FragmentDiskListBinding binding;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
    FloatingActionButton buttonAdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDiskListBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        ListView listView = binding.diskList;
        buttonAdd = binding.addAdminButton;

        buttonAdd.setOnClickListener(view1 -> goToDiskAdding());

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MySharedPref", getActivity().MODE_PRIVATE);
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

        if (isAdmin) {
            buttonAdd.setVisibility(View.VISIBLE);
        } else {
            buttonAdd.setVisibility(View.GONE);
        }

        DatabaseReference disksRef = FirebaseDatabase.getInstance().getReference().child("Disks");

        binding.searchtext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Ничего не делать
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Ничего не делать
            }

            @Override
            public void afterTextChanged(Editable s) {
                String searchText = s.toString();
                disksRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Disk> disks = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Disk disk = snapshot.getValue(Disk.class);
                            if (disk != null) {
                                String diskName = disk.getName().replaceAll("\\s","").toLowerCase();
                                String search = searchText.replaceAll("\\s","").toLowerCase();
                                if (diskName.contains(search)) {
                                    disks.add(disk);
                                }
                            }
                        }
                        DiskAdapter diskAdapter = new DiskAdapter(requireActivity(), R.layout.fragment_disk_list_item, disks);

                        listView.setAdapter(diskAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Обработка ошибок
                    }
                });
            }
        });


        // Извлекаем данные о дисках из базы данных и заполняем список
        disksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Disk> disks = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Disk disk = snapshot.getValue(Disk.class);
                    if (disk != null) {
                        disks.add(disk);
                    }
                }
                DiskAdapter diskAdapter = new DiskAdapter(requireActivity(), R.layout.fragment_disk_list_item, disks);

                listView.setAdapter(diskAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Обработка ошибок
            }
        });

        return view;
    }


    public void goToDiskAdding() {
        Intent intent = new Intent(requireActivity(), DiskADD.class);
        startActivity(intent);
        requireActivity().finish();
    }
}
