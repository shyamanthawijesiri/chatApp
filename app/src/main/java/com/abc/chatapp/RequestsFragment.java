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
public class RequestsFragment extends Fragment {

    //firbase database

    private DatabaseReference mRequestDatabase;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;

    private View mMainView;
    private RecyclerView mRequestList;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        mRequestList = (RecyclerView)mMainView.findViewById(R.id.request_list);
        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mRequestDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(current_user_id);



        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));
        return  mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();
        Query requestQuery = mRequestDatabase;

        FirebaseRecyclerOptions<Requests> options = new FirebaseRecyclerOptions.Builder<Requests>().setQuery(requestQuery, Requests.class).build();
        FirebaseRecyclerAdapter<Requests,RequestViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Requests, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Requests model) {
                final String list_user_id = getRef(position).getKey();

                mUserDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child("name").getValue().toString();
                        String image = dataSnapshot.child("thumb_image").getValue().toString();
                        holder.setName(name);
                        holder.setImage(image);
                         holder.view.setOnClickListener(new View.OnClickListener() {
                             @Override
                             public void onClick(View view) {
                                 Intent intent = new Intent(getContext(),ProfileActivity.class);
                                 intent.putExtra("user_id", list_user_id);
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
            public RequestsFragment.RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_single_layout, parent,false);
                return new RequestViewHolder(view);
            }
        };
            firebaseRecyclerAdapter.startListening();
        mRequestList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{
        View view;
        RequestViewHolder(View itemView){
            super(itemView);
            view = itemView;
        }
        public void setName(String name){
            TextView userName = (TextView)view.findViewById(R.id.user_name);
            userName.setText(name);
        }
        public void setImage(String url){
            CircleImageView image = (CircleImageView) view.findViewById(R.id.user_image);
            if (!url.equals("default")) {

                Picasso.get().load(url).into(image);
            }
        }
    }
}
