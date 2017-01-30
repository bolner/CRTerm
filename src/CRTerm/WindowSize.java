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

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.BufferUtils;
import java.nio.IntBuffer;


class WindowSize {
    private long windowID;
    private int width = 0;
    private int height = 0;

    private IntBuffer bufferGlQueryX = BufferUtils.createIntBuffer(1);
    private IntBuffer bufferGlQueryY = BufferUtils.createIntBuffer(1);

    WindowSize(long windowID) {
        this.windowID = windowID;

        this.checkSize();
    }

    /**
     * Refreshes the size of the client area.
     *
     * @return Returns true in case the client area has been resized.
     */
    boolean checkSize() {
        glfwGetWindowSize(this.windowID, this.bufferGlQueryX, this.bufferGlQueryY);
        int windowWidth = this.bufferGlQueryX.get(0);
        int windowHeight = this.bufferGlQueryY.get(0);

        boolean changed = false;

        if (windowWidth != this.width || windowHeight != this.height) {
            changed = true;
        }

        this.width = windowWidth;
        this.height = windowHeight;

        return changed;
    }

    /**
     * @return Width of client area in pixels.
     */
    int getWidth() {
        return this.width;
    }

    /**
     * @return Height of client area in pixels.
     */
    int getHeight() {
        return this.height;
    }
}
