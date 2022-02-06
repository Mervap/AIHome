package me.mervap.ai.home.InvalidatedGraphView

import android.content.Context
import android.graphics.Color
import android.widget.Toast
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.series.BaseSeries
import me.mervap.ai.home.components.InvalidatedGraphView
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private val formatter =
  DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale("ru"))

fun BaseSeries<*>.setOnDataPointTapListener(context: Context, toastLen: Int, ed: String) {
  setOnDataPointTapListener { _, dataPoint ->
    val date = formatter.format(Date(dataPoint.x.toLong()))
    val value = "${dataPoint.y} $ed"
    val space = " ".repeat((date.length - value.length) / 2)
    Toast.makeText(context, "$date\n$space$value", toastLen).show()
  }
}

fun InvalidatedGraphView.configureTInsideGraph(
  dateMin: Date,
  dateMax: Date,
  tInsideMin: Double,
  tInsideMax: Double,
) {
  configureGraph(dateMin, dateMax, tInsideMin, tInsideMax, 0.1)
}

fun InvalidatedGraphView.configureTOutsideGraph(
  dateMin: Date,
  dateMax: Date,
  tOutsideMin: Double,
  tOutsideMax: Double,
) {
  configureGraph(dateMin, dateMax, tOutsideMin, tOutsideMax, 0.5)
}

fun InvalidatedGraphView.configureHumidityGraph(
  dateMin: Date,
  dateMax: Date,
  humidityMin: Double,
  humidityMax: Double,
) {
  configureGraph(dateMin, dateMax, humidityMin, humidityMax, 0.5)
}

fun InvalidatedGraphView.configureOutsideHumidityGraph(
  dateMin: Date,
  dateMax: Date,
  outsideHumidityMin: Double,
  outsideHumidityMax: Double,
) {
  configureGraph(dateMin, dateMax, outsideHumidityMin, outsideHumidityMax, 0.1)
}

fun InvalidatedGraphView.configurePressureGraph(
  dateMin: Date,
  dateMax: Date,
  pressureMin: Double,
  pressureMax: Double,
) {
  configureGraph(dateMin, dateMax, pressureMin, pressureMax, 0.2)
}

private fun InvalidatedGraphView.configureGraph(
  dateMin: Date,
  dateMax: Date,
  pressureMin: Double,
  pressureMax: Double,
  diffX: Double,
) {
  val dateDiff = dateMax.time - dateMin.time
  val day = 24 * 60 * 60 * 1000
  configureGraph(
    minX = dateMin.time.toDouble(),
    maxX = dateMax.time.toDouble(),
    minY = pressureMin - diffX,
    maxY = pressureMax + diffX,
    numVerticalLabels = if (dateDiff > day * 2) 4 else 5
  )
}

private fun InvalidatedGraphView.configureGraph(
  minX: Double,
  maxX: Double,
  minY: Double,
  maxY: Double,
  numVerticalLabels: Int,
) {
  invalidateSeries()
  gridLabelRenderer.numHorizontalLabels = 4
  gridLabelRenderer.numVerticalLabels = numVerticalLabels
  gridLabelRenderer.gridColor = Color.GRAY
  gridLabelRenderer.horizontalLabelsColor = Color.GRAY
  gridLabelRenderer.verticalLabelsColor = Color.GRAY
  gridLabelRenderer.isHumanRounding = false
  viewport.setMinX(minX)
  viewport.setMaxX(maxX)
  viewport.isXAxisBoundsManual = true
  viewport.setMinY(minY)
  viewport.setMaxY(maxY)
  viewport.isYAxisBoundsManual = true

  gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {

    override fun formatLabel(value: Double, isValueX: Boolean): String {
      return if (isValueX) {
        if (numVerticalLabels == 5) {
          DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(value.toLong()))
        }
        else {
          SimpleDateFormat("dd.MM", Locale("ru")).format(Date(value.toLong()))
        }
      }
      else {
        String.format(Locale("us"), "%.1f", value)
      }
    }
  }
}
