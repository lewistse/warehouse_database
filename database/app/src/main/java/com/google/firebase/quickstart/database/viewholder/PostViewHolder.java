package com.google.firebase.quickstart.database.viewholder;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.quickstart.database.R;
import com.google.firebase.quickstart.database.models.Post;

public class PostViewHolder extends RecyclerView.ViewHolder {

    /*public TextView titleView;
    public TextView authorView;
    public ImageView starView;
    public TextView numStarsView;
    public TextView bodyView*/;

    public TextView orderNumberView;
    public TextView driverNameView;
    //public ImageView driverPhotoView;
    public TextView driverHkidView;
    public TextView carPlateNumberView;
    public TextView userNameView;

    public PostViewHolder(View itemView) {
        super(itemView);

        orderNumberView = itemView.findViewById(R.id.order_number_title);
        userNameView =  itemView.findViewById(R.id.user_name_title);
        driverNameView = itemView.findViewById(R.id.driver_name_title);
        driverHkidView = itemView.findViewById(R.id.driver_hkid_title);
        carPlateNumberView = itemView.findViewById(R.id.car_plate_title);
        // driverPhotoView = itemView.findViewById(R.id.driver_photo_image_view);



        /*titleView = itemView.findViewById(R.id.post_title);
        authorView = itemView.findViewById(R.id.post_author);
        starView = itemView.findViewById(R.id.star);
        numStarsView = itemView.findViewById(R.id.post_num_stars);
        bodyView = itemView.findViewById(R.id.post_body);*/
    }

//    public void bindToPost(Post post, View.OnClickListener starClickListener) {
//        titleView.setText(post.title);
//        authorView.setText(post.author);
//        numStarsView.setText(String.valueOf(post.starCount));
//        bodyView.setText(post.body);
//        starView.setOnClickListener(starClickListener);
//
//        orderNumberView.setText(post.orderNumber);
//        userNameView.setText(post.userName);
//        driverNameView.setText(post.driverName);
//        //driverPhotoView.setImageDrawable(Drawable.createFromPath());
//        driverHkidView.setText(post.driverHkid);
//        carPlateNumberView.setText(post.carPlateNumber);
//
//        //starView.setOnClickListener(starClickListener);
//    }
}
