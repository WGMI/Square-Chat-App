package com.example.mugandaimo.square;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    Context context;
    private String recipientID,usernameString;
    private String currentUserID;

    private FirebaseAuth auth;
    private DatabaseReference root;
    private DatabaseReference userDB;

    private Toolbar toolbar;
    private TextView barUsername,barOnline;
    private CircleImageView barAvatar;

    private EditText messageBox;
    private ImageButton send;

    private RecyclerView messages;

    private List<Message> messageList;
    private LinearLayoutManager layoutManager;
    private MessageAdapter adapter;

    private static final int PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat2);

        context = this;
        recipientID = getIntent().getStringExtra("userID");
        usernameString = getIntent().getStringExtra("user");

        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        root = FirebaseDatabase.getInstance().getReference();
        userDB = root.child("Users");

        toolbar = findViewById(R.id.chat_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);
        actionBar.setTitle("");

        barUsername = findViewById(R.id.username);
        barOnline = findViewById(R.id.online);
        barAvatar = findViewById(R.id.bar_avatar);

        barUsername.setText(usernameString);
        barOnline.setText("Not Online");

        messageBox = findViewById(R.id.message);
        send = findViewById(R.id.send);

        messages = findViewById(R.id.messages);
        messages.setHasFixedSize(true);

        messageList = new ArrayList<>();
        layoutManager = new LinearLayoutManager(context);

        messages.setLayoutManager(layoutManager);
        adapter = new MessageAdapter(messageList);
        messages.setAdapter(adapter);

        loadMessages();

        root.child("Users").child(recipientID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                if(online.equals("true")){
                    barOnline.setText("Online");
                }

                String imageLink = dataSnapshot.child("compressed_image").getValue().toString();

                Picasso.get().load(imageLink).placeholder(R.drawable.nif).into(barAvatar);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        root.child("Chats").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(recipientID)){
                    Map chatMap = new HashMap();
                    chatMap.put("seen",false);
                    chatMap.put("time", ServerValue.TIMESTAMP);

                    Map chatUser = new HashMap();
                    chatUser.put("Chat/" + currentUserID + "/" + recipientID,chatMap);
                    chatUser.put("Chat/" + recipientID + "/" + currentUserID,chatMap);

                    root.updateChildren(chatUser, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Toast.makeText(context,"There was an error: " + databaseError.toString(),Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageBox.getText().toString();
                if(!TextUtils.isEmpty(message)){
                    String currentUserRef = "Messages/" + currentUserID + "/" + recipientID;
                    String recipientRef = "Messages/" + recipientID + "/" + currentUserID;

                    DatabaseReference msgPush = root.child("Messages").child(currentUserID).child(recipientID).push();
                    String msgKey = msgPush.getKey();

                    Map messageMap = new HashMap();
                    messageMap.put("seen",false);
                    messageMap.put("type","text");
                    messageMap.put("time",ServerValue.TIMESTAMP);
                    messageMap.put("message",message);
                    messageMap.put("sender",currentUserID);

                    Map messageUserMap = new HashMap();
                    messageUserMap.put(currentUserRef + "/" + msgKey,messageMap);
                    messageUserMap.put(recipientRef + "/" + msgKey,messageMap);

                    root.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Toast.makeText(context,"There was an error: " + databaseError.toString(),Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    messageBox.setText("");
                }
            }
        });

        messageBox.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent imageIntent = new Intent();
                imageIntent.setType("image/*");
                imageIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(imageIntent,"Select an image"),PICK);

                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            final String currentUserRef = "Messages/" + currentUserID + "/" + recipientID;
            final String recipientRef = "Messages/" + recipientID + "/" + currentUserID;

            DatabaseReference msgPush = root.child("Messages").child(currentUserID).child(recipientID).push();
            final String msgKey = msgPush.getKey();

            StorageReference sRef = FirebaseStorage.getInstance().getReference().child("images").child(msgKey + ".jpg");
            sRef.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        String url = task.getResult().getDownloadUrl().toString();
                        Map messageMap = new HashMap();
                        messageMap.put("seen",false);
                        messageMap.put("type","image");
                        messageMap.put("time",ServerValue.TIMESTAMP);
                        messageMap.put("message",url);
                        messageMap.put("sender",currentUserID);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(currentUserRef + "/" + msgKey,messageMap);
                        messageUserMap.put(recipientRef + "/" + msgKey,messageMap);

                        root.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if(databaseError != null){
                                    Toast.makeText(context,"There was an error: " + databaseError.toString(),Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                        messageBox.setText("");
                    }
                }
            });
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loadMessages() {
        root.child("Messages").child(currentUserID).child(recipientID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messageList.add(message);
                adapter.notifyDataSetChanged();
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
    }

    @Override
    protected void onStart() {
        super.onStart();

        userDB.child(currentUserID).child("online").setValue(true);
    }

    @Override
    protected void onStop() {
        super.onStop();

        userDB.child(currentUserID).child("online").setValue(false);
    }
}
