package com.bilalcagdanlioglu.yemekkapindaserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bilalcagdanlioglu.yemekkapindaserver.Common.Common;
import com.bilalcagdanlioglu.yemekkapindaserver.Interface.ItemClickListener;
import com.bilalcagdanlioglu.yemekkapindaserver.Model.Food;
import com.bilalcagdanlioglu.yemekkapindaserver.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class FoodList extends AppCompatActivity {
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    RelativeLayout rootLayout;
    FloatingActionButton fab;

    FirebaseDatabase db;
    DatabaseReference foodList;
    FirebaseStorage storage;
    StorageReference storageReference;

    String categoryId="";

    EditText edtFoodName,edtDescription,edtPrice,edtDiscount;
    Button btnUpload, btnSelect;
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    Food newFood;
    Uri saveUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_food_list );



        db = FirebaseDatabase.getInstance();
        foodList = db.getReference("Food");
        storage= FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        recyclerView = findViewById( R.id.recycler_food );
        recyclerView.setHasFixedSize( true );
        layoutManager = new LinearLayoutManager( this);
        recyclerView.setLayoutManager( layoutManager );
        rootLayout = findViewById( R.id.rootLayout );

        fab = findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddFoodDialog();
            }
        } );

        if(getIntent()!=null)
            categoryId = getIntent().getStringExtra( "CategoryId" );
        if(!categoryId.isEmpty() && categoryId!= null)
            loadListFood(categoryId);
    }
  //
    private void showAddFoodDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder( FoodList.this );
        alertDialog.setTitle( "Menüye yeni yemek ekle" );
        alertDialog.setMessage( "Lütfen bilgileri giriniz" );

        LayoutInflater inflater = FoodList.this.getLayoutInflater();
        View add_menu_layout = inflater.inflate( R.layout.add_new_food_layout,null);

        edtFoodName = add_menu_layout.findViewById( R.id.edtFoodName );
        edtDescription = add_menu_layout.findViewById( R.id.edtDescription );
        edtPrice = add_menu_layout.findViewById( R.id.edtPrice );
        edtDiscount = add_menu_layout.findViewById( R.id.edtDiscount );
        btnSelect = add_menu_layout.findViewById( R.id.btnSelect );
        btnUpload = add_menu_layout.findViewById( R.id.btnUpload );

        btnSelect.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        } );

        btnUpload.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        } );

        alertDialog.setView( add_menu_layout );
        alertDialog.setIcon( R.drawable.ic_baseline_shopping_cart_24 );

        alertDialog.setPositiveButton( "EVET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if(newFood != null){
                    foodList.push().setValue( newFood );
                    Snackbar.make( rootLayout,"Yeni kategori "+newFood.getName()+" eklendi",Snackbar.LENGTH_SHORT).show();
                }
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

    private void uploadImage() {
        if(saveUri!=null){
            ProgressDialog dialog = new ProgressDialog( this );
            dialog.setMessage( "Yükleniyor.." );
            dialog.show();

            String imageName = UUID.randomUUID().toString();
            StorageReference imageFolder = storageReference.child( "images/"+imageName );
            imageFolder.putFile( saveUri ).addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();
                    Toast.makeText( FoodList.this, "Yüklendi!!", Toast.LENGTH_SHORT ).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener( new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            newFood = new Food();
                            newFood.setName( edtFoodName.getText().toString() );
                            newFood.setDescription( edtDescription.getText().toString() );
                            newFood.setPrice( edtPrice.getText().toString() );
                            newFood.setDiscount( edtDiscount.getText().toString() );
                            newFood.setMenuId( categoryId );
                            newFood.setImage( uri.toString() );
                        }
                    } );
                }
            } )
                    .addOnFailureListener( new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText( FoodList.this, ""+e.getMessage(), Toast.LENGTH_SHORT ).show();
                        }
                    } )
                    .addOnProgressListener( new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            dialog.setMessage( "Yüklendi "+progress+"%" );
                        }
                    } );
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType( "image/*" );
        intent.setAction( Intent.ACTION_GET_CONTENT );
        startActivityForResult( Intent.createChooser( intent,"Resim seç" ), Common.PICK_IMAGE_REQUEST );
    }

    private void loadListFood(String categoryId) {

        Query listFoodByCategoryId =  foodList.orderByChild( "menuId" ).equalTo( categoryId );
        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery( listFoodByCategoryId , Food.class )
                .build();

        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder foodViewHolder, int i,@NonNull Food food) {
                foodViewHolder.food_name.setText( food.getName() );
                Picasso.with( getBaseContext() )
                        .load( food.getImage() )
                        .into( foodViewHolder.food_image );
                foodViewHolder.setItemClickListener( new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                    }
                } );
            }

            @Override
            public FoodViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from( parent.getContext() )
                        .inflate( R.layout.food_item, parent , false );
                return new FoodViewHolder( itemView );
            }
        };
        adapter.startListening();

        adapter.notifyDataSetChanged();
        recyclerView.setAdapter( adapter );

        /*
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(
                Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild( "menuId" ).equalTo( categoryId )
        ) {
            @Override
            protected void populateViewHolder(FoodViewHolder foodViewHolder, Food food, int i) {
                foodViewHolder.food_name.setText( food.getName() );
                Picasso.with( getBaseContext() ).load( food.getImage() )
                        .into( foodViewHolder.food_image );
                foodViewHolder.setItemClickListener( new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //sonra ekle menü detayı açılacak
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            saveUri = data.getData();
            btnSelect.setText( "Resim seçildi!" );
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().equals( Common.UPDATE )){
            showUpdateFoodDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        else if(item.getTitle().equals( Common.DELETE )){
            deleteFood(adapter.getRef( item.getOrder() ).getKey());
        }
        return super.onContextItemSelected( item );
    }

    private void deleteFood(String key) {
        foodList.child( key ).removeValue();
    }

    private void showUpdateFoodDialog(String key, Food item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder( FoodList.this );
        alertDialog.setTitle( "Menü için yemek bilgisi güncelle" );
        alertDialog.setMessage( "Lütfen bilgileri giriniz" );

        LayoutInflater inflater = FoodList.this.getLayoutInflater();
        View add_menu_layout = inflater.inflate( R.layout.add_new_food_layout,null);

        edtFoodName = add_menu_layout.findViewById( R.id.edtFoodName );
        edtDescription = add_menu_layout.findViewById( R.id.edtDescription );
        edtPrice = add_menu_layout.findViewById( R.id.edtPrice );
        edtDiscount = add_menu_layout.findViewById( R.id.edtDiscount );

        edtFoodName.setText( item.getName() );
        edtDescription.setText( item.getDescription() );
        edtDiscount.setText( item.getDiscount() );
        edtPrice.setText( item.getPrice() );

        btnSelect = add_menu_layout.findViewById( R.id.btnSelect );
        btnUpload = add_menu_layout.findViewById( R.id.btnUpload );

        btnSelect.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        } );

        btnUpload.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeImage(item);
            }
        } );

        alertDialog.setView( add_menu_layout );
        alertDialog.setIcon( R.drawable.ic_baseline_shopping_cart_24 );

        alertDialog.setPositiveButton( "EVET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                //update food
                item.setName( edtFoodName.getText().toString() );
                item.setDescription( edtDescription.getText().toString() );
                item.setPrice( edtPrice.getText().toString() );
                item.setDiscount( edtDiscount.getText().toString() );

                foodList.child( key ).setValue( item );
                Snackbar.make( rootLayout," Yeni yemek "+item.getName()+" eklendi",Snackbar.LENGTH_SHORT).show();

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

    private void changeImage(Food item) {
        if(saveUri!=null){
            ProgressDialog dialog = new ProgressDialog( FoodList.this );
            dialog.setMessage( "Yükleniyor.." );
            dialog.show();

            String imageName = UUID.randomUUID().toString();
            StorageReference imageFolder = storageReference.child( "images/"+imageName );
            imageFolder.putFile( saveUri ).addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();
                    Toast.makeText( FoodList.this, "Yüklendi!!", Toast.LENGTH_SHORT ).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener( new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            item.setImage( uri.toString() );

                        }
                    } );
                }
            } )
                    .addOnFailureListener( new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText( FoodList.this, ""+e.getMessage(), Toast.LENGTH_SHORT ).show();
                        }
                    } )
                    .addOnProgressListener( new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            dialog.setMessage( "Yüklendi "+progress+"%" );
                        }
                    } );
        }
    }
}