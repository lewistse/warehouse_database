package com.google.firebase.quickstart.database;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailActivity extends BaseActivity implements View.OnClickListener, DialogInterface.OnClickListener {

    private static final String TAG = "OrderDetailActivity";

    //public static final String EXTRA_POST_KEY = "post_key";
    public static final String EXTRA_ORDER_KEY = "order_key";

    //private DatabaseReference mPostReference;
    //private DatabaseReference mCommentsReference;
    private DatabaseReference mOrderDetailsReference;
    private ValueEventListener mPostListener;
    private ValueEventListener mOrderListener;
    private String mPostKey;
    private String mOrderKey;
    //private CommentAdapter mAdapter;

    private TextView mAuthorView;
    private TextView mTitleView;
    private TextView mBodyView;
    private EditText mCommentField;
    private Button mCommentButton;
    private RecyclerView mCommentsRecycler;

    private TextView mOrderNumberView;
    private TextView mUserNameView;
    private EditText mDriverNameField;
    private EditText mDriverHkidField;
    private EditText mDriverCarPlateField;
    private Button mConfirmButton;
    private RecyclerView mOrdersRecycler;

    private static final int REQUEST_CODE_TAKE_PHOTO = 0;
    private static final int REQUEST_CODE_CHOOSE_PHOTO = 1;
    private static final String DRIVER_PHOTO_DIALOG_TAG = "DRIVER_PHOTO_DIALOG_TAG";
    private String mTempPhotoFilePath;
    private ImageView mDriverPhotoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_order_detail);

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

        mConfirmButton = findViewById(R.id.confirm_button);

        mDriverPhotoImageView = (ImageView) findViewById(R.id.driver_photo_image_view);

        mConfirmButton.setOnClickListener(this);

    }

    @Override
    public void onStart() {
        super.onStart();

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

        // Remove post value event listener
        if (mOrderListener != null) {
            mOrderDetailsReference.removeEventListener(mOrderListener);
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

                        // Create new comment object
                        String orderNumberText = mOrderNumberView.getText().toString();
                        String driverNameText = mDriverNameField.getText().toString();
                        String driverHkidText = mDriverHkidField.getText().toString();
                        String carPlateNumberText = mDriverCarPlateField.getText().toString();
                        OrderDetail orderDetail = new OrderDetail(uid, userName, orderNumberText ,driverNameText, driverHkidText, carPlateNumberText);

                        Log.d(TAG,"uid: " +  uid);
                        Log.d(TAG,"userName: " +  userName);
                        Log.d(TAG,"orderNumberText: " +  orderNumberText);
                        Log.d(TAG,"driverNameText: " +  driverNameText);
                        Log.d(TAG,"driverHkidText: " +  driverHkidText);
                        Log.d(TAG,"carPlateNumberText: " +  carPlateNumberText);

                        // Push the ordes, it will appear in the list
                        //mOrderDetailsReference.push().setValue(orderDetail);
                        mOrderDetailsReference.setValue(orderDetail);

                        Log.d(TAG,"uid: " +  uid);
                        Log.d(TAG,"userName: " +  userName);
                        Log.d(TAG,"mDriverNameField: " +  driverNameText);
                        Log.d(TAG,"mDriverHkidField: " +  driverHkidText);
                        Log.d(TAG,"mDriverCarPlateField: " +  carPlateNumberText);

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
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                // Create a File where the photo will be saved to.
                File photoFile = null;
                try {
                    photoFile = PhotoHelper.createPhotoFile(this);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                Log.d(TAG, "photoFile = " + photoFile);

                if (photoFile != null) {
                    // Save the photo file path globally.
                    mTempPhotoFilePath = photoFile.getAbsolutePath();
                    Log.d(TAG, "mTempPhotoFilePath" + mTempPhotoFilePath);

                    // Get the file content URI using FileProvider to avoid FileUriExposedException.
                    Uri photoUri = FileProvider.getUriForFile(this, getString(R.string.authority), photoFile);

                    Log.d(TAG, "photoUri = " + photoUri);
                    // Set the file content URI as an intent extra and dispatch the camera intent.
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(cameraIntent, REQUEST_CODE_TAKE_PHOTO);
                }

            }
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
        // Check the request code to determine which intent was dispatched.
        if (requestCode == REQUEST_CODE_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                // Take photo successful. If the user has previously set a captured photo on the
                // ImageView, that photo file needs to be deleted since it will be replaced now.
                PhotoHelper.deleteCapturedPhotoFile(mDriverPhotoImageView.getTag());
                // Save the file uri as a tag and display the captured photo on the ImageView.
                mDriverPhotoImageView.setTag(mTempPhotoFilePath);
                new LoadDriverPhotoAsync(this, mDriverPhotoImageView).execute(mTempPhotoFilePath);
            } else if (resultCode == RESULT_CANCELED) {
                // The user cancelled taking a photo. The photo file created from the camera intent
                // is just an empty file so delete it since we don't need it anymore.
                File photoFile = new File(mTempPhotoFilePath);
                photoFile.delete();
            }
        } else if (requestCode == REQUEST_CODE_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK && data != null) {
                // Choose photo successful. Delete previously captured photo file if there's any.
                PhotoHelper.deleteCapturedPhotoFile(mDriverPhotoImageView.getTag());
                // Save the file uri as a tag and display the selected photo on the ImageView.
                String photoPath = data.getData().toString();
                mDriverPhotoImageView.setTag(photoPath);
                new LoadDriverPhotoAsync(this, mDriverPhotoImageView).execute(photoPath);
            }
        }
    }
}

