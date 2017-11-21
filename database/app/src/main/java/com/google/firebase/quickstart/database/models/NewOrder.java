package com.google.firebase.quickstart.database.models;

import android.media.Image;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

// [START post_class]
@IgnoreExtraProperties
public class NewOrder {

    public String uid;
    public String userName;
    public String orderNumber;


    public NewOrder() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    //public NewOrder(String uid, String userName, String orderNumber, String driverName, String driverHkid, String carPlateNumber) {
    public NewOrder(String uid, String userName, String orderNumber) {

        this.uid = uid;
        this.userName = userName;
        this.orderNumber = orderNumber;

    }


    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("userName", userName);
        result.put("orderNumber", orderNumber);

        return result;
    }
    // [END post_to_map]

}
// [END post_class]
