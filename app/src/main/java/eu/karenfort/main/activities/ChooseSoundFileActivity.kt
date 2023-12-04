package eu.karenfort.main.activities

import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.carlkarenfort.test.R
import eu.karenfort.main.StoreData


class ChooseSoundFileActivity : AppCompatActivity() {
    private val TAG = "ChooseSoundFileActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_sound_file)

        val getSoundFile = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) {
            Log.i(TAG, it.toString())

            if (it == null) {
                Toast.makeText(this, getString(R.string.no_file_was_selected), Toast.LENGTH_SHORT).show()
            }

            val resolver = this.contentResolver
            val returnCursor: Cursor = resolver.query(it!!, null, null, null, null)!!
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            val name = returnCursor.getString(nameIndex)
            returnCursor.close()

            StoreData(this).storeSound(name, it)
            intent = Intent(this@ChooseSoundFileActivity, SettingsActivity::class.java)
            startActivity(intent)
        }
        Log.i(TAG, "launching")
        getSoundFile.launch("audio/*")
    }
}