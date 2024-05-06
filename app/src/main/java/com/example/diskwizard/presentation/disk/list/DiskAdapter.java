package com.example.diskwizard.presentation.disk.list;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.diskwizard.R;
import com.example.diskwizard.databinding.FragmentDiskListItemBinding;
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
        FragmentDiskListItemBinding binding = FragmentDiskListItemBinding.bind(view);

        Disk disk = disks.get(position);

        ImageView image = binding.imageView;
        Button delbut = binding.delelement;
        binding.title.setText(disk.getName());
        binding.description.setText(disk.getDescription());

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
                            .asDrawable()
                            .load(uri)
                            .diskCacheStrategy(DiskCacheStrategy.ALL) // Включаем кэширование
                            .apply(new RequestOptions().centerCrop())
                            .into(new CustomTarget<Drawable>() {
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    image.setImageDrawable(resource);

                                    // Сохраняем изображение локально
                                    try {
                                        // Открываем файловый поток и сохраняем изображение
                                        FileOutputStream fos = getContext().openFileOutput(disk.getId() + ".png", Context.MODE_PRIVATE);
                                        Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                        fos.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                    // Очистка ресурсов, если загрузка была отменена
                                }
                            });
                }
            });
        }

        // Применяем анимацию к convertView
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.translate);
        view.startAnimation(animation);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MySharedPref",getContext().MODE_PRIVATE);
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

        if (isAdmin) {
            delbut.setVisibility(View.VISIBLE);
        } else {
            delbut.setVisibility(View.GONE);
        }

        delbut.setOnClickListener(v -> new AlertDialog.Builder(getContext())
                .setTitle("Удалить элемент")
                .setMessage("Вы действительно хотите удалить диск, " + disk.getName() + "?")
                .setPositiveButton("Ok", (dialog, which) -> {
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
                })
                .setNegativeButton("Отмена", null)
                .show());

        view.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), DiskDetails.class);
            intent.putExtra("diskId", disk.getId());
            intent.putExtra("diskName", disk.getName());
            intent.putExtra("diskDescription", disk.getDescription());
            getContext().startActivity(intent);
            activity.finish();
        });



        return view;
    }

}
