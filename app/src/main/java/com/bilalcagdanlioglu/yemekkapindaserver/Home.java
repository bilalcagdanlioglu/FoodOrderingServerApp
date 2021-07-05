package com.bilalcagdanlioglu.yemekkapindaserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bilalcagdanlioglu.yemekkapindaserver.Common.Common;
import com.bilalcagdanlioglu.yemekkapindaserver.Interface.ItemClickListener;
import com.bilalcagdanlioglu.yemekkapindaserver.Model.Category;
import com.bilalcagdanlioglu.yemekkapindaserver.Model.Food;
import com.bilalcagdanlioglu.yemekkapindaserver.Model.Token;
import com.bilalcagdanlioglu.yemekkapindaserver.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;
    FirebaseDatabase database;
    DatabaseReference categories;
    FirebaseStorage storage;
    StorageReference storageReference;
    DrawerLayout drawer;
    TextView txtFullName;
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;
    EditText edtName;
    Button btnUpload, btnSelect;
    Category newCategory;
    Uri saveUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_home );

        Toolbar toolbar = findViewById( R.id.toolbar );
        toolbar.setTitle( "Menü Yönetim" );
        setSupportActionBar( toolbar );

        database = FirebaseDatabase.getInstance();
        categories = database.getReference("Category");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        FloatingActionButton fab = findViewById( R.id.fab );
        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        } );

        drawer = findViewById( R.id.drawer_layout );
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle( this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.setDrawerListener( toggle );
        toggle.syncState();

        NavigationView navigationView = findViewById( R.id.nav_view );
        navigationView.setNavigationItemSelectedListener( this );

        View headerView = navigationView.getHeaderView( 0 );
        txtFullName = headerView.findViewById( R.id.txtFullName );
        txtFullName.setText( Common.currentUser.getName() );

        recycler_menu = findViewById( R.id.recycler_menu );
        recycler_menu.setHasFixedSize( true);
        layoutManager = new LinearLayoutManager( this );
        recycler_menu.setLayoutManager( layoutManager );

        loadMenu();

        updateToken( FirebaseInstanceId.getInstance().getToken() );

    }

    private void updateToken(String token) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token( token,true );
        tokens.child( Common.currentUser.getPhone() ).setValue( data );
    }

    private void showDialog() {
       android.app.AlertDialog.Builder alertDialog = new AlertDialog.Builder( Home.this );
        alertDialog.setTitle( "Yeni kategori ekle" );
        alertDialog.setMessage( "Lütfen bilgileri giriniz" );

        LayoutInflater inflater = Home.this.getLayoutInflater();
        View add_menu_layout = inflater.inflate( R.layout.add_new_menu_layout ,null);

        edtName = add_menu_layout.findViewById( R.id.edtName );
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
                if(newCategory != null){
                    categories.push().setValue( newCategory );
                    Snackbar.make( drawer,"Yeni kategori "+newCategory.getName()+" eklendi",Snackbar.LENGTH_SHORT).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            saveUri = data.getData();
            btnSelect.setText( "Resim seçildi!" );
        }
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
                    Toast.makeText( Home.this, "Yüklendi!!", Toast.LENGTH_SHORT ).show();
                    imageFolder.getDownloadUrl().addOnSuccessListener( new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            newCategory = new Category(edtName.getText().toString(),uri.toString());

                        }
                    } );
                }
            } )
                    .addOnFailureListener( new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText( Home.this, ""+e.getMessage(), Toast.LENGTH_SHORT ).show();
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
        startActivityForResult( Intent.createChooser( intent,"Resim seç" ),Common.PICK_IMAGE_REQUEST);
    }

    private void loadMenu() {
        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery( categories , Category.class )
                .build();

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder menuViewHolder, int i,@NonNull Category category) {
                menuViewHolder.txtMenuName.setText( category.getName() );
                Picasso.with( Home.this).load( category.getImage() )
                        .into( menuViewHolder.imageView );

                menuViewHolder.setItemClickListener( new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodList = new Intent(Home.this,FoodList.class);
                        foodList.putExtra( "CategoryId", adapter.getRef( position ).getKey());
                        startActivity( foodList );
                    }
                } );
            }

            @Override
            public MenuViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from( parent.getContext() )
                        .inflate( R.layout.menu_item, parent,false );

                return new MenuViewHolder( itemView );
            }
        };
        adapter.startListening();

        adapter.notifyDataSetChanged();
        recycler_menu.setAdapter( adapter );

        /*
        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(
                Category.class,
                R.layout.menu_item,
                MenuViewHolder.class,
                categories
        ) {
            @Override
            protected void populateViewHolder(MenuViewHolder menuViewHolder, Category category, int i) {
                menuViewHolder.txtMenuName.setText( category.getName() );
                Picasso.with( Home.this).load( category.getImage() )
                        .into( menuViewHolder.imageView );

                menuViewHolder.setItemClickListener( new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodList = new Intent(Home.this,FoodList.class);
                        foodList.putExtra( "CategoryId", adapter.getRef( position ).getKey());
                        startActivity( foodList );
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
    protected void onResume() {
        super.onResume();
        if(adapter != null)
            adapter.startListening();
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById( R.id.drawer_layout );
        if(drawer.isDrawerOpen( GravityCompat.START )){
            drawer.closeDrawer( GravityCompat.START );
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate( R.menu.main,menu );
        return super.onCreateOptionsMenu( menu );
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected( item );
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();

        if(id == R.id.nav_menu){

        }
        else if(id == R.id.nav_cart){

        }
        else if(id == R.id.nav_orders){
            Intent orders = new Intent(Home.this,OrderStatus.class);
            startActivity( orders );
        }
        else if(id == R.id.nav_banner){
            Intent banner = new Intent(Home.this,BannerActivity.class);
            startActivity( banner );
        }
        else if(id == R.id.nav_log_out){
            Intent login = new Intent(Home.this,SignIn.class);
            login.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
            startActivity( login );
            Toast.makeText( this, "Çıkış Yapıldı.", Toast.LENGTH_SHORT ).show();
        }
        DrawerLayout drawer = findViewById( R.id.drawer_layout );
        drawer.closeDrawer( GravityCompat.START );
        return true;
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().equals(Common.UPDATE)){
            showUpdateDialog(adapter.getRef( item.getOrder() ).getKey(),adapter.getItem( item.getOrder() ));
        }
        else if(item.getTitle().equals( Common.DELETE )){
            deleteCategory(adapter.getRef( item.getOrder() ).getKey(),adapter.getItem( item.getOrder() ));
        }
        return super.onContextItemSelected( item );
    }

    private void showUpdateDialog(String key, Category item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder( Home.this );
        alertDialog.setTitle( "Kategori Güncelleme" );
        alertDialog.setMessage( "Lütfen bilgileri giriniz" );

        LayoutInflater inflater = Home.this.getLayoutInflater();
        View add_menu_layout = inflater.inflate( R.layout.add_new_menu_layout ,null);

        edtName = add_menu_layout.findViewById( R.id.edtName );
        btnSelect = add_menu_layout.findViewById( R.id.btnSelect );
        btnUpload = add_menu_layout.findViewById( R.id.btnUpload );

        edtName.setText( item.getName() );

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
                item.setName( edtName.getText().toString() );
                categories.child( key ).setValue( item );
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

    private void changeImage(Category item) {
        if(saveUri!=null){
            ProgressDialog dialog = new ProgressDialog( Home.this );
            dialog.setMessage( "Yükleniyor.." );
            dialog.show();

            String imageName = UUID.randomUUID().toString();
            StorageReference imageFolder = storageReference.child( "images/"+imageName );
            imageFolder.putFile( saveUri ).addOnSuccessListener( new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();
                    Toast.makeText( Home.this, "Yüklendi!!", Toast.LENGTH_SHORT ).show();
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
                            Toast.makeText( Home.this, ""+e.getMessage(), Toast.LENGTH_SHORT ).show();
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

    private void deleteCategory(String key, Category item) {
        categories.child( key ).removeValue();
        Toast.makeText( Home.this, "Kategori silindi!", Toast.LENGTH_SHORT ).show();
    }
}