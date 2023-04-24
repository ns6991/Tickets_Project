package com.example.tickets_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class AccountDetails extends AppCompatActivity {
    EditText snif,hesh;
    Intent si,gi;
    String email;
    int code;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_details);
        gi = getIntent();
        email = gi.getStringExtra("Email");
        code = gi.getIntExtra("code",1);

        snif = findViewById(R.id.snif);
        hesh = findViewById(R.id.heshbon);


    }

    public void save(View view) {
        if(checkValues()){
            Map<String,String> map1= createMap();
            String coll = "";

            db.collection("AccountDetails").document(map1.get("Email")).set(map1)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(AccountDetails.this, "saved successfully", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AccountDetails.this, "error", Toast.LENGTH_SHORT).show();

                        }
                    });


            si = new Intent(this,MainActivity.class);

            startActivity(si);

        }
    }

    private Map<String,String> createMap(){
        Map<String,String> one= new HashMap<>();

        one.put("Email",email);
        one.put("Snif",snif.getText().toString());
        one.put("Heshbon",hesh.getText().toString());

        return one;

    }
    private boolean checkValues(){

        if (TextUtils.isEmpty(snif.getText().toString())) {
            snif.setError("event's name cannot be empty");
            snif.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(hesh.getText().toString())) {
            hesh.setError("place cannot be empty");
            hesh.requestFocus();
            return false;
        }

        return true;
    }


    public void back(View view) {
        if(code==1){
            si = new Intent(this,Login_Activity.class);
            startActivity(si);
        }
        if(code==2){
            si = new Intent(this,PersonalZone.class);
            si.putExtra("Email",email);
            startActivity(si);
        }

    }
}