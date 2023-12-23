/**
 * Project: https://github.com/TheRedLion/UntisAlarm
 *
 * Licence: GNU GENERAL PUBLIC LICENSE Version 3, 29 June 2007
 *
 * Description: This Class is responsible for requesting school names from Websites.
 */
package eu.karenfort.main.api

import android.util.Log
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
    private val TAG = "WebApiCalls"

    suspend fun getSchools(searchSchoolString: String): Array<Array<String>>? {
        Log.i(TAG, "in getSchools")

        val url = URL("https://mobile.webuntis.com/ms/schoolquery2")
        val connection = withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpURLConnection

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
        connection.setRequestProperty("Content-Length", data.toByteArray(StandardCharsets.UTF_8).size.toString())

        connection.doOutput = true
        val os: OutputStream = connection.outputStream

        withContext(Dispatchers.IO) {
            os.write(data.toByteArray(StandardCharsets.UTF_8))
        }
        withContext(Dispatchers.IO) {
            os.close()
        }

        Log.i(TAG, "in getSchools2")

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (withContext(Dispatchers.IO) {
                    reader.readLine()
                }.also { line = it } != null) {
                response.append(line)
            }
            withContext(Dispatchers.IO) {
                reader.close()
            }

            val jsonResponse = response.toString()

            if (jsonResponse == "{\"id\":\"wu_schulsuche-1697008128606\",\"error\":{\"code\":-6003,\"message\":\"too many results\"},\"jsonrpc\":\"2.0\"}") {
                return arrayOf(arrayOf("too many results"))
            }
            val schoolData = parseSchoolData(jsonResponse)

            connection.disconnect()

            return schoolData
        } else {
            println("Request failed with response code: $responseCode")
            return null
        }
    }

    private fun parseSchoolData(jsonResponse: String): Array<Array<String>> {
        Log.i(TAG, "parseSchoolData")
        val json = JSONObject(jsonResponse)

        if (json.has("result")) {
            val result = json.getJSONObject("result")
            if (result.length() > 0) {
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
            } else {
                // Handle the case where "result" is an empty array
                return emptyArray()
            }
        } else {
            // Handle the case where "result" is missing in the JSON
            return emptyArray()
        }
    }
}