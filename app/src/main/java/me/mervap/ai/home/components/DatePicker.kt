package me.mervap.ai.home.components

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePicker(private val button: DateButton) : DialogFragment(), OnDateSetListener {
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val (year, month, day) = button.date
    val calendar = Calendar.getInstance()
    val maxDate = calendar.timeInMillis + 500
    calendar.set(2015, 1, 2)
    val minDate = calendar.timeInMillis - 500

    val picker = DatePickerDialog(requireContext(), this, year, month, day)
    picker.datePicker.minDate = minDate
    picker.datePicker.maxDate = maxDate
    return picker
  }

  override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
    button.date = Date(year, month, day)
  }
}
