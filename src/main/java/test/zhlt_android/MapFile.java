package test.zhlt_android;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * Created by robert.poncelet on 29/01/18.
 */

public class MapFile {
    public List<Entity> ents;

    public static class Entity {
        public Map<String, String> kvs;
        public Brush[] brushes;

        public Entity(String classname) {
            kvs.put("classname", classname);
        }

        private void out(PrintStream p) {
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
        }
    }

    public static class Brush {
        public List<Face> faces;

        public Brush(List<Face> inFaces) {
            faces = inFaces;
        }

        private void out(PrintStream p) {
            p.print("\t{\n");
            for (Face f : faces) {
                f.out(p);
            }
            p.print("\t}\n");
        }
    }

    public static class Face {
        public Vector points[];
        public String texName;
        public Vector uAxis;
        public Vector vAxis;
        public float rotation;
        public float scaleX;
        public float scaleY;

        private void out(PrintStream p) {
            p.print("\t\t");
            for (Vector v : points) {
                p.print("( ");
                v.out(p);
                p.print(" ) ");
            }
            p.print(texName);
            p.print(" [ ");
            uAxis.out(p);
            p.print(" ] [ ");
            vAxis.out(p);
            p.print(" ] ");
            p.print(String.valueOf(rotation));
            p.print(" ");
            p.print(String.valueOf(scaleX));
            p.print(" ");
            p.print(String.valueOf(scaleY));
            p.print("\n");
        }
    }

    public static class Vector {
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

        private void out(PrintStream p) {
            for (int i = 0; i < v.length; ++i) {
                p.print(String.valueOf(v[i]));
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
    }

    public void write(PrintStream p) {
        for (Entity ent : ents) {
            ent.out(p);
        }
    }
}