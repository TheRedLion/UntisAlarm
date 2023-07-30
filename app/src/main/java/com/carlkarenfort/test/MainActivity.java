package com.carlkarenfort.test;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";
    Button setNewUser;
    TextView timeBeforeSchool;
    EditText setTBS;
    Button updateTBS;
    TextView alarmPreview;
    Switch toggleAlarm;

    Context context = this;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Alarm Settings");

        setNewUser = findViewById(R.id.addNewUser);
        timeBeforeSchool = findViewById(R.id.timeBeforeSchool);
        setTBS = findViewById(R.id.setTBS);
        updateTBS = findViewById(R.id.updateTBS);
        alarmPreview = findViewById(R.id.alarmPreview);
        toggleAlarm = findViewById(R.id.toggleAlarm);


        setNewUser.setOnClickListener((v)->{
                intent = new Intent(MainActivity.this, WelcomeActivity.class);
                startActivity(intent);
        });

        //set listener for setting a new TBS
        updateTBS.setOnClickListener((v)->{
            //temp logging
            StoreData.loadData(context);
            Log.i(TAG,StoreData.getUntisID().toString());
            Log.i(TAG,StoreData.getUntisServer());
            Log.i(TAG,StoreData.getUntisSchool());
            Log.i(TAG,StoreData.getUntisUsername());
            Log.i(TAG,StoreData.getUntisPassword());


            //when clicked
            Log.i(TAG,"updating TBS");

            //get new TBS
            //something should be what is currently entered in TBS field, also clear field afterwards
            Integer newTBS = 60; //temp value
            //val newTBS = updateTBS.getcurrentposition

            //update displayedTBS

            //update alarm preview and alarm tomorrow

        //store data

        //TODO("setting a new TBS")
        });

    }
}