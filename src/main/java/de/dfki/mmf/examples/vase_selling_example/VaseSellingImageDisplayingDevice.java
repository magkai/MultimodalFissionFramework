/*
 * MIT License
 *
 * Copyright (c) 2017 Magdalena Kaiser
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.dfki.mmf.examples.vase_selling_example;

import de.dfki.mmf.devices.ImageDisplayingDevice;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

/**
 * Created by Magdalena Kaiser on 20.12.2016.
 */

/**
 * An Image Displaying Device containing a run-method ready to execute the output on a physical device
 */
public class VaseSellingImageDisplayingDevice extends ImageDisplayingDevice {

    public VaseSellingImageDisplayingDevice(String deviceId) {
        super(deviceId);
    }

    @Override
    public void run() {
        System.out.println(deviceName + ": " + resourceLocation);
        BufferedImage image = null;
        try {
            URLConnection conn = resourceLocation.openConnection();
            InputStream in = conn.getInputStream();
            image = ImageIO.read(in);
        } catch (IOException e) {
            new RuntimeException("Cannot display image. Reason: " + e.getMessage());
        }
        addImageToCurrentDisplayedImages(resourceLocation);
        changeVisibilityOfDisplayedImage(resourceLocation);
        JFrame frame = new JFrame();
        frame.setSize(image.getHeight()*2, image.getWidth()*2);
        JLabel label = new JLabel(new ImageIcon(image));
        frame.add(label);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }

    @Override
    public double getDurationEstimation(Object object) {
        //fill in some estimation of the duration which is needed to execute the given output on this device
        return 0.0;
    }

}
