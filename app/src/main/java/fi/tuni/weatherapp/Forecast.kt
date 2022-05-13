package fi.tuni.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class Forecast : AppCompatActivity() {

    lateinit var forecastInfo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)

        forecastInfo = findViewById(R.id.forecast)

        val extras: Bundle? = intent.extras
        if (extras != null) {
            val forecast: String? = extras.getString("forecast")
            if (forecast != null) {
                forecastInfo.text = forecast
            }
        }
    }
}