package redegs.engine.engine.system.script;

import org.joml.Vector3f;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import redegs.engine.engine.system.script.datatypes.LuaVector3f;
import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.engine.system.component.Component;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptAPI {
    public static void setupAPI(Globals globals, int entity) {
        LuaTable entityTable = new LuaTable();
        LuaTable componentTable = new LuaTable();
        globals.set("self", entityTable);
        globals.set("entity", entityTable);
        entityTable.set("components", componentTable);
        entityTable.set("id", entity);

        globals.set("print", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                System.out.println(arg.toString());
                return NIL;
            }
        });


        for (var o : EntitySceneManager.getInstance().getComponents(entity)) {
            if (o instanceof Component c) {
                LuaTable t = new LuaTable();
                t.set("name", c.getName());

                for (Method method : c.getClass().getMethods()) {
                    String name = method.getName();

                    if (name.equals("getClass") || name.equals("toString") || name.equals("hashCode") || !method.isAnnotationPresent(LuaExpose.class)) continue;

                    t.set(name, new VarArgFunction() {

                        @Override
                        public Varargs invoke(Varargs args) {
                        try {
                            int paramCount = method.getParameterCount();
                            Object[] javaArgs = new Object[paramCount];

                            // Lua index 1 = self, so shift by 1
                            for (int i = 0; i < paramCount; i++) {
                                LuaValue v = args.arg(i + 2);

                                Class<?> type = method.getParameterTypes()[i];

                                javaArgs[i] = convert(v, type);
                            }

                            Object result = method.invoke(o, javaArgs);

                            return toLua(result);

                        } catch (Exception e) {
                            throw new LuaError(e);
                        }
                        }
                    });

                }

                componentTable.set(c.getName(), t);
            }
        }






    }

    public static Varargs toLua(Object result) {

        if (result instanceof Vector3f v)
            return LuaVector3f.create(v);

        if (result == null)
            return LuaValue.NIL;

        if (result instanceof Integer i)
            return LuaValue.valueOf(i);

        if (result instanceof Float f)
            return LuaValue.valueOf(f);

        if (result instanceof Double d)
            return LuaValue.valueOf(d);

        if (result instanceof Boolean b)
            return LuaValue.valueOf(b);

        if (result instanceof String s)
            return LuaValue.valueOf(s);

        if (result instanceof LuaValue lv)
            return lv;

        return LuaValue.userdataOf(result);
    }

    public static Object convert(LuaValue v, Class<?> type) {

        if (type == int.class || type == Integer.class)
            return v.toint();

        if (type == float.class || type == Float.class)
            return (float) v.todouble();

        if (type == double.class || type == Double.class)
            return v.todouble();

        if (type == boolean.class || type == Boolean.class)
            return v.toboolean();

        if (type == String.class)
            return v.tojstring();

        if (LuaValue.class.isAssignableFrom(type))
            return v;

        throw new RuntimeException("Unsupported type: " + type);
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
