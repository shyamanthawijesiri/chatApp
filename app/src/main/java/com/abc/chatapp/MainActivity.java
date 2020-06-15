package com.abc.chatapp;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mUserRef;
    private android.support.v7.widget.Toolbar mToolbar;
    private ViewPager mViewPager;

    private TabLayout mTabLayout;

    private SectionPagerAdapter mSectionPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mCurrentUser = mAuth.getCurrentUser();

        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Chat App");

        if(mAuth.getCurrentUser() != null){
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }

        //tabs
        mViewPager = (ViewPager)findViewById(R.id.main_tabpager);
        mSectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionPagerAdapter);

        mTabLayout = (TabLayout)findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        if(mCurrentUser == null){
            sendToStart();
        }else{
            mUserRef.child("online").setValue("true");
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mCurrentUser != null){

        mUserRef.child("online").setValue(ServerValue.TIMESTAMP);

        }
    }

    private void sendToStart() {

            Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
            startActivity(startIntent);
            finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout_btn){
            FirebaseAuth.getInstance().signOut();
            sendToStart();

        }
        if(item.getItemId() == R.id.main_settings_btn){
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        if(item.getItemId() == R.id.main_all_btn){
            Intent intent = new Intent(MainActivity.this, UserActivity.class);
            startActivity(intent);
        }
        return  true;
    }
}
