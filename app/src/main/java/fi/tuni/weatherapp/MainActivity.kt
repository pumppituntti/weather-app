package fi.tuni.weatherapp

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    lateinit var input: EditText
    lateinit var result_header: TextView
    lateinit var result_info: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        input = findViewById(R.id.input)
        result_header = findViewById(R.id.result_header)
        result_info = findViewById(R.id.result_info)

        val button = findViewById<Button>(R.id.button_find)
        button.setOnClickListener {
            hideKeyboard()
            if (input.text.isEmpty()) {
                Log.d("hello", "here")
                Toast.makeText(this, "Field should not be empty!", Toast.LENGTH_LONG).show()
            } else {
                result_header.text = "Loading..."
                val city = input.text.toString()
                downloadUrlAsync(
                    this,
                    "https://api.openweathermap.org/data/2.5/weather?q=${city}&units=metric&appid=223d2e7247b5a5b808c39b7c173269ae"
                ) {
                    val mp = ObjectMapper()
                    val myObject: WeatherObject = mp.readValue(it, WeatherObject::class.java)
                    Log.d("hello", myObject.name.toString())
                    Log.d("hello", myObject.wind?.speed.toString())
                    Log.d("hello", myObject.weather?.get(0)?.main.toString())
                    Log.d("hello", input.text.toString())
                    result_header.text = "Weather in ${myObject.name.toString()}"
                    result_info.text =
                        "${myObject.weather?.get(0)?.main.toString()}, ${myObject.main?.temp} °C\n" +
                                "(feels like ${myObject.main?.feels_like.toString()} °C)\n" +
                                "Wind: ${myObject.wind?.speed.toString()}"
                }
            }
        }
    }

    fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
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
data class WeatherInfo(
    var main: String? = null,
    var description: String? = null,
    var icon: String? = null
) {}

@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherObject(
    var name: String? = null,
    var weather: MutableList<WeatherInfo>? = null,
    var main: WeatherMain? = null,
    var wind: WeatherWind? = null
) {}