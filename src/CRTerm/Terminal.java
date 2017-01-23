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

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import static org.lwjgl.glfw.GLFW.*;


public class Terminal {
    private boolean glfwInitialized;
    private long windowID;
    private Pipeline pipeline;
    private Grid grid;
    private Font font;

    /**
     * Constructor.
     *
     * @param columns Horizontal character count.
     * @param rows Vertical character count.
     * @param color RGB font color. Example: 0x70fe80
     * @param scanLineBreadth The CRT screen is constructed of vertical scan lines. This parameter tells their breadth or thickness. 1 = no spaces in between. Example value: 0.7d
     * @param fontThickness This parameter tells how much should the scan lines overreach the pixels of the characters. 0 = stay inside the pixels. Example value: 0.25d
     * @param verticalCurvature Vertical curvature of the CRT screen.
     * @param horizontalCurvature Horizontal curvature of the CRT screen.
     * @throws Exception Exceptions contain error texts.
     */
    public Terminal(int columns, int rows, int color, double scanLineBreadth, double fontThickness, double verticalCurvature, double horizontalCurvature) throws Exception {
        this.glfwInitialized = false;
        this.windowID = -1;
        this.pipeline = null;
        this.grid = null;
        this.font = null;

        try {
            if (!glfwInit()) {
                throw new Exception("Cannot init GLFW.");
            }

            /*
             * Create Window
             */
            long primaryMonitor = glfwGetPrimaryMonitor();
            GLFWVidMode mode = glfwGetVideoMode(primaryMonitor);
            glfwWindowHint(GLFW_STENCIL_BITS, 4);
            glfwWindowHint(GLFW_SAMPLES, 4);
            this.windowID = glfwCreateWindow(mode.width(), mode.height(), "Example OpenGL App", primaryMonitor, 0);
            glfwMakeContextCurrent(this.windowID);
            glfwShowWindow(this.windowID);

            /*
             * Initialize OpenGL
             */
            GL.createCapabilities();
            GL11.glEnable(GL13.GL_MULTISAMPLE);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            GL11.glFrustum(- 200, 200, - 150, 150, 10, 1000);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();

            //GL11.glScalef(0.9f, 1.15f, 1);
            GL11.glTranslatef(-200, -150, -12f);

            /*
                Create shader pipeline
             */
            this.pipeline = new Pipeline();
            this.pipeline.addShader("vertex_shader.txt",  GL20.GL_VERTEX_SHADER);
            this.pipeline.addShader("fragment_shader.txt",  GL20.GL_FRAGMENT_SHADER);
            this.pipeline.link();

            /*
                Create vertex arrays
             */
            grid = new Grid(400d, 300d, columns, rows, verticalCurvature, horizontalCurvature);

            /*
                Create texture
             */
            this.font = new Font(Kaypro_II_font.get(), color, scanLineBreadth, fontThickness);

        } catch (Exception ex) {
            this.close();
            throw(ex);
        }

        this.close();
    }

    /**
     * Close all OpenGL resources.
     */
    public void close() {
        if (this.font != null) {
            this.font.close();
            this.font = null;
        }

        if (this.grid != null) {
            this.grid.close();
            this.grid = null;
        }

        if (this.pipeline != null) {
            this.pipeline.close();
            this.pipeline = null;
        }

        if (this.windowID > -1) {
            glfwDestroyWindow(this.windowID);
            this.windowID = -1;
        }

        if (this.glfwInitialized) {
            glfwTerminate();
            this.glfwInitialized = false;
        }
    }

    /**
     * Render screen.
     */
    public void draw() {
        if (this.font == null) {
            return;
        }

        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL20.glUseProgram(this.pipeline.getProgramID());

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.font.getTextureID());

        this.grid.draw();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL20.glUseProgram(0);

        glfwSwapBuffers(this.windowID);
    }

    public long getWindowID() {
        return this.windowID;
    }
}
