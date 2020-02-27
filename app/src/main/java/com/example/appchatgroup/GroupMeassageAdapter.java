package com.example.appchatgroup;

import android.app.Notification;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

public class GroupMeassageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> groupMEassgeList;
    private FirebaseAuth mAuth;
    private DatabaseReference groupRef;

    public MessageAdapter (List<Messages> groupMEassgeList){
        this.groupMEassgeList = groupMEassgeList;
    }

    @NonNull
    @Override
    public MessageAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.MessageViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
