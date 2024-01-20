/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This Fragment is shown after the user has logged in. The user is then able to
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
import com.carlkarenfort.test.databinding.FragmentFirstSettingBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import eu.karenfort.main.extentions.isDisabled
import eu.karenfort.main.extentions.toast
import eu.karenfort.main.extentions.viewBinding
import eu.karenfort.main.helper.StoreData

class FirstSettingDialogFragment : DialogFragment() {
    private lateinit var context: Context
    private val binding: FragmentFirstSettingBinding by viewBinding(FragmentFirstSettingBinding::inflate)
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
        binding.skipButton.setOnClickListener { goToMainActivity() }
        binding.confirmButton.setOnClickListener {
            handleConfirmButtonPressed()
        }
        binding.tbsInputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(text: Editable?) {
                if (text.isNullOrEmpty()) {
                    binding.confirmButton.isDisabled = true
                    return
                }
                binding.confirmButton.isDisabled = false
            }
        })
        binding.tbsInputField.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    binding.tbsInputField.clearFocus()
                    handleConfirmButtonPressed()
                    return true
                }
                return false
            }
        })
    }

    private fun handleConfirmButtonPressed() {
        val newTBSStr = binding.tbsInputField.text.toString()
        binding.tbsInputField.setText("")
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