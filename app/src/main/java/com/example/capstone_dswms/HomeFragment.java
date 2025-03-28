package com.example.capstone_dswms;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String username;

    private ImageView ivaddlocation;
    private ImageView ivcollectors;


    // Obtain a reference to the Firebase Realtime Database
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://caps-dswms-default-rtdb.firebaseio.com/");


    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(String username) {
        HomeFragment fragment = new HomeFragment();
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
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
//        return inflater.inflate(R.layout.fragment_home, container, false);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        // Initialize the TextView
        TextView tvPercentageGreen = view.findViewById(R.id.tvPercentageGreen);
        TextView tvPercentageYellow = view.findViewById(R.id.tvPercentageYellow);
        TextView tvPercentageRed = view.findViewById(R.id.tvPercentageRed);
        TextView tvtrashbin1 = view.findViewById(R.id.tvtrashbin1);

        // Set up the progress bar
        ProgressBar progressBarGreen = view.findViewById(R.id.progressBarGreen);
        ProgressBar progressBarYellow = view.findViewById(R.id.progressBarYellow);
        ProgressBar progressBarRed = view.findViewById(R.id.progressBarRed);

        //Imageview initialization
        ImageView ivaddlocation = view.findViewById(R.id.ivaddlocation);
        ImageView ivlocation = view.findViewById(R.id.ivlocation);
        ImageView ivcollectors = view.findViewById(R.id.ivcollectors);



        // Retrieve the username value from the arguments
//        Bundle args = getArguments();
//        if (args != null && args.containsKey("username")) {
//            username = args.getString("username");
//
//        }
        // Retrieve data from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("KEY_USERNAME", "No data stored");
        Toast.makeText(getContext(),"Mobile number: "+username,Toast.LENGTH_SHORT).show();

        // Check if the username exists as a key in the admin node
        DatabaseReference adminRef = databaseReference.child("admin");
        adminRef.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot adminSnapshot) {
                boolean usernameExistsInAdmin = adminSnapshot.exists();

                if (usernameExistsInAdmin) {
                    // Username matches a key in the admin node, show the ImageViews
                    ivaddlocation.setVisibility(View.VISIBLE);
                    ivcollectors.setVisibility(View.VISIBLE);

                    // Obtain a reference to the "trashbin1" child node
                    DatabaseReference trashbinRef = databaseReference.child("trashbin1");

                    // Listen for changes in the trash level value
                    ValueEventListener valueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            // Retrieve the trash level value
                            int trashLevel = dataSnapshot.getValue(Integer.class);

                            if(trashLevel<50){
                                progressBarYellow.setVisibility(view.GONE);
                                tvPercentageYellow.setVisibility(view.GONE);
                                progressBarRed.setVisibility(view.GONE);
                                tvPercentageRed.setVisibility(view.GONE);
                                progressBarGreen.setVisibility(view.VISIBLE);
                                tvPercentageGreen.setVisibility(view.VISIBLE);
                                progressBarGreen.setProgress(trashLevel);
                                tvPercentageGreen.setText(trashLevel+"%");
                            }
                            else if(trashLevel>=50 && trashLevel <90){
                                progressBarGreen.setVisibility(view.GONE);
                                tvPercentageGreen.setVisibility(view.GONE);
                                progressBarRed.setVisibility(view.GONE);
                                tvPercentageRed.setVisibility(view.GONE);
                                progressBarYellow.setVisibility(view.VISIBLE);
                                tvPercentageYellow.setVisibility(view.VISIBLE);
                                progressBarYellow.setProgress(trashLevel);
                                tvPercentageYellow.setText(trashLevel+"%");

                            }
                            else if(trashLevel >=90 && trashLevel <=100){
                                progressBarGreen.setVisibility(view.GONE);
                                tvPercentageGreen.setVisibility(view.GONE);
                                progressBarYellow.setVisibility(view.GONE);
                                tvPercentageYellow.setVisibility(view.GONE);
                                progressBarRed.setVisibility(view.VISIBLE);
                                tvPercentageRed.setVisibility(view.VISIBLE);
                                progressBarRed.setProgress(trashLevel);
                                tvPercentageRed.setText(trashLevel+"%");

                            }else{
                                Toast.makeText(getContext(), "Invalid Distance", Toast.LENGTH_SHORT).show();
                            }

//                // Update the progress bar
//                progressBar.setProgress(trashLevel);
//                tvPercentage.setText(trashLevel+"%");

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle any errors
                        }
                    };

                    // Start listening for value changes
                    trashbinRef.child("trashLevel").addValueEventListener(valueEventListener);


                } else {
                    // Username does not match any key in the admin node

                    // Proceed to check if the username exists in the collectors node
                    DatabaseReference collectorsRef = databaseReference.child("trashbin1").child("collectors");
                    collectorsRef.orderByChild("mobile_number").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot collectorsSnapshot) {
                            boolean usernameExistsInCollectors = collectorsSnapshot.exists();

                            if (usernameExistsInCollectors) {
                                // Username matches a mobile_number path in collectors node, hide the ImageViews
                                ivaddlocation.setVisibility(View.INVISIBLE);
                                ivcollectors.setVisibility(View.INVISIBLE);

                                // Obtain a reference to the "trashbin1" child node
                                DatabaseReference trashbinRef = databaseReference.child("trashbin1");

                                // Listen for changes in the trash level value
                                ValueEventListener valueEventListener = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        // Retrieve the trash level value
                                        int trashLevel = dataSnapshot.getValue(Integer.class);

                                        if(trashLevel<50){
                                            progressBarYellow.setVisibility(view.GONE);
                                            tvPercentageYellow.setVisibility(view.GONE);
                                            progressBarRed.setVisibility(view.GONE);
                                            tvPercentageRed.setVisibility(view.GONE);
                                            progressBarGreen.setVisibility(view.VISIBLE);
                                            tvPercentageGreen.setVisibility(view.VISIBLE);
                                            progressBarGreen.setProgress(trashLevel);
                                            tvPercentageGreen.setText(trashLevel+"%");
                                        }
                                        else if(trashLevel>=50 && trashLevel <90){
                                            progressBarGreen.setVisibility(view.GONE);
                                            tvPercentageGreen.setVisibility(view.GONE);
                                            progressBarRed.setVisibility(view.GONE);
                                            tvPercentageRed.setVisibility(view.GONE);
                                            progressBarYellow.setVisibility(view.VISIBLE);
                                            tvPercentageYellow.setVisibility(view.VISIBLE);
                                            progressBarYellow.setProgress(trashLevel);
                                            tvPercentageYellow.setText(trashLevel+"%");

                                        }
                                        else if(trashLevel >=90 && trashLevel <=100){
                                            progressBarGreen.setVisibility(view.GONE);
                                            tvPercentageGreen.setVisibility(view.GONE);
                                            progressBarYellow.setVisibility(view.GONE);
                                            tvPercentageYellow.setVisibility(view.GONE);
                                            progressBarRed.setVisibility(view.VISIBLE);
                                            tvPercentageRed.setVisibility(view.VISIBLE);
                                            progressBarRed.setProgress(trashLevel);
                                            tvPercentageRed.setText(trashLevel+"%");

                                        }else{
                                            Toast.makeText(getContext(), "Invalid Distance", Toast.LENGTH_SHORT).show();
                                        }

//                // Update the progress bar
//                progressBar.setProgress(trashLevel);
//                tvPercentage.setText(trashLevel+"%");

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        // Handle any errors
                                        Toast.makeText(getContext(),"Database Error",Toast.LENGTH_SHORT).show();
                                    }
                                };

                                // Start listening for value changes
                                trashbinRef.child("trashLevel").addValueEventListener(valueEventListener);


                            } else {
                                // Username does not match any key in admin or mobile_number path in collectors node, show the ImageViews
                                tvtrashbin1.setText("No trashbin assigned");
                                tvPercentageGreen.setVisibility(View.GONE);
                                tvPercentageYellow.setVisibility(View.GONE);
                                tvPercentageRed.setVisibility(View.GONE);

                                progressBarGreen.setVisibility(View.GONE);
                                progressBarYellow.setVisibility(View.GONE);
                                progressBarRed.setVisibility(View.GONE);

                                ivaddlocation.setVisibility(View.GONE);
                                ivlocation.setVisibility(View.GONE);
                                ivcollectors.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError collectorsError) {
                            // Handle any errors in collectors node read
                            Toast.makeText(getContext(),"Database Error",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError adminError) {
                // Handle any errors in admin node read
                Toast.makeText(getContext(),"Database Error",Toast.LENGTH_SHORT).show();
            }
        });

        ivaddlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), uploadImage.class));

            }
        });

        ivlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ViewLocation.class));
            }
        });

        ivcollectors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), CollectorsFragment.class));
            }
        });

        return view;
    }



}

