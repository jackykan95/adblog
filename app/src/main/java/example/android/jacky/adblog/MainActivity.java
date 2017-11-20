package example.android.jacky.adblog;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.tananaev.adblib.AdbBase64;
import com.tananaev.adblib.AdbConnection;
import com.tananaev.adblib.AdbCrypto;
import com.tananaev.adblib.AdbStream;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

public class MainActivity extends Activity {

    private KeyPair keyPair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        retrieveADBLogs();
    }

    public void retrieveADBLogs(){
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();

            new readAdbTask().execute();
        }catch (GeneralSecurityException ex){}
    }

    public void readADBLogs (KeyPair keyPair) {
        AdbConnection connection = null;

        try {

            Socket socket = new Socket("localhost", 5555);

            AdbCrypto crypto = AdbCrypto.loadAdbKeyPair(new AdbBase64() {
                @Override
                public String encodeToString(byte[] data) {
                    return Base64.encodeToString(data, Base64.DEFAULT);
                }
            }, keyPair);

            connection = AdbConnection.create(socket, crypto);

            connection.connect();

            /** Getting Cache Directory */
            File cDir = new File(getApplicationContext().getFilesDir(),"logcat.log");

            if(!cDir.exists()){
                cDir.createNewFile();
            }

            /** Getting a reference to temporary file, if created earlier */
            File tempFile = new File(cDir.getPath() + "/" + "temp.txt") ;

            AdbStream stream = connection.open("shell:logcat -v time -d > " + cDir.getAbsolutePath());

//            FileReader fr=new FileReader(tempFile);
//            BufferedReader br=new BufferedReader(fr);
//
//            String line = "";
//            StringBuilder logLine = new StringBuilder();
//
//            while((line = br.readLine()) != null) {
//
//                if(!line.isEmpty()){
//                    logLine.append(line.toString()+"\n");
//                }
//
//            }
//
//            File appDir = new File(getApplicationContext().getFilesDir().getAbsolutePath()+"/adb.log");
//
//            FileWriter wr = new FileWriter(appDir);
//            BufferedWriter bw = new BufferedWriter(wr);
//
//            bw.write(logLine.toString());


//            while (!stream.isClosed()){
//
//                for (String line : new String(stream.read()).split("\\r?\\n")) {
//                    if (!line.isEmpty()) {
//                        Log.d("ADB Log", line.toString());
//                    }
//                }
//            }

        } catch (InterruptedException e) {
            try {
                connection.close();
            } catch (IOException ee) {
                Log.w("Test", ee);
            }
        } catch (IOException e) {
            Log.w("Test", e);
        }

    }

    public class readAdbTask extends AsyncTask <Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            readADBLogs(keyPair);
            return null;
        }
    }
}
