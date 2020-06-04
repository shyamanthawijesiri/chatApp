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

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;

    private TextView mStatus;
    private TextView mFriendCount;
    private TextView mDisplayName;

    private Button mSendRequest;
    private Button mDeclineRequest;

    private ProgressDialog mProgressDialog;

  // Firebase
    private DatabaseReference mProfileDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;

    private FirebaseUser mCurrentUser;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        final String id = getIntent().getStringExtra("user_id");

        mProfileDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(id);
        mFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friend");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mDisplayName = (TextView) findViewById(R.id.profile_displayName);
        mStatus = (TextView)findViewById(R.id.profile_status);
        mFriendCount = (TextView)findViewById(R.id.profile_firendsCount);
        mProfileImage = (ImageView)findViewById(R.id.profile_image);
        mSendRequest = (Button)findViewById(R.id.profile_sendRequest_btn);
        mDeclineRequest = (Button)findViewById(R.id.profile_decline_request_btn);

        mDeclineRequest.setVisibility(View.INVISIBLE);
        mDeclineRequest.setEnabled(false);


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

                // ----------------------Friend list / request feature----------------------
                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(id)){
                            String req_type = dataSnapshot.child(id).child("request_type").getValue().toString();
                            if(req_type.equals("received")){
                                mCurrent_state= "request_received";
                                mSendRequest.setText("Accept friend request");
                                mDeclineRequest.setVisibility(View.VISIBLE);
                                mDeclineRequest.setEnabled(true);
                            }else if(req_type.equals("sent")){
                                mCurrent_state= "request_sent";
                                mSendRequest.setText("Cancel friend request");
                            }
                        }else{
                            mFriendDatabase.child(mCurrentUser.getUid()).child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(id)){
                                        mCurrent_state="friends";
                                        mSendRequest.setText("Unfriend");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

                mSendRequest.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // -----------------send request--------------------------
                        mDeclineRequest.setVisibility(View.INVISIBLE);
                        mDeclineRequest.setEnabled(false);
                            mSendRequest.setEnabled(false);
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

                                                HashMap<String, String> notification = new HashMap<>();
                                                notification.put("from", mCurrentUser.getUid());
                                                notification.put("type","request");

                                                mNotificationDatabase.child(id).push().setValue(notification).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                mCurrent_state= "request_sent";
                                                mSendRequest.setText("cancel friend request");
                                                mSendRequest.setEnabled(true);
                                                Toast.makeText(ProfileActivity.this,"sending request successfully",Toast.LENGTH_LONG).show();
                                                    }
                                                });

                                            }
                                        });
                                    }else {
                                        Toast.makeText(ProfileActivity.this,"sending request is failed",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }

                        if(mCurrent_state.equals("request_sent")){
                            mFriendRequestDatabase.child(mCurrentUser.getUid()).child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendRequestDatabase.child(id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mSendRequest.setEnabled(true);
                                            mCurrent_state="not_friend";
                                            mSendRequest.setText("Send Friend Request");
                                        }
                                    });
                                }
                            });
                        }


                        //--------------------------- request received------------------------------------------------
                        if(mCurrent_state.equals("request_received")){
                            final String current_date = DateFormat.getDateInstance().format(new Date());
                            mFriendDatabase.child(mCurrentUser.getUid()).child(id).setValue(current_date)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mFriendDatabase.child(id).child(mCurrentUser.getUid()).setValue(current_date)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mFriendRequestDatabase.child(mCurrentUser.getUid()).child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    mFriendRequestDatabase.child(id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            mSendRequest.setEnabled(true);
                                                                            mCurrent_state="friends";
                                                                            mSendRequest.setText("Unfriend");
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    });
                                        }
                                    });
                        }
                    }
                });



    }
}
