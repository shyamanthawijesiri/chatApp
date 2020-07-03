package com.abc.chatapp;

import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private String mChatUserName;
    private Toolbar mToolBar;

    private TextView mDisplayName;
    private TextView mLastSeen;
    private CircleImageView mImage;

    private String mCurrentUserId;

    private DatabaseReference mRootRef;
    private StorageReference mImageStorage;

    private ImageButton mChatAddbtn;
    private ImageButton mChatSendbtn;
    private EditText mChatmsg;

    private RecyclerView mMessageList;
    private SwipeRefreshLayout mSweepRefreshLayout;

    private  List<Message> messagesList = new ArrayList<>();
    private List<String>keyList = new ArrayList<String>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private static final int TOTAL_ITEM_TO_LOAD = 10;
    private int mCurrentPage = 1;
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";
    private String mFirstKey ;
    private int flag = 1;

    private static final int GELLARY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //send message
        mChatAddbtn = (ImageButton)findViewById(R.id.chat_add_btn);
        mChatSendbtn = (ImageButton)findViewById(R.id.chat_send_btn);
        mChatmsg = (EditText)findViewById(R.id.chat_msg_txt);

        mChatUser = getIntent().getStringExtra("user_id");
        mChatUserName = getIntent().getStringExtra("user_name");
        mAdapter = new MessageAdapter(messagesList,keyList, mChatUser);
        mMessageList = (RecyclerView)findViewById(R.id.message_list);
        mSweepRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.message_sweep_layout);
        mLinearLayout = new LinearLayoutManager(this);

        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearLayout);

        mMessageList.setAdapter(mAdapter);



        
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mToolBar = (Toolbar)findViewById(R.id.chat_app_bar);
        setSupportActionBar(mToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);



        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //getSupportActionBar().setTitle(mChatUserName);
        messagesList.clear();
        //loadMessages();
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        mDisplayName =(TextView)findViewById(R.id.charBar_display_name);
        mLastSeen = (TextView)findViewById(R.id.chatBar_last_seen);
        mImage = (CircleImageView)findViewById(R.id.chat_bar_image);

        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String image = dataSnapshot.child("image").getValue().toString();
                String online = dataSnapshot.child("online").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                mDisplayName.setText(name);
                if (!image.equals("default")){

                    Picasso.get().load(image).into(mImage);
                }
                if(online.equals("true")){
                    mLastSeen.setText("online");
                }else{
                    mLastSeen.setText(online);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mChatUser)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Log.d("Chat_log", databaseError.getMessage());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mRootRef.child("message").child(mCurrentUserId).child(mChatUser).orderByKey().limitToFirst(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                mFirstKey = dataSnapshot.getKey();
                Log.d("All msg","firstKey "+ mFirstKey);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        
        // send message
        
        mChatSendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        mChatAddbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"),GELLARY_PICK);
            }
        });
        mSweepRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               // mCurrentPage++;
                itemPos = 0;
               loadMoreMessages();
//                messagesList.clear();
//                loadMessages();

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        loadMessages();
    }

    //send image


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GELLARY_PICK && resultCode == RESULT_OK){
            Uri imageuri = data.getData();

            final String current_user_ref ="message/" + mCurrentUserId + "/" +mChatUser;
            final String chat_user_ref = "message/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_msg_push = mRootRef.child("message").child(mCurrentUserId).child(mChatUser).push();
            final String push_id = user_msg_push.getKey();

            final StorageReference file_path = mImageStorage.child("message_images").child(push_id + ".jpg");


            file_path.putFile(imageuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                    firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String image_url = uri.toString();

                            Map msgMap = new HashMap();
                            msgMap.put("message", image_url);
                            msgMap.put("seen", false);
                            msgMap.put("type", "image");
                            msgMap.put("time", ServerValue.TIMESTAMP);
                            msgMap.put("from", mCurrentUserId);

                            Map msgUserMap = new HashMap();
                            msgUserMap.put(current_user_ref + "/" + push_id, msgMap);
                            msgUserMap.put(chat_user_ref + "/" + push_id, msgMap);

                            mRootRef.updateChildren(msgUserMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        if(databaseError != null){
                                            Log.d("CHAT_LOG",databaseError.getMessage());
                                        }
                                    }
                                });
                        }
                    });
                }
            });
        }
    }

    private void loadMoreMessages() {
        DatabaseReference messageRef = mRootRef.child("message").child(mCurrentUserId).child(mChatUser);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(TOTAL_ITEM_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                String messageKey = dataSnapshot.getKey();


//
                if(!mPrevKey.equals(messageKey)){
                    keyList.add(itemPos,messageKey);
                    messagesList.add(itemPos++, message);
                }else{
                    mPrevKey = mLastKey;
                }
//
                if(itemPos == 1){
                    mLastKey = messageKey;
                }
                Log.d("All msg", "mLastkey" + mLastKey);
                if(messageKey.equals(mFirstKey)){

                    flag =0;
                    Log.d("All msg","message_poss_10 "+ itemPos);
                    mSweepRefreshLayout.setRefreshing(false);
                    return;
                }


                Log.d("TOTALKEYS", "Last key:" + mLastKey + " | Prev Key:" + mPrevKey + " | Message key:" + messageKey);

                mAdapter.notifyDataSetChanged();
                mSweepRefreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(10,0);
                Log.d("All msg","message list "+ Arrays.toString(messagesList.toArray()) + "item postitom"+ itemPos);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("message").child(mCurrentUserId).child(mChatUser);
        Query messageQuery;
        if(flag == 0){
            messageQuery = messageRef.limitToLast(1);
        }else {
            messageQuery = messageRef.limitToLast(5);
        }
            messageQuery.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Message message = dataSnapshot.getValue(Message.class);
                    itemPos++;

                    if (itemPos == 1 && flag==1) {
                        String messageKey = dataSnapshot.getKey();
                        mLastKey = messageKey;
                        mPrevKey = messageKey;
                        Log.d("All msg", "message_item_poss " + Arrays.toString(messagesList.toArray()) + "item postitom" + itemPos);
                    }
                    Log.d("All msg", "mLastkey" + mLastKey);

                    messagesList.add(message);
                    keyList.add(dataSnapshot.getKey());
                    mAdapter.notifyDataSetChanged();


                    mMessageList.scrollToPosition(messagesList.size() - 1);
                    mSweepRefreshLayout.setRefreshing(false);


                    Log.d("All msg", "message list " + Arrays.toString(messagesList.toArray()) + "item postitom" + itemPos);
                    Toast.makeText(ChatActivity.this, "add child: "+flag, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    //Toast.makeText(ChatActivity.this, "change child: ", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    Toast.makeText(ChatActivity.this, "remove child: ", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

    }

    private void sendMessage() {
        String msg = mChatmsg.getText().toString();

        if(!TextUtils.isEmpty(msg)){

            String current_user_ref ="message/" + mCurrentUserId + "/" +mChatUser;
            String chat_user_ref = "message/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_msg_push = mRootRef.child("message").child(mCurrentUserId).child(mChatUser).push();

            String push_id = user_msg_push.getKey();

            Map msgMap = new HashMap();
            msgMap.put("message", msg);
            msgMap.put("seen", false);
            msgMap.put("type", "text");
            msgMap.put("time", ServerValue.TIMESTAMP);
            msgMap.put("from", mCurrentUserId);

            Map msgUserMap = new HashMap();
            msgUserMap.put(current_user_ref + "/" + push_id, msgMap);
            msgUserMap.put(chat_user_ref + "/" + push_id, msgMap);

            mChatmsg.setText("");

            mRootRef.updateChildren(msgUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError != null){
                            Log.d("CHAT_LOG",databaseError.getMessage());
                        }
                }
            });
        }
    }
}
