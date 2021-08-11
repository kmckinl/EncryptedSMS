package com.example.encryptionsms;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.telephony.SmsManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> messages = new ArrayList<>();
    private ListView messagesView;
    private ArrayAdapter messagesArrayView;
    private EditText phoneNumber, smsText;
    private Button send, encrypt, updateContact;

    private String deviceNumber;

    private boolean encrypted = false;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference deviceColRef;

    private Cipher cipher, decipher;
    private SecretKey key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // This isn't a great method as not all devices will allow this and not all phones will store the
        // correct number (if the number changed it may still hold the last number)
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                deviceNumber = tm.getLine1Number();
            } else {
                requestPermissions(new String[] { Manifest.permission.READ_PHONE_STATE } , 1);
            }
        }

        // If this is the first time the user enters the app, the user needs to be created in the database:
        deviceColRef = db.collection(deviceNumber);
        deviceColRef.document("init").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists()) {
                        Map<String, String> init = new HashMap<>();
                        init.put("key", "init");
                        deviceColRef.document("init").set(init)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(getApplicationContext(), "Successfully Created Collection", Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        //Toast.makeText(getApplicationContext(), "User already created", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        // ArrayAdapter usage from Adam Sinicki - www.androidauthority.com/how-to-create-an-sms-app-721438/
        messagesView = (ListView) findViewById(R.id.messagesList);
        messagesArrayView = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);
        messagesView.setAdapter(messagesArrayView);

        updateContact = (Button) findViewById(R.id.updateContact);
        updateContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContact();
            }
        });

        encrypt = (Button) findViewById(R.id.Encrypt);

        encrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                encrypted = !encrypted;
                if (encrypted) {
                    encrypt.setText("Decrypt");
                } else {
                    encrypt.setText("Encrypt");
                }
                //readSMS();
                read();
            }
        });

        // Keygenerator usage from Android documentation, cipher usage from programmerworld
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            keygen.init(256);
            key = keygen.generateKey();
            cipher = Cipher.getInstance("AES");
            decipher = Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        // Checking permissions from Adam Sinicki - www.androidauthority.com/how-to-create-an-sms-app-721438/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
                //readSMS();
                read();
            } else {
                requestPermissions(new String[] { Manifest.permission.READ_SMS }, 1);
            }

            if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED) {

            } else {
                requestPermissions(new String[] { Manifest.permission.RECEIVE_SMS }, 1);
            }
        }

        phoneNumber = (EditText) findViewById(R.id.phoneNumInput);
        smsText = (EditText) findViewById(R.id.smsText);
        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Checking permissions from Adam Sinicki - www.androidauthority.com/how-to-create-an-sms-app-721438/
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                        //sendSMS();
                        send();
                    } else {
                        requestPermissions(new String[] { Manifest.permission.SEND_SMS }, 1);
                    }
                }
            }
        });
    }

    public void setContact() {
        String no = phoneNumber.getText().toString();
        Map<String, Boolean> contactInit = new HashMap<>();
        contactInit.put("solo", true);

        deviceColRef.document(no)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (!document.exists()) {
                                deviceColRef.document(no).set(contactInit)
                                        .addOnSuccessListener(new OnSuccessListener<Void>(){
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(getApplicationContext(), "Contact added Successfully", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(getApplicationContext(), "Contact already Exists", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    public void send() {
        String no = phoneNumber.getText().toString();
        String msg = smsText.getText().toString();
        SmsManager sms = SmsManager.getDefault();

        String newID = UUID.randomUUID().toString();

        deviceColRef.document(no).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            // Should implement a check for newID in the extremely small chance the id gets reused within one contact
                            if (!document.exists()) {
                                Toast.makeText(getApplicationContext(), "Please submit a recipient", Toast.LENGTH_LONG).show();
                            } else {
                                Map<String, Object> idKey = new HashMap<>();
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    idKey.put(newID, new String(Base64.getEncoder().encodeToString(key.getEncoded())));
                                }
                                deviceColRef.document(no).update(idKey)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT).show();
                                                sms.sendTextMessage(no, null, Encryption(msg) + ":-:" + newID, null, null);
                                                phoneNumber.setText("");
                                                smsText.setText("");
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed in sending", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void read() {
        // Read method altered from code from Adam Sinicki - www.androidauthority.com/how-to-create-an-sms-app-721438/
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = cursor.getColumnIndex("body");
        int indexAddress = cursor.getColumnIndex("address");

        if (cursor.moveToFirst()) {
            messagesArrayView.clear();
            do {
                String message = null;
                String id = null;
                String[] sms = cursor.getString(indexBody).split(":-:");
                if (encrypted) {
                    if (sms.length > 1) {
                        message = cursor.getString(indexBody).split(":-:")[0];
                        id = cursor.getString(indexBody).split(":-:")[1];
                        String str = "From: " + cursor.getString(indexAddress) + "\n" + message + "\n";
                        messagesArrayView.add(str);
                    }
                } else {
                    if (sms.length > 1) {
                        message = Decryption(cursor.getString(indexBody).split(":-:")[0], cursor.getString(indexAddress),
                                cursor.getString(indexBody).split(":-:")[1]);
                    }
                }
            } while (cursor.moveToNext());
            Collections.reverse(messages);
        }
    }

    private String Encryption(String string) {
        // Encryption altered from code from programmerworld.co/android/create-android-chat-message-app-with-end-to-end-aesadvanced-encryption-standard-method-in-firebase/
        byte[] messageByte = string.getBytes();

        byte[] ciphertext = new byte[0];

        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            ciphertext = cipher.doFinal(messageByte);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        String returnString = null;

        try {
            returnString = new String(ciphertext, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return returnString;
    }

    private String Decryption(String string, String sentNumber, String id) {
        // Decryption altered from code from programmerworld.co/android/create-android-chat-message-app-with-end-to-end-aesadvanced-encryption-standard-method-in-firebase/
        String decryptedString = string;

        SecretKey sharedKey = null;
        String key = null;
        db.collection(sentNumber).document(deviceNumber.substring(deviceNumber.length() - 4)).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                if (document.get(id) != null) {
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                        byte[] decodedKey = Base64.getDecoder().decode(document.get(id).toString());
                                        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

                                        byte[] EncryptedByte = new byte[0];

                                        try {
                                            EncryptedByte = string.getBytes("ISO-8859-1");
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                        String decryptedString = string;
                                        byte[] decryption;

                                        try {
                                            decipher.init(cipher.DECRYPT_MODE, originalKey);
                                            decryption = decipher.doFinal(EncryptedByte);
                                            decryptedString = new String(decryption);

                                            messagesArrayView.add("From: " + sentNumber + "\n" + decryptedString + "\n");
                                            //Toast.makeText(getApplicationContext(), decryptedString, Toast.LENGTH_SHORT).show();
                                        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                            }
                        }

                    }
                });
        //Toast.makeText(getApplicationContext(), decryptedString, Toast.LENGTH_SHORT).show();
        return decryptedString;
    }
}