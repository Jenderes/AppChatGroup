package com.example.appchatgroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ChoosePeopleForGroupActivity extends AppCompatActivity{
        private RecyclerView recyclerView;
        private Button buttonCreateGroup;
        private DatabaseReference GroupRef,UsersRef,CreateGroupRef;
        private FirebaseAuth mAuth;
        private String currentUserID;
        private ArrayList<String> listpositiongroup = new ArrayList<String>();
        private ArrayList<String> listgroupuser = new ArrayList<String>();
        private String listString = "";
        private String NameGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_people_for_group);
        buttonCreateGroup = (Button)findViewById(R.id.list_people_button);
        recyclerView = (RecyclerView)findViewById(R.id.list_people);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        GroupRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        CreateGroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        buttonCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listpositiongroup.size() != 0){
                    String currentID = mAuth.getCurrentUser().getUid();
                    NameGroup = getIntent().getExtras().get("group_name").toString();
                    CreateGroupRef.child(currentID).child("name").setValue(NameGroup);
                    CreateGroupRef.child(currentID).child("list");
                    CreateGroupRef.child(currentID).child("list").child(currentUserID).child("name").setValue("saved");
                    UsersRef.child(currentUserID).child("groups").child(currentID).child("status").setValue("saved");
                    for (int i = 0; i < listpositiongroup.size(); i++){
                        CreateGroupRef.child(currentID).child("list").child(listpositiongroup.get(i)).child("name").setValue("saved");
                        UsersRef.child(listpositiongroup.get(i)).child("groups").child(currentID).child("status").setValue("saved");
                    }
                    Intent choosegroup = new Intent(ChoosePeopleForGroupActivity.this,MainActivity.class);
                    startActivity(choosegroup);
                }else {
                    Toast.makeText(ChoosePeopleForGroupActivity.this, "Вы не выбрали друзей", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options
                = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(GroupRef,Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts,GroupViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts,GroupViewHolder>(options){
                    @Override
                    protected void onBindViewHolder(@NonNull final GroupViewHolder groupViewHolder, final int position, @NonNull Contacts contacts) {
                        String UserIDs =  getRef(position).getKey();
                        UsersRef.child(UserIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    if (dataSnapshot.hasChild("image")){
                                        String profileImage = dataSnapshot.child("image").getValue().toString();
                                        String profileName = dataSnapshot.child("name").getValue().toString();
                                        String profileStatus = dataSnapshot.child("status").getValue().toString();
                                        groupViewHolder.userName.setText(profileName);
                                        groupViewHolder.userStatus.setText(profileStatus);
                                        Picasso.get().load(profileImage).into(groupViewHolder.profileImage);
                                    } else {
                                    String profileName = dataSnapshot.child("name").getValue().toString();
                                    String profileStatus = dataSnapshot.child("status").getValue().toString();

                                    groupViewHolder.userName.setText(profileName);
                                    groupViewHolder.userStatus.setText(profileStatus);
                                }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        groupViewHolder.itemView.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View view) {
                                if (listpositiongroup.size() == 0){
                                    listpositiongroup.add(getRef(position).getKey());
                                    groupViewHolder.lineargroup.setBackgroundColor(Color.parseColor("#AEFEA5"));
                                    groupViewHolder.imageButton.setBackgroundColor(Color.parseColor("#AEFEA5"));
                                    groupViewHolder.imageButton.setVisibility(View.VISIBLE);
                                } else {
                                    for (int i = 0; i < listpositiongroup.size(); i++){
                                        if(listpositiongroup.get(i) == getRef(position).getKey()){
                                            listpositiongroup.remove(i);
                                            groupViewHolder.lineargroup.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                            groupViewHolder.imageButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                            groupViewHolder.imageButton.setVisibility(View.INVISIBLE);
                                            break;
                                        } else if(i == listpositiongroup.size()-1){
                                            listpositiongroup.add(getRef(position).getKey());
                                            groupViewHolder.lineargroup.setBackgroundColor(Color.parseColor("#AEFEA5"));
                                            groupViewHolder.imageButton.setBackgroundColor(Color.parseColor("#AEFEA5"));
                                            groupViewHolder.imageButton.setVisibility(View.VISIBLE);
                                            break;
                                        }
                                    }
                                }
                                listString = "";
                                for (String s : listpositiongroup)
                                {
                                    listString += s + ", ";
                                }
                                Toast.makeText(ChoosePeopleForGroupActivity.this, listString, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_group_display,parent,false);
                        GroupViewHolder viewHolder = new GroupViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }
    public static class GroupViewHolder extends RecyclerView.ViewHolder{
        TextView userName,userStatus;
        ImageView profileImage;
        ImageButton imageButton;
        LinearLayout lineargroup;
        public GroupViewHolder (@NonNull View itemView){
            super(itemView);
            lineargroup = itemView.findViewById(R.id.linear_group);
            userName = itemView.findViewById(R.id.profile_name_group);
            userStatus = itemView.findViewById(R.id.status_name_group);
            profileImage = itemView.findViewById(R.id.user_group_image);
            imageButton = itemView.findViewById(R.id.imagebutton_group);
        }

    }

}
