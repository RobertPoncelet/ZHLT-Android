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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

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
                    logString = getStringFromFile(logPath);
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
                        createFileFromInputStream(new FileInputStream(localBsp), bspPath);
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
        // We just received the user-selected map file from the browser
        // Now copy it to the local directory
        if (requestCode == PICK_FILE && resultCode == RESULT_OK) {
            mapUri = data.getData();
            TextView mapPathView = findViewById(R.id.mapPath);
            mapPathView.setText(mapUri.getPath());

            File in = new File(mapUri.getPath());
            String[] strings = in.getName().split(":");
            String fileName = strings[strings.length-1];
            String filePath = getFilesDir().getPath() + File.separator + fileName;

            strings = fileName.split("\\.");
            mapName = strings[strings.length-2];

            try {
                InputStream inStream = getContentResolver().openInputStream(mapUri);
                createFileFromInputStream(inStream, filePath);
                localMapPath = filePath;
            } catch (IOException e) {
                mapPathView.setText(e.toString());
            }
        }
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
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

    public void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }


    private static File createFileFromInputStream(InputStream inputStream, String fileName) {
        try{
            File f = new File(fileName);
            f.setWritable(true, false);
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length;

            while((length=inputStream.read(buffer)) > 0) {
                outputStream.write(buffer,0,length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public native int hlcsgMain(String filePath);
    public native int hlbspMain(String filePath);
    public native int hlvisMain(String filePath);
    public native int hlradMain(String filePath);
}
