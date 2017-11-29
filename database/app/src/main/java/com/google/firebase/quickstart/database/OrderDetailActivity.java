package com.google.firebase.quickstart.database;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.fragment.DriverPhotoDialogFragment;
import com.google.firebase.quickstart.database.helper.PhotoHelper;
import com.google.firebase.quickstart.database.models.OrderDetail;
import com.google.firebase.quickstart.database.models.User;

import com.google.firebase.quickstart.database.helper.PhotoHelper;
import com.google.firebase.quickstart.database.helper.LoadDriverPhotoAsync;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.security.AccessController.getContext;

public class OrderDetailActivity extends BaseActivity implements View.OnClickListener, DialogInterface.OnClickListener {

    private static final String TAG = "OrderDetailActivity";

    public static final String EXTRA_ORDER_KEY = "order_key";

    private DatabaseReference mOrderDetailsReference;
    private ValueEventListener mOrderListener;
    private String mOrderKey;


    private TextView mOrderNumberView;
    private TextView mUserNameView;
    private TextView mConfirmTimeStampView;
    private EditText mDriverNameField;
    private EditText mDriverHkidField;
    private EditText mDriverCarPlateField;
    private EditText mCarrierCompanyNameField;
    private TextView mOrderStatusField;
    private Button mConfirmButton;

    private static final int REQUEST_CODE_TAKE_PHOTO = 0;
    private static final int REQUEST_CODE_CHOOSE_PHOTO = 1;
    private static final String DRIVER_PHOTO_DIALOG_TAG = "DRIVER_PHOTO_DIALOG_TAG";
    private static final int RC_TAKE_PICTURE = 101;

    private static final String KEY_FILE_URI = "key_file_uri";
    private static final String KEY_DOWNLOAD_URL = "key_download_url";

    private BroadcastReceiver mBroadcastReceiver;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;

    private Uri mDownloadUrl = null;
    private Uri mFileUri = null;

    // Global variable to hold the path of the photo file which gets created each time the user
    // capture a photo using camera intent. This is stored globally so the file can be accessed
    // later on. If the user cancel taking a picture via the camera intent or decided to choose
    // a photo from the gallery instead, the file needs to be deleted as it's no longer needed.
    private String mTempPhotoFilePath;
    private ImageView mDriverPhotoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_order_detail);

        // Initialize Firebase Auth : May not needed
        mAuth = FirebaseAuth.getInstance();

        // Click listeners
//        findViewById(R.id.button_download).setOnClickListener(this);

        // Restore instance state
        if (savedInstanceState != null) {
            mFileUri = savedInstanceState.getParcelable(KEY_FILE_URI);
            mDownloadUrl = savedInstanceState.getParcelable(KEY_DOWNLOAD_URL);
        }
        onNewIntent(getIntent());

        // Local broadcast receiver
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive:" + intent);
                hideProgressDialog();

                switch (intent.getAction()) {
                    case MyDownloadService.DOWNLOAD_COMPLETED:
                        // Get number of bytes downloaded
                        long numBytes = intent.getLongExtra(MyDownloadService.EXTRA_BYTES_DOWNLOADED, 0);

                        // Alert success
                        showMessageDialog(getString(R.string.success), String.format(Locale.getDefault(),
                                "%d bytes downloaded from %s",
                                numBytes,
                                intent.getStringExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH)));
                        break;
                    case MyDownloadService.DOWNLOAD_ERROR:
                        // Alert failure
                        showMessageDialog("Error", String.format(Locale.getDefault(),
                                "Failed to download from %s",
                                intent.getStringExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH)));
                        break;
                    case MyUploadService.UPLOAD_COMPLETED:
                        File photoFile = new File(mTempPhotoFilePath);
                        photoFile.delete();

                    case MyUploadService.UPLOAD_ERROR:
                        onUploadResultIntent(intent);

                        Log.w("IMAGE_URL", "Path is " + mDownloadUrl.toString());
                        try{// Here I'm setting image in ImageView
                            mDriverPhotoImageView.setImageBitmap(getImageBitmap(mDownloadUrl.toString()));
                        }catch (Exception e){
                            System.out.print(e.getCause());
                        }
                        break;
                }
            }
        };

        // Get post key from intent
        mOrderKey = getIntent().getStringExtra(EXTRA_ORDER_KEY);
        if (mOrderKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_ORDER_KEY");
        }

        // Initialize Database
        mOrderDetailsReference = FirebaseDatabase.getInstance().getReference().child("orders").child(mOrderKey);

        // Initialize Views
        mOrderNumberView = findViewById(R.id.order_number_title);
        mUserNameView = findViewById(R.id.user_name_title);
        mDriverNameField = findViewById(R.id.driver_name_edit);
        mDriverHkidField = findViewById(R.id.driver_hkid_edit);
        mDriverCarPlateField = findViewById(R.id.car_plate_edit);
        mConfirmTimeStampView= findViewById(R.id.time_stamp_view);
        mCarrierCompanyNameField = findViewById(R.id.carrier_company_name_edit);
        mOrderStatusField = findViewById(R.id.order_status_title);
        mConfirmButton = findViewById(R.id.confirm_button);

        mDriverPhotoImageView = (ImageView) findViewById(R.id.driver_photo_image_view);

        mConfirmButton.setOnClickListener(this);

    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Check if this Activity was launched by clicking on an upload notification
        if (intent.hasExtra(MyUploadService.EXTRA_DOWNLOAD_URL)) {
            onUploadResultIntent(intent);
        }
    }


    @Override
    public void onStart() {
        super.onStart();

        mAuth.getCurrentUser();

        // Register receiver for uploads and downloads
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mBroadcastReceiver, MyDownloadService.getIntentFilter());
        manager.registerReceiver(mBroadcastReceiver, MyUploadService.getIntentFilter());

        // Add value event listener to the post
        // [START post_value_event_listener]

        ValueEventListener orderListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI

                OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);

                // [START_EXCLUDE]
                mOrderNumberView.setText(orderDetail.orderNumber);
                mUserNameView.setText(orderDetail.userName);
                mDriverNameField.setText(orderDetail.driverName);
                mDriverHkidField.setText(orderDetail.driverHkid);
                mDriverCarPlateField.setText(orderDetail.carPlateNumber);
                mConfirmTimeStampView.setText(orderDetail.confirmTimeStamp);
                mCarrierCompanyNameField.setText(orderDetail.carrierCompanyName);
//                mOrderStatusField.setText(orderDetail.orderStatus);

//                mDriverPhotoUrlField.setText(orderDetail.driverPhotoUrl);
                if(orderDetail.driverPhotoUrl!=null)
                    mDriverPhotoImageView.setImageBitmap(getImageBitmap(orderDetail.driverPhotoUrl));
                Log.d(TAG, "mOrderNumberView" );

                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadOrders:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(OrderDetailActivity.this, "Failed to load orders.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        //mPostReference.addValueEventListener(postListener);
        mOrderDetailsReference.addValueEventListener(orderListener);
        // [END post_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mOrderListener = orderListener;

    }

    @Override
    public void onStop() {
        super.onStop();

        // Unregister download receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);

        // Remove post value event listener
        if (mOrderListener != null) {
            mOrderDetailsReference.removeEventListener(mOrderListener);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putParcelable(KEY_FILE_URI, mFileUri);
        out.putParcelable(KEY_DOWNLOAD_URL, mDownloadUrl);
    }

    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // Save the File URI
        mFileUri = fileUri;

        // Clear the last download, if any
        mAuth.getCurrentUser();
        mDownloadUrl = null;

        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(new Intent(this, MyUploadService.class)
                .putExtra(MyUploadService.EXTRA_FILE_URI, fileUri)
                .setAction(MyUploadService.ACTION_UPLOAD));

        // Show loading spinner
        showProgressDialog(getString(R.string.progress_uploading));
    }

    private void beginDownload() {
        // Get path
        String path = "photos/" + mFileUri.getLastPathSegment();

        // Kick off MyDownloadService to download the file
        Intent intent = new Intent(this, MyDownloadService.class)
                .putExtra(MyDownloadService.EXTRA_DOWNLOAD_PATH, path)
                .setAction(MyDownloadService.ACTION_DOWNLOAD);
        startService(intent);

        // Show loading spinner
        showProgressDialog(getString(R.string.progress_downloading));
    }

    private Bitmap getImageBitmap(String url) {
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e(TAG, "Error getting bitmap", e);
        }
        return bm;
    }

    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void launchCamera() {
        Log.d(TAG, "launchCamera");
        Bitmap bitmap = null;

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            // Create a File where the photo will be saved to.
            File photoFile = null;
            try {
                photoFile = PhotoHelper.createPhotoFile(this);
            } catch (IOException e) {
                Log.e("LOG_TAG", e.getMessage(), e);
            }
            if (photoFile != null) {
                // Save the photo file path globally.
                mTempPhotoFilePath = photoFile.getAbsolutePath();
                // Get the file content URI using FileProvider to avoid FileUriExposedException.
                Uri photoUri = FileProvider.getUriForFile(this, "com.google.firebase.quickstart.database", photoFile);
                // Set the file content URI as an intent extra and dispatch the camera intent.
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(cameraIntent,  REQUEST_CODE_TAKE_PHOTO);
            }
        }
    }


    private void onUploadResultIntent(Intent intent) {
        // Got a new intent from MyUploadService with a success or failure
        mDownloadUrl = intent.getParcelableExtra(MyUploadService.EXTRA_DOWNLOAD_URL);
        mFileUri = intent.getParcelableExtra(MyUploadService.EXTRA_FILE_URI);
    }

    private void showMessageDialog(String title, String message) {
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .create();
        ad.show();
    }

    private void showProgressDialog(String caption) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.setMessage(caption);
        mProgressDialog.show();
    }

//    private void hideProgressDialog() {
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        if (i == R.id.confirm_button) {

            confirmUpdate();

            // Notification should be called here

        }
    }


    private void confirmUpdate() {
        final String uid = getUid();

        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user information
                        User user = dataSnapshot.getValue(User.class);
                        String userName = user.username;
                        String driverPhotoUrlText=mDownloadUrl.toString();

                        //Lock current time
                        java.util.Calendar confirmtTime = java.util.Calendar.getInstance();
                        //long timeStamp = eventDate.getTimeInMillis();
                        Date TimeStamp = confirmtTime.getTime();
                        Log.d(TAG, "TimeStamp: " + TimeStamp);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                        String confirmTimeStamp = format.format(TimeStamp);
                        Log.d(TAG, "confirmTimeStamp: " + confirmTimeStamp);

                        // Create new comment object
                        String orderNumberText = mOrderNumberView.getText().toString();
                        String driverNameText = mDriverNameField.getText().toString();
                        String driverHkidText = mDriverHkidField.getText().toString();
                        String carPlateNumberText = mDriverCarPlateField.getText().toString();
                        String carrierCompanyNameText = mCarrierCompanyNameField.getText().toString();
//                        String orderStatusText = mOrderStatusField.getText().toString();
                        String orderStatusText = "Fulfilled";

                        OrderDetail orderDetail = new OrderDetail(uid, userName, orderNumberText ,driverNameText, driverHkidText, carPlateNumberText,confirmTimeStamp, carrierCompanyNameText,driverPhotoUrlText, orderStatusText);
                        //String driverPhotoUrlText = mDriverPhotoUrlField.getText().toString();

                        Log.d(TAG,"uid: " +  uid);
                        Log.d(TAG,"userName: " +  userName);
                        Log.d(TAG,"orderNumberText: " +  orderNumberText);
                        Log.d(TAG,"driverNameText: " +  driverNameText);
                        Log.d(TAG,"driverHkidText: " +  driverHkidText);
                        Log.d(TAG,"carPlateNumberText: " +  carPlateNumberText);
                        Log.d(TAG,"confirmTimeStamp: " +  confirmTimeStamp);
                        Log.d(TAG,"carrierCompanyName: " +  carrierCompanyNameText);
                        Log.d(TAG,"driverPhotoUrlText: " +  driverPhotoUrlText);
                        Log.d(TAG,"orderStatusText: " +  orderStatusText);

                        // Push the ordes, it will appear in the list
                        //mOrderDetailsReference.push().setValue(orderDetail);
                        mOrderDetailsReference.setValue(orderDetail);

                        Log.d(TAG,"uid: " +  uid);
                        Log.d(TAG,"userName: " +  userName);
                        Log.d(TAG,"mDriverNameField: " +  driverNameText);
                        Log.d(TAG,"mDriverHkidField: " +  driverHkidText);
                        Log.d(TAG,"mDriverCarPlateField: " +  carPlateNumberText);
                        Log.d(TAG,"confirmTimeStamp: " +  confirmTimeStamp);
                        Log.d(TAG,"mDriverPhotoUrlField: " +  driverPhotoUrlText);

                        // Clear the field
                        //mCommentField.setText(null);

                        //mDriverNameField.setText(null);
                        //mDriverHkidField.setText(null);
                        //mDriverCarPlateField.setText(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


   /**
     * AlertDialog interface callback method which gets invoked when the user selects one of the
     * available options on the product photo dialog.
     * @param dialogInterface - the dialog interface.
     * @param i - position of the selected option.
     */
    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (i == 0) {
            // The user selects 'Take photo' option. Dispatch the camera intent.
            launchCamera();
        } else {
            // The user selects 'Choose photo' option. Dispatch the choose photo intent.
            Intent choosePhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (choosePhotoIntent.resolveActivity(getPackageManager()) != null)
                startActivityForResult(choosePhotoIntent, REQUEST_CODE_CHOOSE_PHOTO);
        }
    }

    public void viewDriverPhoto(View view) {

        PhotoHelper.dispatchViewImageIntent(this, mDriverPhotoImageView.getTag());
        Log.d(TAG, "viewDriverPhoto: " +" show photo");

    }

    /**
     * Method that gets invoked when the user presses the 'photo camera' floating action button.
     * This method will inflate the product photo dialog using the product photo dialog fragment.
     * @param view - 'photo camera' floating action button.
     */
    public void showDriverPhotoDialog(View view) {
        DriverPhotoDialogFragment dialogFragment = new DriverPhotoDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), DRIVER_PHOTO_DIALOG_TAG);
        Log.d(TAG, "showDriverPhotoDialog: " +"Doing Nothing");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
        // Check the request code to determine which intent was dispatched.
        if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
           if (resultCode == RESULT_OK) {
               Bitmap b= BitmapFactory.decodeFile(mTempPhotoFilePath);
               Bitmap out = getResizedBitmap(b, 1024);
               File file = new File(mTempPhotoFilePath.substring(0,mTempPhotoFilePath.lastIndexOf("/")), "Final_"+ mTempPhotoFilePath.substring(mTempPhotoFilePath.lastIndexOf("/")+1) );
               FileOutputStream fOut;
               try {
                   fOut = new FileOutputStream(file);
                   out.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                   fOut.flush();
                   fOut.close();
                   b.recycle();
                   out.recycle();
               } catch (Exception e) {}
                // Original photo
//               mFileUri =Uri.fromFile(new File(mTempPhotoFilePath)) ;
                // Resized photo
               mFileUri =Uri.fromFile(file) ;
               if (mFileUri != null) {
                   uploadFromUri(mFileUri);
                // Take photo successful. If the user has previously set a captured photo on the
                // ImageView, that photo file needs to be deleted since it will be replaced now.
//                PhotoHelper.deleteCapturedPhotoFile(mDriverPhotoImageView.getTag());
//                // Save the file uri as a tag and display the captured photo on the ImageView.
//                mDriverPhotoImageView.setTag(mTempPhotoFilePath);
//                   new LoadDriverPhotoAsync(this, mDriverPhotoImageView).execute(mDownloadUrl.toString());
               } else {
                   Log.w(TAG, "File URI is null");
               }

            } else if (resultCode == RESULT_CANCELED) {
                // The user cancelled taking a photo. The photo file created from the camera intent
                // is just an empty file so delete it since we don't need it anymore.
                File photoFile = new File(mTempPhotoFilePath);
                photoFile.delete();
            }
        } else if (requestCode == REQUEST_CODE_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK && data != null) {
                String photoPath = getRealPathFromURI(this, data.getData());
                Bitmap b= BitmapFactory.decodeFile(photoPath);
                Bitmap out = getResizedBitmap(b, 1024); // TODO: b = null instead of a bitmap which may due to /storage/emulated/0/DCIM/Camera need root access
                File file = new File(photoPath.substring(0,photoPath.lastIndexOf("/")), "Final_"+ photoPath.substring(photoPath.lastIndexOf("/")+1) );
                FileOutputStream fOut;
                try {
                    fOut = new FileOutputStream(file);
                    out.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                    fOut.flush();
                    fOut.close();
                    b.recycle();
                    out.recycle();
                } catch (Exception e) {}
                // Original photo
                //               mFileUri =Uri.fromFile(new File(photoPath)) ;
                // Resized photo
                mFileUri =Uri.fromFile(file);
                if (mFileUri != null) {
                    uploadFromUri(mFileUri);
                }
                // Choose photo successful. Delete previously captured photo file if there's any.
                PhotoHelper.deleteCapturedPhotoFile(mDriverPhotoImageView.getTag());
//                mDriverPhotoImageView.setTag(photoPath);
//                new LoadDriverPhotoAsync(this, mDriverPhotoImageView).execute(photoPath);
            }
        }
    }
}

