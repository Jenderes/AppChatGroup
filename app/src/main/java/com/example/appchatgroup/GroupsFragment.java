package com.example.appchatgroup;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    private View groupfragmentView;

    private RecyclerView recyclerGroup;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> listGroups = new ArrayList<String>();
    private DatabaseReference GroupRef,UserGroupRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        groupfragmentView = inflater.inflate(R.layout.fragment_groups, container, false);
        recyclerGroup = (RecyclerView)groupfragmentView.findViewById(R.id.list_group_view);
        recyclerGroup.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        GroupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        UserGroupRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID).child("groups");
        return groupfragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(UserGroupRef,Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts,GroupListViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, GroupListViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final GroupListViewHolder groupListViewHolder, final int i, @NonNull Contacts contacts) {
                        String GroupIDs =  getRef(i).getKey();
                        GroupRef.child(GroupIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    String groupname = dataSnapshot.child("name").getValue().toString();
                                    listGroups.add(groupname);
                                    groupListViewHolder.GroupName.setText(groupname);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        groupListViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String GroupIDs =  getRef(i).getKey();
                                Intent groupIntent = new Intent(getContext(),GroupChatActivity.class);
                                groupIntent.putExtra("visit_group_id",GroupIDs);
                                groupIntent.putExtra("visit_group_name",listGroups.get(i));
                                startActivity(groupIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public GroupListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_element_layout,parent,false);
                        GroupListViewHolder viewHolder = new GroupListViewHolder(view);
                        return viewHolder;
                    }
                };
        recyclerGroup.setAdapter(adapter);
        adapter.startListening();
    }

    public static class GroupListViewHolder extends RecyclerView.ViewHolder {
        TextView GroupName;
        public GroupListViewHolder (View itemView){
            super(itemView);
            GroupName = itemView.findViewById(R.id.name_list_group);
        }
    }
}
