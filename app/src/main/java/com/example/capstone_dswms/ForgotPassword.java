package com.example.capstone_dswms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;



import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ForgotPassword extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://caps-dswms-default-rtdb.firebaseio.com/");

    private NetworkChangeReceiver networkChangeReceiver;

    LoadingDialog DialogBar;
    private SharedPreferences sharedPreferences;

    private String phone;

    Toolbar mytoolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);


        final EditText etphone = findViewById(R.id.etphone);
        final EditText etnewpass = findViewById(R.id.etnewpass);
        final Button btnsubmit = findViewById(R.id.btnsubmit);
        mytoolbar = findViewById(R.id.toolbar);

        DialogBar = new LoadingDialog(this);

        mytoolbar.setTitle("Forgot Password");

        networkChangeReceiver = new NetworkChangeReceiver();

        // Retrieve the stored username from SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        phone = sharedPreferences.getString("KEY_USERNAME", "no data stored");
        
        etphone.setText(phone);

        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String phonetxt = etphone.getText().toString().trim();
                final String newpasstxt = etnewpass.getText().toString().trim();

                //Validation
                if(phonetxt.isEmpty() || newpasstxt.isEmpty()){
                    Toast.makeText(ForgotPassword.this,"Fill all fields",Toast.LENGTH_SHORT).show();
                }else if (newpasstxt.length() < 6 || !containsUppercase(newpasstxt) || !containsNumber(newpasstxt) || !containsSpecialCharacter(newpasstxt)) {
                    Toast.makeText(ForgotPassword.this, "Password must be 6+ characters with at least one uppercase letter, one number, and one special character.", Toast.LENGTH_SHORT).show();
                    etnewpass.setText("");
                }
                else{
                    DialogBar.ShowDialog(); // Show the loading dialog
                    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.child("users").hasChild(phonetxt)) {
                                Toast.makeText(ForgotPassword.this, "Number registered", Toast.LENGTH_SHORT).show();
                                databaseReference.child("users").child(phonetxt).child("password").setValue(newpasstxt);
                                Toast.makeText(ForgotPassword.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                // Clear the stored username from SharedPreferences
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.remove("KEY_USERNAME");
                                editor.apply();
                                finish();
                            } else if (snapshot.child("admin").hasChild(phonetxt)) {
                                Toast.makeText(ForgotPassword.this, "Number registered as admin", Toast.LENGTH_SHORT).show();
                                databaseReference.child("admin").child(phonetxt).child("password").setValue(newpasstxt);
                                Toast.makeText(ForgotPassword.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                                // Clear the stored username from SharedPreferences
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.remove("KEY_USERNAME");
                                editor.apply();
                                finish();
                            } else {
                                DialogBar.HideDialog();
                                etphone.setText("");
                                etphone.setError("Invalid number");
                                etphone.requestFocus();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(ForgotPassword.this, "Process Error", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

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