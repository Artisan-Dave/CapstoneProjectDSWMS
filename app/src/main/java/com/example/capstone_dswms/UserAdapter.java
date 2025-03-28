package com.example.capstone_dswms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.example.capstone_dswms.R;
import com.example.capstone_dswms.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserAdapter extends ArrayAdapter<User> {



    private Context context;
    private ArrayList<User> userList;

    public UserAdapter(Context context, ArrayList<User> userList) {
        super(context, 0, userList);
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_user, parent, false);
        }

        // Get the current User object
        User user = userList.get(position);

        // Set the user ID and name in the TextViews
        TextView textViewUserId = convertView.findViewById(R.id.textViewUserId);
        textViewUserId.setText(user.getUserId());

        // Set the first name and last name in a single TextView, aligned horizontally
        TextView textViewName = convertView.findViewById(R.id.textViewName);
        String fullName = user.getName() + " " + user.getLastname();
        textViewName.setText(fullName);

        // Set the click listener for the item
        convertView.setOnClickListener(view -> showConfirmationDialog(user));

        return convertView;
    }

    private void showConfirmationDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Assign Collector");
        builder.setMessage("Are you sure you want to assign this user as a collector?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Assign the user as a collector
            assignUserAsCollector(user);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            // Do nothing or handle cancellation
        });
        builder.create().show();
    }

    private void assignUserAsCollector(User user) {
        // Get the reference to the "trashbin1" node
        DatabaseReference trashbin1Ref = FirebaseDatabase.getInstance().getReferenceFromUrl("https://caps-dswms-default-rtdb.firebaseio.com/").child("trashbin1");

        // Query the "collectors" node to check for duplicate collectors with the same mobile number
        trashbin1Ref.child("collectors").orderByChild("mobile_number").equalTo(user.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Duplicate collector found, show error message
                    Toast.makeText(context, "This user is already assigned as a collector", Toast.LENGTH_SHORT).show();
                } else {
                    // No duplicate collector found, proceed with assigning the user as a collector
                    // Generate a unique key for the collector
                    String collectorKey = trashbin1Ref.child("collectors").push().getKey();

                    // Create a new child under "collectors" node with the generated key
                    DatabaseReference collectorRef = trashbin1Ref.child("collectors").child(collectorKey);

                    // Set the collector's name and mobile number
                    collectorRef.child("firstname").setValue(user.getName());
                    collectorRef.child("lastname").setValue((user.getLastname()));
                    collectorRef.child("mobile_number").setValue(user.getUserId());

                    Toast.makeText(context, "User assigned as collector: " + user.getName(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error.
            }
        });
    }
}
