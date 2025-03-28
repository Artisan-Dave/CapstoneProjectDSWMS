package com.example.capstone_dswms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class ViewLocation extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private TextView tvlocation;
    private ImageView ivviewlocation;

    LoadingDialog DialogBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_location);

        // Initialize the TextView and ImageView
        tvlocation = findViewById(R.id.tvlocation);
        ivviewlocation = findViewById(R.id.ivviewlocation);

        // Initialize the Firebase Database and Storage references
        databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://caps-dswms-default-rtdb.firebaseio.com/");
//        storageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://caps-dswms.appspot.com");

        DatabaseReference imageref = databaseReference.child("trashbin1").child("location");
        DatabaseReference captionref = databaseReference.child("trashbin1").child("caption");

        DialogBar = new LoadingDialog(this);

        DialogBar.ShowDialog();


        imageref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imageUrl = snapshot.getValue(String.class);

                if(imageUrl != null){
                    Glide.with(ViewLocation.this).load(imageUrl).into(ivviewlocation);
                    DialogBar.HideDialog();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewLocation.this,"Error loading image",Toast.LENGTH_SHORT).show();
                DialogBar.HideDialog();
            }
        });

        captionref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String imagecaption = snapshot.getValue(String.class);

                if(imagecaption != null) {
                    tvlocation.setText(imagecaption);
                    DialogBar.HideDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewLocation.this,"Error",Toast.LENGTH_SHORT).show();
                DialogBar.HideDialog();
            }
        });



    }
}