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
    private int characterBuffer;

    private int count;
    private static String testText = "Sed ut perspiciatis, unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam eaque ipsa, quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt, explicabo. Nemo enim ipsam voluptatem, quia voluptas sit, aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos, qui ratione voluptatem sequi nesciunt, neque porro quisquam est, qui dolorem ipsum, quia dolor sit amet consectetur adipiscing velit, sed quia non numquam do eius modi tempora incididunt, ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit, qui in ea voluptate velit esse, quam nihil molestiae consequatur, vel illum, qui dolorem eum fugiat, quo voluptas nulla pariatur? At vero eos et accusamus et iusto odio dignissimos ducimus, qui blanditiis praesentium voluptatum deleniti atque corrupti, quos dolores et quas molestias excepturi sint, obcaecati cupiditate non provident, similique sunt in culpa, qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio, cumque nihil impedit, quo minus id, quod maxime placeat, facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet, ut et voluptates repudiandae sint et molestiae non recusandae. Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat...";

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
        FloatBuffer vertex_data = BufferUtils.createFloatBuffer((int)(this.count * 24));
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
                        left, top,      ((float)Math.sin(left * screen_pi_width) * 0.6f + (float)Math.sin(top * screen_pi_height) * 0.4f),		0, 0, number,
                        left, bottom,   ((float)Math.sin(left * screen_pi_width) * 0.6f + (float)Math.sin(bottom * screen_pi_height) * 0.4f),	    0, 1, number,
                        right, bottom,  ((float)Math.sin(right * screen_pi_width) * 0.6f + (float)Math.sin(bottom * screen_pi_height) * 0.4f),	    1, 1, number,
                        right, top,     ((float)Math.sin(right * screen_pi_width) * 0.6f + (float)Math.sin(top * screen_pi_height) * 0.4f),		1, 0, number
                });
            }
        }

        vertex_data.flip();

        /*
            Character data
         */
        int a, b, j;
        FloatBuffer char_data = BufferUtils.createFloatBuffer(this.count * 4);

        for (int i = 0; i < this.count * 4; i++) {
            char_data.put(i, (float)32);
        }

        for (int i = 0; i < Grid.testText.length(); i++) {
            a = i % columns;
            b = rows - i / columns - 1;
            j = b * columns + a;

            char_data.put(j * 4, (float)Grid.testText.charAt(i));
            char_data.put(j * 4 + 1, (float)Grid.testText.charAt(i));
            char_data.put(j * 4 + 2, (float)Grid.testText.charAt(i));
            char_data.put(j * 4 + 3, (float)Grid.testText.charAt(i));
        }

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

        /*
            Character buffer VBO
         */
        this.characterBuffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.characterBuffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, char_data, GL15.GL_DYNAMIC_DRAW);

        GL20.glVertexAttribPointer(3, 1, GL11.GL_FLOAT, false, 4, 0);	// mark vertex coordinates

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
        GL20.glEnableVertexAttribArray(3);

        GL11.glDrawArrays(GL11.GL_QUADS, 0, this.count * 4);

        GL30.glBindVertexArray(0);
    }

    /**
     * Release OpenGL resources.
     */
    void close() {
        GL15.glDeleteBuffers(this.vertexBuffer);
        GL15.glDeleteBuffers(this.characterBuffer);
    }
}
