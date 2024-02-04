/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This Class is responsible for requesting school names from Webuntis.
 */
package eu.karenfort.main.api

import android.util.Log
import eu.karenfort.main.helper.COROUTINE_EXCEPTION_HANDLER
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class WebApiCalls {
    companion object {
        const val TAG = "WebApiCalls"
        const val TOO_MANY_RESULTS = "too many results"
    }

    suspend fun getUntisSchools(searchSchoolString: String): Array<Array<String>>? {
        Log.i(TAG, "getting Schools")

        if (searchSchoolString.length < 3) { //query probably wont ever succeed with 2 letters
            return null
        }

        val url = URL("https://mobile.webuntis.com/ms/schoolquery2")
        val connection = withContext(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER) {
            url.openConnection()
        } as HttpURLConnection

        //parameters found in html of webuntis.com
        val data = """{
        "id": "wu_schulsuche-1697008128606",
        "method": "searchSchool",
        "params": [{"search": "$searchSchoolString"}],
        "jsonrpc": "2.0"
        }"""
        connection.requestMethod = "POST"
        connection.setRequestProperty("Accept", "application/json, text/plain, */*")
        connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br")
        connection.setRequestProperty("Accept-Language", "de-DE,de;q=0.9,en-US;q=0.8,en;q=0.7")
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Origin", "https://webuntis.com")
        connection.setRequestProperty("Referer", "https://webuntis.com")
        connection.setRequestProperty("Sec-Ch-Ua-Mobile", "?0")
        connection.setRequestProperty("Sec-Fetch-Dest", "empty")
        connection.setRequestProperty("Sec-Fetch-Mode", "cors")
        connection.setRequestProperty("Sec-Fetch-Site", "same-site")
        connection.setRequestProperty(
            "Content-Length",
            data.toByteArray(StandardCharsets.UTF_8).size.toString()
        )
        connection.doOutput = true

        val os: OutputStream = connection.outputStream
        withContext(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER) {
            os.write(data.toByteArray(StandardCharsets.UTF_8))
            os.close()
        }

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (withContext(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER) {
                    reader.readLine()
                }.also { line = it } != null) {
                response.append(line)
            }
            withContext(Dispatchers.IO + COROUTINE_EXCEPTION_HANDLER) {
                reader.close()
            }

            val jsonResponseStr = response.toString()
            if (jsonResponseStr == "{\"id\":\"wu_schulsuche-1697008128606\",\"error\":{\"code\":-6003,\"message\":\"too many results\"},\"jsonrpc\":\"2.0\"}") {
                return arrayOf(arrayOf(TOO_MANY_RESULTS))
            }

            connection.disconnect()

            Log.i(TAG, parseSchoolData(JSONObject(jsonResponseStr)).toString())

            return parseSchoolData(JSONObject(jsonResponseStr))
        } else {
            println("Request failed with response code: $responseCode")
            return null
        }
    }

    private fun parseSchoolData(json: JSONObject): Array<Array<String>>? {
        if (!json.has("result")) {
            return null //null means there was some kind of error with the request
        }

        val result = json.getJSONObject("result")
        if (result.length() <= 0) {
            return emptyArray() //empty array means there is no school
        }

        val schools = result.getJSONArray("schools")
        val schoolData = Array(schools.length()) { Array(4) { "" } }

        for (i in 0 until schools.length()) {
            val school = schools.getJSONObject(i)
            schoolData[i][0] = school.optString("displayName")
            schoolData[i][1] = school.optString("address")
            schoolData[i][2] = school.optString("server")
            schoolData[i][3] = school.optString("loginName")
        }

        return schoolData
    }
}