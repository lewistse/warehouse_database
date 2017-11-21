package com.google.firebase.quickstart.database.models;

import com.google.firebase.database.IgnoreExtraProperties;

// [START comment_class]
@IgnoreExtraProperties
public class OrderDetail {

    public String uid;
    public String userName;
    public String text;
    public String orderNumber;
    public String driverName;
    public String driverHkid;
    public String carPlateNumber;


    public OrderDetail() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    //public Comment(String uid, String author, String text) {
    public OrderDetail(String uid, String userName, String orderNumber, String driverName, String driverHkid, String carPlateNumber) {
        this.uid = uid;
        this.userName = userName;
        this.orderNumber = orderNumber;
        this.driverName = driverName;
        this.driverHkid = driverHkid;
        this.carPlateNumber = carPlateNumber;
    }

}
// [END comment_class]
