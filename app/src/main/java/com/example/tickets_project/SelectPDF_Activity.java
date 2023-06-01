package com.example.tickets_project;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.net.URI;
import java.net.URL;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectPDF_Activity extends AppCompatActivity {


    StorageReference mStorageRefP ;
    DatabaseReference  mDatabaseRefP;
    Intent gi,si;
    String pdfID;
    String url;
    String email,phone;
    List<String> ids =new ArrayList<String>();
    FirebaseFirestore db;
    int code =0;
    String updateId;
    PDFView pdfView;
    String[] info;
    String email1;
    String test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_pdf);



        mDatabaseRefP = FirebaseDatabase.getInstance().getReference("uploadsPDF");
        mStorageRefP = FirebaseStorage.getInstance().getReference("uploadsPDF");
        gi = getIntent();
        pdfID = gi.getStringExtra("pdfID");
        email = gi.getStringExtra("Email");
        phone = gi.getStringExtra("Phone");

        db = FirebaseFirestore.getInstance();
        code = gi.getIntExtra("code",0);
        if(code==1){
            updateId = gi.getStringExtra("updateIDpdf");
            info = gi.getStringArrayExtra("Information");

            email1 = gi.getStringExtra("Email");

        }
        setUploadID();



    }



    private void selectPDF(){
        /**
         * Opens the documents on the user's phone
         */
        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"PDF FILE SELECT"),12);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        /**
         * Loads the selected document on the screen
         */
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==12 && resultCode==RESULT_OK && data != null && data.getData()!=null){

            String s = uploadPDFFile(data.getData());
            test = data.getData().toString();
            Uri uri = Uri.parse(test);
            //URL url = uri.ur;

            Map p = new HashMap<>();
            p.put("imgUri" , test);
            p.put("imgUrl" , uri.toString());
            FirebaseFirestore.getInstance().collection("ID collection").document(s).update(p);

            pdfView = findViewById(R.id.pdfView1);
            pdfView.fromUri(uri)
                    .enableSwipe(true) // allows to block changing pages using swipe
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                    .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                    // spacing between pages in dp. To define spacing color, set view background
                    .spacing(0)
                    .load();




        }

    }


    private String uploadPDFFile(Uri data) {
        /**
         * Uploads the document to the database
         */
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("File is loading...");
        progressDialog.show();
        String[] i = new String[1];

        if(code==1){
            i[0] = updateId;
        }
        else i[0] = ids.get(0);
        StorageReference reference = mStorageRefP.child(i[0]+".pdf");
        reference.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isComplete());
                        Uri uri = uriTask.getResult();



                        putPDF putPDF1 = new putPDF(i[0],uri.toString(), data.toString());
                        mDatabaseRefP.child(i[0]).setValue(putPDF1);
                        FirebaseFirestore.getInstance().collection("ID collection").document(i[0]).update("imgUri",data.toString());
                        Toast.makeText(SelectPDF_Activity.this, "File upload", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        //setUploadID();
                        url = data.toString();


                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                        double progress = (100.0* snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                        progressDialog.setMessage("file uploaded... " + (int) progress +"%");

                    }
                });


        return i[0];


    }


    public void openFiles(View view) {
        selectPDF();

    }



    public void back(View view) {
        if(code==1){
            si = new Intent(SelectPDF_Activity.this,UpdateTickets.class);
            si.putExtra("Information" , info);
            si.putExtra("Email",email1);
            startActivity(si);
        }
        else{


            si = new Intent(this,MainActivity.class);

            si.putExtra("Email" , email);

            startActivity(si);


        }

    }

    private void saveData1(String a){
        /**
         * Saves the ID number of the upload in the database.
         */
        ids.add(a);
        Map<String,String> one= new HashMap<>();
        one.put("UploadID",a);

        db.collection("ID collection")
                .document(a)
                .set(one)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(SelectPDF_Activity.this, "id update", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SelectPDF_Activity.this, "id error update", Toast.LENGTH_SHORT).show();
                    }
                });

    }




    public void setUploadID() {
        /**
         * Extracts from the database an optional ID number for the next upload
         */
        if(!ids.isEmpty()) ids.clear();
        List<Integer> lst = new ArrayList<>();


        db.collection("ID collection")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots ) {
                        List<DocumentSnapshot> snapshots = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot sn : snapshots) {
                            lst.add(Integer.parseInt(sn.getString("UploadID")));
                        }
                        Collections.sort(lst, Collections.reverseOrder());
                        int ind = 0;
                        if(!lst.isEmpty()) ind = lst.get(0)+1;
                        else ind =0;


                        System.out.println(ind);

                        saveData1(ind+"");
                        Toast.makeText(SelectPDF_Activity.this, "" +ids.get(0), Toast.LENGTH_SHORT).show();



                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "FAIL: " , e);
                        Toast.makeText(SelectPDF_Activity.this, "hello!" , Toast.LENGTH_SHORT).show();

                    }
                });


    }


    public void next(View view) {
        /**
         * Moves to the next page according to the page they came from
         */
        Intent si1 = new Intent(this, UpdateTickets.class);
        if(code==1){
            si1.putExtra("Information" , info);
            si1.putExtra("Email",email1);
            startActivity(si1);
        }
        else{
            if(url.equals("")){
                Toast.makeText(this, "please choose the tickets pdf file", Toast.LENGTH_SHORT).show();
            }
            else{
                si = new Intent(this, UploadTicket_Activity.class);
                si.putExtra("Email",email);
                si.putExtra("Phone",phone);

                si.putExtra("upID", ids.get(0));
                startActivity(si);
            }
        }




    }
}
