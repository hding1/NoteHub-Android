package com.cs48.g15.notehub;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.cs48.g15.notehub.FIleChooser;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private Button btnUploadFile, btnViewFile, btnFollowers,  btnRemoveUser, remove, signOut;

//    private EditText ;
    private String myUsername;
    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private DatabaseReference mUserReference;
    private Uri myuri;
    private String myPath;
    private static final int REQUEST_CODE = 6384;

    public void update_file(final String uid, final String filename, final String tag){
        String file_name = filename.replace('.', '_');
        Map<String, Object> childUpdate = new HashMap<>();
        childUpdate.put("/users/" + uid + "/pdfs/" + file_name, tag);
        mDatabase.updateChildren(childUpdate);
        mUserReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                myUsername = user.username;
                if (user.tags.get(tag)==null){
                    Map<String, Object> updateTag = new HashMap<>();
                    updateTag.put("/users/" + uid + "/tags/" + tag, tag);
                    mDatabase.updateChildren(updateTag);
                }

                for (Map.Entry<String, Object> entry : user.followers.entrySet()){
                    mDatabase.child("users").child(entry.getKey()).child("isNew").setValue(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mUserReference.addValueEventListener(postListener);
    }

    public void upload(String username, String file_dir, String tag){
        Uri file = Uri.fromFile(new File(file_dir));
        String filename = username + "_" + file.getLastPathSegment();
        StorageReference storageRef = storage.getReference();
        //StorageReference pdfRed = storageRef.child(filename);
        StorageReference pdfRef = storageRef.child(tag + "/" + filename);

        try {
            InputStream stream = new FileInputStream(FIleChooser.getPath(this,myuri));
            //InputStream stream = new FileInputStream(new File(file_dir));
            UploadTask uploadTask = pdfRef.putStream(stream);
            Toast.makeText(MainActivity.this, "test111",
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
            Toast.makeText(MainActivity.this, "test111111",
                    Toast.LENGTH_LONG).show();
            //handle file not found.
        }
    }

    public void add_follower(String uid, String uid2, final String username){
        Map<String, Object> childUpdate = new HashMap<>();
        childUpdate.put("/users/" + uid + "/followers/" + uid2, username);
        mDatabase.updateChildren(childUpdate);
    }

    public void add_following(String uid, String uid2, final String username){
        Map<String, Object> childUpdate = new HashMap<>();
        childUpdate.put("/users/" + uid + "/following/" + uid2, username);
        mDatabase.updateChildren(childUpdate);
    }

    public void set_isNew(String uid){
        mDatabase.child("users").child(uid).child("isNew").setValue(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
//        Toolbar toolbar = (Toolbar) findViewById(id.toolbar);
//        toolbar.setTitle(getString(R.string.app_name));
//        setSupportActionBar(toolbar);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        btnUploadFile = (Button) findViewById(R.id.upload_file_button);
        btnViewFile = (Button) findViewById(R.id.view_file_button);
        btnFollowers = (Button) findViewById(R.id.followers_button);
        btnRemoveUser = (Button) findViewById(R.id.remove_user_button);
        signOut = (Button) findViewById(R.id.sign_out);
//
//        remove.setVisibility(View.GONE);
//
//        progressBar = (ProgressBar) findViewById(id.progressBar);
//
//        if (progressBar != null) {
//            progressBar.setVisibility(View.GONE);
//        }
//孙德林写这里
        btnUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooser();

            }
        });
//
//        btnViewFile.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
//
//        btnFollowers.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            }
//        });


        btnRemoveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                if (user != null) {
                    user.delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(MainActivity.this, "Your profile is deleted:( Create a account now!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(MainActivity.this, SignupActivity.class));
                                        finish();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(MainActivity.this, "Failed to delete your account!", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

    }
    private void showChooser() {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, "Select a file");
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                // If the file selection was successful
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        Log.i(TAG, "Uri = " + uri.toString());
//                        Toast.makeText(MainActivity.this, "test",
//                                Toast.LENGTH_LONG).show();
                        try {
                            // Get the file path from the URI
                            //你要的路径
                            if(uri==null) {Toast.makeText(MainActivity.this, "test1",
                                    Toast.LENGTH_LONG).show();}
                            final String path = FileUtils.getPath(this, uri);
                            myuri = uri;
                            myPath = path;
                            upload(myUsername,path,"greenhat");
                            Toast.makeText(MainActivity.this,
                                    "File Selected: " + path, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "test2",
                                    Toast.LENGTH_LONG).show();
                            Log.e("FileSelectorActivity", "File select error", e);
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //sign out method
    public void signOut() {
        auth.signOut();

    }


//    @Override
//    protected void onResume() {
//        super.onResume();
//        progressBar.setVisibility(View.GONE);
//    }
//
    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
}
