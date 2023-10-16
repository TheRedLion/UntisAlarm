package com.carlkarenfort.test

import android.content.Intent
import android.icu.text.Transliterator.Position
import android.os.Bundle
import android.os.StrictMode
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException


class WelcomeActivity : AppCompatActivity() {
    //tag for logging
    private var TAG: String = "WelcomeActivity"

    private lateinit var untisSchool: EditText
    private lateinit var untisPassword: EditText
    private lateinit var untisUserName: EditText
    private lateinit var runButton: Button
    private lateinit var intent: Intent
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var schoolAdressDisplay: TextView

    private var policy: StrictMode.ThreadPolicy =  StrictMode.ThreadPolicy.Builder().permitAll().build()

    private var schools: Array<Array<String>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        untisSchool = findViewById(R.id.untisSchool)
        untisUserName = findViewById(R.id.untisUsername)
        untisPassword = findViewById(R.id.untisPassword)
        runButton = findViewById(R.id.runButton)
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView)
        schoolAdressDisplay = findViewById(R.id.schoolAdressDisplay)

        var schoolName: String? = null
        var server: String? = null

        val misc = Misc()

        untisSchool.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(text: Editable?) {
                if (!misc.isOnline(applicationContext)) {
                    Toast.makeText(applicationContext, getString(R.string.you_are_offline), Toast.LENGTH_SHORT).show()
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val webApiCalls = WebApiCalls()
                        schools = webApiCalls.getSchools("$text")
                        if (schools != null) {
                            val schoolNames = schools!!.map { it[0] }.toTypedArray()
                            runOnUiThread {
                                val arrayAdapter = ArrayAdapter(applicationContext, R.layout.dropdown_menu, schoolNames)
                                // get reference to the autocomplete text view
                                val autocompleteTV = findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
                                // set adapter to the autocomplete tv on the main thread
                                autocompleteTV.setAdapter(arrayAdapter)
                            }
                        }
                    }
                }
            }
        })


        autoCompleteTextView.setOnItemClickListener { adapter: AdapterView<*>, view: View, position: Int, id: Long ->
            Toast.makeText(applicationContext, "sel pos: ${position}", Toast.LENGTH_SHORT).show()
            schoolAdressDisplay.text = schools?.get(position)?.get(1)
            schoolName = schools?.get(position)?.get(3)
            server = schools?.get(position)?.get(2)
        }


        runButton.setOnClickListener { _ : View? ->

            if (!misc.isOnline(this)) {
                Toast.makeText(this, getString(R.string.you_are_offline), Toast.LENGTH_SHORT).show()
            } else if (untisSchool.text.toString().isEmpty()) {
                //show warning
                Toast.makeText(this, getString(R.string.webuntis_url_empty), Toast.LENGTH_SHORT).show()
            } else if (untisUserName.text.toString().isEmpty()) {
                //show warning
                Toast.makeText(this, getString(R.string.username_empty), Toast.LENGTH_SHORT).show()
            } else if (untisPassword.text.toString().isEmpty()) {
                //show warning
                Toast.makeText(this, getString(R.string.password_empty), Toast.LENGTH_SHORT).show()
            } else if (schoolName == null || server == null) {
                Toast.makeText(this, "No school selected", Toast.LENGTH_SHORT).show()
            } else {
                //verify login data
                var untisApiCalls: UntisApiCalls? = null
                StrictMode.setThreadPolicy(policy)
                try {
                    untisApiCalls = UntisApiCalls(
                        untisUserName.text.toString(),
                        untisPassword.text.toString(),
                        server!!,
                        schoolName!!
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
                                server!!,
                                schoolName!!
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