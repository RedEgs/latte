package redegs.engine.engine.events;

public class KeyPressEvent {
    public final int key;
    public final int scancode;
    public final int action;
    public final int mods;

    public KeyPressEvent(int key, int scancode, int action, int mods) {
        this.key = key;
        this.scancode = scancode;
        this.action = action;
        this.mods = mods;
    }
}