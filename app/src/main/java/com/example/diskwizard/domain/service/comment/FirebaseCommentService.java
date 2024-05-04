package com.example.diskwizard.domain.service.comment;

import com.example.diskwizard.domain.model.Comment;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class FirebaseCommentService implements CommentService {

    private DatabaseReference mDatabase;

    public FirebaseCommentService() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Comments");
    }

    @Override
    public List<Comment> getAll() {
        final List<Comment> disks = new ArrayList<>();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Comment comment = postSnapshot.getValue(Comment.class);
                    disks.add(comment);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
        return disks;
    }
}
