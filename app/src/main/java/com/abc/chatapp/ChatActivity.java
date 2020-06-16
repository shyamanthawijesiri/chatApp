package com.abc.chatapp;

import android.content.Context;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
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

    private ImageButton mChatAddbtn;
    private ImageButton mChatSendbtn;
    private EditText mChatmsg;

    private RecyclerView mMessageList;
    private SwipeRefreshLayout mSweepRefreshLayout;

    private final List<Message> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private static final int TOTAL_ITEM_TO_LOAD = 10;
    private int mCurrentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //send message
        mChatAddbtn = (ImageButton)findViewById(R.id.chat_add_btn);
        mChatSendbtn = (ImageButton)findViewById(R.id.chat_send_btn);
        mChatmsg = (EditText)findViewById(R.id.chat_msg_txt);


        mAdapter = new MessageAdapter(messagesList);
        mMessageList = (RecyclerView)findViewById(R.id.message_list);
        mSweepRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.message_sweep_layout);
        mLinearLayout = new LinearLayoutManager(this);

        mMessageList.setHasFixedSize(true);
        mMessageList.setLayoutManager(mLinearLayout);

        mMessageList.setAdapter(mAdapter);



        
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mToolBar = (Toolbar)findViewById(R.id.chat_app_bar);
        setSupportActionBar(mToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mChatUser = getIntent().getStringExtra("user_id");
        mChatUserName = getIntent().getStringExtra("user_name");

        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //getSupportActionBar().setTitle(mChatUserName);
        loadMessages();
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
        
        // send message
        
        mChatSendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        mSweepRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                messagesList.clear();
                loadMessages();
            }
        });


    }

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("message").child(mCurrentUserId).child(mChatUser);
        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEM_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message message = dataSnapshot.getValue(Message.class);
                messagesList.add(message);
                mAdapter.notifyDataSetChanged();

                mMessageList.scrollToPosition(messagesList.size() - 1);
                mSweepRefreshLayout.setRefreshing(false);
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

    private void sendMessage() {
        String msg = mChatmsg.getText().toString();

        if(!
                TextUtils.isEmpty(msg)){

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
