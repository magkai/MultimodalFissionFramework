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

package de.dfki.mmf.examples.vase_selling_example;

import com.google.gson.JsonObject;
import de.dfki.mmf.attributeselection.AttributeSelectorType;
import de.dfki.mmf.controller.Controller;
import de.dfki.mmf.devices.Device;
import de.dfki.mmf.input.LanguageFormat;
import de.dfki.mmf.input.worldmodel.ModelType;
import de.dfki.mmf.input.worldmodel.RobotModel;
import de.dfki.mmf.input.worldmodel.WorldModel;
import de.dfki.mmf.input.worldmodel.WorldModelFactory;
import de.dfki.mmf.math.Position;
import de.dfki.mmf.modalities.*;
import de.dfki.mmf.output.ComposedPlanComponent;
import de.dfki.mmf.output.PlanExecutor;
import de.dfki.mmf.planner.modalityplanner.score.*;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Magdalena Kaiser on 20.12.2016.
 */
public class VaseSellingMain {

    private List<Modality> modalities = new ArrayList<>();
    private List<Device> devices = new ArrayList<>();

    public static void main(String[] args) {

        VaseSellingMain main = new VaseSellingMain();
        //create the modalities and devices
        main.createModalityDevices();
        //create the saliency annotation
        JsonObject saliencyAnnotations = main.generateSaliencyAnnotations();
        //initialize the world model
        main.initializeWorldModel(saliencyAnnotations, ModelType.OWL, "vase-ontology.owl");

        //create the predicates which should be output after each other
        ArrayList<String> predicateStrings = new ArrayList<>();
        //Hello
        predicateStrings.add("coi()");
        //Vase4 is next to vase5
        predicateStrings.add("lamji(vase4, vase5, [zoe], [zoe])");
        //Do user1 see vase4?
        predicateStrings.add("[xu] viska(user1, vase4, [zoe])");
        //Vase1 is bigger than vase2
        predicateStrings.add("bramau(vase1, vase2, [zoe], [zoe])");
        //I show you image1
        predicateStrings.add("jarco(robot1, image1, user1)");
        //Image1 shows vase1
        predicateStrings.add("jarco(image1, vase1, [zoe])");
        //Image2 shows a colorful vase
        predicateStrings.add("jarco(image2, colorful vase, [zoe])");
        //The colorful vase is not available.
        predicateStrings.add("[na] nonseldia(colorful vase, [zoe], [zoe])");
        //Vase3 does not have color blue.
        predicateStrings.add("[na] skari(vase3, blue, [zoe], [zoe])");
        //I sell you vase3 for 70 euro
        predicateStrings.add("vecnu(robot1, vase4, user1, 70 euro)");
        //Do you buy vase3?
        predicateStrings.add("[xu] terveu(user1, vase4, [zoe], [zoe])");
        //Goodbye
        predicateStrings.add("coo()");

        ArrayList<String> talkingToUserList = new ArrayList<String>();
        //state to which user the robot is currently talking to
        talkingToUserList.add("User1");
        boolean modifiedColor = false;
        boolean modifiedSize = false;

        //go over each predicate
        for(String predicateString: predicateStrings) {
            //manually change saliency values for certain predicates in order to avoid tautologies
            if(predicateString.contains("skari")) {
                saliencyAnnotations.addProperty("color", "0.0");
                modifiedColor = true;
            }else if(predicateString.contains("bramau")) {
                saliencyAnnotations.addProperty("approximatesize", "0.0");
                modifiedSize = true;
            }
            //update the world model (some changes might have occurred from outside)
            main.updateWorldModel(saliencyAnnotations, WorldModel.getRobotModel(), ModelType.OWL, "vase-ontology.owl");
            Controller controller = new Controller(predicateString, LanguageFormat.ENG_SIMPLENLG, talkingToUserList);
            UserInfoScorer userInfoScorer = new UserInfoScorer();
            OutputHistoryScorer outputHistoryScorer = new OutputHistoryScorer();
            GeneralHumanLikenessScorer humanLikenessScorer = new GeneralHumanLikenessScorer();
            ObjectIdentificationScorer objectIdentificationScorer = new ObjectIdentificationScorer();
            ArrayList<AbstractScorer> scorers = new ArrayList<>();
            //set the scorers which should be used
            scorers.add(userInfoScorer);
            scorers.add(humanLikenessScorer);
            scorers.add(outputHistoryScorer);
            scorers.add(objectIdentificationScorer);
            controller.setScorer(scorers);
            //generate the plan
            List<ComposedPlanComponent> plan = controller.generatePlan();
            PlanExecutor executor = new PlanExecutor(plan);
            System.out.println("Final output: ");
            try {
                //execute the plan
                executor.executePlan();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //reset manually changed saliency values
            if(modifiedColor) {
                saliencyAnnotations.addProperty("color", "1.0");
                modifiedColor = false;
            }else if(modifiedSize) {
                saliencyAnnotations.addProperty("approximatesize", "0.8");
                modifiedSize = false;
            }

            System.out.println("\n\n");
        }
    }

    /**
     * state which modalities and devices are available and should be used
     */
    private void createModalityDevices() {
        //create modalities
        ModalityFactory modalityFactory = new ModalityFactory();
        Modality speechMod = modalityFactory.createModality(ModalityType.SPEECH);
        //choose one of the offered types to select the final attributes used for the object description
        //(default: Shortest_VisibleAboveThreshold with SaliencyThreshold = 0.8)
       // ((SpeechModality) speechMod).setAttributeSelectorType(AttributeSelectorType.MaximizedSaliency_MaximizedShortness);
        ((SpeechModality) speechMod).setAttributeSelectorType(AttributeSelectorType.SHORTEST_MOST_SALIENT_ABOVE_THRESHOLD);
        ((SpeechModality) speechMod).setFinalAttributeSelectionSaliencyThreshold(0.9);

        Modality pointMod = modalityFactory.createModality(ModalityType.POINTING);
        Modality gazeMod = modalityFactory.createModality(ModalityType.GAZE);
        Modality imageMod = modalityFactory.createModality(ModalityType.IMAGE);

        Modality waveMod = modalityFactory.createModality(ModalityType.WAVING);
        //add referenced predicates for waving
        ArrayList<String> predRefList = new ArrayList();
        predRefList.add("coi");
        predRefList.add("fii");
        predRefList.add("coo");
        ((WavingModality) waveMod).setReferencedPredicateNames(predRefList);

        //create devices
        Device speechDevice1 = new VaseSellingSpeechDevice1(Device.createDeviceID());
        speechDevice1.setDeviceName("speechDevice1");
        speechDevice1.setDevicePosition(new Position(5.0, 7.0, 7.0));
        Device speechDevice2 = new VaseSellingSpeechDevice2(Device.createDeviceID());
        speechDevice2.setDeviceName("speechDevice2");
        speechDevice2.setDevicePosition(new Position(2.0, 3.0, 3.0));
        speechMod.addDevice(speechDevice1);
        speechMod.addDevice(speechDevice2);

        Device pointDevice1 = new VaseSellingPointingDevice1(Device.createDeviceID());
        pointDevice1.setDeviceName("pointDevice1");
        pointDevice1.setDevicePosition(new Position(0.0, 2.0, 2.0));
        String pointWaveId = Device.createDeviceID();
        Device pointDevice2 = new VaseSellingPointingDevice2(pointWaveId);
        pointDevice2.setDeviceName("pointDevice2");
        pointDevice2.setDevicePosition(new Position(5.0, 3.0, 2.0));
        Device pointDevice3 = new VaseSellingPointingDevice3(Device.createDeviceID());
        pointDevice3.setDeviceName("pointDevice3");
        pointDevice3.setDevicePosition(new Position(5.0, 5.0, 3.0));
        pointMod.addDevice(pointDevice1);
        pointMod.addDevice(pointDevice2);
        pointMod.addDevice(pointDevice3);

        Device wavingDevice = new VaseSellingWavingDevice(pointWaveId);
        wavingDevice.setDeviceName("wavingDevice");
        wavingDevice.setDevicePosition(new Position(5.0, 3.0, 2.0));
        waveMod.addDevice(wavingDevice);

        Device gazeDevice = new VaseSellingGazeDevice(Device.createDeviceID());
        gazeDevice.setDeviceName("gazeDevice");
        gazeDevice.setDevicePosition(new Position(2.0, 9.0, 2.0));
        gazeMod.addDevice(gazeDevice);

        Device imageDevice = new VaseSellingImageDisplayingDevice(Device.createDeviceID());
        imageDevice.setDeviceName("imageDisplayingDevice");
        imageMod.addDevice(imageDevice);

        //add the individual modalities and devices into lists
        modalities.add(speechMod);
        modalities.add(pointMod);
        modalities.add(gazeMod);
        modalities.add(imageMod);
        modalities.add(waveMod);
        devices.add(speechDevice1);
        devices.add(speechDevice2);
        devices.add(pointDevice1);
        devices.add(pointDevice2);
        devices.add(pointDevice3);
        devices.add(gazeDevice);
        devices.add(imageDevice);
        devices.add(wavingDevice);

    }

    /**
     * Initialize the world model
     * @param saliencyAnnotations
     * @param type of the world model
     * @param dbName
     */
    private void initializeWorldModel(JsonObject saliencyAnnotations, ModelType type, String dbName) {
        RobotModel robotModel = new RobotModel();
        robotModel.setModalities(modalities);
        robotModel.setDevices(devices);
        //update structures for processing the world model
        updateWorldModel(saliencyAnnotations, robotModel, type, dbName);
    }

    /**
     * update, respectively initialize, the world model
     * @param saliencyAnnotations
     * @param robotModel
     * @param type
     * @param dbName
     */
    private void updateWorldModel(JsonObject saliencyAnnotations, RobotModel robotModel, ModelType type, String dbName) {
        WorldModelFactory worldModelFactory = new WorldModelFactory();
        //create/update the world model, different procedure for different kinds of models
        if(type.equals(ModelType.OWL)) {
            VaseSellingMain mainClass = new VaseSellingMain();
            URL resource = mainClass.getClass().getClassLoader().getResource("ExampleResources/VaseSellingExample/"+ dbName);
            String absolutePath = "";
            try {
                File file = Paths.get(resource.toURI()).toFile();
                absolutePath = file.getAbsolutePath();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            worldModelFactory.createUpdateWorldModel(saliencyAnnotations,robotModel, ModelType.OWL, absolutePath);
        }else  {
            worldModelFactory.createUpdateWorldModel(saliencyAnnotations, robotModel, ModelType.MONGODB, dbName);
        }
    }

    /**
     * JsonObject containing object properties and how salient they are (value between 0.0 - 1.0) to identify the object
     * @return saliencyAnnotation JsonObject
     */
    private JsonObject generateSaliencyAnnotations() {
        JsonObject saliencyAnnotation = new JsonObject();
        saliencyAnnotation.addProperty("worldobjectid", "0.0");
        saliencyAnnotation.addProperty("worldobjecttype", "0.0");
        saliencyAnnotation.addProperty("name", "1.0");
        saliencyAnnotation.addProperty("color", "1.0");
        saliencyAnnotation.addProperty("origin", "0.6");
        saliencyAnnotation.addProperty("material", "0.8");
        saliencyAnnotation.addProperty("approximateposition", "0.9");
        saliencyAnnotation.addProperty("approximatesize", "0.8");
        saliencyAnnotation.addProperty("imagesize", "0.8");
        saliencyAnnotation.addProperty("price", "0.49");
        saliencyAnnotation.addProperty("absoluteposition", "0.0");
        saliencyAnnotation.addProperty("xposition", "0.0");
        saliencyAnnotation.addProperty("yposition", "0.0");
        saliencyAnnotation.addProperty("zposition", "0.0");
        saliencyAnnotation.addProperty("preferredmodality", "0.0");

        return saliencyAnnotation;
    }
}
