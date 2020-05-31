package com.abc.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;

    private TextView mStatus;
    private TextView mFriendCount;
    private TextView mDisplayName;

    private Button mSendRequest;

    private ProgressDialog mProgressDialog;

  // Firebase
    private DatabaseReference mProfileDatabase;
    private DatabaseReference mFriendRequestDatabase;

    private FirebaseUser mCurrentUser;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        final String id = getIntent().getStringExtra("user_id");

        mProfileDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mDisplayName = (TextView) findViewById(R.id.profile_displayName);
        mStatus = (TextView)findViewById(R.id.profile_status);
        mFriendCount = (TextView)findViewById(R.id.profile_firendsCount);
        mProfileImage = (ImageView)findViewById(R.id.profile_image);
        mSendRequest = (Button)findViewById(R.id.profile_sendRequest_btn);

        mCurrent_state = "not_friend";

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading user data");
        mProgressDialog.setMessage("please wait until finished");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();


        mProfileDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mDisplayName.setText(display_name);
                mStatus.setText(status);
                Picasso.get().load(image).into(mProfileImage);

                mProgressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCurrent_state.equals("not_friend")){
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(id).child("request_type")
                            .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                mFriendRequestDatabase.child(id).child(mCurrentUser.getUid()).child("request_type")
                                        .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(ProfileActivity.this,"sending request successfully",Toast.LENGTH_LONG).show();
                                    }
                                });
                            }else {
                                Toast.makeText(ProfileActivity.this,"sending request is failed",Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

    }
}
