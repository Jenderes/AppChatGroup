package com.example.appchatgroup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GroupMeassageAdapter extends RecyclerView.Adapter<GroupMeassageAdapter.GroupMessageViewHolder> {
    private List<GroupMessages> groupMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public GroupMeassageAdapter(List<GroupMessages> groupMessagesList){
        this.groupMessagesList = groupMessagesList;
    }

    public class GroupMessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessageText,receiverMessageText;
        public ImageView receivedProfileImage;
        public GroupMessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = (TextView)itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView)itemView.findViewById(R.id.received_message_text);
            receivedProfileImage = (ImageView) itemView.findViewById(R.id.message_profile_image);
        }
    }

    @NonNull
    @Override
    public GroupMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout,parent,false);
        mAuth = FirebaseAuth.getInstance();
        return new GroupMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupMessageViewHolder holder, int position) {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        GroupMessages messages = groupMessagesList.get(position);
        String fromUserID = messages.getFrom();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")){
                    String receiverImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.ic_profile).into(holder.receivedProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(fromUserID.equals(messageSenderID)){
            holder.receiverMessageText.setVisibility(View.INVISIBLE);
            holder.receivedProfileImage.setVisibility(View.INVISIBLE);
            holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
            holder.senderMessageText.setText(messages.getMessage());
        } else {
            holder.senderMessageText.setVisibility(View.INVISIBLE);
            holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
            holder.receiverMessageText.setText(messages.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return groupMessagesList.size();
    }

}
