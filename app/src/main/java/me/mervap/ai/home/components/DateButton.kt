package me.mervap.ai.home.components

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import me.mervap.ai.home.R
import java.util.*

data class Date(val year: Int, val month: Int, val day: Int) {
  fun toCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, day, 0, 0, 0)
    return calendar
  }

  override fun toString(): String = "$year-${month + 1}-$day"
}

fun String.dateTimeToDate(): java.util.Date {
  val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
  return sdf.parse(this)
}

class DateButton : AppCompatButton {
  constructor(context: Context) : super(context)
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
    attrs,
    defStyleAttr)

  var date: Date
    get() {
      val (day, month, year) = text.split(" ")
      return Date(year.toInt(), MOUNTS_REV[month]!!, day.toInt())
    }
    set(value) {
      val (year, month, day) = value
      text = resources.getString(R.string.date, day, MOUNTS[month], year)
    }
}

private val MOUNTS = arrayOf(
  "Янв", "Фев", "Мар", "Апр", "Май", "Июн", "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек"
)
private val MOUNTS_REV = MOUNTS.associateWith { MOUNTS.indexOf(it) }