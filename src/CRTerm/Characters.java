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
import java.nio.IntBuffer;
import java.util.Arrays;

import org.lwjgl.opengl.*;

import static org.lwjgl.opengl.GL20.glUseProgram;


class Characters {
    private int glProgramID;
    private int columns;
    private int rows;
    private int count;
    private char[] characters;
    private char[] rowBuffer;
    private IntBuffer intBuffer;
    private int uniformBufferID;

    /**
     *
     *
     * @param columns Horizontal character count.
     * @param rows Vertical character count.
     * @param glProgramID The ID of the shader program, which draws the characters.
     * @throws Exception The thrown exceptions contain error messages.
     */
    Characters(int columns, int rows, int glProgramID) throws Exception {
        this.columns = columns;
        this.rows = rows;
        this.count = columns * rows;
        this.glProgramID = glProgramID;
        this.characters = new char[this.count];
        this.rowBuffer = new char[this.columns];

        this.intBuffer = BufferUtils.createIntBuffer(this.count);

        for (int i = 0; i < this.count; i++) {
            this.intBuffer.put(i, (int)32);
            this.characters[i] = 32;
        }

        glUseProgram(glProgramID);

        this.uniformBufferID = GL20.glGetUniformLocation(glProgramID, "characters");

        if (this.uniformBufferID < 0) {
            throw new Exception("glGetUniformBlockIndex failed. (Characters)");
        }

        GL20.glUniform1iv(this.uniformBufferID, this.intBuffer);

        if (GL11.glGetError() != GL11.GL_NO_ERROR) {
            throw new Exception("glUniform1iv failed. (Characters)");
        }

        glUseProgram(0);
    }

    /**
     * Release OpenGL resources.
     */
    void close() {

    }

    /**
     * @return The ID of the OpenGL uniform buffer.
     */
    public int getUniformBufferID() {
        return this.uniformBufferID;
    }

    /**
     * @return Returns a reference of the internal array used for the updates. Modify it freely, then call updateCharacters().
     */
    char[] getArray() {
        return this.characters;
    }

    /**
     * Call this after modifying the char array to upload the changes to the GPU.
     */
    void uploadCharacters() {
        int size = Math.min(characters.length, this.count);
        int a, b, j;

        for (int i = 0; i < size; i++) {
            a = i % columns;
            b = rows - i / columns - 1;
            j = b * columns + a;

            this.intBuffer.put(j, (int)this.characters[i]);
        }

        glUseProgram(this.glProgramID);
        GL20.glUniform1iv(this.uniformBufferID, this.intBuffer);
        glUseProgram(0);
    }

    /**
     * Update the characters of the terminal.
     *
     * @param characters Starting at the top-left corner.
     */
    void setCharacters(char[] characters) {
        System.arraycopy(characters, 0, this.characters, 0, Math.min(characters.length, this.count));

        this.uploadCharacters();
    }

    /**
     * Update a character region of the terminal.
     *
     * @param x Starting position X-coordinate.
     * @param y Starting position Y-coordinate.
     * @param characters Output these characters, starting from the given (x, y) coordinates.
     */
    void setCharacters(int x, int y, char[] characters) {
        int destPos = y * this.columns + x;
        int space = this.count - destPos;

        System.arraycopy(characters, 0, this.characters, destPos, Math.min(characters.length, space));

        this.uploadCharacters();
    }

    /**
     * Scrolls the text upwards and leaves an empty line on the bottom.
     */
    void scrollUp() {
        System.arraycopy(this.characters, this.columns, this.characters, 0, this.count - this.columns);

        Arrays.fill(this.characters, this.count - this.columns, this.count - 1, (char)32);
    }

    /**
     * Rotates the text upwards. The first line becomes the last.
     */
    void rotateUp() {
        System.arraycopy(this.characters, 0, this.rowBuffer, 0, this.columns);
        System.arraycopy(this.characters, this.columns, this.characters, 0, this.count - this.columns);
        System.arraycopy(this.rowBuffer, 0, this.characters, this.count - this.columns, this.columns);
    }
}
