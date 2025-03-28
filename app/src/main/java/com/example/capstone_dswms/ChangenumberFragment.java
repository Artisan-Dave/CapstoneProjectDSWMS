package com.example.capstone_dswms;



import static com.example.capstone_dswms.MainActivity.KEY_LOGGED_IN;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChangenumberFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class
ChangenumberFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://caps-dswms-default-rtdb.firebaseio.com/");

    public SharedPreferences sharedPreferences;

    private String username;
    private TextView tvusername;
    private EditText etnewnumber;
    private Button btnsubmit;

    LoadingDialog DialogBar;



    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;

    private String mParam2;

    public ChangenumberFragment() {
        // Required empty public constructor
    }

    public static ChangenumberFragment newInstance(String username) {
        ChangenumberFragment fragment = new ChangenumberFragment();
        Bundle args = new Bundle();
        args.putString("username", username);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChangenumberFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChangenumberFragment newInstance(String param1, String param2) {
        ChangenumberFragment fragment = new ChangenumberFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_changenumber, container, false);

        View view = inflater.inflate(R.layout.fragment_changenumber, container, false);

        tvusername = view.findViewById(R.id.tvusername);
        etnewnumber = view.findViewById(R.id.etnewnumber);
        btnsubmit = view.findViewById(R.id.btnsubmit);

        // Retrieve data from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("KEY_USERNAME", "No data stored");
        Toast.makeText(getContext(),"Mobile number: "+username,Toast.LENGTH_SHORT).show();

        DialogBar = new LoadingDialog (getContext());

        tvusername.setText(username);


        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newNumber = etnewnumber.getText().toString().trim();
                String userId = tvusername.getText().toString().trim();

                DialogBar.ShowDialog();

                // Retrieve existing user details
                DatabaseReference userRef = databaseReference.child("users").child(userId);
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Check if the user exists
                        if (snapshot.exists()) {
                            // Get the user details
                            String firstname = snapshot.child("firstname").getValue(String.class);
                            String lastname = snapshot.child("lastname").getValue(String.class);
                            String password = snapshot.child("password").getValue(String.class);

                            final String regexPattern = "^09\\d{9}$";

                            // Check if the newNumber matches the regex pattern
                            if (newNumber.matches(regexPattern)) {
                                // Validate if the newNumber exists in the admin node
                                DatabaseReference adminRef = databaseReference.child("admin");
                                Query adminQuery = adminRef.orderByKey().equalTo(newNumber);
                                adminQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            // User ID already exists in the admin node, display error message
                                            DialogBar.HideDialog();
                                            Toast.makeText(getContext(), "User ID already exists", Toast.LENGTH_SHORT).show();
                                        } else {
                                            // Validate if the newNumber exists in the users node
                                            DatabaseReference usersRef = databaseReference.child("users");
                                            Query usersQuery = usersRef.orderByKey().equalTo(newNumber);
                                            usersQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.exists()) {
                                                        // User ID already exists in the users node, display error message
                                                        DialogBar.HideDialog();
                                                        Toast.makeText(getContext(), "User ID already exists", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        // Delete the existing user
                                                        userRef.removeValue();
                                                        DialogBar.ShowDialog();

                                                        // Create a new user with the updated user ID
                                                        DatabaseReference newUserRef = databaseReference.child("users").child(newNumber);
                                                        newUserRef.child("firstname").setValue(firstname);
                                                        newUserRef.child("lastname").setValue(lastname);
                                                        newUserRef.child("password").setValue(password);

                                                        // Get a reference to the collectors node under trashbin1
                                                        DatabaseReference collectorsRef = databaseReference.child("trashbin1").child("collectors");

                                                        collectorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                // Iterate through each child node under collectors
                                                                for (DataSnapshot collectorSnapshot : snapshot.getChildren()) {
                                                                    // Get the key of the collector node
                                                                    String collectorKey = collectorSnapshot.getKey();

                                                                    // Get the mobile number under the collector node
                                                                    String mobileNumber = collectorSnapshot.child("mobile_number").getValue(String.class);

                                                                    // Check if the mobile number matches the userId
                                                                    if (mobileNumber.equals(userId)) {
                                                                        // Replace the mobile number with the newNumber
                                                                        collectorsRef.child(collectorKey).child("mobile_number").setValue(newNumber);

                                                                        // Display success message
                                                                        Toast.makeText(getContext(), "Mobile number updated successfully", Toast.LENGTH_SHORT).show();
                                                                        // Inside your fragment
                                                                        Intent intent = new Intent(getActivity(), MainActivity.class);
                                                                        startActivity(intent);
                                                                    }
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError error) {
                                                                Toast.makeText(getContext(), "Failed to update mobile number in collectors", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });

                                                        // Clear the stored username from SharedPreferences
                                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                                        editor.remove("KEY_USERNAME");
                                                        editor.apply();

                                                        // Store the phone number in SharedPreferences
                                                        editor = sharedPreferences.edit();
                                                        editor.putString("KEY_USERNAME", newNumber);
                                                        editor.apply();

                                                        Toast.makeText(getContext(), "User ID updated successfully", Toast.LENGTH_SHORT).show();

                                                        DialogBar.HideDialog();
                                                        // Log out and close the app
//                                                        MainActivity activity = (MainActivity) requireActivity();
//                                                        activity.logoutchangenumber();
                                                        // Inside your fragment
                                                        Intent intent = new Intent(getActivity(), MainActivity.class);
                                                        startActivity(intent);

                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    DialogBar.HideDialog();
                                                    Toast.makeText(getContext(), "Failed to validate User ID", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        DialogBar.HideDialog();
                                        Toast.makeText(getContext(), "Failed to validate User ID in admin node", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                DialogBar.HideDialog();
                                Toast.makeText(getContext(), "Invalid phone number format for users", Toast.LENGTH_SHORT).show();
                                etnewnumber.setText("");
                                etnewnumber.setHint("09xx-xxx-xxxx");
                            }

                        } else {
                            // Check if the snapshot exists in the admin node
                            DatabaseReference adminRef = databaseReference.child("admin").child(userId);
                            adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    // Check if the admin exists
                                    if (snapshot.exists()) {
                                        // Get the admin details
                                        String firstname = snapshot.child("firstname").getValue(String.class);
                                        String lastname = snapshot.child("lastname").getValue(String.class);
                                        String password = snapshot.child("password").getValue(String.class);

                                        // Check if the newNumber matches a record in the admin node
                                        DatabaseReference adminQueryRef = databaseReference.child("admin");
                                        Query adminQuery = adminQueryRef.orderByKey().equalTo(newNumber);
                                        adminQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    // New number already exists as a key in admin records, display error message
                                                    DialogBar.HideDialog();
                                                    Toast.makeText(getContext(), "New number already exists", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    // Check if the newNumber exists in the users node
                                                    DatabaseReference userQueryRef = databaseReference.child("users");
                                                    Query userQuery = userQueryRef.orderByKey().equalTo(newNumber);
                                                    userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.exists()) {
                                                                // New number already exists in users records, display error message
                                                                DialogBar.HideDialog();
                                                                Toast.makeText(getContext(), "New number already exists", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                // Delete the existing admin
                                                                adminRef.removeValue();
                                                                DialogBar.ShowDialog();

                                                                // Create a new admin with the updated user ID
                                                                DatabaseReference newAdminRef = databaseReference.child("admin").child(newNumber);
                                                                newAdminRef.child("firstname").setValue(firstname);
                                                                newAdminRef.child("lastname").setValue(lastname);
                                                                newAdminRef.child("password").setValue(password);

                                                                // Clear the stored username from SharedPreferences
                                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                                editor.remove("KEY_USERNAME");
                                                                editor.apply();

                                                                // Store the phone number in SharedPreferences
                                                                editor = sharedPreferences.edit();
                                                                editor.putString("KEY_USERNAME", newNumber);
                                                                editor.apply();

                                                                Toast.makeText(getContext(), "User ID updated successfully", Toast.LENGTH_SHORT).show();

                                                                // Log out and close the app
//                                                                MainActivity activity = (MainActivity) requireActivity();
//                                                                activity.logoutchangenumber();

                                                                DialogBar.HideDialog();
                                                                // Inside your fragment
                                                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                                                startActivity(intent);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            DialogBar.HideDialog();
                                                            Toast.makeText(getContext(), "Failed to validate User ID in users records", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                DialogBar.HideDialog();
                                                Toast.makeText(getContext(), "Failed to validate User ID in admin records", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        DialogBar.HideDialog();
                                        Toast.makeText(getContext(), "User ID does not exist", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(getContext(), "Failed to update User ID", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to update User ID", Toast.LENGTH_SHORT).show();
                    }
                });



            }
        });

        return view;
    }

}