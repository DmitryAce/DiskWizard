package com.example.diskwizard.presentation.disk.list;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.diskwizard.R;
import com.example.diskwizard.domain.model.Disk;
import com.example.diskwizard.domain.service.disk.FirebaseDiskService;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
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
        Button delbut = view.findViewById(R.id.delelement);

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


        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MySharedPref",getContext().MODE_PRIVATE);
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

        if (isAdmin) {
            delbut.setVisibility(View.VISIBLE);
        } else {
            delbut.setVisibility(View.GONE);
        }

        delbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(getContext())
                        .setTitle("Удалить элемент")
                        .setMessage("Вы действительно хотите удалить диск, " + disk.getName() + "?")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Получаем ID диска, который нужно удалить
                                String diskId = disk.getId();

                                // Удаляем диск из Firebase
                                DatabaseReference diskRef = FirebaseDatabase.getInstance().getReference().child("Disks").child(diskId);
                                StorageReference StorageRef = FirebaseStorage.getInstance().getReference().child("Disks").child(diskId+".jpg");
                                diskRef.removeValue();
                                StorageRef.delete();

                                // Удаляем диск из списка и обновляем адаптер
                                disks.remove(position);
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("Отмена", null)
                        .show();
            }
        });


        return view;
    }

}
