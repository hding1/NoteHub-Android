package com.cs48.g15.notehub;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cs48.g15.notehub.Scanbot.CameraActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ipaulpro.afilechooser.utils.FileUtils;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private Button btnUploadFile, btnViewFile, btnScanbot,  btnRemoveUser, remove, signOut;

//    private EditText ;
    private String myUsername;
//    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private DatabaseReference mUserReference;
    private Uri myuri;
    private String myPath,myUid;
    private User myUser;
    private static final int REQUEST_CODE = 6384;
    private static final int PERMISSIONS_REQUEST_CAMERA = 314;

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
        //get current user
        FirebaseUser user = auth.getCurrentUser();
        myUid = user.getUid();
        if(user!=null)
        mUserReference = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myUser = dataSnapshot.getValue(User.class);
                if(myUser!=null) {
                    myUsername = myUser.username;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                // ...
            }
        };
        mUserReference.addListenerForSingleValueEvent(postListener);



        btnUploadFile = (Button) findViewById(R.id.upload_file_button);
        btnViewFile = (Button) findViewById(R.id.view_file_button);
        btnScanbot = (Button) findViewById(R.id.scanbot_button);
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
//upload class through aFileChooser
        btnUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "choose your file",
                        Toast.LENGTH_LONG).show();
                if(!checkPermissionExtertalStorage()){
                    try {
                        requestPermissionExtertalStorage();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                showChooser();

            }
        });
//
        btnViewFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkPermissionExtertalStorage()){
                    try {
                        requestPermissionExtertalStorage();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Intent intent = new Intent(MainActivity.this, ViewActivity.class);
                intent.putExtra("uid",myUid);
                intent.putExtra("username",myUsername);
                startActivity(intent);
            }
        });
//矛盾体写这里，像我这样加个intent就行
        btnScanbot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        android.Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            android.Manifest.permission.CAMERA)) {
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{android.Manifest.permission.CAMERA},
                                PERMISSIONS_REQUEST_CAMERA);
                        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                        intent.putExtra("username",myUsername);
                        startActivity(intent);
                        onPause();
                    }
                } else {
                    Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                    startActivity(intent);
                    onPause();
                }
                //startActivity(new Intent(getApplicationContext(), ScanbotActivity.class));
            }
        });


        btnRemoveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ViewFollowingActivity.class));
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
        target.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, "Select a file");
        try {
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
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

                            Intent intent = new Intent(this, UploadActivity.class);
                            intent.putExtra("path",path);
                            intent.putExtra("username",myUsername);
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, e.toString(),
                                    Toast.LENGTH_LONG).show();
                            Log.e("FileSelectorActivity", "File select error", e);
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //check permissions
    public boolean checkPermissionExtertalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = getApplicationContext().checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);
            int result1 = getApplicationContext().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED || result1 == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    //permission dialog
    public void requestPermissionExtertalStorage() throws Exception {
        try {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE);
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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
