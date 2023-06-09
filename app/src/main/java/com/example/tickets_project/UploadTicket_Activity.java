package com.example.tickets_project;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.sql.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.mail.Session;
import javax.mail.Message;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.Properties;

public class UploadTicket_Activity extends AppCompatActivity {

    String[] categories = {"soccer", "basketball game", "show", "play", "musical", "movie", "other"};
    String[] amountN = {"1", "2", "3", "4", "5"};
    List<String> ids = new ArrayList<String>();
    EditText Name, Place, price, date, time;
    Spinner Category, amount;
    String cat, am;
    Intent si, gi;
    String emailOfOwner;
    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm");
    SimpleDateFormat format2 = new SimpleDateFormat("dd/MM/yy");

    Uri mImg;
    StorageReference mStorageRef;
    DatabaseReference mDatabaseRef;
    StorageTask mUploadImageTask;
    ProgressBar progressBar;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    AlertDialog.Builder adb;
    AlertDialog ad;

    ArrayAdapter<String> adp, adp2;
    String upldID, pho;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_ticket);

        Name = findViewById(R.id.nameID);
        Place = findViewById(R.id.placeID);

        Category = findViewById(R.id.categoryID);
        price = findViewById(R.id.priceID);
        amount = findViewById(R.id.amountID);
        date = findViewById(R.id.dateID);
        time = findViewById(R.id.timeID);
        gi = getIntent();
        emailOfOwner = gi.getStringExtra("Email");
        pho = gi.getStringExtra("Phone");

        progressBar = findViewById(R.id.progressBar);
        upldID = gi.getStringExtra("upID");
        ids.add(upldID);

        adp = new ArrayAdapter<String>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, categories);
        Category.setAdapter(adp);
        Category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                cat = categories[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        adp2 = new ArrayAdapter<String>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, amountN);
        amount.setAdapter(adp2);
        amount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                am = amountN[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        mStorageRef = FirebaseStorage.getInstance().getReference("uploadsImages");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploadsImages");


        adb = new AlertDialog.Builder(this);

        //Toast.makeText(UploadTicket_Activity.this, "hihihi" +ids.get(0), Toast.LENGTH_SHORT).show();

        //setUploadID();
    }


    public void ret(View view) {

        /**
         * The function returns to the main screen
         */
        startActivity(new Intent(this, MainActivity.class));
    }


    private void updateUserInfo(String uploadID, String active, String lastUpload) throws ParseException {
        /**
         * The function updates the user information (according to the new upload) in the database
         */
        Map<String, Object> one = new HashMap<>();
        ;
        one.put("UploadID", uploadID);
        one.put("Active", active);
        one.put("LastUpload", lastUpload);
        Calendar c = Calendar.getInstance();
        c.setTime(format2.parse(lastUpload));
        c.add(Calendar.DATE, 45);
        Date date = new Date(c.getTimeInMillis());
        String canUpload = format2.format(date);
        one.put("CanUploadMore", canUpload);


        db.collection("UserInfo").document(emailOfOwner).update("CanUploadMore", canUpload
                        , "LastUpload", lastUpload)
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

    private boolean checkValues() {
        /**
         * Checking the correctness of the input
         * @return boolean  - true if the input is valid , false if not
         */
        try {

            if ( TextUtils.isEmpty(date.getText().toString())) {

                date.setError("date cannot be empty ");
                date.requestFocus();
                return false;
            }
            Date event = format2.parse(date.getText().toString());
            String strDate = format2.format(Calendar.getInstance().getTime());
            Date today = format2.parse(strDate);
            if(event.before(today) ){
                date.setError(" date cannot be before today's date ");
                date.requestFocus();
                return false;
            }
            if (ids.isEmpty()) {
                return false;

            }
            if (TextUtils.isEmpty(Name.getText().toString())) {
                Name.setError("event's name cannot be empty");
                Name.requestFocus();
                return false;
            }
            if (TextUtils.isEmpty(Place.getText().toString())) {
                Place.setError("place cannot be empty");
                Place.requestFocus();
                return false;
            }
            if (TextUtils.isEmpty(price.getText().toString())) {
                price.setError("price cannot be empty");
                price.requestFocus();
                return false;
            }
            if(!checkTime(time.getText().toString())){
                time.setError("the time should be proper");
                time.requestFocus();
                return false;
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


        return true;

    }

    private boolean checkTime(String time){
        /**
         * The function checks the correctness of the time
         * @param String time
         * @return boolean  - true if the input is valid , false if not
         */
        if(time.length()!=5) return false;
        else if (Integer.parseInt(time.substring(0,2))>23 ||Integer.parseInt(time.substring(0,2))<0) {
            return false;
        }
        else if (Integer.parseInt(time.substring(3,5))>59 ||Integer.parseInt(time.substring(3,5))<0) {
            return false;
        }
        else if (time.charAt(2)!=':') {
            return false;
        }
        return true;
    }


    private Map<String, String> createMap() {
        /**
         * Saves the card information in the data structure
         * @return Map<String,String> with information about each ticket
         */

        Map<String, String> one = new HashMap<>();

        Date d = null;
        try {
            d = format2.parse(date.getText().toString());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        String datt = format2.format(d);
        one.put("Name", Name.getText().toString().toLowerCase());
        one.put("Place", Place.getText().toString());
        one.put("Date", datt + "  " + time.getText().toString());
        one.put("Category", cat);
        one.put("Price", price.getText().toString());
        one.put("Amount", am);
        one.put("OwnerEmail", emailOfOwner);
        one.put("UploadID", ids.get(0));
        one.put("Active", "1");

        if (emailOfOwner.equals("noashetrit@gmail.com")) {
            one.put("ManagerConfirm", "1");

        } else {
            one.put("ManagerConfirm", "0");

        }
        one.put("phone", pho);
        return one;

    }


    public void uploadPost(View view) throws ParseException {
        /**
         * The action saves the new card in the database and displays a new alert
         */

        if (checkValues()) {
            Map<String, String> map1 = createMap();
            String coll = "";
            int code = 0;
            if (emailOfOwner.equals("noashetrit@gmail.com")) {
                coll = "TicketsInfo";
                code = 1;
            } else coll = "TicketsInfoToConfirm";

            db.collection(coll).document(map1.get("UploadID")).set(map1)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(UploadTicket_Activity.this, "saved successfully", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UploadTicket_Activity.this, "error", Toast.LENGTH_SHORT).show();

                        }
                    });

            String strDate = format.format(Calendar.getInstance().getTime());
            updateUserInfo(map1.get("UploadID"), map1.get("Active"), strDate);

            if (code == 0) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(UploadTicket_Activity.this, "notification");
                builder.setContentTitle("your Post successfully received");
                builder.setContentText("After the system check your post, a mail will be send to you with information about your ticket");
                builder.setSmallIcon(R.drawable.ic_launcher_background);
                builder.setAutoCancel(true);

                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(UploadTicket_Activity.this);
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                managerCompat.notify(1, builder.build());

            }

            checkWishList();

            si = new Intent(this,MainActivity.class);

            startActivity(si);

        }
        else{
            si = new Intent(this,Terms_activity.class);
            adb.setTitle("you can't uploads this ticket's post");
            adb.setMessage("check again ticket's details and if your upload tickets on pdf");
            adb.setPositiveButton("Terms", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(si);
                }
            });
            adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            ad = adb.create();
            ad.show();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode ==1 && resultCode == RESULT_OK && data != null && data.getData() != null){
            mImg = data.getData();
            if(mUploadImageTask != null && mUploadImageTask.isInProgress()){
                Toast.makeText(this,"Upload in progress", Toast.LENGTH_SHORT).show();
            }
            else{
                uploadFile();
            }
        }
    }

    private void openFiles(){
        /**
         * The function opens the photo gallery on the user's phone
         */


        Intent in = new Intent();
        in.setType("image/*");
        in.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(in,1);

    }

    private String getFileExtension(Uri uri){
        /**
         * The function gets the extension of the selected file
         * @return String with extension of the selected file
         */

        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));

    }
    private void uploadFile(){
        /**
         * Uploading the selected image to the database
         */


        if(mImg != null){

            StorageReference fileRef = mStorageRef.child(ids.get(0) + "." + getFileExtension(mImg));
            System.out.println(ids.get(0) + ".................... upload id image");
            mUploadImageTask = fileRef.putFile(mImg)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setProgress(0);
                                }
                            }, 5000);
                            Upload upload = new Upload(ids.get(0), taskSnapshot.getStorage().getDownloadUrl().toString());
                            String uploadImageID = mDatabaseRef.push().getKey();
                            mDatabaseRef.child(uploadImageID).setValue(upload);
                            Toast.makeText(UploadTicket_Activity.this,"Upload successful", Toast.LENGTH_SHORT).show();


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UploadTicket_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            progressBar.setProgress((int)progress);


                        }
                    });
        }
        else{
            Toast.makeText(this,"no file selected", Toast.LENGTH_SHORT).show();
        }

    }
    public void uploadImage(View view) {
        if(!ids.isEmpty()){
            openFiles();

        }
        else{
            Toast.makeText(this,"please upload ticket pdf before.", Toast.LENGTH_SHORT).show();

        }


    }


    private boolean relevant(String date){
        /**
         * Checking if the entered date has passed
         * @param String date
         * @return boolean  - false if the date has passed , true if not
         */
        try {
            Date req = format2.parse(date);
            String strDate = format2.format(Calendar.getInstance().getTime());
            Date today = format2.parse(strDate);
            if(req.before(today)) return false;
            else return true;

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


    }

    private void sendMassage(String p){
        /**
         * The function sends a message to the user
         *  @param    String phone number of user.
         */
        String sms = "The ticket you wanted to buy was probably uploaded to the app \nopen tickets application! ";

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(p,null,sms,null,null);

    }

    private void checkWishList() {
        /**
         * Checks if the uploaded card matches one of the cards in the request list
         */
        db.collection("WishList")
                .whereEqualTo("EventPlace", Place.getText().toString())
                .whereEqualTo("Category", cat)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot sn : snapshots) {
                            if(relevant(sn.getString("EventDate")) && (sn.getString("EventDate").substring(0,8)).equals(date.getText().toString())){
                                sendMassage(sn.getString("phone"));

                            }



                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(UploadTicket_Activity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }

}
