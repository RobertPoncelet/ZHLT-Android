package test.zhlt_android;

import java.util.Map;

/**
 * Created by robert.poncelet on 29/01/18.
 */

public class MapFile {
    private Entity[] ents;

    public class Entity {
        public Map<String, String> kvs;
        public Brush[] brushes;

        public Entity(String classname) {
            kvs.put("classname", classname);
        }
    }

    public class Brush {
        public Face[] faces;
    }

    public class Face {
        public Vector points[];
        public String texName;
        public Vector uAxis[];
        public Vector vAxis[];
        public float rotation;
        public float scaleX;
        public float scaleY;
    }

    public class Vector {
        private float p[];

        public Vector(float x, float y, float z) {
            p = new float[] {x, y, z};
        }

        public Vector(float x, float y, float z, float w) {
            p = new float[] {x, y, z, w};
        }
    }
}