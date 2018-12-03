package com.example.mugandaimo.square;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Muganda Imo on 11/20/2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private FirebaseAuth auth;
    private List<Message> messageList;

    public MessageAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_message,parent,false);
        return new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView msgAvatar;
        public TextView msgText;
        public RelativeLayout msgLayout;
        public ImageView image;

        public MessageViewHolder(View v){
            super(v);
            msgAvatar = v.findViewById(R.id.msg_avatar);
            msgText = v.findViewById(R.id.msg_text);
            msgLayout = v.findViewById(R.id.single_msg_layout);
            image = v.findViewById(R.id.image);
        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder holder, int position) {
        auth = FirebaseAuth.getInstance();
        String currentUserID = auth.getCurrentUser().getUid();
        Message m = messageList.get(position);
        String senderID = m.getSender();
        String type = m.getType();

        DatabaseReference imgRef = FirebaseDatabase.getInstance().getReference().child("Users").child(senderID);
        imgRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String avatarString = dataSnapshot.child("compressed_image").getValue().toString();
                Picasso.get().load(avatarString).placeholder(R.drawable.nif).into(holder.msgAvatar);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(type.equals("text")){
            holder.msgText.setText(m.getMessage());
            holder.image.setVisibility(View.INVISIBLE);
        } else{
            holder.msgText.setVisibility(View.INVISIBLE);
            holder.image.getLayoutParams().width = 600;
            Picasso.get().load(m.getMessage()).placeholder(R.drawable.crop__ic_cancel).into(holder.image);
        }

        if(senderID.equals(currentUserID)){
            holder.msgText.setBackgroundResource(R.drawable.msg_bg2);
            holder.msgText.setTextColor(Color.BLACK);
            holder.msgAvatar.setVisibility(View.INVISIBLE);
            holder.msgLayout.setGravity(Gravity.RIGHT);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}
