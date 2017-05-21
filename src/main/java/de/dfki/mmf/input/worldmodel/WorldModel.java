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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Magdalena Kaiser on 23.08.2016.
 */
/**
 * Defines a model for the interaction environment (= the world).
 * Contains the submodels for users and the robot and the visibiltyAnnotations for the different object properties
 * Contains a map containing for each objects those objects whose cone intersect with the object's cone
 */
public class WorldModel {

    private static List<UserModel> userModels;
    private static RobotModel robotModel;
    private static List<JsonObject> worldProperties = new ArrayList<>();
    private static JsonObject saliencyAnnotation;
    private static Map<JsonObject, List<JsonObject>> coneIntersectionMap = new HashMap<>();

    public static RobotModel getRobotModel() {
        return robotModel;
    }

    public static void setRobotModel(RobotModel robotMod) {
        robotModel = robotMod;
    }

    public static List<UserModel> getUserModels() {
        return userModels;
    }

    public static void setUserModels( List<UserModel> userMod) {
        userModels = userMod;
    }


    public static List<JsonObject> getWorldProperties() {
        return worldProperties;
    }

    public static void setWorldProperties(List<JsonObject> worldProps) {
        worldProperties = worldProps;
    }

    public static JsonObject getSaliencyAnnotation() {
        return saliencyAnnotation;
    }

    public static void setSaliencyAnnotation(JsonObject saliencyAnnotation) {
        WorldModel.saliencyAnnotation = saliencyAnnotation;
    }

    public static Map<JsonObject, List<JsonObject>> getConeIntersectionMap() {
        return coneIntersectionMap;
    }

    public static void setConeIntersectionMap(Map<JsonObject, List<JsonObject>> coneIntersectionMap) {
        WorldModel.coneIntersectionMap = coneIntersectionMap;
    }

}
