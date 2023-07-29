package com.carlkarenfort.test

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.carlkarenfort.test.ui.theme.TestTheme

private const val TAG = "MainActivity"
class MainActivity : ComponentActivity() {
    private lateinit var foreName: EditText
    private lateinit var longName: EditText
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var button: Button
    private lateinit var out: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        foreName = findViewById(R.id.foreNameField)
        longName = findViewById(R.id.longNameField)
        username = findViewById(R.id.untisUsernameField)
        password = findViewById(R.id.untisPasswordField)
        button = findViewById(R.id.runButton)
        out = findViewById(R.id.output)
        //.
        button.setOnClickListener{
            Log.i(TAG,"button clicked")
            //Get api data
            makeAPIcall()
        }


    }

    private fun makeAPIcall() {
        //set logindata
        //ApiCalls.setUsername(username.text.toString())
        //ApiCalls.setPassword(password.text.toString())
        //ApiCalls.setServer()

        //test login data to test api
        ApiCalls.setUsername("KarenfCar")
        ApiCalls.setPassword("Mytimetable1!")
        ApiCalls.setServer("https://nessa.webuntis.com")
        ApiCalls.setSchoolName("gym-beskidenstrasse")
        ApiCalls.setUntisID(436)
        //make API call
        ApiCalls.APIcall()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TestTheme {
        Greeting("Android")
    }
}