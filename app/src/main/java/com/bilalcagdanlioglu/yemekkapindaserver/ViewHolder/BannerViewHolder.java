package com.bilalcagdanlioglu.yemekkapindaserver.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bilalcagdanlioglu.yemekkapindaserver.Common.Common;
import com.bilalcagdanlioglu.yemekkapindaserver.R;

public class BannerViewHolder extends RecyclerView.ViewHolder implements
        View.OnCreateContextMenuListener{

    public TextView banner_name;
    public ImageView banner_image;

    public BannerViewHolder(@NonNull View itemView) {
        super( itemView );
        banner_name = itemView.findViewById( R.id.banner_name);
        banner_image = itemView.findViewById( R.id.banner_image );

        itemView.setOnCreateContextMenuListener( this );

    }
    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle( "İşlem seçiniz" );
        contextMenu.add( 0,0,getAdapterPosition(), Common.UPDATE );
        contextMenu.add( 0,1,getAdapterPosition(), Common.DELETE );
    }
}
