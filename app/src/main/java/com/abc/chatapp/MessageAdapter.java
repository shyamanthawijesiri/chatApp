package com.abc.chatapp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message>  mMessageList;

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

        public MessageViewHolder(View view){
            super(view);
            messageText = (TextView) view.findViewById(R.id.message_txt_layout);
            profileImage = (CircleImageView) view.findViewById(R.id.message_image_layout);
        }
    }
    @Override
    public void onBindViewHolder(MessageViewHolder viewHolder, int i){
        Message c = mMessageList.get(i);
        viewHolder.messageText.setText(c.getMessage());
        //viewHolder.timeText.setText(c.getTime());

    }
    @Override
    public int getItemCount(){
        return mMessageList.size();
    }
}
