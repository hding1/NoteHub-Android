package com.example.peterding.myapplication;

import java.util.ArrayList;

/**
 * Created by Delin Sun on 2/21/2018.
 */

public class User {

    public String username;
    public String email;
    private ArrayList<String> followers;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public void add_follower(String username){
        followers.add(username);
    }
}
