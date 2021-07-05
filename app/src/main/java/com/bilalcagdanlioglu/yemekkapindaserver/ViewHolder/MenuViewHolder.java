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

public class MenuViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener,
        View.OnCreateContextMenuListener
{
    public TextView txtMenuName;
    public ImageView imageView;

    private ItemClickListener itemClickListener;

    public MenuViewHolder(@NonNull View itemView) {
        super( itemView );

        txtMenuName = itemView.findViewById( R.id.menu_name );
        imageView = itemView.findViewById( R.id.menu_image );

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