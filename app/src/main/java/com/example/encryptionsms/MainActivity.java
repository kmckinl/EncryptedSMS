package com.example.encryptionsms;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ArrayList<String> messages = new ArrayList<>();
    ListView messagesView;
    ArrayAdapter messagesArrayView;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messagesView = (ListView) findViewById(R.id.messagesList);
        messagesArrayView = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, R.id.messagesList);
        messagesView.setAdapter(messagesArrayView);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_DENIED) {
            getPermissionToReadSMS();
        } else {
            receiveMessages();
        }


        // Listen for Send button event
        Button sendButton = (Button) findViewById(R.id.send);
        sendButton.setOnClickListener(
                view -> smsSendMessage(view)
        );
    }

    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToReadSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_SMS)) {
                Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.READ_SMS},
                    READ_SMS_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read SMS permission granted", Toast.LENGTH_SHORT).show();
                receiveMessages();
            } else {
                Toast.makeText(this, "Read SMS permission denied", Toast.LENGTH_SHORT).show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
    
    public void smsSendMessage(View view) {
        System.out.println("sent");
        TextView textView = (TextView) findViewById(R.id.phoneNumber);
        String smsNumber = String.format("smsto: %s", textView.getText().toString());
        EditText smsEditText = (EditText) findViewById(R.id.smsText);
        String sms = smsEditText.getText().toString();
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
        smsIntent.setData(Uri.parse(smsNumber));
        smsIntent.putExtra("sms_body", sms);
        messages.add(sms);
        //updateMessages();
        if (smsIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(smsIntent);

        } else {
            Log.e(TAG, "Can't resolve app for ACTION_SENDTO Intent.");
        }
    }

    public void receiveMessages() {
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = cursor.getColumnIndex("body");
        int indexAddress = cursor.getColumnIndex("address");

        if (cursor.moveToFirst()) {
            messagesArrayView.clear();
            do {
                String str = "From: " + cursor.getString(indexAddress) +
                        "\n" + cursor.getString(indexBody) + "\n";
                messagesArrayView.clear();
            } while (cursor.moveToNext());
        }
    }

    public void updateMessages() {

    }
}