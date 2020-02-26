package com.example.appchatgroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTableLayout;
    private TabsAccesorAdapter mTabsAccesorAdapter;
    private FirebaseAuth mAuthMain;
    private DatabaseReference RootRef;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuthMain = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        mToolbar =(Toolbar)findViewById(R.id.name_page);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("ChatApp");
        mViewPager =(ViewPager)findViewById(R.id.main_tabs_page);

        mTabsAccesorAdapter = new TabsAccesorAdapter(getSupportFragmentManager());
        mTableLayout =(TabLayout)findViewById(R.id.main_tabs);
        mViewPager.setAdapter(mTabsAccesorAdapter);
        mTableLayout.setupWithViewPager(mViewPager);

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuthMain.getCurrentUser();
        if (currentUser == null){
            sendUserToLoginActivity();
        }
         else {
            updateUserStatus("online");

             VerifyUserExistance();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuthMain.getCurrentUser();
        if (currentUser != null){
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = mAuthMain.getCurrentUser();
        if (currentUser != null){
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistance() {
        String currentUserID = mAuthMain.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("name").exists()){
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                } else {
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

//    @Override
//    protected void onStop() {
//        mAuthMain.signOut();
//        super.onStop();
//    }

    private void sendUserToLoginActivity() {
        Intent LoginIntent = new Intent(MainActivity.this, LoginActivity.class);
        LoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginIntent);
    }
    private void sendUserToSettingsActivity() {
        Intent SettingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(SettingsIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_logout_options){
            updateUserStatus("offline");
            mAuthMain.signOut();
            sendUserToLoginActivity();
        }
        if (item.getItemId() == R.id.main_settings_options){
            sendUserToSettingsActivity();
        }
        if (item.getItemId() == R.id.main_friend_options){
            sendToFindFriendsActivity();
        }
        if (item.getItemId() == R.id.main_create_group_options){
            sendToCreateGroupActivity();
        }
        return true;
    }

    private void sendToFindFriendsActivity() {
        Intent findfriendactivity = new Intent(MainActivity.this, FindFriendActivity.class);
        startActivity(findfriendactivity);
    }

    private void sendToCreateGroupActivity(){
        Intent creategroupActivity = new Intent(MainActivity.this,CreateGroupActivity.class);
        startActivity(creategroupActivity);
    }
    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter Group Name : ");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g Club Coding");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String GroupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(GroupName)){
                    Toast.makeText(MainActivity.this, "Please write your group name...", Toast.LENGTH_SHORT).show();
                } else {
                    CreateNewGroup(GroupName);
                }
            }
        });
        builder.setNegativeButton("Cencel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }
    private void CreateNewGroup(final String groupName) {
        RootRef.child("Groups").child(groupName).setValue(" ")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this, groupName + " is Create successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void updateUserStatus(String state){
        String saveCurrentTime,saveCurrentDate;
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd MMM, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String,Object> OnlineStateMap = new HashMap<>();
        OnlineStateMap.put("time",saveCurrentTime);
        OnlineStateMap.put("date",saveCurrentDate);
        OnlineStateMap.put("state",state);

        currentUserID = mAuthMain.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(OnlineStateMap);
    }
}
