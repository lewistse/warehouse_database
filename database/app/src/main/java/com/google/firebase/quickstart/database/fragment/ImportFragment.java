package com.google.firebase.quickstart.database.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class ImportFragment extends OrderListFragment {
//  MyPostsFragment   {

    public ImportFragment() {}

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // All my posts
        //return databaseReference.child("user-posts")
        return databaseReference.child("orders").child(getUid());
    }
}
