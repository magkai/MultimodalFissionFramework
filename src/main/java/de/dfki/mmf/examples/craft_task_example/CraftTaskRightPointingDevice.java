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

package de.dfki.mmf.examples.craft_task_example;

import de.dfki.mmf.devices.PointingDevice;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * Created by Magdalena Kaiser on 27.08.2016.
 */

/**
 * Pointing Device with concrete run-method for executing desired output on the device
 */
public class CraftTaskRightPointingDevice extends PointingDevice {

    public CraftTaskRightPointingDevice(String deviceId) {
        super(deviceId);
    }

    @Override
    public void run() {
        System.out.println(deviceName + ": " + outputPosition.toString());
        Process p = null;
        try {
            URL resource = this.getClass().getClassLoader().getResource("ExampleResources/CraftTaskExample/NaoPointing.py");
            String absolutePath = "";
            //call python execution code
            try {
                File file = Paths.get(resource.toURI()).toFile();
                absolutePath = file.getAbsolutePath();
                p = Runtime.getRuntime().exec(new String[] {"python", absolutePath,
                        "--arm", "RArm", "--xposition", outputPosition.getX()+"", "--yposition", outputPosition.getY()+"", "--zposition", outputPosition.getZ()+""});

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public double getDurationEstimation(Object output) {
        //fill in some estimation of the duration which is needed to execute the given output on this device
        return 0.0;
    }
}
