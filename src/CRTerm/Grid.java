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

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;


class Grid {
    private int bufferGroup;
    private int vertexBuffer;
    private int count;

    /**
     * Creates a character grid.
     *
     * @param width Width fo the grid.
     * @param height Height of the grid.
     * @param columns Number of character horizontally.
     * @param rows Number of characters vertically.
     * @param verticalCurvature Vertical curvature of the CRT screen.
     * @param horizontalCurvature Horizontal curvator of the CRT screen.
     */
    Grid(double width, double height, int columns, int rows, double verticalCurvature, double horizontalCurvature) {
        this.count = columns * rows;
        double sizeX = width / ((double)columns);
        double sizeY = height / ((double)rows);

        /*
            Vertex data
         */
        FloatBuffer vertex_data = BufferUtils.createFloatBuffer(this.count * 24);
        float left, right, top, bottom, number;

        double screen_pi_width = Math.PI / width;
        double screen_pi_height = Math.PI / height;

        for(double j = 0; j < rows; j++) {
            for(double i = 0; i < columns; i++) {
                number = (float)(j * columns + i);

                left = (float)(sizeX * i);
                right = (float)(left + sizeX);
                bottom = (float)(sizeY * j);
                top = (float)(bottom + sizeY);

                vertex_data.put(new float[] {
                        left, top,      (float)(Math.sin(left * screen_pi_width) * horizontalCurvature + Math.sin(top * screen_pi_height) * verticalCurvature),		0, 0, number,
                        left, bottom,   (float)(Math.sin(left * screen_pi_width) * horizontalCurvature + Math.sin(bottom * screen_pi_height) * verticalCurvature),	    0, 1, number,
                        right, bottom,  (float)(Math.sin(right * screen_pi_width) * horizontalCurvature + Math.sin(bottom * screen_pi_height) * verticalCurvature),	    1, 1, number,
                        right, top,     (float)(Math.sin(right * screen_pi_width) * horizontalCurvature + Math.sin(top * screen_pi_height) * verticalCurvature),		1, 0, number
                });
            }
        }

        vertex_data.flip();

        /*
            VAO
         */
        this.bufferGroup = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(this.bufferGroup);

        /*
            Vertex buffer VBO
         */
        this.vertexBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.vertexBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertex_data, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 24, 0);	// mark vertex coordinates
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 24, 12);	// mark texture coordinates
        GL20.glVertexAttribPointer(2, 1, GL11.GL_FLOAT, false, 24, 20);	// mark quad identifiers

        GL30.glBindVertexArray(0);
    }

    /**
     * Draw the screen.
     */
    void draw() {
        GL30.glBindVertexArray(this.bufferGroup);
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);

        GL11.glDrawArrays(GL11.GL_QUADS, 0, this.count * 4);

        GL30.glBindVertexArray(0);
    }

    /**
     * Release OpenGL resources.
     */
    void close() {
        GL15.glDeleteBuffers(this.vertexBuffer);
    }

    /**
     * Set up the projection matrix for the grid.
     *
     * @param windowWidth The width of the screen.
     * @param windowHeight The height of the screen.
     */
    void setupProjection(double windowWidth, double windowHeight) {
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
}
