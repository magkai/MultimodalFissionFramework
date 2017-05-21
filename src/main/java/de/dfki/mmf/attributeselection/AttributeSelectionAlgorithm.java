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

package de.dfki.mmf.attributeselection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by Magdalena Kaiser on 17.07.2016.
 */

/**
 * Algorithm to retrieve all the available attributes which identify certain world object
 * and to select the most suitable ones with respect to the used AttributeSelectorType
 */
public class AttributeSelectionAlgorithm implements Runnable{

    private JsonObject queriedObject;
    private List<JsonObject> databaseObjects;
    protected JsonObject saliencyAnnotations;
    private  Set<JsonObject> identifierSet;
    private double saliencyThreshold = 0.5;
    private boolean partialIdentifier = false;

    public AttributeSelectionAlgorithm(List<JsonObject> databaseObjects, JsonObject queriedObject) {
        this.databaseObjects = databaseObjects;
        this.queriedObject = queriedObject;
    }

    public AttributeSelectionAlgorithm(List<JsonObject> databaseObjects, JsonObject queriedObject, JsonObject saliencyAnnotations) {
        this.databaseObjects = databaseObjects;
        this.queriedObject = queriedObject;
        this.saliencyAnnotations = saliencyAnnotations;
    }


    public synchronized  Set<JsonObject> getResult() throws InterruptedException {
        while(identifierSet == null) {
            wait();
        }
        return identifierSet;
    }

    private void setResult(Set<JsonObject> identifier) {
        this.identifierSet = identifier;
    }

    public void setSaliencyThreshold(double threshold) {
        this.saliencyThreshold = threshold;
    }

    public double getSaliencyThreshold() {
        return this.saliencyThreshold;
    }

    public boolean isPartialIdentifiable() {
        return partialIdentifier;
    }

    public void setPartialIdentifier(boolean partialIdentifier) {
        this.partialIdentifier = partialIdentifier;
    }

    /**
     *
     * @param identifierSet
     * @return shortest attribute set where all used attributes have a saliency value higher than default threshold of 0.8
     */
    public JsonObject getShortestMostSalientAboveThresholdIdentifier(Set<JsonObject> identifierSet) {
        //use default threshold of 0.8
        return getShortestMostSalientAboveThresholdIdentifier(identifierSet, 0.8);
    }

    /**
     *
     * @param identifierSet
     * @param saliencyEndResultThreshold
     * @return shortest attribute set where all used attributes have a saliency value higher than certain threshold
     */
    public JsonObject getShortestMostSalientAboveThresholdIdentifier(Set<JsonObject> identifierSet, double saliencyEndResultThreshold) {
        if(identifierSet == null ||identifierSet.isEmpty()) {
            return null;
        }
        ArrayList<JsonObject> resultList = new ArrayList<>(identifierSet);
        Collections.sort(resultList, new CompareSize());
        CompareSaliency cmpSaliency = new CompareSaliency();
        JsonObject resultMap = new JsonObject();
        double oldSaliencyValue = 0.0;
        int oldSize = resultList.get(0).size();
        //check if list element has a saliency value above the threshold
        boolean foundVisible = false;
        //use same sigmoid function to normalize threshold which is used to normalize saliency value
        saliencyEndResultThreshold = saliencyEndResultThreshold/(Math.sqrt(1+Math.pow(saliencyEndResultThreshold,2.0)));
        for(int i = 0; i< resultList.size(); i++) {
            double saliencyValue = cmpSaliency.computeSaliencyValue(resultList.get(i));
            int size = resultList.get(i).size();
            //accept only greater size if no visible one has been found yet
            if(size > oldSize && foundVisible) {
                break;
            }
            //get the most visible one for the smallest possible size
            if(saliencyValue > saliencyEndResultThreshold && saliencyValue > oldSaliencyValue) {
                foundVisible = true;
                resultMap = new JsonObject();
                Set<Map.Entry<String, JsonElement>> entrySet = resultList.get(i).entrySet();
                for(Map.Entry<String,JsonElement> currentEntry : entrySet) {
                    resultMap.add(currentEntry.getKey(), currentEntry.getValue());
                }
                oldSaliencyValue = saliencyValue;
                oldSize = size;
            }

        }
        //no JsonObject has saliency value above required threshold -> take last element in list, will have highest value
        if(resultMap.size() == 0) {
            Set<Map.Entry<String, JsonElement>> entrySet = resultList.get(resultList.size() - 1).entrySet();
            for(Map.Entry<String,JsonElement> entry : entrySet) {
                resultMap.add(entry.getKey(), entry.getValue());
            }
        }
        return resultMap;
    }

    /**
     *
     * @param identifierSet
     * @return shortest attribute set which identifies queried object
     */
    public JsonObject getShortestIdentifier(Set<JsonObject> identifierSet) {
        if(identifierSet == null || identifierSet.isEmpty()) {
            return null;
        }
        ArrayList<JsonObject> resultList = new ArrayList<>(identifierSet);
        Collections.sort(resultList, new CompareSize());
        return resultList.get(0);
    }

    /**
     *
     * @param identifierSet
     * @return attribute set which maximizes the saliency value
     */
    public JsonObject getMostSalientIdentifier(Set<JsonObject> identifierSet) {
        if(identifierSet == null || identifierSet.isEmpty()) {
            return null;
        }
        ArrayList<JsonObject> resultList = new ArrayList<>(identifierSet);
        Collections.sort(resultList, new CompareSaliency());
        return resultList.get(0);
    }

    /**
     *
     * @param identifierSet
     * @return attribute set which maximize the saliency AND the shortness value
     */
    public JsonObject getMaxSalientMaxShortestIdentifier(Set<JsonObject> identifierSet) {
        if (identifierSet == null || identifierSet.isEmpty()) {
            return null;
        }
        ArrayList<JsonObject> saliencyResultList = new ArrayList<>(identifierSet);
        //sort identifiers according to their saliency values
        Collections.sort(saliencyResultList, new CompareSaliency());
        ArrayList<JsonObject> sizeResultList = new ArrayList<>(identifierSet);
        //sort identifiers according to their length
        Collections.sort(sizeResultList, new CompareSize());
        //determine rank in sizeResultList, if there are several with the same length -> receive same rank
        HashMap<JsonObject, Integer> sizeMap = new HashMap<>();
        int sizeCounter = 1;
        sizeMap.put(sizeResultList.get(0), sizeCounter);
        for(int i = 1; i < sizeResultList.size(); i++) {
            if(sizeResultList.get(i).size() == sizeResultList.get(i-1).size()) {
                sizeMap.put(sizeResultList.get(i), sizeCounter);
            }else {
                sizeCounter++;
                sizeMap.put(sizeResultList.get(i), sizeCounter);
            }
        }
        //determine rank in saliencyResultList, if there are several with the same saliency value -> receive same rank
        HashMap<JsonObject, Integer> saliencyMap = new HashMap<>();
        int saliencyCounter = 1;
        saliencyMap.put(saliencyResultList.get(0), saliencyCounter);
        CompareSaliency compareSaliency;
        //use only two decimal places
        DecimalFormat f = new DecimalFormat("##.00");
        for(int i = 1; i < saliencyResultList.size(); i++) {
            compareSaliency = new CompareSaliency();
            if(f.format(compareSaliency.computeSaliencyValue(saliencyResultList.get(i))).equals(f.format(compareSaliency.computeSaliencyValue(saliencyResultList.get(i-1))))) {
                saliencyMap.put(saliencyResultList.get(i), saliencyCounter);
            }else {
                saliencyCounter++;
                saliencyMap.put(saliencyResultList.get(i), saliencyCounter);
            }
        }

        //receive as results those which does have a good rank for both categories
        //add rank of the two maps and add the difference of their rank as well
        HashMap<JsonObject, Double> resultMap = new HashMap<>();
        for(JsonObject object: sizeMap.keySet()) {
            if(saliencyMap.containsKey(object)) {
                //use relative ranks (rank divided by the number of ranks) and  then calculate sum and difference
                double add = ((double) (sizeMap.get(object)))/sizeCounter+ ((double) (saliencyMap.get(object)))/saliencyCounter;
                double diff = ((double) (sizeMap.get(object)))/sizeCounter- ((double) (saliencyMap.get(object)))/saliencyCounter;
                resultMap.put(object, add + Math.abs(diff));
            }
        }

        //take the minimal rank value of the resultMap -> best for both categories, if there are several, take the first one
        Map.Entry<JsonObject, Double> min = Collections.min(resultMap.entrySet(), new Comparator<Map.Entry<JsonObject, Double>>() {
            public int compare(Map.Entry<JsonObject, Double> entry1, Map.Entry<JsonObject, Double> entry2) {
                return entry1.getValue().compareTo(entry2.getValue());
            }
        });
        return min.getKey();
    }

    @Override
    public synchronized void run() {
        //comparison between queriedObject and each database object to find properties which differentiate them
        List<JsonObject> identifiers = computeSingleDiscriminatingIdentifiers();
        if(identifiers == null) {
            setResult(null);
            notifyAll();
            return;
        }

        //create a Jsonobject containing all used discriminating attributes contained in the list of identifiers
        JsonObject allIdentifiers  = new JsonObject();
        for(JsonObject object: identifiers) {
            for (Map.Entry<String,JsonElement> entry : object.entrySet()) {
                //only add entry if it does not exist yet
                if(!allIdentifiers.has(entry.getKey())) {
                    allIdentifiers.add(entry.getKey(), entry.getValue());
                }
            }
        }

        Set<JsonObject> resultSet;
        //combine identifiers from single comparisons to receive list with all possible discriminators for the queried object
        resultSet = combineIdentifiers(identifiers, allIdentifiers);

        //if resultset is empty -> no identifier found, use partial identifier instead
        if(resultSet.isEmpty()) {
            for(JsonObject object: identifiers) {
                if(object.entrySet().size() != 0) {
                    resultSet.add(object);
                }
            }
            if(!resultSet.isEmpty()) {
                setPartialIdentifier(true);
            }
        }
        setResult(resultSet);
        notifyAll();
    }

    /**
     * @return list with JsonObjects each containing those attributes of the queried object which discriminates it from the compared database object
     */
    public List<JsonObject> computeSingleDiscriminatingIdentifiers() {
        //data structure to save discriminating identifiers resulting from the comparisons between queried object and each database object
        ArrayList<JsonObject> identifiers = new ArrayList<>();
        for(JsonObject databaseObject: databaseObjects) {
            //create list for each comparison of queried object properties and the properties of a database object
            // to save property values which uniquely identify queried object
            JsonObject singleDiscriminatingIdentifiers = new JsonObject();
            //go over each property of the queried object
            Set<Map.Entry<String, JsonElement>> entrySet = queriedObject.entrySet();
            for(Map.Entry<String,JsonElement> entry : entrySet){
                String queryKey = entry.getKey();
                //check saliencyAnnotation is exists
                if (saliencyAnnotations != null) {
                    //look up the saliency annotation of the object
                    if(saliencyAnnotations.has(queryKey)) {
                        double saliencyNbr = saliencyAnnotations.get(queryKey).getAsDouble();
                        //if saliency is lower than threshold -> property will be pruned, continue with next property
                        if (saliencyNbr < saliencyThreshold) {
                            continue;
                        }
                    }else {
                        continue;
                    }
                }
                //the values the queried object has for certain property
                JsonElement queriedPropertyValues =  queriedObject.get(queryKey);
                //check if database object has this property
                if (databaseObject.has(queryKey)) {
                    //get the values the database object has for this property
                    JsonElement dataPropertyValues = databaseObject.get(queryKey);
                    //check if objects have the same values for this property -> if not add values of queried object to list
                    if((queriedPropertyValues.isJsonArray() && !dataPropertyValues.isJsonArray())
                            || (!queriedPropertyValues.isJsonArray() && dataPropertyValues.isJsonArray())) {
                        singleDiscriminatingIdentifiers.add(queryKey, queriedPropertyValues);
                    }else if(!queriedPropertyValues.isJsonArray() && !dataPropertyValues.isJsonArray()) {
                        if(!Objects.equals(queriedPropertyValues.toString(), dataPropertyValues.toString())) {
                            singleDiscriminatingIdentifiers.add(queryKey, queriedPropertyValues);
                        }
                    }else if(queriedPropertyValues.isJsonArray() && dataPropertyValues.isJsonArray()) {
                        JsonArray queryArray = queriedPropertyValues.getAsJsonArray();
                        JsonArray dataArray = dataPropertyValues.getAsJsonArray();
                        boolean added = false;
                        for(JsonElement queryElement: queryArray) {
                            if(!dataArray.contains(queryElement)) {
                                singleDiscriminatingIdentifiers.add(queryKey, queriedPropertyValues);
                                added = true;
                                break;
                            }
                        }
                        if(!added) {
                            for(JsonElement dataElement: dataArray) {
                                if(!queryArray.contains(dataElement)) {
                                    singleDiscriminatingIdentifiers.add(queryKey, queriedPropertyValues);
                                    break;
                                }
                            }
                        }
                    }
                    //compared database object does not have particular property -> can be seen as differentiating
                } else {
                    singleDiscriminatingIdentifiers.add(queryKey, queriedPropertyValues);
                }
            }
            //add single discriminating identifiers to list with all found identifiers
            identifiers.add(singleDiscriminatingIdentifiers);
        }
        return identifiers;
    }

    /**
     * Combine the identifiers to retrieve a set containing all discriminators for the queried object
     * @param identifiers individual identifiers
     * @param allIdentifiers all individual identifiers in one Jsonobject
     * @return set with all valid identifiers
     */
    private Set<JsonObject> combineIdentifiers(List<JsonObject> identifiers, JsonObject allIdentifiers) {
        //create the power set for the identifiers
        Set<JsonObject> powerSet = powerSet(allIdentifiers);
        ArrayList<JsonObject> powerList = new ArrayList<>(powerSet);
        boolean found = false;
        ArrayList<Integer> deleteIndices =  new ArrayList<>();

        //powerset contains also some non valid identifiers
        //check if identifiers are valid -> only if the element of the powerset contains an element which is represented in each individual identifier of the list
        for (int i = 0; i < powerList.size(); i++) {
            for(int j = 0; j < identifiers.size(); j++) {
                for (Map.Entry<String,JsonElement> identifierEntry : identifiers.get(j).entrySet()) {
                    for (Map.Entry<String, JsonElement> powerListEntry : powerList.get(i).entrySet()) {
                        //check if at least one element of the current powerset entry is contained in each identifier
                        if(powerListEntry.getKey().equals(identifierEntry.getKey())) {
                            found = true;
                            break;
                        }
                    }
                }
                //one entry is not represented by all identifiers -> save index to delete it later
                if(!found) {
                    deleteIndices.add(i);
                    break;
                }
                else {
                    found = false;
                }
            }
        }

        ArrayList<JsonObject> resultList = new ArrayList<>();
        //delete non valid identifiers from the powerset
        for(int i = 0; i< powerList.size(); i++) {
            if(!deleteIndices.contains(new Integer(i))) {
                resultList.add(powerList.get(i));
            }
        }
        //list with all valid identifiers
        return new HashSet<>(resultList);
    }



    /**
     *
     * @param originalObject
     * @return powerset of original set
     */
    private  Set<JsonObject> powerSet(JsonObject originalObject) {
        HashSet<JsonObject> sets = new HashSet<>();
        //check if termination condition is reached
        if (originalObject.size() == 0) {
            sets.add(new JsonObject());
            return sets;
        }
        JsonObject head = new JsonObject();
        JsonObject rest = new JsonObject();
        //retrieve first object from current set and save it in "head", the remaining objects are saved in "rest"
        for (Map.Entry<String,JsonElement> entry : originalObject.entrySet()) {
            if(head.size() < 1) {
                head.add(entry.getKey(), entry.getValue());
            }else {
                rest.add(entry.getKey(), entry.getValue());
            }
        }
        //recursive call for combining all possibilities
        for (JsonObject set : powerSet(rest)) {
            JsonObject newSet = new JsonObject();
            newSet.add(head.entrySet().iterator().next().getKey(), head.entrySet().iterator().next().getValue());
            for (Map.Entry<String,JsonElement> entry : set.entrySet()) {
                newSet.add(entry.getKey(), entry.getValue());
            }
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }


    /**
     * Class to compare the size of two different sets (each given as a Jsonobject)
     */
    class CompareSize implements Comparator<JsonObject> {
        @Override
        public int compare(JsonObject map1, JsonObject map2) {
            if (map1.size() > map2.size()) {
                return 1;
            } else if (map1.size() < map2.size()) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    /**
     * Class to compare saliency of two different sets (each given as a Jsonobject)
     */
    class CompareSaliency implements Comparator<JsonObject> {
        @Override
        public int compare(JsonObject map1, JsonObject map2) {
            double saliencyCounter1 = computeSaliencyValue(map1);
            double saliencyCounter2 = computeSaliencyValue(map2);
            if (saliencyCounter1 < saliencyCounter2) {
                return 1;
            } else if (saliencyCounter1 > saliencyCounter2) {
                return -1;
            } else {
                return 0;
            }
        }

        /**
         * Calculates the saliency value of the attributes stored in map
         * @param map
         * @return summed saliency value of all properties in map normalized between 0.0 and 1.0
         */
        protected double computeSaliencyValue(JsonObject map){
            double saliencyCounter = 0.0;
            Set<Map.Entry<String, JsonElement>> entrySet = map.entrySet();
            for(Map.Entry<String,JsonElement> entry : entrySet){
                if(saliencyAnnotations.has(entry.getKey())) {
                    saliencyCounter += saliencyAnnotations.get(entry.getKey()).getAsDouble();
                }
            }
            //sigmoid function x/sqrt(1+x^2) used to have saliency counter between 0.0 and 1.0
            return saliencyCounter/(Math.sqrt(1+Math.pow(saliencyCounter,2.0)));
        }
    }

}
