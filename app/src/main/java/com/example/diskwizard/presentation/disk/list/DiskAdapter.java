package com.example.diskwizard.presentation.disk.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.diskwizard.R;
import com.example.diskwizard.domain.model.Disk;
import com.example.diskwizard.domain.service.disk.FirebaseDiskService;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class DiskAdapter extends ArrayAdapter<Disk> {

    private int layout;
    private List<Disk> disks;
    private LayoutInflater inflater;
    private FirebaseDiskService diskService;

    public DiskAdapter(Context context, int resource, List<Disk> disks) {
        super(context, resource, disks);
        this.disks = disks;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
        this.diskService = new FirebaseDiskService();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = inflater.inflate(this.layout, parent, false);

        TextView title = view.findViewById(R.id.title);
        TextView description = view.findViewById(R.id.description);
        ImageView image = view.findViewById(R.id.imageView);

        Disk disk = disks.get(position);

        title.setText(disk.getName());
        description.setText(disk.getDescription());

        StorageReference imageRef = diskService.getImage(disk.getId());

        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(getContext())
                    .load(uri)
                    .apply(new RequestOptions().centerCrop())
                    .into(image);
        });

        return view;
    }

}
