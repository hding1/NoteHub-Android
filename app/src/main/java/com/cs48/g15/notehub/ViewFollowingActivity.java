package com.cs48.g15.notehub;

import android.content.Intent;
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

public class ViewFollowingActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private DatabaseReference mUserReference;
    private User myUser;
    private Map<String, String> myFollowing;
    public ArrayAdapter adapter;
    static List<String> list;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_following);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        list = new ArrayList<String>();
        listView = (ListView) findViewById(R.id.list);
        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());

        mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listView = (ListView) findViewById(R.id.list);
                myUser = snapshot.getValue(User.class);
                myFollowing = myUser.following;
                list.clear();
                for (Object test : myFollowing.values()){
                    list.add(test.toString());
                }
                final Map<String, String> myNewHashMap = new HashMap<>();
                for(Map.Entry<String, String> entry : myFollowing.entrySet()){
                    myNewHashMap.put(entry.getValue(), entry.getKey());
                }

                adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, list);
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
                                myFollowing = myUser.following;
//                                List<PDF> list = new ArrayList<PDF>(my.values());

                                String myUid= myNewHashMap.get(s);
                                Intent intent = new Intent(ViewFollowingActivity.this, ViewActivity.class);
                                intent.putExtra("uid",myUid);
                                startActivity(intent);
                                //                       adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("The read failed: ", databaseError.getMessage());
                            }
                        });
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("The read failed: ", databaseError.getMessage());
            }
        });


    }
    public void setList(Map<String,Object> myFiles){
        for (Object test : myFiles.values()){
            list.add(test.toString());
        }
    }
}
