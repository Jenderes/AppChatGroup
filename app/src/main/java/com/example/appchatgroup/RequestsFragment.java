package com.example.appchatgroup;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {
    private View RequstsFragmentView;
    private RecyclerView mRecyclerView;
    private FirebaseAuth mAuth;
    private String CurrentUserID;

    private DatabaseReference ChatRequestsRef,UsersRef,ContactsRef;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequstsFragmentView =  inflater.inflate(R.layout.fragment_requests, container, false);
        ChatRequestsRef = FirebaseDatabase.getInstance().getReference().child("chat requests");
        mAuth = FirebaseAuth.getInstance();
        CurrentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mRecyclerView = (RecyclerView)RequstsFragmentView.findViewById(R.id.chat_requests_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return RequstsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestsRef.child(CurrentUserID),Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestViewHolder requestViewHolder, int i, @NonNull Contacts contacts) {
                        final String list_user_id = getRef(i).getKey();
                        DatabaseReference getTypeRef = getRef(i).child("request_type").getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    String type = dataSnapshot.getValue().toString();
                                    if (type.equals("received")){
                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image")){
                                                    final String requestUserImage = dataSnapshot.child("image").getValue().toString();
                                                    Picasso.get().load(requestUserImage).into(requestViewHolder.userImage);
                                                }
                                                    final String requestUserName = dataSnapshot.child("name").getValue().toString();
                                                    final String requestUserStatus = dataSnapshot.child("status").getValue().toString();
                                                    requestViewHolder.userName.setText(requestUserName);
                                                    requestViewHolder.userStatus.setText("you have sent a request to " + requestUserName);

                                                requestViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        CharSequence options[] = new CharSequence[]
                                                                {
                                                                        "Cancel Chat Request"
                                                                };
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle("Already sent Request");
                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                if(i == 0){
                                                                    ContactsRef.child(CurrentUserID).child(list_user_id).child("Contacts")
                                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                ContactsRef.child(list_user_id).child(CurrentUserID).child("Contacts")
                                                                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()){
                                                                                            ChatRequestsRef.child(CurrentUserID).child(list_user_id)
                                                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if (task.isSuccessful()){
                                                                                                        ChatRequestsRef.child(list_user_id).child(CurrentUserID)
                                                                                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                if (task.isSuccessful()){
                                                                                                                    Toast.makeText(getContext(), "New Contact Added", Toast.LENGTH_SHORT).show();
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
                                                                    });
                                                                }
                                                                if(i == 0){
                                                                    ChatRequestsRef.child(CurrentUserID).child(list_user_id)
                                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                ChatRequestsRef.child(list_user_id).child(CurrentUserID)
                                                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()){
                                                                                            Toast.makeText(getContext(), "you have cancelled the chat request.", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    }
                                                                                });
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                        builder.show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                    else if (type.equals("sent")) {
                                        Button Accept_friends_req = requestViewHolder.itemView.findViewById(R.id.Accept_friends_req);
                                        Accept_friends_req.setText("Request Sent");

                                        requestViewHolder.itemView.findViewById(R.id.Cancel_friend_req).setVisibility(View.INVISIBLE);

                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_display_requests,parent,false);
                        RequestViewHolder holder = new RequestViewHolder(view);
                        return holder;
                    }
                };
        mRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }
    public static class RequestViewHolder extends RecyclerView.ViewHolder{
        TextView userName,userStatus;
        ImageView userImage;
        Button AcceptButton,CancelButtton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = (TextView)itemView.findViewById(R.id.profile_name_req);
            userStatus = (TextView)itemView.findViewById(R.id.profile_status_req);
            userImage = (ImageView)itemView.findViewById(R.id.user_profile_image_req);
            AcceptButton = (Button)itemView.findViewById(R.id.Accept_friends_req);
            CancelButtton = (Button)itemView.findViewById(R.id.Cancel_friend_req);
        }
    }
}
