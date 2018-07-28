package com.example.valera.homeweatherstation.MySqlReader;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DigitalClock;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.valera.homeweatherstation.BuildGraphs.BuildingGraphsForCustomPeriod;
import com.example.valera.homeweatherstation.BuildGraphs.BuildingGraphsLast24Hour;
import com.example.valera.homeweatherstation.BuildGraphs.LoadingDialogs.LoadingDialogV3;
import com.example.valera.homeweatherstation.R;
import com.example.valera.homeweatherstation.Setting.SettingDialog;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
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

public class RecordData extends AppCompatActivity {

    //TextView
    TextView t_inside_text;
    TextView t_outside_text;
    TextView Pressure_text;
    TextView Humidity_text;

    ImageView thermometer;
    ImageView imageView;

    DigitalClock textMyFont;

    LinearLayout layout_visibility;

    DataPoint[] Pressure_pointS;

    // url получения списка данных
    private String domain;
    private String url_get_data;
    private String url_get_pressure_data;

    private SharedPreferences sPref;
    private final String URL_DATA = "url";

    // JSON тэги
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_DATA = "data";
    private static final String TAG_NUMBER = "number";
    private static final String TAG_DATETIME = "dateTime";
    private static final String TAG_T_INSIDE = "t_inside";
    private static final String TAG_T_OUTSIDE = "t_outside";
    private static final String TAG_PRESSURE = "Pressure";
    private static final String TAG_HUMIDITY = "Humidity";

    //Экземпляр класса получения данных
    ReceiveData receivedata = new ReceiveData();

    //Строки данных
    String a;
    String b;
    String c;
    String d;

    double outside_temperature;

    Date date_min;
    Date date_max;
    Double Pressure_min = 0.0;
    Double Pressure_max = 0.0;

    DialogFragment newFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        sPref = getDefaultSharedPreferences(this);
        domain = loadUrl();
        if (domain.equals("")) {
            domain = "tvv";
            saveData(domain);
        }
        url_get_data = "http://" + domain + getResources().getString(R.string.url_get_data);
        url_get_pressure_data = "http://" + domain + getResources().getString(R.string.url_get_pressure_data);

        layout_visibility = (LinearLayout) findViewById(R.id.layout_visibility);
        layout_visibility.setVisibility(View.INVISIBLE);

        LoadingInfo();
        new GetDataFromMySQL().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_record_data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_build_last_24_hour:
                Intent intent = new Intent(RecordData.this, BuildingGraphsLast24Hour.class);
                startActivity(intent);
                return true;
            case R.id.action_build_graphs_for_custom_period:
                intent = new Intent(RecordData.this, BuildingGraphsForCustomPeriod.class);
                startActivity(intent);
                return true;
            case R.id.my_settings:
                FragmentManager manager = getSupportFragmentManager();
                SettingDialog myDialogFragment = new SettingDialog();
                myDialogFragment.show(manager, "SettingDialog");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Загрузка...
    public void LoadingInfo() {

        textMyFont = (DigitalClock) findViewById(R.id.TimeClock);

        thermometer = (ImageView) findViewById(R.id.t_outside_image);
        imageView = (ImageView) findViewById(R.id.imageView);

        t_inside_text = (TextView) findViewById(R.id.t_inside);
        t_outside_text = (TextView) findViewById(R.id.t_outside);
        Pressure_text = (TextView) findViewById(R.id.Pressure);
        Humidity_text = (TextView) findViewById(R.id.Humidity);

        textMyFont.setTypeface(Typeface.createFromAsset(
                getAssets(), "fonts/Algerian.ttf"));

        t_inside_text.setTypeface(Typeface.createFromAsset(
                getAssets(), "fonts/Algerian.ttf"));
        t_outside_text.setTypeface(Typeface.createFromAsset(
                getAssets(), "fonts/Algerian.ttf"));
        Pressure_text.setTypeface(Typeface.createFromAsset(
                getAssets(), "fonts/Algerian.ttf"));
        Humidity_text.setTypeface(Typeface.createFromAsset(
                getAssets(), "fonts/Algerian.ttf"));

        newFragment = new LoadingDialogV3();
        newFragment.show(getFragmentManager(), "Loading");
    }

    public void Refresh() {
        LoadingInfo();

        domain = loadUrl();
        url_get_data = "http://" + domain + getResources().getString(R.string.url_get_data);
        url_get_pressure_data = "http://" + domain + getResources().getString(R.string.url_get_pressure_data);

        new GetDataFromMySQL().execute();
    }

    public void UpdateInfo(View view) {
        Refresh();
    }

    //Фоновый поток для загрузки данных
    class GetDataFromMySQL extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String[] params) {
            int success;
            int hour_data;
            int number;

            try {

                JSONObject json = receivedata.ReceiveData(url_get_data);
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {

                    JSONArray dataObj = json.getJSONArray(TAG_DATA);
                    JSONObject data = dataObj.getJSONObject(0);

                    a = data.getString(TAG_T_OUTSIDE) + "°C";
                    b = data.getString(TAG_T_INSIDE) + "°C";
                    c = data.getString(TAG_PRESSURE);
                    d = data.getString(TAG_HUMIDITY) + "%";

                    final Calendar cal = Calendar.getInstance();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Sunset_Sunrise(cal.get(Calendar.MONTH), cal.get(Calendar.HOUR_OF_DAY), imageView);
                        }
                    });

                    outside_temperature = data.getDouble(TAG_T_OUTSIDE);

                    JSONObject jsonPressure = receivedata.ReceiveData(url_get_pressure_data);
                    success = jsonPressure.getInt(TAG_SUCCESS);
                    number = jsonPressure.getInt(TAG_NUMBER);

                    Pressure_pointS = new DataPoint[number];
                    if (success == 1)

                    {
                        for (hour_data = 0; hour_data < number; hour_data++) {

                            JSONArray dataObjPressure = jsonPressure.getJSONArray(TAG_DATA + hour_data);
                            JSONObject dataPressure = dataObjPressure.getJSONObject(0);

                            //Формируем дату
                            String DateTime = dataPressure.getString(TAG_DATETIME);
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
                            java.util.Date d1 = calendar.getTime();

                            //Границы даты
                            if (hour_data == 0) {
                                date_min = d1;
                            }
                            if (hour_data == number - 1) {
                                date_max = d1;
                            }

                            DataPoint Pressure_point = new DataPoint(d1, dataPressure.getDouble(TAG_PRESSURE));
                            Pressure_pointS[hour_data] = Pressure_point;

                            if (dataPressure.getDouble(TAG_PRESSURE) > Pressure_max) {
                                Pressure_max = dataPressure.getDouble(TAG_PRESSURE);
                            }
                            if (Pressure_min == 0.0) {
                                Pressure_min = dataPressure.getDouble(TAG_PRESSURE);
                            }
                            if (dataPressure.getDouble(TAG_PRESSURE) < Pressure_min) {
                                Pressure_min = dataPressure.getDouble(TAG_PRESSURE);
                            }
                        }
                    }

                    //Запуск главного потока для работы с UI
                    runOnUiThread(new Runnable() {
                        public void run() {

                            newFragment.dismiss();

                            layout_visibility.setVisibility(View.VISIBLE);

                            t_inside_text = (TextView) findViewById(R.id.t_inside);
                            t_outside_text = (TextView) findViewById(R.id.t_outside);
                            Pressure_text = (TextView) findViewById(R.id.Pressure);
                            Humidity_text = (TextView) findViewById(R.id.Humidity);

                            t_inside_text.setText(b + " ");
                            t_outside_text.setText(a);
                            Pressure_text.setText(c);
                            Humidity_text.setText(d);

                            if (outside_temperature < 5) {
                                thermometer.setImageDrawable(getResources().getDrawable(R.drawable.tm));
                            }
                            if (outside_temperature >= 5 && outside_temperature < 20) {
                                thermometer.setImageDrawable(getResources().getDrawable(R.drawable.tn));
                            }
                            if (outside_temperature >= 20) {
                                thermometer.setImageDrawable(getResources().getDrawable(R.drawable.th));
                            }

                            LineGraphSeries<DataPoint> Pressure_series = new LineGraphSeries<DataPoint>(Pressure_pointS);
                            Pressure_series.setColor(Color.MAGENTA);

                            GraphView graph_Pressure = (GraphView) findViewById(R.id.graph_Pressure);

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

                            graph_Pressure.addSeries(Pressure_series);

                            //"Слушатель" нажатия на график давления
                            Pressure_series.setOnDataPointTapListener(new OnDataPointTapListener() {
                                @Override
                                public void onTap(Series series, DataPointInterface dataPoint) {
                                    Toast.makeText(RecordData.this, "" + new Date((long) dataPoint.getX()).toString().substring(4, 19) + "\n" + dataPoint.getY() + " мм.рт.ст", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            newFragment.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(RecordData.this);
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
                        AlertDialog.Builder builder = new AlertDialog.Builder(RecordData.this);
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
                                .setNeutralButton("Настройки",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                                FragmentManager manager = getSupportFragmentManager();
                                                SettingDialog myDialogFragment = new SettingDialog();
                                                myDialogFragment.show(manager, "SettingDialog");
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

    public void Sunset_Sunrise(int month, int hour, ImageView imageView) {
        if (month == 0 || month == 11) {
            if (hour >= 8 && hour <= 16) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.sun_with_snow));
            }
            if (hour < 8 || hour > 16) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.moon));
            }

        }
        if (month == 1 || month == 9) {
            if (hour >= 7 && hour <= 17) {
                if (month == 1) {
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.sun_with_snow));
                }
                if (month == 9) {
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.sun_without_snow));
                }
            }
            if (hour < 7 || hour > 17) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.moon));
            }
        }
        if (month == 2 || month == 8) {
            if (hour >= 6 && hour <= 18) {
                if (month == 2) {
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.sun_with_snow));
                }
                if (month == 8) {
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.sun_without_snow));
                }
            }
            if (hour < 6 || hour > 18) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.moon));
            }
        }
        if (month == 3 || month == 4) {
            if (hour >= 5 && hour <= 19) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.sun_without_snow));
            }
            if (hour < 5 || hour > 19) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.moon));
            }
        }
        if (month == 5 || month == 6) {
            if (hour >= 5 && hour <= 20) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.sun));
            }
            if (hour < 5 || hour > 20) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.moon));
            }
        }
        if (month == 7) {
            if (hour >= 5 && hour <= 19) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.sun));
            }
            if (hour < 5 || hour > 19) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.moon));
            }
        }
        if (month == 10) {
            if (hour >= 7 && hour <= 16) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.sun_with_snow));
            }
            if (hour < 7 || hour > 16) {
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.moon));
            }
        }
    }

    public void saveData(String domain) {
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(URL_DATA, domain);
        ed.commit();
    }


    public String loadUrl() {
        String domain = sPref.getString(URL_DATA, "");
        return domain;
    }
}