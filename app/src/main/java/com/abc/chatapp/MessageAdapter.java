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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message>  mMessageList;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Message> mMessageList ){
        this.mMessageList = mMessageList;

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
    public void onBindViewHolder(MessageViewHolder viewHolder, int i){
        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();

        Message c = mMessageList.get(i);
        String from_user = c.getFrom();
        String msg_type = c.getType();

        viewHolder.messageText.setText(c.getMessage());

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
