package com.google.firebase.quickstart.database.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

//  MyTopPostsFragment: To be updated
public class ExportFragment extends OrderListFragment {

    public ExportFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // [START my_top_posts_query]
        // My top posts by number of stars
        String myUserId = getUid();
        //Query myTopPostsQuery = databaseReference.child("user-posts").child(myUserId).orderByChild("starCount");
        Query myOrdersQuery = databaseReference.child("orders").child(getUid());
        // [END my_top_posts_query]

        return myOrdersQuery;
    }
}
