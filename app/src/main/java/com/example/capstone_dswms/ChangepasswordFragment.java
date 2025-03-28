package com.example.capstone_dswms;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChangepasswordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChangepasswordFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://caps-dswms-default-rtdb.firebaseio.com/");

    private String username;
    private TextView tvusername; // Declare tvUsername as a member variable
    private EditText etoldpass;
    private EditText etnewpass;
    private Button btnsubmit;

    private ProgressDialog loadingDialog;

    LoadingDialog DialogBar;

//    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://caps-dswms-default-rtdb.firebaseio.com/");

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public ChangepasswordFragment() {
        // Required empty public constructor
    }

    public static ChangepasswordFragment newInstance(String username) {
        ChangepasswordFragment fragment = new ChangepasswordFragment();
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
     * @return A new instance of fragment EditprofFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChangepasswordFragment newInstance(String param1, String param2) {
        ChangepasswordFragment fragment = new ChangepasswordFragment();
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
//        return inflater.inflate(R.layout.fragment_changepass, container, false);

        View view = inflater.inflate(R.layout.fragment_changepass, container, false);

        tvusername = view.findViewById(R.id.tvusername); // Initialize tvUsername
        etoldpass = view.findViewById(R.id.etoldpass);
        etnewpass = view.findViewById(R.id.etnewpass);
        btnsubmit = view.findViewById(R.id.btnsubmit);

        String oldpass = etoldpass.getText().toString().trim();
        String newpass = etnewpass.getText().toString().trim();

        DialogBar = new LoadingDialog(getContext());

        // Retrieve the username value from the arguments
//        Bundle args = getArguments();
//        if (args != null && args.containsKey("username")) {
//            username = args.getString("username");

        // Retrieve data from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("KEY_USERNAME", "No data stored");

            // Use the username value as needed
            if (username != null) {
                tvusername.setText(username);
            }
//        }

        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = tvusername.getText().toString().trim();
                String oldPassword = etoldpass.getText().toString().trim();
                String newPassword = etnewpass.getText().toString().trim();

                if (username.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty()) {
                    Toast.makeText(getContext(), "Fill all fields", Toast.LENGTH_SHORT).show();
                }else if (newPassword.length() < 6 || !containsUppercase(newPassword) || !containsNumber(newPassword) || !containsSpecialCharacter(newPassword)) {
                    Toast.makeText(getContext(), "Password must be 6+ characters with at least one uppercase letter, one number, and one special character.", Toast.LENGTH_SHORT).show();
                    etnewpass.setText("");
                }else {
                    DialogBar.ShowDialog();

                    DatabaseReference userRef = databaseReference.child("users").child(username);
                    DatabaseReference adminRef = databaseReference.child("admin").child(username);

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String storedOldPassword = snapshot.child("password").getValue(String.class);

                                if (storedOldPassword.equals(oldPassword)) {
                                    snapshot.getRef().child("password").setValue(newPassword);
                                    Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Old password is incorrect", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            String storedOldPassword = snapshot.child("password").getValue(String.class);

                                            if (storedOldPassword.equals(oldPassword)) {
                                                snapshot.getRef().child("password").setValue(newPassword);
                                                Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                                                // Inside your fragment
                                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                                startActivity(intent);
                                            } else {
                                                Toast.makeText(getContext(), "Old password is incorrect", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                                        }

                                        DialogBar.HideDialog();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(getContext(), "Failed to change password", Toast.LENGTH_SHORT).show();
                                        DialogBar.HideDialog();
                                    }
                                });
                            }

                            DialogBar.HideDialog();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Failed to change password", Toast.LENGTH_SHORT).show();
                            DialogBar.HideDialog();
                        }
                    });

                }
            }
        });

        return view;

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