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

package de.dfki.mmf.input.worldmodel;

import com.google.gson.JsonObject;
import de.dfki.mmf.devices.Device;
import de.dfki.mmf.math.Position;
import de.dfki.mmf.modalities.Modality;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Magdalena Kaiser on 23.08.2016.
 */

/**
 * Defines a model for the used robot.
 * Contains the robot's modalities and devices as well as its properties defined in the world model
 */
public class RobotModel {

    private List<Modality> modalities = new ArrayList<>();
    private List<Device> devices = new ArrayList<>();
    private JsonObject robotProperties;

    public List<Modality> getModalities() {
        return modalities;
    }

    public void setModalities(List<Modality> modalities) {
        this.modalities = modalities;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public JsonObject getRobotProperties() {
        return robotProperties;
    }

    public void setRobotProperties(JsonObject robotProperties) {
        this.robotProperties = robotProperties;
    }


    public String getRobotName() {
        if(robotProperties == null) {
            return null;
        }
        if(robotProperties.has("name")) {
            return robotProperties.get("name").getAsString();
        }else {
            return null;
        }
    }

    public String getRobotId() {
        if(robotProperties == null) {
            return null;
        }
        if(robotProperties.has("worldobjectid")) {
            return robotProperties.get("worldobjectid").getAsString();
        }else {
            throw new IllegalStateException("No worldobjectid for robot set, id is required.");
        }
    }

    public Position getRobotPosition() {
        if(robotProperties == null) {
            return null;
        }
        if(robotProperties.has("xposition") && robotProperties.has("yposition") && robotProperties.has("zposition")) {
            double xPos = robotProperties.get("xposition").getAsDouble();
            double yPos = robotProperties.get("yposition").getAsDouble();
            double zPos = robotProperties.get("zposition").getAsDouble();
            return new Position(xPos, yPos, zPos);
        }else if(robotProperties.has("position")) {
            JsonObject innerObject = robotProperties.get("position").getAsJsonObject();
            if (innerObject.has("xposition") && innerObject.has("yposition") && innerObject.has("zposition")) {
                double xPos = innerObject.get("xposition").getAsDouble();
                double yPos = innerObject.get("yposition").getAsDouble();
                double zPos = innerObject.get("zposition").getAsDouble();
                return new Position(xPos, yPos, zPos);
            }
            return null;
        }else {
            System.out.println("No position for robot set, position is required.");
            return null;
        }
    }


}
