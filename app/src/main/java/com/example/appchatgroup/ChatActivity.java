package com.example.appchatgroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private String messageReceiverID,messageReceiverName,messageReceiverImage,messageSenderID;
    private TextView UserNameCustom,LastSeenCustom;
    private ImageView ImageCustom;
    private Toolbar ChatToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private ImageButton SendMessageButton;
    private EditText MessageInput;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessageList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_image").toString();
        InitilzeCustom();

        UserNameCustom.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.ic_profile).into(ImageCustom);

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });
    }

    private void InitilzeCustom() {
        ChatToolbar = (Toolbar)findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        UserNameCustom = (TextView)findViewById(R.id.custom_user_name);
        LastSeenCustom = (TextView)findViewById(R.id.custom_last_seen);
        ImageCustom = (ImageView)findViewById(R.id.custom_profile_image);

        SendMessageButton = (ImageButton)findViewById(R.id.button_input_messages);
        MessageInput = (EditText)findViewById(R.id.input_messages);

        messageAdapter = new MessageAdapter(messagesList);
        userMessageList = (RecyclerView)findViewById(R.id.private_messages_list_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messageAdapter);

    }

    private void DisplayLastSeen() {
        RootRef.child("Users").child(messageSenderID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("userState").hasChild("state")){
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online")){
                                LastSeenCustom.setText("Online");
                            }
                            else if(state.equals("offline")){
                                LastSeenCustom.setText("Last Seen: " + date + " " + time);
                            }
                        } else {
                            LastSeenCustom.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();
                        userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());
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

    private void SendMessage(){
        String messageText  = MessageInput.getText().toString();
        if (TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "first write your message", Toast.LENGTH_SHORT).show();
        } else {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiveRef = "Messages/" + messageReceiverID + "/" + messageSenderID;
            DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();
            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderID);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef +"/"+ messagePushID,messageTextBody);
            messageBodyDetails.put(messageReceiveRef +"/"+ messagePushID,messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Message is successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    MessageInput.setText("");
                }
            });
        }
    }
}
