package com.abc.chatapp;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
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
public class FriendsFragment extends Fragment {
    private RecyclerView mFriendList;

    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;

    private String current_user_id;

    private View mMainView;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendList = (RecyclerView)mMainView.findViewById(R.id.friend_list);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);

        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));

        return  mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Query query = FirebaseDatabase.getInstance().getReference().child("Friend").child(current_user_id);
        query.keepSynced(true);
        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(query, Friends.class).build();

        FirebaseRecyclerAdapter<Friends, FriendViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendViewHolder holder, int position, @NonNull Friends friend) {

                String list_user_id = getRef(position).getKey();
                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child("name").getValue().toString();
                        String image_thumb = dataSnapshot.child("thumb_image").getValue().toString();
                        if(dataSnapshot.hasChild("online")){

                        boolean user_online = (boolean)dataSnapshot.child("online").getValue();
                        holder.setOnline(user_online);
                        }

                        holder.setName(name);
                        holder.setThumbImage(image_thumb);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout, parent, false);
                return new FriendViewHolder(view);
            }
        };
        firebaseRecyclerAdapter.startListening();
        mFriendList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder{

        View mView;
        FriendViewHolder(View itemView){
            super(itemView);
            mView = itemView;

        }

        public void setDate(String date){
            TextView userNameView = (TextView) mView.findViewById(R.id.user_status);
            userNameView.setText(date);
        }

        public void setName(String name){
            TextView userNameView = (TextView) mView.findViewById(R.id.user_name);
            userNameView.setText(name);

        }

        public void setThumbImage(String url) {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.user_image);
            if (!url.equals("default")) {

                Picasso.get().load(url).into(image);
            }
        }

        public void setOnline(boolean online){
            ImageView onlineIcon = (ImageView) mView.findViewById(R.id.user_online_icon);
            if(online == true){
                onlineIcon.setVisibility(mView.VISIBLE);

            }else{
                onlineIcon.setVisibility(mView.INVISIBLE);
            }
        }


    }
}
