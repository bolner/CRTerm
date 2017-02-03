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
    private int columns;
    private int rows;
    private boolean glfwInitialized;
    private long windowID;
    private Pipeline fontPipeline;
    private Pipeline mixPipeline;
    private Grid grid;
    private Font font;
    private Characters characters;
    private long lastFrameTime = System.nanoTime();
    private PingPongBuffer pingPongBuffer;
    private Pipeline bloomPipeline;
    private WindowSize windowSize;
    private PingPongBuffer mixBuffer;
    private int uniform_dimensions;
    private int uniform_scanlinePosition;
    private float scanlinePosition = 0;
    private int uniform_gaussianDirection = 0;
    private int uniform_mixAttenuation = 0;

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
        this.columns = columns;
        this.rows = rows;
        this.glfwInitialized = false;
        this.windowID = -1;
        this.fontPipeline = null;
        this.mixPipeline = null;
        this.grid = null;
        this.font = null;
        this.characters = null;
        this.pingPongBuffer = null;
        this.bloomPipeline = null;
        this.windowSize = null;
        this.mixBuffer = null;

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
        glfwWindowHint(GLFW_RESIZABLE, 1);
        glfwWindowHint(GLFW_STENCIL_BITS, 4);
        glfwWindowHint(GLFW_SAMPLES, 4);
        this.windowID = glfwCreateWindow(width, height, "Example OpenGL App", primaryMonitor, 0);
        glfwMakeContextCurrent(this.windowID);
        glfwShowWindow(this.windowID);
        this.windowSize = new WindowSize(this.windowID);

        /*
         * Initialize OpenGL
         */
        GL.createCapabilities();
        GL11.glEnable(GL13.GL_MULTISAMPLE);

        /*
            Create post-processing buffers
         */
        this.pingPongBuffer = new PingPongBuffer(width, height);
        this.mixBuffer = new PingPongBuffer(width, height);

        /*
            Create font-rendering shader pipeline.
         */
        this.fontPipeline = new Pipeline();
        this.fontPipeline.bindAttribLocation(0, "in_Position");
        this.fontPipeline.bindAttribLocation(1, "in_TextureCoord");
        this.fontPipeline.bindAttribLocation(2, "in_Number");
        this.fontPipeline.addShader("grid_vertex_shader.vert",  GL20.GL_VERTEX_SHADER);
        this.fontPipeline.addShader("character_fragment_shader.frag",  GL20.GL_FRAGMENT_SHADER);
        this.fontPipeline.link();

        this.uniform_dimensions = GL20.glGetUniformLocation(this.fontPipeline.getProgramID(), "dimensions");
        this.uniform_scanlinePosition = GL20.glGetUniformLocation(this.fontPipeline.getProgramID(), "scanlinePosition");

        /*
            Uniform buffer for the character data.
         */
        this.characters = new Characters(columns, rows, this.fontPipeline.getProgramID());

        /*
            Create bloom post-processing shader pipeline.
         */
        this.bloomPipeline = new Pipeline();
        this.bloomPipeline.bindAttribLocation(0, "in_Position");
        this.bloomPipeline.bindAttribLocation(1, "in_TextureCoord");
        this.bloomPipeline.addShader("default_vertex_shader.vert",  GL20.GL_VERTEX_SHADER);
        this.bloomPipeline.addShader("gaussian_fragment_shader.frag",  GL20.GL_FRAGMENT_SHADER);
        this.bloomPipeline.link();

        this.uniform_gaussianDirection = GL20.glGetUniformLocation(this.bloomPipeline.getProgramID(), "in_direction");

        /*
            Texture bender shader pipeline
         */
        this.mixPipeline = new Pipeline();
        this.mixPipeline.bindAttribLocation(0, "in_Position");
        this.mixPipeline.bindAttribLocation(1, "in_TextureCoord");
        this.mixPipeline.addShader("default_vertex_shader.vert",  GL20.GL_VERTEX_SHADER);
        this.mixPipeline.addShader("mix_fragment_shader.frag",  GL20.GL_FRAGMENT_SHADER);
        this.mixPipeline.link();

        int image1 = GL20.glGetUniformLocation(this.mixPipeline.getProgramID(), "image1");
        int image2  = GL20.glGetUniformLocation(this.mixPipeline.getProgramID(), "image2");
        this.uniform_mixAttenuation = GL20.glGetUniformLocation(this.mixPipeline.getProgramID(), "in_attenuation");

        GL20.glUseProgram(this.mixPipeline.getProgramID());
        GL20.glUniform1i(image1, 0);
        GL20.glUniform1i(image2,  1);
        GL20.glUseProgram(0);

        /*
            Create vertex arrays for the screen curvature.
         */
        grid = new Grid(400d, 300d, columns, rows, verticalCurvature, horizontalCurvature);

        /*
            Create texture atlas for the Font
         */
        this.font = new Font(Kaypro_II_font.get(), color, scanLineBreadth, fontThickness);
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

        if (this.fontPipeline != null) {
            this.fontPipeline.close();
            this.fontPipeline = null;
        }

        if (this.bloomPipeline != null) {
            this.bloomPipeline.close();
            this.bloomPipeline = null;
        }

        if (this.mixPipeline != null) {
            this.mixPipeline.close();
            this.mixPipeline = null;
        }

        if (this.pingPongBuffer != null) {
            this.pingPongBuffer.close();
            this.pingPongBuffer = null;
        }

        if (this.mixBuffer != null) {
            this.mixBuffer.close();
            this.mixBuffer = null;
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

        boolean resized = this.windowSize.checkSize();

        int width = this.windowSize.getWidth();
        int height = this.windowSize.getHeight();

        // Skip render if the client area is too small
        if (width < 10 || height < 10) {
            return;
        }

        if (resized) {
            /*
                The size of the client area has changed, the framebuffers need to get resized.
             */
            this.pingPongBuffer.resize(width, height);
            this.mixBuffer.resize(width, height);
            GL11.glViewport(0, 0, width, height);

            Thread.sleep(100);
            return;
        }

        this.scanlinePosition += 0.1f;
        if (this.scanlinePosition > this.rows) {
            this.scanlinePosition = 0;
        }

        /*
            Render text to framebuffer object
         */
        this.pingPongBuffer.bindFrameBuffer();
        this.grid.setupProjection(width, height);

        GL20.glUseProgram(this.fontPipeline.getProgramID());
        {
            GL20.glUniform2f(this.uniform_dimensions, (float)this.columns, (float)this.rows);
            GL20.glUniform1f(this.uniform_scanlinePosition,  this.scanlinePosition);

            GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.font.getTextureID());

            this.grid.draw();

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }
        GL20.glUseProgram(0);

        /*
            Fade out
         */
        this.mixBuffer.bindFrameBuffer();
        this.mixBuffer.setupProjection(width, height);

        GL20.glUseProgram(this.mixPipeline.getProgramID());
        {
            GL20.glUniform1f(this.uniform_mixAttenuation,  0.7f);

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mixBuffer.getBackTexture());

            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.pingPongBuffer.getFrontTexture());

            this.mixBuffer.draw();

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }
        GL20.glUseProgram(0);

        this.mixBuffer.unBindFrameBuffer();

        /*
            Bloom 1: horizontal blur
         */
        this.pingPongBuffer.bindFrameBuffer();

        GL20.glUseProgram(this.bloomPipeline.getProgramID());
        {
            GL20.glUniform2f(this.uniform_gaussianDirection, 1.0f, 0.0f);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mixBuffer.getFrontTexture());

            this.pingPongBuffer.draw();

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }
        GL20.glUseProgram(0);

        this.pingPongBuffer.unBindFrameBuffer();

        /*
            Bloom 2: vertical blur
         */
        this.pingPongBuffer.switchBuffers();
        this.pingPongBuffer.bindFrameBuffer();

        GL20.glUseProgram(this.bloomPipeline.getProgramID());
        {
            GL20.glUniform2f(this.uniform_gaussianDirection, 0.0f, 1.0f);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.pingPongBuffer.getBackTexture());

            this.pingPongBuffer.draw();

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }
        GL20.glUseProgram(0);

        this.pingPongBuffer.unBindFrameBuffer();

        /*
            Bloom 3: Mix the blurred result with the original
                        (Draw to screen)
         */
        GL20.glUseProgram(this.mixPipeline.getProgramID());
        {
            GL20.glUniform1f(this.uniform_mixAttenuation,  0.8f);

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.pingPongBuffer.getFrontTexture());

            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.mixBuffer.getFrontTexture());

            this.pingPongBuffer.draw();

            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }
        GL20.glUseProgram(0);

        this.mixBuffer.switchBuffers();
    }

    /**
     * Returns the LWJGL GLFW Window ID, which can be used for calling LWJGL/GLFW functions.
     * @return GLFW Window ID
     */
    public long getWindowID() {
        return this.windowID;
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
