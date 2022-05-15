package fi.tuni.weatherapp

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class MyAdapter(
    private val context: Activity,
    private val arrayList: ArrayList<ForecastWeatherObject>?
) : ArrayAdapter<ForecastWeatherObject>(context, R.layout.forecast_item, arrayList!!) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater: LayoutInflater = LayoutInflater.from(context)
        val view: View = inflater.inflate(R.layout.forecast_item, null)

        val imageView: ImageView = view.findViewById(R.id.icon)
        val temp: TextView = view.findViewById(R.id.temp)
        val date: TextView = view.findViewById(R.id.date)

        val imageContext: Context = imageView.context
        val id = imageContext.resources.getIdentifier(
            "icon_${arrayList?.get(position)?.weather?.get(0)?.icon}",
            "drawable",
            imageContext.packageName
        )
        imageView.setImageResource(id)
        temp.text = "${arrayList?.get(position)?.weather?.get(0)?.main}, ${arrayList?.get(position)?.main?.temp} °C"
        date.text = arrayList?.get(position)?.dt_txt

        return view
    }
}