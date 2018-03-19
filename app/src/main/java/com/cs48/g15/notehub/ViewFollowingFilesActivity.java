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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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

public class ViewFollowingFilesActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_view_following_files);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        list = new ArrayList<String>();
        Intent intent2 = getIntent();
        final String myUid=intent2.getExtras().getString("uid");
        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("users").child(myUid);
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
                        //Toast.makeText(ViewFollowingFilesActivity.this, entry.getValue().filename, Toast.LENGTH_SHORT).show();
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
                                //Toast.makeText(ViewFollowingFilesActivity.this, entry.getValue().filename, Toast.LENGTH_SHORT).show();
                                list.add(entry.getValue());
                            }
                        }
                        for (int i = 0; i < list.size(); i++) {

                            if (list.get(i).filename.equals(s)) {
                                download(list.get(i).tag, myUser.username, list.get(i).filename);
                                //Toast.makeText(ViewFollowingFilesActivity.this, "test", Toast.LENGTH_SHORT).show();
                                //File file = new File("/sdcard/Download/"+s);
                                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
                                //Toast.makeText(ViewFollowingFilesActivity.this, dir.getPath(), Toast.LENGTH_SHORT).show();
                                final File file = new File(dir, s);
                                if (file.exists()) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    Uri apkURI = FileProvider.getUriForFile(
                                            ViewFollowingFilesActivity.this,
                                            "com.cs48.g15.notehub.fileProvider", file);
                                    Uri path = Uri.fromFile(file);
                                    intent.setDataAndType(apkURI, "application/pdf");
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    try {
                                        startActivity(intent);
                                    } catch (ActivityNotFoundException e) {
                                        Toast.makeText(ViewFollowingFilesActivity.this,
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
                        final String s = adapter.getItem(index).toString();
                        //item点击
                        List<PDF> list = new ArrayList<PDF>();
                        for (Map.Entry<String, PDF> entry : myUser.pdfs.entrySet()) {
                            if (!entry.getValue().filename.equals("no_file.hehe")) {
                                //Toast.makeText(ViewFollowingFilesActivity.this, entry.getValue().filename, Toast.LENGTH_SHORT).show();
                                list.add(entry.getValue());
                            }
                        }
                        for (int i = 0; i < list.size(); i++) {

                            if (list.get(i).filename.equals(s)) {
                                download(list.get(i).tag, myUser.username, list.get(i).filename);
                                //Toast.makeText(ViewFollowingFilesActivity.this, "test", Toast.LENGTH_SHORT).show();
                                //File file = new File("/sdcard/Download/"+s);
                                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
                                //Toast.makeText(ViewFollowingFilesActivity.this, dir.getPath(), Toast.LENGTH_SHORT).show();
                                final File file = new File(dir, s);
                                if (file.exists()) {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    Uri apkURI = FileProvider.getUriForFile(
                                            ViewFollowingFilesActivity.this,
                                            "com.cs48.g15.notehub.fileProvider", file);
                                    Uri path = Uri.fromFile(file);
                                    intent.setDataAndType(apkURI, "application/pdf");
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    try {
                                        startActivity(intent);
                                    } catch (ActivityNotFoundException e) {
                                        Toast.makeText(ViewFollowingFilesActivity.this,
                                                "No Application Available to View PDF",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                                break;
                            }

                        }
                        return true;
                    }

                    @Override
                    public void OnControlClick(int rid, View view, int index) {
                        final String s = adapter.getItem(index).toString();
                        AlertDialog.Builder ab = new AlertDialog.Builder(ViewFollowingFilesActivity.this);

                        switch (rid) {
                            case R.id.download:
                                //item点击Download

                                List<PDF> list = new ArrayList<PDF>();
                                for (Map.Entry<String, PDF> entry : myUser.pdfs.entrySet()) {
                                    if (!entry.getValue().filename.equals("no_file.hehe")) {
                                        //Toast.makeText(ViewFollowingFilesActivity.this, entry.getValue().filename, Toast.LENGTH_SHORT).show();
                                        list.add(entry.getValue());
                                    }
                                }
                                for (int i = 0; i < list.size(); i++) {
                                    // Toast.makeText(ViewFollowingFilesActivity.this, "test", Toast.LENGTH_SHORT).show();
                                    if (list.get(i).filename.equals(s)) {
                                        download(list.get(i).tag, myUser.username, list.get(i).filename);
                                        break;
                                    }
                                }
                                break;
                        }
                        adapter.notifyDataSetChanged();
                    }
                }, new int[]{R.id.download});
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
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
        }
        final String file_name = username + "_" + filename;
//        Toast.makeText(ViewFollowingFilesActivity.this, file_name, Toast.LENGTH_SHORT).show();
        StorageReference storageRef = storage.getReference();
        StorageReference fileRef = storageRef.child(tag + "/" + file_name);

        final long ONE_MEGABYTE = 1024 * 1024 * 25;
        fileRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath());
                //Toast.makeText(ViewFollowingFilesActivity.this, dir.getPath(), Toast.LENGTH_SHORT).show();
                final File file = new File(dir, filename);
                String path= file.getPath();

                try {
                    if (!dir.exists()) {
                        boolean temp=dir.mkdirs();
                        if (!temp){
                            Toast.makeText(ViewFollowingFilesActivity.this, "failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    boolean temp1 = file.createNewFile();
                    if (!temp1){
                        //Toast.makeText(ViewFollowingFilesActivity.this, "file already exists", Toast.LENGTH_SHORT).show();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    FileOutputStream fos=new FileOutputStream(path);
                    fos.write(bytes);
                    fos.close();
                    Toast.makeText(ViewFollowingFilesActivity.this, "Success", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(ViewFollowingFilesActivity.this, "File Not Found", Toast.LENGTH_SHORT).show();
                    //Toast.makeText(ViewFollowingFilesActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ViewFollowingFilesActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Toast.makeText(ViewFollowingFilesActivity.this, "failed!!!!!!!!", Toast.LENGTH_SHORT).show();
            }
        });
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
                convertView = View.inflate(getBaseContext(),R.layout.content_view_following_files,null);
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.desc = (TextView) convertView.findViewById(R.id.desc);
                viewHolder.modify = (Button) convertView.findViewById(R.id.download);
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
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }
}
