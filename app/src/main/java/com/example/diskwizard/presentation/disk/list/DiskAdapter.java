package com.example.diskwizard.presentation.disk.list;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.diskwizard.R;
import com.example.diskwizard.domain.model.Disk;
import com.example.diskwizard.domain.service.disk.FirebaseDiskService;
import com.example.diskwizard.presentation.disk.details.DiskDetails;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class DiskAdapter extends ArrayAdapter<Disk> {

    private int layout;
    private List<Disk> disks;
    private LayoutInflater inflater;
    private FirebaseDiskService diskService;

    private Activity activity;

    public DiskAdapter(Activity activity, int resource, List<Disk> disks) {
        super(activity, resource, disks);
        this.disks = disks;
        this.layout = resource;
        this.inflater = LayoutInflater.from(activity);
        this.diskService = new FirebaseDiskService();
        this.activity = activity;
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

        File file = new File(getContext().getFilesDir(), disk.getId() + ".png");
        if (file.exists()) {
            // Если файл существует, загружаем его
            Glide.with(getContext())
                    .load(file)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Включаем кэширование
                    .apply(new RequestOptions().centerCrop())
                    .into(image);
        } else {
            // Если файла нет, загружаем изображение из Firebase и сохраняем его
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                if (!activity.isDestroyed()) {
                    Glide.with(getContext())
                            .asBitmap()
                            .load(uri)
                            .diskCacheStrategy(DiskCacheStrategy.ALL) // Включаем кэширование
                            .apply(new RequestOptions().centerCrop())
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    image.setImageBitmap(resource);

                                    // Сохраняем изображение локально
                                    try {
                                        // Открываем файловый поток и сохраняем изображение
                                        FileOutputStream fos = getContext().openFileOutput(disk.getId() + ".png", Context.MODE_PRIVATE);
                                        resource.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                        fos.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                }
            });
        }



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

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), DiskDetails.class);
                intent.putExtra("diskId", disk.getId());
                intent.putExtra("diskName", disk.getName());
                intent.putExtra("diskDescription", disk.getDescription());
                getContext().startActivity(intent);
                activity.finish();
            }
        });



        return view;
    }

}
