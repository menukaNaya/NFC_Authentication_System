package com.auth.nfc.goingreader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends Activity
{

    //To store NDEF message details...
    private ArrayList<String> messageReceivedArray = new ArrayList<>();

    //Text view for showing the response...
    private ImageView responseImage;
    private TextView responseText;

    private NfcAdapter mNfcAdapter;

    //Simple database for testing...
    private HashMap<String,String> userEmails = new HashMap<>();
    private ArrayList<String> passwords = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initializing adapter and Text view...
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        responseImage = (ImageView) findViewById(R.id.image);
        responseText = (TextView) findViewById(R.id.textView1);

        //Populating the database...
        userEmails.put("mnw@tiqri.com", "Menuka Nayandeepa");
        userEmails.put("sva@tiqri.com", "Shehan Venderputt");
        passwords.add("bahiya");
        passwords.add("yakadaya");

        //Check if NFC is available on device...
        if (mNfcAdapter == null)
        {
            Toast.makeText(this, "NFC not available on this device",
                    Toast.LENGTH_SHORT).show();
        }

        //Handling a new intent...
        if (getIntent().getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED))
        {
            authentacation(getIntent());
        }
    }

    /*This function handles the nfc event..
    *   This will checkout, the person who send the message can be authenticated or not!
     *   It will show the response message accordingly to the sender... */

    private void authentacation(Intent NfcIntent)
    {

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(NfcIntent.getAction()))
        {
            Parcelable[] receivedArray =
                    NfcIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (receivedArray != null)
            {
                //Clear the array before storing a new message...
                messageReceivedArray.clear();

                //Converting to a NDEF message and getting received recrord into an array...
                NdefMessage receivedMessage = (NdefMessage) receivedArray[0];
                NdefRecord[] attachedRecords = receivedMessage.getRecords();

                for (NdefRecord record : attachedRecords)
                {
                    String string = new String(record.getPayload());
                    //Make sure we don't pass along our AAR (Android Applicatoin Record)
                    if (string.equals(getPackageName()))
                    {
                        continue;
                    }
                    messageReceivedArray.add(string);
                }

                /*Toast.makeText(this, "Received " + messageReceivedArray.size() +
                        " Messages", Toast.LENGTH_LONG).show();*/

                authenticationProcess();

            }else
            {
                /*Toast.makeText(this, "Received Blank Parcel", Toast.LENGTH_LONG).show();*/
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        authentacation(intent);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        authentacation(getIntent());
    }

    private void authenticationProcess()
    {

        if(userEmails.containsKey(messageReceivedArray.get(0)) && passwords.contains(messageReceivedArray.get(1)))
        {
            responseImage.setImageResource(R.drawable.approved_green);
            responseText.setText("Hi " + userEmails.get(messageReceivedArray.get(0)) + " !");
            new CountDownTimer(3000, 1000) {

                public void onTick(long millisUntilFinished) {
                    //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                }

                public void onFinish() {
                    responseImage.setImageResource(R.drawable.passivevsactive);
                    responseText.setText("Tap Me using your Mobile!");
                }
            }.start();

        }else
        {
            responseImage.setImageResource(R.drawable.oops);
            responseText.setTextColor(Color.YELLOW);
            responseText.setText("Sorry!\nYour not allowed");

            new CountDownTimer(3000, 1000) {

                public void onTick(long millisUntilFinished) {
                    //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                }

                public void onFinish() {
                    responseImage.setImageResource(R.drawable.passivevsactive);
                    responseText.setTextColor(Color.WHITE);
                    responseText.setText("Tap Me using your Mobile!");
            }
            }.start();

        }
    }


}
