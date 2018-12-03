package com.example.mugandaimo.square;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class Settings extends AppCompatActivity {

    Context context;
    Activity actvityContext;

    private Toolbar toolbar;

    private CircleImageView avatar;
    private TextView username,status;
    private ListView actions;

    private FirebaseAuth auth;
    private DatabaseReference root;
    private String uid;
    private StorageReference sRef;

    private ProgressDialog imageProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        context = this;
        actvityContext = this;

        toolbar = findViewById(R.id.settings_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        avatar = findViewById(R.id.avatar);
        username = findViewById(R.id.username);
        status = findViewById(R.id.status);
        actions = findViewById(R.id.actions);

        auth = FirebaseAuth.getInstance();
        root = FirebaseDatabase.getInstance().getReference();
        uid = auth.getCurrentUser().getUid();
        sRef = FirebaseStorage.getInstance().getReference();

        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent imageIntent = new Intent();
                imageIntent.setType("image/*");
                imageIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(imageIntent,"Select an image"),PICK);*/

                Crop.pickImage(actvityContext);
            }
        });

        final DatabaseReference userDB = root.child("Users").child(uid);
        userDB.keepSynced(true);
        userDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String usernameString = dataSnapshot.child("username").getValue().toString();
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

        final String[] actionArray = new String[]{"Change Username","Change Status","Account Info"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1,actionArray);
        actions.setAdapter(adapter);

        actions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.settings_dialog);
                final TextInputLayout detail = dialog.findViewById(R.id.detail);
                Button change = dialog.findViewById(R.id.change);

                dialog.show();

                String option = actionArray[i];

                switch (option){
                    case "Change Username":
                        change.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String usernameString = detail.getEditText().getText().toString();
                                userDB.child("username").setValue(usernameString);
                                dialog.dismiss();
                            }
                        });
                        break;
                    case "Change Status":
                        change.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String statusString = detail.getEditText().getText().toString();
                                userDB.child("status").setValue(statusString);
                                dialog.dismiss();
                            }
                        });
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK){
            beginCrop(data.getData());
        } else if(requestCode == Crop.REQUEST_CROP){
            try {
                handleCrop(resultCode,data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void beginCrop(Uri data) {
        Uri destination = Uri.fromFile(new File(getCacheDir(),"cropped"));
        Crop.of(data,destination).asSquare().start(actvityContext);
    }

    private void handleCrop(int resultCode, Intent data) throws IOException {
        imageProgress = new ProgressDialog(context);
        imageProgress.setTitle("Uploading Image");
        imageProgress.setMessage("Please wait...");
        imageProgress.setCanceledOnTouchOutside(false);
        imageProgress.show();

        if(resultCode == RESULT_OK){
            Uri croppedUri = Crop.getOutput(data);
            File imageFile = new File(croppedUri.getPath());

            final Bitmap compressedImage = new Compressor(context)
                    .setMaxHeight(200)
                    .setMaxWidth(200)
                    .setQuality(65)
                    .compressToBitmap(imageFile);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            final byte[] imageBytes = baos.toByteArray();

            //avatar.setImageURI(Crop.getOutput(data));
            StorageReference imageRef = sRef.child("avatar_images").child(uid + ".png");
            final StorageReference compressedImageRef = sRef.child("avatar_images").child("compressed").child(uid + ".png");

            imageRef.putFile(Crop.getOutput(data)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        final String imageLink = task.getResult().getDownloadUrl().toString();

                        UploadTask uploadTask = compressedImageRef.putBytes(imageBytes);
                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if(task.isSuccessful()){
                                    String compressedImageLink = task.getResult().getDownloadUrl().toString();

                                    Map imageMap = new HashMap<>();
                                    imageMap.put("image",imageLink);
                                    imageMap.put("compressed_image",compressedImageLink);



                                    DatabaseReference userRef = root.child("Users").child(uid);
                                    userRef.updateChildren(imageMap).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            if(task.isSuccessful()){
                                                imageProgress.dismiss();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    } else{
                        Toast.makeText(context,"Failed To Upload Image",Toast.LENGTH_SHORT).show();
                        imageProgress.dismiss();
                    }
                }
            });
        } else if(resultCode == Crop.RESULT_ERROR){
            Toast.makeText(context,Crop.getError(data).getMessage(),Toast.LENGTH_LONG).show();
        }
    }
}
