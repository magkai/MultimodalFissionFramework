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

package de.dfki.mmf.output;


import de.dfki.mmf.devices.Device;
import de.dfki.mmf.modalities.Modality;

/**
 * Created by Magdalena Kaiser on 29.06.2016.
 */

/**
 * Component containing a triple of <Modality, Output, Device>
 */
public class PlanComponent {

    private Modality modality;
    private Object output;
    private Device device;
    private int executionTime;

    public PlanComponent(Modality modality, Object output, Device device) {
        this.modality = modality;
        this.output = output;
        this.device = device;
    }

    public Modality getModality() {
        return modality;
    }

    public Object getOutput() {
        return output;
    }

    public Device getDevice() {
        return device;
    }
    
    public void setOutput(Object out) {
        this.output = out;
    }

    public int getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(int executionTime) {
        this.executionTime = executionTime;
    }

    @Override
    public String toString() {
        return "(" + modality.toString() + "," + device.toString() + "," + output.toString() + ")";
    }

}
