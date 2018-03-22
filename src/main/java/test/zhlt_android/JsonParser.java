package test.zhlt_android;

/**
 * Created by robert.poncelet on 29/01/18.
 */

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class JsonParser {
    private Context context;
    public static final String TAG = "ZHLT-Android";

    public JsonParser(Context ctx) {
        context = ctx;
    }

    public boolean parse(InputStream jsonStream, File outFile) {
        try {

            String jsonString = FileUtils.convertStreamToString(jsonStream);
            JSONObject json = new JSONObject(jsonString);

            MapFile map = new MapFile();
            Entity world = new Entity("worldspawn");
            world.kvs.put("spawnflags", "0");
            world.kvs.put("wad", "/storage/emulated/0/Android/data/test.zhlt_android/files/Q.wad");

            JSONArray qubes = json.getJSONArray("qubes");
            for (int i = 0; i < qubes.length(); ++i) {
                ArrayList<Brush> brushes = parseQube(qubes.getJSONObject(i));
                if (brushes != null) {
                    world.brushes.addAll(brushes);
                }
            }

            map.ents.add(world);

            Entity player = new Entity("info_player_start");
            player.kvs.put("spawnflags", "0");
            JSONObject cam = json.getJSONObject("camera");
            float x = (float)cam.getDouble("focus_z");
            float y = (float)cam.getDouble("focus_x");
            float z = (float)cam.getDouble("focus_y");
            player.kvs.put("origin", String.format("%f %f %f", x, y, z));

            map.ents.add(player);

            Entity light = new Entity("light_environment");
            light.kvs.put("_light", "255 255 128 200");
            light.kvs.put("pitch", "0");
            light.kvs.put("_falloff", "0");
            light.kvs.put("_fade", "1.0");
            light.kvs.put("angles", "0 0 0");

            map.ents.add(light);

            PrintStream p = new PrintStream(new FileOutputStream(outFile));
            Log.d(TAG, "Starting map write");
            map.write(p);
            p.close();

            return true;
        } catch (JSONException e) {
            Log.d(TAG, String.format("JSON Exception! %s", e.toString()));
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            Log.d(TAG, String.format("Exception! %s", e.toString()));
            e.printStackTrace();
            return false;
        }
    }

    private ArrayList<Brush> parseQube(JSONObject qube) throws JSONException {
        // Qubism uses Y as the up axis, while HL uses Z.
        // Because of this, when converting co-ordinates from
        // Qubism's system to HL's, we reinterpret the axes as follows:
        // X -> Y
        // Y -> Z
        // Z -> X
        float ax = (float)qube.getDouble("pos_z");
        float ay = (float)qube.getDouble("pos_x");
        float az = (float)qube.getDouble("pos_y");
        Vector pos = new Vector(ax, ay, az);

        float bx = (float)qube.getDouble("size_z");
        float by = (float)qube.getDouble("size_x");
        float bz = (float)qube.getDouble("size_y");
        Vector size = new Vector(bx, by, bz);

        String texName;
        switch(qube.getInt("colour")) {
            case 0x282828: // dark grey
                texName = "black";
                break;
            case 0xFFFFFF: // white
                texName = "OUT_WALL1A";
                break;
            case 0xFF0000: // red
                texName = "BASEWALL01A";
                break;
            case 0x00FF00: // green
                texName = "OUT_DIRT1";
                break;
            case 0x0000FF: // blue
                texName = "SKY";
                break;
            default:
                texName = "LAB1_FLOOR1";
                break;
        }

        Vector defU = new Vector(1f, 0f, 0f, 0f);
        Vector defV = new Vector(0f, -1f, 0f, 0f);

        int shape = qube.getInt("shape");
        JSONObject jqube = jsonShape(shape);
        if (jqube == null) {
            return null;
        }
        JSONArray jbrushes = jqube.getJSONArray("brushes");
        ArrayList<Brush> brushes = new ArrayList<>();

        for (int i = 0; i < jbrushes.length(); ++i) {
            JSONObject jbrush = jbrushes.getJSONObject(i);
            JSONArray jfaces = jbrush.getJSONArray("faces");
            List<Face> faces = new ArrayList<Face>();

            for (int j = 0; j < jfaces.length(); ++j) {
                JSONObject jface = jfaces.getJSONObject(j);
                JSONArray jpoints = jface.getJSONArray("points");
                Vector points[] = new Vector[3];

                for (int k = 0; k < 3; ++k) {
                    JSONArray jv = jpoints.getJSONArray(k);
                    // As above, juggle the axes like some horror movie clown
                    // Actually wait don't, I fixed it
                    float tx = (float)jv.getDouble(0);
                    float ty = (float)jv.getDouble(1);
                    float tz = (float)jv.getDouble(2);
                    float x = ax + tx * bx;
                    float y = ay + ty * by;
                    float z = az + tz * bz;
                    points[k] = new Vector(x, y, z);
                }

                faces.add(new Face(points, texName, defU, defV, 0f, 1f, 1f));
            }

            brushes.add(new Brush(faces));
        }

        return brushes;
    }

    JSONObject jsonShape(int shape) {
        String fileName;
        switch (shape) {
            case 271:
                fileName = "shapes/arch_64.json";
                break;
            case 270:
                fileName = "shapes/arch_85.json";
                break;
            case 269:
                fileName = "shapes/arch_128.json";
                break;
            case 259:
                fileName = "shapes/cone.json";
                break;
            case 260:
                fileName = "shapes/cone_quarter.json";
                break;
            case 262:
                fileName = "shapes/cylinder.json";
                break;
            case 263:
                fileName = "shapes/cylinder_quarter.json";
                break;
            case 264:
                fileName = "shapes/cylinder_quarter_inverse.json";
                break;
            default:
                fileName = "shapes/qube" + String.valueOf(shape) + ".json";
        }

        try {
            InputStream stream = context.getAssets().open(fileName);
            String jsonString = FileUtils.convertStreamToString(stream);
            return new JSONObject(jsonString);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            e.printStackTrace();
            return null;
        }
    }
}
