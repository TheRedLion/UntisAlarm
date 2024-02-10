/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This Fragment is shown after the user has logged in. The user is then able to
 *      change the time between school and Alarm Clock Time.
 */
package eu.karenfort.untisAlarm.ui

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
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import eu.karenfort.untisAlarm.R
import eu.karenfort.untisAlarm.databinding.FragmentFirstSettingBinding
import eu.karenfort.untisAlarm.extentions.isDisabled
import eu.karenfort.untisAlarm.extentions.toast
import eu.karenfort.untisAlarm.helper.StoreData

class FirstSettingDialogFragment : DialogFragment() {
    private lateinit var context: Context
    private lateinit var binding: FragmentFirstSettingBinding

    companion object {
        const val TAG: String = "FirstSettingDialogFragment"
        const val DISMISSED: String = "dismissed"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this.getContext() ?: throw Error("Context is Null")
        binding = FragmentFirstSettingBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        setListener()
        return binding.root
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

    internal fun handleConfirmButtonPressed() {
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