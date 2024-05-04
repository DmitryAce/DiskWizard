package com.example.diskwizard.presentation.disk.details;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
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
import com.example.diskwizard.R;
import com.example.diskwizard.domain.model.Comment;
import com.example.diskwizard.domain.model.Disk;
import com.example.diskwizard.domain.service.disk.FirebaseDiskService;
import com.example.diskwizard.presentation.disk.details.DiskDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Objects;


public class CommentAdapter extends ArrayAdapter<Comment> {

    private int layout;
    private List<Comment> comments;
    private LayoutInflater inflater;
    private FirebaseDiskService diskService;
    private StorageReference storageReference;
    private Activity activity;

    public CommentAdapter(Activity activity, int resource, List<Comment> comments) {
        super(activity, resource, comments);
        this.comments = comments;
        this.layout = resource;
        this.inflater = LayoutInflater.from(activity);
        this.diskService = new FirebaseDiskService();
        this.activity = activity;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = inflater.inflate(this.layout, parent, false);

        TextView nametext = view.findViewById(R.id.nametext);
        TextView maintext = view.findViewById(R.id.maintext);
        ImageView avaView = view.findViewById(R.id.avaView);
        TextView date = view.findViewById(R.id.datetext);
        Button delbut = view.findViewById(R.id.delelement);
        Comment comment = comments.get(position);

        nametext.setText(comment.getAuthor());
        Log.d("Comments", "AUTHOR: "+comment.getAuthor());
        maintext.setText(comment.getMaintext());
        date.setText(comment.getDate());
        Log.d("Comments", "UID: "+comment.getUserId());
        Log.d("Comments", "PATH: "+comment.getUserId()+ "/pfp.jpg");
        storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference fileRef = storageReference.child(comment.getUserId()+ "/pfp.jpg");

        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(getContext())
                    .load(uri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Включаем кэширование
                    .apply(new RequestOptions().centerCrop())
                    .into(avaView);
        });

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MySharedPref",getContext().MODE_PRIVATE);
        boolean isAdmin = sharedPreferences.getBoolean("isAdmin", false);

        FirebaseUser curUser = FirebaseAuth.getInstance().getCurrentUser();
        String curUserName = curUser.getDisplayName();

        if (isAdmin || Objects.equals(comment.getAuthor(), curUserName)) {
            delbut.setVisibility(View.VISIBLE);
        } else {
            delbut.setVisibility(View.GONE);
        }

        delbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(getContext())
                        .setTitle("Удалить элемент")
                        .setMessage("Вы действительно хотите удалить отзыв пользователя  " + comment.getAuthor() + "?")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Получаем ID диска, который нужно удалить
                                String commentId = comment.getId();

                                // Удаляем диск из Firebase
                                DatabaseReference diskRef = FirebaseDatabase.getInstance().getReference().child("Comments").child(commentId);
                                diskRef.removeValue();

                                // Удаляем диск из списка и обновляем адаптер
                                comments.remove(position);
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