package com.cs48.g15.notehub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
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

import net.doo.snap.ScanbotSDKInitializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private Button btnUploadFile, btnViewFile, btnFollowers,  btnRemoveUser, remove, signOut;

//    private EditText ;
    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private DatabaseReference mUserReference;
    private DatabaseReference mUserReference2;
    private User user;

    public void update_file_helper(final String uid, final String filename, final String tag, String description){
        String file_name = filename.replace('.', '_');
        Calendar c = Calendar.getInstance();
        PDF pdf = new PDF(filename, tag, description, c.get(Calendar.YEAR), c.get(Calendar.MONTH));
        Map<String, Object> childUpdate = new HashMap<>();
        childUpdate.put("/users/" + uid + "/pdfs/" + file_name, pdf);
        mDatabase.updateChildren(childUpdate);
        mUserReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
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
        mUserReference.addListenerForSingleValueEvent(postListener);
    }

    public void upload(String username, String file_dir, String tag){
        Uri file = Uri.fromFile(new File(file_dir));
        String filename = username + "_" + file.getLastPathSegment();
        StorageReference storageRef = storage.getReference();
        //StorageReference pdfRed = storageRef.child(filename);
        StorageReference pdfRef = storageRef.child(tag + "/" + filename);
        try {
            InputStream stream = new FileInputStream(new File(file_dir));
            UploadTask uploadTask = pdfRef.putStream(stream);
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
            //handle file not found.
        }
    }

    //TODO: how to store in memory.
    public void download(String tag, String username, final String filename){
        final String file_name = username + "_" + filename;
        StorageReference storageRef = storage.getReference();
        StorageReference fileRef = storageRef.child(tag + "/" + file_name);

        final long ONE_MEGABYTE = 1024 * 1024;
        fileRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                File dir = new File(Environment.getExternalStorageDirectory() + "/notehub-pdfs");
                final File file = new File(dir, filename);
                String path= file.getPath();
                try {
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    FileOutputStream fos=new FileOutputStream(path);
                    fos.write(bytes);
                    fos.close();
                    Toast.makeText(MainActivity.this, "Success!!!", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Toast.makeText(MainActivity.this, "failed!!!!!!!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void delete_file(final String username, final String filename, final String tag){
        mUserReference = FirebaseDatabase.getInstance().getReference().child("username").child(username);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                String uid = dataSnapshot.getValue(String.class);
                delete_file_helper(uid, username, filename, tag);
                //Toast.makeText(MainActivity.this, uid, Toast.LENGTH_SHORT).show();
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mUserReference.addListenerForSingleValueEvent(postListener);
    }

    public void delete_file_helper(final String uid, String username, String filename, String tag){
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();
        final String file_name = filename.replace('.', '_');;

        // Create a reference to the file to delete
        StorageReference deleteRef = storageRef.child(tag + "/" + username + "_" + filename);

        mUserReference = FirebaseDatabase.getInstance().getReference().child("users").child(uid);

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                user.pdfs.remove(file_name);
                Map<String, Object> delete_pdf = new HashMap<>();
                for (Map.Entry<String, Object> entry : user.pdfs.entrySet()){
                    if (entry.getKey()!=file_name){
                        delete_pdf.put(entry.getKey(), entry.getValue());
                    }
                }
                mDatabase.child("users").child(uid).child("pdfs").setValue(delete_pdf);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mUserReference.addListenerForSingleValueEvent(postListener);
    }

    public void update_file(final String username, final String filename,final String tag, final String description){
        mUserReference = FirebaseDatabase.getInstance().getReference().child("username").child(username);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                String uid = dataSnapshot.getValue(String.class);
                update_file_helper(uid, filename, tag, description);
                //Toast.makeText(MainActivity.this, uid, Toast.LENGTH_SHORT).show();
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mUserReference.addListenerForSingleValueEvent(postListener);
    }

    public void add_follower_helper(String uid, String uid2, final String username){
        Map<String, Object> childUpdate = new HashMap<>();
        childUpdate.put("/users/" + uid + "/followers/" + uid2, username);
        mDatabase.updateChildren(childUpdate);
    }

    public void add_follower(String username1, final String username2){
        mUserReference = FirebaseDatabase.getInstance().getReference().child("username").child(username1);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                final String uid = dataSnapshot.getValue(String.class);
                mUserReference2 = FirebaseDatabase.getInstance().getReference().child("username").child(username2);
                ValueEventListener postListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get Post object and use the values to update the UI
                        String uid2 = dataSnapshot.getValue(String.class);
                        add_follower_helper(uid, uid2, username2);
                        //Toast.makeText(MainActivity.this, uid, Toast.LENGTH_SHORT).show();
                        // ...
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Getting Post failed, log a message
                        Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                        // ...
                    }
                };
                mUserReference2.addListenerForSingleValueEvent(postListener);
                //Toast.makeText(MainActivity.this, uid, Toast.LENGTH_SHORT).show();
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mUserReference.addListenerForSingleValueEvent(postListener);
    }

    public void add_following_helper(String uid, String uid2, final String username){
        Map<String, Object> childUpdate = new HashMap<>();
        childUpdate.put("/users/" + uid + "/following/" + uid2, username);
        mDatabase.updateChildren(childUpdate);
    }

    public void add_following(String username1, final String username2){
        mUserReference = FirebaseDatabase.getInstance().getReference().child("username").child(username1);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                final String uid = dataSnapshot.getValue(String.class);
                mUserReference2 = FirebaseDatabase.getInstance().getReference().child("username").child(username2);
                ValueEventListener postListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get Post object and use the values to update the UI
                        String uid2 = dataSnapshot.getValue(String.class);
                        add_following_helper(uid, uid2, username2);
                        //Toast.makeText(MainActivity.this, uid, Toast.LENGTH_SHORT).show();
                        // ...
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Getting Post failed, log a message
                        Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                        // ...
                    }
                };
                mUserReference2.addListenerForSingleValueEvent(postListener);
                //Toast.makeText(MainActivity.this, uid, Toast.LENGTH_SHORT).show();
                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mUserReference.addListenerForSingleValueEvent(postListener);
    }

    public void set_isNew(String uid){
        mDatabase.child("users").child(uid).child("isNew").setValue(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDatabase = FirebaseDatabase.getInstance().getReference();
//        Toolbar toolbar = (Toolbar) findViewById(id.toolbar);
//        toolbar.setTitle(getString(R.string.app_name));
//        setSupportActionBar(toolbar);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        //add_follower("Official_Account", "Official_Account");
        //add_following("Official_Account", "Official_Account");
        //get_uid_by_username("Official_Account");
        //delete_file("PrF8HN3WQWTLnP4f8kESHpqsgMr2","delin66", "test.pdf", "Bio");
        update_file("Official_Account", "test.pdf", "Bio", "this is just a test file");
        //set_isNew("6o8Ql6AYBEfHfcoSiCH9YpoLSb62");

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
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(intent);
                finish();
                //update_file(uid, filename, tag);
                //upload(username, pathname, tag);
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
