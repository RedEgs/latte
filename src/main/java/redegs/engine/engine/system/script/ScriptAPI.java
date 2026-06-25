package redegs.engine.engine.system.script;

import org.joml.Vector3f;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.ZeroArgFunction;
import redegs.Engine;
import redegs.engine.engine.system.EntitySceneManager;
import redegs.engine.engine.system.component.Component;
import redegs.engine.engine.system.component.ComponentMeta;
import redegs.engine.engine.system.component.ComponentRegistry;
import redegs.engine.engine.system.script.datatypes.LuaVector3f;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptAPI {
    public static void setupAPI(Globals globals, int entity) {
        LuaTable entityTable = createEntityTable(entity);

        globals.set("self", entityTable);
        globals.set("entity", entityTable);
        globals.set("esm", createEsmTable());
        globals.set("scene", globals.get("esm"));

        globals.set("print", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                System.out.println(joinArgs(args));
                return LuaValue.NIL;
            }
        });
        globals.set("warn", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                System.err.println("[Lua warning] " + joinArgs(args));
                return LuaValue.NIL;
            }
        });
        globals.set("vec3", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return LuaVector3f.create(new Vector3f(
                        (float) args.arg(1).optdouble(0),
                        (float) args.arg(2).optdouble(0),
                        (float) args.arg(3).optdouble(0)
                ));
            }
        });
        globals.set("deltaTime", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(Engine.getDeltaTime());
            }
        });
        globals.set("time", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(org.lwjgl.glfw.GLFW.glfwGetTime());
            }
        });
    }

    private static LuaTable createEntityTable(int entity) {
        LuaTable entityTable = new LuaTable();
        LuaTable componentTable = new LuaTable();

        entityTable.set("id", entity);
        entityTable.set("components", componentTable);
        entityTable.set("refresh", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                populateComponents(entity, componentTable);
                return entityTable;
            }
        });
        entityTable.set("getComponent", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                Component component = getComponentByName(entity, firstStringArg(args));
                return component == null ? LuaValue.NIL : wrapComponent(component);
            }
        });
        entityTable.set("hasComponent", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                return LuaValue.valueOf(getComponentByName(entity, firstStringArg(args)) != null);
            }
        });
        entityTable.set("addComponent", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                Component component = createComponent(firstStringArg(args), entity);
                if (component == null) {
                    return LuaValue.NIL;
                }

                EntitySceneManager.getInstance().addComponent(entity, component);
                populateComponents(entity, componentTable);
                return wrapComponent(component);
            }
        });
        entityTable.set("removeComponent", new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                Component component = getComponentByName(entity, firstStringArg(args));
                if (component == null) {
                    return LuaValue.FALSE;
                }

                EntitySceneManager.getInstance().removeComponent(entity, component.getClass());
                populateComponents(entity, componentTable);
                return LuaValue.TRUE;
            }
        });

        populateComponents(entity, componentTable);
        return entityTable;
    }

    private static LuaTable createEsmTable() {
        LuaTable esmTable = new LuaTable();

        esmTable.set("createEntity", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(EntitySceneManager.getInstance().createEntity());
            }
        });
        esmTable.set("deleteEntity", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                EntitySceneManager.getInstance().deleteEntity(arg.toint());
                return LuaValue.NIL;
            }
        });
        esmTable.set("entityCount", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                return LuaValue.valueOf(EntitySceneManager.getInstance().getEntities());
            }
        });
        esmTable.set("getEntity", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                int id = arg.toint();
                if (id < 0 || id >= EntitySceneManager.getInstance().getEntities()) {
                    return LuaValue.NIL;
                }

                return createEntityTable(id);
            }
        });
        esmTable.set("getComponents", new OneArgFunction() {
            @Override
            public LuaValue call(LuaValue arg) {
                LuaTable components = new LuaTable();
                int index = 1;
                for (Object object : EntitySceneManager.getInstance().getComponents(arg.toint())) {
                    if (object instanceof Component component) {
                        components.set(index++, wrapComponent(component));
                    }
                }
                return components;
            }
        });
        esmTable.set("hasComponent", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue entityArg, LuaValue componentArg) {
                return LuaValue.valueOf(getComponentByName(entityArg.toint(), componentArg.tojstring()) != null);
            }
        });
        esmTable.set("getComponent", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue entityArg, LuaValue componentArg) {
                Component component = getComponentByName(entityArg.toint(), componentArg.tojstring());
                return component == null ? LuaValue.NIL : wrapComponent(component);
            }
        });
        esmTable.set("addComponent", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue entityArg, LuaValue componentArg) {
                int targetEntity = entityArg.toint();
                Component component = createComponent(componentArg.tojstring(), targetEntity);
                if (component == null) {
                    return LuaValue.NIL;
                }

                EntitySceneManager.getInstance().addComponent(targetEntity, component);
                return wrapComponent(component);
            }
        });
        esmTable.set("removeComponent", new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue entityArg, LuaValue componentArg) {
                Component component = getComponentByName(entityArg.toint(), componentArg.tojstring());
                if (component == null) {
                    return LuaValue.FALSE;
                }

                EntitySceneManager.getInstance().removeComponent(entityArg.toint(), component.getClass());
                return LuaValue.TRUE;
            }
        });
        esmTable.set("componentTypes", new ZeroArgFunction() {
            @Override
            public LuaValue call() {
                LuaTable types = new LuaTable();
                int index = 1;
                for (ComponentRegistry.Entry entry : ComponentRegistry.all()) {
                    types.set(index++, entry.name());
                }
                return types;
            }
        });

        return esmTable;
    }

    private static void populateComponents(int entity, LuaTable componentTable) {
        for (LuaValue key : componentTable.keys()) {
            componentTable.set(key, LuaValue.NIL);
        }

        for (Object object : EntitySceneManager.getInstance().getComponents(entity)) {
            if (object instanceof Component component) {
                LuaTable wrapper = wrapComponent(component);
                for (String key : componentKeys(component)) {
                    componentTable.set(key, wrapper);
                }
            }
        }
    }

    private static LuaTable wrapComponent(Component component) {
        LuaTable table = new LuaTable();
        table.set("name", component.getName());
        table.set("type", component.getClass().getSimpleName());
        table.set("entity", component.getEntity());
        table.set("__java", LuaValue.userdataOf(component));

        for (Method method : component.getClass().getMethods()) {
            if (method.getName().equals("getClass")
                    || method.getName().equals("toString")
                    || method.getName().equals("hashCode")
                    || !method.isAnnotationPresent(LuaExpose.class)) {
                continue;
            }

            table.set(method.getName(), new VarArgFunction() {
                @Override
                public Varargs invoke(Varargs args) {
                    try {
                        int paramCount = method.getParameterCount();
                        Object[] javaArgs = new Object[paramCount];
                        int offset = args.narg() > 0 && args.arg(1).raweq(table) ? 2 : 1;

                        for (int i = 0; i < paramCount; i++) {
                            javaArgs[i] = convert(args.arg(i + offset), method.getParameterTypes()[i]);
                        }

                        return toLua(method.invoke(component, javaArgs));
                    } catch (InvocationTargetException e) {
                        throw new LuaError(e.getTargetException());
                    } catch (Exception e) {
                        throw new LuaError(e);
                    }
                }
            });
        }

        return table;
    }

    private static Component createComponent(String name, int entity) {
        ComponentRegistry.Entry entry = findComponentEntry(name);
        return entry == null ? null : entry.factory().apply(entity);
    }

    private static Component getComponentByName(int entity, String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        for (Object object : EntitySceneManager.getInstance().getComponents(entity)) {
            if (object instanceof Component component && matchesComponentName(component, name)) {
                return component;
            }
        }

        return null;
    }

    private static ComponentRegistry.Entry findComponentEntry(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        for (ComponentRegistry.Entry entry : ComponentRegistry.all()) {
            if (entry.name().equals(name) || entry.type().getSimpleName().equals(name)) {
                return entry;
            }
        }

        return null;
    }

    private static boolean matchesComponentName(Component component, String name) {
        if (component.getName().equals(name) || component.getClass().getSimpleName().equals(name)) {
            return true;
        }

        ComponentMeta meta = component.getClass().getAnnotation(ComponentMeta.class);
        return meta != null && meta.name().equals(name);
    }

    private static List<String> componentKeys(Component component) {
        ArrayList<String> keys = new ArrayList<>();
        keys.add(component.getName());
        keys.add(component.getClass().getSimpleName());

        ComponentMeta meta = component.getClass().getAnnotation(ComponentMeta.class);
        if (meta != null) {
            keys.add(meta.name());
        }

        return keys;
    }

    private static String firstStringArg(Varargs args) {
        int offset = args.narg() > 0 && args.arg(1).istable() ? 2 : 1;
        return args.arg(offset).tojstring();
    }

    private static String joinArgs(Varargs args) {
        StringBuilder out = new StringBuilder();
        for (int i = 1; i <= args.narg(); i++) {
            if (i > 1) {
                out.append("\t");
            }
            out.append(args.arg(i).tojstring());
        }
        return out.toString();
    }

    public static Varargs toLua(Object result) {
        if (result instanceof Vector3f v) {
            return LuaVector3f.create(v);
        }

        if (result == null) {
            return LuaValue.NIL;
        }

        if (result instanceof Integer i) {
            return LuaValue.valueOf(i);
        }

        if (result instanceof Float f) {
            return LuaValue.valueOf(f);
        }

        if (result instanceof Double d) {
            return LuaValue.valueOf(d);
        }

        if (result instanceof Boolean b) {
            return LuaValue.valueOf(b);
        }

        if (result instanceof String s) {
            return LuaValue.valueOf(s);
        }

        if (result instanceof LuaValue lv) {
            return lv;
        }

        return LuaValue.userdataOf(result);
    }

    public static Object convert(LuaValue v, Class<?> type) {
        if (type == int.class || type == Integer.class) {
            return v.toint();
        }

        if (type == float.class || type == Float.class) {
            return (float) v.todouble();
        }

        if (type == double.class || type == Double.class) {
            return v.todouble();
        }

        if (type == boolean.class || type == Boolean.class) {
            return v.toboolean();
        }

        if (type == String.class) {
            return v.tojstring();
        }

        if (LuaValue.class.isAssignableFrom(type)) {
            return v;
        }

        if (type == Vector3f.class) {
            LuaValue raw = v.get("__java");
            if (raw.isuserdata(Vector3f.class)) {
                return raw.touserdata(Vector3f.class);
            }

            return new Vector3f(
                    (float) v.get("x").optdouble(0),
                    (float) v.get("y").optdouble(0),
                    (float) v.get("z").optdouble(0)
            );
        }

        throw new RuntimeException("Unsupported type: " + type);
    }

    public static Integer getErrorLine(LuaError e) {
        Pattern pattern = Pattern.compile(":(\\d+)");
        Matcher matcher = pattern.matcher(e.getMessage());

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }
}
