package com.cs48.g15.notehub;

import com.cs48.g15.notehub.PDF;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Map;

/**
 * Created by Delin Sun on 3/10/2018.
 */

public abstract class Abstract_User {
    public String username;
    public String email;
    public Map<String, Object> followers;
    public Map<String, Object> following;
    public Map<String, PDF> pdfs;
    public Map<String, Object> tags;
    public boolean isNew;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    public abstract String getInfo();
}
