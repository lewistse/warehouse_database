package com.google.firebase.quickstart.database.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.quickstart.database.helper.PhotoHelper;

import java.io.IOException;

public class LoadDriverPhotoAsync extends AsyncTask<String, Void, Bitmap> {

    private static final String LOG_TAG = LoadDriverPhotoAsync.class.getSimpleName();

    private Context mContext;
    private ImageView mDriverPhotoImageView;

    public LoadDriverPhotoAsync(Context context, ImageView driverPhotoImageView) {
        mContext = context;
        mDriverPhotoImageView = driverPhotoImageView;
    }

    @Override
    protected Bitmap doInBackground(String... args) {
        Bitmap photoBitmap = null;
        try {
            photoBitmap = PhotoHelper.getBitmapFromPhotoPath(mContext, args[0]);
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
        return photoBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (bitmap != null) mDriverPhotoImageView.setImageBitmap(bitmap);
    }

}