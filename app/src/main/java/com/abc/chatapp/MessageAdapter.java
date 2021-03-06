package com.abc.chatapp;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message>  mMessageList;
    private List<String> mMessagkey;
    private FirebaseAuth mAuth;
    private DatabaseReference mMessageDatabase;

    private String mChatUser;


    public MessageAdapter(List<Message> mMessageList, List<String> mMessagkey, String mChatUer ){
        this.mMessageList = mMessageList;
        this.mMessagkey = mMessagkey;
        this.mChatUser = mChatUer;

    }
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.messsage_single_layout, parent, false);
        return new MessageViewHolder(v);
    }



    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView messageText;
        public CircleImageView profileImage;
        public ImageView messageImage;

        public MessageViewHolder(View view){
            super(view);
            messageText = (TextView) view.findViewById(R.id.message_txt_layout);
            profileImage = (CircleImageView) view.findViewById(R.id.message_image_layout);
            messageImage = (ImageView) view.findViewById(R.id.message_imageView);


        }
    }
    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, final int i){
        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("message").child(current_user_id).child(mChatUser);
        //Query key = mMessageDatabase;
        Message c = mMessageList.get(i);
        String from_user = c.getFrom();
        final String msg_type = c.getType();

        viewHolder.messageText.setText(c.getMessage());
        viewHolder.messageText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                mMessageList.remove(i);
                notifyItemRemoved(i);
                mMessageDatabase.child(mMessagkey.get(i)).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                      //  Toast.makeText(viewHolder.messageText.getContext(), "Long-tapped on: "+mMessagkey.get(i), Toast.LENGTH_SHORT).show();
                    }
                });
               // String delete_key = getItem(i);
                //notifyItemRangeChanged(i, mMessageList.size());


                return false;
            }
        });

        if(msg_type.equals("text")){
            viewHolder.messageImage.setVisibility(View.INVISIBLE);
            viewHolder.messageText.setVisibility(View.VISIBLE);

            if(from_user.equals(current_user_id)){
                viewHolder.messageText.setBackgroundColor(Color.WHITE);
                viewHolder.messageText.setTextColor(Color.BLACK);

            }else{
                viewHolder.messageText.setBackgroundResource(R.drawable.message_txt_background);
                viewHolder.messageText.setTextColor(Color.WHITE);

            }

        }else{

            viewHolder.messageText.setVisibility(View.INVISIBLE);
            viewHolder.messageImage.setVisibility(View.VISIBLE);
            Picasso.get().load(c.getMessage()).placeholder(R.drawable.avatar).into(viewHolder.messageImage);
        }



        //viewHolder.timeText.setText(c.getTime());

    }
    @Override
    public int getItemCount(){
        return mMessageList.size();
    }


}
