package com.cs48.g15.notehub;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class UploadActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Button btnUpload, btnBack;
    private EditText inputName, description;
    private String name, myDescription;
    private String selected;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private DatabaseReference mUserReference;
    private User myUser;
    private String userID;
    private String myUsername,myPath, myPathName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        Intent intent2 = getIntent();
        final String path=intent2.getExtras().getString("path");
        myPath=intent2.getExtras().getString("path");
        final String username=intent2.getExtras().getString("username");
        myUsername = username;
        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        btnUpload = (Button) findViewById(R.id.btn_upload);
        btnBack = (Button) findViewById(R.id.btn_backHome);
        inputName = (EditText) findViewById(R.id.item_name);
        name = inputName.getText().toString();
        description = (EditText) findViewById(R.id.description);
        myDescription = description.getText().toString();
        userID = auth.getCurrentUser().getUid();
        myPathName = path.substring(path.lastIndexOf('/') + 1);;
        // Spinner element
        Spinner spinner = findViewById(R.id.spinner);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        List tags = new ArrayList<>();
        tags.add("Mathematics");
        tags.add("Computer Science");
        tags.add("Art & Music");
        tags.add("Business");
        tags.add("Statistical Science");
        tags.add("World History");
        tags.add("Physics");
        tags.add("Chemistry");
        tags.add("Literature");
        // Creating adapter for spinner
        ArrayAdapter dataAdapter = new ArrayAdapter (this, android.R.layout.simple_spinner_item, tags);

// Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

// attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload(myUsername,myPath,selected);
                update_file_helper(userID,myPathName,selected,myDescription);
                Intent intent = new Intent(UploadActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
        selected = item;
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    public void upload(String username, String file_dir, String tag){
        Uri file = Uri.fromFile(new File(file_dir));
        String filename = username + "_" + file.getLastPathSegment();
        StorageReference storageRef = storage.getReference();
        //StorageReference pdfRed = storageRef.child(filename);
        StorageReference pdfRef = storageRef.child(tag + "/" + filename);

        try {
            InputStream stream = new FileInputStream(FIleChooser.getPath(this,file));
            //InputStream stream = new FileInputStream(new File(file_dir));
            UploadTask uploadTask = pdfRef.putStream(stream);
            Toast.makeText(this, "upload success",
                    Toast.LENGTH_LONG).show();
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                }
            });
        }
        catch (FileNotFoundException e){
            Toast.makeText(this, "upload failed",
                    Toast.LENGTH_LONG).show();
            //handle file not found.
        }
    }
    public void update_file(final String username, final String filename,final String tag, final String description){
        mUserReference = FirebaseDatabase.getInstance().getReference().child("user").child(userID);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                String uid = dataSnapshot.getValue(String.class);
                update_file_helper(userID, inputName.toString(), selected, myDescription);
                //Toast.makeText(MainActivity.this, uid, Toast.LENGTH_SHORT).show();
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mUserReference.addListenerForSingleValueEvent(postListener);
    }
    public void update_file_helper(final String uid, final String filename, final String tag, String description){
        String file_name = filename.replace('.', '_');
        Calendar c = Calendar.getInstance();
        PDF pdf = new PDF(filename, tag, description, c.get(Calendar.YEAR), c.get(Calendar.MONTH));
        Map<String, Object> childUpdate = new HashMap<>();
        childUpdate.put("/users/" + userID + "/pdfs/" + file_name, pdf);
        mDatabase.updateChildren(childUpdate);
        mUserReference = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myUser = dataSnapshot.getValue(User.class);
                if (myUser.tags.get(tag)==null){
                    Map<String, Object> updateTag = new HashMap<>();
                    updateTag.put("/users/" + uid + "/tags/" + tag, tag);
                    mDatabase.updateChildren(updateTag);
                }else {

                    for (Map.Entry<String, Object> entry : myUser.followers.entrySet()) {
                        mDatabase.child("users").child(entry.getKey()).child("isNew").setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                // Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mUserReference.addListenerForSingleValueEvent(postListener);
    }
}

