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
import android.widget.ImageView;
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
    EditText email,password, phone;
    TextView tv;
    FirebaseAuth mAuth;
    FirebaseUser user;
    AlertDialog.Builder adb;
    ImageView imageView;
    AlertDialog ad;
    int code;
    String password1;
    String email1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        email = (EditText) findViewById(R.id.emailSI);
        password = (EditText) findViewById(R.id.passwordSI);
        tv = findViewById(R.id.textV);
        phone = findViewById(R.id.phone4);
        imageView = findViewById(R.id.signinBT);
        code =0;


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

        /**
         * Saves the information about the new user in a data structure
         */
        Map<String,String> one= new HashMap<>();
        one.put("OwnerEmail",email.getText().toString());
        one.put("LastUpload","");
        one.put("CanUploadMore","");
        one.put("phone",phone.getText().toString());


        return one;
    }



    private void updateInfo(){
        /**
         * Saves the information about the new user in the database
         */
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
        /**
         * If the entered details are correct, the function registers the user to the application
         */
        email1 = email.getText().toString();
        password1 = password.getText().toString();
        if (TextUtils.isEmpty(email1)) {
            email.setError("email cannot be empty");
            email.requestFocus();
        }
        else if (!checkEmail(email1)) {
            email.setError("email should be proper");
            email.requestFocus();
        }
        else if (phone.getText().toString().length()!=10 || phone.getText().toString().charAt(0)!='0') {

            phone.setError("phone should be proper");
            phone.requestFocus();
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
                                            tv.setText("please press me after \nverify your email to enter");
                                            tv.setTextSize(15);
                                            imageView.setVisibility(View.INVISIBLE);
                                            code=1;




                                        }
                                    });
                                    ad = adb.create();
                                    ad.show();




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

    private void loginn(){
        startActivity(new Intent(this, Login_Activity.class));

    }
    public void login() {
        /**
         * Connects the newly registered user to the application
         */
        if(code ==1){
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email1,password1).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        loginn();



                    }
                    else{

                    }
                }
            });

        }
        else{
            startActivity(new Intent(this, Login_Activity.class));

        }

    }

    private boolean checkEmail(String email){
        /**
         * The function checks the correctness of the email address
         *
         * @param	String email
         * @return	boolean - true if the email is valid, false if not
         */

        int flag =0;
        int flag1 =0;
        for (int i =0;i<email.length();i++){
            if(email.charAt(i)=='@') flag++;
            if(email.charAt(i)=='.') flag1++;

        }
        if(flag==1 && flag1>=1) return true;
        else return false;


    }
}