package eu.karenfort.untisAlarm.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import eu.karenfort.untisAlarm.api.UntisApiCalls
import eu.karenfort.untisAlarm.databinding.FragmentCancelledMessageInfoBinding
import eu.karenfort.untisAlarm.databinding.FragmentCancelledMessageSettingsBinding
import eu.karenfort.untisAlarm.extentions.viewBinding
import eu.karenfort.untisAlarm.helper.StoreData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CancelledMessageSettingsFragment : DialogFragment() {
    private val binding: FragmentCancelledMessageSettingsBinding by viewBinding(FragmentCancelledMessageSettingsBinding::inflate)
    private lateinit var context: Context
    private lateinit var nonNullActivity: FragmentActivity

    companion object {
        const val TAG = "CancelledMessageInfoFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nonNullActivity = activity ?: throw Error("Activity is Null")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val preList = arrayOf("Loading...")
        val preAdapter: ArrayAdapter<String> =
            ArrayAdapter(context, android.R.layout.simple_list_item_1, preList)
        binding.listView.adapter = preAdapter

        CoroutineScope(Dispatchers.Default).launch {
            val loginData: Array<String?> = StoreData(context).loadLoginData()
            val id: Int = StoreData(context).loadID() ?: return@launch

            if (loginData[0].isNullOrEmpty() || loginData[1].isNullOrEmpty() || loginData[2].isNullOrEmpty() || loginData[3].isNullOrEmpty()) {
                return@launch
            }
            val list = UntisApiCalls(
                loginData[0]!!,
                loginData[1]!!,
                loginData[2]!!,
                loginData[3]!!
            ).getCancelledMessages(id)
            val adapter: ArrayAdapter<String> =
                ArrayAdapter(context, android.R.layout.simple_list_item_1, list)

            nonNullActivity.runOnUiThread {
                binding.listView.adapter = adapter
                binding.listView.onItemClickListener =
                    AdapterView.OnItemClickListener { _, _, i, _ ->
                        StoreData(context).storeCancelledMessage(list[i])
                        Log.i(TAG, "item was selected storing: ${list[i]}")
                        dismiss()
                    }
            }
        }
        return binding.root
    }

    override fun onAttach(givenContext: Context) {
        super.onAttach(givenContext)
        context = givenContext
    }
}
/*
val lv = findViewById<ListView>(R.id.list_view)
val adapter: ArrayAdapter<String> = new (this, android. R. layout. simple 2 ist item l, eharaetersDC)
iv . setAdapter (adapter) ;
lv.setOnItemC1ickListener (new AdapterView.onItemC1ickListener () {
    @Override
    public void onltemC1iek(AdapterView < 2 > adapterView, View view, int i, long 1) {

    }
}*/