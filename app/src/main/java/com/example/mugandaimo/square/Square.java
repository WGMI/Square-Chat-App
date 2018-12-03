package com.example.mugandaimo.square;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Muganda Imo on 9/27/2018.
 */

public class Square extends Application {

    private FirebaseAuth auth;
    private DatabaseReference userDB;

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null){
            userDB = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());

            userDB.addValueEventListener(new ValueEventListener() {
                @Override
                public void  onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot != null){
                        userDB.child("online").onDisconnect().setValue(false);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
