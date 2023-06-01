package com.example.tickets_project;

import static android.content.ContentValues.TAG;

import static com.example.tickets_project.util.PaymentsUtil.createPaymentsClient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuyTicket_Activity extends AppCompatActivity {
    /**
     * @author		Noa Shetrit
     * The purpose of the class is to show the user the possibility of buying the ticket using google pay.
     */


    TextView price;
    Intent si,gi;
    String pr, ownerPhone, buyerEmail ,uploadID,  buyerPhone , priceGoogle;
    String pdfUrl;
    FirebaseFirestore db = FirebaseFirestore.getInstance();




    private PaymentsClient paymentsClient;
    Button googlePayButton;
    private JSONObject transactionInfo = new JSONObject();
    private JSONObject tokenizationSpecification = new JSONObject();
    private JSONObject cardPaymentMethod = new JSONObject();
    private JSONObject merchantInfo = new JSONObject();
    private JSONObject paymentDataRequestJson;
    PaymentDataRequest paymentDataRequest;
    private final int LOAD_PAYMENT_DATA_REQUEST_CODE = 101;

    ImageView buy;
    String info[];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_ticket);


        gi = getIntent();
        pr = gi.getStringExtra("Price");
        ownerPhone = gi.getStringExtra("ownerPhone");
        //buyerPhone = gi.getStringExtra("buyerPhone");
        buyerEmail = gi.getStringExtra("EmailBuyer");
        uploadID = gi.getStringExtra("PDF");
        info = gi.getStringArrayExtra("info");


        paymentsClient = createPaymentsClient(this);
        price = findViewById(R.id.price111);
        buy = findViewById(R.id.buyTicket);
        priceGoogle = gi.getStringExtra("PriceforGoogle");


        IsReadyToPayRequest readyToPayRequest = IsReadyToPayRequest.fromJson(googlePayBaseConfiguration.toString());

        Task<Boolean> readyToPayTask = paymentsClient.isReadyToPay(readyToPayRequest);
        readyToPayTask.addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                try {
                    if (task.getResult(ApiException.class) != null) {
                        setGooglePayAvailable(task.getResult(ApiException.class));
                    }
                } catch (ApiException exception) {
                    // Error determining readiness to use Google Pay.
                    // Inspect the logs for more details.
                }
            }
        });





        price.setText("Press 'buy' to buy the tickets in \n" +pr );
        createNotiChannel();






        FirebaseDatabase.getInstance().getReference().child("uploadsPDF").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String s = snapshot.child(uploadID).child("url").getValue().toString();


                saveData(s);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





    }






    private void sendMassage(){
        /**
         * The function sends a message to two users, a ticket seller and a ticket buyer
         */

        String sms = "your ticket sold successfully!\n -tickets";
    String sms2 = pdfUrl;
    String sms3 = "here is your ticket:\n you can see it also in personal zone in tickets app";

    String phone = info[11];
    SmsManager smsManager = SmsManager.getDefault();
    smsManager.sendTextMessage(ownerPhone,null,sms,null,null);
    smsManager.sendTextMessage(phone,null,sms3,null,null);
    smsManager.sendTextMessage(phone,null,sms2,null,null);


    }



    private void saveData(String s){
        /**
         * The function save information from Listener function
         * @param String string of url
         */
        pdfUrl = s;}

    private void noti(){
        /**
         * The function sets a scheduled alert
         */
        Intent intent = new Intent(BuyTicket_Activity.this,Notification_receiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(BuyTicket_Activity.this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);


        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.HOUR_OF_DAY,20);
        cal.set(Calendar.MINUTE,00);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis() ,pendingIntent);

    }

    private void createNotiChannel(){
        /**
         * The function defines a channel that activates the scheduled alert
         */

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "TicChannel";
            String description = "Channel for tic";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("tic",name,importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

        }
    }

    public void buy2() {
        /**
         * The function deletes the ticket from the store of tickets for purchase,
         * and activates the functions that send an alert and a message to the user.
         * The user is then taken to the main screen

         */


        //send email to email owner that his ticket sold and he will get the money
        //send pdf in email to email buyer

        db.collection("TicketInfo").document(uploadID).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        sendMassage();
                        noti();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
        si = new Intent(this,MainActivity.class);

        startActivity(si);
    }

    public void back(View view) {
        /**
         * Returns the user to the main screen

         */
        si = new Intent(this,MainActivity.class);

        startActivity(si);
    }


    private PaymentsClient createPaymentsClient(Context context) {
        /**
         * Creating a google pay client
         */
        Wallet.WalletOptions walletOptions = new Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST).build();
        return Wallet.getPaymentsClient(context, walletOptions);
    }

    private JSONObject baseCardPaymentMethod = new JSONObject();

    {
        try {
            baseCardPaymentMethod.put("type", "CARD");

            JSONObject parameters = new JSONObject();
            parameters.put("allowedCardNetworks", new JSONArray(Arrays.asList("VISA", "MASTERCARD")));
            parameters.put("allowedAuthMethods", new JSONArray(Arrays.asList("PAN_ONLY", "CRYPTOGRAM_3DS")));

            baseCardPaymentMethod.put("parameters", parameters);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject googlePayBaseConfiguration = new JSONObject();

    {
        try {
            googlePayBaseConfiguration.put("apiVersion", 2);
            googlePayBaseConfiguration.put("apiVersionMinor", 0);

            JSONArray allowedPaymentMethods = new JSONArray();
            allowedPaymentMethods.put(baseCardPaymentMethod);

            googlePayBaseConfiguration.put("allowedPaymentMethods", allowedPaymentMethods);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setGooglePayAvailable(Boolean available) {
        /**
         * Enables the purchase after connecting the customer
         */
        if (available) {
            buy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    requestPayment();


                }
            });
        } else {
            // Unable to pay using Google Pay. Update your UI accordingly.
        }
    }

    private void requestPayment() {
        /**
         * Using Google Pay to set up the purchase details
         */
        try {
            tokenizationSpecification.put("type", "PAYMENT_GATEWAY");
            JSONObject parameters = new JSONObject();
            parameters.put("gateway", "example");
            parameters.put("gatewayMerchantId", "exampleGatewayMerchantId");
            tokenizationSpecification.put("parameters", parameters);
            try {
                transactionInfo.put("totalPrice", priceGoogle);
                transactionInfo.put("totalPriceStatus", "FINAL");
                transactionInfo.put("currencyCode", "NIS");

                try {
                    cardPaymentMethod.put("type", "CARD");
                    cardPaymentMethod.put("tokenizationSpecification", tokenizationSpecification);
                    JSONObject parameters2 = new JSONObject();
                    parameters2.put("allowedCardNetworks", new JSONArray(Arrays.asList("VISA", "MASTERCARD")));
                    parameters2.put("allowedAuthMethods", new JSONArray(Arrays.asList("PAN_ONLY", "CRYPTOGRAM_3DS")));
                    parameters2.put("billingAddressRequired", true);
                    JSONObject billingAddressParameters = new JSONObject();
                    billingAddressParameters.put("format", "FULL");
                    parameters2.put("billingAddressParameters", billingAddressParameters);
                    cardPaymentMethod.put("parameters", parameters2);

                    try {
                        merchantInfo.put("merchantName", "Example Merchant");
                        merchantInfo.put("merchantId", "01234567890123456789");

                        try {
                            paymentDataRequestJson = new JSONObject(googlePayBaseConfiguration.toString());
                            paymentDataRequestJson.put("allowedPaymentMethods", new JSONArray().put(cardPaymentMethod));
                            paymentDataRequestJson.put("transactionInfo", transactionInfo);
                            paymentDataRequestJson.put("merchantInfo", merchantInfo);

                            try {
                                paymentDataRequest = PaymentDataRequest.fromJson(paymentDataRequestJson.toString());
                                AutoResolveHelper.resolveTask(
                                        paymentsClient.loadPaymentData(paymentDataRequest),
                                        this,
                                        LOAD_PAYMENT_DATA_REQUEST_CODE);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    PaymentData paymentData = PaymentData.getFromIntent(data);
                    if (paymentData != null) {
                        handlePaymentSuccess(paymentData);
                    }
                    break;

                case Activity.RESULT_CANCELED:
                    // The user cancelled without selecting a payment method.
                    break;

                case AutoResolveHelper.RESULT_ERROR:
                    Status status = AutoResolveHelper.getStatusFromIntent(data);
                    if (status != null) {
                        Toast.makeText(this, status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                    }
                    break;

                default:
                    // Unexpected resultCode.
                    break;
            }
        }
    }

    private void handlePaymentSuccess(PaymentData paymentData) {
        /**
         * When the purchase was made successfully,
         * the function calls the functions that implement the purchase in the application
         */
        addToTable();
        removeFromTable();
        buy2();
    }

    private void addToTable(){
        /**
         * Adds the ticket to the user's list of tickets
         */
        Map<String,String> map1= createMap();
        String s1 = map1.get("uploadID");
        FirebaseFirestore.getInstance().collection("UserTickets").document(s1).set(map1)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Toast.makeText(BuyTicket_Activity.this, "saved successfully", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Toast.makeText(BuyTicket_Activity.this, "error", Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private Map<String,String> createMap(){
        /**
         * Stores the information in a data structure
         * @return	Map<String,String>  - field name, data
         */
        Map<String,String> one= new HashMap<>();
        one.put("name",info[0]);
        one.put("place",info[1]);
        one.put("date",info[2]);
        one.put("price" , info[4]);
        one.put("amount" , info[5]);
        one.put("uploadID",info[7]);
        one.put("userEmail",buyerEmail);

        return one;
    }

    private void removeFromTable(){
        /**
         * Deleted the card from the users' card database
         */
        db.collection("TicketsInfo").document(info[7]).delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        //Toast.makeText(BuyTicket_Activity.this, "added to manager", Toast.LENGTH_SHORT).show();

                    }
                });
    }




}