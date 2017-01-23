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

package TestApp;

import CRTerm.Terminal;
import java.util.concurrent.TimeUnit;
import static org.lwjgl.glfw.GLFW.*;


public class Main {
    public static void main(String[] args) {
        Terminal term = null;

        try {
            term = new Terminal(80, 25, 0x00FF00, 0.7d, 0.25d, 0.8d, 0.7d);

            while(true) {
                /*
                    Render frame
                 */
                term.draw();

                /*
                    Handle events
                 */
                glfwPollEvents();
                if (glfwWindowShouldClose(term.getWindowID())) {
                    break;
                }

                TimeUnit.MILLISECONDS.sleep(5);
            }

            term.draw();


        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            if (term != null) {
                term.close();
            }
            System.exit(-1);
        }

        term.close();
    }
}
