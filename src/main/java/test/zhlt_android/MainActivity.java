package test.zhlt_android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    private Uri xashUri = null;
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

        final Button xashButton = (Button)findViewById(R.id.xashButton);
        xashButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                i.addCategory(Intent.CATEGORY_DEFAULT);
                startActivityForResult(Intent.createChooser(i, "Choose Xash3D \"valve\" folder"), 9999);
            }
        });

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

                if (xashUri != null) {
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
                        String localBspPath = getFilesDir().getPath() + File.separator + mapName + ".bsp";
                        FileInputStream localBspStream = new FileInputStream(new File(localBspPath));
                        Log.d(TAG, String.format("Local BSP: %s", localBspPath));

                        String bspPath = getExternalFilesDir(null).getPath() + File.separator + mapName + ".bsp";
                        FileUtils.createFileFromInputStream(localBspStream, bspPath);

                        // Also copy it to the Xash maps folder if possible
                        File xashDir = new File(xashUri.getPath());
                        File dirs[] = xashDir.listFiles();
                        File mapsDir = null;
                        boolean success = false;
                        if (dirs != null) {
                            for (File dir : dirs) {
                                if (dir.getPath().contains("maps")) {
                                    mapsDir = dir;
                                    break;
                                }
                            }
                            if (mapsDir != null) {
                                String xashBspPath = mapsDir.getPath() + File.separator + mapName + ".bsp";
                                if (FileUtils.createFileFromInputStream(localBspStream, xashBspPath) != null) {
                                    tv.setText(bspPath);
                                    success = true;
                                }
                            }
                        }

                        if (!success) {
                            Toast.makeText(getApplicationContext(), "Couldn't copy to Xash folder", Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException e) {
                        tv.setText(e.toString());
                    }
                } else {
                    tv.setText("Set your Xash3D path first!");
                }
            }
        });

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Uri uri = (Uri)bundle.get(Intent.EXTRA_STREAM);
        if (uri != null) {
            handleUri(uri);
        } else {
            Log.d(TAG, String.format("No URI from intent: " + intent.toString()));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri == null) {
                Log.d(TAG, String.format("No URI from intent: " + data.toString()));
                return;
            }
            handleUri(uri);
        } else if (requestCode == 9999) {
            xashUri = data.getData();
            if (xashUri == null) {
                return;
            }
            TextView xashPathView = (TextView)findViewById(R.id.xashPath);
            xashPathView.setText(xashUri.getPath());
        }
    }

    void handleUri(Uri uri) {
        String fileName = "test";//FileUtils.getFileName(getApplicationContext(), uri); FIX ME
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

    public native int hlcsgMain(String filePath);
    public native int hlbspMain(String filePath);
    public native int hlvisMain(String filePath);
    public native int hlradMain(String filePath);
}
