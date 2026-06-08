package redegs.engine.graphics;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL20.*;

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
            System.err.println(fragment_source);
        }

        vs_id = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vs_id, vertex_source);
        glCompileShader(vs_id);

        glGetShaderiv(vs_id, GL_COMPILE_STATUS, status);
        if (status.get(0) == GL_FALSE) {
            int infoLogLength = glGetShaderi(vs_id, GL_INFO_LOG_LENGTH);
            String infoLog = glGetShaderInfoLog(vs_id, infoLogLength);
            System.err.println("Vertex shader compilation failed: " + infoLog);
            System.err.println(vertex_source);
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

    public void setUniform3f(String name, Vector3f vector) {
        int location = glGetUniformLocation(this.id, name);
        glUniform3f(location, vector.x, vector.y, vector.z);
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

    public void setUniformMat4(String name, Matrix4f matrix) {
        int location = glGetUniformLocation(this.id, name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            matrix.get(buffer);
            glUniformMatrix4fv(location, false, buffer);
        }
    }

    public void setUniformMat3(String name, Matrix3f matrix) {
        int location = glGetUniformLocation(this.id, name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(9);
            matrix.get(buffer);
            glUniformMatrix3fv(location, false, buffer);
        }
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

    public static String VertexShader_lit = """
    #version 330 core
    #extension GL_NV_shader_buffer_load : enable
    
    layout (location = 0) in vec3 aPos;
    layout (location = 1) in vec3 aNormal;
    layout (location = 2) in vec2 aUV;
       
    layout (std140) uniform CameraInfo {
        mat4 proj;
        mat4 view;
    };
    
    uniform mat4 model;
    
    out vec3 normal;
    out vec3 fragPos;
    out vec2 uv;
    
    void main()
    {
        gl_Position = proj * view * model * vec4(aPos, 1.0);
        
    
        normal = aNormal;
        fragPos = vec3(model * vec4(aPos, 1.0));
        uv = aUV;       
    }
    """;

    public static String FragmentShader_lit = """
    #version 330 core
    out vec4 FragColor;
        
    in vec3 normal;
    in vec3 fragPos;
    in vec2 uv;
    
    uniform sampler2D texture0;
    uniform vec3 lightPos;
    
    void main()
    {
    
        vec3 lightColor = vec3(1.0, 0.0, 0.0);
    
        vec3 norm = normalize(normal);
        vec3 lightDir = normalize(lightPos - fragPos); 
        float diff = max(dot(norm, lightDir), 0.0);
        vec3 diffuse = diff * lightColor;
        
        vec3 ambient = vec3(0.1f);
        vec4 objectColor = texture(texture0, uv);
        
        vec3 result = (ambient + diffuse) * objectColor.xyz;
        FragColor = vec4(result, 1.0);
        
    }
    """;


    public static String VertexShader_geometry = """
    #version 330 core
    
    layout (location = 0) in vec3 aPos;
    layout (location = 1) in vec3 aNormal;
    layout (location = 2) in vec2 aUV;
    
    out vec3 FragPos;
    out vec2 TexCoords;
    out vec3 Normal;
            out vec4 FragPosLightSpace;
               
    layout (std140) uniform CameraInfo {
        mat4 proj;
        mat4 view;
    };
    
    uniform mat4 model;
    uniform mat3 normalMatrix;
            uniform mat4 lightSpaceMatrix;
    
    void main()
    {
        vec4 worldPos = model * vec4(aPos, 1.0);
    
        FragPos = worldPos.xyz;
        TexCoords = aUV;
        Normal = normalize(normalMatrix * aNormal);
                FragPosLightSpace = lightSpaceMatrix * vec4(FragPos, 1.0);
        
        gl_Position = proj * view * worldPos;     
    }
    """;

    public static String FragmentShader_geometry = """
    #version 330 core
    layout (location = 0) out vec3 gPosition;
    layout (location = 1) out vec3 gNormal;
    layout (location = 2) out vec4 gAlbedoSpec;
            layout (location = 3) out vec4 gShadowmap;
       
    in vec2 TexCoords;
    in vec3 FragPos;
    in vec3 Normal;
            in vec4 FragPosLightSpace;
    
    layout (std140) uniform CameraInfo {
        mat4 proj;
        mat4 view;
    };
    
    struct Material {
        vec3 ambient;
        sampler2D diffuse;
        sampler2D specular;
        float shininess;
    };
    
    uniform Material material;
    uniform vec3 view_pos;
    


    void main()
    {
    
        gPosition = FragPos;                         // world-space position
        gNormal   = normalize(Normal);               // world-space normal
        gAlbedoSpec.rgb = texture(material.diffuse, TexCoords).rgb;  // diffuse color
        gAlbedoSpec.a   = texture(material.specular, TexCoords).r;   // specular intensity
                gShadowmap = FragPosLightSpace;
        
    }
    """;


    public static String VertexShader_lighting = """
    #version 330 core
    #extension GL_NV_shader_buffer_load : enable
    
    layout (location = 0) in vec3 aPos;
    layout (location = 1) in vec3 aNormal;
    layout (location = 2) in vec2 aUV;
    
    out vec2 TexCoords;
    
    void main() {
        TexCoords = aUV;
        gl_Position = vec4(aPos, 1.0);
    }
    """;

    public static String FragmentShader_lighting = """
                    #version 330 core
                       out vec4 FragColor;
            
                       in vec2 TexCoords;
            
                       struct PointLight {
                           vec3 position;
                           vec3 color;
                           float intensity;
                           float radius;
                       };
            
                       struct DirectionalLight {
                           vec3 direction;
                           vec3 ambient;
                           vec3 diffuse;
                           vec3 specular;
                       };
            
                       uniform sampler2D gPosition;
                       uniform sampler2D gNormal;
                       uniform sampler2D gAlbedoSpec;
                       uniform sampler2D gFragPosLightSpace;
                       uniform sampler2D gShadowmap;
            
                       uniform vec3 viewPos;
                       uniform int light_count;
                       uniform PointLight lights[10];
                       uniform DirectionalLight dir_light;
            
                       float calcShadow(vec4 fragPosLightSpace, vec3 normal, vec3 lightDir) {
                           vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
                           projCoords = projCoords * 0.5 + 0.5;
            
                           // Outside far plane = no shadow
                           if (projCoords.z > 1.0) return 0.0;
            
                           float currentDepth = projCoords.z;
                           float bias = max(0.05 * (1.0 - dot(normal, lightDir)), 0.005);
            
                           // PCF
                           float shadow = 0.0;
                           vec2 texelSize = 1.0 / textureSize(gShadowmap, 0);
                           for (int x = -1; x <= 1; x++) {
                               for (int y = -1; y <= 1; y++) {
                                   float pcfDepth = texture(gShadowmap, projCoords.xy + vec2(x, y) * texelSize).r;
                                   shadow += currentDepth - bias > pcfDepth ? 1.0 : 0.0;
                               }
                           }
                           return shadow / 9.0;
                       }
            
                       void main() {
                           // Sample G-buffer
                           vec3 FragPos        = texture(gPosition,    TexCoords).rgb;
                           vec3 Normal         = normalize(texture(gNormal, TexCoords).rgb);
                           vec3 Albedo         = texture(gAlbedoSpec,  TexCoords).rgb;
                           float Specular      = texture(gAlbedoSpec,  TexCoords).a;
                           vec4 FragPosLightSpace = texture(gFragPosLightSpace, TexCoords);
            
                           vec3 viewDir  = normalize(viewPos - FragPos);
                           vec3 lightDir = normalize(-dir_light.direction);
            
                           // Directional light
                           float diff = max(dot(Normal, lightDir), 0.0);
            
                           // Use material shininess — guard against 0 to avoid undefined pow()
                           float shininess = max(Specular * 256.0, 1.0);
                           vec3 halfwayDir = normalize(lightDir + viewDir);
                           float spec = pow(max(dot(Normal, halfwayDir), 0.0), shininess);
            
                           vec3 ambient  = dir_light.ambient  * Albedo;
                           vec3 diffuse  = dir_light.diffuse  * diff * Albedo;
                           vec3 specular = dir_light.specular * spec * Albedo;
            
                           float shadow = calcShadow(FragPosLightSpace, Normal, lightDir);
                           vec3 lighting = ambient + (1.0 - shadow) * (diffuse + specular);
            
                           // Point lights
                           for (int i = 0; i < light_count; i++) {
                               float distance = length(lights[i].position - FragPos);
                               if (distance >= lights[i].radius) continue;
            
                               vec3 pointLightDir = normalize(lights[i].position - FragPos);
                               vec3 pointHalfway  = normalize(pointLightDir + viewDir);
            
                               float attenuation = 1.0 - (distance / lights[i].radius);
                               attenuation *= attenuation;
            
                               vec3 pointDiffuse  = max(dot(Normal, pointLightDir), 0.0) * Albedo * lights[i].color;
                               float pointSpec    = pow(max(dot(Normal, pointHalfway), 0.0), 16.0);
                               vec3 pointSpecular = lights[i].color * pointSpec * Specular;
            
                               lighting += (pointDiffuse + pointSpecular) * lights[i].intensity * attenuation;
                           }
            
                           FragColor = vec4(lighting, 1.0);
                       }
    """;

    public static String VertexShader_shadowDepth = """
                #version 330 core
                layout (location = 0) in vec3 aPos;
            
                uniform mat4 lightSpaceMatrix;
                uniform mat4 model;
            
                void main()
                {
                    gl_Position = lightSpaceMatrix * model * vec4(aPos, 1.0);
                }
            """;

    public static String FragmentShader_shadowDepth = """
                #version 330 core
            
                void main()
                {
            
                }
            """;

    public static String VertexShader_cubemap = """
    #version 330 core
    layout (location = 0) in vec3 aPos;
    layout (location = 1) in vec3 aNormal;
    layout (location = 2) in vec2 aUV;

    
    out vec3 TexCoords;
    
    uniform mat4 model;
    uniform mat4 proj;
    uniform mat4 view;

     
    void main()
    {
        TexCoords = aPos;
        vec4 pos = proj * view * vec4(aPos, 1.0);
        gl_Position = pos.xyww;
    }
    """;

    public static String FragmentShader_cubemap = """
    #version 330 core
    out vec4 FragColor;
    
    in vec3 TexCoords;
    
    uniform vec3 cameraPos;
    uniform samplerCube skybox;
        
    void main()
    {   
        FragColor = texture(skybox, TexCoords); 
    }
    """;

}
