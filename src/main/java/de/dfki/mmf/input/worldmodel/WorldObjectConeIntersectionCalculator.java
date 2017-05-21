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
import de.dfki.mmf.math.DirectionVector;
import de.dfki.mmf.math.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Magdalena Kaiser on 06.01.2017.
 */

/**
 * Intersection Calculator for each cone from robot to world object
 */
public class WorldObjectConeIntersectionCalculator {

    /**
     *
     * @param worldProperties
     * @return map containing for each objects those objects whose cone intersect with the object's cone
     */
    public Map<JsonObject, List<JsonObject>> calculateObjectsConeIntersection(List<JsonObject> worldProperties) {
        //a cone is the area from the robot's position to the world object with certain radius around the world object
        //map for saving for each object those objects those cones intersect with the object
        Map<JsonObject, List<JsonObject>> coneIntersectionMap = new HashMap<>();
        //get position of robot
        Position robotPosition = WorldModel.getRobotModel().getRobotPosition();
        if(robotPosition == null) {
            return new HashMap<>();
        }
        for(JsonObject worldObject: worldProperties) {
            Position worldObjectPosition = null;
            //get position of current world object
            if (worldObject.has("xposition") && worldObject.has("yposition") && worldObject.has("zposition")) {
                double xPos = worldObject.get("xposition").getAsDouble();
                double yPos = worldObject.get("yposition").getAsDouble();
                double zPos = worldObject.get("zposition").getAsDouble();
                worldObjectPosition = new Position(xPos, yPos, zPos);
            } else if (worldObject.has("position")) {
                JsonObject innerObject = worldObject.get("position").getAsJsonObject();
                if (innerObject.has("xposition") && innerObject.has("yposition") && innerObject.has("zposition")) {
                    double xPos = innerObject.get("xposition").getAsDouble();
                    double yPos = innerObject.get("yposition").getAsDouble();
                    double zPos = innerObject.get("zposition").getAsDouble();
                    worldObjectPosition =  new Position(xPos, yPos, zPos);
                }
            }
            if(worldObjectPosition != null ) {
                //list to save objects whose cones intersect with the current one's cone
                ArrayList<JsonObject> coneIntersectionList = new ArrayList<>();
                double worldObjectRadius;
                //check if certain radius is given for the current world object
                if(worldObject.has("proximityradius")) {
                    worldObjectRadius = worldObject.get("proximityradius").getAsDouble();
                 //if not: calculate radius based on the size of the object -> if no size is given: assume small size
                }else {
                    String sizeSpecification = "";
                    if(worldObject.has("worldobjectsizecategory")) {
                        sizeSpecification = worldObject.get("worldobjectsizecategory").getAsString();
                    }
                    worldObjectRadius = calculateProximityRadius(worldObjectPosition, robotPosition, sizeSpecification);
                }
                //go through other objects
                for(JsonObject otherObject: worldProperties) {
                    if(!worldObject.equals(otherObject)) {
                        Position otherObjectPosition = null;
                        //get position of other world object
                        if (otherObject.has("xposition") && otherObject.has("yposition") && otherObject.has("zposition")) {
                            double xPos = otherObject.get("xposition").getAsDouble();
                            double yPos = otherObject.get("yposition").getAsDouble();
                            double zPos = otherObject.get("zposition").getAsDouble();
                            otherObjectPosition = new Position(xPos, yPos, zPos);
                        } else if (otherObject.has("position")) {
                            JsonObject innerObject = otherObject.get("position").getAsJsonObject();
                            if (innerObject.has("xposition") && innerObject.has("yposition") && innerObject.has("zposition")) {
                                double xPos = innerObject.get("xposition").getAsDouble();
                                double yPos = innerObject.get("yposition").getAsDouble();
                                double zPos = innerObject.get("zposition").getAsDouble();
                                otherObjectPosition =  new Position(xPos, yPos, zPos);
                            }
                        }
                        if(otherObjectPosition != null) {
                            double otherObjectRadius;
                            //check if certain radius is given for the other world object
                            if(otherObject.has("proximityradius")) {
                                otherObjectRadius = otherObject.get("proximityradius").getAsDouble();
                                //if not: calculate radius based on the size of the object -> if no size is given: assume small size
                            }else {
                                String sizeSpecification = "";
                                if(otherObject.has("worldobjectsizecategory")) {
                                    sizeSpecification = otherObject.get("worldobjectsizecategory").getAsString();
                                }
                                otherObjectRadius = calculateProximityRadius(otherObjectPosition, robotPosition, sizeSpecification);
                            }
                            //check if the cones of the two objects intersect -> if so, add other object to list
                            if(HasConeIntersection(worldObjectPosition, otherObjectPosition, robotPosition, worldObjectRadius, otherObjectRadius)) {
                                coneIntersectionList.add(otherObject);
                            }
                        }
                    }
                }
                coneIntersectionMap.put(worldObject, coneIntersectionList);
            }
        }
        return  coneIntersectionMap;
    }

    /**
     *
     * @param objectPosition
     * @param robotPosition
     * @param sizeSpecification
     * @return radius around the world object
     */
    public double calculateProximityRadius(Position objectPosition, Position robotPosition, String sizeSpecification) {
        double distance = robotPosition.calculateDistance(objectPosition);
        if(sizeSpecification.equals("big")) {
            return 0.075 + distance*0.15;
        }else if(sizeSpecification.equals("medium")) {
            return 0.05 + distance*0.1;
        }else if(sizeSpecification.equals("small")) {
            return 0.025 + distance*0.05;
            //if no information about size is given, assume object is small
        }else {
            return 0.025 + distance*0.05;
        }
    }

    /**
     *
     * @param position1 position of current world object
     * @param position2 position of the other world object
     * @param robotOrigin position of robot
     * @param radius1 radius of current world object
     * @param radius2 radius of the other world object
     * @return true if the cones of the objects intersect with each other
     */
    public boolean HasConeIntersection(Position position1, Position position2, Position robotOrigin, double radius1, double radius2) {
        DirectionVector normalVector1 = new DirectionVector(position1.getX()-robotOrigin.getX(), position1.getY()-robotOrigin.getY(), position1.getZ()-robotOrigin.getZ());
        DirectionVector unitNormalVector1 = normalVector1.getUnitVector();
        DirectionVector normalVector2 = new DirectionVector(position2.getX()-robotOrigin.getX(), position2.getY()-robotOrigin.getY(), position2.getZ()-robotOrigin.getZ());
        DirectionVector unitNormalVector2 = normalVector2.getUnitVector();
        //calculate angle between the cone wall and the normal vector
        double angle1 = Math.atan(radius1/normalVector1.getVectorLength());
        double angle2 = Math.atan(radius2/normalVector2.getVectorLength());
        //calculate angle between the two plane normal vectors
        double scalarProduct = unitNormalVector1.calculateScalarProduct(unitNormalVector2);
        double angleBetweenNormVecs = Math.acos(scalarProduct);
        //cones can only intersect if scalar product between normal vectors is > 0 and the sum of the individual angles is bigger than of the angle between the cones
        return scalarProduct > 0 && angle1+angle2 >= angleBetweenNormVecs;
    }
}
