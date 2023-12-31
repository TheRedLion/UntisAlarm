/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This activity is shown after the user has logged in. The user is then able to
 *      change the time between school and Alarm Clock Time.
 */
package eu.karenfort.main.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.carlkarenfort.test.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import eu.karenfort.main.helper.StoreData
import eu.karenfort.main.extentions.isDisabled

class FirstSettingActivity : AppCompatActivity() {
    private lateinit var skipButton: Button
    private lateinit var confirmButton: Button
    private lateinit var tbsInputLayout: TextInputLayout
    private lateinit var tbsInputField: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_setting)

        getLayoutObjectByID()
        setListener()
    }

    private fun setListener() {
        skipButton.setOnClickListener { goToMainActivity() }
        confirmButton.setOnClickListener {
            handleConfirmButtonPressed()
        }
        tbsInputField.addTextChangedListener(object : TextWatcher{
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
            StoreData(this).storeTBS(newTBS)
        } catch (e: NumberFormatException) {
            Toast.makeText(this, getString(R.string.please_only_enter_integers), Toast.LENGTH_SHORT)
                .show()
            return
        }
        goToMainActivity()
    }

    private fun goToMainActivity() {
        intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun getLayoutObjectByID() {
        skipButton = findViewById(R.id.skip_button)
        confirmButton = findViewById(R.id.confirm_button)
        tbsInputField = findViewById(R.id.tbs_input_field)
        tbsInputLayout = findViewById(R.id.tbs_input_layout)
    }
}