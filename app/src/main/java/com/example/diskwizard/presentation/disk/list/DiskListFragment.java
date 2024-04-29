package com.example.diskwizard.presentation.disk.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.diskwizard.databinding.ActivityRegistrationBinding;
import com.example.diskwizard.databinding.FragmentDiskListBinding;
import com.example.diskwizard.domain.model.User;
import com.example.diskwizard.domain.service.disk.LocalDiskService;

import com.example.diskwizard.R;
import com.example.diskwizard.domain.model.Disk;
import com.example.diskwizard.domain.service.disk.DiskService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class DiskListFragment extends Fragment {

    private final DiskService diskService = new LocalDiskService();
    FragmentDiskListBinding binding;
    String name;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDiskListBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        List<Disk> disks = diskService.getAll();
        ListView listView = binding.diskList;
        DiskAdapter diskAdapter = new DiskAdapter(view.getContext(), R.layout.fragment_disk_list_item, disks);
        FloatingActionButton button_add = binding.addAdminButton;

        if (name.equals("dmitryace")) {
            button_add.setVisibility(View.VISIBLE);
        } else {
            button_add.setVisibility(View.GONE);
        }

        listView.setAdapter(diskAdapter);

        Fragment fragment = new Fragment();

        return view;
    }

    public DiskListFragment(String name) {
        this.name = name;
    }
}
