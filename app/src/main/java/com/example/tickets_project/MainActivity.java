package com.example.tickets_project;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,AdapterView.OnItemSelectedListener {

    /**
     * The role of the class is to present the cards in the database in the application to all users.
     */

    String[] categories = {"soccer", "basketball game", "show", "play", "musical", "movie", "other"};

    ArrayAdapter<String> adpSpinner;
    CustomAdapter adp;
    List<List<String>> eventsNames = new ArrayList<>();
    List<String> tmp = new ArrayList<>();
    ImageView button,uploadPost;


    List<List<String>> eventsNames1 = new ArrayList<>();
    List<String[]> active = new ArrayList<>();
    List<String[]> notactive = new ArrayList<>();





    AlertDialog.Builder adb;
    AlertDialog ad;

    Spinner spinner;
    FirebaseAuth mAuth;
    Intent gi, si;
    ListView lv;
    FirebaseFirestore db;

    FirebaseUser user;

    String email;
    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy");
    String[] phones = new String[1];


    String[] sendInfo = new String[12];
    String[] ticketsOfOwnerNotActive = new String[12];
    String[] ticketsOfOwnerActive = new String[12];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.upPostID);



        mAuth = FirebaseAuth.getInstance();

        email = mAuth.getCurrentUser().getEmail();


        db = FirebaseFirestore.getInstance();


        removePostsByDate();
        removePostsByDateWish();
        spinner = findViewById(R.id.spinnerID);
        uploadPost = findViewById(R.id.upPostID);
        lv = findViewById(R.id.listViewID);
        gi = getIntent();


        loadToList();
        lv.setOnItemClickListener(this);

        adp = new CustomAdapter(getApplicationContext(), eventsNames);
        lv.setAdapter(adp);

        adb = new AlertDialog.Builder(this);

        adpSpinner = new ArrayAdapter<String>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, categories);
        spinner.setAdapter(adpSpinner);
        spinner.setOnItemSelectedListener(this);

        getPhone();









    }

    protected void onStart() {
        super.onStart();
        if (mAuth == null) {
            startActivity(new Intent(this, Login_Activity.class));
        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        loadToList();
        //extract from db data to eventsNames
    }

    private void searchData(String query) {
        /**
         * The purpose of the function is to load into the ticket list tickets that meet the literal search.
         * The action uses the filtering action of the database according to the string that the function receives
         * and the conditions for the ticket to appear in the public ticket list.
         * After receiving a list with the cards that meet the description,
         * the action initializes the list of cards to display in the same list that was received.
         * If no tickets matching the description have been received, the action displays an appropriate message.
         *
         * @param String query of tickets
         */

        eventsNames1.clear();
        db.collection("TicketsInfo")
                .whereEqualTo("ManagerConfirm","1")
                .whereEqualTo("Active" , "1")
                .whereEqualTo("Name", query.toLowerCase())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().isEmpty()) {
                            adb.setTitle("There aren't tickets with this description..");
                            adb.setMessage("you can select another category or upload a request to 'wish list'");
                            adb.setPositiveButton("Wish List", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    wishList();
                                }
                            });
                            adb.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                            ad = adb.create();
                            ad.show();
                        } else {
                            for (DocumentSnapshot sn : task.getResult().getDocuments()) {

                                String s = sn.getString("Price") + " for " + sn.getString("Amount") + " tickets.";
                                tmp.add(sn.getString("Name"));
                                tmp.add(s);
                                tmp.add(sn.getString("UploadID"));
                                eventsNames1.add(tmp);

                            }
                            adp = new CustomAdapter(getApplicationContext(), eventsNames1);

                            lv.setAdapter(adp);
                        }


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "fail" + e.getMessage());

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setQueryHint("Type here to search");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchData(query);
                return false;

            }

            @Override
            public boolean onQueryTextChange(String newText) {

                searchData(newText);
                return false;
            }
        });
        menu.add(0, 0, 100, "Personal area");
        menu.add(0, 2, 300, "Terms");
        menu.add(0, 3, 400, "Log Out");

        if(email.equals("noashetrit@gmail.com")){
            menu.add(0, 5, 350, "manager page");

        }

        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        String st = item.getTitle().toString();
        if (st.equals("manager page")) {
            startActivity(new Intent(this, Manager_Activity.class));
        }
        if (st.equals("Personal area")) {
            si = new Intent(this, PersonalZone.class);


            startActivity(si);
        }


        if (st.equals("Terms")) {
            startActivity(new Intent(this, Terms_activity.class));
        }
        if (st.equals("Log Out")) {
            mAuth.signOut();
            startActivity(new Intent(this, Login_Activity.class));
            finish();
        }
        return true;
    }




    private void loadToList() {
        /**
         * The function loads the cards matching the conditions into the list
         */

        active.clear();
        notactive.clear();
        db.collection("TicketsInfo")
                .orderBy("UploadID", Query.Direction.DESCENDING)
                .whereEqualTo("Active", "1")
                .whereEqualTo("ManagerConfirm","1")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                        if (snapshots.isEmpty()) {
                            adb.setTitle("There aren't tickets with this description..");
                            adb.setMessage("you can select another category or upload a request to 'wish list'");
                            adb.setPositiveButton("Wish List", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    wishList();
                                }
                            });
                            adb.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                            ad = adb.create();
                            ad.show();

                        } else {
                            for (DocumentSnapshot sn : snapshots) {
                                    String s = sn.getString("Price") + " for " + sn.getString("Amount") + " tickets.";
                                    tmp.add(sn.getString("Name"));
                                    tmp.add(s);
                                    tmp.add(sn.getString("UploadID"));
                                    eventsNames1.add(tmp);


                            }

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

    private void saveData(String[] info){
        /**
         * The function save information from Listener function
         * @param String[] array of information about the ticket
         */
        sendInfo = info;
        si = new Intent(this, TicketsInformation.class);
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

            db.collection("TicketsInfo").document(id).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String[] info = new String[12];
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
                                info[11] = phones[0];
                               // info[11] =


                                saveData(info);

                            } else {
                                Toast.makeText(MainActivity.this, "doc doesn't exist", Toast.LENGTH_SHORT).show();

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

    private void removePostsByDate(){
        /**
         * Deletes tickets whose show date has passed, from the database
         */
        db.collection("TicketsInfo").get()
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

                                db.collection("TicketsInfo").document(sn.getId())
                                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(MainActivity.this, "delete error", Toast.LENGTH_SHORT).show();


                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(MainActivity.this, "delete error", Toast.LENGTH_SHORT).show();

                                            }
                                        });

                                db.collection("ID collection").document(sn.getId())
                                        .delete();

                            }



                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "delete error", Toast.LENGTH_SHORT).show();

                    }
                });

    }
    private void removePostsByDateWish(){
        /**
         * Deletes requests whose show date has passed, from the database
         */
        db.collection("WishList").get()
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
                                req= format2.parse( sn.getString("EventDate").substring(0,8));
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                            if(req.before(today)){

                                db.collection("WishList").document(sn.getId())
                                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(MainActivity.this, "delete error", Toast.LENGTH_SHORT).show();


                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(MainActivity.this, "delete error", Toast.LENGTH_SHORT).show();

                                            }
                                        });

                            }



                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "delete error", Toast.LENGTH_SHORT).show();

                    }
                });

    }




    private void savePhone(String phone){
        phones[0] = phone;

    }

    public void getPhone(){
        /**
         * Saves the user's phone number
         */
        db.collection("UserInfo").document(email).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            savePhone(documentSnapshot.getString("phone"));
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }




    public void uploadPost(View view) {
        /**
         * Checks if the user can upload a ticket according to the date of the last time.
         */
        if(email.equals("noashetrit@gmail.com")) uploadp("0533365168");
        else {
            db.collection("UserInfo").document(email).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                               // savePhone(documentSnapshot.getString("phone"));
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
                                            uploadp(documentSnapshot.getString("phone"));

                                        }
                                    }
                                    else uploadp(documentSnapshot.getString("phone"));
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }

                            } else {
                                Toast.makeText(MainActivity.this, "doc doesn't exist", Toast.LENGTH_SHORT).show();

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

    private void uploadp(String s) {
        si = new Intent(this, SelectPDF_Activity.class);
        si.putExtra("Email",email);
        si.putExtra("Phone",s);

        startActivity(si);
    }




    private void wishList(){
        /**
         * Moves to the requests page
         */
        si = new Intent(this, WishList.class);
        si.putExtra("Email",email);
        startActivity(si);
    }
    public void requestTicketsBT(View view) {
        /**
         * Displays a message adapted to the screen
         */
        adb.setTitle("you can upload a request to some ticket");
        adb.setMessage("Click 'OK' to go to the 'Requests' page");
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                wishList();
            }
        });
        adb.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        ad = adb.create();
        ad.show();
    }



    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        /**
         * Loads the ticket list by category
         */
        eventsNames1.clear();

        db.collection("TicketsInfo")
                .whereEqualTo("Active", "1")
                .whereEqualTo("ManagerConfirm","1")
                .whereEqualTo("Category", categories[i])
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                        if(snapshots.isEmpty()){
                            adb.setTitle("There aren't tickets with this description..");
                            adb.setMessage("you can select another category or upload a request to 'wish list'");
                            adb.setPositiveButton("Wish List", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    wishList();
                                }
                            });
                            adb.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                            ad = adb.create();
                            ad.show();

                        }
                        else{
                            for(DocumentSnapshot sn:snapshots) {
                                tmp = new ArrayList<>();
                                String s = sn.getString("Price") + " for " + sn.getString("Amount") + " tickets.";
                                tmp.add(sn.getString("Name"));
                                tmp.add(s);
                                tmp.add(sn.getString("UploadID")) ;
                                eventsNames1.add(tmp);

                            }
                            adp = new CustomAdapter(getApplicationContext(), eventsNames1);
                            lv.setAdapter(adp);
                        }



                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "EXCEPTION: " +e);
                    }
                });


        //add costume adp to info in sub title

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }



}