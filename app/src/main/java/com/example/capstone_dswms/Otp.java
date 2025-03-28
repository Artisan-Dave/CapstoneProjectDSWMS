package com.example.capstone_dswms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import static com.example.capstone_dswms.MainActivity.KEY_LOGGED_IN;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class Otp extends AppCompatActivity {

    private String phone;
    private String countryCode = "+63";
    private String id="";
    private boolean otpSent = false;

    public SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        final EditText etcode = findViewById(R.id.etcode);
        Button btngetcode = findViewById(R.id.btngetcode);

        // Retrieve the stored username from SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        phone = sharedPreferences.getString("KEY_USERNAME", "no data stored");

        Toast.makeText(Otp.this,"Mobile number: " + phone,Toast.LENGTH_SHORT).show();

        FirebaseApp.initializeApp(this);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        btngetcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(otpSent){
                    final String getOtp = etcode.getText().toString();

                    if(id.isEmpty()){
                        Toast.makeText(Otp.this,"Unable to verify OTP",Toast.LENGTH_SHORT).show();
                    }else{
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(id, getOtp);

                        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()){
                                    FirebaseUser userDetails = task.getResult().getUser();
                                    Toast.makeText(Otp.this,"Verified",Toast.LENGTH_SHORT).show();

                                    // Delete the user's account
                                    userDetails.delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> deleteTask) {
                                                    if (deleteTask.isSuccessful()) {
                                                        // User account deleted successfully. The user's session is terminated.
                                                        Intent intent = new Intent(Otp.this, ForgotPassword.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        // An error occurred while deleting the user.
                                                        // Handle the error, log it, or inform the user as needed.
                                                        Toast.makeText(Otp.this,"Error occured",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                                else {
                                    Toast.makeText(Otp.this,"Something went wrong",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                }
                 else{
                     final String getPhone = String.valueOf(phone);

                    PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                            .setPhoneNumber(countryCode+""+getPhone)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(Otp.this)
                            .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                @Override
                                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                    Toast.makeText(Otp.this,"OTP sent successfully",Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onVerificationFailed(@NonNull FirebaseException e) {

                                    Toast.makeText(Otp.this,"Something went wrong" +e.getMessage(),Toast.LENGTH_SHORT).show();
                                    // Clear the stored username from SharedPreferences
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.remove("KEY_USERNAME");
                                    editor.apply();
                                    Intent intent = new Intent(Otp.this, Login.class);
                                    finish();
                                    startActivity(intent);
                                }
                                @Override
                                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                    super.onCodeSent(s, forceResendingToken);
                                    etcode.setVisibility(View.VISIBLE);
                                    btngetcode.setText("Verify OTP");
                                    id = s;
                                    otpSent = true;
                                }
                            }).build();

                    PhoneAuthProvider.verifyPhoneNumber(options);
                }

            }
        });

    }
    public void setLoggedInStatus() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_LOGGED_IN, false);
        editor.apply();
    }
    private void setLoggedInStatus(boolean isLoggedIn) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_LOGGED_IN, isLoggedIn);
        editor.apply();
    }
}