package com.example.valera.homeweatherstation.Setting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.valera.homeweatherstation.MySqlReader.ReceiveData;
import com.example.valera.homeweatherstation.MySqlReader.RecordData;
import com.example.valera.homeweatherstation.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Valery on 17.09.2017.
 */

public class SettingDialog extends DialogFragment {

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_DATA = "data";
    private static final String TAG_MAIN = "main_vent_flap";
    private static final String TAG_ROOM = "room_vent_flap";
    private static final String TAG_MODE = "trigger_mode";

    //Экземпляр класса получения данных
    private EditText edit_domain;
    private TextView textView;
    private TextView text_main_vent_flap;
    private TextView text_room_vent_flap;
    private SeekBar seekBar_main;
    private SeekBar seekBar_room;
    private Switch switch_mode;

    private String domain;
    int trigger_mode;
    int main_progress;
    int room_progress;

    private SharedPreferences sPref;

    //Экземпляр класса получения данных
    ReceiveData receivedata = new ReceiveData();

    String url_get_data;
    String url_put_data;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.settings_layout, null);
        builder.setView(view);

        edit_domain = view.findViewById(R.id.domain);
        textView = view.findViewById(R.id.textView);
        text_main_vent_flap = view.findViewById(R.id.text_main_vent_flap);
        text_room_vent_flap = view.findViewById(R.id.text_room_vent_flap);
        seekBar_main = view.findViewById(R.id.seekBar_main);
        seekBar_room = view.findViewById(R.id.seekBar_room);
        switch_mode = view.findViewById(R.id.switch_mode);

        seekBar_main.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                text_main_vent_flap.setText("Положение заслонок в гараже: " + String.valueOf(seekBar.getProgress() * 5));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekBar_room.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                text_room_vent_flap.setText("Положение заслонок в комнате: " + String.valueOf(seekBar.getProgress() * 5));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        switch_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    seekBar_main.setEnabled(true);
                    seekBar_room.setEnabled(true);
                } else {
                    seekBar_main.setEnabled(false);
                    seekBar_room.setEnabled(false);
                }
            }
        });

        domain = ((RecordData) getActivity()).loadUrl();

        Set();

        ((Button) view.findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                domain = edit_domain.getText().toString();
                trigger_mode = switch_mode.isChecked() ? 1 : 0;
                main_progress = seekBar_main.getProgress() * 5;
                room_progress = seekBar_room.getProgress() * 5;

                textView.setText(getResources().getString(R.string.waiting));
                ((RecordData) getActivity()).saveData(domain);
                ((RecordData) getActivity()).Refresh();
                new PutDataToMySQL().execute();
                getDialog().cancel();
            }
        });

        return builder.create();
    }

    private void Set() {
        textView.setText(getResources().getString(R.string.settings));
        edit_domain.setText(domain);
        url_get_data = "http://" + domain + getResources().getString(R.string.url_get_flap_data);
        url_put_data = "http://" + domain + getResources().getString(R.string.url_put_flap_data);

        GetDataFromMySQL testAsyncTask = new GetDataFromMySQL(new FragmentCallback() {
            @Override
            public void onTaskDone(String ok) {
                setFlapProgress(ok);
            }
        });

        testAsyncTask.execute();
    }

    private void setFlapProgress(String ok) {
        if (ok.equals("ok")) {
            switch_mode.setChecked(trigger_mode == 1);
            if (trigger_mode == 1) {
                seekBar_main.setEnabled(true);
                seekBar_room.setEnabled(true);
            } else {
                seekBar_main.setEnabled(false);
                seekBar_room.setEnabled(false);
            }
            seekBar_main.setProgress(main_progress);
            seekBar_room.setProgress(room_progress);
            text_main_vent_flap.setText("Положение заслонок в гараже: " + (main_progress * 5));
            text_room_vent_flap.setText("Положение заслонок в комнате: " + (room_progress * 5));
        }
    }

    class GetDataFromMySQL extends AsyncTask<String, String, String> {
        private FragmentCallback mFragmentCallback;

        public GetDataFromMySQL(FragmentCallback fragmentCallback) {
            mFragmentCallback = fragmentCallback;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String[] params) {
            int success;

            try {
                JSONObject json = receivedata.ReceiveData(url_get_data);
                success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    JSONArray dataObj = json.getJSONArray(TAG_DATA);
                    JSONObject data = dataObj.getJSONObject(0);

                    main_progress = Integer.parseInt(data.getString(TAG_MAIN)) / 5;
                    room_progress = Integer.parseInt(data.getString(TAG_ROOM)) / 5;
                    trigger_mode = Integer.parseInt(data.getString(TAG_MODE));
                    return "ok";
                } else {
                    return "fail";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
            }
            return "fail";
        }

        protected void onPostExecute(String result) {
            mFragmentCallback.onTaskDone(result);
        }
    }

    class PutDataToMySQL extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String[] params) {
            try {
                receivedata.ReceiveData(url_put_data, trigger_mode, main_progress, room_progress);
            } catch (NullPointerException e) { }
            return null;
        }

        protected void onPostExecute(String result) { }
    }

    public interface FragmentCallback {
        public void onTaskDone(String ok);
    }
}




