package com.google.firebase.quickstart.database.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.google.firebase.quickstart.database.R;

public class DriverPhotoDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_dialog_driver_photo);
        builder.setItems(R.array.driver_photo_dialog_options, (DialogInterface.OnClickListener) getActivity());
        return builder.create();
    }

}