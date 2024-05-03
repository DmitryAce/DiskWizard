package com.example.diskwizard.domain.service.disk;

import android.util.Log;

import com.example.diskwizard.domain.model.Disk;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.ArrayList;
import java.util.List;

public class FirebaseDiskService implements DiskService {

    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;

    public FirebaseDiskService() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Disks");
        mStorageRef = FirebaseStorage.getInstance().getReference().child("Disks");
    }

    @Override
    public List<Disk> getAll() {
        final List<Disk> disks = new ArrayList<>();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Disk disk = postSnapshot.getValue(Disk.class);
                    disks.add(disk);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
        return disks;
    }

    public StorageReference getImage(String id) {
        Log.d("MyLogs", "получаем изображение из Storge: "+id);
        return mStorageRef.child(id+".jpg");
    }
}
