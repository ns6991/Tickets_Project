package com.example.tickets_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Login_Activity extends AppCompatActivity {
    Intent si;
    EditText email,password;
    Button login,signin;
    FirebaseAuth mAuth;
    AlertDialog.Builder adb;
    AlertDialog ad;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.emailLI);
        password = (EditText) findViewById(R.id.passwordLI);
        login = (Button) findViewById(R.id.loginB);
        signin = (Button) findViewById(R.id.signinB);
        mAuth.getInstance();

        //onclick login {loginUser()}
    }


    private void loginUser() {
        String email1 = email.getText().toString();
        String password1 = password.getText().toString();
        if (TextUtils.isEmpty(email1)) {
            email.setError("email cannot be empty");
            email.requestFocus();
        }
        else if(TextUtils.isEmpty(password1)){
            password.setError("password cannot be empty");
            password.requestFocus();
        }
        else{
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email1,password1).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(Login_Activity.this, "user logged successfully", Toast.LENGTH_SHORT).show();

                        si = new Intent(Login_Activity.this, MainActivity.class);
                        startActivity(si);
                    }
                    else{
                        Toast.makeText(Login_Activity.this, "Log in Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
            });

        }
    }

    public void register(View view) {
        si = new Intent(this, Signin_Activity.class);
        startActivity(si);

    }

    public void login(View view) {
        loginUser();
    }

    public void forgot(View view) {
        if(email.getText().toString().equals("")){
            adb.setTitle("please enter your email above in the right place :)");
            adb.setPositiveButton("Got it!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });

            ad = adb.create();
            ad.show();
        }
        else{
            String email1 = email.getText().toString();

            FirebaseAuth.getInstance().sendPasswordResetEmail(email1)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(Login_Activity.this, "Password reset email sent", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Login_Activity.this, "Failed to send password reset email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }
}