package com.example.tickets_project;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.tickets_project.viewmodel.Manager_Activity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class TicketsManagerInformation extends AppCompatActivity {

    Intent si,gi;
    TextView name,place,date_time,price,amount;

    ImageView imageView;
    String[] info;
    String imageID;
    FirebaseFirestore db;
    StorageReference storageReference;
    ProgressDialog progressDialog;

    AlertDialog.Builder adb;
    AlertDialog ad;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tickets_manager_information);

        name = findViewById(R.id.nameTvIDM);
        place = findViewById(R.id.placeTvIDM);
        date_time = findViewById(R.id.dateTimeIDM);
        price = findViewById(R.id.priceTvIDM);
        amount = findViewById(R.id.amountTVM);
        imageView = findViewById(R.id.imageViewMan);

        gi = getIntent();
        info = gi.getStringArrayExtra("TicketInfo");
        db = FirebaseFirestore.getInstance();
        name.setText(info[0]);
        place.setText(info[1]);
        date_time.setText(info[2]);
        price.setText(info[4]);
        amount.setText(info[5]);
        imageID = info[7];


        progressDialog = new ProgressDialog(TicketsManagerInformation.this);
        progressDialog.setMessage("Fetching image...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        Toast.makeText(this, "imageID" +imageID, Toast.LENGTH_SHORT).show();

        storageReference = FirebaseStorage.getInstance().getReference("uploads/" + imageID + ".jpg");
        if(storageReference.equals(null)){
            imageView.setImageResource(R.drawable.ti);
        }
        else {
            Glide.with(TicketsManagerInformation.this)
                    .load(storageReference)
                    .into(imageView);
        }



        try{
            File localfile = File.createTempFile("tempFile", ".jpg");
            storageReference.getFile(localfile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            if(progressDialog.isShowing()){
                                progressDialog.dismiss();
                            }
                            Bitmap bitmap = BitmapFactory.decodeFile(localfile.getAbsolutePath());
                            imageView.setImageBitmap(bitmap);


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if(progressDialog.isShowing()){
                                progressDialog.dismiss();
                            }
                            imageView.setImageResource(R.drawable.ti);

                            Toast.makeText(TicketsManagerInformation.this, "failed ", Toast.LENGTH_SHORT).show();

                        }
                    });


        }
        catch (IOException e){
            e.printStackTrace();
        }

    }

    public void ticDoc(View view) {
    }

    public void Confirm(View view) {

        adb.setTitle("Are you sure that this ticket is approved?");
        adb.setPositiveButton("YES!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                db.collection("TicketsInfo").document(info[7])
                        .update("Active","1"
                                ,"ManagerConfirm","1")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully updated!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error updating document", e);
                            }
                        });
            }
        });
        adb.setNegativeButton("NO..", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        ad = adb.create();
        ad.show();


    }

    public void Canceled(View view) {
        adb.setTitle("Are you sure that this ticket is Not approved?");
        adb.setPositiveButton("YES!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                db.collection("TicketsInfo").document(info[7])
                        .update("Active","0"
                                ,"ManagerConfirm","0")
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully updated!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error updating document", e);
                            }
                        });
            }
        });
        adb.setNegativeButton("NO..", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        ad = adb.create();
        ad.show();
    }

    public void back(View view) {
        si = new Intent(this, Manager_Activity.class);

        startActivity(si);

    }
}