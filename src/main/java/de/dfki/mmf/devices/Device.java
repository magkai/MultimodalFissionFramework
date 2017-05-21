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

import de.dfki.mmf.math.DirectionVector;
import de.dfki.mmf.math.Position;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Magdalena on 13.06.2016.
 */

/**
 * Represents the topmost Device in the abstract device hierarchy. All other devices inherit from this class
 */
public abstract class Device implements Runnable {

    protected static AtomicLong idCounter = new AtomicLong();

    protected String deviceName;
    protected Position devicePosition;
    //describes the direction of the device
    protected DirectionVector directionVector;
    String deviceId;

    public static String createDeviceID()
    {
        return String.valueOf(idCounter.getAndIncrement());
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Position getDevicePosition() {
        return this.devicePosition;
    }

    public void setDevicePosition(Position position) {
        this.devicePosition = position;
    }

    public DirectionVector getDirectionVector() {
        return this.directionVector;
    }

    public void setDirectionVector(DirectionVector directionVector) {
        this.directionVector = directionVector;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    @Override
    public String toString() {
        if(deviceName != null) {
            return deviceName;
        }else {
            return "Device"+deviceId;
        }
    }

    public abstract Object getOutput();

    public abstract void setOutput(Object output);

    public abstract void clearOutput();

    public abstract double getDurationEstimation(Object object);

}