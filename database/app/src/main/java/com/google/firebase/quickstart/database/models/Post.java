package com.google.firebase.quickstart.database.models;

import android.media.Image;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

// [START post_class]
@IgnoreExtraProperties
public class Post {

    public String uid;
    public String userName;
    public String orderNumber;
    public String driverName;
    public String driverHkid;
    public String carPlateNumber;
    //public Boolean fulfilled;
    //public Image driverPhoto;

//    public String driverName;
//    public int starCount = 0;
//    public Map<String, Boolean> stars = new HashMap<>();

//    public String uid;
//    public String author;
//    public String title;
//    public String body;
//    public int starCount = 0;
//    public Map<String, Boolean> stars = new HashMap<>();

    public Post() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Post(String uid, String userName, String orderNumber, String driverName, String driverHkid, String carPlateNumber) {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
        this.uid = uid;
        this.userName = userName;
        this.orderNumber = orderNumber;
        this.driverName = driverName;
        this.driverHkid = driverHkid;
        this.carPlateNumber = carPlateNumber;
        //this.fulfilled = fulfilled;
        //this.driverPhoto = driverPhoto;
    }

    /*public Post(String uid, String author, String title, String body) {
        this.uid = uid;
        this.author = author;
        this.title = title;
        this.body = body;
    }*/

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("userName", userName);
        result.put("orderNumber", orderNumber);
        result.put("driverName", driverName);
        result.put("driverHkid", driverHkid);
        result.put("carPlateNumber", carPlateNumber);

        //result.put("fulfilled", fulfilled);
        //result.put("driverPhoto", driverPhoto);

        //result.put("orderNumber", orderNumber);
        //result.put("body", body);
        //result.put("starCount", starCount);
        //result.put("stars", stars);

        return result;
    }
    // [END post_to_map]

}
// [END post_class]
