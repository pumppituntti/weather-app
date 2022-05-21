package fi.tuni.weatherapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Custom adapter for rendering forecast items
 *
 * @property context the context in which the list is rendered
 * @property arrayList list to parse and render
 */
class MyAdapter(
    private val context: Activity,
    private val arrayList: ArrayList<ForecastWeatherObject>?
) : ArrayAdapter<ForecastWeatherObject>(context, R.layout.forecast_item, arrayList!!) {
    @SuppressLint("SetTextI18n", "ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.forecast_item, null)

        //item contains image and two textViews
        val imageView: ImageView = view.findViewById(R.id.icon)
        val temp: TextView = view.findViewById(R.id.temp)
        val date: TextView = view.findViewById(R.id.date)

        val imageContext: Context = imageView.context

        //set data to item
        val id = imageContext.resources.getIdentifier(
            "icon_${arrayList?.get(position)?.weather?.get(0)?.icon}",
            "drawable",
            imageContext.packageName
        )
        imageView.setImageResource(id)
        temp.text = "${arrayList?.get(position)?.main?.temp} °C\nWind: ${arrayList?.get(position)?.wind?.speed.toString()} m/s"
        date.text = SimpleDateFormat("dd.MM, HH:mm", Locale("en", "FI")).format(Date(arrayList?.get(position)?.dt!!.toLong() * 1000))

        return view
    }
}