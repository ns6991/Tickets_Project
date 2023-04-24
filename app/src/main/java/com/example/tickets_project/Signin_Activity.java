package com.example.tickets_project;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class Signin_Activity extends AppCompatActivity {
    EditText email,password;
    Button signin;
    TextView tv;
    FirebaseAuth mAuth;
    FirebaseUser user;
    AlertDialog.Builder adb;
    AlertDialog ad;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        email = (EditText) findViewById(R.id.emailSI);
        password = (EditText) findViewById(R.id.passwordSI);
        tv = findViewById(R.id.textV);


        signin = (Button) findViewById(R.id.signinBT);

        mAuth = FirebaseAuth.getInstance();
        adb = new AlertDialog.Builder(this);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();

            }
        });


    }


    private Map<String,String> createMap(){
        Map<String,String> one= new HashMap<>();
        one.put("OwnerEmail",email.getText().toString());
        one.put("LastUpload","");
        one.put("CanUploadMore","");
        return one;
    }

    private void updateInfo(){
        Map<String,String> map1= createMap();
        String s1 = map1.get("OwnerEmail");
        FirebaseFirestore.getInstance().collection("UserInfo").document(s1).set(map1)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(Signin_Activity.this, "saved successfully", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Signin_Activity.this, "error", Toast.LENGTH_SHORT).show();

                    }
                });
    }



    private void createUser(){
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
            mAuth.createUserWithEmailAndPassword(email1,password1).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    updateInfo();
                                    adb.setTitle("registered successfully");
                                    adb.setMessage("please check your email box to verify your email address before your trying to sign in again");
                                    adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Intent si = new Intent(Signin_Activity.this, AccountDetails.class);
                                            si.putExtra("Email",email1);
                                            si.putExtra("code",1);

                                            startActivity(si);


                                        }
                                    });
                                    ad = adb.create();
                                    ad.show();

                                    if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
                                        Log.d(TAG,"verify");

                                    }
                                    else
                                        Toast.makeText(Signin_Activity.this, "please check your email box", Toast.LENGTH_SHORT).show();

                                }
                                else
                                    Toast.makeText(Signin_Activity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });

                    }
                    else{
                        Toast.makeText(Signin_Activity.this, "sign in Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
            });



        }
    }

    public void register(View view) {
        createUser();
    }

    public void login() {
        startActivity(new Intent(this, Login_Activity.class));
    }
}