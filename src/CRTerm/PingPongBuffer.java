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

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import java.nio.*;


class PingPongBuffer {
    private int frontBufferIndex;  // 0, 1
    private int[] frameBuffers;
    private int[] colorBuffers;
    private int[] depthBuffers;
    private int vertexBuffer;

    PingPongBuffer(int width, int height) throws Exception {
        this.frameBuffers = new int[2];
        this.frameBuffers[0] = -1;
        this.frameBuffers[1] = -1;
        this.colorBuffers = new int[2];
        this.colorBuffers[0] = -1;
        this.colorBuffers[1] = -1;
        this.depthBuffers = new int[2];
        this.depthBuffers[0] = -1;
        this.depthBuffers[1] = -1;
        this.frontBufferIndex = 0;

        /*
            Create framebuffer
         */
        if (!GL.getCapabilities().GL_EXT_framebuffer_object) {
            throw new Exception("Your version of OpenGL doesn't support framebuffers.");
        }

        /*
            Create framebuffers
         */
        for(int i = 0; i < 2; i++) {
            this.frameBuffers[i] = GL30.glGenFramebuffers();
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.frameBuffers[i]);

            if (this.frameBuffers[i] < 1) {
                throw new Exception("Unable to create framebuffer.");
            }

            /*
                Create color buffer texture
             */
            this.colorBuffers[i] = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.colorBuffers[i]);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, this.colorBuffers[i], 0);

            if (GL11.glGetError() != GL11.GL_NO_ERROR) {
                throw new Exception("Creating texture failed. (PingPongBuffer)");
            }

            /*
                Create depth buffer texture
             */
            this.depthBuffers[i] = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.depthBuffers[i]);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, 0);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_FUNC, GL11.GL_LEQUAL);
            GL11.glTexParameteri (GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_MODE, GL11.GL_NONE);

            if (GL11.glGetError() != GL11.GL_NO_ERROR) {
                throw new Exception("Creating depth texture failed. (PingPongBuffer)");
            }

            int fboStatus = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
            if (fboStatus != GL30.GL_FRAMEBUFFER_COMPLETE) {
                throw new Exception("Frame buffer is not complete. (constructor)");
            }

            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        }

        /*
            Create vertex buffer for a full-screen quad
         */
        FloatBuffer vertex_data = BufferUtils.createFloatBuffer(20);
        vertex_data.put(new float[]{
                0.0f, 0.0f, 0.0f,                       0.0f, 0.0f,
                (float)width, 0.0f, 0.0f,              1.0f, 0.0f,
                (float)width, (float)height, 0.0f,      1.0f, 1.0f,
                0.0f, (float)height, 0.0f,               0.0f, 1.0f
        });
        vertex_data.flip();

        this.vertexBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vertexBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertex_data, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 20, 0);	// mark vertex coordinates
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 20, 12);	// mark texture coordinates

        GL30.glBindVertexArray(0);
    }

    /**
     * Release OpenGL resources
     */
    void close() {
        for(int i = 0; i < 2; i++) {
            if (this.colorBuffers[i] > -1) {
                GL30.glDeleteRenderbuffers(this.colorBuffers[i]);
                this.colorBuffers[i] = -1;
            }

            if (this.depthBuffers[i] > -1) {
                GL30.glDeleteRenderbuffers(this.depthBuffers[i]);
                this.depthBuffers[i] = -1;
            }
        }

        for(int i = 0; i < 2; i++) {
            if (this.frameBuffers[i] > -1) {
                GL30.glDeleteFramebuffers(this.frameBuffers[i]);
                this.frameBuffers[i] = -1;
            }
        }
    }

    /**
     * @return The ID of the texture in the front framebuffer.
     */
    int getFrontColorBufer() {
        return this.colorBuffers[this.frontBufferIndex];
    }

    /**
     * @return The ID of the texture in the back framebuffer.
     */
    int getBackColorBuffer() {
        return this.colorBuffers[1 - this.frontBufferIndex];
    }

    /**
     * Switches the front and the back framebuffer. Unbinds the old and binds the new. Makes the now back texture directly accessible.
     */
    void switchBuffers() {
        this.unBindFrameBuffer();
        this.frontBufferIndex = 1 - frontBufferIndex;
        this.bindFrameBuffer();
    }

    /**
     * Binds the front framebuffer. (After this the front texture should not be accessed.)
     */
    void bindFrameBuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.frameBuffers[this.frontBufferIndex]);
    }

    /**
     * Unbinds the front framebuffer. (After this the front texture should not be accessed.)
     */
    void unBindFrameBuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    /**
     * Draw one screen-sized quad.
     */
    void draw() {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vertexBuffer);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);

        GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    /**
     * Set up a screen-sized ortho projection for the rendering.
     *
     * @param windowWidth Width of the client area.
     * @param windowHeight Height of the client area.
     */
    void setupProjection(int windowWidth, int windowHeight) {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, windowWidth, 0, windowHeight, -1024, 1024);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
    }

    /**
     * Resizes the framebuffers.
     */
    void resize() {
        // todo
    }
}
