package com.example.diskwizard.presentation.disk.list;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.diskwizard.databinding.FragmentDiskListBinding;
import com.example.diskwizard.domain.service.disk.LocalDiskService;

import com.example.diskwizard.R;
import com.example.diskwizard.domain.model.Disk;
import com.example.diskwizard.domain.service.disk.DiskService;
import com.example.diskwizard.presentation.disk.details.DiskDetails;
import com.example.diskwizard.presentation.login.LoginActivity;
import com.example.diskwizard.presentation.registration.RegistrationActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class DiskListFragment extends Fragment {

    private final DiskService diskService = new LocalDiskService();
    FragmentDiskListBinding binding;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
    FloatingActionButton buttonAdd;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDiskListBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        ListView listView = binding.diskList;
        buttonAdd = binding.addAdminButton;

        buttonAdd.setOnClickListener(view1 -> goToDiskAdding());
        usersRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean isAdmin = dataSnapshot.child("admin").getValue(Boolean.class);
                    if (isAdmin) {
                        buttonAdd.setVisibility(View.VISIBLE);
                    } else {
                        buttonAdd.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        List<Disk> disks = diskService.getAll();
        DiskAdapter diskAdapter = new DiskAdapter(view.getContext(), R.layout.fragment_disk_list_item, disks);
        listView.setAdapter(diskAdapter);

        return view;
    }

    public void goToDiskAdding() {
        Intent intent = new Intent(requireActivity(), DiskDetails.class);
        startActivity(intent);
        requireActivity().finish();
    }
}
