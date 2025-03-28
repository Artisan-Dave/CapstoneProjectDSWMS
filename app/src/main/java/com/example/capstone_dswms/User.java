package com.example.capstone_dswms;

public class User {
    private String userId; // This will be the mobile number and also the ID of the user
    private String name;
    private String lastname;


    public User() {
        // Required empty constructor for Firebase
    }

    public User(String userId, String name, String lastname) {
        this.userId = userId;
        this.name = name;
        this.lastname = lastname;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
}

