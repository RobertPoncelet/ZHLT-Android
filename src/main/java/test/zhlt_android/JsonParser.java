package test.zhlt_android;

/**
 * Created by robert.poncelet on 29/01/18.
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import test.zhlt_android.FileUtils;
import test.zhlt_android.MapFile;

public class JsonParser {
    public void parse(File jsonFile) {
        try {

            String jsonString = FileUtils.getStringFromFile(jsonFile.getPath());
            JSONObject json = new JSONObject(jsonString);
            JSONArray qubes = json.getJSONArray("qubes");

            MapFile map = new MapFile();
            map.ents.add(new MapFile.Entity("worldspawn"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MapFile.Brush parseQube(JSONObject qube) throws JSONException {
        qube.getDouble("pos_x");
    }
}
