package com.example.tickets_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PDF_Activity extends AppCompatActivity {

    WebView wv;
    Intent gi, si;
    String info[];

    String pdfID;
    String url;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
        url = "";

        gi = getIntent();
        info = gi.getStringArrayExtra("TicketInfo");
        pdfID = info[7];

        reference = FirebaseDatabase.getInstance().getReference().child("uploadsPDF");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String s = snapshot.child(pdfID).child("url").getValue().toString();
                saveData(s);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        wv = findViewById(R.id.web2);
        wv.setWebViewClient(new WebViewClient());
       // String s = "https://firebasestorage.googleapis.com/v0/b/tickets-project-8310d.appspot.com/o/uploadsPDF%2F5.pdf?alt=media&token=3d05dc9e-5098-4ace-bc6a-4c973110e67d";

        wv.getSettings().setSupportZoom(true);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.loadUrl( "https://drive.google.com/viewerng/viewer?embedded=true&url=" + url);
    }

    private void saveData(String s){url = s;}

    public void back(View view) {
        si = new Intent(this, TicketsManagerInformation.class);
        si.putExtra("TicketInfo", info);
        startActivity(si);

    }
}