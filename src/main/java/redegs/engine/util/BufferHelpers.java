package redegs.engine.util;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class BufferHelpers {
    public static FloatBuffer toFloatBuffer(Vector3f v) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(3);
        buffer.put(v.x);
        buffer.put(v.y);
        buffer.put(v.z);
        buffer.flip();
        return buffer;
    }
}
