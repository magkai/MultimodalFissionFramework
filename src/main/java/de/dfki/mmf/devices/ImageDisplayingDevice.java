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


import com.google.gson.JsonObject;
import de.dfki.mmf.input.worldmodel.WorldModel;

import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Magdalena Kaiser on 03.08.2016.
 */
/**
 * Represents an abstract ImageDisplayingDevice. Extend it and implement its run-method to use a specific ImageDisplayingDevice
 */
public abstract class ImageDisplayingDevice extends Device {
    //the location of the image
    protected URL resourceLocation;
    //store the images currently displayed in a list
    protected ArrayList<URL> currentDisplayedImages = new ArrayList<>();

    public ImageDisplayingDevice(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public URL getOutput() {
        return resourceLocation;
    }

    @Override
    public void setOutput(Object output) {
        this.resourceLocation = (URL) output;
    }

    @Override
    public void clearOutput() {
        resourceLocation = null;
    }

    /**
     * changes the visibility of the image -> if image is about to be displayed: change it to visible
     * @param currentImageLocation
     */
    public void changeVisibilityOfDisplayedImage(URL currentImageLocation) {
        for(JsonObject worldobject: WorldModel.getWorldProperties()) {
            if(worldobject.has("worldobjecttype") && worldobject.get("worldobjecttype").getAsString().equals("image")) {
                if(worldobject.has("resourceurl") && worldobject.get("resourceurl").getAsString().equals(currentImageLocation.toString())) {
                    if(worldobject.has("isinvisible")) {
                        if(worldobject.get("isinvisible").getAsBoolean()) {
                            worldobject.addProperty("isinvisible", false);
                        }else{
                            worldobject.addProperty("isinvisible", true);
                        }
                    }
                }
            }
        }
    }

    public abstract void run();

    public abstract double getDurationEstimation(Object object);


    public ArrayList<URL> getCurrentDisplayedImages() {
        return currentDisplayedImages;
    }

    public void setCurrentDisplayedImages(ArrayList<URL> currentDisplayedImages) {
        this.currentDisplayedImages = currentDisplayedImages;
    }

    public void addImageToCurrentDisplayedImages(URL imageURL) {
        this.currentDisplayedImages.add(imageURL);
    }

    public void removeImageFromCurrentDisplayedImages(URL imageURL) {
        this.currentDisplayedImages.remove(imageURL);
    }

    public void clearAllDisplayedImages() {
        this.currentDisplayedImages.clear();
    }

}
