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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TicketsManagerInformation extends AppCompatActivity {

    Intent si,gi;
    TextView name,place,date_time,price,amount , category;

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
        category = findViewById(R.id.categoryIn2);
        adb = new AlertDialog.Builder(this);

        gi = getIntent();
        info = gi.getStringArrayExtra("TicketInfo");
        db = FirebaseFirestore.getInstance();


        imageID = info[7];

        name.setText("Event Name: " + info[0]);
        place.setText("Event place: " +info[1]);
        date_time.setText("Event Date: " +info[2]);
        category.setText("Event Category: " +info[3]);
        price.setText("Tickets Price:\n" +info[4]);
        amount.setText("Number of Tickets :\n"+info[5]);


        progressDialog = new ProgressDialog(TicketsManagerInformation.this);
        progressDialog.setMessage("Fetching image...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        Toast.makeText(this, "imageID" +imageID, Toast.LENGTH_SHORT).show();

        storageReference = FirebaseStorage.getInstance().getReference("uploadsImages/" + imageID + ".jpg");
        if(storageReference.equals(null)){
            imageView.setImageResource(R.drawable.ti);
        }
        else {
            Glide.with(TicketsManagerInformation.this)
                    .load(storageReference)
                    .into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    seeImage();
                }
            });
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

    private void seeImage(){
        si = new Intent(this, Image_Activity.class);
        si.putExtra("TicketInfo", info );
        si.putExtra("storageRef",("uploadsImages/" + imageID + ".jpg"));
        si.putExtra("activity" , "informationMan");
        startActivity(si);
    }
    public void ticDoc(View view) {
        si = new Intent(this, PDF_Activity.class);
        si.putExtra("TicketInfo", info);
        startActivity(si);
    }

    private Map<String,String> createMap(){
        Map<String,String> one= new HashMap<>();

        one.put("Name",info[0]);
        one.put("Place",info[1]);
        one.put("Date",info[2]);
        one.put("Category",info[3]);
        one.put("Price",info[4]);
        one.put("Amount",info[5]);
        one.put("OwnerEmail",info[6]);
        one.put("UploadID",info[7]);
        one.put("Active",info[8]);
        one.put("ManagerConfirm","1");
        return one;

    }

    public void Confirm(View view) {

        adb.setTitle("Are you sure that this ticket is approved?");
        adb.setPositiveButton("YES!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Map<String,String> map1= createMap();

                db.collection("TicketsInfo").document(map1.get("UploadID")).set(map1)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(TicketsManagerInformation.this, "saved successfully", Toast.LENGTH_SHORT).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(TicketsManagerInformation.this, "error", Toast.LENGTH_SHORT).show();

                            }
                        });

                db.collection("TicketsInfoToConfirm").document(map1.get("UploadID"))
                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(TicketsManagerInformation.this, "delete successfully", Toast.LENGTH_SHORT).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(TicketsManagerInformation.this, "delete error", Toast.LENGTH_SHORT).show();

                            }
                        });
                back(view);

                //send email
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
                db.collection("TicketsInfoToConfirm").document(info[7])
                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(TicketsManagerInformation.this, "delete successfully", Toast.LENGTH_SHORT).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(TicketsManagerInformation.this, "delete error", Toast.LENGTH_SHORT).show();

                            }
                        });






                db.collection("UserInfo").document(info[6]).update("CanUploadMore",""
                                    ,"LastUpload","")
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
                back(view);

                //send email




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