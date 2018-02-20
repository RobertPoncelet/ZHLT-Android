package test.zhlt_android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
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
    public static final int PICK_DIR = 2;
    public static final String TAG = "ZHLT-Android";

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
                startActivityForResult(Intent.createChooser(i, "Choose Xash3D folder (\".../in.celest.xash3d.hl/xash\")"), PICK_DIR);
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

        final EditText xashPath = findViewById(R.id.xashPath);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String mapsPath = sharedPref.getString("xashPath", "in.celest.xash3d.hl/xash/valve/maps");
        xashPath.setText(mapsPath);

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {
                String mapsPath = xashPath.getText().toString();

                SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("xashPath", mapsPath);
                editor.apply();
            }
        };

        xashPath.addTextChangedListener(textWatcher);

        final Button compileButton = (Button)findViewById(R.id.compileButton);
        compileButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Compile

                EditText xashDirView = findViewById(R.id.xashPath);
                String mapsDir = xashDirView.getText().toString();
                File maps = new File(mapsDir);

                if (maps.isDirectory()) {
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
                        File localBsp = new File(localBspPath);
                        Log.d(TAG, String.format("Local BSP: %s", localBspPath));

                        String bspPath = getExternalFilesDir(null).getPath() + File.separator + mapName + ".bsp";
                        Log.d(TAG, String.format("BSP: %s", bspPath));
                        FileInputStream localBspStream = new FileInputStream(localBsp);
                        File fudge = FileUtils.createFileFromInputStream(localBspStream, bspPath);
                        if (fudge == null) {
                            Log.d(TAG, String.format("Default BSP copy failed: %s", bspPath));
                        }

                        String xashBspPath = mapsDir + File.separator + mapName + ".bsp";
                        Log.d(TAG, String.format("Xash BSP: %s", xashBspPath));
                        FileInputStream localBspStream2 = new FileInputStream(localBsp);
                        fudge = FileUtils.createFileFromInputStream(localBspStream2, xashBspPath);
                        if (fudge == null) {
                            Toast.makeText(getApplicationContext(), "Couldn't copy to " + xashBspPath, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Compile and copy completed successfully" + xashBspPath, Toast.LENGTH_LONG).show();
                        }

                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), mapsDir + " is an invalid Xash3D path!", Toast.LENGTH_LONG).show();
                }
            }
        });

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        Uri uri = null;
        if (bundle != null) {
            uri = (Uri) bundle.get(Intent.EXTRA_STREAM);
        }
        if (uri != null) {
            handleUri(uri);
        } else {
            Log.d(TAG, "No URI from intent: " + intent.toString());
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
        } else if (requestCode == PICK_DIR && resultCode == RESULT_OK) {
            Uri xashUri = data.getData();
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
        TextView mapPathView = (TextView)findViewById(R.id.mapPath);
        mapPathView.setText(uri.getPath());

        String filePath = getFilesDir().getPath() + File.separator + fileName + ".map";

        mapName = fileName;

        try {
            InputStream inStream = getContentResolver().openInputStream(uri);
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
