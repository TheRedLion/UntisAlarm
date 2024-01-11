/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This activity is shown after the user has logged in. The user is then able to
 *      change the time between school and Alarm Clock Time.
 */
package eu.karenfort.main.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.carlkarenfort.test.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import eu.karenfort.main.extentions.isDisabled
import eu.karenfort.main.extentions.toast
import eu.karenfort.main.helper.StoreData

class FirstSettingDialogFragment : DialogFragment() {
    private lateinit var skipButton: Button
    private lateinit var confirmButton: Button
    private lateinit var tbsInputLayout: TextInputLayout
    private lateinit var tbsInputField: TextInputEditText
    private lateinit var context: Context

    companion object {
        const val TAG = "FirstSettingDialogFragment"
        const val DISMISSED = "dismissed"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this.getContext()?: throw Error("Context is Null")
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_first_setting, container)
        skipButton = view.findViewById(R.id.skip_button)
        confirmButton = view.findViewById(R.id.confirm_button)
        tbsInputField = view.findViewById(R.id.tbs_input_field)
        tbsInputLayout = view.findViewById(R.id.tbs_input_layout)
        setListener()
        return view
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setFragmentResult(DISMISSED, Bundle())
    }

    private fun setListener() {
        skipButton.setOnClickListener { goToMainActivity() }
        confirmButton.setOnClickListener {
            handleConfirmButtonPressed()
        }
        tbsInputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(text: Editable?) {
                if (text.isNullOrEmpty()) {
                    confirmButton.isDisabled = true
                    return
                }
                confirmButton.isDisabled = false
            }
        })
        tbsInputField.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    tbsInputField.clearFocus()
                    handleConfirmButtonPressed()
                    return true
                }
                return false
            }
        })
    }

    private fun handleConfirmButtonPressed() {
        val newTBSStr = tbsInputField.text.toString()
        tbsInputField.setText("")
        val newTBS: Int
        try {
            newTBS = Integer.parseInt(newTBSStr)
            StoreData(context).storeTBS(newTBS)
        } catch (e: NumberFormatException) {
            context.toast(getString(R.string.please_only_enter_integers))
            return
        }
        goToMainActivity()
    }

    private fun goToMainActivity() {
        val intent = Intent(context, MainActivity::class.java)
        startActivity(intent)
    }
}