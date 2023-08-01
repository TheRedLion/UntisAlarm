package com.carlkarenfort.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity {
    String TAG = "WelcomeActivity";

    EditText foreName;
    EditText longName;
    EditText untisURL;
    EditText untisPassword;
    EditText untisUserName;
    TextView warningText;
    Button runButton;
    Intent intent;

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
        warningText = findViewById(R.id.warningText);
        runButton = findViewById(R.id.runButton);

        runButton.setOnClickListener((v)->{
            //getID from foreName and longName
            if (foreName.getText().toString().isEmpty()) {
                //show warning
                warningText.setTextColor(Color.rgb(244,67,54));
                warningText.setText("Fore Name may not be Empty!");
            } else if (longName.getText().toString().isEmpty()) {
                //show warning
                warningText.setTextColor(Color.rgb(244,67,54));
                warningText.setText("Last Name may not be Empty!");
            } else if (untisURL.getText().toString().isEmpty()) {
                //show warning
                warningText.setTextColor(Color.rgb(244,67,54));
                warningText.setText("Webuntis URL may not be Empty!");
            } else if (untisUserName.getText().toString().isEmpty()) {
                //show warning
                warningText.setTextColor(Color.rgb(244,67,54));
                warningText.setText("Username may not be Empty!");
            } else if (untisPassword.getText().toString().isEmpty()) {
                //show warning
                warningText.setTextColor(Color.rgb(244, 67, 54));
                warningText.setText("Password may not be Empty!");
            } else {
                //all fields were filled
                //get data from url
                String untisServer = StoreData.returnServerFromURL(untisURL.getText().toString());
                String untisSchool = StoreData.returnSchoolFromURL(untisURL.getText().toString());
                if (untisServer == null) {
                    //invalid url
                    warningText.setTextColor(Color.rgb(244, 67, 54));
                    warningText.setText("Invalid URL format!");
                } else if (untisSchool == null) {
                    //invalid url
                    warningText.setTextColor(Color.rgb(244, 67, 54));
                    warningText.setText("Invalid URL format!");
                } else {
                    //get ID
                    ApiCalls apiCalls = new ApiCalls();
                    apiCalls.setForeName(foreName.getText().toString());
                    apiCalls.setLongName(longName.getText().toString());
                    Thread thread = new Thread(apiCalls);
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Integer untisID = apiCalls.getUntisID();
                    untisID = 123;
                    //show warning if no ID was found
                    if (untisID == 0) {
                        //no match was found
                        warningText.setTextColor(Color.rgb(244, 67, 54));
                        warningText.setText("No user matching your Fore and Last Name!");
                    } else {
                        //id was found
                        //store data
                        Log.i(TAG, "setting values and calling store data");
                        StoreData.storeUntisData(context, untisID, untisUserName.getText().toString(), untisPassword.getText().toString(), untisServer, untisSchool);

                        //go to MainActvity
                        intent = new Intent(WelcomeActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
            }
        });

    }
}