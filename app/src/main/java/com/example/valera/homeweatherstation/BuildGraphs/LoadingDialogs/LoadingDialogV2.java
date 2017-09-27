package com.example.valera.homeweatherstation.BuildGraphs.LoadingDialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.example.valera.homeweatherstation.MySqlReader.RecordData;
import com.example.valera.homeweatherstation.R;

/**
 * Created by Valera on 08.04.2015.
 */
public class LoadingDialogV2 extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_loading, null))
               .setCancelable(false);

        return builder.create();
    }

    @Override
    public void onCancel (DialogInterface dialog) {
        Intent intent = new Intent(getActivity(), RecordData.class);
        startActivity(intent);
    }

}
