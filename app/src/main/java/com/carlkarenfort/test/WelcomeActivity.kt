package com.carlkarenfort.test

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.runBlocking


class WelcomeActivity : AppCompatActivity() {
    private lateinit var foreName: EditText
    private lateinit var longName: EditText
    private lateinit var untisURL: EditText
    private lateinit var untisPassword: EditText
    private lateinit var untisUserName: EditText
    private lateinit var warningText: TextView
    private lateinit var runButton: Button
    private lateinit var intent: Intent
    private var TAG: String = "WelcomeActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        foreName = findViewById(R.id.foreName);
        longName = findViewById(R.id.longName);
        untisURL = findViewById(R.id.untisURL);
        untisUserName = findViewById(R.id.untisUsername);
        untisPassword = findViewById(R.id.untisPassword);
        warningText = findViewById(R.id.warningText);
        runButton = findViewById(R.id.runButton);

        runButton.setOnClickListener { v: View? ->
            val redHex: String = "F44336"
            //getID from foreName and longName
            if (foreName.text.toString().isEmpty()) {
                //show warning
                warningText.setTextColor(Color.rgb(244, 67, 54))
                warningText.text = "Fore Name may not be Empty!"
            } else if (longName.text.toString().isEmpty()) {
                //show warning
                warningText.setTextColor(Color.rgb(244, 67, 54))
                warningText.text = "Last Name may not be Empty!"
            } else if (untisURL.text.toString().isEmpty()) {
                //show warning
                warningText.setTextColor(Color.rgb(244, 67, 54))
                warningText.text = "Webuntis URL may not be Empty!"
            } else if (untisUserName.text.toString().isEmpty()) {
                //show warning
                warningText.setTextColor(Color.rgb(244, 67, 54))
                warningText.text = "Username may not be Empty!"
            } else if (untisPassword.text.toString().isEmpty()) {
                //show warning
                warningText.setTextColor(Color.rgb(244, 67, 54))
                warningText.text = "Password may not be Empty!"
            } else {
                //all fields were filled
                //get data from url
                val untisServer: String =
                    ExtractURLData.returnServerFromURL(untisURL.text.toString())
                val untisSchool: String =
                    ExtractURLData.returnSchoolFromURL(untisURL.text.toString())
                if (untisServer == null) {
                    //invalid url
                    warningText.setTextColor(Color.rgb(244, 67, 54))
                    warningText.text = "Invalid URL format!"
                } else if (untisSchool == null) {
                    //invalid url
                    warningText.setTextColor(Color.rgb(244, 67, 54))
                    warningText.text = "Invalid URL format!"
                } else {
                    //get ID
                    val apiCalls = ApiCalls()
                    apiCalls.foreName = foreName.text.toString()
                    apiCalls.longName = longName.text.toString()
                    val thread = Thread(apiCalls)
                    thread.start()
                    try {
                        thread.join()
                    } catch (e: InterruptedException) {
                        throw RuntimeException(e)
                    }
                    var untisID = apiCalls.untisID
                    untisID = 123
                    //show warning if no ID was found
                    if (untisID == 0) {
                        //no match was found
                        warningText.setTextColor(Color.rgb(244, 67, 54))
                        warningText.text = "No user matching your Fore and Last Name!"
                    } else {
                        //id was found
                        //store data
                        Log.i(TAG, "setting values and calling store data")
                        var storeData = StoreData(applicationContext)
                        runBlocking {
                            storeData.storeLoginData(
                                untisUserName.text.toString(),
                                untisPassword.text.toString(),
                                untisServer,
                                untisSchool
                            )
                        }

                        //go to MainActvity
                        intent = Intent(this@WelcomeActivity, MainActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }

    }
}