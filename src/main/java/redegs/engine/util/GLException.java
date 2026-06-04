package redegs.engine.util;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL11C.GL_OUT_OF_MEMORY;
import static org.lwjgl.opengl.GL30C.GL_INVALID_FRAMEBUFFER_OPERATION;

public class GLException extends Exception {
    public GLException(int glErrorEnum) {
        String error = "NONE";
        switch (glErrorEnum) {
            case GL_INVALID_ENUM -> error = "INVALID_ENUM";
            case GL_INVALID_VALUE -> error = "INVALID_VALUE";
            case GL_INVALID_OPERATION -> error = "INVALID_OPERATION";
            case GL_STACK_OVERFLOW -> error = "STACK_OVERFLOW";
            case GL_STACK_UNDERFLOW -> error = "STACK_UNDERFLOW";
            case GL_OUT_OF_MEMORY -> error = "OUT_OF_MEMORY";
            case GL_INVALID_FRAMEBUFFER_OPERATION -> error = "INVALID_FRAMEBUFFER_OPERATION";
        }

        System.err.print(error);
        System.err.print(" Encountered | ");
        System.err.print(getStackTrace());
    }
}
