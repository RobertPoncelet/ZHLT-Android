package test.zhlt_android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends Activity {

    public static final int PICK_FILE = 1;
    public static final String TAG = "ZHLT-Android";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        Log.d("ZHLT-Android", "==================== STARTED ====================");
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            //File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            tv.setText(hlcsgMain(getFilesDir().getPath() + "/test.map"));
        } else {
            tv.setText("External storage unavailable");
        }

        TextView log = (TextView) findViewById(R.id.log);
        String logString;
        try {
            logString = getStringFromFile(getFilesDir().getPath() + "/test.log");
            log.setText(logString);
        } catch (Exception e) {
            log.setText("Could not find log file " + getFilesDir().getPath() + "/test.log");
            e.printStackTrace();
        }

        final Button button = findViewById(R.id.mapButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, PICK_FILE);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE) {
            if (resultCode == RESULT_OK) {
                // User pick the file
                Uri uri = data.getData();
                File file = new File(uri.getPath());
                String filePath = file.getPath();
                EditText mapPathView = findViewById(R.id.mapPath);
                mapPathView.setText(filePath);
                //Toast.makeText(this, fileContent, Toast.LENGTH_LONG).show();
            } else {
                //Log.i(TAG, data.toString());
            }
        }
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    public native String hlcsgMain(String filePath);
}
