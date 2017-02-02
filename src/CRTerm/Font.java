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


import org.lwjgl.opengl.*;
import java.nio.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.*;
import java.awt.image.BufferedImage;


class Font {
    private int textureID;

    /**
     * Create a Font as a 1024x512 texture atlas, with 16*8 = 128 characters.
     *
     * @param font 8x8 pixel data of characters. One byte is one horizontal line. 128 characters.
     * @param color RGB font color. Example: 0x70fe80
     * @param scanLineBreadth The CRT screen is constructed of vertical scan lines. This parameter tells their breadth or thickness. 1 = no spaces in between. Example value: 0.7d
     * @param fontThickness This parameter tells how much should the scan lines overreach the pixels of the characters. 0 = stay inside the pixels. Example value: 0.25d
     * @throws Exception Exceptions contain error texts.
     */
    Font(byte[] font, int color, double scanLineBreadth, double fontThickness) throws Exception {
        BufferedImage image = new BufferedImage(1024, 512, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = image.createGraphics();
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHints(rh);
        g2d.setColor(Color.black);
        g2d.clearRect(0, 0, 1024, 512);
        g2d.setColor(new Color(color));

        Ellipse2D.Double ellipse = new Ellipse2D.Double(0, 0, 0, 0);
        Rectangle2D.Double rectangle = new Rectangle2D.Double(0, 0, 0, 0);

        double dx = 6.4d;
        double dy = 6.4d;
        scanLineBreadth *= dy;
        fontThickness *= dx;

        double startx, lengthx, liney, step, i;
        int c, x, y, a, b, starta, enda;

        for (c = 0; c < 128; c++) {
            x = c % 16;
            y = c / 16;

            for(b = 0; b < 8; b++) {
                starta = -1;
                enda = -1;
                liney = y * 64.0d + (b + 1.5d) * dy;

                for(a = 0; a < 8; a++) {
                    if (starta == -1 && (font[c * 8 + b] & (128 >> a)) > 0) {
                        starta = a;

                        if (a == 7) {
                            enda = 7;
                        }
                    } else if (starta > -1 && (font[c * 8 + b] & (128 >> a)) == 0) {
                        enda = a - 1;
                    } else if (starta > -1 && a == 7) {
                        enda = 7;
                    }

                    if (starta > -1 && enda > -1) {
                        startx = x * 64.0d + (starta + 1.5d) * dx;
                        lengthx = (enda - starta) * dx;

                        // if fontThickness=0 then it should be a single circle

                        ellipse.setFrame(startx - scanLineBreadth / 2.0d - fontThickness, liney + (dy - scanLineBreadth) / 2.0d, scanLineBreadth, scanLineBreadth);
                        g2d.fill(ellipse);

                        ellipse.setFrame(startx + lengthx - scanLineBreadth / 2.0d + fontThickness, liney + (dy - scanLineBreadth) / 2.0d, scanLineBreadth, scanLineBreadth);
                        g2d.fill(ellipse);

                        rectangle.setFrame(
                            startx - fontThickness,
                            liney + (dy - scanLineBreadth) / 2.0d,
                            lengthx + fontThickness * 2.0d,
                            scanLineBreadth
                        );
                        g2d.fill(rectangle);

                        starta = - 1;
                        enda = - 1;
                    }
                }
            }
        }

        /*
            Create texture from the image
         */
        this.textureID = this.createTexture(image);
    }

    /**
     * Create an OpenGL texture and upload it to the GPU.
     *
     * @param image Source image.
     * @return OpenGL texture ID.
     * @throws Exception Exceptions contain error texts.
     */
    private int createTexture(BufferedImage image) throws Exception {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        int[] data = new int[width * height];
        for (int i = 0; i < width * height; i++) {
            int a = pixels[i] & 0xff000000;
            int r = (pixels[i] & 0xff0000) >> 16;
            int g = pixels[i] & 0xff00;
            int b = pixels[i] & 0xff;

            data[i] = a | b << 16 | g | r;
        }

        IntBuffer intBuffer1 = ByteBuffer.allocateDirect(data.length << 2).order(ByteOrder.nativeOrder()).asIntBuffer();
        intBuffer1.put(data).flip();

        int result = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, result);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, intBuffer1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        if (GL11.glGetError() != GL11.GL_NO_ERROR) {
            throw new Exception("Creating texture failed. (Font)");
        }

        return result;
    }

    /**
     * Release OpenGL resources.
     */
    void close() {
        GL11.glDeleteTextures(this.textureID);
    }

    /**
     * @return OpenGL texture ID.
     */
    int getTextureID() {
        return this.textureID;
    }
}
