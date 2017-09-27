package com.example.valera.homeweatherstation.BuildGraphs;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.valera.homeweatherstation.BuildGraphs.DatePickers.DatePickerFrom;
import com.example.valera.homeweatherstation.BuildGraphs.DatePickers.DatePickerTo;
import com.example.valera.homeweatherstation.BuildGraphs.LoadingDialogs.LoadingDialogV1;
import com.example.valera.homeweatherstation.MySqlReader.ReceiveData;
import com.example.valera.homeweatherstation.R;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Valera on 25.03.2015.
 */
public class BuildingGraphsForCustomPeriod extends ActionBarActivity {

    private static String url_get_data = "http://tvv.dd-dns.de/meteo/get_custom_period_data.php";

    //Экземпляр класса получения данных
    ReceiveData receivedata = new ReceiveData();

    //Элементы UI
    ScrollView Scroll;
    static Button dateFrom;
    static Button dateTo;

    // JSON тэги
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_NUMBER = "number";
    private static final String TAG_DATA = "data";
    private static final String TAG_DATETIME = "dateTime";
    private static final String TAG_T_ISIDE = "t_inside";
    private static final String TAG_T_OUTSIDE = "t_outside";
    private static final String TAG_PRESSURE = "Pressure";
    private static final String TAG_HUMIDITY = "Humidity";

    //Границы графиков
    Date date_min;
    Date date_max;
    Double t_intside_min =0.0;
    Double t_intside_max =0.0;
    Double t_outside_min =0.0;
    Double t_outside_max =0.0;
    Double Humidity_min = 0.0;
    Double Humidity_max = 0.0;
    Double Pressure_min = 0.0;
    Double Pressure_max = 0.0;

    //Даты по умолчанию
    final Calendar lastWeekDate = Calendar.getInstance();
    final Calendar nowDate = Calendar.getInstance();

    //Границы дат запроса
    Calendar LastDay = Calendar.getInstance();
    Calendar FirstDay = Calendar.getInstance();
    int firstDay;
    int lastDay;

    //Массивы точек
    DataPoint[] t_inside_pointS;
    DataPoint[] t_outside_pointS;
    DataPoint[] Humidity_pointS;
    DataPoint[] Pressure_pointS;

    String [] dates;
    boolean equalityYears=false;
    boolean validateDate=false;

    DialogFragment newFragment;


    public void onCreate(Bundle savedInstanceState) {
        String StringDay;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_graphs);

        //Инициализация элементов UI
        Scroll = (ScrollView) findViewById(R.id.scrollView);
        dateFrom = (Button) findViewById(R.id.dateFrom);
        dateTo = (Button) findViewById(R.id.dateTo);

        dateFrom.setTypeface(Typeface.createFromAsset(
                getAssets(), "fonts/Algerian.ttf"));
        dateTo.setTypeface(Typeface.createFromAsset(
                getAssets(), "fonts/Algerian.ttf"));

        //Установка даты по умолчанию
        lastWeekDate.add(Calendar.DAY_OF_MONTH, -7);
        int yearFrom = lastWeekDate.get(Calendar.YEAR);
        int monthFrom = lastWeekDate.get(Calendar.MONTH);
        int dayFrom = lastWeekDate.get(Calendar.DAY_OF_MONTH);

        int yearTo = nowDate.get(Calendar.YEAR);
        int monthTo = nowDate.get(Calendar.MONTH);
        int dayTo = nowDate.get(Calendar.DAY_OF_MONTH);

        dateFrom.setText(dayFrom + " " + IdentificationMonth(monthFrom) + " " + yearFrom);
        dateTo.setText(dayTo + " " + IdentificationMonth(monthTo) + " " + yearTo);

        ((LockableScrollView)findViewById(R.id.scrollView)).setScrollingEnabled(false);
    }

    //Отображение графиков
    public void BuildCustomGraphs(View view) {

        ((LockableScrollView)findViewById(R.id.scrollView)).setScrollingEnabled(true);
        //Проверка корректности ввода даты
        boolean result = ValidateData();

        if(result) {
            newFragment = new LoadingDialogV1();
            newFragment.show(getFragmentManager(), "Loading");
            dates = getTextFromDatePickerTextView();
            new GetDataFromMySQL().execute();
        }
        else{
            Toast.makeText(BuildingGraphsForCustomPeriod.this, "Интервал задан некорректно" , Toast.LENGTH_LONG).show();
        }
    }

    //Фоновый поток для загрузки данных
    class GetDataFromMySQL extends AsyncTask<String, String, String> {
        protected void onPreExecute() {

            //Обнуление границ
            t_intside_min =0.0;
            t_intside_max =0.0;
            t_outside_min =0.0;
            t_outside_max =0.0;
            Humidity_min = 0.0;
            Humidity_max = 0.0;
            Pressure_min = 0.0;
            Pressure_max = 0.0;
            super.onPreExecute();
        }

        protected String doInBackground(String[] params) {
            int success;
            int number;
            int hour_data;
            try {

                JSONObject json = receivedata.ReceiveData(url_get_data, dates[0], dates[1]);
                success = json.getInt(TAG_SUCCESS);
                number = json.getInt(TAG_NUMBER);

                t_inside_pointS = new DataPoint[number];
                t_outside_pointS = new DataPoint[number];
                Humidity_pointS = new DataPoint[number];
                Pressure_pointS = new DataPoint[number];

                if (success == 1) {
                    //Цикл для прохода по всем часам
                    for(hour_data = 0; hour_data<number; hour_data++) {

                        JSONArray dataObj = json.getJSONArray(TAG_DATA + hour_data);
                        JSONObject data = dataObj.getJSONObject(0);

                        //Формируем дату
                        String DateTime = data.getString(TAG_DATETIME);
                        String DateAndTime[] = DateTime.split(" ");
                        String Date[] = DateAndTime[0].split("-");
                        String Time[] = DateAndTime[1].split(":");

                        Integer Year = new Integer(Date[0]);
                        Integer Month = new Integer(Date[1])-1;
                        Integer Day = new Integer(Date[2]);

                        Integer Hour = new Integer(Time[0]);
                        Integer Minute = new Integer(Time[1]);
                        Integer Second = new Integer(Time[2]);

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Year, Month, Day, Hour, Minute, Second);
                        java.util.Date d1 = calendar.getTime();

                        //Границы даты
                        if (hour_data == 0) {
                            date_min = d1;
                            FirstDay.set(Year, Month, Day);
                            firstDay = FirstDay.get(Calendar.DAY_OF_YEAR);
                        }
                        if (hour_data == number - 1) {
                            date_max = d1;
                            LastDay.set(Year, Month, Day);
                            lastDay = LastDay.get(Calendar.DAY_OF_YEAR);
                            if(FirstDay.get(Calendar.YEAR)==LastDay.get(Calendar.YEAR)){
                                equalityYears=true;
                            }
                        }

                        //Точки для графика домашней температуры и его границы
                        DataPoint t_inside_point = new DataPoint(d1, data.getDouble(TAG_T_ISIDE));
                        t_inside_pointS[hour_data] = t_inside_point;

                        if (data.getDouble(TAG_T_ISIDE) > t_intside_max) {
                            t_intside_max = data.getDouble(TAG_T_ISIDE);
                        }
                        if (t_intside_min == 0.0) {
                            t_intside_min = data.getDouble(TAG_T_ISIDE);
                        }
                        if (data.getDouble(TAG_T_ISIDE) < t_intside_min) {
                            t_intside_min = data.getDouble(TAG_T_ISIDE);
                        }

                        //Точки для графика уличной температуры и его границы
                        DataPoint t_outside_point = new DataPoint(d1, data.getDouble(TAG_T_OUTSIDE));
                        t_outside_pointS[hour_data] = t_outside_point;

                        if (data.getDouble(TAG_T_OUTSIDE) > t_outside_max) {
                            t_outside_max = data.getDouble(TAG_T_OUTSIDE);
                        }
                        if (t_outside_min == 0.0) {
                            t_outside_min = data.getDouble(TAG_T_OUTSIDE);
                        }
                        if (data.getDouble(TAG_T_OUTSIDE) < t_outside_min) {
                            t_outside_min = data.getDouble(TAG_T_OUTSIDE);
                        }

                        //Точки для графика влажности и его границы
                        DataPoint Humidity_point = new DataPoint(d1, data.getDouble(TAG_HUMIDITY));
                        Humidity_pointS[hour_data] = Humidity_point;

                        if (data.getDouble(TAG_HUMIDITY) > Humidity_max) {
                            Humidity_max = data.getDouble(TAG_HUMIDITY);
                        }
                        if (Humidity_min == 0.0) {
                            Humidity_min = data.getDouble(TAG_HUMIDITY);
                        }
                        if (data.getDouble(TAG_HUMIDITY) < Humidity_min) {
                            Humidity_min = data.getDouble(TAG_HUMIDITY);
                        }

                        //Точки для графика давления и его границы
                        DataPoint Pressure_point = new DataPoint(d1, data.getDouble(TAG_PRESSURE));
                        Pressure_pointS[hour_data] = Pressure_point;

                        if (data.getDouble(TAG_PRESSURE) > Pressure_max) {
                            Pressure_max = data.getDouble(TAG_PRESSURE);
                        }
                        if (Pressure_min == 0.0) {
                            Pressure_min = data.getDouble(TAG_PRESSURE);
                        }
                        if (data.getDouble(TAG_PRESSURE) < Pressure_min) {
                            Pressure_min = data.getDouble(TAG_PRESSURE);
                        }
                      }

                    //Запуск главного потока для работы с UI
                    runOnUiThread(new Runnable() {
                        public void run() {

                            newFragment.dismiss();

                            //Формируем "Серии"
                            LineGraphSeries<DataPoint> t_outside_series = new LineGraphSeries<DataPoint>(t_outside_pointS);

                            LineGraphSeries<DataPoint> t_inside_series = new LineGraphSeries<DataPoint>(t_inside_pointS);
                            t_inside_series.setColor(Color.RED);

                            LineGraphSeries<DataPoint> Pressure_series = new LineGraphSeries<DataPoint>(Pressure_pointS);
                            Pressure_series.setColor(Color.MAGENTA);

                            LineGraphSeries<DataPoint> Humidity_series = new LineGraphSeries<DataPoint>(Humidity_pointS);
                            Humidity_series.setColor(Color.GREEN);

                            //Инициализируем графики
                            GraphView graph_t_inside = (GraphView) findViewById(R.id.graph_t_inside);
                            GraphView graph_t_outside = (GraphView) findViewById(R.id.graph_t_outside);
                            GraphView graph_Humidity = (GraphView) findViewById(R.id.graph_Humidity);
                            GraphView graph_Pressure = (GraphView) findViewById(R.id.graph_Pressure);

                            //Параметры графика домашней температуры
                            graph_t_inside.getGridLabelRenderer().setNumHorizontalLabels(3);
                            graph_t_inside.getGridLabelRenderer().setNumVerticalLabels(5);
                            graph_t_inside.getGridLabelRenderer().setGridColor(Color.GRAY);
                            graph_t_inside.getGridLabelRenderer().setHorizontalLabelsColor(Color.GRAY);
                            graph_t_inside.getGridLabelRenderer().setVerticalLabelsColor(Color.GRAY);
                            graph_t_inside.getViewport().setMinX(date_min.getTime());
                            graph_t_inside.getViewport().setMaxX(date_max.getTime());
                            graph_t_inside.getViewport().setXAxisBoundsManual(true);
                            graph_t_inside.getViewport().setMinY(t_intside_min-0.1);
                            graph_t_inside.getViewport().setMaxY(t_intside_max+0.1);
                            graph_t_inside.getViewport().setYAxisBoundsManual(true);
                            graph_t_inside.setTitle("Температура в доме (°C)");
                            graph_t_inside.setTitleTextSize(25);
                            graph_t_inside.setTitleColor(Color.GRAY);

                            //Параметры графика уличной температуры
                            graph_t_outside.getGridLabelRenderer().setNumHorizontalLabels(3);
                            graph_t_outside.getGridLabelRenderer().setNumVerticalLabels(5);
                            graph_t_outside.getGridLabelRenderer().setGridColor(Color.GRAY);
                            graph_t_outside.getGridLabelRenderer().setHorizontalLabelsColor(Color.GRAY);
                            graph_t_outside.getGridLabelRenderer().setVerticalLabelsColor(Color.GRAY);
                            graph_t_outside.getViewport().setMinX(date_min.getTime());
                            graph_t_outside.getViewport().setMaxX(date_max.getTime());
                            graph_t_outside.getViewport().setXAxisBoundsManual(true);
                            graph_t_outside.getViewport().setMinY(t_outside_min-0.5);
                            graph_t_outside.getViewport().setMaxY(t_outside_max+0.5);
                            graph_t_outside.getViewport().setYAxisBoundsManual(true);
                            graph_t_outside.setTitle("Температура на улице (°C)");
                            graph_t_outside.setTitleTextSize(25);
                            graph_t_outside.setTitleColor(Color.GRAY);

                            //Параметры графика влажности
                            graph_Humidity.getGridLabelRenderer().setNumHorizontalLabels(3);
                            graph_Humidity.getGridLabelRenderer().setNumVerticalLabels(5);
                            graph_Humidity.getGridLabelRenderer().setGridColor(Color.GRAY);
                            graph_Humidity.getGridLabelRenderer().setHorizontalLabelsColor(Color.GRAY);
                            graph_Humidity.getGridLabelRenderer().setVerticalLabelsColor(Color.GRAY);
                            graph_Humidity.getViewport().setMinX(date_min.getTime());
                            graph_Humidity.getViewport().setMaxX(date_max.getTime());
                            graph_Humidity.getViewport().setXAxisBoundsManual(true);
                            graph_Humidity.getViewport().setMinY(Humidity_min-0.5);
                            graph_Humidity.getViewport().setMaxY(Humidity_max+0.5);
                            graph_Humidity.getViewport().setYAxisBoundsManual(true);
                            graph_Humidity.setTitle("Влажность в доме (%)");
                            graph_Humidity.setTitleTextSize(25);
                            graph_Humidity.setTitleColor(Color.GRAY);

                            //Параметры графика давления
                            graph_Pressure.getGridLabelRenderer().setNumHorizontalLabels(3);
                            graph_Pressure.getGridLabelRenderer().setNumVerticalLabels(5);
                            graph_Pressure.getGridLabelRenderer().setGridColor(Color.GRAY);
                            graph_Pressure.getGridLabelRenderer().setHorizontalLabelsColor(Color.GRAY);
                            graph_Pressure.getGridLabelRenderer().setVerticalLabelsColor(Color.GRAY);
                            graph_Pressure.getViewport().setMinX(date_min.getTime());
                            graph_Pressure.getViewport().setMaxX(date_max.getTime());
                            graph_Pressure.getViewport().setXAxisBoundsManual(true);
                            graph_Pressure.getViewport().setMinY(Pressure_min-0.5);
                            graph_Pressure.getViewport().setMaxY(Pressure_max+0.5);
                            graph_Pressure.getViewport().setYAxisBoundsManual(true);
                            graph_Pressure.setTitle("Давление (мм.рт.ст.)");
                            graph_Pressure.setTitleTextSize(25);
                            graph_Pressure.setTitleColor(Color.GRAY);

                            //Устанавливаем параметры меток
                            SetLableFormater(graph_t_inside, graph_t_outside, graph_Humidity, graph_Pressure, firstDay, lastDay, equalityYears);

                            //Очищаем и строим графики
                            graph_t_inside.removeAllSeries();
                            graph_t_outside.removeAllSeries();
                            graph_Humidity.removeAllSeries();
                            graph_Pressure.removeAllSeries();

                            graph_t_inside.addSeries(t_inside_series);
                            graph_t_outside.addSeries(t_outside_series);
                            graph_Humidity.addSeries(Humidity_series);
                            graph_Pressure.addSeries(Pressure_series);

                            //Прокручиваем экран
                           if(!validateDate){
                               Scroll.smoothScrollTo(0, 210);
                           }

                            //"Слушатель" нажатия на график домашней температуры
                            t_inside_series.setOnDataPointTapListener(new OnDataPointTapListener() {
                                @Override
                                public void onTap(Series series, DataPointInterface dataPoint) {
                                    Toast.makeText(BuildingGraphsForCustomPeriod.this, "" + new Date((long) dataPoint.getX()).toString().substring(4, 19) + "\n\t\t" + dataPoint.getY() + "°C", Toast.LENGTH_LONG).show();
                                }
                            });
                            //"Слушатель" нажатия на график уличной температуры
                            t_outside_series.setOnDataPointTapListener(new OnDataPointTapListener() {
                                @Override
                                public void onTap(Series series, DataPointInterface dataPoint) {
                                    Toast.makeText(BuildingGraphsForCustomPeriod.this, "" + new Date((long)dataPoint.getX()).toString().substring(4,19)+ "\n\t\t" + dataPoint.getY() + "°C", Toast.LENGTH_LONG).show();
                                }
                            });
                            //"Слушатель" нажатия на график влажности
                            Humidity_series.setOnDataPointTapListener(new OnDataPointTapListener() {
                                @Override
                                public void onTap(Series series, DataPointInterface dataPoint) {
                                    Toast.makeText(BuildingGraphsForCustomPeriod.this, "" + new Date((long)dataPoint.getX()).toString().substring(4,19)+ "\n\t\t" + dataPoint.getY() + "%", Toast.LENGTH_LONG).show();
                                }
                            });
                            //"Слушатель" нажатия на график давления
                            Pressure_series.setOnDataPointTapListener(new OnDataPointTapListener() {
                                @Override
                                public void onTap(Series series, DataPointInterface dataPoint) {
                                    Toast.makeText(BuildingGraphsForCustomPeriod.this, "" + new Date((long)dataPoint.getX()).toString().substring(4,19)+ "\n" + dataPoint.getY() + " мм.рт.ст", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                     public void run() {
                        newFragment.dismiss();
                        Toast.makeText(BuildingGraphsForCustomPeriod.this, "Отсутствуют данные за данный период, выберите другой интервал" , Toast.LENGTH_LONG).show();
                    }
                });
                }
                catch (NullPointerException e) {
                    //Сообщение в случае отсутствия соединения
                    runOnUiThread(new Runnable() {
                        public void run() {
                            newFragment.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(BuildingGraphsForCustomPeriod.this);
                            builder.setTitle("Внимание!")
                                    .setMessage("Нет соединения с сервером, попробуйте позже.")
                                    .setCancelable(false)
                                    .setPositiveButton("Повторить",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                    new GetDataFromMySQL().execute();
                                                }
                                            })
                                    .setNegativeButton("Выход",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.cancel();
                                                    System.exit(0);
                                                }
                                            });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    });
                }
            return null;
        }
    }

    public static String IdentificationMonth(int month){
        if(month==0){
            return "Янв";
        }
        if(month==1){
            return "Фев";
        }
        if(month==2){
            return "Мар";
        }
        if(month==3){
            return "Апр";
        }
        if(month==4){
            return "Май";
        }
        if(month==5){
            return "Июн";
        }
        if(month==6){
            return "Июл";
        }
        if(month==7){
            return "Авг";
        }
        if(month==8){
            return "Сен";
        }
        if(month==9){
            return "Окт";
        }
        if(month==10){
            return "Ноя";
        }
        if(month==11){
            return "Дек";
        }
        return  null;
    }

    public static int IdentificationMonth(String month){
        if(month.equals("Янв")){
            return 0;
        }
        if(month.equals("Фев")){
            return 1;
        }
        if(month.equals("Мар")){
            return 2;
        }
        if(month.equals("Апр")){
            return 3;
        }
        if(month.equals("Май")){
            return 4;
        }
        if(month.equals("Июн")){
            return 5;
        }
        if(month.equals("Июл")){
            return 6;
        }
        if(month.equals("Авг")){
            return 7;
        }
        if(month.equals("Сен")){
            return 8;
        }
        if(month.equals("Окт")){
            return 9;
        }
        if(month.equals("Ноя")){
            return 10;
        }
        if(month.equals("Дек")){
            return 11;
        }
        return Integer.parseInt(null);
    }

    //Установка выбраной пользователем даты
    public static void setTextOnDatePickerTextView(int year, int month, int day, int id){

        if(id==1) {
            dateFrom.setText(day + " " + IdentificationMonth(month) + " " + year);
        }
        if(id==2) {
            dateTo.setText(day + " " + IdentificationMonth(month) + " " + year);
        }
    }

    //Считывание установленой даты
    public static int[] getTextFromDatePickerTextView(int id){
        if(id==1) {
            String textDate = ((String) dateFrom.getText());
            String Date [] = textDate.split(" ");
            int [] date = new int [3];
            date[0]= Integer.parseInt(Date[2]);
            date[1]= IdentificationMonth(Date[1]);
            date[2]= Integer.parseInt(Date[0]);
            return date;
        }
        if(id==2) {
            String textDate = ((String) dateTo.getText());
            String Date [] = textDate.split(" ");
            int [] date = new int [3];
            date[0]= Integer.parseInt(Date[2]);
            date[1]= IdentificationMonth(Date[1]);
            date[2]= Integer.parseInt(Date[0]);
            return date;
        }
        return null;
    }

    public static String[] getTextFromDatePickerTextView(){
        String [] dates = new String[2];
        int [] i = new int[3];
        Calendar c = Calendar.getInstance();
        i = getTextFromDatePickerTextView(1);
        dates [0] = String.valueOf(i[0]+ "-" + (i[1]+1) + "-" + i[2]);
        i = getTextFromDatePickerTextView(2);
        c.set(i[0], i[1], i[2]);
        c.add(Calendar.DAY_OF_YEAR, 1);
        i[0] = c.get(Calendar.YEAR);
        i[1] = c.get(Calendar.MONTH)+1;
        i[2] = c.get(Calendar.DAY_OF_MONTH);
        dates [1] = String.valueOf(i[0]+ "-" + i[1] + "-" + i[2]);
       return dates;
    }

    //Проверка корректности даты
    public boolean ValidateData(){
        Calendar dateFrom = Calendar.getInstance();
        Calendar dateTo = Calendar.getInstance();
        Calendar startRecord = Calendar.getInstance();
        startRecord.set(2015, 01, 01);
        int df[]= getTextFromDatePickerTextView(1);
        dateFrom.set(df[0], df[1], df[2]);
        int dt[]= getTextFromDatePickerTextView(2);
        dateTo.set(dt[0], dt[1], dt[2]);
        if(startRecord.after(dateFrom)){
            validateDate=true;
        }
        else{
            validateDate=false;
        }
        if(dateTo.equals(dateFrom)){
           return true;
        }
        else {
           return dateTo.after(dateFrom);
        }
    }

    //Параметры меток графиков
    public void SetLableFormater(GraphView graph_t_inside, GraphView graph_t_outside, GraphView graph_Humidity, GraphView graph_Pressure, int FirstDay, int LastDay, boolean equalityYears){
        if(LastDay-FirstDay<2 && equalityYears==true){

            graph_t_inside.getGridLabelRenderer().setNumHorizontalLabels(4);
            graph_t_outside.getGridLabelRenderer().setNumHorizontalLabels(4);
            graph_Humidity.getGridLabelRenderer().setNumHorizontalLabels(4);
            graph_Pressure.getGridLabelRenderer().setNumHorizontalLabels(4);

            graph_t_inside.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        Long m = new Long((long) value);
                        Calendar n = Calendar.getInstance();
                        Date y = n.getTime();
                        y.setTime(m);
                        return y.toString().substring(10,15)+"0";
                    } else {
                        // show currency for y values
                        return String.format("%.1f", value).replace(",", ".");
                    }
                }
            });

            graph_t_outside.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        Long m = new Long((long) value);
                        Calendar n = Calendar.getInstance();
                        Date y = n.getTime();
                        y.setTime(m);
                        return y.toString().substring(10,15)+"0";
                    } else {
                        // show currency for y values
                        return String.format("%.1f", value).replace(",", ".");
                    }
                }
            });

            graph_Humidity.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        Long m = new Long((long) value);
                        Calendar n = Calendar.getInstance();
                        Date y = n.getTime();
                        y.setTime(m);
                        return y.toString().substring(10,15)+"0";
                    } else {
                        // show currency for y values
                        return String.format("%.1f", value).replace(",", ".");
                    }
                }
            });

            graph_Pressure.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        Long m = new Long((long) value);
                        Calendar n = Calendar.getInstance();
                        Date y = n.getTime();
                        y.setTime(m);
                        return y.toString().substring(10,15)+"0";
                    } else {
                        // show currency for y values
                        return String.format("%.1f", value).replace(",", ".");
                    }
                }
            });
        }
        else{
            graph_t_inside.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(BuildingGraphsForCustomPeriod.this) {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        return super.formatLabel(value, isValueX);
                    } else {
                        // show currency for y values
                        return String.format("%.1f", value).replace(",", ".");
                    }
                }
            });

            graph_t_outside.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(BuildingGraphsForCustomPeriod.this) {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        return super.formatLabel(value, isValueX);
                    } else {
                        // show currency for y values
                        return String.format("%.1f", value).replace(",", ".");
                    }
                }
            });

            graph_Humidity.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(BuildingGraphsForCustomPeriod.this) {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        return super.formatLabel(value, isValueX);
                    } else {
                        // show currency for y values
                        return String.format("%.1f", value).replace(",", ".");
                    }
                }
            });

            graph_Pressure.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(BuildingGraphsForCustomPeriod.this) {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if (isValueX) {
                        return super.formatLabel(value, isValueX);
                    } else {
                        // show currency for y values
                        return String.format("%.1f", value).replace(",", ".");
                    }
                }
            });
        }
    }

    public void showDatePickerDialogFrom(View v) {
        DialogFragment newFragment = new DatePickerFrom();
        newFragment.show(getFragmentManager(), "datePickerFrom");
    }

    public void showDatePickerDialogTo(View v) {
        DialogFragment newFragment = new DatePickerTo();
        newFragment.show(getFragmentManager(), "datePickerTo");
    }
}

