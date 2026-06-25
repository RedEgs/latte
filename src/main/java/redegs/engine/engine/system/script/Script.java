package redegs.engine.engine.system.script;

import com.google.gson.JsonObject;
import imgui.ImGui;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import redegs.Engine;
import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.engine.system.FileDialogs;
import redegs.engine.engine.system.component.Component;
import redegs.engine.engine.system.component.ComponentMeta;
import redegs.engine.engine.system.component.ComponentRegistry;

import java.nio.file.Files;
import java.nio.file.Path;

@ComponentMeta(name = "Script", category = "Scripting", description = "A Lua script responsible for controlling entity behaviour.")
public class Script extends Component {
    protected String scriptName = "Script";
    protected String source = "";
    protected String location;

    protected Globals globals;
    protected LuaValue fnOnStart;
    protected LuaValue fnOnUpdate;
    protected LuaValue fnOnDelete;
    protected LuaValue fnOnDraw;
    protected LuaValue fnOnQuit;

    protected boolean loaded = false;
    protected boolean started = false;
    private boolean errored = false;  // stop spamming errors every frame


    public Script(int entity) {
        super(entity);
        name = "ScriptComponent";
    }

    public Script(String scriptName, int entity) {
        super(entity);
        name = "ScriptComponent";
        this.scriptName = scriptName;
    }

    public Script(String scriptName, String path, int entity) {
        super(entity);
        name = "ScriptComponent";
        this.scriptName = scriptName;
        this.location = path;
        loadScript(location);
    }

    static {
        ComponentRegistry.register(
                Script.class,
                entity -> new Script(entity)
        );
    }

    @LuaExpose
    public void loadScript(String location) {
        if (location == null) return;
        this.location = location;

        globals = JsePlatform.standardGlobals();

        try {
            Path p = Path.of(location);
            source = Files.readString(p);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find lua script: " + location);
        }

        rebuild();

    }

    private void setupAPI() {
        EntitySceneManager esm = EntitySceneManager.getInstance();
        int ent = this.entity;

        ScriptAPI.setupAPI(globals, ent);

    }


    @LuaExpose
    public LuaError rebuild() {
        try {
            setupAPI();

            LuaValue chunk = globals.loadfile(location);
            chunk.call();
            System.out.println("built!");
            System.out.println(source);

            fnOnStart = globals.get("OnStart");
            fnOnUpdate = globals.get("OnUpdate");

            loaded = true;

        } catch (LuaError e) {
            errored = true;
            loaded = false;
            System.err.println("Failed to load lua chunk: " + e.getMessage());
            return e;
        }
        return null;
    }


    @Override
    public JsonObject Save() {
        super.Save();
        JsonObject o = new JsonObject();
        o.addProperty("scriptName", scriptName);
        o.addProperty("source", source);
        o.addProperty("location", location);
        return o;
    }

    @Override
    public void Load(JsonObject data) {
        super.Load(data);
        scriptName = data.get("scriptName").getAsString();
        if (data.get("location") == null) {
            source = data.get("source").getAsString();
        } else {
            location = data.get("location").getAsString();
            loadScript(location);
        }


    }

    @Override
    public void OnEditorInspect() {
        super.OnEditorInspect();

        ImGui.separatorText(scriptName);
        ImGui.text("Location: " + location);
        ImGui.sameLine();
        if (ImGui.button("Load lua file ...")) {
            String path = FileDialogs.openFile("Select .lua ...", FileDialogs.homeDirectory(), new String[]{"*.lua"}, ".lua files");
            this.location = path;
            loadScript(location);
        }
        ImGui.separator();
        ImGui.spacing();
        ImGui.indent(16.0f);
    }

    @Override
    public void OnUpdate() {
        super.OnUpdate();

        if (!loaded || errored || !Engine.getGameMode()) return;

        if (!fnOnUpdate.isnil()) {
            fnOnUpdate.call();
        }
    }

    public void OnStart() {
        if (!loaded || errored || !Engine.getGameMode()) return;

        if (!fnOnStart.isnil()) {
            fnOnStart.call();
        }
    }





    @LuaExpose
    public String getSource() {
        return source;
    }

    @LuaExpose
    public String getScriptName() {
        return scriptName;
    }

    @LuaExpose
    public String getLocation() {
        return location;
    }

    @LuaExpose
    public boolean isLoaded() {
        return loaded;
    }
}
