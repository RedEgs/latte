package redegs.engine.engine.adapters;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;

public class ByteBufferAdapter implements JsonSerializer<ByteBuffer>, JsonDeserializer<ByteBuffer> {
    @Override
    public JsonElement serialize(ByteBuffer buf, Type type, JsonSerializationContext ctx) {
        JsonArray arr = new JsonArray();
        ByteBuffer dup = buf.duplicate();
        dup.rewind();
        while (dup.hasRemaining()) arr.add(dup.get());
        return arr;
    }

    @Override
    public ByteBuffer deserialize(JsonElement el, Type type, JsonDeserializationContext ctx) {
        JsonArray arr = el.getAsJsonArray();
        ByteBuffer buf = ByteBuffer.allocateDirect(arr.size()); // direct if this feeds OpenGL
        for (JsonElement e : arr) buf.put((byte) e.getAsInt());
        buf.flip();
        return buf;
    }
}