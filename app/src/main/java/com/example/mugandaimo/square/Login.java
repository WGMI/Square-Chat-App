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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    Context context;

    private FirebaseAuth auth;

    private Toolbar toolbar;
    private TextInputLayout email,password;
    private Button login;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;

        auth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.register_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        login = findViewById(R.id.login);

        progressDialog = new ProgressDialog(context);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailString = email.getEditText().getText().toString();
                String passwordString = password.getEditText().getText().toString();

                if(!TextUtils.isEmpty(emailString) || !TextUtils.isEmpty(passwordString)){
                    progressDialog.setTitle("Signing In");
                    progressDialog.setMessage("Please wait...");
                    progressDialog.show();
                    progressDialog.setCanceledOnTouchOutside(false);
                    loginUser(emailString,passwordString);
                } else{
                    Toast.makeText(context,"Please fill all fields",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loginUser(String emailString, String passwordString) {
        auth.signInWithEmailAndPassword(emailString,passwordString).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = auth.getCurrentUser();
                    Intent intent = new Intent(context,MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else{
                    Toast.makeText(context,"Signing in failed. Please try again.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
