package com.bilalcagdanlioglu.yemekkapindaserver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;

import com.bilalcagdanlioglu.yemekkapindaserver.Common.Common;
import com.bilalcagdanlioglu.yemekkapindaserver.Model.Order;
import com.bilalcagdanlioglu.yemekkapindaserver.ViewHolder.OrderDetailAdapter;

public class OrderDetail extends AppCompatActivity {
    TextView order_id,order_phone,order_total, order_comment,order_address;
    String order_id_value="";
    RecyclerView lstFoods;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_order_detail );

        order_id = findViewById( R.id.order_id );
        order_phone = findViewById( R.id.order_phone );
        order_total = findViewById( R.id.order_total );
        order_address = findViewById( R.id.order_address );
        order_comment = findViewById( R.id.order_comment );

        lstFoods = findViewById( R.id.lstFoods );
        lstFoods.setHasFixedSize( true );
        layoutManager = new LinearLayoutManager( this );
        lstFoods.setLayoutManager( layoutManager );

        if(getIntent() !=null){
            order_id_value  =getIntent().getStringExtra( "OrderId" );
        }

        order_id.setText( order_id_value );
        order_phone.setText( Common.currentRequest.getPhone() );
        order_total.setText( Common.currentRequest.getTotal() );
        order_address.setText( Common.currentRequest.getAddress() );
        order_comment.setText( Common.currentRequest.getComment() );

        OrderDetailAdapter adapter = new OrderDetailAdapter(Common.currentRequest.getFoods());
        adapter.notifyDataSetChanged();
        lstFoods.setAdapter( adapter);
    }
}