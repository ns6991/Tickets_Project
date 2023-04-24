package com.example.tickets_project;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class BuyTicket_Activity extends AppCompatActivity {

    EditText credit,cvv,val,id;
    TextView price;
    Intent si,gi;
    String pr, ownerEmail, buyerEmail ,uploadID;
    DatabaseReference reference;
    String pdfUrl;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_ticket);

        price = findViewById(R.id.price);
        credit = findViewById(R.id.creditNum);
        cvv = findViewById(R.id.num3);
        val = findViewById(R.id.tockef);
        id = findViewById(R.id.idNum);
        gi = getIntent();
        pr = gi.getStringExtra("Price");
        ownerEmail = gi.getStringExtra("EmailOwner");
        buyerEmail = gi.getStringExtra("EmailBuyer");
        uploadID = gi.getStringExtra("PDF");

        price.setText(pr +"₪");

        reference = FirebaseDatabase.getInstance().getReference().child("uploadsPDF");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String s = snapshot.child(uploadID).child("url").getValue().toString();
                saveData(s);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void saveData(String s){pdfUrl = s;}

    public void buy(View view) {

        //send email to email owner that his ticket sold and he will get the money
        //send pdf in email to email buyer

        db.collection("TicketInfo").document(uploadID).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(BuyTicket_Activity.this, "sold", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(BuyTicket_Activity.this, "fail to sold", Toast.LENGTH_SHORT).show();
                    }
                });
        si = new Intent(this,MainActivity.class);

        startActivity(si);
    }

    public void back(View view) {
        si = new Intent(this,MainActivity.class);

        startActivity(si);
    }
}