package me.mervap.ai.home.activity

import android.content.DialogInterface
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import me.mervap.ai.home.R
import me.mervap.ai.home.components.Date
import me.mervap.ai.home.components.DateButton
import me.mervap.ai.home.components.DatePicker
import me.mervap.ai.home.components.LockableScrollView
import me.mervap.ai.home.http.DataAPI
import me.mervap.ai.home.http.FullInfo
import me.mervap.ai.home.http.Result
import retrofit2.await
import java.util.*

class BuildingGraphsForCustomPeriod : BuildingGraphs() {

  private lateinit var scrollView: LockableScrollView
  private lateinit var dateFromButton: DateButton
  private lateinit var dateToButton: DateButton

  override fun onLoadingDialogCancel(fragment: DialogFragment, dialog: DialogInterface) {}

  override fun onRequestFailure() {
    runOnUiThread {
      loadingFragment?.dismiss()
      Toast.makeText(this, getString(R.string.noDataForPeriod), Toast.LENGTH_LONG).show()
    }
  }

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.custom_graphs)

    scrollView = findViewById(R.id.scrollView)
    dateFromButton = findViewById(R.id.dateFrom)
    dateToButton = findViewById(R.id.dateTo)

    dateFromButton.typeface = Typeface.createFromAsset(assets, "fonts/Algerian.ttf")
    dateToButton.typeface = Typeface.createFromAsset(assets, "fonts/Algerian.ttf")

    val date = Calendar.getInstance()
    val yearTo = date[Calendar.YEAR]
    val monthTo = date[Calendar.MONTH]
    val dayTo = date[Calendar.DAY_OF_MONTH]

    date.add(Calendar.DAY_OF_MONTH, -7)
    val yearFrom = date[Calendar.YEAR]
    val monthFrom = date[Calendar.MONTH]
    val dayFrom = date[Calendar.DAY_OF_MONTH]

    dateFromButton.date = Date(yearFrom, monthFrom, dayFrom)
    dateToButton.date = Date(yearTo, monthTo, dayTo)
    scrollView.setScrollingEnabled(false)
  }

  @Suppress("UNUSED_PARAMETER")
  fun buildCustomGraphs(v: View) {
    scrollView.setScrollingEnabled(true)
    if (isDataValid()) {
      loadData()
    }
    else {
      Toast.makeText(this, getString(R.string.badInterval), Toast.LENGTH_LONG).show()
    }
  }

  override suspend fun getData(client: DataAPI): Result<List<FullInfo>> {
    val dateTimeFrom = dateFromButton.date.toString()
    val dateTimeTo = dateToButton.date.toString()
    return client.getCustomPeriodData(dateTimeFrom, dateTimeTo).await()
  }

  override fun onGraphConfigured() {
    scrollView.smoothScrollTo(0, 210)
  }

  private fun isDataValid(): Boolean {
    val dateFrom = dateFromButton.date.toCalendar()
    val dateTo = dateToButton.date.toCalendar()
    return dateTo == dateFrom || dateTo.after(dateFrom)
  }

  @Suppress("UNUSED_PARAMETER")
  fun showDatePickerDialogFrom(v: View) {
    val newFragment = DatePicker(dateFromButton)
    newFragment.show(supportFragmentManager, "datePickerFrom")
  }

  @Suppress("UNUSED_PARAMETER")
  fun showDatePickerDialogTo(v: View) {
    val newFragment = DatePicker(dateToButton)
    newFragment.show(supportFragmentManager, "datePickerTo")
  }
}