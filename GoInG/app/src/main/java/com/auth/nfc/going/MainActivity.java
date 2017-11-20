package com.auth.nfc.going;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends Activity
        implements NfcAdapter.OnNdefPushCompleteCallback,
        NfcAdapter.CreateNdefMessageCallback {

    private NfcAdapter mNfcAdapter;
    private EditText emailText;
    private EditText pwText;
    private Button signIn;
    private TextView dateAndTime;

    private ArrayList<String> messagesToSendArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        emailText = (EditText) findViewById(R.id.editText1);
        pwText = (EditText) findViewById(R.id.editText2);
        signIn = (Button) findViewById(R.id.sign_in_btn);
        dateAndTime = (TextView) findViewById(R.id.date_time_tapped);

        //Check if NFC is available on device...
        if (mNfcAdapter != null) {

            //This will refer back to createNdefMessage for what it will send...
            mNfcAdapter.setNdefPushMessageCallback(this, this);

            //This will be called if the message is sent successfully...
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this);
        } else {
            Toast.makeText(this, "NFC not available on this device",
                    Toast.LENGTH_SHORT).show();
        }

    }

    public void signIn(View v){
        String email = emailText.getText().toString();
        String pw = pwText.getText().toString();

        if(email.isEmpty() && pw.isEmpty()){
            Toast.makeText(this, "Please Enter your Details!",
                    Toast.LENGTH_SHORT).show();
            messagesToSendArray.clear();
        }else if(email.isEmpty() && !pw.isEmpty()){
            Toast.makeText(this, "Please Enter your Email Address",
                    Toast.LENGTH_SHORT).show();
            messagesToSendArray.clear();
        }else if(!email.isEmpty() && pw.isEmpty()){
            Toast.makeText(this, "Please Enter your Password",
                    Toast.LENGTH_SHORT).show();
            messagesToSendArray.clear();
        }else{
            if(messagesToSendArray.size()==2){
                messagesToSendArray.clear();
                messagesToSendArray.add(email);
                messagesToSendArray.add(pw);
            }else{
                messagesToSendArray.add(email);
                messagesToSendArray.add(pw);
            }
            Toast.makeText(this, "You are now Ready. Please tap your device on reader device",
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {

        //This will be called when another NFC capable device is detected...
        if (messagesToSendArray.size() == 0) {
            return null;
        }

        //Creating the NDEF msg using createRecord method...
        //When creating an NdefMessage we need to provide an NdefRecord[]...

        NdefRecord[] recordsToAttach = createRecords();

        return new NdefMessage(recordsToAttach);
    }

    public NdefRecord[] createRecords() {
        NdefRecord[] records = new NdefRecord[messagesToSendArray.size()];
        //To Create Messages Manually if API is less than
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            for (int i = 0; i < messagesToSendArray.size(); i++) {
                byte[] payload = messagesToSendArray.get(i).
                        getBytes(Charset.forName("UTF-8"));
                NdefRecord record = new NdefRecord(
                        NdefRecord.TNF_WELL_KNOWN,      //Our 3-bit Type name format...
                        NdefRecord.RTD_TEXT,            //Description of our payload...
                        new byte[0],                    //The optional id for our Record...
                        payload);                       //Our payload for the Record...

                records[i] = record;
            }
        }
        //Api is high enough that we can use createMime, which is preferred.
        else {
            for (int i = 0; i < messagesToSendArray.size(); i++) {
                byte[] payload = messagesToSendArray.get(i).
                        getBytes(Charset.forName("UTF-8"));

                NdefRecord record = NdefRecord.createMime("text/plain", payload);
                records[i] = record;
            }
        }
        //records[messagesToSendArray.size()] = NdefRecord.createApplicationRecord(getPackageName());
        return records;
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        /*This is called when the system detects that our NdefMessage was
        Successfully sent*/

        String currentDateTime = DateFormat.getDateTimeInstance().format(new Date());
//        Toast.makeText(this, "Your tap time is " + currentDateTime , Toast.LENGTH_LONG).show();
        dateAndTime.setText("Your tap time is " +currentDateTime);

    }

    //Save our Array Lists of Messages for if the user navigates away...
    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putStringArrayList("messagesToSend", messagesToSendArray);
    }

    //Load our Array Lists of Messages for when the user navigates back...
    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        messagesToSendArray = savedInstanceState.getStringArrayList("messagesToSend");
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
