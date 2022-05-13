package fi.tuni.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button_find)
        button.setOnClickListener {
            downloadUrlAsync(this, "https://api.openweathermap.org/data/2.5/weather?q=tampere&units=metric&appid=223d2e7247b5a5b808c39b7c173269ae"){
                Log.d("hello", it)
                val mp = ObjectMapper()
                val myObject: WeatherObject = mp.readValue(it, WeatherObject::class.java)
                Log.d("hello", myObject.name.toString())
                Log.d("hello", myObject.wind?.speed.toString())
                Log.d("hello", myObject.weather?.get(0)?.main.toString())
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
@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherWind(var speed: String? = null) {}

@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherMain(var temp: String? = null, var feels_like: String? = null) {}

@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherInfo(var main: String? = null, var description: String? = null, var icon: String? = null) {}

@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherObject(var name: String? = null, var weather: MutableList<WeatherInfo>? = null, var main: WeatherMain? = null, var wind: WeatherWind? = null) {}