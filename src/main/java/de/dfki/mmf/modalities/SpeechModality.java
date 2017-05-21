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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.dfki.mmf.input.predicates.*;
import de.dfki.mmf.input.worldmodel.WorldModel;
import de.dfki.mmf.planner.modalityplanner.SpeechOutputType;
import de.dfki.mmf.attributeselection.AttributeSelectionAlgorithm;
import de.dfki.mmf.attributeselection.AttributeSelectorType;
import de.dfki.mmf.attributeselection.AttributiveObjectIdentifier;
import simplenlg.features.Feature;
import simplenlg.features.Form;
import simplenlg.features.InterrogativeType;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.realiser.english.Realiser;

import java.util.*;

import static de.dfki.mmf.modalities.ModalityType.SPEECH;

/**
 * Created by Magdalena Kaiser on 07.06.2016.
 */

/**
 * A SpeechModality is structure-forming and object-referencing and therefore creates the preliminary output structure
 * and a linguistic object description based on the salient attributes retrieved by the Attribute Selection Algorithm
 */
public class SpeechModality extends Modality implements StructureFormingModality, ObjectReferencingModality {

    //some variables needed for using SimpelNLG
    private Lexicon lexicon = Lexicon.getDefaultLexicon();
    private NLGFactory nlgFactory = new NLGFactory(lexicon);
    private Realiser realiser = new Realiser(lexicon);
    private SPhraseSpec phrase;
    private String outputStructure;
    //some configuration parameters for the Attribute Selection Algorithm
    private AttributeSelectorType attributeSelectorType;
    private double finalAttributeSelectionSaliencyThreshold = -1.0;

    private static volatile SpeechModality speechInstance = new SpeechModality();

    public static SpeechModality getInstance() {
        return speechInstance;
    }

    private SpeechModality() {
        super(SPEECH);
    }

    public AttributeSelectorType getAttributeSelectorType() {
        return attributeSelectorType;
    }

    public void setAttributeSelectorType(AttributeSelectorType attributeSelectorType) {
        this.attributeSelectorType = attributeSelectorType;
    }

    public double getFinalAttributeSelectionSaliencyThreshold() {
        return finalAttributeSelectionSaliencyThreshold;
    }

    public void setFinalAttributeSelectionSaliencyThreshold(double finalAttributeSelectionSaliencyThreshold) {
        this.finalAttributeSelectionSaliencyThreshold = finalAttributeSelectionSaliencyThreshold;
    }


    public String getOutputStructure() {
        return outputStructure;
    }

    private void setOutputStructure(String outputStructure) {
        this.outputStructure = outputStructure;
    }

    @Override
    public void fillModalityOutputMap(Predicate predicate) {
        for (PredicateElement element : predicate.getElements()) {
            modalityOutputMap.put(element, element.toString());
        }
    }

    public void updateModalityOutputMap(PredicateElement keyElement, String updateValue) {
        modalityOutputMap.put(keyElement, updateValue);
    }


    @Override
    public Map<PredicateElement, Double> scorePresentability(Predicate predicate) {
        HashMap<PredicateElement, Double> scorePresentabilityMap = new HashMap<>();
        for(PredicateElement element: predicate.getElements()) {
            if (element == null || element.toString().trim().equals("")) {
                scorePresentabilityMap.put(element, 0.0);
            }else {
                scorePresentabilityMap.put(element, 1.0);
            }
        }
        return scorePresentabilityMap;
    }

    @Override
    public boolean isObjectReferenced(PredicateElement element) {
        for(JsonObject objectProperties: WorldModel.getWorldProperties()) {
            if (objectProperties.has("worldobjectid")) {
                //checks if element refers to object in the world
                if(element.getWorldObjectId() != null) {
                    if (element.getWorldObjectId().equals(objectProperties.get("worldobjectid").getAsString())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     *
     * @param element input predicate element that refer to world object
     * @return AttributiveObjectIdentifier that identify corresponding world object by using some selected attributes
     * @throws InterruptedException
     */
    public AttributiveObjectIdentifier findAttributiveObjectIdentifier(PredicateElement element) throws InterruptedException {
        JsonObject queriedObject = null;
        String queriedType = "";
        ArrayList<JsonObject> databaseObjects = new ArrayList<>();
        //retrieve the jsonObject in the world model which is referenced by the predicate element
        for (JsonObject objectProperties : WorldModel.getWorldProperties()) {
            if (objectProperties.has("worldobjectid") && objectProperties.has("worldobjecttype")) {
                if (element.getWorldObjectId().equals(objectProperties.get("worldobjectid").getAsString())) {
                    queriedObject = objectProperties;
                    queriedType = objectProperties.get("worldobjecttype").getAsString();
                    break;
                }
            } else {
                throw new IllegalStateException("worldobjectid and worldobjecttype are required for each object");
            }
        }

        //no corresponding object has been found
        if(queriedObject == null) {
            return null;
        }

        //get all other objects of same type from the world model
        for(JsonObject objectProperties: WorldModel.getWorldProperties()) {
            if (objectProperties.get("worldobjecttype").getAsString().equals(queriedType) && !element.toString().contains(objectProperties.get("worldobjectid").getAsString())) {
                //if object is an image and the image is not displayed yet (invisible = true), then do not use object's properties
                if(objectProperties.has("isinvisible") && objectProperties.get("isinvisible").getAsBoolean()) {
                    continue;
                }
                databaseObjects.add(objectProperties);
            }
        }
        //if queried object is the only object of this type -> its type uniquely identifies it
        //(type is used in generateObjectDescriptionOutput())
        if(databaseObjects.isEmpty()) {
            //create the identifier, no attributes need to be selected since the type is sufficient
            AttributiveObjectIdentifier attributiveObjectIdentifier = new AttributiveObjectIdentifier(element, queriedObject.get("worldobjectid").getAsString(), queriedObject.get("worldobjecttype").getAsString());
            attributiveObjectIdentifier.setSelectedAttributes(null);
            attributiveObjectIdentifier.setSuccess(true);
            return attributiveObjectIdentifier;
        }

        //get the saliency annotation
        JsonObject saliencyAnnotation = WorldModel.getSaliencyAnnotation();
        //create and call the Attribute Selection Algorithm to select those attributes which makes queried object distinguishable from the other database objects
        AttributeSelectionAlgorithm attributeSelectionAlgorithm = new AttributeSelectionAlgorithm(databaseObjects, queriedObject, saliencyAnnotation);
        Thread asThread = new Thread(attributeSelectionAlgorithm);
        asThread.start();
        //get result set containing jsonObjects where each object contains the different possible attributes to identify the queried object
        Set<JsonObject> resultSet = attributeSelectionAlgorithm.getResult();

        //no unique and no partial unique identifier have been found
        if(resultSet.isEmpty()) {
            //set identifier properties accordingly
            AttributiveObjectIdentifier attributiveObjectIdentifier = new AttributiveObjectIdentifier(element, queriedObject.get("worldobjectid").getAsString(), queriedObject.get("worldobjecttype").getAsString());
            attributiveObjectIdentifier.setSelectedAttributes(null);
            attributiveObjectIdentifier.setSuccess(false);
            return attributiveObjectIdentifier;
        }
        //check if only a partial unique identifier has been found
        if(attributeSelectionAlgorithm.isPartialIdentifiable()) {
            AttributiveObjectIdentifier attributiveObjectIdentifier = new AttributiveObjectIdentifier(element, queriedObject.get("worldobjectid").getAsString(), queriedObject.get("worldobjecttype").getAsString());
            Iterator<JsonObject> iter = resultSet.iterator();
            //since only partial identifier -> use all found properties, retrieve them from the jsonObjects (check for duplicates)
            JsonObject jsonIdentifier = iter.next();
            while (iter.hasNext()) {
                for (Map.Entry<String,JsonElement> entry : iter.next().entrySet()) {
                    if(!jsonIdentifier.has(entry.getKey())) {
                        jsonIdentifier.add(entry.getKey(), entry.getValue());
                    }
                }
            }
            //set identifier properties accordingly
            attributiveObjectIdentifier.setSelectedAttributes(jsonIdentifier);
            attributiveObjectIdentifier.setPartialIdentifier(true);
            attributiveObjectIdentifier.setSuccess(true);
            return attributiveObjectIdentifier;
        }
        //retrieve from the resultset the "best" (according to the chosen criteria)  combination of attributes to uniquely identify the queried object
         JsonObject jsonIdentifier = null;
        //no selector chosen -> use "shortest visible above threshold" as default
        if(attributeSelectorType == null) {
            jsonIdentifier = attributeSelectionAlgorithm.getShortestMostSalientAboveThresholdIdentifier(resultSet);
        }
        //if selector has been chosen -> use corresponding method to retrieve the attributive identifier
        else if(attributeSelectorType.equals(AttributeSelectorType.SHORTEST_MOST_SALIENT_ABOVE_THRESHOLD)) {
            //check if saliency threshold is given
            if(finalAttributeSelectionSaliencyThreshold == -1.0) {
                jsonIdentifier = attributeSelectionAlgorithm.getShortestMostSalientAboveThresholdIdentifier(resultSet);
            } else {
                jsonIdentifier = attributeSelectionAlgorithm.getShortestMostSalientAboveThresholdIdentifier(resultSet, finalAttributeSelectionSaliencyThreshold);
            }
        }else if(attributeSelectorType.equals(AttributeSelectorType.SHORTEST)) {
            jsonIdentifier = attributeSelectionAlgorithm.getShortestIdentifier(resultSet);
        }else if(attributeSelectorType.equals(AttributeSelectorType.MOST_SALIENT)) {
            jsonIdentifier = attributeSelectionAlgorithm.getMostSalientIdentifier(resultSet);
        }else if(attributeSelectorType.equals(AttributeSelectorType.MAXIMIZED_SALIENCY_MAXIMIZED_SHORTNESS)) {
            jsonIdentifier = attributeSelectionAlgorithm.getMaxSalientMaxShortestIdentifier(resultSet);
        }
        //create attributive identifier
        AttributiveObjectIdentifier attributiveObjectIdentifier = new AttributiveObjectIdentifier(element, queriedObject.get("worldobjectid").getAsString(), queriedObject.get("worldobjecttype").getAsString());
        attributiveObjectIdentifier.setSelectedAttributes(jsonIdentifier);
        if(jsonIdentifier == null) {
            attributiveObjectIdentifier.setSuccess(false);
        }else {
            attributiveObjectIdentifier.setSuccess(true);
        }
        return attributiveObjectIdentifier;
    }

    /**
     *
     * @param identifier the attributive identifier
     * @param speechOutputType the selected type for the speech output chosen in the planning process
     * @return a string representing the output phrase for a element referencing a world object
     */
    public String generateObjectDescriptionOutput(AttributiveObjectIdentifier identifier, SpeechOutputType speechOutputType) {
        PredicateElement predicateElement = identifier.getPredicateElement();
        String worldId = identifier.getWorldId();
        String preposition = "";
        String speechOutput = "";
        //receive possible prepositions in front of PredicateElement
        if(!predicateElement.toString().equals(worldId)) {
            String[] splitNameArray = predicateElement.toString().split(" ");
            for(int i = 0; i < splitNameArray.length; i++) {
                if(!splitNameArray[i].startsWith(worldId)) {
                    preposition += splitNameArray[i] + " ";
                }
            }
        }
        //create speech output from attributive identifier
        if(speechOutputType.equals(SpeechOutputType.ATTRIBUTIVE_IDENTIFIER)) {
            JsonObject identifierMap = identifier.getSelectedAttributes();
            SPhraseSpec phrase = nlgFactory.createClause();
            //no attributes were selected, because only one object of this type -> use "the" + type
            if(identifierMap == null) {
                phrase.setSubject(preposition + "the " + identifier.getType());
            }
            else {
                //use possible prepositions + object's type as subject
                //extract the attributes from the identifier and use them as modifiers
                phrase.setSubject(preposition + identifier.getType());
                for (Map.Entry<String, JsonElement> entry : identifierMap.entrySet()) {
                    String modifier = "with " + entry.getKey() + " ";
                    if (entry.getValue().isJsonPrimitive()) {
                        modifier += entry.getValue().getAsString() + " ";
                        phrase.addModifier(modifier + ", ");
                    } else if (entry.getValue().isJsonArray()) {
                        JsonArray array = entry.getValue().getAsJsonArray();
                        for (int i = 0; i < array.size(); i++) {
                            modifier += array.get(i).getAsString() + " ";
                            phrase.addModifier(modifier + ", ");
                        }
                    } else if (entry.getValue().isJsonObject()) {
                        JsonObject innerObject = entry.getValue().getAsJsonObject();
                        for (Map.Entry<String, JsonElement> innerEntry : innerObject.entrySet()) {
                            modifier = "with " + innerEntry.getKey() + " ";
                            modifier += innerEntry.getValue().getAsString() + " ";
                            phrase.addModifier(modifier + ", ");
                        }
                    }
                }
            }
            //create speech output
            speechOutput = realiser.realiseSentence(phrase);
            if (speechOutput.endsWith(".")) {
                speechOutput = speechOutput.substring(0, speechOutput.length() - 1);
                if(speechOutput.endsWith(",")) {
                    speechOutput = speechOutput.substring(0, speechOutput.length() - 1);
                }
            }
        //create other forms of speech output according to the chosen SpeechOutputType
        }else if(speechOutputType.equals(SpeechOutputType.THIS)) {
            speechOutput = preposition + "this ";
        }else if(speechOutputType.equals(SpeechOutputType.THIS_TYPE)) {
            speechOutput = preposition + "this " + identifier.getType();
        }else if(speechOutputType.equals(SpeechOutputType.THE_TYPE)) {
            speechOutput = preposition + "the " + identifier.getType();
        }else if(speechOutputType.equals(SpeechOutputType.IT)) {
            speechOutput = preposition + "it";
        }

        return speechOutput;
    }

    @Override
    public Predicate generateOutputStructure(Predicate predicate) {
        //create modified predicate for the output
        Predicate modifiedPredicate = new Predicate(predicate.getPredicateName());
        phrase = nlgFactory.createClause();
        //create phrase using the annotations of each predicate element describing its function in the sentence
        for (PredicateElement element : predicate.getElements()) {
            if (element != null && element.getPredicateElementAnnotation() != null) {
                PredicateElementAnnotation elementAnnotation = element.getPredicateElementAnnotation();
                if (elementAnnotation.equals(PredicateElementAnnotation.subject)) {
                    phrase.setSubject(element.toString());
                } else if (elementAnnotation.equals(PredicateElementAnnotation.verb)) {
                    phrase.setVerb(element.toString());
                } else if (elementAnnotation.equals(PredicateElementAnnotation.directObject)) {
                    phrase.setObject(element.toString());
                } else if (elementAnnotation.equals(PredicateElementAnnotation.indirectObject)) {
                    phrase.setIndirectObject(element.toString());
                } else if (elementAnnotation.equals(PredicateElementAnnotation.complement)) {
                    phrase.setComplement(element.toString());
                }
            }
        }
        //get annotation which affect whole predicate and use them to create correct type of phrase
        if (predicate.getPredicateAnnotations() != null) {
            List<PredicateAnnotation> predicateAnnotations = predicate.getPredicateAnnotations();
            if (predicateAnnotations.contains(PredicateAnnotation.NEGATION)) {
                phrase.setFeature(Feature.NEGATED, true);
            }
            if (predicateAnnotations.contains(PredicateAnnotation.YES_NO)) {
                phrase.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.YES_NO);
            }
            if (predicateAnnotations.contains(PredicateAnnotation.WHO_SUBJECT)) {
                phrase.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHO_SUBJECT);
            }
            if (predicateAnnotations.contains(PredicateAnnotation.WHAT_SUBJECT)) {
                phrase.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHAT_SUBJECT);
            }
            if (predicateAnnotations.contains(PredicateAnnotation.WHO_OBJECT)) {
                phrase.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHO_OBJECT);
            }
            if (predicateAnnotations.contains(PredicateAnnotation.WHAT_OBJECT)) {
                phrase.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.WHAT_OBJECT);
            }
            if (predicateAnnotations.contains(PredicateAnnotation.HOW)) {
                phrase.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.HOW);
            }
            if (predicateAnnotations.contains(PredicateAnnotation.HOW_MANY)) {
                phrase.setFeature(Feature.INTERROGATIVE_TYPE, InterrogativeType.HOW_MANY);
            }
            if (predicateAnnotations.contains((PredicateAnnotation.IMPERATIVE))) {
                phrase.setFeature(Feature.FORM, Form.IMPERATIVE);
            }
        }
        //create the output sentence
        String resultString = realiser.realiseSentence(phrase);
        setOutputStructure(resultString);
        //split output structure into its elements
        String resStructure = phrase.getParent().getFeatureAsString(("textComponents"));
        resStructure = resStructure.replace("[", "");
        resStructure = resStructure.replace("]", "");
        ArrayList<String> outputOrderList = new ArrayList<>();
        String[] resSplit = resStructure.split(",");
        for (int i = 0; i < resSplit.length; i++) {
            outputOrderList.add(resSplit[i].trim());
        }
        //create new predicate element list
         ArrayList<PredicateElement> modifiedPredicateElementList = new ArrayList<>();
        //use this orderList as new predicate element list -> order important for planning
        boolean elementAlreadyAdded = false;
        for (String outputOrderElement : outputOrderList) {
            //keep old predicate if worldobjectid and type were already set ("I", "you")
            for(PredicateElement element: predicate.getElements()) {
                if(element.getWorldObjectId() != null && element.toString().equals(outputOrderElement)) {
                    modifiedPredicateElementList.add(element);
                    elementAlreadyAdded = true;
                    break;
                }
            }
            if(elementAlreadyAdded) {
                elementAlreadyAdded = false;
                continue;
            }
            modifiedPredicateElementList.add(new StringPredicateElement(outputOrderElement));
        }
        if(phrase.hasFeature(Feature.INTERROGATIVE_TYPE)) {
            modifiedPredicateElementList.add(new StringPredicateElement("?"));
        }else {
            modifiedPredicateElementList.add(new StringPredicateElement("."));
        }
        //set new elements for the modified predicate
        modifiedPredicate.setElements(modifiedPredicateElementList.toArray(new StringPredicateElement[modifiedPredicateElementList.size()]));
       return modifiedPredicate;
    }

}
