package com.example.capstone_dswms;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class Register extends AppCompatActivity{

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://caps-dswms-default-rtdb.firebaseio.com/");

    private NetworkChangeReceiver networkChangeReceiver;

    LoadingDialog DialogBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText etfirstname = findViewById(R.id.etfirstname);
        final EditText etlastname = findViewById(R.id.etlastname);
        final EditText etphone = findViewById(R.id.etphone);
        final EditText etpass = findViewById(R.id.etpass);
        final EditText etconpass = findViewById(R.id.etconpass);
        final TextView tvlogin = findViewById(R.id.tvlogin);
        final Button btnregister = findViewById(R.id.btnregister);


        networkChangeReceiver = new NetworkChangeReceiver();
        DialogBar = new LoadingDialog(this);


        btnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String fnametxt = etfirstname.getText().toString().trim();
                final String lnametxt = etlastname.getText().toString().trim();
                final String phonetxt = etphone.getText().toString().trim();
                final String passtxt = etpass.getText().toString().trim();
                final String conpasstxt = etconpass.getText().toString().trim();
                final String regexPattern = "^09\\d{9}$"; //Phone number format pattern


                //Input validation
                if(fnametxt.isEmpty() || lnametxt.isEmpty() || phonetxt.isEmpty() || passtxt.isEmpty()){
                    Toast.makeText(Register.this,"Fill all fields",Toast.LENGTH_SHORT).show();
                }else if(!passtxt.equals(conpasstxt)){
                    Toast.makeText(Register.this,"Passwords don't match",Toast.LENGTH_SHORT).show();
                }
                else if (!containsSpecialCharacter(passtxt) || !containsNumber(passtxt) || !containsUppercase(passtxt) || passtxt.length()<6) {
                    // Handle the case where the input contains all three types
                    etpass.setText("");
                    etconpass.setText("");
                    Toast.makeText(Register.this,"Password must be 6+ characters with at least one uppercase letter, one number, and one special character.",Toast.LENGTH_SHORT).show();
                }
                else if(!phonetxt.matches(regexPattern)){
                    etphone.setText("");
                    Toast.makeText(Register.this,"Invalid number format",Toast.LENGTH_SHORT).show();
                    etphone.requestFocus();
                    etphone.setHint("09xx xxx xxxx");
                }
                else{
                    DialogBar.ShowDialog(); // Show the loading dialog
                    databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if(snapshot.hasChild(phonetxt)){
                                DialogBar.HideDialog(); // Dismiss the loading dialog
                                Toast.makeText(Register.this,"Number already registered",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                databaseReference.child("users").child(phonetxt).child("firstname").setValue(fnametxt);
                                databaseReference.child("users").child(phonetxt).child("lastname").setValue(lnametxt);
                                databaseReference.child("users").child(phonetxt).child("password").setValue(passtxt);

                                Toast.makeText(Register.this,"Registered successfully",Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
            }
        });

        tvlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    //Method for internet status
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
    }

    // Helper methods to check for conditions
    private boolean containsSpecialCharacter(String input) {
        return input.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    }

    private boolean containsNumber(String input) {
        return input.matches(".*\\d.*");
    }

    private boolean containsUppercase(String input) {
        return input.matches(".*[A-Z].*");
    }
}