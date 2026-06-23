package redegs.engine.engine.script;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptAPI {
    public static void setupAPI(Globals globals, int entity) {
        LuaTable entityTable = new LuaTable();
        globals.set("self", entityTable);

        globals.set("print", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                System.out.println(arg.toString());
                return NIL;
            }
        });

        entityTable.set("getID", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(entity);
            }
        } );







    }

    public static Integer getErrorLine(LuaError e) {
        Pattern pattern = Pattern.compile(":(\\d+)");
        Matcher matcher = pattern.matcher(e.getMessage());

        if (matcher.find()) {
            int line = Integer.parseInt(matcher.group(1));
            return line;
        }
        return null;
    }
}
