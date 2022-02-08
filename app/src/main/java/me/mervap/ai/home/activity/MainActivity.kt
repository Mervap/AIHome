package me.mervap.ai.home.activity

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import me.mervap.ai.home.InvalidatedGraphView.configurePressureGraph
import me.mervap.ai.home.InvalidatedGraphView.setOnDataPointTapListener
import me.mervap.ai.home.R
import me.mervap.ai.home.components.InvalidatedGraphView
import me.mervap.ai.home.components.dateTimeToDate
import me.mervap.ai.home.http.PressureInfo
import me.mervap.ai.home.http.Result.Success
import me.mervap.ai.home.http.WeatherInfo
import me.mervap.ai.home.http.client
import retrofit2.await
import java.util.*
import kotlin.math.roundToInt

class MainActivity : RequestActivity() {
  private lateinit var layoutVisibility: LinearLayout
  private lateinit var thermometer: ImageView
  private lateinit var imageView: ImageView

  override fun onLoadingDialogCancel(fragment: DialogFragment, dialog: DialogInterface) {
    val myDialogFragment = SettingDialog(::client)
    myDialogFragment.show(supportFragmentManager, "SettingDialog")
  }

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_info)

    layoutVisibility = findViewById(R.id.layout_visibility)
    layoutVisibility.visibility = View.INVISIBLE

    thermometer = findViewById(R.id.t_outside_image)
    imageView = findViewById(R.id.imageView)

    for (id in TEXT_VIEW_IDS) {
      val textView = findViewById<View>(id) as TextView
      textView.typeface = Typeface.createFromAsset(assets, "fonts/Algerian.ttf")
    }

    loadData()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_record_data, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.action_build_last_24_hour -> {
        val intent = Intent(this, BuildingGraphsLast24Hour::class.java)
        startActivity(intent)
        true
      }
      R.id.action_build_graphs_for_custom_period -> {
        intent = Intent(this, BuildingGraphsForCustomPeriod::class.java)
        startActivity(intent)
        true
      }
      R.id.my_settings -> {
        val myDialogFragment = SettingDialog(::client)
        myDialogFragment.show(supportFragmentManager, "SettingDialog")
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  @Suppress("UNUSED_PARAMETER")
  fun updateInfo(v: View) {
    loadData()
  }

  private fun setDrawableByCurrentTime() {
    val calendar = Calendar.getInstance()
    val month = calendar[Calendar.MONTH]
    val hour = calendar[Calendar.HOUR_OF_DAY]
    val imageId = when {
      month == 0 && hour in 8..16 -> R.drawable.sun_with_snow
      month == 1 && hour in 7..17 -> R.drawable.sun_with_snow
      month == 2 && hour in 6..18 -> R.drawable.sun_with_snow
      month in 3..4 && hour in 5..19 -> R.drawable.sun_without_snow
      month in 5..6 && hour in 5..20 -> R.drawable.sun
      month == 7 && hour in 5..19 -> R.drawable.sun
      month == 8 && hour in 6..18 -> R.drawable.sun_without_snow
      month == 9 && hour in 7..17 -> R.drawable.sun_without_snow
      month == 10 && hour in 7..16 -> R.drawable.sun_with_snow
      month == 11 && hour in 8..16 -> R.drawable.sun_with_snow
      else -> R.drawable.moon
    }

    imageView.setImageDrawable(
      ResourcesCompat.getDrawable(resources, imageId, null)
    )
  }

  override suspend fun loadDataImpl() {
    try {
      val currentDataResponse = client().getData().await()
      if (currentDataResponse !is Success<WeatherInfo>) {
        showNoDataDialog()
        return
      }

      runOnUiThread {
        setDrawableByCurrentTime()

        loadingFragment?.dismiss()
        layoutVisibility.visibility = View.VISIBLE

        val tOutsideText = findViewById<TextView>(R.id.t_outside)
        val tInsideText = findViewById<TextView>(R.id.t_inside)
        val pressureText = findViewById<TextView>(R.id.Pressure)
        val humidityText = findViewById<TextView>(R.id.Humidity)

        val currentData = currentDataResponse.data

        tOutsideText.text = getString(R.string.celsius, currentData.tOutside)
        tInsideText.text = getString(R.string.celsius, currentData.tInside)
        pressureText.text = currentData.pressure.toString()
        humidityText.text = getString(R.string.percentage, currentData.humidity.roundToInt())

        setThermometerByTemperature(currentData.tOutside)
      }

      val pressureDataResponse = client().getPressureData().await()
      if (pressureDataResponse !is Success<List<PressureInfo>>) {
        return
      }

      val pressureData = pressureDataResponse.data
      if (pressureData.isEmpty()) {
        return
      }

      val dates = pressureData.map { it.dateTime.dateTimeToDate() }
      val dateMin = dates.first()
      val dateMax = dates.last()
      val (pressureMin, pressureMax, pressureSeries) = extractSeries(dates,
        pressureData,
        PressureInfo::pressure)
      pressureSeries.color = Color.MAGENTA

      runOnUiThread {
        val pressureGraph = findViewById<InvalidatedGraphView>(R.id.graph_Pressure)
        pressureGraph.configurePressureGraph(dateMin, dateMax, pressureMin, pressureMax)
        pressureGraph.addSeries(pressureSeries)

        pressureSeries.setOnDataPointTapListener(this,
          Toast.LENGTH_SHORT,
          getString(R.string.pressureUnits))
        pressureGraph.visibility = View.VISIBLE
      }
    }
    catch (e: Exception) {
      e.printStackTrace()
      showNoConnectionDialog()
    }
  }

  private fun setThermometerByTemperature(outsideTemperature: Double) {
    val thermometerImg = when {
      outsideTemperature < 5 -> R.drawable.tm
      5 <= outsideTemperature && outsideTemperature < 20 -> R.drawable.tn
      else -> R.drawable.th
    }

    thermometer.setImageDrawable(
      ResourcesCompat.getDrawable(resources, thermometerImg, null)
    )
  }

  companion object {
    private val TEXT_VIEW_IDS =
      arrayOf(R.id.TimeClock, R.id.t_inside, R.id.t_outside, R.id.Pressure, R.id.Humidity)
  }
}