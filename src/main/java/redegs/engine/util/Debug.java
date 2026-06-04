package redegs.engine.util;

import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL30C.GL_INVALID_FRAMEBUFFER_OPERATION;
import static org.lwjgl.opengl.KHRRobustness.GL_NO_ERROR;

public class Debug {
    public static int glCheckError() throws GLException {
        int errorCode;
        String error = "";
        while ((errorCode = glGetError()) != GL_NO_ERROR) {
            switch (errorCode) {
                case GL_INVALID_ENUM -> error = "INVALID_ENUM";
                case GL_INVALID_VALUE -> error = "INVALID_VALUE";
                case GL_INVALID_OPERATION -> error = "INVALID_OPERATION";
                case GL_STACK_OVERFLOW -> error = "STACK_OVERFLOW";
                case GL_STACK_UNDERFLOW -> error = "STACK_UNDERFLOW";
                case GL_OUT_OF_MEMORY -> error = "OUT_OF_MEMORY";
                case GL_INVALID_FRAMEBUFFER_OPERATION -> error = "INVALID_FRAMEBUFFER_OPERATION";
            }

            if (errorCode != GL_INVALID_OPERATION) {
                throw new GLException(errorCode);
            }

        }
        return errorCode;
    }
}
