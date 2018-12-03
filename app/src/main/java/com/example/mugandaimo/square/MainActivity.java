package com.example.mugandaimo.square;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    Context context;

    private FirebaseAuth auth;
    private DatabaseReference userDB;

    private Toolbar toolbar;

    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser() != null){
            userDB = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());
        }


        toolbar = findViewById(R.id.main_bar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Square");

        viewPager = findViewById(R.id.main_view_pager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(adapter);

        tabLayout = findViewById(R.id.main_activity_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();

        sendToStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(auth.getCurrentUser() != null){
            userDB.child("online").setValue(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout :
                userDB.child("online").setValue(false);
                auth.signOut();
                sendToStart();
                break;
            case R.id.settings :
                startActivity(new Intent(context,Settings.class));
                break;
            case R.id.users :
                startActivity(new Intent(context,Users.class));
                break;
        }

        return true;
    }

    private void sendToStart() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser == null){
            startActivity(new Intent(context,StartActivity.class));
            finish();
        } else{
            userDB.child("online").setValue(true);
        }
    }
}
