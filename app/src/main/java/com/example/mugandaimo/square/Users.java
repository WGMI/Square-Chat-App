package com.example.mugandaimo.square;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Users extends AppCompatActivity {

    //1. Create View Holder class
    //2. Create a FirebaseRecyclerAdapter
    //3. Create methods in the View Holder class to use in the adapter

    Context context;

    private Toolbar toolbar;

    private RecyclerView userList;

    private DatabaseReference userDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        context = this;
        userDB = FirebaseDatabase.getInstance().getReference().child("Users");

        toolbar = findViewById(R.id.users_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userList = findViewById(R.id.users_list);
        userList.setHasFixedSize(true);
        userList.setLayoutManager(new LinearLayoutManager(context));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<User,UserHolder> adapter = new FirebaseRecyclerAdapter<User, UserHolder>(
                User.class,
                R.layout.single_user,
                UserHolder.class,
                userDB
        ) {
            @Override
            protected void populateViewHolder(UserHolder viewHolder, User model, int position) {
                final String uid = getRef(position).getKey();

                viewHolder.setUsername(model.getUsername());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setIcon(model.getCompressed_image());
                viewHolder.setOnline(model.isOnline());
                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context,Profile.class);
                        intent.putExtra("uid",uid);
                        startActivity(intent);
                    }
                });
            }
        };

        userList.setAdapter(adapter);
    }

    public static class UserHolder extends RecyclerView.ViewHolder {

        View view;

        public UserHolder(View itemView) {
            super(itemView);

            view = itemView;
        }

        public void setUsername(String username){
            TextView nameView = view.findViewById(R.id.username);
            nameView.setText(username);
        }

        public void setStatus(String status){
            TextView statusView = view.findViewById(R.id.status);
            statusView.setText(status);
        }

        public void setIcon(String compressedImage){
            CircleImageView avatar = view.findViewById(R.id.avatar);
            Picasso.get().load(compressedImage).placeholder(R.drawable.nif).into(avatar);
        }

        public void setOnline(boolean online) {
            if(online){
                View onlineView = view.findViewById(R.id.online);
                onlineView.setVisibility(View.VISIBLE);
                onlineView.setBackgroundColor(Color.parseColor("#228B22"));
            }
        }
    }
}
