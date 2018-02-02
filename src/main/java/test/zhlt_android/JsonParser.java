package test.zhlt_android;

/**
 * Created by robert.poncelet on 29/01/18.
 */

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

import test.zhlt_android.FileUtils;
import test.zhlt_android.MapFile;

public class JsonParser {
    public static final String TAG = "ZHLT-Android";

    public void parse(InputStream jsonStream, File outFile) {
        try {

            String jsonString = FileUtils.convertStreamToString(jsonStream);
            JSONObject json = new JSONObject(jsonString);

            MapFile map = new MapFile();
            Entity world = new Entity("worldspawn");
            world.kvs.put("spawnflags", "0");
            world.kvs.put("wad", "/data/user/0/test.zhlt_android/files/Q.wad");

            JSONArray qubes = json.getJSONArray("qubes");
            for (int i = 0; i < qubes.length(); ++i) {
                Brush qube = parseQube(qubes.getJSONObject(i));
                world.brushes.add(qube);
            }

            map.ents.add(world);

            Entity player = new Entity("info_player_start");
            player.kvs.put("spawnflags", "0");
            player.kvs.put("origin", "0 0 0");

            map.ents.add(player);

            PrintStream p = new PrintStream(new FileOutputStream(outFile));
            Log.d(TAG, "Starting map write");
            map.write(p);
            p.close();

        } catch (JSONException e) {
            Log.d(TAG, String.format("JSON Exception! %s", e.toString()));
            e.printStackTrace();
        } catch (Exception e) {
            Log.d(TAG, String.format("Exception! %s", e.toString()));
            e.printStackTrace();
        }
    }

    private Brush parseQube(JSONObject qube) throws JSONException {
        float px = (float)qube.getDouble("pos_x");
        float py = (float)qube.getDouble("pos_y");
        float pz = (float)qube.getDouble("pos_z");
        Vector pos = new Vector(px, py, pz);

        float sx = (float)qube.getDouble("size_x");
        float sy = (float)qube.getDouble("size_y");
        float sz = (float)qube.getDouble("size_z");
        //Vector halfSize = new Vector(sx, sy, sz).times(0.5f);

        // Create 3D array
        Vector verts[][][] = new Vector[2][2][2];
        for (int i = 0; i < 2; ++i) {
            for (int j = 0; j < 2; ++j) {
                for (int k = 0; k < 2; ++k) {
                    float x = sx * 0.5f * (float)(i*2-1);
                    float y = sy * 0.5f * (float)(j*2-1);
                    float z = sz * 0.5f * (float)(k*2-1);

                    Vector v = new Vector(x, y, z);
                    v = v.plus(pos);
                    verts[i][j][k] = v;
                }
            }
        }

        Vector defU = new Vector(1f, 0f, 0f, 0f);
        Vector defV = new Vector(0f, -1f, 0f, 0f);
        List<Face> faces = new ArrayList<Face>();

        Vector bottomVerts[] = new Vector[] { verts[0][1][0], verts[0][0][0], verts[1][0][0] };
        faces.add(new Face(bottomVerts, "mmetal1_2", defU, defV, 0f, 1f, 1f));

        Vector topVerts[] = new Vector[] { verts[1][0][1], verts[0][0][1], verts[0][1][1] };
        faces.add(new Face(topVerts, "mmetal1_2", defU, defV, 0f, 1f, 1f));

        Vector leftVerts[] = new Vector[] { verts[0][1][1], verts[0][1][0], verts[1][1][0] };
        faces.add(new Face(leftVerts, "mmetal1_2", defU, defV, 0f, 1f, 1f));

        Vector rightVerts[] = new Vector[] { verts[1][0][0], verts[0][0][0], verts[0][0][1] };
        faces.add(new Face(rightVerts, "mmetal1_2", defU, defV, 0f, 1f, 1f));

        Vector frontVerts[] = new Vector[] { verts[1][1][0], verts[1][0][0], verts[1][0][1] };
        faces.add(new Face(frontVerts, "mmetal1_2", defU, defV, 0f, 1f, 1f));

        Vector backVerts[] = new Vector[] { verts[0][0][1], verts[0][0][0], verts[0][1][0] };
        faces.add(new Face(backVerts, "mmetal1_2", defU, defV, 0f, 1f, 1f));

        return new Brush(faces);
    }
}
