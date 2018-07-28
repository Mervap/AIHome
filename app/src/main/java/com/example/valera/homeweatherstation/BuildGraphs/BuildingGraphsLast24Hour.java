package com.example.valera.homeweatherstation.BuildGraphs;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.valera.homeweatherstation.BuildGraphs.LoadingDialogs.LoadingDialogV2;
import com.example.valera.homeweatherstation.MySqlReader.ReceiveData;
import com.example.valera.homeweatherstation.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.Viewport;
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

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Created by Valera on 23.03.2015.
 */
public class BuildingGraphsLast24Hour extends AppCompatActivity {

    //Экземпляр класса получения данных
    ReceiveData receivedata = new ReceiveData();

    // url получения списка данных за последние 24 часа
    private String domain;
    private static String url_get_data;
    private SharedPreferences sPref;
    private final String URL_DATA = "url";

    // JSON тэги
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_DATA = "data";
    private static final String TAG_DATETIME = "dateTime";
    private static final String TAG_T_ISIDE = "t_inside";
    private static final String TAG_T_OUTSIDE = "t_outside";
    private static final String TAG_PRESSURE = "Pressure";
    private static final String TAG_HUMIDITY = "Humidity";
    private static final String TAG_HUMIDITY_OUTSIDE = "Humidity_outside";
    private static final String TAG_MAIN_VENT_FLAP = "main_vent_flap";
    private static final String TAG_ROOM_VENT_FLAP = "room_vent_flap";


    //Массивы точек
    DataPoint[] t_inside_pointS = new DataPoint[24];
    DataPoint[] t_outside_pointS = new DataPoint[24];
    DataPoint[] Humidity_pointS = new DataPoint[24];
    DataPoint[] Pressure_pointS = new DataPoint[24];
    DataPoint[] Humidity_outside_pointS = new DataPoint[24];
    DataPoint[] main_vent_flap_pointS = new DataPoint[24];
    DataPoint[] room_vent_flap_pointS = new DataPoint[24];

    //Границы графиков
    Date date_min;
    Date date_max;
    Double t_intside_min = 0.0;
    Double t_intside_max = 0.0;
    Double t_outside_min = 0.0;
    Double t_outside_max = 0.0;
    Double Humidity_min = 0.0;
    Double Humidity_max = 0.0;
    Double Pressure_min = 0.0;
    Double Pressure_max = 0.0;
    Double Humidity_outside_min = 0.0;
    Double Humidity_outside_max = 0.0;
    Double main_vent_flap_min = 0.0;
    Double main_vent_flap_max = 0.0;
    Double room_vent_flap_min = 0.0;
    Double room_vent_flap_max = 0.0;


    DialogFragment newFragment;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graphs_activity);

        sPref = getDefaultSharedPreferences(this);
        domain = loadUrl();
        url_get_data = "http://" + domain + getResources().getString(R.string.url_get_last_data);

        newFragment = new LoadingDialogV2();
        newFragment.show(getFragmentManager(), "Loading");
        new GetDataFromMySQL().execute();
    }

    //Фоновый поток для загрузки данных
    class GetDataFromMySQL extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String[] params) {
            int success;
            int hour_data;
            try {
                JSONObject json = receivedata.ReceiveData(url_get_data);
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    //Цикл для прохода по всем часам
                    for (hour_data = 0; hour_data < 24; hour_data++) {

                        JSONArray dataObj = json.getJSONArray(TAG_DATA + hour_data);
                        JSONObject data = dataObj.getJSONObject(0);

                        //Формируем дату
                        String DateTime = data.getString(TAG_DATETIME);
                        String DateAndTime[] = DateTime.split(" ");
                        String Date[] = DateAndTime[0].split("-");
                        String Time[] = DateAndTime[1].split(":");

                        Integer Year = new Integer(Date[0]);
                        Integer Month = new Integer(Date[1]) - 1;
                        Integer Day = new Integer(Date[2]);

                        Integer Hour = new Integer(Time[0]);
                        Integer Minute = new Integer(Time[1]);
                        Integer Second = new Integer(Time[2]);

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Year, Month, Day, Hour, Minute, Second);
                        Date d1 = calendar.getTime();

                        //Границы даты
                        if (hour_data == 0) {
                            date_min = d1;
                        }
                        if (hour_data == 23) {
                            date_max = d1;
                        }

                        //Точки для графика домашней температуры и его границы
                        DataPoint t_inside_point = new DataPoint(d1, data.getDouble(TAG_T_ISIDE));
                        t_inside_pointS[hour_data] = t_inside_point;

                        if (data.getDouble(TAG_T_ISIDE) > t_intside_max) {
                            t_intside_max = data.getDouble(TAG_T_ISIDE);
                        }
                        if (t_intside_min == 0.0 && hour_data == 0) {
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
                        if (t_outside_min == 0.0 && hour_data == 0) {
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
                        if (Humidity_min == 0.0 && hour_data == 0) {
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
                        if (Pressure_min == 0.0 && hour_data == 0) {
                            Pressure_min = data.getDouble(TAG_PRESSURE);
                        }
                        if (data.getDouble(TAG_PRESSURE) < Pressure_min) {
                            Pressure_min = data.getDouble(TAG_PRESSURE);
                        }

                        //Точки для графика влажности на улице и его границы
                        DataPoint Humidity_outside_point = new DataPoint(d1, data.getDouble(TAG_HUMIDITY_OUTSIDE));
                        Humidity_outside_pointS[hour_data] = Humidity_outside_point;

                        if (data.getDouble(TAG_HUMIDITY_OUTSIDE) > Humidity_outside_max) {
                            Humidity_outside_max = data.getDouble(TAG_HUMIDITY_OUTSIDE);
                        }
                        if (Humidity_outside_min == 0.0 && hour_data == 0) {
                            Humidity_outside_min = data.getDouble(TAG_HUMIDITY_OUTSIDE);
                        }
                        if (data.getDouble(TAG_HUMIDITY_OUTSIDE) < Humidity_outside_min) {
                            Humidity_outside_min = data.getDouble(TAG_HUMIDITY_OUTSIDE);
                        }

                        //Точки для графика главной вентиляции и его границы
                        DataPoint main_vent_flap_point = new DataPoint(d1, data.getDouble(TAG_MAIN_VENT_FLAP));
                        main_vent_flap_pointS[hour_data] = main_vent_flap_point;

                        if (data.getDouble(TAG_MAIN_VENT_FLAP) > main_vent_flap_max) {
                            main_vent_flap_max = data.getDouble(TAG_MAIN_VENT_FLAP);
                        }
                        if (main_vent_flap_min == 0.0 && hour_data == 0) {
                            main_vent_flap_min = data.getDouble(TAG_MAIN_VENT_FLAP);
                        }
                        if (data.getDouble(TAG_MAIN_VENT_FLAP) < main_vent_flap_min) {
                            main_vent_flap_min = data.getDouble(TAG_MAIN_VENT_FLAP);
                        }

                        //Точки для графика вентиляции комнаты и его границы
                        DataPoint room_vent_flap = new DataPoint(d1, data.getDouble(TAG_ROOM_VENT_FLAP));
                        room_vent_flap_pointS[hour_data] = room_vent_flap;

                        if (data.getDouble(TAG_ROOM_VENT_FLAP) > room_vent_flap_max) {
                            room_vent_flap_max = data.getDouble(TAG_ROOM_VENT_FLAP);
                        }
                        if (room_vent_flap_min == 0.0 && hour_data == 0) {
                            room_vent_flap_min = data.getDouble(TAG_ROOM_VENT_FLAP);
                        }
                        if (data.getDouble(TAG_ROOM_VENT_FLAP) < room_vent_flap_min) {
                            room_vent_flap_min = data.getDouble(TAG_ROOM_VENT_FLAP);
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

                            LineGraphSeries<DataPoint> Humidity_outside_series = new LineGraphSeries<DataPoint>(Humidity_outside_pointS);
                            Humidity_outside_series.setColor(Color.YELLOW);

                            LineGraphSeries<DataPoint> main_vent_flap_series = new LineGraphSeries<DataPoint>(main_vent_flap_pointS);
                            main_vent_flap_series.setColor(Color.WHITE);

                            LineGraphSeries<DataPoint> room_vent_flap_series = new LineGraphSeries<DataPoint>(room_vent_flap_pointS);
                            room_vent_flap_series.setColor(Color.CYAN);

                            //Инициализируем графики
                            GraphView graph_t_inside = (GraphView) findViewById(R.id.graph_t_inside);
                            GraphView graph_t_outside = (GraphView) findViewById(R.id.graph_t_outside);
                            GraphView graph_Humidity = (GraphView) findViewById(R.id.graph_Humidity);
                            GraphView graph_Pressure = (GraphView) findViewById(R.id.graph_Pressure);
                            GraphView graph_Humidity_outside = (GraphView) findViewById(R.id.graph_Humidity_outside);

                            //Параметры графика домашней температуры
                            graph_t_inside.getGridLabelRenderer().setNumHorizontalLabels(4);
                            graph_t_inside.getGridLabelRenderer().setNumVerticalLabels(5);
                            graph_t_inside.getGridLabelRenderer().setGridColor(Color.GRAY);
                            graph_t_inside.getGridLabelRenderer().setHorizontalLabelsColor(Color.GRAY);
                            graph_t_inside.getGridLabelRenderer().setVerticalLabelsColor(Color.GRAY);
                            graph_t_inside.getViewport().setMinX(date_min.getTime());
                            graph_t_inside.getViewport().setMaxX(date_max.getTime());
                            graph_t_inside.getViewport().setXAxisBoundsManual(true);
                            graph_t_inside.getViewport().setMinY(t_intside_min - 0.1);
                            graph_t_inside.getViewport().setMaxY(t_intside_max + 0.1);
                            graph_t_inside.getViewport().setYAxisBoundsManual(true);
                            graph_t_inside.setTitle("Температура в доме (°C)");
                            graph_t_inside.setTitleTextSize(25);
                            graph_t_inside.setTitleColor(Color.GRAY);

                            //Параметры графика уличной температуры
                            graph_t_outside.getGridLabelRenderer().setNumHorizontalLabels(4);
                            graph_t_outside.getGridLabelRenderer().setNumVerticalLabels(5);
                            graph_t_outside.getGridLabelRenderer().setGridColor(Color.GRAY);
                            graph_t_outside.getGridLabelRenderer().setHorizontalLabelsColor(Color.GRAY);
                            graph_t_outside.getGridLabelRenderer().setVerticalLabelsColor(Color.GRAY);
                            graph_t_outside.getViewport().setMinX(date_min.getTime());
                            graph_t_outside.getViewport().setMaxX(date_max.getTime());
                            graph_t_outside.getViewport().setXAxisBoundsManual(true);
                            graph_t_outside.getViewport().setMinY(t_outside_min - 0.5);
                            graph_t_outside.getViewport().setMaxY(t_outside_max + 0.5);
                            graph_t_outside.getViewport().setYAxisBoundsManual(true);
                            graph_t_outside.setTitle("Температура на улице (°C)");
                            graph_t_outside.setTitleTextSize(25);
                            graph_t_outside.setTitleColor(Color.GRAY);

                            //Параметры графика влажности
                            graph_Humidity.getGridLabelRenderer().setNumHorizontalLabels(4);
                            graph_Humidity.getGridLabelRenderer().setNumVerticalLabels(5);
                            graph_Humidity.getGridLabelRenderer().setGridColor(Color.GRAY);
                            graph_Humidity.getGridLabelRenderer().setHorizontalLabelsColor(Color.GRAY);
                            graph_Humidity.getGridLabelRenderer().setVerticalLabelsColor(Color.GRAY);
                            graph_Humidity.getViewport().setMinX(date_min.getTime());
                            graph_Humidity.getViewport().setMaxX(date_max.getTime());
                            graph_Humidity.getViewport().setXAxisBoundsManual(true);
                            graph_Humidity.getViewport().setMinY(Humidity_min - 0.5);
                            graph_Humidity.getViewport().setMaxY(Humidity_max + 0.5);
                            graph_Humidity.getViewport().setYAxisBoundsManual(true);
                            graph_Humidity.setTitle("Влажность в гараже (%)");
                            graph_Humidity.setTitleTextSize(25);
                            graph_Humidity.setTitleColor(Color.GRAY);

                            //Параметры графика давления
                            graph_Pressure.getGridLabelRenderer().setNumHorizontalLabels(4);
                            graph_Pressure.getGridLabelRenderer().setNumVerticalLabels(5);
                            graph_Pressure.getGridLabelRenderer().setGridColor(Color.GRAY);
                            graph_Pressure.getGridLabelRenderer().setHorizontalLabelsColor(Color.GRAY);
                            graph_Pressure.getGridLabelRenderer().setVerticalLabelsColor(Color.GRAY);
                            graph_Pressure.getViewport().setMinX(date_min.getTime());
                            graph_Pressure.getViewport().setMaxX(date_max.getTime());
                            graph_Pressure.getViewport().setXAxisBoundsManual(true);
                            graph_Pressure.getViewport().setMinY(Pressure_min - 0.2);
                            graph_Pressure.getViewport().setMaxY(Pressure_max + 0.2);
                            graph_Pressure.getViewport().setYAxisBoundsManual(true);
                            graph_Pressure.setTitle("Давление (мм.рт.ст.)");
                            graph_Pressure.setTitleTextSize(25);
                            graph_Pressure.setTitleColor(Color.GRAY);

                            //Параметры графика влажности на улице
                            graph_Humidity_outside.getGridLabelRenderer().setNumHorizontalLabels(4);
                            graph_Humidity_outside.getGridLabelRenderer().setNumVerticalLabels(5);
                            graph_Humidity_outside.getGridLabelRenderer().setGridColor(Color.GRAY);
                            graph_Humidity_outside.getGridLabelRenderer().setHorizontalLabelsColor(Color.GRAY);
                            graph_Humidity_outside.getGridLabelRenderer().setVerticalLabelsColor(Color.GRAY);
                            graph_Humidity_outside.getViewport().setMinX(date_min.getTime());
                            graph_Humidity_outside.getViewport().setMaxX(date_max.getTime());
                            graph_Humidity_outside.getViewport().setXAxisBoundsManual(true);
                            graph_Humidity_outside.getViewport().setMinY(Humidity_outside_min - 0.2);
                            graph_Humidity_outside.getViewport().setMaxY(Humidity_outside_max + 0.2);
                            graph_Humidity_outside.getViewport().setYAxisBoundsManual(true);
                            graph_Humidity_outside.setTitle("Влажность на улице (%)");
                            graph_Humidity_outside.setTitleTextSize(25);
                            graph_Humidity_outside.setTitleColor(Color.GRAY);

                            //Параметры графика заслонок
                            graph_Humidity.getGridLabelRenderer().setVerticalLabelsSecondScaleColor(Color.GRAY);
                            graph_Humidity.getSecondScale().setMinY(Math.max(Math.min(main_vent_flap_min, room_vent_flap_min) - 0.5, 0));
                            graph_Humidity.getSecondScale().setMaxY(Math.max(main_vent_flap_max, room_vent_flap_max) + 0.5);

                            //Легенда
                            Humidity_series.setTitle("Влажность");
                            main_vent_flap_series.setTitle("Гараж");
                            room_vent_flap_series.setTitle("Комната");
                            graph_Humidity.getLegendRenderer().setTextColor(Color.GRAY);
                            graph_Humidity.getLegendRenderer().setTextSize(25);
                            graph_Humidity.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
                            graph_Humidity.getLegendRenderer().setVisible(true);

                            //Устанавливаем параметры меток
                            SetLableFormater(graph_t_inside, graph_t_outside, graph_Humidity, graph_Pressure, graph_Humidity_outside);

                            //Строим графики
                            graph_t_inside.addSeries(t_inside_series);
                            graph_t_outside.addSeries(t_outside_series);
                            graph_Humidity.addSeries(Humidity_series);
                            graph_Pressure.addSeries(Pressure_series);
                            graph_Humidity_outside.addSeries(Humidity_outside_series);

                            graph_Humidity.getSecondScale().addSeries(main_vent_flap_series);
                            graph_Humidity.getSecondScale().addSeries(room_vent_flap_series);

                            //"Слушатель" нажатия на график домашней температуры
                            t_inside_series.setOnDataPointTapListener(new OnDataPointTapListener() {
                                @Override
                                public void onTap(Series series, DataPointInterface dataPoint) {
                                    Toast.makeText(BuildingGraphsLast24Hour.this, "" + new Date((long) dataPoint.getX()).toString().substring(4, 19) + "\n\t\t" + dataPoint.getY() + "°C", Toast.LENGTH_LONG).show();
                                }
                            });
                            //"Слушатель" нажатия на график уличной температуры
                            t_outside_series.setOnDataPointTapListener(new OnDataPointTapListener() {
                                @Override
                                public void onTap(Series series, DataPointInterface dataPoint) {
                                    Toast.makeText(BuildingGraphsLast24Hour.this, "" + new Date((long) dataPoint.getX()).toString().substring(4, 19) + "\n\t\t" + dataPoint.getY() + "°C", Toast.LENGTH_LONG).show();
                                }
                            });
                            //"Слушатель" нажатия на график влажности
                            Humidity_series.setOnDataPointTapListener(new OnDataPointTapListener() {
                                @Override
                                public void onTap(Series series, DataPointInterface dataPoint) {
                                    Toast.makeText(BuildingGraphsLast24Hour.this, "" + new Date((long) dataPoint.getX()).toString().substring(4, 19) + "\n\t\t" + dataPoint.getY() + "%", Toast.LENGTH_LONG).show();
                                }
                            });
                            //"Слушатель" нажатия на график давления
                            Pressure_series.setOnDataPointTapListener(new OnDataPointTapListener() {
                                @Override
                                public void onTap(Series series, DataPointInterface dataPoint) {
                                    Toast.makeText(BuildingGraphsLast24Hour.this, "" + new Date((long) dataPoint.getX()).toString().substring(4, 19) + "\n" + dataPoint.getY() + " мм.рт.ст", Toast.LENGTH_LONG).show();
                                }
                            });
                            //"Слушатель" нажатия на график влажности на улице
                            Humidity_outside_series.setOnDataPointTapListener(new OnDataPointTapListener() {
                                @Override
                                public void onTap(Series series, DataPointInterface dataPoint) {
                                    Toast.makeText(BuildingGraphsLast24Hour.this, "" + new Date((long) dataPoint.getX()).toString().substring(4, 19) + "\n" + dataPoint.getY() + " %", Toast.LENGTH_LONG).show();
                                }
                            });
                            //"Слушатель" нажатия на график положения заслонок общее
                            main_vent_flap_series.setOnDataPointTapListener(new OnDataPointTapListener() {
                                @Override
                                public void onTap(Series series, DataPointInterface dataPoint) {
                                    Toast.makeText(BuildingGraphsLast24Hour.this, "" + new Date((long) dataPoint.getX()).toString().substring(4, 19) + "\n" + dataPoint.getY() + "°", Toast.LENGTH_LONG).show();
                                }
                            });
                            //"Слушатель" нажатия на график положения заслонок в комнате
                            room_vent_flap_series.setOnDataPointTapListener(new OnDataPointTapListener() {
                                @Override
                                public void onTap(Series series, DataPointInterface dataPoint) {
                                    Toast.makeText(BuildingGraphsLast24Hour.this, "" + new Date((long) dataPoint.getX()).toString().substring(4, 19) + "\n" + dataPoint.getY() + "°", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            newFragment.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(BuildingGraphsLast24Hour.this);
                            builder.setTitle("Внимание!")
                                    .setMessage("Отсутствуют данные, попробуйте позже")
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
            } catch (JSONException e) {
                e.printStackTrace();

            } catch (NullPointerException e) {
                //Сообщение в случае отсутствия соединения
                runOnUiThread(new Runnable() {
                    public void run() {
                        newFragment.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(BuildingGraphsLast24Hour.this);
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

    public void SetLableFormater(GraphView graph_t_inside, GraphView graph_t_outside, GraphView graph_Humidity, GraphView graph_Pressure,
                                 GraphView graph_Humidity_outside) {
        graph_t_inside.getGridLabelRenderer().setLabelFormatter(new LabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    Long m = new Long((long) value);
                    Calendar n = Calendar.getInstance();
                    Date y = n.getTime();
                    y.setTime(m);
                    return y.toString().substring(10, 15) + "0";
                } else {
                    // show currency for y values
                    return String.format("%.1f", value).replace(",", ".");
                }
            }

            @Override
            public void setViewport(Viewport viewport) {
            }
        });
        graph_t_outside.getGridLabelRenderer().setLabelFormatter(new LabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    Long m = new Long((long) value);
                    Calendar n = Calendar.getInstance();
                    Date y = n.getTime();
                    y.setTime(m);
                    return y.toString().substring(10, 15) + "0";
                } else {
                    // show currency for y values
                    return String.format("%.1f", value).replace(",", ".");
                }
            }

            @Override
            public void setViewport(Viewport viewport) {
            }
        });
        graph_Humidity.getGridLabelRenderer().setLabelFormatter(new LabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    Long m = new Long((long) value);
                    Calendar n = Calendar.getInstance();
                    Date y = n.getTime();
                    y.setTime(m);
                    return y.toString().substring(10, 15) + "0";
                } else {
                    // show currency for y values
                    return String.format("%.1f", value).replace(",", ".");
                }
            }

            @Override
            public void setViewport(Viewport viewport) {
            }
        });
        graph_Pressure.getGridLabelRenderer().setLabelFormatter(new LabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    Long m = new Long((long) value);
                    Calendar n = Calendar.getInstance();
                    Date y = n.getTime();
                    y.setTime(m);
                    return y.toString().substring(10, 15) + "0";
                } else {
                    // show currency for y values
                    return String.format("%.1f", value).replace(",", ".");
                }
            }

            @Override
            public void setViewport(Viewport viewport) {
            }
        });

        graph_Humidity_outside.getGridLabelRenderer().setLabelFormatter(new LabelFormatter() {
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    Long m = new Long((long) value);
                    Calendar n = Calendar.getInstance();
                    Date y = n.getTime();
                    y.setTime(m);
                    return y.toString().substring(10, 15) + "0";
                } else {
                    // show currency for y values
                    return String.format("%.1f", value).replace(",", ".");
                }
            }

            @Override
            public void setViewport(Viewport viewport) {
            }
        });
    }

    public String loadUrl() {
        String domain = sPref.getString(URL_DATA, "");
        return domain;
    }

}
