package com.example.diskwizard.presentation.disk.details;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.diskwizard.MainActivity;
import com.example.diskwizard.R;
import com.example.diskwizard.databinding.ActivityDiskDetailsBinding;
import com.example.diskwizard.databinding.ActivityLoginBinding;
import com.example.diskwizard.domain.model.Comment;
import com.example.diskwizard.domain.model.Disk;
import com.example.diskwizard.presentation.disk.list.DiskAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DiskDetails extends AppCompatActivity {

    ActivityDiskDetailsBinding binding;
    StorageReference Storage;
    DatabaseReference db;
    String diskId;
    private StorageReference storageReference;
    View backGroundView;

    DatabaseReference disksRef = FirebaseDatabase.getInstance().getReference().child("Disks");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("APP_PREFERENCES", MODE_PRIVATE);
        String themeName = prefs.getString("THEME", "Base.Theme.DiskWizard"); // Значение по умолчанию - ваша текущая тема

        // Устанавливаем тему
        switch (themeName) {
            case "Base.Theme.DiskWizard.Green":
                setTheme(R.style.Base_Theme_DiskWizard_Green);
                break;
            case "Base.Theme.DiskWizard.DeepPurple":
                setTheme(R.style.Base_Theme_DiskWizard_DeepPurple);
                break;
            case "Base.Theme.DiskWizard.LightBlue":
                setTheme(R.style.Base_Theme_DiskWizard_LightBlue);
                break;
            case "Base.Theme.DiskWizard.Orange":
                setTheme(R.style.Base_Theme_DiskWizard_Orange);
                break;
            default:
                setTheme(R.style.Base_Theme_DiskWizard);
                break;
        }

        super.onCreate(savedInstanceState);

        binding = ActivityDiskDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        backGroundView = binding.mainbackground;

        // Устанавливаем тему
        switch (themeName) {
            case "Base.Theme.DiskWizard.Green":
                backGroundView.setBackgroundResource(R.drawable.backgrounddeepgreen);
                break;
            case "Base.Theme.DiskWizard.DeepPurple":
                backGroundView.setBackgroundResource(R.drawable.backgrounddeeppurple);
                break;
            case "Base.Theme.DiskWizard.LightBlue":
                backGroundView.setBackgroundResource(R.drawable.backgroundskies);
                break;
            case "Base.Theme.DiskWizard.Orange":
                backGroundView.setBackgroundResource(R.drawable.backgroundorange);
                break;
            default:
                backGroundView.setBackgroundResource(R.drawable.background);
                break;
        }

        Intent intent = getIntent();
        diskId = intent.getStringExtra("diskId");
        String diskName = intent.getStringExtra("diskName");
        String diskSmallDescription = intent.getStringExtra("diskDescription");
        db = FirebaseDatabase.getInstance().getReference();

        TextView smallDescTextView = binding.SmallDescriptionText;
        TextView descTextView = binding.DescriptionText;
        TextView nameTextView = binding.tv1;

        smallDescTextView.setText(diskSmallDescription);
        nameTextView.setText(diskName);
        binding.addComment.setOnClickListener(view1 -> onAddCommentClick());

        Storage = FirebaseStorage.getInstance().getReference();
        StorageReference fileRef = Storage.child("Disks/"+diskId+".jpg");
        loadImage(fileRef);

        disksRef.child(diskId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String details = dataSnapshot.child("details").getValue(String.class);
                    descTextView.setText(details);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Обработка ошибок
            }
        });

        // Извлекаем данные о комментариях из базы данных и заполняем список
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("Comments");
        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("Comments", "commentsRef.addListenerForSingleValueEvent");
                List<Comment> comments = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    if (comment != null && (Objects.equals(comment.getDiskId(), diskId))) {
                        comments.add(comment);
                    }
                }
                Log.d("Comments", "прям перед вызовом адаптера");

                try {
                    LinearLayout commentList = findViewById(R.id.commentList);
                    for (Comment comment : comments) {
                        View commentView = getLayoutInflater().inflate(R.layout.fragment_comment_item, null);
                        // Заполните commentView данными из комментария
                        TextView nametext = commentView.findViewById(R.id.nametext);
                        TextView maintext = commentView.findViewById(R.id.maintext);
                        ImageView avaView = commentView.findViewById(R.id.avaView);
                        TextView date = commentView.findViewById(R.id.datetext);
                        Button delbut = commentView.findViewById(R.id.delelement);

                        nametext.setText(comment.getAuthor());
                        maintext.setText(comment.getMaintext());
                        date.setText(comment.getDate());

                        storageReference = FirebaseStorage.getInstance().getReference();
                        StorageReference fileRef = storageReference.child(comment.getUserId()+ "/pfp.jpg");

                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(DiskDetails.this)
                                .load(uri)
                                .diskCacheStrategy(DiskCacheStrategy.ALL) // Включаем кэширование
                                .apply(new RequestOptions().centerCrop())
                                .into(avaView));

                        SharedPreferences sharedPreferences = DiskDetails.this.getSharedPreferences("MySharedPref",DiskDetails.this.MODE_PRIVATE);
                        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

                        FirebaseUser curUser = FirebaseAuth.getInstance().getCurrentUser();
                        String curUserName = curUser.getDisplayName();

                        if (isAdmin || Objects.equals(comment.getAuthor(), curUserName)) {
                            delbut.setVisibility(View.VISIBLE);
                        } else {
                            delbut.setVisibility(View.GONE);
                        }

                        delbut.setOnClickListener(v -> new AlertDialog.Builder(DiskDetails.this)
                                .setTitle("Удалить элемент")
                                .setMessage("Вы действительно хотите удалить отзыв пользователя  " + comment.getAuthor() + "?")
                                .setPositiveButton("Ok", (dialog, which) -> {
                                    // Получаем ID диска, который нужно удалить
                                    String commentId = comment.getId();

                                    // Удаляем диск из Firebase
                                    DatabaseReference diskRef = FirebaseDatabase.getInstance().getReference().child("Comments").child(commentId);
                                    diskRef.removeValue();

                                    // Удаляем диск из списка и обновляем адаптер
                                    int position = comments.indexOf(comment);
                                    comments.remove(position);
                                    commentList.removeViewAt(position);
                                })
                                .setNegativeButton("Отмена", null)
                                .show());

                        commentList.addView(commentView);
                    }
                } catch (Exception e) {
                    // Обработка ошибки
                    Log.e("Comments", "Error creating comments: " + e.getMessage());
                    e.printStackTrace();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Comments", "ERROR: "+error.toString()); // Выводим ошибку в логи
            }
        });

        binding.backtomain.setOnClickListener(view1 -> exit());

    }

    private void onAddCommentClick() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String Author = user.getDisplayName();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String maintext = binding.newCommentText.getText().toString();
        Date currentDate = new Date();
        String date = formatDate(currentDate);

        if (maintext.isEmpty()) {
            // Поля пустые, показать ошибку
            Toast.makeText(this, "Нельзя оставить пустой отзыв", Toast.LENGTH_SHORT).show();
        } else {
            // Поля заполнены, добавить в Firebase
            DatabaseReference commentsRef = db.child("Comments");

            String id = commentsRef.push().getKey(); // Генерация уникального ключа
            Comment comment = new Comment(id, diskId, userId, Author, maintext, date);
            commentsRef.child(id).setValue(comment);

            // Создаем новое представление для комментария и добавляем его в LinearLayout
            View commentView = getLayoutInflater().inflate(R.layout.fragment_comment_item, null);

            TextView nametextView = commentView.findViewById(R.id.nametext);
            TextView maintextView = commentView.findViewById(R.id.maintext);
            ImageView avaView = commentView.findViewById(R.id.avaView);
            TextView dateView = commentView.findViewById(R.id.datetext);
            nametextView.setText(comment.getAuthor());
            maintextView.setText(comment.getMaintext());
            dateView.setText(comment.getDate());
            storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference fileRef = storageReference.child(comment.getUserId()+ "/pfp.jpg");
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(DiskDetails.this)
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Включаем кэширование
                    .apply(new RequestOptions().centerCrop())
                    .into(avaView));

            LinearLayout commentList = findViewById(R.id.commentList);
            commentList.addView(commentView);
        }
    }


    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    private void exit() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("fragmentToLoad", "search");
        startActivity(intent);
        finish();
    }

    private void loadImage(StorageReference fileRef) {
        fileRef.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(this)
                .load(uri)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Включаем кэширование
                .apply(new RequestOptions().centerCrop())
                .into(binding.imageView));
    }
}