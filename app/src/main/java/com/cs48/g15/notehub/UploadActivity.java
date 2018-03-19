package com.cs48.g15.notehub;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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

import static android.content.ContentValues.TAG;

public class UploadActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Button btnUpload, btnBack;
    private EditText inputName, getDescription;
    private String name, myDescription;
    private String selected;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private DatabaseReference mUserReference, mUserReference1;
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

        getDescription = (EditText) findViewById(R.id.get_description);
        myDescription = "  ";
        mUserReference = FirebaseDatabase.getInstance().getReference().child("users").child(auth.getUid());

        userID = auth.getCurrentUser().getUid();
        Toast.makeText(getApplicationContext(),path,Toast.LENGTH_LONG).show();
//        myPathName = path.substring(path.lastIndexOf('/') + 1);

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
//        setSupportActionBar(toolbar);Toast.makeText(this, myUsername,Toast.LENGTH_LONG).show();
//        btnUpload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                myPathName = path.substring(path.lastIndexOf('/') + 1);
//                name = inputName.getText().toString();
//                myDescription = getDescription.getText().toString();
//                upload(myUsername,myPath,selected);
//                update_file_helper(userID,myPathName,selected,myDescription);
//                Intent intent = new Intent(UploadActivity.this, MainActivity.class);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//
//                startActivity(intent);
//                finish();
//            }
//        });
//        btnBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
        mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                myUsername = user.username;
                btnUpload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myPathName = inputName.getText().toString();
                        String extension = "";
                        String old_extension = "";
                        int pos = myPath.lastIndexOf('.');
                        int i = myPathName.lastIndexOf('.');
                        old_extension = myPath.substring(pos);
                        int p = Math.max(myPathName.lastIndexOf('/'), myPathName.lastIndexOf('\\'));

                        if (i > p) {
                            extension = myPathName.substring(i+1);
                        }else{
                            myPathName = myPathName + ".pdf";
                            extension = "pdf";
                        }
                        if(old_extension.equals("pdf")) {
                            name = inputName.getText().toString();
                            myDescription = getDescription.getText().toString();
                            upload(myUsername, myPath, selected, myPathName);
                            update_file_helper(userID, myPathName, selected, myDescription);
                        }else{
                            Toast.makeText(UploadActivity.this, "File format is not supported, Upload pdf file only",Toast.LENGTH_LONG).show();
                        }
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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
    public void upload(String username, String file_dir, String tag, String file_name){
        Uri file = Uri.fromFile(new File(file_dir));
        String filename = username + "_" + file_name;
        StorageReference storageRef = storage.getReference();
        //StorageReference pdfRed = storageRef.child(filename);
        StorageReference pdfRef = storageRef.child(tag + "/" + filename);
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
        }

        try {
            InputStream stream = new FileInputStream(FIleChooser.getPath(this,file));
            //InputStream stream = new FileInputStream(new File(file_dir));
            UploadTask uploadTask = pdfRef.putStream(stream);
            //Toast.makeText(this, "upload success",Toast.LENGTH_LONG).show();
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
    public void update_file_helper(final String uid, final String filename, final String tag, final String description){
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
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final String file_name = filename.replace('.', '_');

        mUserReference1 = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

        ValueEventListener postListener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                User user = dataSnapshot.getValue(User.class);
                String username = user.username;
                final String file_name1 = username + "_" + filename;
                StorageReference storageRef = storage.getReference();
                StorageReference fileRef = storageRef.child(tag + "/" + file_name1);
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Got the download URL for 'users/me/profile.png'
                        String temp = uri.toString();
                        Calendar c = Calendar.getInstance();
                        //Toast.makeText(UploadActivity.this, temp, Toast.LENGTH_SHORT).show();
                        PDF pdf = new PDF(filename, tag, description, temp, c.get(Calendar.YEAR), c.get(Calendar.MONTH)+1, c.get(Calendar.DAY_OF_MONTH));
                        Map<String, Object> childUpdate = new HashMap<>();
                        childUpdate.put("/users/" + userID + "/pdfs/" + file_name, pdf);
                        mDatabase.updateChildren(childUpdate);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mUserReference1.addListenerForSingleValueEvent(postListener1);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }
}

