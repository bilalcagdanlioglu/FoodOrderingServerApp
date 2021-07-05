package com.bilalcagdanlioglu.yemekkapindaserver.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bilalcagdanlioglu.yemekkapindaserver.Common.Common;
import com.bilalcagdanlioglu.yemekkapindaserver.Interface.ItemClickListener;
import com.bilalcagdanlioglu.yemekkapindaserver.R;

public class OrderViewHolder extends RecyclerView.ViewHolder  {
    public TextView txtOrderId, txtOrderStatus, txtOrderPhone,txtOrderAddress;
    public Button btnEdit,btnRemove,btnDetail,btnDirection;

    public OrderViewHolder(@NonNull View itemView) {
        super( itemView );
        txtOrderPhone = itemView.findViewById( R.id.order_phone );
        txtOrderId = itemView.findViewById( R.id.order_id );
        txtOrderStatus = itemView.findViewById( R.id.order_status );
        txtOrderAddress = itemView.findViewById( R.id.order_address );

        btnEdit = itemView.findViewById( R.id.btnEdit );
        btnRemove = itemView.findViewById( R.id.btnRemove );
        btnDetail = itemView.findViewById( R.id.btnDetail );
        btnDirection = itemView.findViewById( R.id.btnDirection );

    }

}
