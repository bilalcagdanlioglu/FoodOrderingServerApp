package com.bilalcagdanlioglu.yemekkapindaserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bilalcagdanlioglu.yemekkapindaserver.Common.Common;
import com.bilalcagdanlioglu.yemekkapindaserver.Interface.ItemClickListener;
import com.bilalcagdanlioglu.yemekkapindaserver.Model.Category;
import com.bilalcagdanlioglu.yemekkapindaserver.Model.MyResponse;
import com.bilalcagdanlioglu.yemekkapindaserver.Model.Notification;
import com.bilalcagdanlioglu.yemekkapindaserver.Model.Order;
import com.bilalcagdanlioglu.yemekkapindaserver.Model.Request;
import com.bilalcagdanlioglu.yemekkapindaserver.Model.Sender;
import com.bilalcagdanlioglu.yemekkapindaserver.Model.Token;
import com.bilalcagdanlioglu.yemekkapindaserver.Remote.APIService;
import com.bilalcagdanlioglu.yemekkapindaserver.ViewHolder.MenuViewHolder;
import com.bilalcagdanlioglu.yemekkapindaserver.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderStatus extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;
    FirebaseDatabase db;
    DatabaseReference requests;
    MaterialSpinner materialSpinner;

    APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_order_status );

        mService = Common.getFCMClient();

        db= FirebaseDatabase.getInstance();
        requests = db.getReference("Requests");

        recyclerView = findViewById( R.id.listOrders);
        recyclerView.setHasFixedSize( true );
        layoutManager = new LinearLayoutManager( this);
        recyclerView.setLayoutManager( layoutManager);

        loadOrders();
    }

    private void loadOrders() {

        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery( requests , Request.class )
                .build();

        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder orderViewHolder, int i, @NonNull Request request) {
                orderViewHolder.txtOrderId.setText( adapter.getRef( i ).getKey() );
                orderViewHolder.txtOrderStatus.setText( Common.convertCodeToStatus(request.getStatus()) );
                orderViewHolder.txtOrderAddress.setText( request.getAddress() );
                orderViewHolder.txtOrderPhone.setText( request.getPhone() );

                orderViewHolder.btnEdit.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showUpdateDialog(adapter.getRef( i ).getKey(),adapter.getItem( i ));
                    }
                } );

                orderViewHolder.btnRemove.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteOrder(adapter.getRef( i ).getKey());
                    }
                } );

                orderViewHolder.btnDetail.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent orderDetail = new Intent( OrderStatus.this,OrderDetail.class );
                        Common.currentRequest = request;
                        orderDetail.putExtra( "OrderId",adapter.getRef( i ).getKey());
                        startActivity( orderDetail );
                    }
                } );

                orderViewHolder.btnDirection.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent trackingOrder = new Intent( OrderStatus.this,TrackingOrder.class );
                        Common.currentRequest = request;
                        startActivity( trackingOrder );
                    }
                } );
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from( parent.getContext() )
                        .inflate( R.layout.order_layout, parent,false );

                return new OrderViewHolder( itemView );
            }
        };
        adapter.startListening();

        adapter.notifyDataSetChanged();
        recyclerView.setAdapter( adapter );

        /*
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests
        ) {
            @Override
            protected void populateViewHolder(OrderViewHolder orderViewHolder, Request request, int i) {
                orderViewHolder.txtOrderId.setText( adapter.getRef( i ).getKey() );
                orderViewHolder.txtOrderStatus.setText( Common.convertCodeToStatus(request.getStatus()) );
                orderViewHolder.txtOrderAddress.setText( request.getAddress() );
                orderViewHolder.txtOrderPhone.setText( request.getPhone() );

                orderViewHolder.btnEdit.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showUpdateDialog(adapter.getRef( i ).getKey(),adapter.getItem( i ));
                    }
                } );

                orderViewHolder.btnRemove.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteOrder(adapter.getRef( i ).getKey());
                    }
                } );

                orderViewHolder.btnDetail.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent orderDetail = new Intent( OrderStatus.this,OrderDetail.class );
                        Common.currentRequest = request;
                        orderDetail.putExtra( "OrderId",adapter.getRef( i ).getKey());
                        startActivity( orderDetail );
                    }
                } );

                orderViewHolder.btnDirection.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent trackingOrder = new Intent( OrderStatus.this,TrackingOrder.class );
                        Common.currentRequest = request;
                        startActivity( trackingOrder );
                    }
                } );


            }
        };

         */

    }
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
    private void deleteOrder(String key) {
        requests.child( key).removeValue();
        adapter.notifyDataSetChanged();
    }

    private void showUpdateDialog(String key, Request item) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder( this );
        alertDialog.setTitle( "Sipariş Güncelleme" );
        alertDialog.setMessage( "Lütfen sipariş durumunu seçiniz." );

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate( R.layout.update_order_layout,null );

        materialSpinner = view.findViewById( R.id.statusSpinner );
        materialSpinner.setItems( "Sipariş alındı. Hazırlanıyor","Yolda","Gönderildi" );
        alertDialog.setView( view );

        final String localKey = key;
        alertDialog.setPositiveButton( "EVET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                item.setStatus( String.valueOf( materialSpinner.getSelectedIndex() ) );

                requests.child( localKey ).setValue( item );
                adapter.notifyDataSetChanged();
                Toast.makeText( OrderStatus.this, "Durum güncellendi!", Toast.LENGTH_SHORT ).show();

                sendOrderStatusToUser(localKey,item);
            }
        } );
        alertDialog.setNegativeButton( "HAYIR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        } );
        alertDialog.show();
    }

    private void sendOrderStatusToUser(final String key,final Request item) {
        DatabaseReference tokens = db.getReference("Tokens");
        tokens.orderByKey().equalTo( item.getPhone() )
                .addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot postSnapshot : snapshot.getChildren())
                        {
                            Token token = postSnapshot.getValue(Token.class);
                            Notification notification = new Notification( "Yemek Kapında","Siparişiniz "+key+" güncellendi" );
                            Sender content = new Sender( token.getToken(),notification );

                            mService.sendNotification( content )
                                    .enqueue( new Callback<MyResponse>() {
                                        @Override
                                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                            if(response.body().success == 1){
                                                Toast.makeText( OrderStatus.this, "Sipariş Durumu güncellendi", Toast.LENGTH_SHORT ).show();
                                            }
                                            else{
                                                Toast.makeText( OrderStatus.this, "Sipariş güncellendi fakat bildirim hatası", Toast.LENGTH_SHORT ).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<MyResponse> call, Throwable t) {
                                            Log.e("ERROR",t.getMessage());
                                        }
                                    } );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                } );
    }
}