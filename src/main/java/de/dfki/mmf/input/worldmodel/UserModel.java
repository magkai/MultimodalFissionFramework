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
import de.dfki.mmf.math.Position;

/**
 * Created by Magdalena Kaiser on 23.08.2016.
 */
/**
 * Defines a model for the user.
 * Contains the user's properties defined in the world model
 */
public class UserModel {

    private JsonObject userProperties;

    public JsonObject getUserProperties() {
        return userProperties;
    }

    public void setUserProperties(JsonObject userProperties) {
        this.userProperties = userProperties;
    }

    public String getUserName() {
        if(userProperties == null) {
            return null;
        }
        if(userProperties.has("name")) {
            return userProperties.get("name").getAsString();
        }else {
            System.out.println("Warning: No user name was set.");
            return null;
        }
    }

    public String getUserId() {
        if(userProperties == null) {
            return null;
        }
        if(userProperties.has("worldobjectid")) {
            return userProperties.get("worldobjectid").getAsString();
        }else {
            throw new IllegalStateException("No worldobjectid for user set, id is required.");
        }
    }

    public Position getUserPosition() {
        if(userProperties == null) {
            return null;
        }
        if(userProperties.has("xposition") && userProperties.has("yposition") && userProperties.has("zposition")) {
            double xPos = userProperties.get("xposition").getAsDouble();
            double yPos = userProperties.get("yposition").getAsDouble();
            double zPos = userProperties.get("zposition").getAsDouble();
            return new Position(xPos, yPos, zPos);
        }else if(userProperties.has("position")) {
            JsonObject innerObject = userProperties.get("position").getAsJsonObject();
            if (innerObject.has("xposition") && innerObject.has("yposition") && innerObject.has("zposition")) {
                double xPos = innerObject.get("xposition").getAsDouble();
                double yPos = innerObject.get("yposition").getAsDouble();
                double zPos = innerObject.get("zposition").getAsDouble();
                return new Position(xPos, yPos, zPos);
            }
            return null;
        }else {
            System.out.println("No position for user set, position is required.");
            return null;
        }
    }


}
