/*
Copyright 2017 Tamas Bolner

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package CRTerm;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.opengl.*;
import static org.lwjgl.opengl.GL20.*;


class Pipeline {
    private int programID;
    private List<Integer> shaders = new ArrayList<Integer>();

    /**
     * Create shader program.
     */
    Pipeline() {
        this.programID = GL20.glCreateProgram();
    }

    /**
     * Release OpenGL resources.
     */
    void close() {
        for(int shaderID : this.shaders) {
            glDetachShader(this.programID, shaderID);
            glDeleteShader(shaderID);
        }

        glDeleteProgram(this.programID);
    }

    /**
     * Define shader input.
     *
     * @param index The location of the input.
     * @param name The variable name used in the shader code.
     */
    void bindAttribLocation(int index, CharSequence name) {
        GL20.glBindAttribLocation(this.programID, index, name);
    }

    /**
     * Add a shader to the pipeline.
     *
     * @param filePath Path to the resource file.
     * @param shaderType Examples: GL20.GL_VERTEX_SHADER, GL20.GL_FRAGMENT_SHADER
     * @throws Exception The thrown exceptions contain error messages.
     */
    void addShader(String filePath, int shaderType) throws Exception {
        StringBuilder shaderSource = new StringBuilder();
        int shaderID = 0;

        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(filePath);
        if (resource == null) {
            throw new Exception("Resource not found: " + filePath);
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(resource.getFile()));
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            throw new Exception("Unable to read file: " + resource.getFile());
        }

        shaderID = GL20.glCreateShader(shaderType);
        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);

        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new Exception("Unable to compile shader: " + filePath + ". Error message: " + glGetShaderInfoLog(shaderID, 1024));
        }

        GL20.glAttachShader(this.programID, shaderID);

        this.shaders.add(shaderID);
    }

    /**
     * @throws Exception The thrown exceptions contain error messages.
     */
    void link() throws Exception {
        GL20.glLinkProgram(this.programID);

        if (GL11.glGetError() != GL11.GL_NO_ERROR) {
            throw new Exception("Linking shader program failed.");
        }

        int linked = glGetProgrami(this.programID, GL_LINK_STATUS);
        if (linked == 0) {
            throw new Exception("Linking program failed. (pipeline");
        }

        GL20.glValidateProgram(this.programID);

        if (GL11.glGetError() != GL11.GL_NO_ERROR) {
            throw new Exception("Validating shader program failed.");
        }
    }

    /**
     * @return The OpenGL ID of the program.
     */
    int getProgramID() {
        return this.programID;
    }
}
