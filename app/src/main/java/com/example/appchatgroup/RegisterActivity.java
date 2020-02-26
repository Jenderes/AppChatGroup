package com.example.appchatgroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {
    private Button ButtonRegister;
    private EditText RegEmailText,RegPasswordText;
    private TextView AlreadyHaveText;

    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private ProgressDialog loadinBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        InitilizeFolder();

        AlreadyHaveText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToLoginctivity();
            }
        });

        ButtonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {
        String email = RegEmailText.getText().toString();
        String password = RegPasswordText.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter email...",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter password",Toast.LENGTH_SHORT).show();
        } else {
            loadinBar.setTitle("Creating new Account");
            loadinBar.setMessage("Please wait");
            loadinBar.setCanceledOnTouchOutside(true);
            loadinBar.show();

            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                String  devicetoken = FirebaseInstanceId.getInstance().getToken();
                                String currenUserID = mAuth.getCurrentUser().getUid();
                                RootRef.child("Users").child(currenUserID).setValue("");

                                RootRef.child("Users").child(currenUserID).child("device_token")
                                        .setValue(devicetoken);

                                Toast.makeText(RegisterActivity.this,"Account Created Successfully",Toast.LENGTH_SHORT);
                                loadinBar.dismiss();
                                mAuth.signOut();
                                sendUserToMainctivity();
                            }
                            else {
                                String messege = task.getException().toString();
                                Toast.makeText(RegisterActivity.this,"Error: "+ messege,Toast.LENGTH_SHORT);
                                loadinBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void InitilizeFolder() {
        ButtonRegister = (Button)findViewById(R.id.Register_button);
        RegEmailText = (EditText)findViewById(R.id.Register_email);
        RegPasswordText = (EditText)findViewById(R.id.Register_password);
        AlreadyHaveText = (TextView)findViewById(R.id.already_have_account_link);

        loadinBar = new ProgressDialog(this);
    }
    private void sendUserToLoginctivity() {
        Intent LoginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(LoginIntent);
    }
    private void sendUserToMainctivity() {
        Intent MainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
}
