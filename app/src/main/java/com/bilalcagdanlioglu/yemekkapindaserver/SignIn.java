package com.bilalcagdanlioglu.yemekkapindaserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bilalcagdanlioglu.yemekkapindaserver.Common.Common;
import com.bilalcagdanlioglu.yemekkapindaserver.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignIn extends AppCompatActivity {

    Button btnSignIn;
    EditText edtPhone , edtPassword;
    FirebaseDatabase db;
    DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_sign_in );

        edtPhone = findViewById( R.id.edtPhone );
        edtPassword = findViewById( R.id.edtPassword );
        btnSignIn = findViewById( R.id.btnSignIn );

        db= FirebaseDatabase.getInstance();
        users = db.getReference("User");

        btnSignIn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInUser(edtPhone.getText().toString(),edtPassword.getText().toString());
            }
        } );
    }

    private void signInUser(String phone, String password) {
        ProgressDialog mDialog = new ProgressDialog( SignIn.this );
        mDialog.setMessage( "Lütfen bekleyin.." );
        mDialog.show();

        final String localPhone = phone;
        final String localPassword = password;
        users.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child( localPhone ).exists()){
                    mDialog.dismiss();
                    User user = snapshot.child( localPhone ).getValue(User.class);
                    user.setPhone( localPhone );
                    if(Boolean.parseBoolean( user.getIsStaff() )){
                        if(user.getPassword().equals( localPassword )){
                            Intent homeIntent = new Intent(SignIn.this,Home.class);
                            Common.currentUser= user;
                            startActivity( homeIntent );
                            finish();
                            Toast.makeText( SignIn.this, "Giriş Başarılı", Toast.LENGTH_SHORT ).show();

                        }
                        else{
                            Toast.makeText( SignIn.this, "Hatalı Şifre!!", Toast.LENGTH_SHORT ).show();
                        }
                    }
                    else{
                        Toast.makeText( SignIn.this, "Lütfen personel hesabıyla giriş yapınız!!", Toast.LENGTH_SHORT ).show();
                    }
                }
                else{
                    mDialog.dismiss();
                    Toast.makeText( SignIn.this, "Mevcut kullanıcı bulunamadı!", Toast.LENGTH_SHORT ).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        } );
    }
}