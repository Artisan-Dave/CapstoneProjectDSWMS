package com.example.capstone_dswms;

import static android.view.View.GONE;
import static com.example.capstone_dswms.R.id.createadmin;
import static com.example.capstone_dswms.R.id.tvtrashbin1;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.capstone_dswms.databinding.ActivityMainBinding;
import com.google.android.material.navigation.NavigationView;
import com.example.capstone_dswms.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://caps-dswms-default-rtdb.firebaseio.com/");
    ActivityMainBinding binding;

    private long lastBackPressTime = 0;

    private NetworkChangeReceiver networkChangeReceiver;
    public static final String KEY_LOGGED_IN = "isLoggedIn";
    private static final int PERMISSION_REQUEST_SEND_SMS = 1;
    private static final int PERMISSION_REQUEST_NOTIFICATIONS = 2; // Define the constant

    DatabaseReference trashLevelRef = databaseReference.child("trashbin1").child("trashLevel");
    DatabaseReference collectorsRef = databaseReference.child("trashbin1").child("collectors");

    DatabaseReference usersRef = databaseReference.child("users");

    DatabaseReference adminRef = databaseReference.child("admin");
    public SharedPreferences sharedPreferences;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        setContentView(R.layout.activity_main);

        // Retrieve the stored username from SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("KEY_USERNAME", "no data stored");

        Toast.makeText(this, username, Toast.LENGTH_SHORT).show();

        // Check for SMS permission
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.SEND_SMS},
                    PERMISSION_REQUEST_SEND_SMS);
        } else {
            // SMS permission already granted, proceed with checking trash level
            CheckUser();
        }

        // Check for notification permission (regardless of API level)
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        }

        if (!foregroundServiceRunning()) {
            Intent serviceIntent = new Intent(this, MyForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //For android Oreo and later versions
                startForegroundService(serviceIntent);
            }
        }

        handleMenuItemVisibility();

        setSupportActionBar(binding.appBarMain.toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.appBarMain.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

//        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.containers, new HomeFragment());
//        transaction.commit();

        // Create the transaction before using it to replace HomeFragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        HomeFragment fragment = HomeFragment.newInstance(username); // Pass the username value
        transaction.replace(R.id.containers, fragment);
        transaction.commit();

        binding.navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                int id = item.getItemId();

                if (id == R.id.home) {
                    transaction.replace(R.id.containers, new HomeFragment());
//                    HomeFragment fragment = HomeFragment.newInstance(username); // Pass the username value
                    transaction.commit();
                } else if (id == R.id.createadmin) {
                    transaction.replace(R.id.containers, new CreateAdminFragment());
                    transaction.commit();
                } else if (id == R.id.users) {
                    transaction.replace(R.id.containers, new UsersFragment());
                    transaction.commit();
                } else if (id == R.id.changenumber) {
//                    ChangenumberFragment fragment = ChangenumberFragment.newInstance(username); // Pass the username value
                    transaction.replace(R.id.containers, new ChangenumberFragment());
                    transaction.commit();

                } else if (id == R.id.changepass) {
//                    transaction.replace(R.id.containers, new ChangepassFragment());
//                    transaction.commit();
                    ChangepasswordFragment fragment = ChangepasswordFragment.newInstance(username); // Pass the username value
                    transaction.replace(R.id.containers, fragment);
                    transaction.commit();

                } else if (id == R.id.exit) {
                    performLogout();
                }

                binding.drawerLayout.closeDrawer(GravityCompat.START);

                return true;
            }
        });

        // Get the SharedPreferences instance
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        networkChangeReceiver = new NetworkChangeReceiver();

    }

    public boolean foregroundServiceRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)){
            if(MyForegroundService.class.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }


    // This method sends SMS notifications to collectors
    private void sendSmsToCollectors() {
        collectorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot collectorSnapshot : snapshot.getChildren()) {
                    if (collectorSnapshot.child("mobile_number").exists()) {
                        String mobileNumber = collectorSnapshot.child("mobile_number").getValue(String.class);
                        if (mobileNumber != null) {
                            String message = "Trash reached critical level. Please collect the trash.";
                            sendSms(mobileNumber, message);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // This method sends an SMS message to the specified mobile number
    private void sendSms(String mobileNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(mobileNumber, null, message, null, null);
            Toast.makeText(this, "SMS sent to collector", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "SMS sending failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    public void CheckUser(){
        collectorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot collectorSnapshot : snapshot.getChildren()) {
                    // Each child node under "collectors" will represent a generated key

                    if (collectorSnapshot.hasChild("mobile_number")) {
                        String mobileNumber = collectorSnapshot.child("mobile_number").getValue(String.class);

                        if (mobileNumber != null && mobileNumber.equals(username)) {
                            trashLevelRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    // Get the trashLevel value from the dataSnapshot
                                    Integer trashLevelValue = dataSnapshot.getValue(Integer.class);

                                    // Check if the trashLevel has reached 100
                                    if (trashLevelValue != null && trashLevelValue >= 100) {
                                        // Call your notification method when trashLevel reaches 100
                                        checkAndMakeNotification();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Handle any errors that may occur
                                    Toast.makeText(MainActivity.this,"Database Error",Toast.LENGTH_SHORT).show();
                                }
                            });

                            break; // Assuming you want to stop searching after finding the match
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this,"Database Error",Toast.LENGTH_SHORT).show();
            }
        });

        adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(username)){
                    trashLevelRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get the trashLevel value from the dataSnapshot
                            Integer trashLevelValue = dataSnapshot.getValue(Integer.class);

                            // Check if the trashLevel has reached 100
                            if (trashLevelValue != null && trashLevelValue >= 100) {
                                checkAndMakeNotification();
                                sendSmsToCollectors();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle any errors that may occur
                            Toast.makeText(MainActivity.this,"Database Error",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this,"Database Error",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // SMS permission granted
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    // If notification permission is not granted, request it
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            PERMISSION_REQUEST_NOTIFICATIONS);
                } else {
                    // Both SMS and notification permissions granted, proceed
//                    checkTrashLevelAndMakeNotification();
//                    sendSmsToCollectors();
                    CheckUser();
                }
            } else {
                // SMS permission denied
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_REQUEST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Notification permission granted
//                checkTrashLevelAndMakeNotification();
//                sendSmsToCollectors();
                CheckUser();
            } else {
                // Notification permission denied
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void checkAndMakeNotification() {
        int currentApiVersion = Build.VERSION.SDK_INT;
        if (currentApiVersion >= Build.VERSION_CODES.O) {
            makeNotification();
        } else {
            // Handle devices with API level below 26 (e.g., show a regular notification)
            makeRegularNotification();
        }
    }

    private void handleMenuItemVisibility() {
        // Inflate the menu and find the specific menu items by their IDs
        final NavigationView navigationView = findViewById(R.id.nav_view);
        final Menu menu = navigationView.getMenu();
        final MenuItem createAdminMenuItem = menu.findItem(R.id.createadmin);
        final MenuItem usersMenuItem = menu.findItem(R.id.users);

        // Query the Firebase Realtime Database to check if the username exists as a key
        DatabaseReference usersRef = databaseReference.child("users");
        usersRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean usernameExists = dataSnapshot.exists();
                // Update the visibility of the menu items based on the query result
                createAdminMenuItem.setVisible(!usernameExists);
                usersMenuItem.setVisible(!usernameExists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void makeNotification() {
        String channelID = "CHANNEL_ID_NOTIFICATION";
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), channelID);
        builder.setSmallIcon(R.drawable.baseline_notifications_active_24)
                .setContentTitle("Trash Bin Alert")
                .setContentText("Trash reached critical level")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, intent, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    notificationManager.getNotificationChannel(channelID);
            if (notificationChannel == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channelID,
                        "Channel Name", importance);
                notificationChannel.setDescription("Some description");
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        notificationManager.notify(0, builder.build());
    }

    private void makeRegularNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.baseline_notifications_active_24)
                .setContentTitle("Trash Bin Alert")
                .setContentText("Trash reached critical level")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, intent, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }


    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            // If the navigation drawer is open, close it.
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Check if the current fragment is not the home fragment (Fragment1).
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.containers);
            if (currentFragment != null && !(currentFragment instanceof HomeFragment)) {
                // Replace the current fragment with the home fragment.
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                HomeFragment homeFragment = HomeFragment.newInstance(username); // Pass the username value
                transaction.replace(R.id.containers, homeFragment);
                transaction.addToBackStack(null); // Add the transaction to the back stack.
                transaction.commit();
            } else {
                // If you are already on the home fragment, perform a different action.
                // For example, you can show a confirmation dialog to exit the app.
                showExitConfirmationDialog();
            }
        }
    }

    // Method to show a confirmation dialog when exiting the app from the home fragment.
    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit App");
        builder.setMessage("Are you sure you want to exit the app?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked "Yes," exit the app.
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked "No," dismiss the dialog.
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void performLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Logout");
        builder.setMessage("Are you sure you want to log out?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked "Yes", proceed with logout
                // Clear user session or perform any other necessary logout actions

                        // Clear the stored username from SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove("KEY_USERNAME");
                        editor.apply();

                        Intent serviceIntent = new Intent(getApplicationContext(), MyForegroundService.class);

                        // Stop the service
                        stopService(serviceIntent);


                        setLoggedInStatus();

                        // Finish the current activity (optional)
                        finish();

                        // Start the Login activity
                        Intent intent = new Intent(MainActivity.this, Login.class);
                        startActivity(intent);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked "No", dismiss the dialog
                dialogInterface.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void setLoggedInStatus() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_LOGGED_IN, false);
        editor.apply();
    }

    //Method for internet status
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, intentFilter);

        // Retrieve the stored username from SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("KEY_USERNAME", "");

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(networkChangeReceiver);
    }

}
