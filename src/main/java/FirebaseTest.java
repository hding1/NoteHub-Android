import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FirebaseTest {
    DatabaseReference mDatabase;
    DatabaseReference mUserReference;
    DatabaseReference mUserReference2;

    public FirebaseTest(){
        String path = null;
        //path = Resources.getResource("ServiceAccountKey.json").getPath();
        path = test.class.getClassLoader().getResource("ServiceAccountKey.json").getPath();
        FileInputStream serviceAccount =
                null;
        try {
            serviceAccount = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        FirebaseOptions options = null;
        try {
            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://notehub-cs48.firebaseio.com")
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FirebaseApp.initializeApp(options);
    }

    //Firebase function
    public void add_follower_helper(String uid, String uid2, final String username){
        Map<String, Object> childUpdate = new HashMap<>();
        childUpdate.put("/users/" + uid + "/followers/" + uid2, username);
        mDatabase.updateChildrenAsync(childUpdate);
    }

    public void add_follower(String username1, final String username2){
        mUserReference = FirebaseDatabase.getInstance().getReference("username/" + username1);
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                final String uid = dataSnapshot.getValue(String.class);
                mUserReference2 = FirebaseDatabase.getInstance().getReference("username/" + username2);
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
                // ...
            }
        };
        mUserReference.addListenerForSingleValueEvent(postListener);
    }
}
