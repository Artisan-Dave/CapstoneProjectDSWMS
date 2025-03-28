package com.example.capstone_dswms;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateAdminFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateAdminFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://caps-dswms-default-rtdb.firebaseio.com/");
    private EditText etfirstname, etlastname, etnumber, etpass, etconpass;
    private Button btntregister;

    private ProgressDialog loadingDialog;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CreateAdminFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateAdminFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateAdminFragment newInstance(String param1, String param2) {
        CreateAdminFragment fragment = new CreateAdminFragment();
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
//        return inflater.inflate(R.layout.fragment_create_admin, container, false);
        View view = inflater.inflate(R.layout.fragment_create_admin, container, false);

        etfirstname = view.findViewById(R.id.etfirstname);
        etlastname = view.findViewById(R.id.etlastname);
        etnumber = view.findViewById(R.id.etnumber);
        etpass = view.findViewById(R.id.etpass);
        etconpass = view.findViewById(R.id.etconpass);
        btntregister = view.findViewById(R.id.btnregister);

        btntregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String fnametxt = etfirstname.getText().toString().trim();
                final String lnametxt = etlastname.getText().toString().trim();
                final String phonetxt = etnumber.getText().toString().trim();
                final String passtxt = etpass.getText().toString().trim();
                final String conpasstxt = etconpass.getText().toString().trim();

                //Input validation
                if(fnametxt.isEmpty() || lnametxt.isEmpty() || phonetxt.isEmpty() || passtxt.isEmpty()){
                    Toast.makeText(getContext(),"Fill all fields",Toast.LENGTH_SHORT).show();
                }else if(!passtxt.equals(conpasstxt)){
                    Toast.makeText(getContext(),"Passwords don't match",Toast.LENGTH_SHORT).show();
                }else if (passtxt.length() < 6 || !containsUppercase(passtxt) || !containsNumber(passtxt) || !containsSpecialCharacter(passtxt)) {
                    Toast.makeText(getContext(), "Password must be 6+ characters with at least one uppercase letter, one number, and one special character.", Toast.LENGTH_SHORT).show();
                    etpass.setText("");
                    etconpass.setText("");
                }
                else{
                    showLoadingDialog(); // Show the loading dialog
                    DatabaseReference adminRef = databaseReference.child("admin").child(phonetxt); // Use phonetxt as the key
                    adminRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                dismissLoadingDialog(); // Dismiss the loading dialog
                                Toast.makeText(getContext(), "Number already registered", Toast.LENGTH_SHORT).show();
                            } else {
                                adminRef.child("firstname").setValue(fnametxt);
                                adminRef.child("lastname").setValue(lnametxt);
                                adminRef.child("password").setValue(passtxt);

                                etfirstname.setText("");
                                etlastname.setText("");
                                etnumber.setText("");
                                etpass.setText("");
                                etconpass.setText("");
                                dismissLoadingDialog(); // Dismiss the loading dialog
                                Toast.makeText(getContext(), "Registered successfully", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }

            }
        });

        return view;
    }

    //show dialog method
    private void showLoadingDialog() {
        loadingDialog = new ProgressDialog(getActivity());
        loadingDialog.setCancelable(true);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();
        loadingDialog.setContentView(R.layout.custom_dialog_loading);
    }

    //dismiss dialog method
    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
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