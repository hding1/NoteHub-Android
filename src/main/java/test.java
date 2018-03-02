import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class test {
    //previous variables
    DatabaseReference mUserReference;
    DatabaseReference mUserReference2;

    //new variables
    private static final String UID = "some-uid";
    public static void main(String[] args) throws IOException {
        /*
        FirebaseTest base = new FirebaseTest();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();

        base.add_follower("Official_Account", "delin66668");
        */


        //FireBase initialize
        String path = null;
        //path = Resources.getResource("ServiceAccountKey.json").getPath();
        path = test.class.getClassLoader().getResource("ServiceAccountKey.json").getPath();
        FileInputStream serviceAccount = new FileInputStream(path);



        // Initialize the app with a service account, granting admin privileges
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://notehub-cs48.firebaseio.com")
                .build();
        FirebaseApp.initializeApp(options);

        // As an admin, the app has access to read and write all data, regardless of Security Rules
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("username/Official_Account");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String res = dataSnapshot.getValue(String.class);
                System.out.println(res);
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }
}
