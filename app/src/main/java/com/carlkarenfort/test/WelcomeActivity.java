package com.carlkarenfort.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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

    Context context = this;
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

            //set values in store data
            StoreData.setUntisID(423); //tbd
            StoreData.setUntisUsername(untisUserName.getText().toString());
            StoreData.setUntisPassword(untisPassword.getText().toString());
            StoreData.setUntisServer(StoreData.returnServerFromURL(untisURL.getText().toString()));
            StoreData.setUntisSchool(StoreData.returnSchoolFromURL(untisURL.getText().toString()));

            StoreData.storeData(context);
            //TODO("go to MainActivity")
        });

    }
}