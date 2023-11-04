package eu.karenfort.main.activities

import android.content.Intent
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
import eu.karenfort.main.Misc
import eu.karenfort.main.StoreData
import eu.karenfort.main.api.UntisApiCalls
import eu.karenfort.main.api.WebApiCalls
import com.carlkarenfort.test.R
import eu.karenfort.main.helper.ALLOW_NETWORK_ON_MAIN_THREAD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException


class WelcomeActivity : AppCompatActivity() {

    private var TAG: String = "WelcomeActivity"

    private lateinit var untisSchool: EditText
    private lateinit var untisPassword: EditText
    private lateinit var untisUserName: EditText
    private lateinit var runButton: Button
    private lateinit var intent: Intent
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var schoolAddressDisplay: TextView
    private var schools: Array<Array<String>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        getLayoutObjectsByID()

        var schoolName: String? = null
        var server: String? = null


        untisSchool.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(text: Editable?) {
                Log.i(TAG, "text changed")

                if (!Misc.isOnline(applicationContext)) {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.you_are_offline),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                //phone is online
                CoroutineScope(Dispatchers.IO).launch {
                    Log.i(TAG, "started coroutine")
                    val webApiCalls = WebApiCalls()
                    schools = webApiCalls.getSchools("$text")
                    Log.i(TAG, schools.toString())

                    if (schools == null) {
                        return@launch
                    }
                    Log.i(TAG, "schools is not null")

                    //check if there are too many results
                    if (schools!![0][0] == "too many results") {
                        Log.i(TAG, "too many results")
                        runOnUiThread {
                            Log.i(TAG, "in main coroutine")
                            autoCompleteTextView.hint = "Too many results"
                        }
                        return@launch
                    }
                    //there are not
                    val schoolNames =
                        schools!!.map { "${it[0]}, ${it[1].split(',')[1].trim()}" }.toTypedArray()

                    Log.i(TAG, schoolNames.toString())
                    runOnUiThread {
                        val arrayAdapter =
                            ArrayAdapter(applicationContext, R.layout.dropdown_menu, schoolNames)
                        // get reference to the autocomplete text view
                        val autocompleteTV = autoCompleteTextView
                        // set adapter to the autocomplete tv on the main thread
                        autocompleteTV.setAdapter(arrayAdapter)
                    }
                }
            }
        })


        autoCompleteTextView.setOnItemClickListener { _: AdapterView<*>, _: View, position: Int, _: Long ->
            schoolAddressDisplay.text = schools?.get(position)?.get(1)
            schoolName = schools?.get(position)?.get(3)
            server = schools?.get(position)?.get(2)
        }


        runButton.setOnClickListener { _: View? ->

            if (!Misc.isOnline(this)) {
                Toast.makeText(this, getString(R.string.you_are_offline), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (untisSchool.text.toString().isEmpty()) {
                //show warning
                Toast.makeText(this, getString(R.string.webuntis_url_empty), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (untisUserName.text.toString().isEmpty()) {
                //show warning
                Toast.makeText(this, getString(R.string.username_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (untisPassword.text.toString().isEmpty()) {
                //show warning
                Toast.makeText(this, getString(R.string.password_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //check if school was selected
            if (schoolName == null || server == null) {
                Toast.makeText(this, "No school selected", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //verify login data
            var untisApiCalls: UntisApiCalls? = null
            StrictMode.setThreadPolicy(ALLOW_NETWORK_ON_MAIN_THREAD)
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
                Toast.makeText(this, getString(R.string.invalid_login_data), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val untisID = untisApiCalls.getID()

            if (untisID == null) {
                //no match was found
                Toast.makeText(this, getString(R.string.no_id_found), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val storeData = StoreData(applicationContext)
            storeData.storeLoginData(
                untisUserName.text.toString(),
                untisPassword.text.toString(),
                server!!,
                schoolName!!
            )
            storeData.storeID(untisID)

            intent = Intent(this@WelcomeActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getLayoutObjectsByID() {
        untisSchool = findViewById(R.id.untisSchool)
        untisUserName = findViewById(R.id.untisUsername)
        untisPassword = findViewById(R.id.untisPassword)
        runButton = findViewById(R.id.runButton)
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView)
        schoolAddressDisplay = findViewById(R.id.schoolAddressDisplay)
    }
}