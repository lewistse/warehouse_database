package com.google.firebase.quickstart.database;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.models.NewOrder;
//import com.google.firebase.quickstart.database.models.Post;
import com.google.firebase.quickstart.database.models.User;

import com.google.firebase.quickstart.database.helper.PhotoHelper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NewOrderActivity extends BaseActivity {

    private static final String TAG = "NewOrderActivity";
    private static final String REQUIRED = "Required";

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]

    private EditText mTitleField;
    private EditText mNewOrderField;
    private EditText mBodyField;
    private FloatingActionButton mSubmitButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order);

        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

        mNewOrderField = findViewById(R.id.new_order_title);
        mSubmitButton = findViewById(R.id.fab_new_order);

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitNewOrder();
            }
        });
    }

    private void submitNewOrder() {
        final String orderNumber = mNewOrderField.getText().toString();

        final String driverName = "Driver Name";
        final String driverHkid = "HKID number";
        final String carPlateNumber = "Car plate number";

        final Boolean fulfilled = false;


        // Title is required
        if (TextUtils.isEmpty(orderNumber)) {
            mNewOrderField.setError(REQUIRED);
            return;
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false);
        Toast.makeText(this, "Synchronizing...", Toast.LENGTH_SHORT).show();

        // [START single_value_read]
        final String userId = getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(NewOrderActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            writeNewOrder(userId, user.username, orderNumber);
                        }

                        // Finish this Activity, back to the stream
                        setEditingEnabled(true);
                        finish();
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                        // [START_EXCLUDE]
                        setEditingEnabled(true);
                        // [END_EXCLUDE]
                    }
                });
        // [END single_value_read]
    }

    private void setEditingEnabled(boolean enabled) {
        //mTitleField.setEnabled(enabled);
        mNewOrderField.setEnabled(enabled);
        //mBodyField.setEnabled(enabled);
        if (enabled) {
            mSubmitButton.setVisibility(View.VISIBLE);
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }
    }

    // [START write_fan_out]
    private void writeNewOrder(String userId, String username, String orderNumber) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.child("orders").push().getKey();

        NewOrder order = new NewOrder(userId, username, orderNumber);   //updated ../models/Post.java
        Map<String, Object> postValues = order.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/orders/" + key, postValues);
        mDatabase.updateChildren(childUpdates);
    }
    // [END write_fan_out]


}
