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

package de.dfki.mmf.modalities;

import com.google.gson.JsonObject;
import de.dfki.mmf.input.predicates.Predicate;
import de.dfki.mmf.input.predicates.PredicateElement;
import de.dfki.mmf.input.worldmodel.WorldModel;
import de.dfki.mmf.math.Position;

import java.util.HashMap;
import java.util.Map;

import static de.dfki.mmf.modalities.ModalityType.GAZE;

/**
 * Created by Magdalena Kaiser on 01.08.2016.
 */


public class GazeModality  extends Modality implements ObjectReferencingModality {

    private static volatile GazeModality gazeInstance = new GazeModality();

    public static GazeModality getInstance() {
        return gazeInstance;
    }

    private GazeModality() {
        super(GAZE);
    }


    @Override
    public Map<PredicateElement, Double> scorePresentability(Predicate predicate) {
        Map<PredicateElement, Double> scorePresentabilityMap = new HashMap<>();
        for(PredicateElement element: predicate.getElements()) {
            //robot cannot look at itself
            if (WorldModel.getRobotModel().getRobotProperties() != null && element.getWorldObjectId() != null) {
                if (element.getWorldObjectId().equals(WorldModel.getRobotModel().getRobotId())) {
                    scorePresentabilityMap.put(element, 0.0);
                    continue;
                }
            }
            //if object in world is referenced, looking on object is possible -> 1.0
            if (isObjectReferenced(element)) {
                scorePresentabilityMap.put(element, 1.0);
            } else {
                scorePresentabilityMap.put(element, 0.0);
            }
        }
        return  scorePresentabilityMap;
    }


    public void fillModalityOutputMap(Predicate predicate) {
        for(PredicateElement element: predicate.getElements()) {
            for (JsonObject objectProperties : WorldModel.getWorldProperties()) {
                if (objectProperties.has("worldobjectid")) {
                    //checks if element refers to object in the world
                    if(element.getWorldObjectId() != null) {
                        if (element.getWorldObjectId().equals(objectProperties.get("worldobjectid").getAsString())) {
                            //if object has position -> looking at object is possible, save it in the outputMap
                            if (objectProperties.has("xposition") && objectProperties.has("yposition") && objectProperties.has("zposition")) {
                                Position position = new Position(objectProperties.get("xposition").getAsDouble(), objectProperties.get("yposition").getAsDouble(), objectProperties.get("zposition").getAsDouble());
                                modalityOutputMap.put(element, position);
                            } else if (objectProperties.has("position")) {
                                JsonObject innerObject = objectProperties.get("position").getAsJsonObject();
                                if (innerObject.has("xposition") && innerObject.has("yposition") && innerObject.has("zposition")) {
                                    Position position = new Position(innerObject.get("xposition").getAsDouble(), innerObject.get("yposition").getAsDouble(), innerObject.get("zposition").getAsDouble());
                                    modalityOutputMap.put(element, position);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isObjectReferenced(PredicateElement element) {
        if(modalityOutputMap.containsKey(element)) {
            return true;
        }
        return false;
    }
}
