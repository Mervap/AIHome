package com.example.valera.homeweatherstation.Setting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.valera.homeweatherstation.MySqlReader.RecordData;
import com.example.valera.homeweatherstation.R;

/**
 * Created by Valery on 17.09.2017.
 */

public class SettingDialog extends DialogFragment {

    private static final String TAG_SUCCESS = "success";

    //Экземпляр класса получения данных
    private EditText edit_domain;
    private TextView textView;

    private String domain;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.settings_layout, null);
        builder.setView(view);

        edit_domain = view.findViewById(R.id.domain);
        textView = view.findViewById(R.id.textView);

        domain = ((RecordData) getActivity()).loadUrl();

        Set();

        ((Button) view.findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                domain = edit_domain.getText().toString();

                textView.setText(getResources().getString(R.string.waiting));
                ((RecordData) getActivity()).saveData(domain);
                ((RecordData) getActivity()).Refresh();
                getDialog().cancel();
            }
        });

        return builder.create();
    }

    private void Set() {
        textView.setText(getResources().getString(R.string.settings));
        edit_domain.setText(domain);
    }
}




