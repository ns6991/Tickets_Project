package com.example.tickets_project;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WishList extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    EditText eventName, eventPlace, eventDate;
    Intent si,gi;
    Spinner cat;
    String category;
    String[] categories = {"soccer", "basketball game", "show", "play","musical" ,"movie", "other"};
    ArrayAdapter<String> adp;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<String> ids =new ArrayList<String>();
    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish_list);

        gi = getIntent();

        eventName = findViewById(R.id.eventName);
        eventPlace = findViewById(R.id.eventPlace);
        eventDate = findViewById(R.id.eventDate);
        cat = findViewById(R.id.spinner11);

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("notification", "Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        adp = new ArrayAdapter<String>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,categories);
        cat.setAdapter(adp);
        cat.setOnItemSelectedListener(this);
        setUploadID();
    }
    private boolean checkValues(){
        if (TextUtils.isEmpty(eventName.getText().toString())) {
            eventName.setError("event's name cannot be empty");
            eventName.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(eventDate.getText().toString())) {
            eventDate.setError("event's date cannot be empty");
            eventDate.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(eventPlace.getText().toString())) {
            eventPlace.setError("event's place cannot be empty");
            eventPlace.requestFocus();
            return false;
        }
        return true;

    }

    private void saveData(String a){
        ids.add(a);

    }
    public void setUploadID() {
        if(!ids.isEmpty()) ids.clear();
        db
                .collection("WishList")
                .orderBy("UploadID", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots ) {
                        List<Integer> lst = new ArrayList<>();
                        int i=0;
                        List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot sn : snapshots) {
                            lst.add(Integer.parseInt(sn.getString("UploadID")));
                            i++;

                        }
                        Collections.sort(lst, Collections.reverseOrder());
                        int ind = lst.get(i)+1;
                        saveData(ind+"");

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "FAIL: " , e);

                    }
                });
        if (ids.isEmpty()){
            Log.d(TAG, "is empty");

        }

    }
    private Map<String,String> createMap(){
        String email = gi.getStringExtra("Email");
        Map<String,String> one= new HashMap<>();
        one.put("OwnerEmail",email);
        one.put("EventName",eventName.getText().toString());
        one.put("EventDate",eventDate.getText().toString());
        one.put("EventPlace",eventPlace.getText().toString());
        one.put("Category",category);
        one.put("UploadID",ids.get(0));
        String strDate = format.format(Calendar.getInstance().getTime());
        //Date today = format.parse(strDate);
        one.put("RequestDate", strDate);
        return one;
    }
    private void addUserWish(){
        if(checkValues()){
            Map<String,String> map1= createMap();
            String s1 = map1.get("UploadID");
            db.collection("WishList").document(s1).set(map1)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(WishList.this, "saved successfully", Toast.LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(WishList.this, "error", Toast.LENGTH_SHORT).show();

                        }
                    });

        }

    }

    private void notification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(WishList.this, "notification");
        builder.setContentTitle("your wish successfully received");
        builder.setContentText("If the card you requested is uploaded to the application, a message will be sent to your email.");
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setAutoCancel(true);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(WishList.this);
        managerCompat.notify(1,builder.build());
    }
    public void uploadRequest(View view) {//new table "wish list" doc email
        addUserWish();
        notification();
        si = new Intent(this, MainActivity.class);
        startActivity(si);

    }

    public void back(View view) {
        si = new Intent(this, MainActivity.class);
        startActivity(si);

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        category = categories[i];

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}