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

import com.google.gson.*;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.bson.Document;

import java.util.*;


/**
 * Created by Magdalena Kaiser on 24.08.2016.
 */

/**
 *  A factory to create the world model
 */
public class WorldModelFactory {

    /**
     * Create/update world model by either retrieving data from owl model or mongoDB database
     * @param saliencyAnnotation
     * @param robotModel
     * @param type
     * @param resource
     *
     */
    public void createUpdateWorldModel(JsonObject saliencyAnnotation, RobotModel robotModel, ModelType type, String resource) {
        switch (type) {
            case OWL:
                try {
                    owlModelRetrieval(saliencyAnnotation, robotModel, resource);
                } catch (Exception e) {
                    throw new RuntimeException("Error while parsing owl file." , e );
                }
                break;
            case MONGODB:
                mongoDBModelRetrieval(saliencyAnnotation, robotModel, resource);
                break;
            default:
               throw new UnsupportedOperationException("Model type " + type + " not supported yet.");
        }

    }

    /**
     *
     * @param saliencyAnnotation
     * @param robotModel
     * @param databaseName
     * Retrieve object properties from a MongoDB database
     */
    private void mongoDBModelRetrieval(JsonObject saliencyAnnotation, RobotModel robotModel, String databaseName) {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase db = mongoClient.getDatabase(databaseName);
        MongoIterable<String> collectionNames = db.listCollectionNames();
        ArrayList<JsonObject> worldProperties = new ArrayList<>();
        for (String name : collectionNames) {
            FindIterable<Document> iterable = db.getCollection(name).find();
            iterable.forEach(new Block<Document>() {
                public void apply(final Document document) {
                    JsonObject jsonObject = (new JsonParser()).parse(document.toJson()).getAsJsonObject();
                    worldProperties.add(jsonObject);
                }
            });
        }
        postProcessWorldModel(worldProperties, saliencyAnnotation, robotModel);

    }


    /**
     *
     * @param saliencyAnnotation
     * @param robotModel
     * @param absoluteFilePath
     * @throws Exception
     * Retrieve object properties from an OWL Model
     */
    private void owlModelRetrieval(JsonObject saliencyAnnotation, RobotModel robotModel, String absoluteFilePath) throws Exception {
        OwlModelRetrieval owlModelRetrieval = new OwlModelRetrieval(absoluteFilePath);
        owlModelRetrieval.init();
        ArrayList<JsonObject> worldProperties = owlModelRetrieval.processRetrieval();
        postProcessWorldModel(worldProperties, saliencyAnnotation, robotModel);
    }

    /**
     *
     * @param worldProperties
     * @param saliencyAnnotation
     * @param robotModel
     */
    private void postProcessWorldModel(List<JsonObject> worldProperties, JsonObject saliencyAnnotation, RobotModel robotModel) {
        //set saliencyAnnotation to lower case
        jsonObjectToLowerCase(saliencyAnnotation);
        //set the saliencyAnnotation of the world model
        WorldModel.setSaliencyAnnotation(saliencyAnnotation);
        //all properties should be lower case
        for (int i = 0; i < worldProperties.size(); i++) {
            jsonObjectToLowerCase(worldProperties.get(i));
        }
        //the invisible-status of objects may change during execution (e.g. the image device could display an image which was invisible before)
        //should be insured that the current invisible status from the program is used and not possible old data from the database
        if(WorldModel.getWorldProperties() != null) {
            for(JsonObject object: WorldModel.getWorldProperties( )) {
                if(object.has("isinvisible")) {
                    String worldId = object.get("worldobjectid").getAsString();
                    for(JsonObject newObject: worldProperties) {
                        if(newObject.get("worldobjectid").getAsString().equals(worldId)){
                            newObject.add("isinvisible", object.get("isinvisible"));
                            break;
                        }
                    }
                }
            }
        }
        //set the world properties of the world model
        WorldModel.setWorldProperties(worldProperties);
       // System.out.println("worldproperties after processing: " + worldProperties.toString());

        //retrieve user models and the robot model and add it to the world model
        ArrayList<UserModel> userModels = new ArrayList<>();
        for(JsonObject worldObject: worldProperties) {
            if(worldObject.has("worldobjecttype")) {
                if(Objects.equals(worldObject.get("worldobjecttype").getAsString(), "robot")) {
                    robotModel.setRobotProperties(worldObject);
                }
                else if(Objects.equals(worldObject.get("worldobjecttype").getAsString(), "user")) {
                    UserModel userModel = new UserModel();
                    userModel.setUserProperties(worldObject);
                    userModels.add(userModel);
                }
            }
        }
        WorldModel.setRobotModel(robotModel);
        WorldModel.setUserModels(userModels);

        //calculate cone intersection of the world objects (used in the planning phase to determine if pointing is useful)
        WorldObjectConeIntersectionCalculator coneIntersectionCalculator = new WorldObjectConeIntersectionCalculator();
        Map<JsonObject, List<JsonObject>> coneIntersectionMap = coneIntersectionCalculator.calculateObjectsConeIntersection(worldProperties);
        WorldModel.setConeIntersectionMap(coneIntersectionMap);

    }


    private void jsonObjectToLowerCase(JsonObject object) {
        if (object.isJsonNull()) {
            return;
        }
        ArrayList<Map.Entry<String, JsonElement>> modifiedEntryList = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry: object.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                jsonObjectToLowerCase(entry.getValue().getAsJsonObject());
            }
            if (entry.getValue().isJsonArray()) {
                JsonArray array = entry.getValue().getAsJsonArray();
                jsonArrayToLowerCase(array);
                Map.Entry<String, JsonElement> newEntry =
                        new AbstractMap.SimpleEntry<String, JsonElement>(entry.getKey(), array);
                modifiedEntryList.add(newEntry);
            }
            if (entry.getValue().isJsonPrimitive()) {
                Gson gson = new Gson();
                String entryString = entry.getValue().getAsString().toLowerCase();
                String jsonString = gson.toJson(entryString);
                JsonElement element = gson.fromJson (jsonString, JsonElement.class);
                Map.Entry<String, JsonElement> newEntry =
                        new AbstractMap.SimpleEntry<String, JsonElement>(entry.getKey(), element);
                modifiedEntryList.add(newEntry);

            }

        }
        for(Map.Entry<String, JsonElement> newEntry: modifiedEntryList) {
            object.remove(newEntry.getKey());
            object.add(newEntry.getKey().toLowerCase(), newEntry.getValue());

        }
        return;
    }

    private void jsonArrayToLowerCase(JsonArray array) {
        ArrayList<Map.Entry<Integer, JsonElement>> modifiedArrayEntryList = new ArrayList<>();
        for(int i = 0; i < array.size(); i++) {
            if(array.get(i).isJsonObject()) {
                jsonObjectToLowerCase(array.get(i).getAsJsonObject());
            }
            if(array.get(i).isJsonArray()) {
                jsonArrayToLowerCase(array.get(i).getAsJsonArray());
            }
            if(array.get(i).isJsonPrimitive()) {
                Gson gson = new Gson();
                String entryString = array.get(i).getAsString().toLowerCase();
                String jsonString = gson.toJson(entryString);
                JsonElement element = gson.fromJson (jsonString, JsonElement.class);
                Map.Entry<Integer, JsonElement> newEntry =
                        new AbstractMap.SimpleEntry<Integer, JsonElement>(i, element);
               modifiedArrayEntryList.add(newEntry);
            }
        }
        for(Map.Entry<Integer, JsonElement>  entry: modifiedArrayEntryList) {
            array.set(entry.getKey(), entry.getValue());

        }
        return;

    }

}
