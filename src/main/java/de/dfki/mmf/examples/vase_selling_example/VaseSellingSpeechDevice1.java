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

import de.dfki.mmf.devices.SpeechDevice;

/**
 * Created by Magdalena Kaiser on 20.12.2016.
 */

/**
 * A Speech Device containing a run-method ready to execute the output on a physical device
 */
public class VaseSellingSpeechDevice1 extends SpeechDevice {

    public VaseSellingSpeechDevice1(String deviceId) {
        super(deviceId);
    }

    @Override
    public void run() {
        //fill in some concrete execution commands for executing the output on a physical device
        System.out.println(deviceName + ": " + "\"" + output + "\"");
    }

    @Override
    public double getDurationEstimation(Object output) {
        //fill in some estimation of the duration which is needed to execute the given output on this device
        return 0.0;
    }
}
