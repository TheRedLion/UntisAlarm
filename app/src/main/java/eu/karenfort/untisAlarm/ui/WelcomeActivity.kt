/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This activity lets the user select a school and log in.
 */
package eu.karenfort.untisAlarm.ui

import android.os.Bundle
import android.os.StrictMode
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.setFragmentResultListener
import eu.karenfort.untisAlarm.R
import eu.karenfort.untisAlarm.api.UntisApiCalls
import eu.karenfort.untisAlarm.api.WebApiCalls
import eu.karenfort.untisAlarm.databinding.ActivityWelcomeBinding
import eu.karenfort.untisAlarm.extentions.hasNetworkConnection
import eu.karenfort.untisAlarm.extentions.isDisabled
import eu.karenfort.untisAlarm.extentions.toast
import eu.karenfort.untisAlarm.extentions.viewBinding
import eu.karenfort.untisAlarm.helper.ALLOW_NETWORK_ON_MAIN_THREAD
import eu.karenfort.untisAlarm.helper.COROUTINE_EXCEPTION_HANDLER
import eu.karenfort.untisAlarm.helper.StoreData
import eu.karenfort.untisAlarm.helper.ensureBackgroundCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException


class WelcomeActivity : AppCompatActivity() {
    internal val binding: ActivityWelcomeBinding by viewBinding(ActivityWelcomeBinding::inflate)

    private var schools: Array<Array<String>>? = null
    private var schoolName: String? = null
    private var server: String? = null
    private var untisApiCalls: UntisApiCalls? = null

    companion object {
        private const val TAG = "WelcomeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setListener()
    }

    private fun setListener() {
        setSchoolFieldListener()
        setSelectionListener()
        setRunButtonListener()
        setEnterOnPasswordListener()
    }

    private fun setEnterOnPasswordListener() {
        binding.untisPassword.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    binding.untisPassword.clearFocus()
                    handleRunButtonPressed()
                    return true
                }
                return false
            }
        })
    }

    private fun setRunButtonListener() {
        binding.runButton.setOnClickListener { _: View? ->
            handleRunButtonPressed()
        }
    }

    internal fun handleRunButtonPressed() {
        binding.runButton.isDisabled = true
        CoroutineScope(Dispatchers.Default).launch {
            if (!hasNetworkConnection()) {
                this@WelcomeActivity.toast(getString(R.string.you_are_offline))
                runOnUiThread { binding.runButton.isDisabled = false }
                return@launch
            }

            if (binding.untisSchool.text.toString().isEmpty()) {
                //show warning
                runOnUiThread {
                    binding.untisSchoolInputLayout.error = getString(R.string.may_not_be_empty)
                    binding.runButton.isDisabled = false
                }
                return@launch
            }
            runOnUiThread { binding.untisSchoolInputLayout.error = null }

            if (binding.untisUserName.text.toString().isEmpty()) {
                //show warning
                runOnUiThread {
                    binding.untisUserNameInputLayout.error = getString(R.string.may_not_be_empty)
                    binding.runButton.isDisabled = false
                }
                return@launch
            }
            runOnUiThread { binding.untisUserNameInputLayout.error = null }

            if (binding.untisPassword.text.toString().isEmpty()) {
                //show warning
                runOnUiThread {
                    binding.untisPasswordInputLayout.error = getString(R.string.may_not_be_empty)
                    binding.runButton.isDisabled = false
                }
                return@launch
            }
            runOnUiThread { binding.untisPasswordInputLayout.error = null }

            //check if school was selected
            if (schoolName == null || server == null) {
                runOnUiThread {
                    binding.selectUntisSchoolInputLayout.error =
                        getString(R.string.school_must_be_selected)
                    binding.runButton.isDisabled = false
                }
                return@launch
            }
            runOnUiThread { binding.selectUntisSchoolInputLayout.error = null }

            //verify login data
            if (untisApiCalls == null) {
                if (!verifyLoginData()) {
                    runOnUiThread { binding.runButton.isDisabled = false }
                    return@launch
                }
            }
            runOnUiThread {
                binding.untisPasswordInputLayout.error = null
                binding.untisUserNameInputLayout.error = null
            }

            val untisID =
                (untisApiCalls ?: return@launch).getID()

            if (untisID == null) {
                //no match was found
                runOnUiThread {
                    binding.untisPasswordInputLayout.error = getString(R.string.invalid_login_data)
                    binding.untisUserNameInputLayout.error = getString(R.string.invalid_login_data)
                    binding.runButton.isDisabled = false
                }
                return@launch
            }

            val storeData = StoreData(this@WelcomeActivity)
            storeData.storeLoginData(
                binding.untisUserName.text.toString(),
                binding.untisPassword.text.toString(),
                server ?: return@launch,
                schoolName ?: return@launch
            )
            storeData.storeID(untisID)

            runOnUiThread {
                val firstSettingDialogFragment = FirstSettingDialogFragment()
                firstSettingDialogFragment.show(
                    supportFragmentManager,
                    FirstSettingDialogFragment.TAG
                )
                firstSettingDialogFragment.setFragmentResultListener(FirstSettingDialogFragment.DISMISSED) { _, _ ->
                    binding.runButton.isDisabled = false
                }
            }
        }
    }

    private fun verifyLoginData(): Boolean {
        if (server == null || schoolName == null || binding.untisUserName.text.isNullOrEmpty() || binding.untisPassword.text.isNullOrEmpty()) {
            return false
        }
        try {
            //StrictMode.setThreadPolicy(ALLOW_NETWORK_ON_MAIN_THREAD)
            ensureBackgroundCoroutine { //todo check that is still works
                untisApiCalls = UntisApiCalls(
                    binding.untisUserName.text.toString(),
                    binding.untisPassword.text.toString(),
                    server ?: return@ensureBackgroundCoroutine,
                    schoolName ?: return@ensureBackgroundCoroutine
                )
            }
        } catch (_: IOException) {
        } catch (_: NullPointerException) {
        }

        if (untisApiCalls == null) {
            runOnUiThread {
                binding.untisPasswordInputLayout.error = getString(R.string.invalid_login_data)
                binding.untisUserNameInputLayout.error = getString(R.string.invalid_login_data)
            }
            return false
        }
        return true
    }

    private fun setSelectionListener() {
        binding.untisSchoolSelector.setOnItemClickListener { _: AdapterView<*>, _: View, position: Int, _: Long ->
            Log.i(TAG, "position: $position")
            try {
                binding.selectUntisSchoolInputLayout.helperText = schools!![position][1]
                schoolName = schools!![position][3]
                server = schools!![position][2]
            } catch (e: IndexOutOfBoundsException) {
                binding.selectUntisSchoolInputLayout.helperText =
                    getString(R.string.unable_to_load_address)
            }
            binding.selectUntisSchoolInputLayout.error = null
        }
    }

    private fun setSchoolFieldListener() {
        binding.untisSchool.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(text: Editable?) {
                if (text.isNullOrEmpty()) {
                    binding.untisSchoolInputLayout.error = getString(R.string.may_not_be_empty)
                    return
                }

                binding.untisSchoolInputLayout.error = null

                CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
                    if (!hasNetworkConnection()) {
                        runOnUiThread {
                            binding.untisSchoolInputLayout.error =
                                getString(R.string.you_are_offline)
                        }
                        return@launch
                    }

                    val webApiCalls = WebApiCalls()
                    try {
                        schools = webApiCalls.getUntisSchools("$text")
                    } catch (_: Error) {
                        runOnUiThread {
                            binding.untisSchoolInputLayout.error =
                                getString(R.string.error_when_searching_for_schools)
                        }
                        return@launch
                    }

                    if (schools!!.isEmpty()) {
                        runOnUiThread {
                            binding.untisSchoolInputLayout.error =
                                getString(R.string.no_school_found)
                        }
                        return@launch
                    }

                    if (schools!![0].isEmpty()) {
                        return@launch
                    }

                    if (schools!![0][0] == WebApiCalls.TOO_MANY_RESULTS) {
                        runOnUiThread {
                            binding.selectUntisSchoolInputLayout.hint =
                                getString(R.string.too_many_results)
                        }
                        return@launch
                    }

                    runOnUiThread {
                        binding.selectUntisSchoolInputLayout.hint =
                            getString(R.string.select_school)
                    }

                    try {
                        val schoolNames =
                            schools!!.map { "${it[0]}, ${it[1].split(',')[1].trim()}" }
                                .toTypedArray()
                        val arrayAdapter =
                            ArrayAdapter(this@WelcomeActivity, R.layout.dropdown_menu, schoolNames)
                        val autocompleteTV = binding.untisSchoolSelector
                        runOnUiThread {
                            autocompleteTV.setAdapter(arrayAdapter)
                        }

                        Log.i(TAG, "Size ${schools!!.size} ${schools!![0][0]}")
                        //if there is exactly one result
                        if (schools!!.size == 1 && schools!![0][0] != WebApiCalls.TOO_MANY_RESULTS) {
                            runOnUiThread {
                                //select the only result
                                binding.untisSchoolSelector.setText(
                                    binding.untisSchoolSelector.adapter.getItem(0).toString(),
                                    false
                                )
                                binding.untisSchoolSelector.setSelection(0)
                                binding.untisSchoolSelector.listSelection = 0
                                binding.untisSchoolSelector.performCompletion()

                                //store the data of this result
                                binding.selectUntisSchoolInputLayout.helperText = schools!![0][1]
                                schoolName = schools?.get(0)?.get(3)
                                server = schools?.get(0)?.get(2)
                            }

                        }
                    } catch (_: ArrayIndexOutOfBoundsException) {
                    }
                }
            }
        })
        binding.untisPassword.onFocusChangeListener = View.OnFocusChangeListener { _, b ->
            if (b) {
                //entered in the edit text
            } else {
                //left edit text
                if (!binding.untisUserName.text.isNullOrEmpty() && !binding.untisPassword.text.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
                        verifyLoginData()
                    }
                }
            }
        }
        binding.untisUserName.onFocusChangeListener = View.OnFocusChangeListener { _, b ->
            if (b) {
                //entered in the edit text
            } else {
                //left edit text
                if (!binding.untisUserName.text.isNullOrEmpty() && !binding.untisPassword.text.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER).launch {
                        verifyLoginData()
                    }
                }
            }
        }
        binding.untisPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(text: Editable?) {
                if (text.isNullOrEmpty()) {
                    binding.untisPasswordInputLayout.error = getString(R.string.may_not_be_empty)
                    return
                }
                binding.untisPasswordInputLayout.error = null
                binding.untisUserNameInputLayout.error = null
            }
        })
        binding.untisUserName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(text: Editable?) {
                if (text.isNullOrEmpty()) {
                    binding.untisUserName.error = getString(R.string.may_not_be_empty)
                    return
                }
                binding.untisPasswordInputLayout.error = null
                binding.untisUserNameInputLayout.error = null
            }
        })
    }
}