package com.carlkarenfort.test

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException


class WelcomeActivity : AppCompatActivity() {
    //tag for logging
    private var TAG: String = "WelcomeActivity"

    private lateinit var untisURL: EditText
    private lateinit var untisPassword: EditText
    private lateinit var untisUserName: EditText
    private lateinit var runButton: Button
    private lateinit var intent: Intent

    private var policy: StrictMode.ThreadPolicy =  StrictMode.ThreadPolicy.Builder().permitAll().build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        untisURL = findViewById(R.id.untisURL)
        untisUserName = findViewById(R.id.untisUsername)
        untisPassword = findViewById(R.id.untisPassword)
        runButton = findViewById(R.id.runButton)

        runButton.setOnClickListener { _ : View? ->
            val misc = Misc()

            if (!misc.isOnline(this)) {
                Toast.makeText(this, getString(R.string.you_are_offline), Toast.LENGTH_SHORT).show()
            } else {
                //getID from foreName and longName
                if (untisURL.text.toString().isEmpty()) {
                    //show warning
                    Toast.makeText(this, getString(R.string.webuntis_url_empty), Toast.LENGTH_SHORT).show()
                } else if (untisUserName.text.toString().isEmpty()) {
                    //show warning
                    Toast.makeText(this, getString(R.string.username_empty), Toast.LENGTH_SHORT).show()
                } else if (untisPassword.text.toString().isEmpty()) {
                    //show warning
                    Toast.makeText(this, getString(R.string.password_empty), Toast.LENGTH_SHORT).show()
                } else {
                    //all fields were filled
                    //get data from url
                    val untisServer: String =
                        ExtractURLData.returnServerFromURL(untisURL.text.toString())
                    val untisSchool: String =
                        ExtractURLData.returnSchoolFromURL(untisURL.text.toString())
                    if (untisServer == "") {
                        //invalid url
                        Toast.makeText(this, getString(R.string.invalid_url_format), Toast.LENGTH_SHORT).show()
                    } else if (untisSchool == "") {
                        //invalid url
                        Toast.makeText(this, getString(R.string.invalid_url_format), Toast.LENGTH_SHORT).show()
                    } else {
                        //verify login data
                        var untisApiCalls: UntisApiCalls? = null
                        try {
                            untisApiCalls = UntisApiCalls(
                                untisUserName.text.toString(),
                                untisPassword.text.toString(),
                                untisServer,
                                untisSchool
                            )
                        } catch (e: IOException) {
                            Log.i(TAG, "login failed")
                        }

                        if (untisApiCalls == null) {
                            //Log.i(TAG, "invalid")
                            Toast.makeText(this, getString(R.string.invalid_login_data), Toast.LENGTH_SHORT).show()
                        } else {
                            //Log.i(TAG, "valid")
                            //get ID
                            StrictMode.setThreadPolicy(policy)
                            val untisID = untisApiCalls.getID()


                            //show warning if no ID was found
                            if (untisID == null) {
                                //no match was found
                                Log.i(TAG, "error, user doen't have an ID???")
                                //TODO("error handling???")
                            } else {
                                //id exists

                                //store data
                                val storeData = StoreData(applicationContext)
                                CoroutineScope(Dispatchers.IO).launch {
                                    storeData.storeLoginData(
                                        untisUserName.text.toString(),
                                        untisPassword.text.toString(),
                                        untisServer,
                                        untisSchool
                                    )
                                    storeData.storeID(untisID)
                                }


                                //Log.i(TAG, apiCalls.getSchoolStartForDay(untisUserName.text.toString(), untisPassword.text.toString(), untisServer, untisSchool, untisID, LocalDate.of(2023,6,20)).toString())
                                //go to MainActivity
                                intent = Intent(this@WelcomeActivity, MainActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
        }
    }
}