package com.example.diskwizard.presentation.disk.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.diskwizard.R;
import com.example.diskwizard.domain.model.Disk;

import java.util.List;

public class DiskAdapter extends ArrayAdapter<Disk> {

    private int layout;
    private List<Disk> disks;
    private LayoutInflater inflater;

    public DiskAdapter(Context context, int resource, List<Disk> disks) {
        super(context, resource, disks);
        this.disks = disks;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = inflater.inflate(this.layout, parent, false);

        TextView title = view.findViewById(R.id.title);
        TextView description = view.findViewById(R.id.description);

        Disk disk = disks.get(position);

        title.setText(disk.getName());
        description.setText(disk.getDescription());

        return view;
    }

}
