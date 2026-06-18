package redegs.engine.engine.system.component;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public final class ComponentBootstrapper {
    private ComponentBootstrapper() {}

    public static void scanAndRegister() {
        try {
            String packageName = "redegs.engine";
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            URL resource = classLoader.getResource(path);

            if (resource == null) {
                System.err.println("ComponentBootstrapper: could not find package " + packageName);
                return;
            }

            scanDirectory(new File(resource.toURI()), packageName, classLoader);
        } catch (Exception e) {
            System.err.println("ComponentBootstrapper: scan failed — " + e.getMessage());
        }
    }

    private static void scanDirectory(File directory, String packageName, ClassLoader classLoader) {
        if (!directory.exists()) return;

        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), classLoader);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().replace(".class", "");
                try {
                    // getResource is safe — it doesn't execute any class code
                    String resourcePath = className.replace('.', '/') + ".class";
                    URL classResource = classLoader.getResource(resourcePath);
                    if (classResource == null) continue;

                    // Read the bytecode to check for the annotation without loading the class
                    try (var stream = classResource.openStream()) {
                        byte[] bytes = stream.readAllBytes();
                        String bytecode = new String(bytes);
                        // ComponentMeta's descriptor will appear in the constant pool if present
                        if (!bytecode.contains("ComponentMeta")) continue;
                    }

                    // Only NOW fully load the class
                    Class<?> c = Class.forName(className, true, classLoader);
                    if (c.isAnnotationPresent(ComponentMeta.class)) {
                        System.out.println("ComponentBootstrapper: registered " + className);
                    }

                } catch (Exception | NoClassDefFoundError e) {
                    // skip anything that fails — other classes may have unsatisfied deps
                }
            }
        }
    }
}