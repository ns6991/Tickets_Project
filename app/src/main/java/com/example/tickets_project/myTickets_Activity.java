package com.example.tickets_project;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class myTickets_Activity extends AppCompatActivity implements AdapterView.OnItemClickListener{
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Intent gi,si;
    String email;
    ListView lv;
    List<String[]> Tickets =new ArrayList<String[]>();

    CustomAdapter2 adp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tickets);

        lv = findViewById(R.id.lvMyTic);

        lv.setOnItemClickListener(this);
        lv.setAdapter(adp);
        gi = getIntent();
        removePostsByDate();

        email = gi.getStringExtra("Email");
        Tickets = loadToList();

    }

    public void back(View view) {
        si = new Intent(myTickets_Activity.this,PersonalZone.class);

        startActivity(si);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        /**
         * Moves to the card information page
         */
        si = new Intent(myTickets_Activity.this,MyTicketInfo_Activity.class);
        si.putExtra("TicketInfo" , Tickets.get(i));
        si.putExtra("Email" , email);


        startActivity(si);




    }

    private void removePostsByDate(){
        /**
         * Deletes tickets whose show date has passed, from the database
         */
        db.collection("UserTickets").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        SimpleDateFormat format2 = new SimpleDateFormat("dd/MM/yy");

                        Date req = null;
                        Date today = null;
                        try {
                            String strDate = format2.format(Calendar.getInstance().getTime());
                            today = format2.parse(strDate);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }

                        List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot sn : snapshots) {
                            try {
                                req= format2.parse( sn.getString("date").substring(0,8));
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                            if(req.before(today)){

                                db.collection("UserTickets").document(sn.getId())
                                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(myTickets_Activity.this, "delete error", Toast.LENGTH_SHORT).show();


                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(myTickets_Activity.this, "delete error", Toast.LENGTH_SHORT).show();

                                            }
                                        });

                            }



                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(myTickets_Activity.this, "delete error", Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private List<String[]> loadToList(){
        /**
         * Loads the tickets the user bought into the list
         * @return List<String[]> list with information about each ticket
         */

        List<String[]> ret = new ArrayList<>();

        Tickets.clear();
        db.collection("UserTickets")
                .whereEqualTo("userEmail",email)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                        if (!snapshots.isEmpty()){
                            for(DocumentSnapshot sn:snapshots) {
                                String[] temp ={sn.getString("name"),sn.getString("place"),sn.getString("date"),sn.getString("price"),sn.getString("amount"),sn.getString("uploadID"),sn.getString("userEmail")};
                                ret.add(temp);


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
}