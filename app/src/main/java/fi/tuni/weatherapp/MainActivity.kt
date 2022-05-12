package fi.tuni.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.fasterxml.jackson.databind.ObjectMapper
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            downloadUrlAsync(this, "https://api.openweathermap.org/data/2.5/weather?q=tampere&appid=223d2e7247b5a5b808c39b7c173269ae"){
                Log.d("hello", it)

            }
        }

    }


    private fun downloadUrlAsync(
        activity: AppCompatActivity,
        url: String,
        callback: (result: String) -> Unit
    ) {
        var line: String
        thread {
            val httpURLConnection = URL(url).openConnection() as HttpURLConnection
            val reader = BufferedReader(InputStreamReader(httpURLConnection.inputStream))
            line = reader.readLine()
            httpURLConnection.disconnect()
            activity.runOnUiThread {
                callback(line)
            }
        }
    }
}