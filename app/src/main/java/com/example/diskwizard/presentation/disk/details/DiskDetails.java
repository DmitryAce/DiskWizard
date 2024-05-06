package com.example.diskwizard.presentation.disk.details;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.diskwizard.MainActivity;
import com.example.diskwizard.R;
import com.example.diskwizard.databinding.ActivityDiskDetailsBinding;
import com.example.diskwizard.databinding.FragmentCommentItemBinding;
import com.example.diskwizard.domain.model.Comment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class DiskDetails extends AppCompatActivity {

    ActivityDiskDetailsBinding binding;
    DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
    String diskId;
    View backGroundView;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    LinearLayout commentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("APP_PREFERENCES", MODE_PRIVATE);
        String themeName = prefs.getString("THEME", "Base.Theme.DiskWizard");

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
        commentList = binding.commentList;

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

        binding.SmallDescriptionText.setText(diskSmallDescription);
        binding.tv1.setText(diskName);
        binding.addComment.setOnClickListener(view1 -> onAddCommentClick());

        StorageReference fileRef = storageReference.child("Disks/"+diskId+".jpg");
        loadImage(fileRef);

        db.child("Disks").child(diskId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String details = dataSnapshot.child("details").getValue(String.class);
                    binding.DescriptionText.setText(details);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Обработка ошибок
            }
        });

        // Извлекаем данные о комментариях из базы данных и заполняем список
        db.child("Comments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Comment> comments = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Comment comment = snapshot.getValue(Comment.class);
                    if (comment != null && (Objects.equals(comment.getDiskId(), diskId))) {
                        comments.add(comment);
                    }
                }

                try {
                    for (Comment comment : comments) {
                        FragmentCommentItemBinding commentBinding = FragmentCommentItemBinding.inflate(getLayoutInflater());

                        TextView nametext = commentBinding.nametext;
                        TextView maintext = commentBinding.maintext;
                        ImageView avaView = commentBinding.avaView;
                        TextView date = commentBinding.datetext;
                        Button delbut = commentBinding.delelement;

                        nametext.setText(comment.getAuthor());
                        maintext.setText(comment.getMaintext());
                        date.setText(comment.getDate());

                        StorageReference fileRef = storageReference.child(comment.getUserId() + "/pfp.jpg");

                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(DiskDetails.this)
                                .load(uri)
                                .diskCacheStrategy(DiskCacheStrategy.ALL) // Включаем кэширование
                                .apply(new RequestOptions().centerCrop())
                                .into(avaView));

                        SharedPreferences sharedPreferences = DiskDetails.this.getSharedPreferences("MySharedPref", DiskDetails.this.MODE_PRIVATE);
                        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

                        String curUserName = user.getDisplayName();

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
                                    DatabaseReference diskRef = db.child("Comments").child(commentId);
                                    diskRef.removeValue();

                                    // Удаляем диск из списка и обновляем адаптер
                                    int position = comments.indexOf(comment);
                                    comments.remove(position);
                                    commentList.removeViewAt(position);
                                })
                                .setNegativeButton("Отмена", null)
                                .show());

                        binding.commentList.addView(commentBinding.getRoot());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        binding.backtomain.setOnClickListener(view1 -> exit());

    }

    private void onAddCommentClick() {
        String author = user.getDisplayName();
        String userId = user.getUid();
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
            Comment comment = new Comment(id, diskId, userId, author, maintext, date);
            commentsRef.child(id).setValue(comment);

            // Создаем новое представление для комментария и добавляем его в LinearLayout
            FragmentCommentItemBinding commentBinding = FragmentCommentItemBinding.inflate(getLayoutInflater());
            commentBinding.nametext.setText(comment.getAuthor());
            commentBinding.maintext.setText(comment.getMaintext());
            commentBinding.datetext.setText(comment.getDate());

            // Загрузка изображения
            StorageReference fileRef = storageReference.child(comment.getUserId()+ "/pfp.jpg");
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> Glide.with(DiskDetails.this)
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Включаем кэширование
                    .apply(new RequestOptions().centerCrop())
                    .into(commentBinding.avaView));

            commentList.addView(commentBinding.getRoot());
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