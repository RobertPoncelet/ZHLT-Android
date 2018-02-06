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

        final Button mapButton = findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, PICK_FILE);
            }
        });

        final Button compileButton = findViewById(R.id.compileButton);
        compileButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Compile
                TextView tv = (TextView) findViewById(R.id.sample_text);
                Log.d("ZHLT-Android", "==================== STARTED ====================");

                int code = hlcsgMain(localMapPath);
                if (code == 0) {
                    code = hlbspMain(localMapPath);
                }
                if (code == 0) {
                    code = hlvisMain(localMapPath);
                }
                if (code == 0) {
                    hlradMain(localMapPath);
                }

                // Log
                TextView log = findViewById(R.id.log);
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

            String uriPath = uri.getPath();
            String[] strings = uriPath.split("\\.");
            switch (strings[strings.length-1]) {
                case "json":
                    try {
                        Log.d(TAG, "Starting JSON parse");
                        strings = uriPath.split(":");
                        String fileName = strings[strings.length - 1];
                        strings = fileName.split("\\.");
                        fileName = strings[strings.length - 2];
                        String dir;
                        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                            dir = getExternalFilesDir(null).getPath();
                        } else {
                            dir = getFilesDir().getPath();
                        }
                        String filePath = dir + File.separator + fileName + ".map";
                        JsonParser json = new JsonParser();
                        boolean success = json.parse(getContentResolver().openInputStream(uri), new File(filePath));
                        if (success) {
                            Toast.makeText(getApplicationContext(), String.format("Written map to %s", filePath), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), String.format("Error parsing JSON", filePath), Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException e) {
                        Log.d(TAG, String.format("IOException! %s", e.toString()));
                        e.printStackTrace();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        Log.d(TAG, String.format("ArrayIndexOutOfBoundsException! %s", e.toString()));
                        e.printStackTrace();
                    }
                    break;

                case "map":
                    Log.d(TAG, "Starting map compile");
                    // We just received the user-selected map file from the browser
                    // Now copy it to the local directory
                    mapUri = uri;
                    TextView mapPathView = findViewById(R.id.mapPath);
                    mapPathView.setText(mapUri.getPath());

                    File in = new File(mapUri.getPath());
                    strings = in.getName().split(":");
                    String fileName = strings[strings.length - 1];
                    String filePath = getFilesDir().getPath() + File.separator + fileName;

                    strings = fileName.split("\\.");
                    mapName = strings[strings.length - 2];

                    try {
                        InputStream inStream = getContentResolver().openInputStream(mapUri);
                        FileUtils.createFileFromInputStream(inStream, filePath);
                        localMapPath = filePath;
                    } catch (IOException e) {
                        mapPathView.setText(e.toString());
                    }
                    break;

                default:
                    Log.d(TAG, "No result");
                    break;
            }
        }
    }

    public native int hlcsgMain(String filePath);
    public native int hlbspMain(String filePath);
    public native int hlvisMain(String filePath);
    public native int hlradMain(String filePath);
}
