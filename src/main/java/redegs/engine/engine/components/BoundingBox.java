package redegs.engine.engine.components;
import org.joml.Vector3dc;
import org.joml.Vector3f;

public class BoundingBox {
    public Vector3f min;
    public Vector3f max;

    public BoundingBox(Vector3f min, Vector3f max) {
        this.min = min;
        this.max = max;
    }

    public Vector3f getCenter() {
        return new Vector3f((Vector3dc) min).add(max).mul(0.5f);
    }

    public Vector3f getSize() {
        return new Vector3f((Vector3dc) max).sub(min);
    }

    public float getRadius() {
        return getSize().length() * 0.5f;
    }
}