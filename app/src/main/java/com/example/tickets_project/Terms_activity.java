package com.example.tickets_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Terms_activity extends AppCompatActivity {

    TextView tv;
    String s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        tv= findViewById(R.id.textView6);

        s = "";
        try{
            InputStream is = getAssets().open("TermsTicketsApp.txt");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            s = new String(buffer);

        }catch (IOException ex){
            ex.printStackTrace();
        }
        tv.setText(s);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,0,100,"Main");
        menu.add(0,0,200,"Upload ticket");
        menu.add(0,0,300,"Personal area");


        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        String st = item.getTitle().toString();
        if(st.equals("Main")){startActivity(new Intent(this, MainActivity.class));}
        if (st.equals("Upload ticket")) {startActivity(new Intent(this, UploadTicket_Activity.class));}
        if(st.equals("Personal area")) {startActivity(new Intent(this, PersonalZone.class));}

        return true;
    }
}