package com.example.valera.homeweatherstation.BuildGraphs.DatePickers;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.widget.DatePicker;

import com.example.valera.homeweatherstation.BuildGraphs.BuildingGraphsForCustomPeriod;

import java.util.Calendar;

/**
 * Created by Valera on 26.03.2015.
 */
public class DatePickerTo extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Context context = getActivity();
        context.setTheme(android.R.style.Theme_Holo_Wallpaper);

        int Date [] = BuildingGraphsForCustomPeriod.getTextFromDatePickerTextView(2);
        int year = Date[0];
        int month = Date[1];
        int day = Date[2];
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog d = new DatePickerDialog(context, this, year, month, day);
        d.getDatePicker().setMaxDate(calendar.getTimeInMillis()+500);
        calendar.set(2015, 01, 02);
        d.getDatePicker().setMinDate(calendar.getTimeInMillis()-500);
        return d;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        BuildingGraphsForCustomPeriod.setTextOnDatePickerTextView(year, month, day, 2);
    }
}
