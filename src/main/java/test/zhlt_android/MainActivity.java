package test.zhlt_android;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends Activity {

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
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String hlcsgMain(String filePath);
}
