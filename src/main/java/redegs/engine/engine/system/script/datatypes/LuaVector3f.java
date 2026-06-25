package redegs.engine.engine.system.script.datatypes;

import org.joml.Vector3f;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;

import java.text.NumberFormat;

public class LuaVector3f {
    public static LuaValue create(Vector3f v) {
        LuaTable ud = new LuaTable();

        ud.set("x", v.x);
        ud.set("y", v.y);
        ud.set("z", v.z);

        // update method
        ud.set("set", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                v.x = (float) args.arg(2).todouble();
                v.y = (float) args.arg(3).todouble();
                v.z = (float) args.arg(4).todouble();
                return ud;
            }
        });

        // expose getters
        ud.set("get", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return LuaValue.varargsOf(
                        LuaValue.valueOf(v.x),
                        LuaValue.valueOf(v.y),
                        LuaValue.valueOf(v.z)
                );
            }
        });

        ud.set("toString", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(v.toString(NumberFormat.getNumberInstance()));
            }
        });

        // store raw Java object
        ud.set("__java", LuaValue.userdataOf(v));

        // metatable (optional but recommended)
        LuaTable mt = new LuaTable();
        mt.set("__index", ud);
        ud.setmetatable(mt);

        return ud;
    }
}
