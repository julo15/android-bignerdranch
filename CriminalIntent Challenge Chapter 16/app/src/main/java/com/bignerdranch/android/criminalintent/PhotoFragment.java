package com.bignerdranch.android.criminalintent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by julianlo on 12/7/15.
 */
public class PhotoFragment extends DialogFragment {

    private final static String ARG_FILE_PATH = "file_path";

    public static PhotoFragment newInstance(String filePath) {
        Bundle args = new Bundle();
        args.putString(ARG_FILE_PATH, filePath);

        PhotoFragment photoFragment = new PhotoFragment();
        photoFragment.setArguments(args);
        return photoFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String filePath = getArguments().getString(ARG_FILE_PATH);

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_photo, null);

        Bitmap bitmap = PictureUtils.getScaledBitmap(filePath, getActivity());
        ImageView photoImageView = (ImageView)v.findViewById(R.id.crime_photo_zoomed);
        photoImageView.setImageBitmap(bitmap);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.photo_dialog_title)
                .create();
    }
}
