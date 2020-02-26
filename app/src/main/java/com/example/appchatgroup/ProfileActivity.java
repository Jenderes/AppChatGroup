package com.example.appchatgroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {
    private String receiverUserID,senderUserID, Current_state;
    private ImageView ProfileImage;
    private TextView userProfileName,userProfileStatus;
    private Button SendUserMessangeButton,DeclineChatRequest;

    private DatabaseReference UserRef,chatRequestRef,ContactsRef,NotificationRef;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("chat requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        senderUserID = mAuth.getCurrentUser().getUid();

        ProfileImage = (ImageView)findViewById(R.id.Profile_image_photo);
        userProfileName = (TextView)findViewById(R.id.username_profile);
        userProfileStatus = (TextView)findViewById(R.id.userstatus_profile);
        SendUserMessangeButton = (Button)findViewById(R.id.button_send_massage);
        DeclineChatRequest = (Button) findViewById(R.id.decline_message_request_button);
        Current_state = "new";

        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))){
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    Picasso.get().load(userImage).placeholder(R.drawable.ic_profile).into(ProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                } else {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequest() {
        chatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(receiverUserID)){
                            String request_type =dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                            if (request_type.equals("sent")){
                                Current_state = "request_sent";
                                SendUserMessangeButton.setText("Cancel chat request");
                            } else if(request_type.equals("received")) {
                                Current_state = "request_received";
                                SendUserMessangeButton.setText("Accept Chat Request");

                                DeclineChatRequest.setVisibility(View.VISIBLE);
                                DeclineChatRequest.setEnabled(true);
                                DeclineChatRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        } else {
                            ContactsRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(receiverUserID)){
                                                Current_state = "friends";
                                                SendUserMessangeButton.setText("Remove this Contacts");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if(!senderUserID.equals(receiverUserID)){
            SendUserMessangeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SendUserMessangeButton.setEnabled(false);
                    if(Current_state.equals("new")){
                        SendChatRequest();
                    }
                    if(Current_state.equals("request_sent")){
                        CancelChatRequest();
                    }
                    if(Current_state.equals("request_received")){
                        AcceptChatRequest();
                    }
                    if(Current_state.equals("friends")){
                        RemoveCpecificContact();
                    }
                }
            });
        } else {
            SendUserMessangeButton.setVisibility(View.INVISIBLE);

        }
    }

    private void RemoveCpecificContact() {
        ContactsRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            ContactsRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                SendUserMessangeButton.setEnabled(true);
                                                Current_state = "new";
                                                SendUserMessangeButton.setText("Send Message");
                                                DeclineChatRequest.setVisibility(View.INVISIBLE);
                                                DeclineChatRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {
        ContactsRef.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            chatRequestRef.child(senderUserID).child(receiverUserID)
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        SendUserMessangeButton.setEnabled(true);
                                        Current_state = "friends";
                                        SendUserMessangeButton.setText("Remove this contacts");

                                        DeclineChatRequest.setVisibility(View.INVISIBLE);
                                        DeclineChatRequest.setEnabled(false);

                                    }
                                }
                            });
                        }
                    }
                });
    }

    private void CancelChatRequest() {
        chatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                SendUserMessangeButton.setEnabled(true);
                                                Current_state = "new";
                                                SendUserMessangeButton.setText("Send Message");
                                                DeclineChatRequest.setVisibility(View.INVISIBLE);
                                                DeclineChatRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendChatRequest() {
        chatRequestRef.child(senderUserID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                HashMap<String,String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from",senderUserID);
                                                chatNotificationMap.put("type","request");
                                                NotificationRef.child(receiverUserID).push()
                                                        .setValue(chatNotificationMap)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    SendUserMessangeButton.setEnabled(true);
                                                                    Current_state = "request_sent";
                                                                    SendUserMessangeButton.setText("Cancel chat Request");
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
