package test.zhlt_android;

import android.util.Log;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by robert.poncelet on 29/01/18.
 */

class MapFile {
    public static final String TAG = "ZHLT-Android";
    public List<Entity> ents;

    public MapFile() {
        ents = new ArrayList<Entity>();
    }

    public void write(PrintStream p) {
        for (Entity ent : ents) {
            Log.d(TAG, String.format("Writing entity %s", ent.kvs.get("classname")));
            ent.out(p);
        }
    }
}

class Entity {
    public Map<String, String> kvs;
    public List<Brush> brushes;

    public Entity(String classname) {
        kvs = new HashMap<String, String>();
        brushes = new ArrayList<Brush>();
        kvs.put("classname", classname);
    }

    public void out(PrintStream p) {
        p.print("{\n");
        for (Map.Entry<String, String> kv : kvs.entrySet())
        {
            p.print("\t\"");
            p.print(kv.getKey());
            p.print("\" \"");
            p.print(kv.getValue());
            p.print("\"\n");
        }
        for (Brush b : brushes)
        {
            b.out(p);
        }
        p.print("}\n");
    }
}

class Face {
    public Vector points[];
    public String texName;
    public Vector uAxis;
    public Vector vAxis;
    public float rotation;
    public float scaleX;
    public float scaleY;

    public Face(Vector inPoints[], String inTexName, Vector inUAxis, Vector inVAxis,
                float inRot, float inScaleX, float inScaleY) {
        points = inPoints;
        texName = inTexName;
        uAxis = inUAxis;
        vAxis = inVAxis;
        rotation = inRot;
        scaleX = inScaleX;
        scaleY = inScaleY;
    }

    public void out(PrintStream p) {
        p.print("\t\t\t");
        for (Vector v : points) {
            p.print("( ");
            v.out(p);
            p.print(" ) ");
        }
        p.print(texName);
        /*p.print(" [ ");
        uAxis.out(p);
        p.print(" ] [ ");
        vAxis.out(p);
        p.print(" ] ");
        p.print(String.valueOf((int)rotation));
        p.print(" ");
        p.print(String.valueOf((int)scaleX));
        p.print(" ");
        p.print(String.valueOf((int)scaleY));*/
        p.print(" 0 0 0 1 1\n"); // ????
    }
}

class Brush {
    public List<Face> faces;

    public Brush(List<Face> inFaces) {
        faces = inFaces;
    }

    public void out(PrintStream p) {
        p.print("\t{\n");
        for (Face f : faces) {
            f.out(p);
        }
        p.print("\t}\n");
    }
}

class Vector {
    private float v[];

    public Vector(float x, float y, float z) {
        v = new float[] {x, y, z};
    }
    public Vector(float x, float y, float z, float w) {
        v = new float[] {x, y, z, w};
    }
    public Vector(Vector other) {
        v = new float[other.v.length];
        for (int i = 0; i < other.v.length; ++i) {
            v[i] = other.v[i];
        }
    }

    public void out(PrintStream p) {
        for (int i = 0; i < v.length; ++i) {
            p.print(String.valueOf((int)v[i]));
            if (i != v.length - 1) {
                p.print(" ");
            }
        }
    }

    public Vector plus(Vector b) {
        Vector a = new Vector(this);
        int length = a.v.length < b.v.length ? a.v.length : b.v.length;
        for (int i = 0; i < length; ++i) {
            a.v[i] += b.v[i];
        }
        return a;
    }

    public Vector minus(Vector b) {
        Vector a = new Vector(this);
        int length = a.v.length < b.v.length ? a.v.length : b.v.length;
        for (int i = 0; i < length; ++i) {
            a.v[i] -= b.v[i];
        }
        return a;
    }

    public Vector times(float b) {
        Vector a = new Vector(this);
        for (int i = 0; i < a.v.length; ++i) {
            a.v[i] *= b;
        }
        return a;
    }
}