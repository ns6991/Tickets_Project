package com.example.tickets_project;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension;
import com.google.android.gms.common.api.ApiException;
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

/**
 * The class connects the user to the application
 */

public class Login_Activity extends AppCompatActivity {
    Intent si;
    EditText email,password;
    TextView forget, notRegistered;

    ImageView login;
    FirebaseAuth mAuth;
    AlertDialog.Builder adb;
    AlertDialog ad;
    int flag =0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.emailLI);
        password = (EditText) findViewById(R.id.passwordLI);
        login = (ImageView) findViewById(R.id.loginB);
      //  gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
       // gsc = GoogleSignIn.getClient(this,gso);



        forget = findViewById(R.id.textView11);
        notRegistered = findViewById(R.id.textView2);

        forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forgot();

            }
        });



        notRegistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
        mAuth = mAuth.getInstance();
        adb = new AlertDialog.Builder(this);





        if(mAuth.getCurrentUser() != null){
            si = new Intent(Login_Activity.this, MainActivity.class);
            startActivity(si);


        }



        //onclick login {loginUser()}
    }

    protected void onResume() {
        super.onResume();

        //extract from db data to eventsNames
    }



    private void loginUser() {
        /**
         * The operation checks the correctness of the input of the email address and password,
         * after the check it calls a operation of firebase auth and connects the same user to
         * the application using the email address and password. If the password does not match the email address,
         * the function will not connect the user and will display a corresponding message.
         */

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

                        si = new Intent(Login_Activity.this, MainActivity.class);
                        startActivity(si);
                        Toast.makeText(Login_Activity.this, "user logged successfully", Toast.LENGTH_SHORT).show();


                    }
                    else{
                        Toast.makeText(Login_Activity.this, "Log in Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
            });

        }
    }

    public void register() {
        /**
         * Moves the user to the registration screen
         */
        si = new Intent(this, Signin_Activity.class);
        startActivity(si);

    }

    public void login(View view) {
        loginUser();
    }

    public void forgot() {
        /**
         * The function allows the user to change the password through a message sent to the email address
         */
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