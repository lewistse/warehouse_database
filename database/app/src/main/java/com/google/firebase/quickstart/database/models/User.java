package com.google.firebase.quickstart.database.models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

// [START blog_user_class]
@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    public String telephone;
    public String displayName;
    public Map <String, String> notificationTokens = new HashMap<String, String>();;
    public String photoURL;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, Map <String, String> token) {
        this.username = username;
        this.email = email;
        this.displayName = username;
        this.photoURL = "https://lh3.googleusercontent.com/-XdUIqdMkCWA/AAAAAAAAAAI/AAAAAAAAAAA/4252rscbv5M/photo.jpg";
        //this.notificationTokens = new HashMap<String, String>();
        this.notificationTokens = token;
        this.notificationTokens.put(FirebaseInstanceId.getInstance().getToken(),"true");
    }

}
// [END blog_user_class]
