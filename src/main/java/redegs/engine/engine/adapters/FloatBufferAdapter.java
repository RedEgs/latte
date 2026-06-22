package redegs.engine.engine.adapters;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.nio.FloatBuffer;


public class FloatBufferAdapter implements JsonSerializer<FloatBuffer>, JsonDeserializer<FloatBuffer> {
    @Override
    public JsonElement serialize(FloatBuffer buf, Type type, JsonSerializationContext ctx) {
        JsonArray arr = new JsonArray();
        FloatBuffer dup = buf.duplicate(); // don't disturb the original's position/limit
        dup.rewind();
        while (dup.hasRemaining()) {
            arr.add(dup.get());
        }
        return arr;
    }

    @Override
    public FloatBuffer deserialize(JsonElement el, Type type, JsonDeserializationContext ctx) {
        JsonArray arr = el.getAsJsonArray();
        FloatBuffer buf = FloatBuffer.allocate(arr.size());
        for (JsonElement e : arr) {
            buf.put(e.getAsFloat());
        }
        buf.flip(); // make it readable from position 0
        return buf;
    }
}