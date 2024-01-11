/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This activity lets the user select a school and log in.
 */
package eu.karenfort.main.ui

import android.os.Bundle
import android.os.StrictMode
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.setFragmentResultListener
import com.carlkarenfort.test.R
import com.google.android.material.textfield.TextInputLayout
import eu.karenfort.main.api.UntisApiCalls
import eu.karenfort.main.api.WebApiCalls
import eu.karenfort.main.extentions.isDisabled
import eu.karenfort.main.extentions.isOnline
import eu.karenfort.main.extentions.toast
import eu.karenfort.main.helper.ALLOW_NETWORK_ON_MAIN_THREAD
import eu.karenfort.main.helper.COROUTINE_EXCEPTION_HANDLER
import eu.karenfort.main.helper.StoreData
import eu.karenfort.main.helper.ensureBackgroundCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException


class WelcomeActivity : AppCompatActivity() {
    private lateinit var untisSchool: EditText
    private lateinit var untisPassword: EditText
    private lateinit var untisUserName: EditText
    private lateinit var runButton: Button
    private lateinit var schoolSelector: AutoCompleteTextView
    private lateinit var untisSchoolInputLayout: TextInputLayout
    private lateinit var untisUserNameInputLayout: TextInputLayout
    private lateinit var untisPasswordInputLayout: TextInputLayout
    private lateinit var untisSelectInputLayout: TextInputLayout
    private var schools: Array<Array<String>>? = null
    private var schoolName: String? = null
    private var server: String? = null
    private var untisApiCalls: UntisApiCalls? = null

    companion object {
        private const val TAG = "WelcomeActivity"
    }
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
        setEnterOnPasswordListener()
    }

    private fun setEnterOnPasswordListener() {
        untisPassword.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    untisPassword.clearFocus()
                    handleRunButtonPressed()
                    return true
                }
                return false
            }
        })
    }

    private fun setRunButtonListener() {
        runButton.setOnClickListener { _: View? ->
            handleRunButtonPressed()
        }
    }

    private fun handleRunButtonPressed() {
        runButton.isDisabled = true
        CoroutineScope(Dispatchers.Default).launch {
            if (!isOnline()) {
                this@WelcomeActivity.toast(getString(R.string.you_are_offline))
                runOnUiThread{ runButton.isDisabled = false }
                return@launch
            }

            if (untisSchool.text.toString().isEmpty()) {
                //show warning
                runOnUiThread{
                    untisSchoolInputLayout.error = getString(R.string.may_not_be_empty)
                    runButton.isDisabled = false
                }
                return@launch
            }
            runOnUiThread{ untisSchoolInputLayout.error = null }

            if (untisUserName.text.toString().isEmpty()) {
                //show warning
                runOnUiThread{
                    untisUserNameInputLayout.error = getString(R.string.may_not_be_empty)
                    runButton.isDisabled = false
                }
                return@launch
            }
            runOnUiThread{ untisUserNameInputLayout.error = null }

            if (untisPassword.text.toString().isEmpty()) {
                //show warning
                runOnUiThread{
                    untisPasswordInputLayout.error = getString(R.string.may_not_be_empty)
                    runButton.isDisabled = false
                }
                return@launch
            }
            runOnUiThread{ untisPasswordInputLayout.error = null }

            //check if school was selected
            if (schoolName == null || server == null) {
                runOnUiThread{
                    untisSelectInputLayout.error = getString(R.string.school_must_be_selected)
                    runButton.isDisabled = false
                }
                return@launch
            }
            runOnUiThread{ untisSelectInputLayout.error = null }

            //verify login data
            if (untisApiCalls == null) {
                if (!verifyLoginData()) {
                    runOnUiThread{ runButton.isDisabled = false }
                    return@launch
                }
            }
            runOnUiThread{
                untisPasswordInputLayout.error = null
                untisUserNameInputLayout.error = null
            }

            val untisID =
                untisApiCalls!!.getID() //!! valid since untisApiCalls is never set to null in code

            if (untisID == null) {
                //no match was found
                runOnUiThread{
                    untisPasswordInputLayout.error = getString(R.string.invalid_login_data)
                    untisUserNameInputLayout.error = getString(R.string.invalid_login_data)
                    runButton.isDisabled = false
                }
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

            runOnUiThread{
                val firstSettingDialogFragment = FirstSettingDialogFragment()
                firstSettingDialogFragment.show(supportFragmentManager, FirstSettingDialogFragment.TAG)
                firstSettingDialogFragment.setFragmentResultListener(FirstSettingDialogFragment.DISMISSED) { _, _ ->
                    runButton.isDisabled = false
                }
            }
        }
    }

    private fun verifyLoginData(): Boolean {
        if (server == null || schoolName == null || untisUserName.text.isNullOrEmpty() || untisPassword.text.isNullOrEmpty()) {
            return false
        }
        try {
            StrictMode.setThreadPolicy(ALLOW_NETWORK_ON_MAIN_THREAD)
            ensureBackgroundCoroutine{
                untisApiCalls = UntisApiCalls(
                    untisUserName.text.toString(),
                    untisPassword.text.toString(),
                    server!!,
                    schoolName!!
                )
            }
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
        schoolSelector.setOnItemClickListener { _: AdapterView<*>, _: View, position: Int, _: Long ->
            try {
                untisSelectInputLayout.helperText = schools!![position][1]
                schoolName = schools!![position][3]
                server = schools!![position][2]
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
                        runOnUiThread{ untisSchoolInputLayout.error = getString(R.string.you_are_offline) }
                        return@launch
                    }

                    val webApiCalls = WebApiCalls()
                    schools = webApiCalls.getUntisSchools("$text")

                    if (schools == null) {
                        runOnUiThread{
                            untisSchoolInputLayout.error =
                                getString(R.string.error_when_searching_for_schools)
                        }
                        return@launch
                    }

                    if (schools!!.isEmpty()) {
                        runOnUiThread{ untisSchoolInputLayout.error = getString(R.string.no_school_found) }
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

                    runOnUiThread{ untisSelectInputLayout.hint = getString(R.string.select_school) }

                    try{
                        val schoolNames =
                            schools!!.map { "${it[0]}, ${it[1].split(',')[1].trim()}" }
                                .toTypedArray()
                        val arrayAdapter =
                            ArrayAdapter(this@WelcomeActivity, R.layout.dropdown_menu, schoolNames)
                        val autocompleteTV = schoolSelector
                        runOnUiThread {
                            autocompleteTV.setAdapter(arrayAdapter)
                        }

                        Log.i(TAG, "Size ${schools!!.size} ${schools!![0][0]}")
                        //if there is exactly one result
                        if (schools!!.size == 1 && schools!![0][0] != WebApiCalls.TOO_MANY_RESULTS) {
                            runOnUiThread{
                                //select the only result
                                schoolSelector.setText(
                                    schoolSelector.adapter.getItem(0).toString(),
                                    false
                                )
                                schoolSelector.setSelection(0)
                                schoolSelector.listSelection = 0
                                schoolSelector.performCompletion()

                                //store the data of this result
                                untisSelectInputLayout.helperText = schools!![0][1]
                                schoolName = schools?.get(0)?.get(3)
                                server = schools?.get(0)?.get(2)
                            }

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
        untisSchool = findViewById(R.id.untis_school)
        untisUserName = findViewById(R.id.untis_username)
        untisPassword = findViewById(R.id.untis_password)
        runButton = findViewById(R.id.run_button)
        schoolSelector = findViewById(R.id.school_selector)
        untisPasswordInputLayout = findViewById(R.id.untis_password_input_layout)
        untisSchoolInputLayout = findViewById(R.id.untis_school_input_layout)
        untisUserNameInputLayout = findViewById(R.id.untis_username_input_layout)
        untisSelectInputLayout = findViewById(R.id.select_school_input_layout)
    }
}