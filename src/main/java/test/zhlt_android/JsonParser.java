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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import test.zhlt_android.FileUtils;
import test.zhlt_android.MapFile;

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
            world.kvs.put("wad", "/storage/9C33-6BBD/Android/data/test.zhlt_android/files/halflife.wad");

            JSONArray qubes = json.getJSONArray("qubes");
            for (int i = 0; i < qubes.length(); ++i) {
                ArrayList<Brush> brushes = parseQube(qubes.getJSONObject(i));
                world.brushes.addAll(brushes);
            }

            map.ents.add(world);

            Entity player = new Entity("info_player_start");
            player.kvs.put("spawnflags", "0");
            JSONObject cam = json.getJSONObject("camera");
            float x = (float)cam.getDouble("focus_x");
            float y = -(float)cam.getDouble("focus_z");
            float z = (float)cam.getDouble("focus_y");
            player.kvs.put("origin", String.format("%f %f %f", x, y, z));

            map.ents.add(player);

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
        // Qubism uses Y as the up axis, while HL uses Z
        // Because of this, we need to swap them and invert Y
        float px = (float)qube.getDouble("pos_x");
        float py = -(float)qube.getDouble("pos_z");
        float pz = (float)qube.getDouble("pos_y");
        Vector pos = new Vector(px, py, pz);

        float sx = (float)qube.getDouble("size_x");
        float sy = -(float)qube.getDouble("size_z");
        float sz = (float)qube.getDouble("size_y");
        //Vector halfSize = new Vector(sx, sy, sz).times(0.5f);

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

        // Create 3D array
        Vector verts[][][] = new Vector[2][2][2];
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                for (int k = 0; k < 2; ++k) {
                    float x = sx * (float)i;
                    float y = sy * (float)j;
                    float z = sz * (float)k;

                    Vector v = new Vector(x, y, z);
                    v = v.plus(pos);
                    verts[i][j][k] = v;
                }
            }
        }

        Vector defU = new Vector(1f, 0f, 0f, 0f);
        Vector defV = new Vector(0f, -1f, 0f, 0f);
        List<Face> faces = new ArrayList<Face>();

        Vector bottomVerts[] = new Vector[] { verts[0][1][0], verts[1][0][0], verts[0][0][0] };
        faces.add(new Face(bottomVerts, texName, defU, defV, 0f, 1f, 1f));

        Vector topVerts[] = new Vector[] { verts[1][0][1], verts[0][1][1], verts[0][0][1] };
        faces.add(new Face(topVerts, texName, defU, defV, 0f, 1f, 1f));

        Vector leftVerts[] = new Vector[] { verts[1][0][0], verts[1][1][0], verts[1][0][1] };
        faces.add(new Face(leftVerts, texName, defU, defV, 0f, 1f, 1f));

        Vector rightVerts[] = new Vector[] { verts[0][0][0], verts[0][0][1], verts[0][1][0] };
        faces.add(new Face(rightVerts, texName, defU, defV, 0f, 1f, 1f));

        Vector frontVerts[] = new Vector[] { verts[1][1][0], verts[0][1][0], verts[0][1][1] };
        faces.add(new Face(frontVerts, texName, defU, defV, 0f, 1f, 1f));

        Vector backVerts[] = new Vector[] { verts[0][0][0], verts[1][0][0], verts[1][0][1] };
        faces.add(new Face(backVerts, texName, defU, defV, 0f, 1f, 1f));

        return new Brush(faces);
    }

    JSONObject brushAsset(int shape) {
        String fileName = "shapes/qube" + String.valueOf(shape) + ".json";
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
