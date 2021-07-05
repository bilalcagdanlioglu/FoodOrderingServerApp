package com.bilalcagdanlioglu.yemekkapindaserver.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bilalcagdanlioglu.yemekkapindaserver.Common.Common;
import com.bilalcagdanlioglu.yemekkapindaserver.Interface.ItemClickListener;
import com.bilalcagdanlioglu.yemekkapindaserver.R;

public class FoodViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener,
        View.OnCreateContextMenuListener
{
    public TextView food_name;
    public ImageView food_image;

    private ItemClickListener itemClickListener;

    public FoodViewHolder(@NonNull View itemView) {
        super( itemView );

        food_name = itemView.findViewById( R.id.food_name );
        food_image = itemView.findViewById( R.id.food_image );

        itemView.setOnCreateContextMenuListener( this );
        itemView.setOnClickListener( this );
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick( view,getAdapterPosition(),false );
    }

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle( "İşlem seçiniz" );
        contextMenu.add( 0,0,getAdapterPosition(), Common.UPDATE );
        contextMenu.add( 0,1,getAdapterPosition(), Common.DELETE );
    }
}