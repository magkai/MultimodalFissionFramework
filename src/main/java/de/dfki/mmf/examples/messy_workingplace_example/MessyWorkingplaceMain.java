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

package de.dfki.mmf.examples.messy_workingplace_example;


import com.google.gson.JsonObject;
import de.dfki.mmf.controller.Controller;
import de.dfki.mmf.devices.Device;
import de.dfki.mmf.history.OutputHistory;
import de.dfki.mmf.input.LanguageFormat;
import de.dfki.mmf.input.worldmodel.ModelType;
import de.dfki.mmf.input.worldmodel.RobotModel;
import de.dfki.mmf.input.worldmodel.WorldModelFactory;
import de.dfki.mmf.math.DirectionVector;
import de.dfki.mmf.math.Position;
import de.dfki.mmf.modalities.Modality;
import de.dfki.mmf.modalities.ModalityFactory;
import de.dfki.mmf.modalities.ModalityType;
import de.dfki.mmf.output.ComposedPlanComponent;
import de.dfki.mmf.output.PlanComponent;
import de.dfki.mmf.output.PlanExecutor;
import de.dfki.mmf.planner.modalityplanner.score.AbstractScorer;
import de.dfki.mmf.planner.modalityplanner.score.GeneralHumanLikenessScorer;
import de.dfki.mmf.planner.modalityplanner.score.ObjectIdentificationScorer;
import de.dfki.mmf.planner.modalityplanner.score.OutputHistoryScorer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;


/**
 * Created by Magdalena Kaiser on 08.12.2016.
 */
public class MessyWorkingplaceMain {

    private List<Modality> modalities = new ArrayList<>();
    private List<Device> devices = new ArrayList<>();


    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        //flag to set current group (defines which predicates are used, 1 or 2 possible)
        int group = 1;
        //flag to set speech only or multimodality
        boolean speechOnly = false;
        //create the database
        MongoDBWorldDescription mongoDBWorldDescription = new MongoDBWorldDescription();
        mongoDBWorldDescription.clearDatabase("userstudydb");
        mongoDBWorldDescription.createUserStudyDatabase();

        MessyWorkingplaceMain main = new MessyWorkingplaceMain();
        //start Nao's init
        main.callNaoInitNaoRest("init");
        //create corresponding devices
        if(speechOnly) {
            main.createSpeechOnlyModalityDevice();
        }else {
            main.createModalitiesDevices();
        }
        JsonObject saliencyAnnotation = main.generateSaliencyAnnotations();
        //create the world model
        main.initializeWorldModel(saliencyAnnotation, ModelType.MONGODB, "userstudydb");
        ArrayList<String> predicateStrings = new ArrayList<>();
        //use corresponding predicates
        if(group == 1) {
            predicateStrings = main.createPredicatesGroup1();
        }else if(group == 2) {
            predicateStrings = main.createPredicatesGroup2();
        }
        //define to whom robot is talking to
        ArrayList<String> talkingToUserList = new ArrayList<String>();
        talkingToUserList.add("User2");
        //define which scorers to use
        OutputHistoryScorer outputHistoryScorer = new OutputHistoryScorer();
        GeneralHumanLikenessScorer humanLikenessScorer = new GeneralHumanLikenessScorer();
        ObjectIdentificationScorer objectIdentificationScorer = new ObjectIdentificationScorer();
        ArrayList<AbstractScorer> scorers = new ArrayList<>();
        scorers.add(humanLikenessScorer);
        scorers.add(outputHistoryScorer);
        scorers.add(objectIdentificationScorer);


        boolean modifiedColor = false;
        boolean modifiedSize = false;
        Map<String, ComposedPlanComponent> lastOutputHistory = new HashMap<>();
        //go over the predicates
        for(int i = 0; i < predicateStrings.size(); i++) {
            //wait for user input -> either repeat previous predicate or continue
            System.out.println("Enter 're' for 'repeat' anything else to continue.");
            Controller controller;
            String userInput = input.nextLine();
            //repeat previous predicate, delete the set output history to have same basis as before
            if (userInput.equals("re") && i > 0) {
                OutputHistory.clearLastOutputHistory();
                if(!lastOutputHistory.isEmpty()) {
                    OutputHistory.setLastOutputHistory(lastOutputHistory);
                }
                //create the controller
                controller = new Controller(predicateStrings.get(i - 1), LanguageFormat.ENG_SIMPLENLG, talkingToUserList);
                i--;
            }else {
                 //create the controller
                controller = new Controller(predicateStrings.get(i), LanguageFormat.ENG_SIMPLENLG, talkingToUserList);
                //get the latest output history
                if(i != predicateStrings.size()-1) {
                    lastOutputHistory = OutputHistory.getLastOutputHistory();
                }
            }
            //manually change saliency values for certain predicates in order to avoid tautologies
            if(predicateStrings.get(i).contains("skari")) {
                saliencyAnnotation.addProperty("color", "0.0");
                modifiedColor = true;
            }else if(predicateStrings.get(i).contains("bramau")) {
                saliencyAnnotation.addProperty("size", "0.0");
                modifiedSize = true;
            }
            if(controller != null) {
                controller.setScorer(scorers);
                //generate the plan
                List<ComposedPlanComponent> plan = controller.generatePlan();
                //modify the plan to improve Nao's speech output
                List<ComposedPlanComponent> newPlan = main.naoSpeechExecutionHelper(plan);
                PlanExecutor executor = new PlanExecutor(newPlan);
                System.out.println("Final output: ");
                //execute the plan
                try {
                    executor.executePlan();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            if(modifiedColor) {
                saliencyAnnotation.addProperty("color", "1.0");
                modifiedColor = false;
            }else if(modifiedSize) {
                saliencyAnnotation.addProperty("size", "0.9");
                modifiedSize = false;
            }
        }
        System.out.println("Enter 're' for 'repeat' anything else to continue.");
        Controller controller;
        //last output may be also repeated several times
        String userInput = input.nextLine();
        while (userInput.equals("re")) {
            OutputHistory.clearLastOutputHistory();
            OutputHistory.setLastOutputHistory(lastOutputHistory);
            controller = new Controller(predicateStrings.get(predicateStrings.size()-1), LanguageFormat.ENG_SIMPLENLG, talkingToUserList);
            if(controller != null) {
                controller.setScorer(scorers);
                List<ComposedPlanComponent> plan = controller.generatePlan();
                List<ComposedPlanComponent> newPlan = main.naoSpeechExecutionHelper(plan);
                PlanExecutor executor = new PlanExecutor(newPlan);
                System.out.println("Final output: ");
                try {
                    executor.executePlan();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                userInput = input.nextLine();
            }
        }
        //send Nao to rest
        main.callNaoInitNaoRest("rest");

    }

    /**
     *  either let the Nao robot go to its init position or to its rest position
     * @param initOrRest
     */
    public void callNaoInitNaoRest(String initOrRest) {
        Process p = null;
        try {
            URL resource;
            if(initOrRest.equals("init")) {
               
                resource = this.getClass().getClassLoader().getResource("ExampleResources/MessyWorkingplaceExample/NaoInit.py");
            }else if(initOrRest.equals("rest")) {
                resource = this.getClass().getClassLoader().getResource("ExampleResources/MessyWorkingplaceExample/NaoRest.py");
            }else {
                return;
            }
            String absolutePath = "";
            try {
                File file = Paths.get(resource.toURI()).toFile();
                absolutePath = file.getAbsolutePath();
                p = Runtime.getRuntime().exec(new String[] {"python", absolutePath});

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * create the predicates for group 1
     * @return list of predicates
     */
    private ArrayList<String> createPredicatesGroup1() {
        ArrayList<String> predicateStrings = new ArrayList<>();
        //Is markerpen3 next to cup1?
        predicateStrings.add("[xu] lamji(markerpen3, cup1, [zoe], [zoe])");
        //Do user2 see colorpen1?
        predicateStrings.add("[xu] viska(you, coloredpencil1, [zoe])");
        //Put colorpen1 in cup1.
        predicateStrings.add("[ko] punji([zoe], coloredpencil1, into cup1)");
        //Put ballpen2 in cup1.
        predicateStrings.add("[ko] punji([zoe], ballpen2, into cup1)");
        //Do user2 prefer plate1 over plate3?
        predicateStrings.add("[xu] zmanei(you, plate1, plate3, [zoe], [zoe])");
        //Put markerpen4 in cup1.
        predicateStrings.add("[ko] punji([zoe], markerpen4, into cup2)");
        //Use scissors1 to cut paper1!
        predicateStrings.add("[ko] pilno([zoe], scissors1, to cut paper1)");
        return  predicateStrings;
    }

    /**
     * create the predicates for group 2
     * @return list of predicates
     */
    private ArrayList<String> createPredicatesGroup2() {
        ArrayList<String> predicateStrings = new ArrayList<>();
        //Is scissor2 bigger than scissors1?
        predicateStrings.add("[xu] bramau(scissors2, scissors1, [zoe], [zoe])");
        //Is MarkerPen2 of color yellow?
        predicateStrings.add("[xu] skari(markerPen2, yellow, [zoe], [zoe])");
        //Use MarkerPen2 for drawing a line on paper1.
        predicateStrings.add("[ko] pilno([zoe], markerPen2, to draw a line on paper1)");
        //Use colorpen2 for drawing a line on paper1.
        predicateStrings.add("[ko] pilno([zoe], coloredpencil2, to draw a line on paper1)");
        //What is next to plate2?
        predicateStrings.add("lamji([ma], plate2, [zoe], [zoe])");
        //Use MarkerPen1 for drawing a line on paper1.
        predicateStrings.add("[ko] pilno([zoe], markerPen1, to draw a line on paper1)");
        //Give ballpen3 to person1.
        predicateStrings.add("[ko] dunda([zoe], ballpen3, person1)");
        return  predicateStrings;
    }

    /**
     * only create speech modalities and devices
     */
    private void createSpeechOnlyModalityDevice() {
        ModalityFactory modalityFactory = new ModalityFactory();
        Modality speechMod = modalityFactory.createModality(ModalityType.SPEECH);
        Device mySpeechDevice = new MessyWorkingplaceSpeechDevice(Device.createDeviceID());
        mySpeechDevice.setDeviceName("speechDevice");
        mySpeechDevice.setDevicePosition(new Position(0.0, 0.0, 0.1265));
        speechMod.addDevice(mySpeechDevice);
        modalities.add(speechMod);
        devices.add(mySpeechDevice);
    }

    /**
     * state which modalities and devices are available and should be used
     */
    private void createModalitiesDevices() {
        //create the modalities
        ModalityFactory modalityFactory = new ModalityFactory();
        Modality speechMod = modalityFactory.createModality(ModalityType.SPEECH);
        Modality pointMod = modalityFactory.createModality(ModalityType.POINTING);
        Modality gazeMod = modalityFactory.createModality(ModalityType.GAZE);

        //create the devices
        Device mySpeechDevice = new MessyWorkingplaceSpeechDevice(Device.createDeviceID());
        mySpeechDevice.setDeviceName("speechDevice");
        mySpeechDevice.setDevicePosition(new Position(0.0, 0.0, 0.1265));
        speechMod.addDevice(mySpeechDevice);

        Device myLeftPointDevice = new MessyWorkingplaceLeftPointingDevice(Device.createDeviceID());
        myLeftPointDevice.setDeviceName("leftPointDevice");
        myLeftPointDevice.setDevicePosition(new Position(0.0, 0.0900, 0.1059));
        myLeftPointDevice.setDirectionVector(new DirectionVector(0.0, 0.0, -0.29));
        Device myRightPointDevice = new MessyWorkingplaceRightPointingDevice(Device.createDeviceID());
        myRightPointDevice.setDeviceName("rightPointDevice");
        myRightPointDevice.setDevicePosition(new Position(0.0, -0.0900, 0.1059));
        myRightPointDevice.setDirectionVector(new DirectionVector(0.0, 0.0, -0.29));
        pointMod.addDevice(myLeftPointDevice);
        pointMod.addDevice(myRightPointDevice);

        Device myGazeDevice = new MessyWorkingplaceGazeDevice(Device.createDeviceID());
        myGazeDevice.setDeviceName("gazeDevice");
        myGazeDevice.setDevicePosition(new Position(0.0, 0.0, 0.1265));
        gazeMod.addDevice(myGazeDevice);

        //add the individual modalities and devices into lists
        modalities.add(speechMod);
        modalities.add(pointMod);
        modalities.add(gazeMod);
        devices.add(mySpeechDevice);
        devices.add(myLeftPointDevice);
        devices.add(myRightPointDevice);
        devices.add(myGazeDevice);
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
            MessyWorkingplaceMain mainClass = new MessyWorkingplaceMain();
            URL resource = mainClass.getClass().getClassLoader().getResource(dbName);
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
     *  in order to let Nao sound more natural: put successive speech output together
     * @param plan the original plan
     * @return the modified plan
     */
    public List<ComposedPlanComponent> naoSpeechExecutionHelper(List<ComposedPlanComponent> plan) {
        List<ComposedPlanComponent> modifiedplan = new ArrayList<ComposedPlanComponent>();
        boolean singleSpeechOutput = false;
        ComposedPlanComponent previousComponent = null;
        for(ComposedPlanComponent composedPlanComponent: plan) {
             //check if current plan component only contains speech modality and previous one as well
            if(singleSpeechOutput && composedPlanComponent.getPlanComponents().size() == 1 && composedPlanComponent.getPlanComponents().get(0).getModality().getModalityType().equals(ModalityType.SPEECH)) {
                String previousOutput = (String) previousComponent.getPlanComponents().get(0).getOutput();
                //combine previous and current speech output and add it to current component
                composedPlanComponent.getPlanComponents().get(0).setOutput(previousOutput + " " + composedPlanComponent.getPlanComponents().get(0).getOutput().toString().toLowerCase());
            }
            //check if current plan component only contains speech modality
            else if(composedPlanComponent.getPlanComponents().size() == 1 && composedPlanComponent.getPlanComponents().get(0).getModality().getModalityType().equals(ModalityType.SPEECH)) {
                singleSpeechOutput = true;
                //add previous one to the new plan
                if(previousComponent != null) {
                    modifiedplan.add(previousComponent);
                }
            }else {
                singleSpeechOutput = false;
                //add previous one to the new plan
                if(previousComponent != null) {
                    modifiedplan.add(previousComponent);
                }
            }
            previousComponent = composedPlanComponent;
        }
        //add last component to the new plan
        if(previousComponent != null) {
            modifiedplan.add(previousComponent);
        }
        //prevent having "." and "?" as single speech output -> add them to previous output even if that output was not speech only
        if(previousComponent.getPlanComponents().size() == 1 && previousComponent.getPlanComponents().get(0).getModality().getModalityType().equals(ModalityType.SPEECH)) {
            if(modifiedplan.size() > 1) {
                ComposedPlanComponent composedPlanComponent = modifiedplan.get(modifiedplan.size()-2);
                if(composedPlanComponent.getPlanComponents().size() > 1) {
                    String previousOutput = (String) previousComponent.getPlanComponents().get(0).getOutput();
                    for(PlanComponent component: composedPlanComponent.getPlanComponents()) {
                        if(component.getModality().getModalityType().equals(ModalityType.SPEECH)) {
                            component.setOutput(component.getOutput().toString().toLowerCase()+ " " + previousOutput);
                            break;
                        }
                    }
                    modifiedplan.remove(modifiedplan.size()-1);
                }
            }
        }
        //set the new plan
        if(modifiedplan != null) {
            plan = modifiedplan;
        }
        return plan;
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
        saliencyAnnotation.addProperty("size", "1.0");
        saliencyAnnotation.addProperty("approximate Position", "0.8");
        saliencyAnnotation.addProperty("label", "0.8");
        saliencyAnnotation.addProperty("motif", "0.8");
        saliencyAnnotation.addProperty("material", "0.8");
        saliencyAnnotation.addProperty("position", "0.0");
        saliencyAnnotation.addProperty("xposition", "0.0");
        saliencyAnnotation.addProperty("yposition", "0.0");
        saliencyAnnotation.addProperty("zposition", "0.0");
        saliencyAnnotation.addProperty("_id", "0.0");

        return saliencyAnnotation;
    }
}
