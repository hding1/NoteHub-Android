package com.cs48.g15.notehub;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Delin Sun on 2/21/2018.
 */

public class User extends Abstract_User{

    public String username;
    public String email;
    public Map<String, Object> followers;
    public Map<String, String> following;
    public Map<String, PDF> pdfs;
    public Map<String, Object> tags;
    public boolean isNew;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, Map<String, Object> followers, Map<String, String> following, Map<String, PDF> pdfs, Map<String, Object> tags, boolean isNew) {
        this.username = username;
        this.email = email;
        this.followers = new HashMap<String, Object>(followers);
        this.following = new HashMap<String, String>(following);
        this.pdfs = new HashMap<String, PDF>(pdfs);
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

    public String getInfo(){
        String rtn_info = "username: ";
        rtn_info += this.username + "\n";
        rtn_info += "email: "+this.email + "\n";
        for (Map.Entry<String, Object> entry : this.followers.entrySet()){
            rtn_info += "follower:"+entry.getKey()+"\n";
        }
        for (Map.Entry<String, String> entry : this.following.entrySet()){
            rtn_info += "following: "+entry.getKey()+"\n";
        }
        for (Map.Entry<String, PDF> entry : this.pdfs.entrySet()){
            rtn_info += "PDF: "+entry.getKey()+"\n";
        }
        for (Map.Entry<String, Object> entry : this.tags.entrySet()){
            rtn_info += "tags: "+entry.getKey()+"\n";
        }
        return rtn_info;
    }
    public Map<String,PDF> getPDFS(){
        return pdfs;
    }

}


