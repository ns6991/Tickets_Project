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

    String active1;
    String email1;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    StorageReference storageReference;
    ProgressDialog progressDialog;

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


        price.setEnabled(false);
        amount.setEnabled(false);



        gi = getIntent();
        String[] info = gi.getStringArrayExtra("Information");
        int change = gi.getIntExtra("CanChange",0);
        email1 = gi.getStringExtra("Email");

        if(info[6]=="1") active.setChecked(true);
        if(info[6]=="0") active.setChecked(false);

        name.setText(info[0]);
        place.setText(info[1]);
        date.setText(info[2]);
        price.setText(info[3]);
        amount.setText(info[4]);
        if(change==1) {
            price.setEnabled(true);
            amount.setEnabled(true);
        }
        id = info[6];

        storageReference = FirebaseStorage.getInstance().getReference("uploads/" + id + ".jpg");
        System.out.println(id+"333333333333333333");
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





    }
    //documentSnapshot.getString("Name"),documentSnapshot.getString("Place"),documentSnapshot.getString("Date"),documentSnapshot.getString("Price"),documentSnapshot.getString("Amount"),
     //       documentSnapshot.getString("OrderLink"),documentSnapshot.getString("UploadID"),documentSnapshot.getString("Active")};

    public void back(View view) {
        si = new Intent(this, PersonalZone.class);
        si.putExtra("Email",email1);

        startActivity(si);
    }

    private boolean checkValues(){
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
        if(Integer.parseInt(amount.getText().toString())<6 && checkValues()){
            Map<String, Object> one= new HashMap<>();;
            one.put("Price",price.getText().toString());
            one.put("Amount",amount.getText().toString());
            one.put("Active",active1);


            db.collection("TicketsInfo").document(id).update(one);
            Toast.makeText(this, "update successfully", Toast.LENGTH_SHORT).show();
            back(view);
        }
        else  Toast.makeText(this, "something go wrong..", Toast.LENGTH_SHORT).show();

    }

    public void active(View view) {
        if(active.isChecked()) active1 = "1";
        else active1 = "0";
    }
}