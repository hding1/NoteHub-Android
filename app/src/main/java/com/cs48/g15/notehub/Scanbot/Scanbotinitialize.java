package com.cs48.g15.notehub.Scanbot;
import android.app.Application;

import net.doo.snap.ScanbotSDKInitializer;

/**
 * Created by Edwardong on 2018/2/26.
 */

public class Scanbotinitialize extends Application {

    @Override
    public void onCreate() {
        new ScanbotSDKInitializer()
                // TODO add your license
                .license(this, "TLJebgOQFb4DQ71/HFp11tqgB+3Tcn" +
                        "9FlpLFc6NDpPjoRmc6pB5sZabfhKbj" +
                        "t1sTJBWZJMOhE6inF9VUTdu7sMIs9t" +
                        "i/aZTjsK6rV1U2hCKhhAlHRAsNaHza" +
                        "ySipQ3sR5E6HsgJDREtfqMn4Fx4Csz" +
                        "dohTwoHET/1hexxwiQuuER0OzUNHpT" +
                        "z7LC5qHHm5fHshvcoLM+uyOoDqzhh7" +
                        "9rcGhntO8bBmUIbom3yV7G/nOcvMqL" +
                        "hgeqZNnnhYrN+3pCHmGppzBGc6kqYV" +
                        "ffktJs59KW6uBZdzxQgM44ecw8dEei" +
                        "XUw0mKHqqcxBCViTQfgbdDRn6JNlA0" +
                        "6CNVmWaw/WaA==\nU2NhbmJvdFNESw" +
                        "pjb20uY3M0OC5nMTUubm90ZWh1Ygox" +
                        "NTIyMTk1MTk5CjU5MAoy\n")
                .initialize(this);
        super.onCreate();
    }

}