package com.example.capstone_dswms;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UsersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UsersFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private ListView listViewUsers;
    private ArrayList<User> userList;
    private ArrayAdapter<User> adapter;
    private DatabaseReference usersRef;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UsersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UsersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UsersFragment newInstance(String param1, String param2) {
        UsersFragment fragment = new UsersFragment();
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
//        return inflater.inflate(R.layout.fragment_users, container, false);
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        // Initialize the ListView
        listViewUsers = view.findViewById(R.id.listViewUsers);

        // Create an ArrayList to hold the user data
        userList = new ArrayList<>();

        // Create the adapter
        adapter = new UserAdapter(requireContext(), userList);

        // Set the adapter to the ListView
        listViewUsers.setAdapter(adapter);

        // Get the reference to "users" node in the Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance().getReferenceFromUrl("https://caps-dswms-default-rtdb.firebaseio.com/").getDatabase();
        usersRef = database.getReference("users");


        // Attach a ValueEventListener to listen for changes in the "users" node
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear(); // Clear the existing user data

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    // Iterate through all the child nodes under "users"
                    String userId = userSnapshot.getKey(); // Get the user ID (mobile number)
                    String name = userSnapshot.child("firstname").getValue(String.class);
                    String lastname = userSnapshot.child("lastname").getValue(String.class);


                    // Create a User object and add it to the userList
                    User user = new User(userId, name, lastname
                    );
                    userList.add(user);
                }

                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error.
            }
        });


        return view;
    }
}