package com.example.capstone_dswms;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import pl.droidsonroids.gif.GifImageView;


public class Login extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://caps-dswms-default-rtdb.firebaseio.com/");
    private static final int INTERNET_PERMISSION_REQUEST_CODE = 1;

    //For Internet Status
  private NetworkChangeReceiver networkChangeReceiver;

    //For loading screen dialog
    LoadingDialog DialogBar;

    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_LOGGED_IN = "isLoggedIn";
    private SharedPreferences sharedPreferences;

    EditText etpass;

    //boolean for toggle password
    boolean passwordVisible;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        final TextView tvsignup = findViewById(R.id.tvsignup);
        final TextView tvforgot = findViewById(R.id.tvforgot);
        etpass = findViewById(R.id.etpass);

        DialogBar = new LoadingDialog(this);

        networkChangeReceiver = new NetworkChangeReceiver();

        // Get the SharedPreferences instance
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);


        etpass.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                final int Right = 2; // Index of the right drawable (eye icon)
                float touchX = motionEvent.getRawX(); // Get the X-coordinate of the touch

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    // Calculate the right boundary of the right drawable (eye icon)
                    int drawableRightBoundary = etpass.getRight() - etpass.getCompoundDrawables()[Right].getBounds().width();

                    if (touchX >= drawableRightBoundary) {
                        int selection = etpass.getSelectionEnd();
                        if (passwordVisible) {
                            etpass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            etpass.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                    R.drawable.baseline_lock_24, // Set your padlock drawable here
                                    0,
                                    R.drawable.baseline_visibility_off_24,
                                    0
                            );
                            passwordVisible = false;
                        } else {
                            etpass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                            etpass.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                    R.drawable.baseline_lock_24, // Set your padlock drawable here
                                    0,
                                    R.drawable.baseline_visibility_24,
                                    0
                            );
                            passwordVisible = true;
                        }
                        etpass.setSelection(selection); // Restore cursor position
                        return true; // Consume the touch event
                    }
                }

                return false; // Let other touch events be handled normally
            }
        });


        boolean isLoggedIn = getLoggedInStatus();

        // Check if the permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted
            performAppOperation();
        } else {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, INTERNET_PERMISSION_REQUEST_CODE);
        }

        if (isLoggedIn) {
            // User is already logged in, proceed to the second activity
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
            finish();

        }else {
            final EditText etphone = findViewById(R.id.etphone);
            final EditText etpass = findViewById(R.id.etpass);
            final Button btnlogin = findViewById(R.id.btnlogin);

            btnlogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String phonetxt = etphone.getText().toString();
                    final String passtxt = etpass.getText().toString();

                    // Input validation
                    if (phonetxt.isEmpty() || passtxt.isEmpty()) {
                        Toast.makeText(Login.this, "Please enter mobile number or password", Toast.LENGTH_SHORT).show();
                    } else {
                        DialogBar.ShowDialog(); // Show the loading dialog

                        // Check if the phone number exists in the "users" node
                        databaseReference.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                                if (userSnapshot.hasChild(phonetxt)) {
                                    final String getpass = userSnapshot.child(phonetxt).child("password").getValue(String.class);

                                    if (getpass.equals(passtxt)) {
                                        // Phone number exists in the "users" node and password matches
                                        tvforgot.setVisibility(View.GONE);
                                        Toast.makeText(Login.this, "Successfully Logged in", Toast.LENGTH_SHORT).show();
                                        setLoggedInStatus(true);

//                                        // Update the isloggedin child node to "1" for the logged-in user
//                                        databaseReference.child("users").child(phonetxt).child("isLoggedin").setValue("1");

//                                        // Retrieve FCM Device Token
//                                        FirebaseMessaging.getInstance().getToken()
//                                                .addOnCompleteListener(task -> {
//                                                    if (task.isSuccessful() && task.getResult() != null) {
//                                                        String deviceToken = task.getResult();
//                                                        saveDeviceToken(deviceToken, phonetxt); // Save the device token to the database
//                                                    } else {
//                                                        // Handle error in getting the device token
//                                                        Toast.makeText(Login.this, "Error getting device token", Toast.LENGTH_SHORT).show();
//                                                    }
//                                                });

                                        Intent intent = new Intent(Login.this, MainActivity.class);
                                        // Store the phone number in SharedPreferences
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("KEY_USERNAME", phonetxt);
                                        editor.apply();
                                        startActivity(intent);
                                        finish();
                                        DialogBar.HideDialog(); // Dismiss the loading dialog
                                        return; // Exit the method to avoid checking the "admin" node
                                    } else {
                                        // Phone number exists in the "users" node but invalid password
                                        DialogBar.HideDialog(); // Dismiss the loading dialog
                                        etpass.setText("");
                                        Toast.makeText(Login.this, "Invalid password", Toast.LENGTH_SHORT).show();
                                        // Store the phone number in SharedPreferences
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("KEY_USERNAME", phonetxt);
                                        editor.apply();
                                        tvforgot.setVisibility(View.VISIBLE);
                                        return; // Exit the method to avoid checking the "admin" node
                                    }
                                }

                                // Check if the phone number exists in the "admin" node
                                databaseReference.child("admin").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot adminSnapshot) {
                                        if (adminSnapshot.hasChild(phonetxt)) {
                                            final String getpass = adminSnapshot.child(phonetxt).child("password").getValue(String.class);

                                            if (getpass.equals(passtxt)) {
                                                // Phone number exists in the "admin" node and password matches
                                                tvforgot.setVisibility(View.GONE);
                                                Toast.makeText(Login.this, "Successfully Logged in as Admin", Toast.LENGTH_SHORT).show();
                                                setLoggedInStatus(true);
                                                Intent intent = new Intent(Login.this, MainActivity.class);
                                                // Store the phone number in SharedPreferences
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putString("KEY_USERNAME", phonetxt);
                                                editor.apply();
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                // Phone number exists in the "admin" node but invalid password
                                                DialogBar.HideDialog(); // Dismiss the loading dialog
                                                etpass.setText("");
                                                // Store the phone number in SharedPreferences
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putString("KEY_USERNAME", phonetxt);
                                                editor.apply();
                                                tvforgot.setVisibility(View.VISIBLE);
                                            }
                                        } else {
                                            // Phone number does not exist in both "users" and "admin" nodes
                                            DialogBar.HideDialog(); // Dismiss the loading dialog
                                            etphone.setText("");
                                            Toast.makeText(Login.this, "Invalid number", Toast.LENGTH_SHORT).show();
                                            // Clear the stored username from SharedPreferences
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.remove("KEY_USERNAME");
                                            editor.apply();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        DialogBar.HideDialog(); // Dismiss the loading dialog (in case of cancellation)
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                DialogBar.HideDialog(); // Dismiss the loading dialog (in case of cancellation)
                            }
                        });
                    }
                }
            });
        }

        tvsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });

        tvforgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, ForgotPassword.class));
            }
        });

    }


    // Method to save the device token to the database
//    private void saveDeviceToken(String deviceToken, String phonetxt) {
//        // Save the device token to the database under the user's node (either "users" or "admin")
//        databaseReference.child("users").child(phonetxt).child("deviceToken").setValue(deviceToken)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        // Device token saved successfully
//                        Toast.makeText(Login.this, "Device token saved successfully", Toast.LENGTH_SHORT).show();
//                    } else {
//                        // Handle error in saving the device token
//                        Toast.makeText(Login.this, "Error saving device token", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

    private boolean getLoggedInStatus() {
        return sharedPreferences.getBoolean(KEY_LOGGED_IN, false);
    }

    private void setLoggedInStatus(boolean isLoggedIn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_LOGGED_IN, isLoggedIn);
        editor.apply();
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

    //Internet permission method
        private void performAppOperation() {
            // Perform your app operations that require internet permission here
            //Toast.makeText(this, "Internet permission granted", Toast.LENGTH_SHORT).show();
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == INTERNET_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, perform the app operation
                performAppOperation();
            } else {
                // Permission denied
                Toast.makeText(this, "Internet permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}