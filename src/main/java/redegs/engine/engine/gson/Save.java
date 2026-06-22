package redegs.engine.engine.gson;

import com.google.gson.JsonObject;
import org.joml.Vector3f;

public class Save {
    public static JsonObject Vec3ToJson(Vector3f v) {
        JsonObject o = new JsonObject();
        o.addProperty("x", v.x);
        o.addProperty("y", v.y);
        o.addProperty("z", v.z);
        return o;
    }

    public static Vector3f JsonToVec3(JsonObject o) {
        return new Vector3f(o.get("x").getAsFloat(), o.get("y").getAsFloat(), o.get("z").getAsFloat());
    }
}
