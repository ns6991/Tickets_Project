package com.example.tickets_project;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
//import com.github.barteksc.pdfviewer.PDFView;


import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PDF_Activity extends AppCompatActivity {

    WebView wv;
    Intent gi, si;
    String info[];

    String pdfID;
    String url;
    StorageReference reference;
    PDFView pdfView;
    int code;
    FirebaseAuth mAuth;
    String Email;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
        url = "";

        gi = getIntent();
        code = gi.getIntExtra("code",0);
        //info = gi.getStringArrayExtra("TicketInfo");
        if(code==0){
            info = gi.getStringArrayExtra("TicketInfo");
            pdfID = info[7];
        }
        else {
            pdfID = gi.getStringExtra("upId2");
        }
        mAuth = FirebaseAuth.getInstance();

        // Request permissions at runtime




        Email = mAuth.getCurrentUser().getEmail();

        pdfView = findViewById(R.id.pdfView);


        FirebaseFirestore.getInstance().collection("ID collection").document(pdfID)
                        .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Uri uri = Uri.parse(documentSnapshot.getString("imgUri"));

                        pdfView.fromUri(uri)
                                .enableSwipe(true) // allows to block changing pages using swipe
                                .swipeHorizontal(false)
                                .enableDoubletap(true)
                                .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                                .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                                // spacing between pages in dp. To define spacing color, set view background
                                .spacing(0)
                                .load();
                    }
                });


    }







    public void back(View view) {
        /**
         * Back to the previous page
         */
        Intent si1 = new Intent(this, MyTicketInfo_Activity.class);
        si = new Intent(this, TicketsManagerInformation.class);
        si.putExtra("TicketInfo", info);
        si1.putExtra("TicketInfo", info);
        si1.putExtra("Email" , Email);
        if(code==1){
            startActivity(si1);
        }
        else{
            startActivity(si);
        }


    }
}