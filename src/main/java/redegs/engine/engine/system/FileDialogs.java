package redegs.engine.engine.system;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.io.File;

import static org.lwjgl.util.tinyfd.TinyFileDialogs.*;

public final class FileDialogs {

    private FileDialogs() {}

    /**
     * Opens a file selection dialog.
     */
    public static String openFile(
            String title,
            String defaultPath,
            String[] filters,
            String filterDescription
    ) {
        try (MemoryStack stack = MemoryStack.stackPush()) {

            PointerBuffer filterBuffer = null;

            if (filters != null && filters.length > 0) {
                filterBuffer = stack.mallocPointer(filters.length);

                for (String filter : filters) {
                    filterBuffer.put(stack.UTF8(filter));
                }

                filterBuffer.flip();
            }

            return tinyfd_openFileDialog(
                    title,
                    defaultPath,
                    filterBuffer,
                    filterDescription,
                    false
            );
        }
    }

    /**
     * Opens a multi-file selection dialog.
     */
    public static String openFiles(
            String title,
            String defaultPath,
            String[] filters,
            String filterDescription
    ) {
        try (MemoryStack stack = MemoryStack.stackPush()) {

            PointerBuffer filterBuffer = null;

            if (filters != null && filters.length > 0) {
                filterBuffer = stack.mallocPointer(filters.length);

                for (String filter : filters) {
                    filterBuffer.put(stack.UTF8(filter));
                }

                filterBuffer.flip();
            }

            return tinyfd_openFileDialog(
                    title,
                    defaultPath,
                    filterBuffer,
                    filterDescription,
                    true
            );
        }
    }

    /**
     * Save file dialog.
     */
    public static String saveFile(
            String title,
            String defaultPath,
            String[] filters,
            String filterDescription
    ) {
        try (MemoryStack stack = MemoryStack.stackPush()) {

            PointerBuffer filterBuffer = null;

            if (filters != null && filters.length > 0) {
                filterBuffer = stack.mallocPointer(filters.length);

                for (String filter : filters) {
                    filterBuffer.put(stack.UTF8(filter));
                }

                filterBuffer.flip();
            }

            return tinyfd_saveFileDialog(
                    title,
                    defaultPath,
                    filterBuffer,
                    filterDescription
            );
        }
    }

    /**
     * Folder picker.
     */
    public static String selectFolder(String title, String defaultPath) {
        return tinyfd_selectFolderDialog(
                title,
                defaultPath
        );
    }


    /**
     * Text input dialog.
     */
    public static String input(
            String title,
            String message,
            String defaultValue
    ) {
        return tinyfd_inputBox(
                title,
                message,
                defaultValue
        );
    }

    /**
     * Returns user's home folder.
     */
    public static String homeDirectory() {
        return System.getProperty("user.home");
    }

    /**
     * Returns current working directory.
     */
    public static String workingDirectory() {
        return new File(".").getAbsolutePath();
    }
}