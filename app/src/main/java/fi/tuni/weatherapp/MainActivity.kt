package fi.tuni.weatherapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import fi.tuni.weatherapp.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var button: Button
    private lateinit var buttonLocation: Button
    private lateinit var buttonForecast: Button
    private lateinit var input: EditText
    private lateinit var resultHeader: TextView
    private lateinit var resultInfo: TextView
    private lateinit var image: ImageView
    private lateinit var list: ListView
    private lateinit var forecastUrl: String
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMainBinding
    private var apiKey: String = "223d2e7247b5a5b808c39b7c173269ae"
    private var searchByLocation: Boolean = false
    private var isError: Boolean = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //binding for listView custom adapter
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        input = findViewById(R.id.input)
        resultHeader = findViewById(R.id.result_header)
        resultInfo = findViewById(R.id.result_info)
        image = findViewById(R.id.icon)
        list = findViewById(R.id.listView)
        button = findViewById(R.id.button_find)
        buttonLocation = findViewById(R.id.button_location)
        buttonForecast = findViewById(R.id.button_forecast)

        //fetch location
        buttonLocation.setOnClickListener {
            fetchLocation()
            searchByLocation = true
        }

        //button for weather fetching
        button.setOnClickListener {
            hideKeyboard()
            if (input.text.isEmpty()) {
                Toast.makeText(this, "Field should not be empty!", Toast.LENGTH_LONG).show()
            } else {
                list.visibility = View.INVISIBLE
                resultHeader.text = "Loading..."
                val city = input.text.toString()
                //set url depending on if search by city name or by GPS location
                val url: String
                if (searchByLocation) {
                    url =
                        "https://api.openweathermap.org/data/2.5/weather?${input.text}&units=metric&appid=${apiKey}"
                    input.text.clear()
                    searchByLocation = false
                } else {
                    url =
                        "https://api.openweathermap.org/data/2.5/weather?q=${city}&units=metric&appid=${apiKey}"
                }
                //fetch weather
                downloadUrlAsync(
                    this, url
                ) {
                    //if response code == 200
                    if (!isError) {
                        // response parsing
                        val mp = ObjectMapper()
                        //get json object
                        val myObject: WeatherObject = mp.readValue(it, WeatherObject::class.java)
                        resultHeader.text = "Weather in ${myObject.name.toString()}"
                        resultInfo.text =
                            "${myObject.weather?.get(0)?.main.toString()}, ${myObject.main?.temp} °C\n" +
                                    "(feels like ${myObject.main?.feels_like.toString()} °C)\n" +
                                    "Wind: ${myObject.wind?.speed.toString()} m/s"
                        forecastUrl =
                            "https://api.openweathermap.org/data/2.5/forecast?q=${myObject.name.toString()}&units=metric&appid=${apiKey}"
                        val context: Context = image.context

                        // set weather icon depending on icon id in response
                        val id = context.resources.getIdentifier(
                            "icon_${myObject.weather?.get(0)?.icon}",
                            "drawable",
                            context.packageName
                        )
                        image.setImageResource(id)
                        image.visibility = View.VISIBLE
                        resultInfo.visibility = View.VISIBLE

                        // if weather is displayed, set forecast button visibility
                        buttonForecast.visibility = View.VISIBLE
                    } else {
                        //if response code != 200 -> error, try again
                        resultHeader.text = "Oops, something went wrong!\nTry again!"
                        image.visibility = View.INVISIBLE
                        resultInfo.visibility = View.INVISIBLE
                        buttonForecast.visibility = View.INVISIBLE
                    }
                }
            }
        }

        // forecast button
        buttonForecast.setOnClickListener {
            //fetch forecast
            downloadUrlAsync(
                this, forecastUrl
            ) {
                //response parsing
                val mp = ObjectMapper()
                //get json object
                val myObject: ForecastObject = mp.readValue(it, ForecastObject::class.java)
                //render listview using custom adapter
                binding.listView.adapter = MyAdapter(this, myObject.list)
                list.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Function for fetching location, using fusedLocationProviderClient
     *
     */
    @SuppressLint("SetTextI18n")
    private fun fetchLocation() {
        val task = fusedLocationProviderClient.lastLocation
        //check location permission
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //ask permission from user
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }
        task.addOnSuccessListener {
            if (it != null) {
                //set location latitude and longitude to input text for weather fetching
                input.setText("lat=${it.latitude}&lon=${it.longitude}")
            }
        }
    }

    /**
     * Function for keyboard hiding
     *
     */
    private fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }
    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * Function in which the data is fetched using httpURLConnection
     *
     * @param activity the activity in which the fetching happens
     * @param url url to fetch
     * @param callback function to parse response
     */
    private fun downloadUrlAsync(
        activity: AppCompatActivity,
        url: String,
        callback: (result: String) -> Unit
    ) {
        var line: String
        thread {
            //create connection
            val httpURLConnection = URL(url).openConnection() as HttpURLConnection
            if (httpURLConnection.responseCode == 200) {
                isError = false
                val reader = BufferedReader(InputStreamReader(httpURLConnection.inputStream))
                line = reader.readLine()
                httpURLConnection.disconnect()
                activity.runOnUiThread {
                    callback(line)
                }
            } else {
                isError = true
                httpURLConnection.disconnect()
                activity.runOnUiThread {
                    callback("")
                }
            }
        }
    }
}

//data classes for response parsing

/**
 * Data class for wind info
 *
 * @property speed wind speed
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherWind(var speed: String? = null)

/**
 * Data class for temperature info
 *
 * @property temp actual temperature
 * @property feels_like temperature feels like
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherMain(var temp: String? = null, var feels_like: String? = null)

/**
 * Data class for weather description and icon
 *
 * @property main short weather description
 * @property description full description
 * @property icon weather icon ID
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherInfo(
    var main: String? = null,
    var description: String? = null,
    var icon: String? = null
)

/**
 * Data class for weather object, that contains all info
 *
 * @property name city name
 * @property weather weather info object
 * @property main object with weather description and icon
 * @property wind object with wind info
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherObject(
    var name: String? = null,
    var weather: MutableList<WeatherInfo>? = null,
    var main: WeatherMain? = null,
    var wind: WeatherWind? = null
)
//forecast item
/**
 * Data class for forecast item
 *
 * @property weather weather info object
 * @property main object with weather description and icon
 * @property wind object with wind info
 * @property dt date
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ForecastWeatherObject(
    var weather: MutableList<WeatherInfo>? = null,
    var main: WeatherMain? = null,
    var wind: WeatherWind? = null,
    var dt: Long? = null
)

/**
 * Data class for forecast list
 *
 * @property list list with forecast items
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ForecastObject(
    var list: ArrayList<ForecastWeatherObject>? = null
)