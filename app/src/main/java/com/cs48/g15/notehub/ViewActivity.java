package com.cs48.g15.notehub;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;

public class ViewActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private DatabaseReference mUserReference;
    private User myUser;
    private Map<String, PDF> myPDF;
    public  ArrayAdapter adapter;
    static List<String> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        list = new ArrayList<String>();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        ListView listView = (ListView) findViewById(R.id.list);
        mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                myUser = snapshot.getValue(User.class);
                myPDF = myUser.pdfs;
                list.clear();
                setList(myPDF);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("The read failed: " ,databaseError.getMessage());
            }
        });

        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,list);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                final String s = adapterView.getItemAtPosition(i).toString();

//                adapter.dismiss(); // If you want to close the adapter
                mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        myUser = snapshot.getValue(User.class);
                        myPDF = myUser.pdfs;
                        List<PDF> list = new ArrayList<PDF>(myPDF.values());

                        for(int i = 0;i<list.size();i++){
                            if(list.get(i).filename.equals(s)){
                                Toast.makeText(getApplicationContext(), "found something", Toast.LENGTH_LONG).show();
                                download(list.get(i).tag,myUser.username,list.get(i).filename);
                                break;
                            }
                        }
 //                       adapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("The read failed: " ,databaseError.getMessage());
                    }
                });
            }
        });
    }
    public void setList(Map<String,PDF> myFiles){
        for (PDF test : myFiles.values()){
            list.add(test.filename);
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
                File dir = new File("/sdcard/Download");
                //Toast.makeText(ViewActivity.this, dir.getPath(), Toast.LENGTH_SHORT).show();
                final File file = new File(dir, filename);
                String path= file.getPath();

                try {
                    if (!dir.exists()) {
                        boolean temp=dir.mkdirs();
                        if (!temp){
                            Toast.makeText(ViewActivity.this, "failed wtfffffffff", Toast.LENGTH_SHORT).show();
                        }
                    }
                    boolean temp1 = file.createNewFile();
                    Toast.makeText(ViewActivity.this, file.getPath(), Toast.LENGTH_SHORT).show();
                    if (!temp1){
                        Toast.makeText(ViewActivity.this, "11111failed wtfffffffff", Toast.LENGTH_SHORT).show();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    FileOutputStream fos=new FileOutputStream(path);
                    fos.write(bytes);
                    fos.close();
                    Toast.makeText(ViewActivity.this, "Success!!!", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(ViewActivity.this, "rilegou", Toast.LENGTH_SHORT).show();
                    //Toast.makeText(ViewActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ViewActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Toast.makeText(ViewActivity.this, "failed!!!!!!!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
