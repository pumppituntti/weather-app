package fi.tuni.weatherapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    lateinit var button: Button
    lateinit var button_location: Button
    lateinit var button_forecast: Button
    lateinit var input: EditText
    lateinit var result_header: TextView
    lateinit var result_info: TextView
    var searchByLocation: Boolean = false
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        input = findViewById(R.id.input)
        result_header = findViewById(R.id.result_header)
        result_info = findViewById(R.id.result_info)

        button = findViewById(R.id.button_find)
        button_location = findViewById(R.id.button_location)
        button_forecast = findViewById(R.id.button_forecast)
        button_location.setOnClickListener {
            fetchLocation()
            searchByLocation = true
        }
        button.setOnClickListener {
            hideKeyboard()
            if (input.text.isEmpty()) {
                Toast.makeText(this, "Field should not be empty!", Toast.LENGTH_LONG).show()
            } else {
                result_header.text = "Loading..."
                val city = input.text.toString()
                val url: String
                if (searchByLocation) {
                    url =
                        "https://api.openweathermap.org/data/2.5/weather?${input.text}&units=metric&appid=223d2e7247b5a5b808c39b7c173269ae"
                    input.text.clear()
                } else {
                    url =
                        "https://api.openweathermap.org/data/2.5/weather?q=${city}&units=metric&appid=223d2e7247b5a5b808c39b7c173269ae"
                }
                downloadUrlAsync(
                    this, url
                ) {
                    val mp = ObjectMapper()
                    val myObject: WeatherObject = mp.readValue(it, WeatherObject::class.java)
                    result_header.text = "Weather in ${myObject.name.toString()}"
                    result_info.text =
                        "${myObject.weather?.get(0)?.main.toString()}, ${myObject.main?.temp} °C\n" +
                                "(feels like ${myObject.main?.feels_like.toString()} °C)\n" +
                                "Wind: ${myObject.wind?.speed.toString()} m/s"
                }
                button_forecast.visibility = View.VISIBLE
            }
        }

        button_forecast.setOnClickListener {
            val intent = Intent(this, Forecast::class.java)
            intent.putExtra("forecast", "THIS IS FORECAST")
            startActivity(intent)
        }
    }

    private fun fetchLocation() {
        val task = fusedLocationProviderClient.lastLocation

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }
        task.addOnSuccessListener {
            if (it != null) {
                input.setText("lat=${it.latitude}&lon=${it.longitude}")
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

     fun downloadUrlAsync(
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