package com.carlkarenfort.test

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

class WebApiCalls {
    private val TAG = "WebApiCalls"

    fun getSchools(searchSchoolString: String) {
        Log.i(TAG, "in getSchools")

        val url = URL("https://mobile.webuntis.com/ms/schoolquery2")
        val connection = url.openConnection() as HttpURLConnection

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

        os.write(data.toByteArray(StandardCharsets.UTF_8))
        os.close()

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()

            println(response.toString())
        } else {
            println("Request failed with response code: $responseCode")
        }

        connection.disconnect()
    }
}