package redegs.engine.engine.adapters;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.nio.IntBuffer;

public class IntBufferAdapter implements JsonSerializer<IntBuffer>, JsonDeserializer<IntBuffer> {
    @Override
    public JsonElement serialize(IntBuffer buf, Type type, JsonSerializationContext ctx) {
        JsonArray arr = new JsonArray();
        IntBuffer dup = buf.duplicate(); // don't disturb the original's position/limit
        dup.rewind();
        while (dup.hasRemaining()) {
            arr.add(dup.get());
        }
        return arr;
    }

    @Override
    public IntBuffer deserialize(JsonElement el, Type type, JsonDeserializationContext ctx) {
        JsonArray arr = el.getAsJsonArray();
        IntBuffer buf = IntBuffer.allocate(arr.size());
        for (JsonElement e : arr) {
            buf.put(e.getAsInt());
        }
        buf.flip();
        return buf;
    }
}