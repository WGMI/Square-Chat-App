package com.example.mugandaimo.square;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class Chats extends Fragment {

    private RecyclerView chats;

    private DatabaseReference chatDB,msgDB,userDB;

    private String currentUId;

    private View view;


    public Chats() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_chats, container, false);

        chats = view.findViewById(R.id.chats_list);

        currentUId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        chatDB = FirebaseDatabase.getInstance().getReference().child("Chat").child(currentUId);

        chatDB.keepSynced(true);
        userDB = FirebaseDatabase.getInstance().getReference().child("Users");
        msgDB = FirebaseDatabase.getInstance().getReference().child("Messages").child(currentUId);
        userDB.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        chats.setHasFixedSize(true);
        chats.setLayoutManager(linearLayoutManager);


        // Inflate the layout for this fragment
        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        Query chatsQuery = chatDB.orderByChild("time");

        FirebaseRecyclerAdapter<Chat,ChatViewHolder> adapter = new FirebaseRecyclerAdapter<Chat, ChatViewHolder>(
                Chat.class,
                R.layout.single_user,
                ChatViewHolder.class,
                chatsQuery
        ) {
            @Override
            protected void populateViewHolder(final ChatViewHolder viewHolder, final Chat model, int position) {
                final String recipientID = getRef(position).getKey();

                Query lastMessageQuery = msgDB.child(recipientID).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String msg = dataSnapshot.child("message").getValue().toString();
                        Log.d("user_ds", "populateViewHolder: " + dataSnapshot.toString());
                        viewHolder.setMessage(msg);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                userDB.child(recipientID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String username = dataSnapshot.child("username").getValue().toString();
                        String avatar = dataSnapshot.child("compressed_image").getValue().toString();

                        if(dataSnapshot.hasChild("online")) {

                            String online = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setOnline(online);

                        }

                        viewHolder.setName(username);
                        viewHolder.setAvatar(avatar);

                        viewHolder.v.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("userID", recipientID);
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

        chats.setAdapter(adapter);

    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        View v;

        public ChatViewHolder(View itemView) {
            super(itemView);
            v = itemView;
        }

        public void setMessage(String message){
            TextView statusView = v.findViewById(R.id.status);
            statusView.setText(message);
        }

        public void setName(String name){
            TextView usernameView = v.findViewById(R.id.username);
            usernameView.setText(name);
        }

        public void setAvatar(String avatarString){
            CircleImageView avatar = v.findViewById(R.id.avatar);
            Picasso.get().load(avatarString).placeholder(R.drawable.nif).into(avatar);

        }

        public void setOnline(String online) {
            View userOnlineView = v.findViewById(R.id.online);

            if(online.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            } else {
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }
    }
}