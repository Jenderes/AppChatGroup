package com.example.appchatgroup;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
public class ContactsFragment extends Fragment {
    private View ContactsView;
    private RecyclerView recyclerView;
    private DatabaseReference ContacsRef,UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);
        recyclerView = (RecyclerView)ContactsView.findViewById(R.id.contacts_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        ContacsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ContacsRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ContacsViewHolder> adapter  =
                new FirebaseRecyclerAdapter<Contacts, ContacsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ContacsViewHolder contacsViewHolder, int i, @NonNull Contacts contacts) {
                        String UserIDs =  getRef(i).getKey();
                        UsersRef.child(UserIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    if (dataSnapshot.child("userState").hasChild("state")){
                                        String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                        String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                        if (state.equals("online")){
                                            contacsViewHolder.CheckOnline.setVisibility(View.VISIBLE);
                                        }
                                        else if(state.equals("offline")){
                                            contacsViewHolder.CheckOnline.setVisibility(View.INVISIBLE);
                                        }
                                    } else {
                                        contacsViewHolder.CheckOnline.setVisibility(View.INVISIBLE);
                                    }

                                    if (dataSnapshot.hasChild("image")){
                                        String profileImage = dataSnapshot.child("image").getValue().toString();
                                        String profileName = dataSnapshot.child("name").getValue().toString();
                                        String profileStatus = dataSnapshot.child("status").getValue().toString();

                                        contacsViewHolder.userName.setText(profileName);
                                        contacsViewHolder.userStatus.setText(profileStatus);
                                        Picasso.get().load(profileImage).into(contacsViewHolder.userImage);

                                    } else {
                                        String profileName = dataSnapshot.child("name").getValue().toString();
                                        String profileStatus = dataSnapshot.child("status").getValue().toString();

                                        contacsViewHolder.userName.setText(profileName);
                                        contacsViewHolder.userStatus.setText(profileStatus);
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
                    public ContacsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                        ContacsViewHolder viewHolder = new ContacsViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static  class  ContacsViewHolder extends RecyclerView.ViewHolder {
        TextView userName,userStatus;
        ImageView userImage;
        ImageView CheckOnline;

        public ContacsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.profile_name);
            userStatus = itemView.findViewById(R.id.profile_status);
            userImage = itemView.findViewById(R.id.user_profile_image);
            CheckOnline = itemView.findViewById(R.id.off_on_status);
        }
    }
}
