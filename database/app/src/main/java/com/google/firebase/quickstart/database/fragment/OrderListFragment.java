package com.google.firebase.quickstart.database.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.MainActivity;
import com.google.firebase.quickstart.database.OrderDetailActivity;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.models.NewOrder;
import com.google.firebase.quickstart.database.viewholder.OrderViewHolder;


public abstract class OrderListFragment extends Fragment {

    private static final String TAG = "OrderListFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase; //ben
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<NewOrder, OrderViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    public OrderListFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        mRecycler = rootView.findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);

        Log.d(TAG, "onCreateView: ");
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);



        // Set up FirebaseRecyclerAdapter with the Query
        Query ordersQuery = getQuery(mDatabase);

//        Query searchRef = getQuery(mDatabase).orderByChild("Orders").startAt(newText);
//
//        searchRef.addValueEventListener(new ValueEventListener()
//        {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot)
//            {
//                for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
//                {
//                    //TODO get the data here
//
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError){
//                Log.w(TAG, "searchOrders:onCancelled", databaseError.toException());
//            }
//        });


        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<NewOrder>()
                .setQuery(ordersQuery, NewOrder.class)
                .build();

        Log.d(TAG, "FirebaseRecyclerOptions: ");

        mAdapter = new FirebaseRecyclerAdapter<NewOrder, OrderViewHolder>(options) {

            // how to retrieve data ?
            @Override
            public OrderViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                Log.d(TAG, "onCreateViewHolder: ");

                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                Log.d(TAG, "LayoutInflater: " );
                return new OrderViewHolder(inflater.inflate(R.layout.item_post, viewGroup, false));

            }


            @Override
             protected void onBindViewHolder(OrderViewHolder viewHolder, int position, final NewOrder model) {
                final DatabaseReference orderRef = getRef(position);
                Log.d(TAG, "onBindViewHolder: " );

                // Set click listener for the whole post view
                final String orderKey = orderRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailActivity
                        Intent intent = new Intent(getActivity(), OrderDetailActivity.class);
                        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_KEY, orderKey);
                        startActivity(intent);

                        Log.d(TAG, "OrderDetailView: " );
                    }
                });

                viewHolder.bindToOrder(model);

            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }


    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);

}
