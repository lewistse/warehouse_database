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
//    public String newOrderTimeStamp;
    public String orderStatus;

    public NewOrder() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    //public NewOrder(String uid, String userName, String orderNumber, String driverName, String driverHkid, String carPlateNumber) {
    public NewOrder(String uid, String userName, String orderNumber, String orderStatus) {

        this.uid = uid;
        this.userName = userName;
        this.orderNumber = orderNumber;
//        this.newOrderTimeStamp = newOrderTimeStamp;
        this.orderStatus = orderStatus;


    }


    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("userName", userName);
        result.put("orderNumber", orderNumber);
//        result.put("newOrderTimeStamp", newOrderTimeStamp);
        result.put("orderStatus", orderStatus);

        return result;
    }
    // [END post_to_map]

}
// [END post_class]
