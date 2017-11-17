package com.google.firebase.quickstart.database;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.quickstart.database.helper.PhotoHelper;
import com.google.firebase.quickstart.database.models.Comment;
import com.google.firebase.quickstart.database.models.Post;
import com.google.firebase.quickstart.database.models.User;

import com.google.firebase.quickstart.database.helper.PhotoHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailActivity extends BaseActivity implements View.OnClickListener, DialogInterface.OnClickListener {

    private static final String TAG = "OrderDetailActivity";

    public static final String EXTRA_POST_KEY = "post_key";
    public static final String EXTRA_ORDER_KEY = "order_key";

    private DatabaseReference mPostReference;
    private DatabaseReference mCommentsReference;
    private ValueEventListener mPostListener;
    private String mPostKey;
    private String mOrderKey;
    private CommentAdapter mAdapter;

    private TextView mAuthorView;
    private TextView mTitleView;
    private TextView mBodyView;
    private EditText mCommentField;
    private Button mCommentButton;
    private RecyclerView mCommentsRecycler;

    private TextView mOrderView;
    private TextView mUserNameView;
    private EditText mDriverNameView;
    private EditText mDriverHkidView;
    private EditText mDriverCarPlateView;
    private Button mConfirmButton;

    private static final int REQUEST_CODE_TAKE_PHOTO = 0;
    private static final int REQUEST_CODE_CHOOSE_PHOTO = 1;
    private String mTempPhotoFilePath;
    private ImageView mProductPhotoImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_post_detail);
        setContentView(R.layout.activity_order_detail);

        // Get post key from intent
        //mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        mOrderKey = getIntent().getStringExtra(EXTRA_ORDER_KEY);
        if (mOrderKey == null) {
            //throw new IllegalArgumentException("Must pass EXTRA_POST_KEY");
            throw new IllegalArgumentException("Must pass EXTRA_ORDER_KEY");
        }

        // Initialize Database
        mPostReference = FirebaseDatabase.getInstance().getReference().child("orders").child(mOrderKey);
        //mCommentsReference = FirebaseDatabase.getInstance().getReference().child("post-comments").child(mPostKey);

        // Initialize Views
//        mAuthorView = findViewById(R.id.post_author);
//        mTitleView = findViewById(R.id.post_title);
//        mBodyView = findViewById(R.id.post_body);
//        mCommentField = findViewById(R.id.field_comment_text);
//        mCommentButton = findViewById(R.id.button_post_comment);
//        mCommentsRecycler = findViewById(R.id.recycler_comments);

        mOrderView = findViewById(R.id.order_number_title);
        mUserNameView = findViewById(R.id.user_name_title);
        mDriverNameView = findViewById(R.id.driver_name_edit);
        mDriverHkidView = findViewById(R.id.driver_hkid_edit);
        mDriverCarPlateView = findViewById(R.id.car_plate_edit);


        //mCommentButton = findViewById(R.id.button_post_comment);
        //mCommentsRecycler = findViewById(R.id.recycler_comments);
        mConfirmButton = findViewById(R.id.confirm_button);


        //mCommentButton.setOnClickListener(this);
        //mCommentsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mConfirmButton.setOnClickListener(this);

    }

    @Override
    public void onStart() {
        super.onStart();

        // Add value event listener to the post
        // [START post_value_event_listener]
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Post post = dataSnapshot.getValue(Post.class);
                // [START_EXCLUDE]
//                mAuthorView.setText(post.author);
//                mTitleView.setText(post.title);
//                mBodyView.setText(post.body);

                mOrderView.setText(post.orderNumber);
                mUserNameView.setText(post.userName);
                mDriverNameView.setText(post.driverName);
                mDriverHkidView.setText(post.driverHkid);
                mDriverCarPlateView.setText(post.carPlateNumber);

                // [END_EXCLUDE]
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(OrderDetailActivity.this, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        mPostReference.addValueEventListener(postListener);
        // [END post_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        mPostListener = postListener;

        // Listen for comments
        //mAdapter = new CommentAdapter(this, mCommentsReference);
//        mAdapter = new CommentAdapter(this, mPostReference);
//        mCommentsRecycler.setAdapter(mAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();

        // Remove post value event listener
        if (mPostListener != null) {
            mPostReference.removeEventListener(mPostListener);
        }

        // Clean up comments listener
//        mAdapter.cleanupListener();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
//        if (i == R.id.button_post_comment) {
        if (i == R.id.confirm_button) {

            //postComment();
            //sendNotifcaition();
        }
    }

    private void postComment() {
        final String uid = getUid();
        FirebaseDatabase.getInstance().getReference().child("users").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user information
                        User user = dataSnapshot.getValue(User.class);
                        String authorName = user.username;

                        // Create new comment object
                        String commentText = mCommentField.getText().toString();
                        Comment comment = new Comment(uid, authorName, commentText);

                        // Push the comment, it will appear in the list
                        mCommentsReference.push().setValue(comment);

                        // Clear the field
                        mCommentField.setText(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private static class CommentViewHolder extends RecyclerView.ViewHolder {

        public TextView userNameView;
        public TextView orderNumberView;


        public CommentViewHolder(View itemView) {
            super(itemView);

            userNameView = itemView.findViewById(R.id.user_name_title);
            orderNumberView = itemView.findViewById(R.id.order_number_title);
            Log.d(TAG, "CommentViewHolder:");
        }
    }

    private static class CommentAdapter extends RecyclerView.Adapter<CommentViewHolder> {


        private Context mContext;
        private DatabaseReference mDatabaseReference;
        private ChildEventListener mChildEventListener;

        //private List<String> mCommentIds = new ArrayList<>();
        //private List<Comment> mComments = new ArrayList<>();
        private List<String> mPostIds = new ArrayList<>();
        private List<Post> mPosts = new ArrayList<>();


        public CommentAdapter(final Context context, DatabaseReference ref) {
            mContext = context;
            mDatabaseReference = ref;

            // Create child event listener
            // [START child_event_listener_recycler]
            ChildEventListener childEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                    // A new comment has been added, add it to the displayed list
                    //Comment comment = dataSnapshot.getValue(Comment.class);
                    Post post = dataSnapshot.getValue(Post.class);

                    // [START_EXCLUDE]
                    // Update RecyclerView
                    mPostIds.add(dataSnapshot.getKey());
                    mPosts.add(post);
                    notifyItemInserted(mPosts.size() - 1);
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
//                    Comment newComment = dataSnapshot.getValue(Comment.class);
//                    String commentKey = dataSnapshot.getKey();

                    Post newPost = dataSnapshot.getValue(Post.class);
                    String postKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int postIndex = mPostIds.indexOf(postKey);
                    if (postIndex > -1) {
                        // Replace with the new data
                        mPosts.set(postIndex, newPost);

                        // Update the RecyclerView
                        notifyItemChanged(postIndex);
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child:" + postKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    //String commentKey = dataSnapshot.getKey();
                    String postKey = dataSnapshot.getKey();

                    // [START_EXCLUDE]
                    int postIndex = mPostIds.indexOf(postKey);
                    if (postIndex > -1) {
                        // Remove data from the list
                        mPostIds.remove(postIndex);
                        mPosts.remove(postIndex);

                        // Update the RecyclerView
                        notifyItemRemoved(postIndex);
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + postKey);
                    }
                    // [END_EXCLUDE]
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                    // A comment has changed position, use the key to determine if we are
                    // displaying this comment and if so move it.
//                    Comment movedComment = dataSnapshot.getValue(Comment.class);
//                    String commentKey = dataSnapshot.getKey();

                    Post movedPost = dataSnapshot.getValue(Post.class);
                    String postKey = dataSnapshot.getKey();

                    // ...
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                    Toast.makeText(mContext, "Failed to load comments.",
                            Toast.LENGTH_SHORT).show();
                }
            };
            ref.addChildEventListener(childEventListener);
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            mChildEventListener = childEventListener;
        }

        @Override
        public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_comment, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CommentViewHolder holder, int position) {
//            Comment comment = mComments.get(position);
//            holder.authorView.setText(comment.author);
//            holder.bodyView.setText(comment.text);

            Post post = mPosts.get(position);
            holder.userNameView.setText(post.userName);
            holder.orderNumberView.setText(post.orderNumber);
        }

        @Override
        public int getItemCount() {
            return mPosts.size();
        }

        public void cleanupListener() {
            if (mChildEventListener != null) {
                mDatabaseReference.removeEventListener(mChildEventListener);
            }
        }

    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (i == 0) {
            // The user selects 'Take photo' option. Dispatch the camera intent.
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                // Create a File where the photo will be saved to.
                File photoFile = null;
                try {
                    photoFile = PhotoHelper.createPhotoFile(this);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
                if (photoFile != null) {
                    // Save the photo file path globally.
                    mTempPhotoFilePath = photoFile.getAbsolutePath();
                    // Get the file content URI using FileProvider to avoid FileUriExposedException.
                    Uri photoUri = FileProvider.getUriForFile(this, getString(R.string.authority), photoFile);
                    // Set the file content URI as an intent extra and dispatch the camera intent.
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(cameraIntent, REQUEST_CODE_TAKE_PHOTO);
                }
            }
        } else {
            // The user selects 'Choose photo' option. Dispatch the choose photo intent.
            Intent choosePhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (choosePhotoIntent.resolveActivity(getPackageManager()) != null)
                startActivityForResult(choosePhotoIntent, REQUEST_CODE_CHOOSE_PHOTO);
        }
    }

    public void viewProductPhoto(View view) {
        PhotoHelper.dispatchViewImageIntent(this, mProductPhotoImageView.getTag());
        Log.d(TAG, "viewProductPhoto: " +"Doing Nothing");
    }

    /**
     * Method that gets invoked when the user presses the 'photo camera' floating action button.
     * This method will inflate the product photo dialog using the product photo dialog fragment.
     * @param view - 'photo camera' floating action button.
     */
    public void showProductPhotoDialog(View view) {
        //ProductPhotoDialogFragment dialogFragment = new ProductPhotoDialogFragment();
        //dialogFragment.show(getSupportFragmentManager(), PRODUCT_PHOTO_DIALOG_TAG);
        Log.d(TAG, "showProductPhotoDialog: " +"Doing Nothing");
    }
}

