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

package de.dfki.mmf.controller;

import com.google.gson.JsonObject;
import de.dfki.mmf.attributeselection.AttributiveObjectIdentifier;
import de.dfki.mmf.devices.Device;
import de.dfki.mmf.math.Position;
import de.dfki.mmf.history.OutputHistory;
import de.dfki.mmf.input.LanguageFormat;
import de.dfki.mmf.input.predicates.Predicate;
import de.dfki.mmf.input.predicates.PredicateAnnotation;
import de.dfki.mmf.input.predicates.PredicateElement;
import de.dfki.mmf.input.predicates.StringPredicateElement;
import de.dfki.mmf.input.templates.SimpleNLGTemplateParser;
import de.dfki.mmf.input.worldmodel.RobotModel;
import de.dfki.mmf.input.worldmodel.WorldModel;
import de.dfki.mmf.modalities.Modality;
import de.dfki.mmf.modalities.ModalityType;
import de.dfki.mmf.modalities.SpeechModality;
import de.dfki.mmf.modalities.StructureFormingModality;
import de.dfki.mmf.output.ComposedPlanComponent;
import de.dfki.mmf.output.PlanComponent;
import de.dfki.mmf.planner.deviceplanner.DeviceRepresentation;
import de.dfki.mmf.planner.deviceplanner.PhraseModalityComponent;
import de.dfki.mmf.planner.modalityplanner.ModalityRepresentation;
import de.dfki.mmf.planner.modalityplanner.PhraseComponent;
import de.dfki.mmf.planner.modalityplanner.PowerSetModality;
import de.dfki.mmf.planner.modalityplanner.score.AbstractScorer;
import de.dfki.mmf.planner.modalityplanner.score.GeneralHumanLikenessScorer;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

import java.util.*;

/**
 * Created by Magdalena Kaiser on 24.08.2016.
 */

/**
 * central controller of the framework
 */
public class Controller {
    //predicate which will be processed
    private Predicate predicate;
    //if predicate is given as string
    private String predicateString = "";
    //list with the current users the robot is interacting with
    private static List<String> talkingToUserList = new ArrayList<>();
    //the available modalities and devices
    private List<Modality> modalities = new ArrayList<>();
    private List<Device> devices = new ArrayList<>();
    //the used language format
    private LanguageFormat language;
    //list for the scorer which should be used for the modality selection
    private List<AbstractScorer> scorer = new ArrayList<>();

    public static List<String> getTalkingToUserList() {
        return talkingToUserList;
    }

    public Controller(Predicate predicate, LanguageFormat language, List<String> talkingToUserList) {
        this.predicate = predicate;
        for(String userid: talkingToUserList) {
            Controller.talkingToUserList.add(userid.toLowerCase());
        }
        this.language = language;
        init();
    }

    public Controller(String predicateString, LanguageFormat language, List<String> talkingToUserList) {
        this.predicateString = predicateString;
        for(String userid: talkingToUserList) {
            Controller.talkingToUserList.add(userid.toLowerCase());
        }
        this.language = language;
        init();
    }

    /**
     * initializes modalities and devices, parse template information for current predicate
     */
    private void init() {
        RobotModel robotModel = WorldModel.getRobotModel();
        modalities = robotModel.getModalities();
        devices = robotModel.getDevices();
        //currently only simpleNLG is supported (but other NLGs could be added)
        if(language != LanguageFormat.ENG_SIMPLENLG) {
           throw new UnsupportedOperationException("Create your own TemplateParser if SimpleNLG is not used.");
        }
        SimpleNLGTemplateParser templateParser = new SimpleNLGTemplateParser();
        //if predicate is only given as string -> create predicate
        if(!predicateString.equals("")) {
            this.predicate = templateParser.createPredicateFromString(predicateString);
        }
        //elements need to be lowercase
        if(predicate.getElements() != null) {
            for (int i = 0; i < predicate.getElements().length; i++) {
                predicate.getElements()[i] = new StringPredicateElement(predicate.getElements()[i].toString().toLowerCase());
            }
        }
        //parse template to retrieve annotations
        predicate = templateParser.parseTemplate(predicate);

        //for having a better output: replace robotId with "I" and the userIds with "you" if robot is talking to the specific user
        for(int i = 0; i < predicate.getElements().length; i++) {
            if(predicate.getElements()[i].toString().contains(robotModel.getRobotId())) {
                String robotString = predicate.getElements()[i].toString();
                String newRobotString = robotString.replace(robotModel.getRobotId(), "I");
                ((StringPredicateElement)predicate.getElements()[i]).setElement(newRobotString);
                predicate.getElements()[i].setWorldObjectType("robot");
                predicate.getElements()[i].setWorldObjectId(robotModel.getRobotId());

            }
            for(String userId: talkingToUserList) {
                if (predicate.getElements()[i].toString().contains(userId)) {
                    String userString = predicate.getElements()[i].toString();
                    String newUserString = userString.replace(userId, "you");
                    ((StringPredicateElement)predicate.getElements()[i]).setElement(newUserString);
                    predicate.getElements()[i].setWorldObjectType("user");
                    predicate.getElements()[i].setWorldObjectId(userId);
                }
            }
        }

    }

    public void setScorer(List<AbstractScorer> scorer) {
        this.scorer = scorer;
    }

    /**
     *
     * @return the resulting plan: sequence with <Modality, Device, Output> triples as ComposedPlanComponents
     */
    public List<ComposedPlanComponent> generatePlan() {

        //get preliminary output structure
        for(Modality modality: modalities) {
            if(modality.getModalityType().equals(ModalityType.SPEECH)) {
                if(!predicate.getPredicateAnnotations().contains(PredicateAnnotation.NO_NLG)) {
                    //generate the output structure and save structure as order of predicate elements in old predicate
                    predicate = ((StructureFormingModality) modality).generateOutputStructure(predicate);
                    String sentence = ((SpeechModality) modality).getOutputStructure();
                    System.out.println("Preliminary output structure: " + sentence);
                }
            }
        }

        //if element refers to object in the world -> save id and type of object in the element for later usage
        for(PredicateElement element: predicate.getElements()) {
            for (JsonObject objectProperties : WorldModel.getWorldProperties()) {
                if (objectProperties.has("worldobjectid") && objectProperties.has("worldobjecttype")) {
                    if (element.toString().contains(objectProperties.get("worldobjectid").getAsString())) {
                        //save worldobjectid and worldobjecttype of element
                        element.setWorldObjectId(objectProperties.get("worldobjectid").getAsString());
                        element.setWorldObjectType(objectProperties.get("worldobjecttype").getAsString());
                        break;
                    }
                }
            }
        }

        //fill for each modality its output map stating what to output for each element
        //output fixed for some like Position for Pointing Modality, can be updated later for others like Speech Modality)
        for(Modality modality: modalities) {
            modality.fillModalityOutputMap(predicate);
        }

        //save in map how well each modality can present each predicate element
        Map<ModalityType,Map<PredicateElement, Double>> modalityScorePresentabilityMap = new HashMap<>();
        for(Modality modality: modalities) {
            Map<PredicateElement, Double> scorePresentabilityMap = modality.scorePresentability(predicate);
            modalityScorePresentabilityMap.put(modality.getModalityType(), scorePresentabilityMap);
        }

        //plan which device to use for each modality based on different criteria
        DeviceRepresentation solvedDeviceRepresentation = planDevices(modalityScorePresentabilityMap);

        //plan which modalities to use based on different criteria
        ModalityRepresentation solvedModalityRepresentation = planModalities(solvedDeviceRepresentation, modalityScorePresentabilityMap);

        //create and return the output components
        return createOutputPlanComponents(solvedModalityRepresentation, solvedDeviceRepresentation);
    }


    /**
     * plan which device to use for each modality based on different criteria
     * @return DeviceRepresentation: containing for each modality the device which should be used
     */
    private DeviceRepresentation planDevices(Map<ModalityType,Map<PredicateElement, Double>> modalityScorePresentabilityMap) {
        //Build the solver
        SolverFactory<DeviceRepresentation> deviceSolverFactory = SolverFactory.createFromXmlResource(
                "DeviceRepresentationSolverConfig.xml");
        Solver<DeviceRepresentation> deviceSolver = deviceSolverFactory.buildSolver();

        ArrayList<PhraseModalityComponent> phraseModalityComponents = new ArrayList<>();
        for(PredicateElement element: predicate.getElements()) {
            for(Modality modality: modalities) {
                if(modality.getDevices().isEmpty()) {
                    System.out.println("Warning: modality has no corresponding device: " + modality.toString());
                    continue;
                }
                double presentationValue = 0.0;
                //retrieve current presentationValue for current modality and element from map
                if(modalityScorePresentabilityMap.containsKey(modality.getModalityType())
                        && modalityScorePresentabilityMap.get(modality.getModalityType()).containsKey(element)) {
                   presentationValue = modalityScorePresentabilityMap.get(modality.getModalityType()).get(element);
                }
                //if number is above 0.5 -> create PhraseModalityComponent (= a tuple of element and presenting modality)
                if(presentationValue > 0.5) {
                    PhraseModalityComponent phraseModalityComponent = new PhraseModalityComponent(element, modality);
                    phraseModalityComponents.add(phraseModalityComponent);
                    //if output will be a position set position for phraseModalityComponent (will be used in the planning process)
                    Map<PredicateElement, Object> modalityOutputMap = modality.getModalityOutputMap();
                    if(modalityOutputMap.containsKey(element)) {
                        if(modalityOutputMap.get(element) instanceof Position) {
                            phraseModalityComponent.setArgumentObjectPosition((Position)modalityOutputMap.get(element));
                        }
                    }
                }
            }
        }

        //set the main device planning unit and its properties
        DeviceRepresentation unsolvedDeviceRepresentation = new DeviceRepresentation();
        unsolvedDeviceRepresentation.setPhraseModalityComponents(phraseModalityComponents);
        unsolvedDeviceRepresentation.setDevices(devices);

        // Solve the problem
        DeviceRepresentation solvedDeviceRepresentation = deviceSolver.solve(unsolvedDeviceRepresentation);

        // Display the result
        System.out.println("\nSolved DeviceRepresentation for sentence: \n"
                + toDisplayDeviceRepresentation(solvedDeviceRepresentation));

        return solvedDeviceRepresentation;
    }

    private ModalityRepresentation planModalities(DeviceRepresentation solvedDeviceRepresentation, Map<ModalityType,Map<PredicateElement, Double>> modalityScorePresentabilityMap) {
        // Build the Solver
        SolverFactory<ModalityRepresentation> modalitySolverFactory = SolverFactory.createFromXmlResource(
                "ModalityRepresentationSolverConfig.xml");
        Solver<ModalityRepresentation> modalitySolver = modalitySolverFactory.buildSolver();

        ArrayList<PhraseComponent> phraseComponents = new ArrayList<>();
        //create the phrase components used in the modality planning process, each refers to one predicate element
        for(PredicateElement element: predicate.getElements()) {
            PhraseComponent phraseComponent = new PhraseComponent(element);
            phraseComponents.add(phraseComponent);
        }

        SpeechModality speechModality = null;
        for(PhraseComponent phraseComponent: phraseComponents) {
            //map for each element to state how well each modality can represent it
            EnumMap<ModalityType, Double> modalityRepresentationMap = new EnumMap<>(ModalityType.class);
            for (Modality modality : modalities) {
                //save speech modality for later usage
               if(modality.getModalityType().equals(ModalityType.SPEECH)) {
                   speechModality = (SpeechModality) modality;
               }
                double presentationValue = 0.0;
                //retrieve current presentationValue for current modality and element from map
                if(modalityScorePresentabilityMap.containsKey(modality.getModalityType())
                        && modalityScorePresentabilityMap.get(modality.getModalityType()).containsKey(phraseComponent.getPredicateElement())) {
                    presentationValue = modalityScorePresentabilityMap.get(modality.getModalityType()).get(phraseComponent.getPredicateElement());
                }
                //save modality, presentationvalue pair in new map for each phrase component
                modalityRepresentationMap.put(modality.getModalityType(), presentationValue);
            }
            phraseComponent.setModalityRepresentationMap(modalityRepresentationMap);
        }

        //get attributive identifier for each element which refer to world object
        for(PhraseComponent component: phraseComponents) {
            AttributiveObjectIdentifier identifier = null;
            try {
                if(speechModality != null) {
                    //test if world object id of the element has been set -> if so element refers to world object
                    if (component.getPredicateElement().getWorldObjectId() != null) {
                        //no search for attributive identifier if current robot or current user is referenced ("I", "you" should be used instead)
                        if (!(component.getPredicateElement().getWorldObjectType().equals("robot") ||
                                (component.getPredicateElement().getWorldObjectType().equals("user") && talkingToUserList.contains(component.getPredicateElement().getWorldObjectId())))) {
                            //search for attributive identifier
                            identifier = speechModality.findAttributiveObjectIdentifier(component.getPredicateElement());
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            //set identifier for the phrase component
            component.setAttributiveObjectIdentifier(identifier);
        }

        //create PowersetModalities -> each Phrasecomponent will have a different combination of modalities
        ArrayList<PowerSetModality> powerSetModalities = new ArrayList<>();
        Set<ModalityType> modalitySet = new HashSet<>();
        for(Modality modality: modalities) {
            modalitySet.add(modality.getModalityType());
        }
        //calculate the powerset of the modality type set
        Set<Set<ModalityType>> modPowerSet = powerSet(modalitySet);
        //each PowersetModality receives one such set
        for(Set<ModalityType> singleModPowerSet: modPowerSet) {
            powerSetModalities.add(new PowerSetModality(singleModPowerSet));
        }

       //create the main planning unit and add its properties
        ModalityRepresentation modalityRepresentation = new ModalityRepresentation();
        modalityRepresentation.setPhraseComponents(phraseComponents);
        modalityRepresentation.setPowerSetModalityList(powerSetModalities);
        modalityRepresentation.setSolvedDeviceRepresentation(solvedDeviceRepresentation);
        modalityRepresentation.setDefaultSpeechOutputTypeList();

        //set the scorer which should be used, if no scorer is specified use default one
        if(scorer != null && !scorer.isEmpty()) {
            modalityRepresentation.setScorer(scorer);
        }else {
            modalityRepresentation.setScorer(getStandardScorer());
        }

        // Solve the problem
        ModalityRepresentation solvedModalityRepresentation = modalitySolver.solve(modalityRepresentation);

        //generate object output description and write updated output into the modality output map
        for(PhraseComponent phraseComponent: solvedModalityRepresentation.getPhraseComponents()) {
            if(phraseComponent.getAttributiveObjectIdentifier() != null) {
                if(speechModality != null) {
                    //get object description based on planned speech output and identifier consisting of selected attributes
                    String speechOutput = speechModality.generateObjectDescriptionOutput(phraseComponent.getAttributiveObjectIdentifier(), phraseComponent.getSpeechOutputType());
                    //update output in modality output map
                    speechModality.updateModalityOutputMap(phraseComponent.getPredicateElement(), speechOutput);
                }
            }
        }

        // Display the result
        System.out.println("\nSolved ModalityRepresentation for sentence: \n"
                + toDisplayModalityRepresentation(solvedModalityRepresentation));

        return solvedModalityRepresentation;
    }

    /**
     *
     * @param solvedModalityRepresentation
     * @param solvedDeviceRepresentation
     * @return the resulting plan: sequence with <Modality, Output, Device> triples as ComposedPlanComponents
     */
    private  ArrayList<ComposedPlanComponent> createOutputPlanComponents(ModalityRepresentation solvedModalityRepresentation, DeviceRepresentation solvedDeviceRepresentation) {
        //create final composedPlanComponent list
        ArrayList<ComposedPlanComponent> composedPlanComponentList = new ArrayList<>();
        //map to save fore each element which refer to a world object the used composedPlanComponent
        HashMap<String, ComposedPlanComponent> argumentPlanComponentMap = new HashMap<>();
        //retrieve the triple <modality, output, device> from the solved modality and device planning
        for(PhraseComponent phraseComponent: solvedModalityRepresentation.getPhraseComponents()) {
            //create for each phraseComponent a composedPlanComponent
            ComposedPlanComponent composedPlanComponent = new ComposedPlanComponent();
            //get the modalities for the current phraseComponent found in the modality planning process
            Set<ModalityType> modalityTypeSet = phraseComponent.getPowerSetModality().getModalitySet();
            //get the corresponding device found in the device planning process
            for(ModalityType modalityType: modalityTypeSet) {
                Device device = null;
                for(PhraseModalityComponent phraseModalityComponent: solvedDeviceRepresentation.getPhraseModalityComponents()) {
                    if(Objects.equals(phraseModalityComponent.getPredicateElement().toString(), phraseComponent.getPredicateElement().toString())
                            && Objects.equals(phraseModalityComponent.getModality().getModalityType(), modalityType)) {
                        device = phraseModalityComponent.getDevice();
                    }
                }
                //retrieve for each of the used modalities its corresponding output
                for(Modality modality: modalities) {
                    if(modality.getModalityType().equals(modalityType)) {
                        Map<PredicateElement, Object> outputMap = modality.getModalityOutputMap();
                        Object output = outputMap.get(phraseComponent.getPredicateElement());
                        //create a plan component for this triple and add it to the composedPlanComponents
                        if(device != null) {
                            PlanComponent planComponent = new PlanComponent(modality, output,device);
                            composedPlanComponent.addComponent(planComponent);
                        }
                    }
                }
            }
            composedPlanComponentList.add(composedPlanComponent);
            //check if element refers to a world object -> save combination in map and add corresponding id and type for the outputHistory
            if(phraseComponent.getPredicateElement().getWorldObjectId() != null && phraseComponent.getPredicateElement().getWorldObjectType() != null) {
                argumentPlanComponentMap.put(phraseComponent.getPredicateElement().getWorldObjectId(), composedPlanComponent);
                OutputHistory.addToIdTypeHistory(phraseComponent.getPredicateElement().getWorldObjectId(), phraseComponent.getPredicateElement().getWorldObjectType());
            }
        }
        //set the latest output history
        OutputHistory.setLastOutputHistory(argumentPlanComponentMap);

        //clear modality output map and device output for the next usage
        for(Modality modality: modalities) {
            modality.clearModalityOutputMap();
        }
        for(Device device: devices) {
            device.clearOutput();
        }

        return composedPlanComponentList;
    }

    /**
     *
     * @param originalSet
     * @return powerset of originalset
     */
    private <T> Set<Set<T>> powerSet(Set<T> originalSet) {
        Set<Set<T>> sets = new HashSet<>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<T>());
            return sets;
        }
        List<T> list = new ArrayList<>(originalSet);
        T head = list.get(0);
        Set<T> rest = new HashSet<>(list.subList(1, list.size()));
        for (Set<T> set : powerSet(rest)) {
            Set<T> newSet = new HashSet<>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }

    /**
     *
     * @return default scorer: GeneralHumanLikenessScorer
     */
    private List<AbstractScorer> getStandardScorer() {
        GeneralHumanLikenessScorer humanLikenessScorer = new GeneralHumanLikenessScorer();
        scorer.add(humanLikenessScorer);
        return scorer;
    }


    /**
     *
     * @param representation
     * @return the list of modalities chosen for certain component in string format
     */
    public String toDisplayModalityRepresentation(ModalityRepresentation representation) {
        String resultString = "";
        for (PhraseComponent component : representation.getPhraseComponents()) {
            resultString += component.toString() + ": " + component.getPowerSetModality().getModalitySet().toString() + "\n";
        }
        return resultString;
    }

    /**
     *
     * @param representation
     * @return the list of devices chosen for certain predicate element and modality in string format
     */
    public String toDisplayDeviceRepresentation(DeviceRepresentation representation) {
        String resultString = "";
        for (PhraseModalityComponent component : representation.getPhraseModalityComponents()){
             resultString += "["+ component.getPredicateElement().toString() + ", " + component.getModality().toString() + "]:"
                    + component.getDevice().toString() + "\n";

        }
        return resultString;
    }
}
