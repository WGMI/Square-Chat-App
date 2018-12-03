package com.example.mugandaimo.square;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    Context context;
    FirebaseAuth auth;
    DatabaseReference root;

    private Toolbar toolbar;
    private TextInputLayout email,username,password;
    private Button register;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        context = this;
        auth = FirebaseAuth.getInstance();
        root = FirebaseDatabase.getInstance().getReference();

        toolbar = findViewById(R.id.register_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        email = findViewById(R.id.et_email);
        username = findViewById(R.id.et_username);
        password = findViewById(R.id.et_password);
        register = findViewById(R.id.register);

        progressDialog = new ProgressDialog(context);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailString = email.getEditText().getText().toString();
                String usernameString = username.getEditText().getText().toString();
                String passwordString = password.getEditText().getText().toString();

                if(!TextUtils.isEmpty(emailString) || !TextUtils.isEmpty(usernameString) || !TextUtils.isEmpty(passwordString)){
                    progressDialog.setTitle("Creating Account");
                    progressDialog.setMessage("Please wait...");
                    progressDialog.show();
                    progressDialog.setCanceledOnTouchOutside(false);
                    registerUser(emailString,usernameString,passwordString);
                } else{
                    Toast.makeText(context,"Please fill all fields",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void registerUser(String emailString, final String usernameString, String passwordString) {
        auth.createUserWithEmailAndPassword(emailString,passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d("complete", "onComplete: ");
                if(task.isSuccessful()){
                    FirebaseUser user = auth.getCurrentUser();
                    String uid = user.getUid();

                    HashMap<String,String> userMap = new HashMap<>();
                    userMap.put("username",usernameString);
                    userMap.put("status","Join Square Today. Best App Ever.");
                    userMap.put("image","default");
                    userMap.put("compressed_image","default");

                    root.child("Users").child(uid).setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressDialog.dismiss();
                                Intent intent = new Intent(context, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else{
                                progressDialog.hide();
                                Toast.makeText(context, "Error inserting your details to the database.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else{
                    progressDialog.hide();
                    Toast.makeText(context, "Authentication failed.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
