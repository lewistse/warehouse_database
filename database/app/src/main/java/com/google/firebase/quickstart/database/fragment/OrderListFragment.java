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
import android.widget.SearchView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.quickstart.database.MainActivity;
import com.google.firebase.quickstart.database.OrderDetailActivity;
import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.models.NewOrder;
import com.google.firebase.quickstart.database.viewholder.OrderViewHolder;


public abstract class OrderListFragment extends Fragment {

    private static final String TAG = "OrderListFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<NewOrder, OrderViewHolder> mAdapter, mSearchAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;
    private SearchView mSearchView;

    public OrderListFragment() {}

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_orders, container, false);

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
        mSearchView = getActivity().findViewById(R.id.search_bar);

        // Set up FirebaseRecyclerAdapter with the Query
        Query ordersQuery = getQuery(mDatabase);

        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {

                Log.d(TAG, " setOnCloseListener " );

                        if (mSearchAdapter !=null) {
                            mSearchAdapter.stopListening();
                            Log.d(TAG, " mSearchAdapter.stopListening() ");
                            mRecycler.invalidate();
                        }

                Log.d(TAG, " recreate activity() ");
                getActivity().recreate();

                return false;
            }
        });


                mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            System.out.println("inside onQueryTextSubmit  " + query);
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {

                            Log.d(TAG, "inside onQueryTextChange  " + newText );

//                            getActivity().getSupportFragmentManager().beginTransaction().detach();
//                            getFragmentManager().beginTransaction().detach(new OrderFragment()).commitNowAllowingStateLoss();
//                            getFragmentManager().beginTransaction().attach(new OrderFragment()).commitAllowingStateLoss();

//                            OrderListFragment mFragment = getActivity().getSupportFragmentManager().findFragmentByTag("OrderListFragment"); //if you are using support library

//                            OrderListFragment mFragment = (OrderListFragment) getFragmentManager().findFragmentByTag("OrderListFragment"); //if you are using support library
//                            FragmentManager fm = getFragmentManager();
//                            FragmentTransaction ft = fm.beginTransaction();
//                            ft.detach(mFragment);
//                            ft.attach(mFragment).commit();

                            if (newText == "") {
                                Log.d(TAG, "newText is null ");
                           }

                            else {
                                //Query searchQuery = getQuery(mDatabase).orderByChild("Orders").startAt(newText).limitToFirst(10);
                                Query searchQuery = mDatabase.child("orders").orderByChild("orderNumber").startAt(newText).endAt(newText + "\uf8ff").limitToFirst(10);

                                FirebaseRecyclerOptions searchOptions = new FirebaseRecyclerOptions.Builder<NewOrder>()
                                        .setQuery(searchQuery, NewOrder.class)
                                        .build();

                                Log.d(TAG, "FirebaseRecyclerOptions with search: ");

                                mSearchAdapter = new FirebaseRecyclerAdapter<NewOrder, OrderViewHolder>(searchOptions) {

                                    @Override
                                    public OrderViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
                                        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                                        Log.d(TAG, "LayoutInflater in search: ");
                                        return new OrderViewHolder(inflater.inflate(R.layout.order_list, viewGroup, false));

                                    }

                                    @Override
                                    protected void onBindViewHolder(OrderViewHolder viewHolder, int position, final NewOrder search) {
                                        final DatabaseReference orderSearchRef = getRef(position);
                                        Log.d(TAG, "onBindViewHolder  in search: ");

                                        // Set click listener for the whole post view
                                        final String orderSearchRefKey = orderSearchRef.getKey();
                                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                // Launch PostDetailActivity
                                                Intent intent = new Intent(getActivity(), OrderDetailActivity.class);
                                                intent.putExtra(OrderDetailActivity.EXTRA_ORDER_KEY, orderSearchRefKey);
                                                startActivity(intent);
                                            }
                                        });

                                        viewHolder.bindToOrder(search);
                                        Log.d(TAG, "bindToOrder search  in search");

                                    }

                                };

                                mSearchAdapter.startListening();
                                Log.d(TAG, " mSearchAdapter.startListening()");

                                mRecycler.setAdapter(mSearchAdapter);
                                Log.d(TAG, " mRecycler.setAdapter in search");

                                mRecycler.invalidate();
                                Log.d(TAG, " mRecycler.invalidate in search");


                            }   //else
                            return true;


                        }   // onQueryTextChange

                    });


        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<NewOrder>()
                .setQuery(ordersQuery, NewOrder.class)
                .build();

        Log.d(TAG, "FirebaseRecyclerOptions: ");

        mAdapter = new FirebaseRecyclerAdapter<NewOrder, OrderViewHolder>(options) {

            @Override
            public OrderViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                Log.d(TAG, "onCreateViewHolder: ");

                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                Log.d(TAG, "LayoutInflater: " );
                return new OrderViewHolder(inflater.inflate(R.layout.order_list, viewGroup, false));

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
            Log.d(TAG, " mAdapter.startListening() " );
//            mSearchAdapter.startListening();
        }
//        if (mSearchAdapter !=null){
//            mSearchAdapter.startListening();
//            Log.d(TAG, " mSearchAdapter.startListening() " );
//        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
//            mSearchAdapter.stopListening();
            Log.d(TAG, " mAdapter.stopListening() " );
        }
//        if (mSearchAdapter !=null){
//            mSearchAdapter.stopListening();
//            Log.d(TAG, " mSearchAdapter.stopListening() " );
//        }
    }


    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);

}
