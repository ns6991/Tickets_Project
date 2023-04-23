package com.example.tickets_project;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.tickets_project.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.grpc.Context;

public class TicketsInformation extends AppCompatActivity {

    Intent si,gi;
    TextView name,place,date_time,price,amount,category;

    ImageView imageView;
    String[] info;
    String imageID;
    Switch aSwitch;
    FirebaseFirestore db;


    StorageReference storageReference;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tickes_information);

        name = findViewById(R.id.nameTvID);
        place = findViewById(R.id.placeTvID);
        date_time = findViewById(R.id.dateTimeID);
        price = findViewById(R.id.priceTvID);
        amount = findViewById(R.id.amountTV);
        imageView = findViewById(R.id.imageView);
        category = findViewById(R.id.categoryIn);

        gi = getIntent();
        info = gi.getStringArrayExtra("TicketInfo");
        db = FirebaseFirestore.getInstance();
        name.setText("Event Name: " + info[0]);
        place.setText("Event place: " +info[1]);
        date_time.setText("Event Date: " +info[2]);
        category.setText("Event Category: " +info[3]);
        price.setText("Tickets Price:\n" +info[4]);
        amount.setText("Number of Tickets :\n"+info[5]);
        imageID = info[7];

        aSwitch = findViewById(R.id.switch3);
        aSwitch.setVisibility(View.INVISIBLE);
        if(info[8].equals("1")) aSwitch.setChecked(true);
        if(info[8].equals("0")) aSwitch.setChecked(false);
        if((FirebaseAuth.getInstance().getCurrentUser().getEmail()).equals("noashetrit@gmail.com")){
            aSwitch.setVisibility(View.VISIBLE);

        }



        progressDialog = new ProgressDialog(TicketsInformation.this);
        progressDialog.setMessage("Fetching image...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        Toast.makeText(this, "imageID" +imageID, Toast.LENGTH_SHORT).show();

        storageReference = FirebaseStorage.getInstance().getReference("uploadsImages/" + imageID + ".jpg");
        if(storageReference.equals(null)){
            imageView.setImageResource(R.drawable.ti);
        }
        else {
            Glide.with(TicketsInformation.this)
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

                            Toast.makeText(TicketsInformation.this, "failed ", Toast.LENGTH_SHORT).show();

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
        si.putExtra("activity" , "information");
        startActivity(si);
    }



    public void back(View view) {
        si = new Intent(this, MainActivity.class);
        startActivity(si);

    }

    public void payScreen(View view) {
    }

    private void updateAct(int code){

        db.collection("TicketsInfo").document(info[7])
                .update("Active",code+""
                ,"ManagerConfirm",code+"")
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
    public void isAct(View view) {
        if((FirebaseAuth.getInstance().getCurrentUser().getEmail()).equals("noashetrit@gmail.com")){
            if(aSwitch.isChecked()){
                updateAct(1);
            }
            else {
                updateAct(0);
            }


        }
    }
}