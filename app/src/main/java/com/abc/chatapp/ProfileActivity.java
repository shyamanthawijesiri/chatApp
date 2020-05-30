package com.abc.chatapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ProfileActivity extends AppCompatActivity {

    private TextView mDisplayName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        String id = getIntent().getStringExtra("user_id");
        mDisplayName = (TextView) findViewById(R.id.profile_displayName);
        mDisplayName.setText(id);
    }
}
