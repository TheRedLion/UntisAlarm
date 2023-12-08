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
import androidx.core.widget.addTextChangedListener
import eu.karenfort.main.StoreData
import eu.karenfort.main.api.UntisApiCalls
import eu.karenfort.main.api.WebApiCalls
import com.carlkarenfort.test.R
import com.google.android.material.textfield.TextInputLayout
import eu.karenfort.main.helper.ALLOW_NETWORK_ON_MAIN_THREAD
import eu.karenfort.main.helper.isOnline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.IndexOutOfBoundsException


class WelcomeActivity : AppCompatActivity() {

    private var TAG: String = "WelcomeActivity"

    private lateinit var untisSchool: EditText
    private lateinit var untisPassword: EditText
    private lateinit var untisUserName: EditText
    private lateinit var runButton: Button
    private lateinit var intent: Intent
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var untisSchoolInputLayout: TextInputLayout
    private lateinit var untisUserNameInputLayout: TextInputLayout
    private lateinit var untisPasswordInputLayout: TextInputLayout
    private lateinit var untisSelectInputLayout: TextInputLayout
    private var schools: Array<Array<String>>? = null
    private var schoolName: String? = null
    private var server: String? = null
    private var untisApiCalls: UntisApiCalls? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        getLayoutObjectsByID()
        setListener()
    }

    private fun setListener() {
        schoolFieldListener()
        selectionListener()
        runButtonListener()
    }

    private fun runButtonListener() {
        runButton.setOnClickListener { _: View? ->

            if (!isOnline()) {
                Toast.makeText(this, getString(R.string.you_are_offline), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (untisSchool.text.toString().isEmpty()) {
                //show warning
                untisSchoolInputLayout.error = getString(R.string.may_not_be_empty)
                return@setOnClickListener
            }
            untisSchoolInputLayout.error = null

            if (untisUserName.text.toString().isEmpty()) {
                //show warning
                untisUserNameInputLayout.error = getString(R.string.may_not_be_empty)
                return@setOnClickListener
            }
            untisUserNameInputLayout.error = null

            if (untisPassword.text.toString().isEmpty()) {
                //show warning
                untisPasswordInputLayout.error = getString(R.string.may_not_be_empty)
                return@setOnClickListener
            }
            untisPasswordInputLayout.error = null

            //check if school was selected
            if (schoolName == null || server == null) {
                untisSelectInputLayout.error = getString(R.string.school_must_be_selected)
                return@setOnClickListener
            }
            untisSelectInputLayout.error = null

            //verify login data
            if (untisApiCalls == null) {
                if (!verifyLoginData()) return@setOnClickListener
            }
            untisPasswordInputLayout.error = null
            untisUserNameInputLayout.error = null

            val untisID = untisApiCalls!!.getID() //!! valid since untisApiCalls is never set to null in code

            if (untisID == null) {
                //no match was found
                untisPasswordInputLayout.error = getString(R.string.invalid_login_data)
                untisUserNameInputLayout.error = getString(R.string.invalid_login_data)
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

    private fun verifyLoginData(): Boolean {
        StrictMode.setThreadPolicy(ALLOW_NETWORK_ON_MAIN_THREAD)
        if (server == null || schoolName == null || untisUserName.text.isNullOrEmpty() || untisPassword.text.isNullOrEmpty()) {
            return false
        }
        try {
            untisApiCalls = UntisApiCalls(
                untisUserName.text.toString(),
                untisPassword.text.toString(),
                server!!, //!! should be fine
                schoolName!! //hopefully
            )
        } catch (e: IOException) {
            Log.i(TAG, "login failed")
        }

        if (untisApiCalls == null) {
            runOnUiThread {
                untisPasswordInputLayout.error = getString(R.string.invalid_login_data)
                untisUserNameInputLayout.error = getString(R.string.invalid_login_data)
            }
            return false
        }
        return true
    }

    private fun selectionListener() {
        autoCompleteTextView.setOnItemClickListener { _: AdapterView<*>, _: View, position: Int, _: Long ->
            try {
                untisSelectInputLayout.helperText = schools?.get(position)?.get(1)
                schoolName = schools?.get(position)?.get(3)
                server = schools?.get(position)?.get(2)
            } catch (e: IndexOutOfBoundsException) {
                untisSelectInputLayout.helperText = getString(R.string.unable_to_load_address)
            }
            untisSelectInputLayout.error = null
        }
    }

    private fun schoolFieldListener() {
        // TODO: 2 fields is unnecessary 1 field better - true but how
        untisSchool.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(text: Editable?) {
                Log.i(TAG, "text changed")

                if (text.isNullOrEmpty()) {
                    untisSchoolInputLayout.error = getString(R.string.may_not_be_empty)
                    return
                }
                untisSchoolInputLayout.error = null

                if (!isOnline()) {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.you_are_offline),
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                CoroutineScope(Dispatchers.IO).launch {
                    Log.i(TAG, "started coroutine")
                    val webApiCalls = WebApiCalls()
                    schools = webApiCalls.getSchools("$text")
                    Log.i(TAG, schools.toString())

                    if (schools == null) {
                        Log.i(TAG, "schools is null")
                        return@launch
                    }
                    Log.i(TAG, "schools is not null")

                    //check if there are too many results
                    if (!schools!!.isEmpty()) {
                        if (!schools!![0].isEmpty()) {
                            if (schools!![0][0] == "too many results") {
                                Log.i(TAG, "too many results")
                                runOnUiThread {
                                    untisSelectInputLayout.hint = "Too many results"
                                }
                                return@launch
                            }
                        }
                    }
                    //there are not
                    val schoolNames =
                        schools!!.map { "${it[0]}, ${it[1].split(',')[1].trim()}" }.toTypedArray()

                    Log.i(TAG, schoolNames.toString())
                    val arrayAdapter =
                        ArrayAdapter(applicationContext, R.layout.dropdown_menu, schoolNames)
                    val autocompleteTV = autoCompleteTextView
                    runOnUiThread {
                        autocompleteTV.setAdapter(arrayAdapter)
                    }
                }
            }
        })
        untisPassword.onFocusChangeListener = View.OnFocusChangeListener { _, b ->
            Log.i(TAG, "focus changed")
            if(b){
                //entered in the edit text
                Log.i(TAG, "enterd edittext")
            } else {
                //left edit text
                Log.i(TAG, "left edit untisUsername")
                if (!untisUserName.text.isNullOrEmpty() && !untisPassword.text.isNullOrEmpty()) {
                    Log.i(TAG, "veryfiying login data")
                    CoroutineScope(Dispatchers.IO).launch {
                        verifyLoginData()
                    }
                }
            }
        }
        untisUserName.onFocusChangeListener = View.OnFocusChangeListener { _, b ->
            Log.i(TAG, "focus changed")
            if(b){
                //entered in the edit text
                Log.i(TAG, "entered edit username")
            } else {
                //left edit text
                Log.i(TAG, "left edit untisUsername")
                if (!untisUserName.text.isNullOrEmpty() && !untisPassword.text.isNullOrEmpty()) {
                    Log.i(TAG, "veryfiying login data")
                    CoroutineScope(Dispatchers.IO).launch {
                        verifyLoginData()
                    }
                }
            }
        }
        untisPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(text: Editable?) {
                if (text.isNullOrEmpty()) {
                    untisPasswordInputLayout.error = getString(R.string.may_not_be_empty)
                    return
                }
                untisPasswordInputLayout.error = null
                untisUserNameInputLayout.error = null
            }
        })
        untisUserName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(text: Editable?) {
                if (text.isNullOrEmpty()) {
                    getString(R.string.may_not_be_empty)
                    return
                }
                untisPasswordInputLayout.error = null
                untisUserNameInputLayout.error = null
            }
        })
    }

    private fun getLayoutObjectsByID() {
        untisSchool = findViewById(R.id.untisSchool)
        untisUserName = findViewById(R.id.untisUsername)
        untisPassword = findViewById(R.id.untisPassword)
        runButton = findViewById(R.id.runButton)
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView)
        untisPasswordInputLayout = findViewById(R.id.untis_password_input_layout)
        untisSchoolInputLayout = findViewById(R.id.untis_school_input_layout)
        untisUserNameInputLayout = findViewById(R.id.untis_username_input_layout)
        untisSelectInputLayout = findViewById(R.id.select_school_input_layout)
    }
}