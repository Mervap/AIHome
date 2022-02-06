package me.mervap.ai.home.activity

import android.graphics.Color
import android.view.View
import android.widget.Toast
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import me.mervap.ai.home.InvalidatedGraphView.*
import me.mervap.ai.home.R
import me.mervap.ai.home.components.InvalidatedGraphView
import me.mervap.ai.home.components.dateTimeToDate
import me.mervap.ai.home.http.DataAPI
import me.mervap.ai.home.http.FullInfo
import me.mervap.ai.home.http.Result
import me.mervap.ai.home.http.client
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KProperty1

abstract class BuildingGraphs : RequestActivity() {
  abstract suspend fun getData(client: DataAPI): Result<List<FullInfo>>
  abstract fun onRequestFailure()
  open fun onGraphConfigured() {}

  override suspend fun loadDataImpl() {
    try {
      val dataResponse = getData(client())
      if (dataResponse !is Result.Success<List<FullInfo>>) {
        onRequestFailure()
        return
      }

      val graphTInside = findViewById<InvalidatedGraphView>(R.id.graph_t_inside)
      val graphTOutside = findViewById<InvalidatedGraphView>(R.id.graph_t_outside)
      val graphHumidity = findViewById<InvalidatedGraphView>(R.id.graph_Humidity)
      val graphPressure = findViewById<InvalidatedGraphView>(R.id.graph_Pressure)
      val graphHumidityOutside = findViewById<InvalidatedGraphView>(R.id.graph_Humidity_outside)

      val data = dataResponse.data
      val dates = data.map { it.dateTime.dateTimeToDate() }

      graphTInside.addSeries(
        dates,
        data,
        FullInfo::tInside,
        Color.RED,
        getString(R.string.home_temperature_title),
        getString(R.string.celsium_ed),
        InvalidatedGraphView::configureTInsideGraph
      )

      runOnUiThread {
        loadingFragment?.dismiss()
      }

      graphTOutside.addSeries(
        dates,
        data,
        FullInfo::tOutside,
        -0xFF8834,
        getString(R.string.temperature_outside_title),
        getString(R.string.celsium_ed),
        InvalidatedGraphView::configureTOutsideGraph
      )
      graphPressure.addSeries(
        dates,
        data,
        FullInfo::pressure,
        Color.MAGENTA,
        getString(R.string.pressure_title),
        getString(R.string.pressure_ed),
        InvalidatedGraphView::configurePressureGraph
      )
      graphHumidityOutside.addSeries(
        dates,
        data,
        FullInfo::humidityOutside,
        Color.YELLOW,
        getString(R.string.huminity_outside_title),
        getString(R.string.just_persentage),
        InvalidatedGraphView::configureOutsideHumidityGraph
      )

      val humiditySeries = graphHumidity.addSeries(
        dates,
        data,
        FullInfo::humidity,
        Color.GREEN,
        getString(R.string.humidity_inside_title),
        getString(R.string.just_persentage),
        InvalidatedGraphView::configureHumidityGraph
      )

      humiditySeries.title = getString(R.string.humidity_legend)
      graphHumidity.addSecondSeries(
        dates, data,
        listOf(FullInfo::mainVentFlap, FullInfo::roomVentFlap),
        listOf(Color.WHITE, Color.CYAN),
        listOf(getString(R.string.main_vent_legend), getString(R.string.room_vent_legend)),
        listOf(getString(R.string.degree), getString(R.string.degree))
      )
      onGraphConfigured()
    }
    catch (e: Exception) {
      showNoConnectionDialog()
    }
  }

  private fun InvalidatedGraphView.addSeries(
    dates: List<Date>,
    data: List<FullInfo>,
    field: KProperty1<FullInfo, Double>,
    color: Int,
    title: String,
    ed: String,
    configuration: InvalidatedGraphView.(Date, Date, Double, Double) -> Unit,
  ): LineGraphSeries<DataPoint> {
    val dateMin = dates.first()
    val dateMax = dates.last()
    val (dataMin, dataMax, dataSeries) = extractSeries(dates, data, field)

    dataSeries.color = color
    dataSeries.setOnDataPointTapListener(this@BuildingGraphs, Toast.LENGTH_LONG, ed)

    runOnUiThread {
      this.configuration(dateMin, dateMax, dataMin, dataMax)
      this.title = "$title ($ed)"
      this.titleTextSize = 40f
      this.titleColor = Color.GRAY
      this.visibility = View.VISIBLE
      this.addSeries(dataSeries)
    }

    return dataSeries
  }


  private fun InvalidatedGraphView.addSecondSeries(
    dates: List<Date>,
    data: List<FullInfo>,
    fields: List<KProperty1<FullInfo, Double>>,
    colors: List<Int>,
    titles: List<String>,
    eds: List<String>,
  ) {
    var globalMin = Double.MAX_VALUE
    var globalMax = Double.MIN_VALUE

    val series = mutableListOf<LineGraphSeries<*>>()
    for (i in fields.indices) {
      val field = fields[i]
      val (dataMin, dataMax, dataSeries) = extractSeries(dates, data, field)

      globalMin = min(globalMin, dataMin)
      globalMax = max(globalMax, dataMax)

      dataSeries.color = colors[i]
      dataSeries.title = titles[i]
      dataSeries.setOnDataPointTapListener(this@BuildingGraphs, Toast.LENGTH_LONG, eds[i])

      series.add(dataSeries)
    }

    runOnUiThread {
      clearSecondScale()

      legendRenderer.textColor = Color.GRAY
      legendRenderer.textSize = 25f
      legendRenderer.align = LegendRenderer.LegendAlign.TOP
      legendRenderer.isVisible = true

      gridLabelRenderer.verticalLabelsSecondScaleColor = Color.GRAY
      secondScale.setMinY(max(globalMin - 5, 0.0))
      secondScale.setMaxY(min(globalMax + 5, 100.0))

      series.forEach { secondScale.addSeries(it) }
    }
  }
}

fun <T> extractSeries(
  dates: List<Date>,
  data: List<T>,
  field: KProperty1<T, Double>,
): ExtractedData {
  val dataMin = data.minOf { field.call(it) }
  val dataMax = data.maxOf { field.call(it) }
  val dataPoints = data.mapIndexed { ind, info ->
    DataPoint(dates[ind], field.call(info))
  }
  return ExtractedData(dataMin, dataMax, LineGraphSeries(dataPoints.toTypedArray()))
}

data class ExtractedData(
  val dataMin: Double,
  val dataMax: Double,
  val series: LineGraphSeries<DataPoint>,
)
