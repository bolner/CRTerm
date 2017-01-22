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

    Terminal() throws Exception {
        this.glfwInitialized = false;
        this.windowID = -1;

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
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            GL11.glFrustum(- mode.width() / 2, mode.width() / 2, - mode.height() / 2, mode.height() / 2, 6, 1024);

            if (GL11.glGetError() != GL11.GL_NO_ERROR) {
                throw new Exception("Setting up perspective projection failed.");
            }

            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();

            GL11.glScalef(0.9f, 1.15f, 1);
            GL11.glTranslatef(-960, -540, -8f);

            GL11.glEnable(GL13.GL_MULTISAMPLE);



        } catch (Exception ex) {
            this.close();
            throw(ex);
        }
    }

    public void close() {
        if (this.windowID > -1) {
            glfwDestroyWindow(this.windowID);
            this.windowID = -1;
        }

        if (this.glfwInitialized) {
            glfwTerminate();
            this.glfwInitialized = false;
        }
    }
}
