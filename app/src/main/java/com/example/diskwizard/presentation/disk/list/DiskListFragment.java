package com.example.diskwizard.presentation.disk.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.diskwizard.domain.service.disk.LocalDiskService;

import com.example.diskwizard.R;
import com.example.diskwizard.domain.model.Disk;
import com.example.diskwizard.domain.service.disk.DiskService;

import java.util.List;

public class DiskListFragment extends Fragment {

    private final DiskService diskService = new LocalDiskService();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_disk_list, container, false);

        List<Disk> disks = diskService.getAll();
        ListView listView = view.findViewById(R.id.diskList);
        DiskAdapter diskAdapter = new DiskAdapter(view.getContext(), R.layout.fragment_disk_list_item, disks);

        listView.setAdapter(diskAdapter);

        Fragment fragment = new Fragment();

        return view;
    }

}
