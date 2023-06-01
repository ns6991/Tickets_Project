
package com.example.tickets_project;

import static android.content.ContentValues.TAG;

        import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

        import android.annotation.SuppressLint;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.ImageView;
        import android.widget.ListView;
        import android.widget.Toast;

        import com.example.tickets_project.CustomAdapter;
        import com.example.tickets_project.Login_Activity;
        import com.example.tickets_project.MainActivity;
        import com.example.tickets_project.PersonalZone;
        import com.example.tickets_project.R;
        import com.example.tickets_project.TicketsInformation;
        import com.example.tickets_project.TicketsManagerInformation;
        import com.example.tickets_project.UploadTicket_Activity;
        import com.google.android.gms.tasks.OnFailureListener;
        import com.google.android.gms.tasks.OnSuccessListener;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.firestore.DocumentSnapshot;
        import com.google.firebase.firestore.FirebaseFirestore;
        import com.google.firebase.firestore.Query;
        import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Manager_Activity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    Intent si;
    ListView listView1;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    List<String> tmp = new ArrayList<>();
    List<List<String>> eventsNames1 = new ArrayList<>();
    String[] sendInfo = new String[11];
    //11
    CustomAdapter adp;
    AlertDialog.Builder adb;
    AlertDialog ad;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        listView1 = findViewById(R.id.lvTicToCheck);
        db = FirebaseFirestore.getInstance();

        removePostsByDate();
        adb = new AlertDialog.Builder(this);
        listView1.setOnItemClickListener(this);
        loadToList();
       // if(eventsNames1.isEmpty()) nothingToCheck();

        adp = new CustomAdapter(getApplicationContext(), eventsNames1);
        listView1.setAdapter(adp);




    }


    private void removePostsByDate(){
        /**
         * Deletes tickets whose show date has passed, from the database
         */
        db.collection("TicketsInfoToConfirm").get()
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
                                req= format2.parse( sn.getString("Date").substring(0,8));
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                            if(req.before(today)){

                                db.collection("TicketsInfoToConfirm").document(sn.getId())
                                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(Manager_Activity.this, "delete error", Toast.LENGTH_SHORT).show();


                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(Manager_Activity.this, "delete error", Toast.LENGTH_SHORT).show();

                                            }
                                        });

                            }



                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Manager_Activity.this, "delete error", Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void loadToList() {
        /**
         * The function loads the cards matching the conditions into the list
         */
        eventsNames1.clear();
        db.collection("TicketsInfoToConfirm")
                .whereEqualTo("Active", "1")
                .whereEqualTo("ManagerConfirm","0")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                        if (!snapshots.isEmpty()) {
                            for (DocumentSnapshot sn : snapshots) {
                                tmp = new ArrayList<>();
                                String s = sn.getString("Price") + " for " + sn.getString("Amount") + " tickets.";
                                tmp.add(sn.getString("Name"));
                                tmp.add(s);
                                tmp.add(sn.getString("UploadID"));
                                eventsNames1.add(tmp);

                            }
                            if(eventsNames1.isEmpty()) nothingToCheck();

                            adp = new CustomAdapter(getApplicationContext(), eventsNames1);
                            listView1.setAdapter(adp);

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "fail: ", e);

                    }
                });

    }

    private void nothingToCheck(){
        /**
         * When there are no cards in the list, a corresponding message is displayed
         */
        adb.setTitle("There Are Not Tickets to Check :)");
        adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

            }
        });

        ad = adb.create();
        ad.show();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,0,100,"Main");
        menu.add(0,0,300,"Personal area");
        menu.add(0, 3, 400, "Log Out");


        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        String st = item.getTitle().toString();
        if(st.equals("Main")){startActivity(new Intent(this, MainActivity.class));}
        if(st.equals("Personal area")) {startActivity(new Intent(this, PersonalZone.class));}
        if (st.equals("Log Out")) {
            mAuth.signOut();
            startActivity(new Intent(this, Login_Activity.class));
            finish();
        }

        return true;
    }






    public void upload(View view) {
        /**
         * Moves to the ticket upload page
         */
        si = new Intent(this, SelectPDF_Activity.class);
        si.putExtra("Email","noashetrit@gmail.com");

        startActivity(si);

    }

    public void back(View view) {
        /**
         * Moves to the main page
         */
        si = new Intent(this, MainActivity.class);

        startActivity(si);
    }


    private void saveData(String[] info){
        sendInfo = info;
        si = new Intent(this, TicketsManagerInformation.class);
        si.putExtra("TicketInfo", sendInfo );

        startActivity(si);

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        /**
         * Saves the information on the selected card and sends to the information page
         */
        if (!eventsNames1.isEmpty()) {
            String id = eventsNames1.get(i).get(2);

            db.collection("TicketsInfoToConfirm").document(id).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String[] info = new String[11];
                                info[0] = documentSnapshot.getString("Name");
                                info[1] = documentSnapshot.getString("Place");
                                info[2] = documentSnapshot.getString("Date");
                                info[3] = documentSnapshot.getString("Category");
                                info[4] = documentSnapshot.getString("Price");
                                info[5] = documentSnapshot.getString("Amount");
                                info[6] = documentSnapshot.getString("OwnerEmail");
                                info[7] = documentSnapshot.getString("UploadID");
                                info[8] = documentSnapshot.getString("Active");
                                info[9] = documentSnapshot.getString("ManagerConfirm");
                                info[10] = documentSnapshot.getString("phone");
                                saveData(info);

                            } else {
                                Toast.makeText(Manager_Activity.this, "doc doesn't exist", Toast.LENGTH_SHORT).show();

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
}