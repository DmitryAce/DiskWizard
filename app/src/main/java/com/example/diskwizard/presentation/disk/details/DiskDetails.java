package com.example.diskwizard.presentation.disk.details;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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

    CommentAdapter commentAdapter;

    DatabaseReference disksRef = FirebaseDatabase.getInstance().getReference().child("Disks");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDiskDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

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
                    commentAdapter = new CommentAdapter(DiskDetails.this, R.layout.fragment_comment_item, comments);
                    binding.commentList.setAdapter(commentAdapter);
                } catch (Exception e) {
                    // Обработка ошибки
                    Log.e("Comments", "Error creating CommentAdapter: " + e.getMessage());
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
            commentAdapter.notifyDataSetChanged();
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
        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this)
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Включаем кэширование
                    .apply(new RequestOptions().centerCrop())
                    .into(binding.imageView);
        });
    }
}