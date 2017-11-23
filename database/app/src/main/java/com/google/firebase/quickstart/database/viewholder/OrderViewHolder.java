package com.google.firebase.quickstart.database.viewholder;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.quickstart.database.R;
//import com.google.firebase.quickstart.database.models.Post;
import com.google.firebase.quickstart.database.models.NewOrder;

public class OrderViewHolder extends RecyclerView.ViewHolder {

    public TextView orderNumberView;
    public TextView driverNameView;
    //public ImageView driverPhotoView;
    public TextView driverHkidView;
    public TextView carPlateNumberView;
    public TextView userNameView;

    public OrderViewHolder(View itemView) {
        super(itemView);

        orderNumberView = itemView.findViewById(R.id.order_number_title);
        userNameView =  itemView.findViewById(R.id.user_name_title);
        driverNameView = itemView.findViewById(R.id.driver_name_title);
        driverHkidView = itemView.findViewById(R.id.driver_hkid_title);
        carPlateNumberView = itemView.findViewById(R.id.car_plate_title);

        // driverPhotoView = itemView.findViewById(R.id.driver_photo_image_view);

    }

    public void bindToOrder(NewOrder newOrder) {

        orderNumberView.setText(newOrder.orderNumber);
        userNameView.setText(newOrder.userName);

    }
}
