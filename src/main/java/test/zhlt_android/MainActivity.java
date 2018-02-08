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
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import test.zhlt_android.FileUtils;

public class MainActivity extends Activity {

    public static final int PICK_FILE = 1;
    public static final String TAG = "ZHLT-Android";

    private Uri mapUri = null;
    private String localMapPath = "<ERROR>";
    private String mapName = "<ERROR>";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button mapButton = (Button)findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, PICK_FILE);
            }
        });

        final Button compileButton = (Button)findViewById(R.id.compileButton);
        compileButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Compile
                TextView tv = (TextView) findViewById(R.id.sample_text);
                Log.d("ZHLT-Android", "==================== STARTED ====================");

                int code = hlcsgMain(localMapPath);
                if (code == 0) {
                    code = hlbspMain(localMapPath);
                }
                /*if (code == 0) {
                    code = hlvisMain(localMapPath);
                }
                if (code == 0) {
                    hlradMain(localMapPath);
                }*/

                // Log
                TextView log = (TextView)findViewById(R.id.log);
                String logString;
                String logPath = getFilesDir().getPath() + File.separator + mapName + ".log";
                try {
                    logString = FileUtils.getStringFromFile(logPath);
                    log.setText(logString);
                } catch (Exception e) {
                    log.setText("Could not open log file " + logPath);
                    e.printStackTrace();
                }

                // Copy resulting BSP
                try {
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        String localBspPath = getFilesDir().getPath() + File.separator + mapName + ".bsp";
                        File localBsp = new File(localBspPath);

                        File storage = getExternalFilesDir(null);
                        String bspPath = storage.getPath() + File.separator + mapName + ".bsp";

                        tv.setText(bspPath);
                        FileUtils.createFileFromInputStream(new FileInputStream(localBsp), bspPath);
                    } else {
                        tv.setText("External storage unavailable");
                    }
                } catch (IOException e) {
                    tv.setText(e.toString());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri == null) {
                return;
            }

            String fileName = "test";//FileUtils.getFileName(getApplicationContext(), uri); fuck
            String mimeType = getContentResolver().getType(uri);
            Log.d(TAG, String.format("Filename: %s, MIME type: %s", fileName, mimeType));
            if (fileName != null && mimeType != null && mimeType.contains("json")) {
                try {

                    String dir;
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        dir = getExternalFilesDir(null).getPath();
                    } else {
                        dir = getFilesDir().getPath();
                    }
                    String filePath = dir + File.separator + fileName + ".map";
                    JsonParser json = new JsonParser();
                    File mapFile = new File(filePath);
                    boolean success = json.parse(getContentResolver().openInputStream(uri), mapFile);
                    if (success) {
                        Toast.makeText(getApplicationContext(), String.format("Written map to %s", filePath), Toast.LENGTH_LONG).show();
                        uri = Uri.fromFile(mapFile);
                    } else {
                        Toast.makeText(getApplicationContext(), String.format("Error parsing JSON: %s", filePath), Toast.LENGTH_LONG).show();
                        return;
                    }
                } catch (IOException e) {
                    Log.d(TAG, String.format("IOException! %s", e.toString()));
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.d(TAG, String.format("Exception! %s", e.toString()));
                    e.printStackTrace();
                }
            }

            Log.d(TAG, "Preparing for map compile");
            // We just received the user-selected map file from the browser
            // Now copy it to the local directory
            mapUri = uri;
            TextView mapPathView = (TextView)findViewById(R.id.mapPath);
            mapPathView.setText(mapUri.getPath());

            String filePath = getFilesDir().getPath() + File.separator + fileName + ".map";

            mapName = fileName;

            try {
                InputStream inStream = getContentResolver().openInputStream(mapUri);
                FileUtils.createFileFromInputStream(inStream, filePath);
                localMapPath = filePath;
            } catch (IOException e) {
                mapPathView.setText(e.toString());
            }
        }
    }

    public native int hlcsgMain(String filePath);
    public native int hlbspMain(String filePath);
    public native int hlvisMain(String filePath);
    public native int hlradMain(String filePath);
}
