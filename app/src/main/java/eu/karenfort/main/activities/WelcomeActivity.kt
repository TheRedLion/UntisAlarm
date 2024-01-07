/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This activity lets the user select a school and log in.
 */
package eu.karenfort.main.activities

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.carlkarenfort.test.R
import com.google.android.material.textfield.TextInputLayout
import eu.karenfort.main.helper.StoreData
import eu.karenfort.main.api.UntisApiCalls
import eu.karenfort.main.api.WebApiCalls
import eu.karenfort.main.helper.ALLOW_NETWORK_ON_MAIN_THREAD
import eu.karenfort.main.helper.COROUTINE_EXCEPTION_HANDLER
import eu.karenfort.main.extentions.isOnline
import eu.karenfort.main.extentions.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException


class WelcomeActivity : AppCompatActivity() {
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
        setSchoolFieldListener()
        setSelectionListener()
        setRunButtonListener()
    }

    private fun setRunButtonListener() {
        runButton.setOnClickListener { _: View? ->
            CoroutineScope(Dispatchers.Default).launch{
                if (!isOnline()) {
                    this@WelcomeActivity.toast(getString(R.string.you_are_offline))
                    return@launch
                }

                if (untisSchool.text.toString().isEmpty()) {
                    //show warning
                    untisSchoolInputLayout.error = getString(R.string.may_not_be_empty)
                    return@launch
                }
                untisSchoolInputLayout.error = null

                if (untisUserName.text.toString().isEmpty()) {
                    //show warning
                    untisUserNameInputLayout.error = getString(R.string.may_not_be_empty)
                    return@launch
                }
                untisUserNameInputLayout.error = null

                if (untisPassword.text.toString().isEmpty()) {
                    //show warning
                    untisPasswordInputLayout.error = getString(R.string.may_not_be_empty)
                    return@launch
                }
                untisPasswordInputLayout.error = null

                //check if school was selected
                if (schoolName == null || server == null) {
                    untisSelectInputLayout.error = getString(R.string.school_must_be_selected)
                    return@launch
                }
                untisSelectInputLayout.error = null

                //verify login data
                if (untisApiCalls == null) {
                    if (!verifyLoginData()) return@launch
                }
                untisPasswordInputLayout.error = null
                untisUserNameInputLayout.error = null

                val untisID =
                    untisApiCalls!!.getID() //!! valid since untisApiCalls is never set to null in code

                if (untisID == null) {
                    //no match was found
                    untisPasswordInputLayout.error = getString(R.string.invalid_login_data)
                    untisUserNameInputLayout.error = getString(R.string.invalid_login_data)
                    return@launch
                }

                val storeData = StoreData(this@WelcomeActivity)
                storeData.storeLoginData(
                    untisUserName.text.toString(),
                    untisPassword.text.toString(),
                    server!!,
                    schoolName!!
                )
                storeData.storeID(untisID)
                intent = Intent(this@WelcomeActivity, FirstSettingActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun verifyLoginData(): Boolean { //todo move to different coroutine
        if (server == null || schoolName == null || untisUserName.text.isNullOrEmpty() || untisPassword.text.isNullOrEmpty()) {
            return false
        }
        try {
            StrictMode.setThreadPolicy(ALLOW_NETWORK_ON_MAIN_THREAD)
            untisApiCalls = UntisApiCalls(
                untisUserName.text.toString(),
                untisPassword.text.toString(),
                server!!,
                schoolName!!
            )
        } catch (_: IOException) {
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

    private fun setSelectionListener() {
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

    private fun setSchoolFieldListener() {
        untisSchool.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(text: Editable?) {
                if (text.isNullOrEmpty()) {
                    untisSchoolInputLayout.error = getString(R.string.may_not_be_empty)
                    return
                }

                untisSchoolInputLayout.error = null

                CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
                    if (!isOnline()) {
                        untisSchoolInputLayout.error = getString(R.string.you_are_offline)
                        return@launch
                    }

                    val webApiCalls = WebApiCalls()
                    schools = webApiCalls.getUntisSchools("$text")

                    if (schools == null) {
                        untisSchoolInputLayout.error = getString(R.string.error_when_searching_for_schools)
                        return@launch
                    }

                    if (schools!!.isEmpty()) {
                        untisSchoolInputLayout.error = getString(R.string.no_school_found)
                        return@launch
                    }

                    if (schools!![0].isEmpty()) {
                        return@launch
                    }

                    if (schools!![0][0] == WebApiCalls.TOO_MANY_RESULTS) {
                        runOnUiThread {
                            untisSelectInputLayout.hint = getString(R.string.too_many_results)
                        }
                        return@launch
                    }

                    untisSelectInputLayout.hint = getString(R.string.select_school)
                    //there are not
                    try{
                        val schoolNames =
                            schools!!.map { "${it[0]}, ${it[1].split(',')[1].trim()}" }
                                .toTypedArray()
                        val arrayAdapter =
                            ArrayAdapter(this@WelcomeActivity, R.layout.dropdown_menu, schoolNames)
                        val autocompleteTV = autoCompleteTextView
                        runOnUiThread {
                            autocompleteTV.setAdapter(arrayAdapter)
                        }
                    } catch (_: ArrayIndexOutOfBoundsException) {
                    }
                }
            }
        })
        untisPassword.onFocusChangeListener = View.OnFocusChangeListener { _, b ->
            if(b){
                //entered in the edit text
            } else {
                //left edit text
                if (!untisUserName.text.isNullOrEmpty() && !untisPassword.text.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
                        verifyLoginData()
                    }
                }
            }
        }
        untisUserName.onFocusChangeListener = View.OnFocusChangeListener { _, b ->
            if(b){
                //entered in the edit text
            } else {
                //left edit text
                if (!untisUserName.text.isNullOrEmpty() && !untisPassword.text.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
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
                    untisUserName.error = getString(R.string.may_not_be_empty)
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