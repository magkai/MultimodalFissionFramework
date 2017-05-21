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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static de.dfki.mmf.modalities.ModalityType.IMAGE;

/**
 * Created by Magdalena Kaiser on 01.12.2016.
 */
public class ImageModality extends Modality implements ObjectReferencingModality {

    private static volatile ImageModality imageInstance = new ImageModality();

    public static ImageModality getInstance() {
        return imageInstance;
    }

    private ImageModality() {
        super(IMAGE);
    }


    @Override
    public Map<PredicateElement, Double> scorePresentability(Predicate predicate) {
        Map<PredicateElement, Double> scorePresentabilityMap = new HashMap<>();
        for(PredicateElement element: predicate.getElements()) {
            //if image object in world is referenced, displaying image is possible -> 1.0
            if (isObjectReferenced(element)) {
                scorePresentabilityMap.put(element, 1.0);
            } else {
                scorePresentabilityMap.put(element, 0.0);
            }
        }
        return  scorePresentabilityMap;
    }

    public void fillModalityOutputMap(Predicate predicate) {
        for (PredicateElement element : predicate.getElements()) {
            for (JsonObject objectProperties : WorldModel.getWorldProperties()) {
                if(objectProperties.has("worldobjectid")) {
                    //checks if element refers to object in the world
                    if(element.getWorldObjectId() != null) {
                        if (element.getWorldObjectId().equals(objectProperties.get("worldobjectid").getAsString())) {
                            if (objectProperties.has("worldobjecttype") && objectProperties.get("worldobjecttype").getAsString().equals("image")) {
                                URL resourceURL = null;
                                //image resource can be referenced differently -> create url to refer to it in a unified way
                                if (objectProperties.has("resourceurl")) {
                                    try {
                                        resourceURL = new URL(objectProperties.get("resourceurl").getAsString());
                                    } catch (MalformedURLException e) {
                                        System.out.println("Warning: malformed url: " + e.getMessage() + ". Image will not be displayed.");
                                        //just continue -> this particular image cannot be displayed but rest is working fine
                                        continue;
                                    }
                                } else if (objectProperties.has("resourcepath")) {
                                    try {
                                        resourceURL = Paths.get(objectProperties.get("resourcepath").getAsString()).toAbsolutePath().toUri().toURL();
                                        objectProperties.addProperty("resourceurl", resourceURL.toString());
                                    } catch (MalformedURLException e) {
                                        System.out.println("Warning: malformed url: " + e.getMessage() + ". Image will not be displayed.");
                                        //just continue -> this particular image cannot be displayed but rest is working fine
                                        continue;
                                    }
                                } else if (objectProperties.has("resourcename")) {
                                    String fileName = objectProperties.get("resourcename").getAsString();
                                    resourceURL = ImageModality.getInstance().getClass().getClassLoader().getResource(fileName);
                                    objectProperties.addProperty("resourceurl", resourceURL.toString());
                                }
                                //add url to resource to the output map
                                if (resourceURL != null) {
                                    modalityOutputMap.put(element, resourceURL);
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
