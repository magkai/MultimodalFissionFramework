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

package de.dfki.mmf.examples.craft_task_example;

import com.google.gson.JsonObject;
import de.dfki.mmf.controller.Controller;
import de.dfki.mmf.devices.Device;
import de.dfki.mmf.input.LanguageFormat;
import de.dfki.mmf.input.predicates.Predicate;
import de.dfki.mmf.input.predicates.PredicateElement;
import de.dfki.mmf.input.predicates.StringPredicateElement;
import de.dfki.mmf.input.worldmodel.ModelType;
import de.dfki.mmf.input.worldmodel.RobotModel;
import de.dfki.mmf.input.worldmodel.WorldModel;
import de.dfki.mmf.input.worldmodel.WorldModelFactory;
import de.dfki.mmf.math.DirectionVector;
import de.dfki.mmf.math.Position;
import de.dfki.mmf.modalities.Modality;
import de.dfki.mmf.modalities.ModalityFactory;
import de.dfki.mmf.modalities.ModalityType;
import de.dfki.mmf.modalities.NoddingHeadShakingModality;
import de.dfki.mmf.output.ComposedPlanComponent;
import de.dfki.mmf.output.PlanComponent;
import de.dfki.mmf.output.PlanExecutor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;


/**
 * Created by Magdalena Kaiser on 23.08.2016.
 */
public class CraftTaskMain {

    private ArrayList<Modality> modalities = new ArrayList<>();
    private ArrayList<Device> devices = new ArrayList<>();
    
    public static void main(String[] args) {
        CraftTaskMain main = new CraftTaskMain();
        //create the database
        MongoDBWorldDescription mongoDBWorldDescription = new MongoDBWorldDescription();
        mongoDBWorldDescription.clearDatabase("craftdb");
        mongoDBWorldDescription.createCraftTaskDatabase();
        //create the modalities and devices which should be used
        main.createModalityDevices();
        //create a JsonObject containing world objects' properties together with a value indicating how salient each property is
        JsonObject saliencyAnnotation = main.generateSaliencyAnnotations();
        //inialize the world model
        main.initializeWorldModel(saliencyAnnotation, ModelType.MONGODB, "craftdb");
        ArrayList<Predicate> predicates = main.createPredicates();
         //start Nao's init
        main.callNaoInitNaoRest("init");
        //define which persons the robot is currently talking to
        ArrayList<String> talkingToUserList = new ArrayList<String>();
        talkingToUserList.add("User1");
        boolean saliencyChanged = false;
        //execute each of the predicates after each other
        for(Predicate predicate: predicates) {
            //redo a change in the saliency annotation
            if(saliencyChanged) {
                saliencyAnnotation.addProperty("size", "1.0");
                WorldModel.setSaliencyAnnotation(saliencyAnnotation);
                saliencyChanged = false;
            }
            //change saliency for a certain predicate manually (otherwise output could be the following: "the object with size big is bigger than ...")
            if(predicate.getPredicateName().toString().equals("bramau")) {
                if(saliencyAnnotation.has("size") && saliencyAnnotation.get("size").getAsString().equals("1.0")) {
                    saliencyAnnotation.addProperty("size", "0.0");
                    WorldModel.setSaliencyAnnotation(saliencyAnnotation);
                    saliencyChanged = true;
                }
            }
            //create the controller
            Controller controller = new Controller(predicate, LanguageFormat.ENG_SIMPLENLG, talkingToUserList);
            //generate the plan
            List<ComposedPlanComponent> plan = controller.generatePlan();
            //modify the plan to improve Nao's speech output
            List<ComposedPlanComponent> newPlan = main.naoSpeechExecutionHelper(plan);
            //create the executor which will distribute the output on the concrete devices
            PlanExecutor executor = new PlanExecutor(newPlan);
            System.out.println("Final output: ");
            try {
                //execute the plan
                executor.executePlan();
                //small pause after most predicates
                if(!predicate.getPredicateName().equals("coi") && !predicate.getPredicateName().equals("goi") && !predicate.getPredicateName().equals("na_goi")) {
                    sleep(3000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("\n\n");
        }
        //send Nao to its rest position
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
               
                resource = this.getClass().getClassLoader().getResource("ExampleResources/CraftTaskExample/NaoInit.py");
            }else if(initOrRest.equals("rest")) {
                resource = this.getClass().getClassLoader().getResource("ExampleResources/CraftTaskExample/NaoRest.py");
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
     * create the predicates used for this example
     * @return predicateList: list with the created predicates
     */
    private ArrayList<Predicate> createPredicates() {
        ArrayList<Predicate> predicateList = new ArrayList<>();
        //Hello
        Predicate predicate1a = new Predicate("coi");
        //Robot1 greets user1.
        PredicateElement[] arguments1 = new StringPredicateElement[3];
        arguments1[0] = new StringPredicateElement("Robot1");
        arguments1[1] = new StringPredicateElement("User1");
        arguments1[2] = new StringPredicateElement("[zoe]");
        Predicate predicate1 = new Predicate("rinsa", arguments1);
        //Robot1 helps user1 with the craft task.
        PredicateElement[] arguments2 = new StringPredicateElement[3];
        arguments2[0] = new StringPredicateElement("Robot1");
        arguments2[1] = new StringPredicateElement("User1");
        arguments2[2] = new StringPredicateElement("with the craft task");
        Predicate predicate2 = new Predicate("sidju", arguments2);
        //No.
        Predicate predicate3a = new Predicate("na_goi");
        //Scissors1 is bigger than scissors2
        PredicateElement[] arguments3b = new StringPredicateElement[4];
        arguments3b[0] = new StringPredicateElement("Scissors1");
        arguments3b[1] = new StringPredicateElement("Scissors2");
        arguments3b[2] = new StringPredicateElement("[zoe]");
        arguments3b[3] = new StringPredicateElement("[zoe]");
        Predicate predicate3b = new Predicate("bramau", arguments3b);
        //User1 needs scissors1 for cutting efficiently.
        PredicateElement[] arguments4 = new StringPredicateElement[3];
        arguments4[0] = new StringPredicateElement("User1");
        arguments4[1] = new StringPredicateElement("Scissors1");
        arguments4[2] = new StringPredicateElement("cutting efficiently");
        Predicate predicate4 = new Predicate("nitcu", arguments4);
        //Yes.
        Predicate predicate5a = new Predicate("goi");
        //Hammer1 belongs to toolbox1
        PredicateElement[] arguments5b = new StringPredicateElement[2];
        arguments5b[0] = new StringPredicateElement("Hammer1");
        arguments5b[1] = new StringPredicateElement("Toolbox1");
        Predicate predicate5b = new Predicate("cmima", arguments5b);
        //Sponge1 is next to scissors2.
        PredicateElement[] arguments6 = new StringPredicateElement[4];
        arguments6[0] = new StringPredicateElement("Sponge1");
        arguments6[1] = new StringPredicateElement("Scissors2");
        arguments6[2] = new StringPredicateElement("[zoe]");
        arguments6[3] = new StringPredicateElement("[zoe]");
        Predicate predicate6 = new Predicate("lamji", arguments6);
      
        predicateList.add(predicate1a);
        predicateList.add(predicate1);
        predicateList.add(predicate2);
        predicateList.add(predicate3a);
        predicateList.add(predicate3b);
        predicateList.add(predicate4);
        predicateList.add(predicate5a);
        predicateList.add(predicate5b);
        predicateList.add(predicate6);

        return predicateList;
    }

    /**
     * state which modalities and devices are available and should be used
     */
    private void createModalityDevices() {
        ModalityFactory modalityFactory = new ModalityFactory();

        //create the modalities
        Modality speechMod = modalityFactory.createModality(ModalityType.SPEECH);
        Modality pointMod = modalityFactory.createModality(ModalityType.POINTING);
        Modality gazeMod = modalityFactory.createModality(ModalityType.GAZE);
        Modality noddingHeadShakingMod = modalityFactory.createModality(ModalityType.NODDING_HEADSHAKING);
        //set the predicates which are referenced by the nodding/head shaking modality
        ArrayList<String> noddingList = new ArrayList<String>();
        ArrayList<String> headShakingList = new ArrayList<String>();
        noddingList.add("goi");
        headShakingList.add("na_goi");
        ((NoddingHeadShakingModality) noddingHeadShakingMod).setReferencedPredicateNoddingList(noddingList);
        ((NoddingHeadShakingModality) noddingHeadShakingMod).setReferencedPredicateHeadShakingList(headShakingList);

        //create the devices
        Device mySpeechDevice = new CraftTaskSpeechDevice(Device.createDeviceID());
        mySpeechDevice.setDeviceName("speechDevice");
        mySpeechDevice.setDevicePosition(new Position(0.0, 0.0, 0.1265));
        speechMod.addDevice(mySpeechDevice);

        Device myLeftPointDevice = new CraftTaskLeftPointingDevice(Device.createDeviceID());
        myLeftPointDevice.setDeviceName("leftPointDevice");
        myLeftPointDevice.setDevicePosition(new Position(0.0, 0.0900, 0.1059));
        myLeftPointDevice.setDirectionVector(new DirectionVector(0.0, 0.0, -0.29));
        Device myRightPointDevice = new CraftTaskRightPointingDevice(Device.createDeviceID());
        myRightPointDevice.setDeviceName("rightPointDevice");
        myRightPointDevice.setDevicePosition(new Position(0.0, -0.0900, 0.1059));
        myRightPointDevice.setDirectionVector(new DirectionVector(0.0, 0.0, -0.29));
        pointMod.addDevice(myLeftPointDevice);
        pointMod.addDevice(myRightPointDevice);

        //the physical device for gaze and nodding/head shaking is the same (therefore they receive same device id),
        //however the classes which give the execution commands have different run methods since different types of output will be executed
        String gazeNodId = Device.createDeviceID();
        Device myGazeDevice = new CraftTaskGazeDevice(gazeNodId);
        myGazeDevice.setDeviceName("gazeDevice");
        myGazeDevice.setDevicePosition(new Position(0.0, 0.0, 0.1265));
        gazeMod.addDevice(myGazeDevice);

        Device myNoddingHeadShakingDevice = new CraftTaskNoddingHeadShakingDevice(Device.createDeviceID());
        myNoddingHeadShakingDevice.setDeviceName("noddingHeadShakingDevice");
        myNoddingHeadShakingDevice.setDevicePosition(new Position(0.0, 0.0, 0.1265));
        noddingHeadShakingMod.addDevice(myNoddingHeadShakingDevice);

        //add the individual modalities and devices into lists
        modalities.add(speechMod);
        modalities.add(pointMod);
        modalities.add(gazeMod);
        modalities.add(noddingHeadShakingMod);
        devices.add(mySpeechDevice);
        devices.add(myLeftPointDevice);
        devices.add(myRightPointDevice);
        devices.add(myGazeDevice);
        devices.add(myNoddingHeadShakingDevice);
    }

    /**
     * Initialize the world model
     * @param saliencyAnnotations
     * @param type of the world model
     * @param dbName
     */
    private void initializeWorldModel(JsonObject saliencyAnnotations, ModelType type, String dbName) {
        //create the robot model
        RobotModel robotModel = new RobotModel();
        robotModel.setModalities(modalities);
        robotModel.setDevices(devices);
        //create/update structures for processing the world model
        WorldModelFactory worldModelFactory = new WorldModelFactory();
        //create/update the world model, different procedure for different kinds of models
        if(type.equals(ModelType.OWL)) {
            CraftTaskMain mainClass = new CraftTaskMain();
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
        saliencyAnnotation.addProperty("position", "0.0");
        saliencyAnnotation.addProperty("xposition", "0.0");
        saliencyAnnotation.addProperty("yposition", "0.0");
        saliencyAnnotation.addProperty("zposition", "0.0");
        saliencyAnnotation.addProperty("_id", "0.0");
        saliencyAnnotation.addProperty("belongsto", "0.0");
        saliencyAnnotation.addProperty("amount", "0.0");
        saliencyAnnotation.addProperty("content", "0.0");

        return saliencyAnnotation;
    }
}
