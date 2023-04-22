package com.example.tickets_project;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tickets_project.viewmodel.Manager_Activity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PersonalZone extends AppCompatActivity implements AdapterView.OnItemClickListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    String Email;
    List<String[]> retA =new ArrayList<String[]>();
    List<String[]> retNA =new ArrayList<String[]>();


    TextView tv;
    ListView lv;
    Switch aSwitch;

    Intent si, s1;
    AlertDialog.Builder adb;
    AlertDialog ad;
    FirebaseAuth mAuth;
    CustomAdapter2 adp;
    Intent gi;
    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_zone);
        gi = getIntent();
        Email = gi.getStringExtra("Email");
       // adp = new CustomAdapter2(getApplicationContext(), uploadsActive);


        tv= findViewById(R.id.emailTv);
        tv.setText(Email);
        aSwitch = findViewById(R.id.switch1);
        lv = findViewById(R.id.lvTic);

        lv.setOnItemClickListener(this);
        lv.setAdapter(adp);
        adb = new AlertDialog.Builder(this);
        aSwitch.setChecked(true);
        mAuth = FirebaseAuth.getInstance();
        activeOrNot(1);



    }
    @Override
    protected void onResume() {
        super.onResume();

        //extract from db data to eventsNames
    }




    private List<String[]> activeOrNot(int code){
        List<String[]> ret = new ArrayList<>();
        retA.clear();
        retNA.clear();
        db.collection("TicketsInfo")
                .whereEqualTo("OwnerEmail",Email)
                .whereEqualTo("Active", code+"")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                        if (!snapshots.isEmpty()){
                            for(DocumentSnapshot sn:snapshots) {
                                String[] temp ={sn.getString("Name"),sn.getString("Place"),sn.getString("Date"),sn.getString("Price"),sn.getString("Amount"),sn.getString("UploadID"),sn.getString("Active")};
                                ret.add(temp);
                                if(code==1) retA.add(temp);
                                else retNA.add(temp);

                            }


                        }

                        adp = new CustomAdapter2(getApplicationContext(), ret );
                        lv.setAdapter(adp);



                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"FAIL "+ e);
                    }
                });
        return ret;


    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,0,100,"Main");
        menu.add(0,1,200,"Upload ticket");
        menu.add(0,2,300,"Terms");
        menu.add(0,3,400,"Log Out");
        if(Email.equals("noashetrit@gmail.com")){
            menu.add(0, 5, 350, "manager page");

        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        String st = item.getTitle().toString();
        if (st.equals("manager page")) {
            startActivity(new Intent(this, Manager_Activity.class));
        }
        if(st.equals("Main")){startActivity(new Intent(this, PersonalZone.class));}
        if (st.equals("Upload ticket")) {startActivity(new Intent(this, UploadTicket_Activity.class));}
        if(st.equals("Terms")) {startActivity(new Intent(this, Terms_activity.class));}
        if(st.equals("Log Out")) {
            mAuth.signOut();
            startActivity(new Intent(this, Login_Activity.class));
            finish();
        }

        return true;
    }






    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(aSwitch.isChecked()){
           // activeOrNot(1);
            String[] s  = retA.get(i);
            si = new Intent(this, UpdateTickets.class);
            si.putExtra("Information",s);
            si.putExtra("CanChange",1);
        }
        else{
            //activeOrNot(0);

            String[] s  = retNA.get(i);
            si = new Intent(this, UpdateTickets.class);
            si.putExtra("Information",s);
            si.putExtra("CanChange",0);
        }




        startActivity(si);

    }

    public void lastUpload(View view) {
        db.collection("UserInfo").document(Email).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            String s = documentSnapshot.getString("CanUploadMore");
                            String s1 =documentSnapshot.getString("LastUpload");
                            if(s1=="" || s ==""){
                                adb.setTitle("you aren't upload post yet");
                                adb.setMessage("you can upload now");
                            }
                            else{
                                adb.setTitle("your last upload was on: " + s1);
                                adb.setMessage("you can upload again on: " + s);

                            }

                            adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            ad = adb.create();
                            ad.show();


                        }
                        else{
                            Toast.makeText(PersonalZone.this, "doc doesn't exist", Toast.LENGTH_SHORT).show();

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    public void back(View view) {
        s1 = new Intent(this, MainActivity.class);
        startActivity(s1);
    }

    public void upload(View view) {
        db.collection("UserInfo").document(Email).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            try {
                                if (documentSnapshot.getString("CanUploadMore") != "") {
                                    Date canUploadMore = format.parse(documentSnapshot.getString("CanUploadMore"));
                                    String strDate = format.format(Calendar.getInstance().getTime());
                                    Date today = format.parse(strDate);

                                    if (today.before(canUploadMore)) {

                                        adb.setTitle("your last upload was on: " + documentSnapshot.getString("LastUpload"));
                                        adb.setMessage("you can upload again on: " + documentSnapshot.getString("CanUploadMore"));

                                        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        });
                                        ad = adb.create();
                                        ad.show();
                                    } else {
                                        uploadp();

                                    }
                                }
                                else uploadp();
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }

                        } else {
                            Toast.makeText(PersonalZone.this, "doc doesn't exist", Toast.LENGTH_SHORT).show();

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });


    }

    private void uploadp() {
        si = new Intent(this, UploadTicket_Activity.class);
        si.putExtra("Email",Email);

        startActivity(si);
    }

    public void active(View view) {
        if(aSwitch.isChecked()){
            activeOrNot(1);
        }
        else{
            activeOrNot(0);
        }
    }
}