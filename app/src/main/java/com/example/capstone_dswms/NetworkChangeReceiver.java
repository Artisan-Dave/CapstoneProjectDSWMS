package com.example.capstone_dswms;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

public class NetworkChangeReceiver extends BroadcastReceiver {

    //Inner class for internet status
    @Override
    public void onReceive(Context context, Intent intent) {

        if (isNetworkAvailable(context)) {
            // Internet connection restored
            Toast.makeText(context, "Internet connection restored", Toast.LENGTH_SHORT).show();
        } else {
            // Internet connection disconnected
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Error");
            builder.setMessage("Please Check Internet Connection");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Positive button clicked
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    //Check if internet is available
    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
}
