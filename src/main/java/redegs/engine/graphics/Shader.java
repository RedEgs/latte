package redegs.engine.graphics;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;
import static redegs.engine.util.Debug.glCheckError;

public class Shader {
    private int id;

    public Shader(String fragment_source, String vertex_source) {
        IntBuffer status = BufferUtils.createIntBuffer(1);
        int fs_id, vs_id;

        fs_id = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fs_id, fragment_source);
        glCompileShader(fs_id);

        glGetShaderiv(fs_id, GL_COMPILE_STATUS, status);
        if (status.get(0) == GL_FALSE) {
            int infoLogLength = glGetShaderi(fs_id, GL_INFO_LOG_LENGTH);
            String infoLog = glGetShaderInfoLog(fs_id, infoLogLength);
            System.err.println("Fragment shader compilation failed: " + infoLog);
        }

        vs_id = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs_id, vertex_source);
        glCompileShader(vs_id);

        glGetShaderiv(vs_id, GL_COMPILE_STATUS, status);
        if (status.get(0) == GL_FALSE) {
            int infoLogLength = glGetShaderi(vs_id, GL_INFO_LOG_LENGTH);
            String infoLog = glGetShaderInfoLog(vs_id, infoLogLength);
            System.err.println("Vertex shader compilation failed: " + infoLog);
        }



        this.id = glCreateProgram();
        glAttachShader(this.id, vs_id);
        glAttachShader(this.id, fs_id);
        glLinkProgram(this.id);
        glValidateProgram(this.id);

        glGetShaderiv(this.id, GL_LINK_STATUS, status);
        if (status.get(0) == GL_FALSE) {
            String infoLog = glGetProgramInfoLog(this.id, GL_INFO_LOG_LENGTH);
            System.err.println("Shader program link failed: " + infoLog);
        }

        glDeleteShader(vs_id);
        glDeleteShader(fs_id);

    }

    public void use() {
        glUseProgram(this.id);
    }

    public int getId() {
        return id;
    }











    public void setUniform1f(String name, float v1) {
        int location = glGetUniformLocation(this.id, name);
        glUniform1f(location, v1);
    }

    public void setUniform2f(String name, float v1, float v2) {
        int location = glGetUniformLocation(this.id, name);
        glUniform2f(location, v1, v2);
    }

    public void setUniform3f(String name, float v1, float v2, float v3) {
        int location = glGetUniformLocation(this.id, name);
        glUniform3f(location, v1, v2, v3);
    }

    public void setUniform4f(String name, float v1, float v2, float v3, float v4) {
        int location = glGetUniformLocation(this.id, name);
        glUniform4f(location, v1, v2, v3, v4);
    }

    public void setUniform1i(String name, int v1) {
        int location = glGetUniformLocation(this.id, name);
        glUniform1i(location, v1);
    }

    public void setUniform2i(String name, int v1, int v2) {
        int location = glGetUniformLocation(this.id, name);
        glUniform2i(location, v1, v2);
    }

    public void setUniform3i(String name, int v1, int v2, int v3) {
        int location = glGetUniformLocation(this.id, name);
        glUniform3i(location, v1, v2, v3);
    }

    public void setUniform4i(String name, int v1, int v2, int v3, int v4) {
        int location = glGetUniformLocation(this.id, name);
        glUniform4i(location, v1, v2, v3, v4);
    }

    public void setUniformMat4(String name, FloatBuffer mat) {
        int location = glGetUniformLocation(this.id, name);
        glUniformMatrix4fv(location, false, mat);
    }















    public static String VertexShader_position = """
    #version 330 core
    layout (location = 0) in vec3 aPos;

    void main()
    {
        gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
    }
    """;

    public static String FragmentShader_position = """
    #version 330 core
    out vec4 FragColor;
        
    void main()
    {
        FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);
    }
    """;

    public static String VertexShader_positionColor = """
    #version 330 core
    layout (location = 0) in vec3 aPos;
    layout (location = 1) in vec3 aColor;
    
    out vec3 color;
    
    void main()
    {
        gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
        color = aColor;
    }
    """;

    public static String FragmentShader_positionColor = """
    #version 330 core
    out vec4 FragColor;
        
    in vec3 color;
    
    void main()
    {
        FragColor = vec4(color.x, color.y, color.z, 1.0f);
    }
    """;

    public static String VertexShader_positionColorUV = """
    #version 330 core
    layout (location = 0) in vec3 aPos;
    layout (location = 1) in vec3 aColor;
    layout (location = 2) in vec2 aUV;
    
    out vec3 color;
    out vec2 uv;
    
    void main()
    {
        gl_Position = vec4(aPos.x, aPos.y, aPos.z, 1.0);
        color = aColor;
        uv = aUV;       
    }
    """;

    public static String FragmentShader_positionColorUV = """
    #version 330 core
    out vec4 FragColor;
        
    in vec3 color;
    in vec2 uv;
    
    uniform sampler2D texture0;
    
    void main()
    {
        FragColor = texture(texture0, uv) * vec4(color.xyz, 1.0f);
    }
    """;

    public static String VertexShader_camera = """
    #version 330 core
    #extension GL_NV_shader_buffer_load : enable
    
    layout (location = 0) in vec3 aPos;
    layout (location = 1) in vec3 aColor;
    layout (location = 2) in vec2 aUV;
       
    layout (std140) uniform CameraInfo {
        mat4 proj;
        mat4 view;
    };
    
    uniform mat4 model;
    
    out vec3 color;
    out vec2 uv;
    
    void main()
    {
        gl_Position = proj * view * model * vec4(aPos, 1.0);
        color = aColor;
        uv = aUV;       
    }
    """;

    public static String FragmentShader_camera = """
    #version 330 core
    out vec4 FragColor;
        
    in vec3 color;
    in vec2 uv;
    
    uniform sampler2D texture0;
    
    void main()
    {
        FragColor = texture(texture0, uv);
    }
    """;



}
