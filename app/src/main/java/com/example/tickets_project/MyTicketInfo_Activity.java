package com.example.tickets_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MyTicketInfo_Activity extends AppCompatActivity {


    TextView name,place,date_time,price;
    Intent si,gi;
    String[] info;
    String email;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ticket_info);

        name = findViewById(R.id.nameTvID11);
        place = findViewById(R.id.placeTvID11);
        date_time = findViewById(R.id.dateTimeID11);
        price = findViewById(R.id.price_amount11);

        gi = getIntent();
        info = gi.getStringArrayExtra("TicketInfo");
        email = gi.getStringExtra("Email" );

        name.setText("Event Name: " + info[0]);
        place.setText("Event place: " +info[1]);
        date_time.setText("Event Date: " +info[2]);
        price.setText(info[3] + "₪ for " + info[4] + " tickets");

    }

    //String[] temp ={sn.getString("name"),sn.getString("place"),sn.getString("date"),sn.getString("price"),sn.getString("amount"),sn.getString("uploadID"),sn.getString("userEmail")};


    public void back(View view) {
        /**
         * Returns to the user's tickets page
         */
        si = new Intent(MyTicketInfo_Activity.this,myTickets_Activity.class);

        si.putExtra("Email" , email);
        startActivity(si);

    }

    public void seeTicket(View view) {
        /**
         * Moves to the card document page
         */
        si = new Intent(MyTicketInfo_Activity.this,PDF_Activity.class);
        si.putExtra("code" , 1);
        String up  =  info[5];
        si.putExtra("upId2",up);
        si.putExtra("TicketInfo",info);
        startActivity(si);


    }
}