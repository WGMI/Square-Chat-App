package com.example.mugandaimo.square;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    Context context;

    private CircleImageView avatar;
    private TextView username,status;
    private Button message,friend;

    private String uid,usernameString;

    private FirebaseAuth auth;
    private DatabaseReference friendsDB,userDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        context = this;

        auth = FirebaseAuth.getInstance();
        final String currentUID = auth.getCurrentUser().getUid();

        uid = getIntent().getStringExtra("uid");
        friendsDB = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUID);;
        userDB = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        avatar = findViewById(R.id.avatar);
        username = findViewById(R.id.username);
        status = findViewById(R.id.status);
        message = findViewById(R.id.message);
        friend = findViewById(R.id.friend);

        userDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usernameString = dataSnapshot.child("username").getValue().toString();
                String statusString = dataSnapshot.child("status").getValue().toString();
                String imageLink = dataSnapshot.child("image").getValue().toString();

                username.setText(usernameString);
                status.setText(statusString);

                Picasso.get().load(imageLink).placeholder(R.drawable.nif).into(avatar);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(uid.equals(currentUID)){
            message.setVisibility(View.INVISIBLE);
            friend.setVisibility(View.INVISIBLE);
        }

        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,ChatActivity.class);
                intent.putExtra("userID",uid);
                intent.putExtra("user",usernameString);
                startActivity(intent);
            }
        });

        friendsDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(uid)){
                    friend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            friendsDB.child(uid).child("time").setValue(ServerValue.TIMESTAMP);
                            Toast.makeText(context,"You and " + usernameString + " are friends!",Toast.LENGTH_SHORT).show();
                        }
                    });
                } else{
                    friend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(context,"You and " + usernameString + " are friends!",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
