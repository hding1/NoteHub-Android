package com.cs48.g15.notehub;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Delin Sun on 2/21/2018.
 */

public class User {

    public String username;
    public String email;
    public Map<String, Object> followers;
    public Map<String, Object> following;
    public Map<String, Object> pdfs;
    public Map<String, Object> tags;
    public boolean isNew;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, Map<String, Object> followers, Map<String, Object> following, Map<String, Object> pdfs, Map<String, Object> tags, boolean isNew) {
        this.username = username;
        this.email = email;
        this.followers = new HashMap<String, Object>(followers);
        this.following = new HashMap<String, Object>(following);
        this.pdfs = new HashMap<String, Object>(pdfs);
        this.tags = new HashMap<String, Object>(tags);
        this.isNew = isNew;
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.followers = new HashMap<>();
        this.following = new HashMap<>();
        this.pdfs = new HashMap<>();
        this.tags = new HashMap<>();
        this.tags.put("initial_tag", "intial_tag");
        this.isNew = false;
    }
}


