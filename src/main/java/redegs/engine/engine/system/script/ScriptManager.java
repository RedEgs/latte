package redegs.engine.engine.system.script;

import redegs.engine.engine.system.EntitySceneManager;

import java.util.ArrayList;
import java.util.List;

public class ScriptManager {
    public static void buildAllScripts() {
        EntitySceneManager esm = EntitySceneManager.getInstance();
        List<Script> scripts = new ArrayList<>(esm.GetScene().getStore(Script.class).get());
        for (Script s: scripts) {
            s.rebuild();
            s.OnStart();
        }
    }
}
