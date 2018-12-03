package com.example.mugandaimo.square;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class Friends extends Fragment {

    Context context;
    View view;

    private String currentUID;
    private DatabaseReference root,friendsDB,userDB;
    private RecyclerView friends;

    public Friends() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_friends, container, false);
        context = getActivity();

        currentUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        root = FirebaseDatabase.getInstance().getReference();
        friendsDB = root.child("Friends").child(currentUID);
        userDB = root.child("Users");

        friends = view.findViewById(R.id.friends_list);

        friendsDB.keepSynced(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(context);

        friends.setHasFixedSize(true);
        friends.setLayoutManager(layoutManager);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friend,FriendViewHolder> adapter = new FirebaseRecyclerAdapter<Friend, FriendViewHolder>(
                Friend.class,
                R.layout.single_user,
                FriendViewHolder.class,
                friendsDB
        ) {
            @Override
            protected void populateViewHolder(final FriendViewHolder viewHolder, Friend model, int position) {
                final String friendID = getRef(position).getKey();

                DatabaseReference userDB = root.child("Users").child(friendID);
                userDB.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String username = dataSnapshot.child("username").getValue().toString();
                        String avatar = dataSnapshot.child("compressed_image").getValue().toString();

                        viewHolder.setUsername(username);
                        viewHolder.setStatus(dataSnapshot.child("status").getValue().toString());
                        viewHolder.setAvatar(avatar);
                        viewHolder.v.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("userID", friendID);
                                chatIntent.putExtra("user", username);
                                startActivity(chatIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        friends.setAdapter(adapter);
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {

        View v;

        public FriendViewHolder(View itemView) {
            super(itemView);
            v = itemView;
        }

        public void setUsername(String username) {
            TextView usernameView = v.findViewById(R.id.username);
            usernameView.setText(username);
        }

        public void setStatus(String status) {
            TextView statusView = v.findViewById(R.id.status);
            statusView.setText(status);
        }

        public void setAvatar(String avatarString){
            CircleImageView avatar = v.findViewById(R.id.avatar);
            Picasso.get().load(avatarString).placeholder(R.drawable.nif).into(avatar);
        }
    }
}
