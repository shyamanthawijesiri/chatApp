package com.abc.chatapp;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {


    //firebase database
    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUserDatabase;

    //firebase Auth
    private FirebaseAuth mAuth;


    private RecyclerView mChatList;
    private View mMainView;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_chat, container, false);
        mChatList = (RecyclerView)mMainView.findViewById(R.id.chat_list);

        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();
        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(current_user_id);
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("message").child(current_user_id);
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mChatList.setHasFixedSize(true);
        mChatList.setLayoutManager(new LinearLayoutManager(getContext()));
        return mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();

        Query conversationQuery = mConvDatabase.orderByChild("timestamp");
        FirebaseRecyclerOptions<Conv> options = new FirebaseRecyclerOptions.Builder<Conv>().setQuery(conversationQuery, Conv.class).build();
        FirebaseRecyclerAdapter<Conv, ConvViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ConvViewHolder holder, int position, @NonNull final Conv conv) {
                final String list_user_id = getRef(position).getKey();


                Query lastMessage = mMessageDatabase.child(list_user_id).limitToLast(1);//get last msg

                lastMessage.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        String msg = dataSnapshot.child("message").getValue().toString();
                        holder.setMsg(msg, conv.isSeen());

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

                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String name = dataSnapshot.child("name").getValue().toString();
                        String thumb_img = dataSnapshot.child("thumb_image").getValue().toString();
                        holder.setName(name);
                        holder.setImage(thumb_img);

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(getContext(), ChatActivity.class);
                                intent.putExtra("user_id",list_user_id);
                                intent.putExtra("user_name",name);
                                startActivity(intent);
                            }
                        });
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ConvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout, parent, false);
                return new ConvViewHolder(view);
            }
        };
        firebaseRecyclerAdapter.startListening();
        mChatList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class ConvViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ConvViewHolder(View itemView){
            super(itemView);
            mView = itemView;
        }

        public void setMsg(String msg, boolean isSeen){
            TextView lMsg = (TextView)mView.findViewById(R.id.user_status);
            lMsg.setText(msg);

            if(!isSeen){
                lMsg.setTypeface(lMsg.getTypeface(), Typeface.BOLD);
            }else{
                lMsg.setTypeface(lMsg.getTypeface(), Typeface.NORMAL);
            }
        }

        public void setName(String name){
            TextView userName = (TextView)mView.findViewById(R.id.user_name);
            userName.setText(name);
        }

        public void setImage(String url){
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.user_image);
            if (!url.equals("default")) {

                Picasso.get().load(url).into(image);
            }
        }
    }
}
