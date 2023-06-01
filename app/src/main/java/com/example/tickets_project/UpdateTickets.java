package com.example.tickets_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UpdateTickets extends AppCompatActivity {
    Intent si,gi;
    EditText amount, price;
    TextView name, place, date;
    Switch active;
    ImageView iv;
    String id;

    String active1 ;
    String email1;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageReference;
    ProgressDialog progressDialog;
    String[] info;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_tickets);
        amount = findViewById(R.id.amountED);
        price = findViewById(R.id.priceEdID);
        name = findViewById(R.id.nameTvID2);
        place = findViewById(R.id.placeTvID2);
        date = findViewById(R.id.dateTimeID2);
        active = findViewById(R.id.switch2);
        iv = findViewById(R.id.imageViewEdit);

        active1 = "1";
        gi = getIntent();
        info = gi.getStringArrayExtra("Information");
        email1 = gi.getStringExtra("Email");

        name.setText("Event Name: " + info[0]);
        place.setText("Event place: " +info[1]);
        date.setText("Event Date: " +info[2]);
        price.setText( info[3]);
        amount.setText(info[4]);

        id = info[5];

        storageReference = FirebaseStorage.getInstance().getReference("uploadsImages/" + id + ".jpg");
        if(storageReference.equals(null)){
            iv.setImageResource(R.drawable.ti);
        }
        else {
            Glide.with(UpdateTickets.this)
                    .load(storageReference)
                    .into(iv);

        }

        progressDialog = new ProgressDialog(UpdateTickets.this);
        progressDialog.setMessage("Fetching image...");
        progressDialog.setCancelable(false);
        progressDialog.show();



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
                            iv.setImageBitmap(bitmap);


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if(progressDialog.isShowing()){
                                progressDialog.dismiss();
                            }
                            Toast.makeText(UpdateTickets.this, "failed ", Toast.LENGTH_SHORT).show();
                            iv.setImageResource(R.drawable.ti);

                        }
                    });


        }
        catch (IOException e){
            e.printStackTrace();
        }

        if(info[6].equals("1")){
            active.setChecked(true);
        }
        else active.setChecked(false);

    }
    //documentSnapshot.getString("Name"),documentSnapshot.getString("Place"),documentSnapshot.getString("Date"),documentSnapshot.getString("Price"),documentSnapshot.getString("Amount"),
     //       documentSnapshot.getString("OrderLink"),documentSnapshot.getString("UploadID"),documentSnapshot.getString("Active")};

    public void back(View view) {
        /**
         * Returning to the personal zone page
         */
        si = new Intent(this, PersonalZone.class);
        si.putExtra("Email",email1);

        startActivity(si);
    }

    private boolean checkValues(){
        /**
         * Checking the correctness of the input
         * @return boolean  - true if the input is valid , false if not
         */
        if (TextUtils.isEmpty(amount.getText().toString()) || Integer.parseInt(amount.getText().toString())>5) {
            amount.setError("amount cannot be empty or more than 5");
            amount.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(price.getText().toString())) {
            price.setError("price cannot be empty");
            price.requestFocus();
            return false;
        }
        return true;

    }
    public void done(View view) {
        /**
         * Updates the database with the new card information
         */
        if(Integer.parseInt(amount.getText().toString())<6 && checkValues()){
            Map<String, Object> one= new HashMap<>();;
            one.put("Price",price.getText().toString());
            one.put("Amount",amount.getText().toString());
            one.put("Active",active1);


            db.collection("TicketsInfo").document(id).update(one);
            //Toast.makeText(this, "update successfully", Toast.LENGTH_SHORT).show();
            sendToConfirm();
            back(view);
        }

    }

    public void active(View view) {
        if(active.isChecked()) active1 = "1";
        else active1 = "0";
    }

    public void uploadPDF(View view) {
        /**
         * Moves to the page, selecting a new document for the card
         */

        si =new Intent(UpdateTickets.this, SelectPDF_Activity.class);
        si.putExtra("code" , 1);
        si.putExtra("updateIDpdf" , id);
        si.putExtra("Information" , info);
        si.putExtra("Email",email1);

        startActivity(si);
    }

    private void sendToConfirm(){
        /**
         * The function deletes the card from the pool of available cards and adds it to the pool of cards for testing
         */
        db.collection("TicketsInfo").document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Map<String,String> one= new HashMap<>();
                            one.put("Name",documentSnapshot.getString("Name")) ;
                            one.put("Place", documentSnapshot.getString("Place"));
                            one.put( "Date",documentSnapshot.getString("Date"));
                            one.put("Category", documentSnapshot.getString("Category"));
                            one.put( "Price", documentSnapshot.getString("Price"));
                            one.put( "Amount",documentSnapshot.getString("Amount"));
                            one.put( "OwnerEmail",documentSnapshot.getString("OwnerEmail"));
                            one.put( "UploadID",documentSnapshot.getString("UploadID"));
                            one.put("Active", documentSnapshot.getString("Active"));
                            one.put( "ManagerConfirm","0");

                            db.collection("TicketsInfoToConfirm").document(id).set(one).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    db.collection("TicketsInfo").document(id).delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Toast.makeText(UpdateTickets.this, "added to manager", Toast.LENGTH_SHORT).show();

                                                }
                                            });
                                }
                            });




                        }

                        else {
                            Toast.makeText(UpdateTickets.this, "doc doesn't exist", Toast.LENGTH_SHORT).show();

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }
}