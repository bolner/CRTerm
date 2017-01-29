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
    private Characters characters;
    private long lastFrameTime = System.nanoTime();

    /**
     * Initialize OpenGL, the resources, and go fullscreen.
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
        this.characters = null;

        try {
            if (columns * rows > 4096) {
                throw new Exception("The number of characters cannot be more than 4096. Please change the 'columns' or the 'rows' parameter.");
            }

            if (!glfwInit()) {
                throw new Exception("Cannot init GLFW.");
            }

            /*
             * Create Window
             */
            long primaryMonitor = glfwGetPrimaryMonitor();
            GLFWVidMode mode = glfwGetVideoMode(primaryMonitor);
            int width = mode.width();
            int height = mode.height();
            glfwWindowHint(GLFW_STENCIL_BITS, 4);
            glfwWindowHint(GLFW_SAMPLES, 4);
            this.windowID = glfwCreateWindow(width, height, "Example OpenGL App", primaryMonitor, 0);
            glfwMakeContextCurrent(this.windowID);
            glfwShowWindow(this.windowID);

            /*
             * Initialize OpenGL
             */
            GL.createCapabilities();
            GL11.glEnable(GL13.GL_MULTISAMPLE);
            this.setupProjection(width, height);

            /*
                Create shader pipeline
             */
            this.pipeline = new Pipeline();
            this.pipeline.bindAttribLocation(0, "in_Position");
            this.pipeline.bindAttribLocation(1, "in_TextureCoord");
            this.pipeline.bindAttribLocation(2, "in_Number");
            this.pipeline.addShader("grid_vertex_shader.vert",  GL20.GL_VERTEX_SHADER);
            this.pipeline.addShader("grid_fragment_shader.frag",  GL20.GL_FRAGMENT_SHADER);
            this.pipeline.link();

            /*
                Uniform buffer for the character data.
             */
            this.characters = new Characters(columns, rows, this.pipeline.getProgramID());

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

        if (this.characters != null) {
            this.characters.close();
            this.characters = null;
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
    public void renderFrame() throws Exception {
        if (this.font == null) {
            throw new Exception("renderFrame() was called on a closed Terminal instance.");
        }

        // check for client are size changes (resize)

        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL20.glUseProgram(this.pipeline.getProgramID());

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.font.getTextureID());

        this.grid.draw();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL20.glUseProgram(0);
    }

    /**
     * Returns the LWJGL GLFW Window ID, which can be used for calling LWJGL/GLFW functions.
     * @return GLFW Window ID
     */
    public long getWindowID() {
        return this.windowID;
    }

    /**
     * Set up the projection matrix.
     *
     * @param windowWidth The width of the screen.
     * @param windowHeight The height of the screen.
     */
    private void setupProjection(double windowWidth, double windowHeight) {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        double ratio = windowHeight / windowWidth;
        GL11.glFrustum(- 200d, 200d, - 200d * ratio, 200d * ratio, 10, 1000);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        double magnify = 1d;

        if (ratio < 1) {
            magnify = ratio * 1.3d;
        }

        GL11.glScaled(1.2d * magnify, 1.2d * magnify, 1);
        GL11.glTranslated(-200d, -150d, -14d);
    }

    /**
     * Makes the current thread sleep to maintain the given frame rate.
     *
     * @param FPS A frame rate in frame/seconds to be maintained.
     * @throws InterruptedException Thrown if any thread has interrupted the current thread.
     */
    public void keepFPS(long FPS) throws InterruptedException {
        long frameTime = 1000000000L / FPS; // nanoseconds

        while (System.nanoTime() - this.lastFrameTime < frameTime) {
            Thread.sleep(1L);
        }

        this.lastFrameTime = System.nanoTime();
    }

    /**
     * @return Returns a reference for the internal character array, which can be used to directly manipulate the character data. Call uploadCharacterArray() after the modifications.
     */
    public char[] getCharacterArray() {
        return this.characters.getArray();
    }

    /**
     * Call this after modifying the char array. It uploads the changes to the GPU.
     */
    public void uploadCharacterArray() {
        this.characters.uploadCharacters();
    }

    /**
     * Update the characters of the terminal. The size of the given array can be arbitrary.
     *
     * @param characters Starting at the top-left corner.
     */
    public void setCharacters(char[] characters) {
        this.characters.setCharacters(characters);
    }

    /**
     * Update a character region of the terminal.
     *
     * @param x Starting position X-coordinate. The top-left corner is (0, 0).
     * @param y Starting position Y-coordinate. The top-left corner is (0, 0).
     * @param characters Start writing these characters out, starting from the given (x, y) coordinates.
     */
    public void setCharacters(int x, int y, char[] characters) {
        this.characters.setCharacters(x, y, characters);
    }

    /**
     * Scrolls the text upwards and leaves an empty line on the bottom.
     */
    public void scrollUp() {
        this.characters.scrollUp();
    }

    /**
     * Rotates the text upwards. The first line becomes the last.
     */
    public void rotateUp() {
        this.characters.rotateUp();
    }
}
