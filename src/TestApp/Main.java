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

import CRTerm.*;
import static org.lwjgl.glfw.GLFW.*;


public class Main {
    private static String testText = "Sed ut perspiciatis, unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam eaque ipsa, quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt, explicabo. Nemo enim ipsam voluptatem, quia voluptas sit, aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos, qui ratione voluptatem sequi nesciunt, neque porro quisquam est, qui dolorem ipsum, quia dolor sit amet consectetur adipiscing velit, sed quia non numquam do eius modi tempora incididunt, ut labore et dolore magnam aliquam quaerat voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure reprehenderit, qui in ea voluptate velit esse, quam nihil molestiae consequatur, vel illum, qui dolorem eum fugiat, quo voluptas nulla pariatur? At vero eos et accusamus et iusto odio dignissimos ducimus, qui blanditiis praesentium voluptatum deleniti atque corrupti, quos dolores et quas molestias excepturi sint, obcaecati cupiditate non provident, similique sunt in culpa, qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio, cumque nihil impedit, quo minus id, quod maxime placeat, facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet, ut et voluptates repudiandae sint et molestiae non recusandae. Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat...";

    public static void main(String[] args) {
        Terminal term = null;

        try {
            term = new Terminal(80, 25, 0x00FF00, 0.6d, 0.25d, 0.8d, 1.9d);
            term.setCharacters(Main.testText.toCharArray());
            term.uploadCharacterArray();

            long counter = 0;

            while(true) {
                term.renderFrame();

                term.keepFPS(60);   // This should be called directly before the glfwSwapBuffers function.
                glfwSwapBuffers(term.getWindowID());

                /*
                    Handle events
                 */
                glfwPollEvents();
                if (glfwWindowShouldClose(term.getWindowID())) {
                    break;
                }

                /*
                    Animation (rotate the text upwards)
                 */
                counter++;
                if (counter % 50 == 0) {
                    term.rotateUp();
                    term.uploadCharacterArray();
                }
            }
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
