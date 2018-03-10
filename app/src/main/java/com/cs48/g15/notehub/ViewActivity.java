package com.cs48.g15.notehub;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cs48.g15.notehub.SwipeListView.OnSwipeListItemClickListener;
import com.cs48.g15.notehub.SwipeListView.SwipeListView;
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
    private ListAdapter adapter;
    static List<String> list;
    private SwipeListView listView;
    private ArrayList<Info> listData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        listData = new ArrayList<>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        list = new ArrayList<String>();
        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        listView = (SwipeListView) findViewById(R.id.listView);
        adapter = new ListAdapter(listData);
//        listView.setAdapter(adapter);
        mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                myUser = snapshot.getValue(User.class);
                myPDF = myUser.pdfs;
                listData.clear();
                list.clear();
                for (Map.Entry<String, PDF> entry : myUser.pdfs.entrySet()) {

                    if (!entry.getValue().filename.equals("no_file.hehe")) {
                        Info info = new Info();
                        info.name = entry.getValue().filename;
                        info.desc = entry.getValue().tag+" - " + entry.getValue().description;
                        listData.add(info);
                        adapter.notifyDataSetChanged();
                        //Toast.makeText(ViewActivity.this, entry.getValue().filename, Toast.LENGTH_SHORT).show();
                        list.add(entry.getValue().filename);
                    }
                }

                adapter = new ListAdapter(listData);
                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                listView.setListener(new OnSwipeListItemClickListener() {
                    @Override
                    public void OnClick(View view, int index) {

                        final String s = adapter.getItem(index).toString();
                        //item点击
                        List<PDF> list = new ArrayList<PDF>();
                        for (Map.Entry<String, PDF> entry : myUser.pdfs.entrySet()) {
                            if (!entry.getValue().filename.equals("no_file.hehe")) {
                                //Toast.makeText(ViewActivity.this, entry.getValue().filename, Toast.LENGTH_SHORT).show();
                                list.add(entry.getValue());
                            }
                        }
                        for (int i = 0; i < list.size(); i++) {

                            if (list.get(i).filename.equals(s)) {
                                download(list.get(i).tag, myUser.username, list.get(i).filename);
                                // Toast.makeText(ViewActivity.this, "test", Toast.LENGTH_SHORT).show();
                                //File file = new File("/sdcard/Download/"+s);
                                File dir = new File("/sdcard/Download");
                                //Toast.makeText(ViewActivity.this, dir.getPath(), Toast.LENGTH_SHORT).show();
                                final File file = new File(dir, s);
                                if (file.exists()) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    Uri apkURI = FileProvider.getUriForFile(
                                            ViewActivity.this,
                                            "com.cs48.g15.notehub.fileProvider", file);
                                    Uri path = Uri.fromFile(file);
                                    intent.setDataAndType(apkURI, "application/pdf");
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    try {
                                        startActivity(intent);
                                    } catch (ActivityNotFoundException e) {
                                        Toast.makeText(ViewActivity.this,
                                                "No Application Available to View PDF",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                                break;
                            }
                        }
                    }

                    @Override
                    public boolean OnLongClick(View view, int index) {
                        //item点击长按
                        AlertDialog.Builder ab = new AlertDialog.Builder(ViewActivity.this);
                        ab.setTitle("LongClick");
                        ab.setMessage("long click item " + index);
                        ab.create().show();
                        return false;
                    }

                    @Override
                    public void OnControlClick(int rid, View view, int index) {
                        final String s = adapter.getItem(index).toString();
                        AlertDialog.Builder ab = new AlertDialog.Builder(ViewActivity.this);
                        switch (rid) {
                            case R.id.modify:
                                //item点击Download

                                List<PDF> list = new ArrayList<PDF>();
                                for (Map.Entry<String, PDF> entry : myUser.pdfs.entrySet()) {
                                    if (!entry.getValue().filename.equals("no_file.hehe")) {
                                        //Toast.makeText(ViewActivity.this, entry.getValue().filename, Toast.LENGTH_SHORT).show();
                                        list.add(entry.getValue());
                                    }
                                }
                                for (int i = 0; i < list.size(); i++) {
                                    // Toast.makeText(ViewActivity.this, "test", Toast.LENGTH_SHORT).show();
                                    if (list.get(i).filename.equals(s)) {
                                        download(list.get(i).tag, myUser.username, list.get(i).filename);
                                        break;
                                    }
                                }
                                break;
                            case R.id.delete:
                                //item点击delete
                                list = new ArrayList<PDF>();
                                for (Map.Entry<String, PDF> entry : myUser.pdfs.entrySet()) {
                                    if (!entry.getValue().filename.equals("no_file.hehe")) {
                                        //Toast.makeText(ViewActivity.this, entry.getValue().filename, Toast.LENGTH_SHORT).show();
                                        list.add(entry.getValue());
                                    }
                                }
                                for (int i = 0; i < list.size(); i++) {
                                    if (list.get(i).filename.equals(s)) {
                                        delete_file(myUser.username, list.get(i).filename, list.get(i).tag);
                                        adapter.remove(index);
                                        adapter.notifyDataSetChanged();
                                        listView.invalidateViews();
                                        break;
                                    }
                                }
                                break;
                        }
                        adapter.notifyDataSetChanged();
                    }
                }, new int[]{R.id.modify, R.id.delete});
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("The read failed: ", databaseError.getMessage());
            }

        });

    }
    //Firebase Methods

    //Firebase Download
    public void download(String tag, String username, final String filename){
        final String file_name = username + "_" + filename;
//        Toast.makeText(ViewActivity.this, file_name, Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ViewActivity.this, "failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    boolean temp1 = file.createNewFile();
                    if (!temp1){
                        //Toast.makeText(ViewActivity.this, "file already exists", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ViewActivity.this, "File Not Found", Toast.LENGTH_SHORT).show();
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
    //firebase delete
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
                myUser = dataSnapshot.getValue(User.class);
                myUser.pdfs.remove(file_name);
                Map<String, Object> delete_pdf = new HashMap<>();
                for (Map.Entry<String, PDF> entry : myUser.pdfs.entrySet()){
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
    @Override
    protected void onResume() {
        super.onResume();
    }
    class Info{
        public String name="";
        public String desc="";
    }
    class ViewHolder{
        public TextView name;
        public TextView desc;
        public Button modify;
        public Button delete;
    }
    class ListAdapter extends com.cs48.g15.notehub.SwipeListView.SwipeListAdapter {
        private ArrayList<Info> listData;
        public ListAdapter(ArrayList<Info> listData){
            this.listData= (ArrayList<Info>) listData.clone();
        }
        @Override
        public int getCount() {
            return listData.size();
        }

        @Override
        public Object getItem(int position) {
            return listData.get(position).name;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void remove(int position){
            listData.remove(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = new ViewHolder();
            if(convertView == null){
                convertView = View.inflate(getBaseContext(),R.layout.swipe_menu,null);
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.desc = (TextView) convertView.findViewById(R.id.desc);
                viewHolder.modify = (Button) convertView.findViewById(R.id.modify);
                viewHolder.delete = (Button) convertView.findViewById(R.id.delete);
                convertView.setTag(viewHolder);
            }
            else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.name.setText(listData.get(position).name);
            viewHolder.desc.setText(listData.get(position).desc);
            return super.bindView(position, convertView);
        }
    }
}
