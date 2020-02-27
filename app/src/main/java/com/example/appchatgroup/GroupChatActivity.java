package com.example.appchatgroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ImageButton SendMessageButton;
    private EditText TextSendMessage;
    private TextView TextMessage;
    private ScrollView mScrollView;

    private FirebaseAuth mAuth;
    private String currentGroupID,currentUserID,currentUserName,currentDate,currentTime;
    private DatabaseReference usersRef,GroupNameRef,GroupMessageKeyRef;
    private String nameGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        currentGroupID = getIntent().getExtras().get("visit_group_id").toString();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupID).child("chats");
        nameGroup = getIntent().getExtras().get("visit_group_name").toString();
        IntializeFields();
        GetUserInfo();
        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveMessageInfoToDatabase();
                TextSendMessage.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    DisplayMEssages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()){
                    DisplayMEssages(dataSnapshot);
                }
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


    private void IntializeFields() {
        mToolbar = (Toolbar)findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(nameGroup);

        SendMessageButton = (ImageButton)findViewById(R.id.send_message_button);
        TextSendMessage = (EditText)findViewById(R.id.input_group_message);
        TextMessage = (TextView)findViewById(R.id.group_chat_text_display);
        mScrollView = (ScrollView)findViewById(R.id.my_scroll_view);
    }

    private void GetUserInfo() {
        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SaveMessageInfoToDatabase() {
        String message = TextSendMessage.getText().toString();
        String messageKEY = GroupNameRef.push().getKey();
        if (TextUtils.isEmpty(message)){
            Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show();
        } else {
            Calendar ccalForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd MMM, yyyy");
            currentDate = currentDateFormat.format(ccalForDate.getTime());

            Calendar ccalForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("HH:mm");
            currentTime = currentTimeFormat.format(ccalForTime.getTime());

            HashMap<String,Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);

            GroupMessageKeyRef = GroupNameRef.child(messageKEY);

            HashMap<String,Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);
            GroupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }

    private void DisplayMEssages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()){
            
            String chatDate = (String)((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String)((DataSnapshot)iterator.next()).getValue();
            String chatName = (String)((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String)((DataSnapshot)iterator.next()).getValue();

            TextMessage.append(chatName + ":\n" + chatMessage + "\n" + chatTime + "   "+ chatDate+ "\n\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}
