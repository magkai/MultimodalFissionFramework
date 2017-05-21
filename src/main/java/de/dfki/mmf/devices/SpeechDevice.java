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

package de.dfki.mmf.devices;

/**
 * Created by Magdalena Kaiser on 30.06.2016.
 */
/**
 * Represents an abstract SpeechDevice. Extend it and implement its run-method to use a specific SpeechDevice
 */
public abstract class SpeechDevice extends Device {

    protected String output;

    public SpeechDevice(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String getOutput() {
        return output;
    }

    @Override
    public void setOutput(Object output) {
        this.output = (String) output;
    }

    @Override
    public void clearOutput() {
        output = null;
    }

    public abstract void run();

    public abstract double getDurationEstimation(Object output);
}
