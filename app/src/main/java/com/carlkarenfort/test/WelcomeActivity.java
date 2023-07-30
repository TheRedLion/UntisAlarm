package com.carlkarenfort.test;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

public class WelcomeActivity extends AppCompatActivity {
    String TAG = "WelcomeActivity";

    EditText foreName;
    EditText longName;
    EditText untisURL;
    EditText untisPassword;
    EditText untisUserName;
    Button runButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Log.i(TAG,"in onCreate");

        foreName = findViewById(R.id.foreName);
        longName = findViewById(R.id.longName);
        untisURL = findViewById(R.id.untisURL);
        untisUserName = findViewById(R.id.untisUsername);
        untisPassword = findViewById(R.id.untisPassword);
        runButton = findViewById(R.id.runButton);

        runButton.setOnClickListener((v)->{
            //getID from foreName and longName
            Log.i(TAG,foreName.toString());
            Log.i(TAG,foreName.toString());
            //set values in store data
            //StoreData.setUntisID(0) //tbd
            //StoreData.setUntisUsername(untisUserName.)
            //StoreData.setUntisPassword(untisPassword.toString())
            //StoreData.setUntisServer(StoreData.returnServerFromURL(untisURL.))

            //TODO("store data in sharedPrefs")

            //TODO("go to MainActivity")
        });

    }
}