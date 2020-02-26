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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuthLogin;
    private Button ButtonLogin,ButtonPhone;
    private EditText EmailText,PasswordText;
    private TextView ForgPasswText,NewAccText;
    private ProgressDialog loadinBar;
    private DatabaseReference UsersRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuthLogin = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        InitilizeFolder();

        NewAccText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToRegisterctivity();
            }
        });

        ButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowUserToLogin();
            }
        });
    }

    private void AllowUserToLogin() {
        String email = EmailText.getText().toString();
        String password = PasswordText.getText().toString();

        if (TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter email...",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter password",Toast.LENGTH_SHORT).show();
        } else {
            loadinBar.setTitle("Sign In");
            loadinBar.setMessage("Please wait.... ");
            loadinBar.setCanceledOnTouchOutside(true);
            loadinBar.show();

            mAuthLogin.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                String currentUserID = mAuthLogin.getCurrentUser().getUid();
                                String  devicetoken = FirebaseInstanceId.getInstance().getToken();

                                UsersRef.child(currentUserID).child("device_token")
                                        .setValue(devicetoken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    sendUserToMainctivity();
                                                    Toast.makeText(LoginActivity.this,"Login is successfully",Toast.LENGTH_SHORT).show();
                                                    loadinBar.dismiss();
                                                }
                                            }
                                        });
                            }  else {
                                String messege = task.getException().toString();
                                Toast.makeText(LoginActivity.this,"Error: "+ messege,Toast.LENGTH_SHORT);
                                loadinBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void InitilizeFolder() {
        ButtonLogin = (Button)findViewById(R.id.Login_button);
        EmailText = (EditText)findViewById(R.id.Login_email);
        PasswordText = (EditText)findViewById(R.id.Login_password);
        ForgPasswText = (TextView)findViewById(R.id.forget_password_link);
        NewAccText = (TextView)findViewById(R.id.need_new_account_link);
        loadinBar = new ProgressDialog(this);
    }


    private void sendUserToMainctivity() {
        Intent MainIntent = new Intent(LoginActivity.this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
    private void sendUserToRegisterctivity() {
        Intent RegisterIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(RegisterIntent);
    }
}
