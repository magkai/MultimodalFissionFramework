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

package de.dfki.mmf.planner.modalityplanner.score;

import de.dfki.mmf.devices.ImageDisplayingDevice;
import de.dfki.mmf.history.OutputHistory;
import de.dfki.mmf.modalities.ModalityType;
import de.dfki.mmf.output.ComposedPlanComponent;
import de.dfki.mmf.output.PlanComponent;
import de.dfki.mmf.planner.modalityplanner.ModalityRepresentation;
import de.dfki.mmf.planner.modalityplanner.PhraseComponent;
import de.dfki.mmf.planner.modalityplanner.SpeechOutputType;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.Map;

/**
 * Created by Magdalena Kaiser on 12.11.2016.
 */

/**
 * Scorer which takes previous output into account
 */
public class OutputHistoryScorer extends AbstractScorer {

    private int maxReducedScore = 0;
    private int speechOutputTypeScore = 0;
    private int pointingScore = 0;
    private int imageScore = 0;

    private void clearScores() {
        maxReducedScore = 0;
        speechOutputTypeScore = 0;
        pointingScore = 0;
        imageScore = 0;
    }

    @Override
    public Score calculateScore(ModalityRepresentation modalityRepresentation) {
        int softScore = 0;
        //calculate the output history scores considering different modalities and the speech output type
        calculateSpeechOutputTypePointingScore(modalityRepresentation);
        calculateImageScore(modalityRepresentation);

        //sum up single scores to receive overall output history softScore
        //used form of speech output is a special case
        // -> overwrite general case in ObjectIdentification scorer with multiplying with weight 3
        softScore += speechOutputTypeScore*3;
        //not using pointing is a special case
        // -> use bigger weight than the one used in the GeneralHumanLikeness scorer to overwrite default usage
        softScore += pointingScore*3;
        //not using image displaying (because image is already displayed) is a special case
        // -> use bigger weight than the one used in the GeneralHumanLikeness scorer to overwrite default usage
        softScore += imageScore*3;

        //increase the maxReducedScore by the new weights
        maxReducedScore += speechOutputTypeScore*2;
        maxReducedScore += pointingScore*2;
        maxReducedScore += imageScore*2;

        setMaximalReducedScore(maxReducedScore);
        //reset individual scores for next round
        clearScores();

        return HardSoftScore.valueOf(0, softScore);
    }

    /**
     * Calculate the output history softScore considering the pointing modality and the used speech output type
     * @param modalityRepresentation
     */
    public void calculateSpeechOutputTypePointingScore(ModalityRepresentation modalityRepresentation) {
        //get the idTypeHistoryMap to know which id belongs to which type
        Map<String, String> idTypeHistoryMap = OutputHistory.getIdTypeHistoryMap();
        //check if in latest output history current argument appear -> if so it is not necessary to use pointing, because focus is already on the object
        for (PhraseComponent argument : modalityRepresentation.getPhraseComponents()) {
            if (argument.getPowerSetModality() != null && !argument.getPowerSetModality().getModalitySet().isEmpty()) {
                if (argument.getPredicateElement().getWorldObjectId() != null && argument.getPredicateElement().getWorldObjectType() != null) {
                    //used to check if worldobjectid of current argument has been used in previous predicate
                    boolean containedArg = false;
                    //count how many objects with the same worldobjecttype have been referenced in the previous predicate
                    int typeCounter = 0;
                    //count how many objects have been referenced in the previous predicate
                    int allObjCounter = 0;
                    //get worldobjecttype of current argument
                    String worldObjectType = argument.getPredicateElement().getWorldObjectType();
                    //go through latest history
                    for (String historyString : OutputHistory.getLastOutputHistory().keySet()) {
                        //check if in latest history objects of type equal to type of current argument appeared
                        if (idTypeHistoryMap.containsKey(historyString) && idTypeHistoryMap.get(historyString).equals(worldObjectType)) {
                            typeCounter++;
                        }
                        if (idTypeHistoryMap.containsKey(historyString)) {
                            allObjCounter++;
                        }
                        //check if worldobjectid of current argument appeared
                        if (historyString.equals(argument.getPredicateElement().getWorldObjectId())) {
                            containedArg = true;
                        }
                    }
                    //only if worldobjectid appeared in previous output
                    if (containedArg) {
                        //if there haven't been several object of the same type as the argument
                        if (typeCounter < 2) {
                            //if only one object have been referenced in the previous output ("it" can be used to reference same one again)
                            if (allObjCounter < 2) {
                                if (!argument.getSpeechOutputType().equals(SpeechOutputType.IT)) {
                                    speechOutputTypeScore -= 1;
                                }
                            } else {
                                boolean singleType = true;
                                //check if there is an object with the same type in current component  -> then the type cannot be used
                                for(PhraseComponent otherArgument: modalityRepresentation.getPhraseComponents()) {
                                    if (otherArgument.getPredicateElement().getWorldObjectId() != null && otherArgument.getPredicateElement().getWorldObjectType() != null) {
                                        if(otherArgument.getPredicateElement().getWorldObjectType().equals(argument.getPredicateElement().getWorldObjectType())
                                            && !otherArgument.getPredicateElement().getWorldObjectId().equals(argument.getPredicateElement().getWorldObjectId())) {
                                            singleType = false;
                                        }
                                    }
                                }
                                //object was mentioned before and no other object of the same type in current sentence -> not necessary to use an attributive identifier (again)
                                if(singleType) {
                                    if (!(argument.getSpeechOutputType().equals(SpeechOutputType.THE_TYPE))) {
                                        speechOutputTypeScore -= 1;
                                    }
                                }
                            }
                            //if wrong speech type is used
                            maxReducedScore -= 1;
                        }
                        if (argument.getModalityRepresentationMap().containsKey(ModalityType.POINTING) && argument.getModalityRepresentationMap().get(ModalityType.POINTING) > 0.5) {
                            //pointing is not needed since the focus is already on the object
                            if (argument.getPowerSetModality().getModalitySet().contains(ModalityType.POINTING)) {
                                pointingScore -= 1;
                            }
                            //if pointing is used
                            maxReducedScore -= 1;
                        }
                    }
                }
            }
        }
    }

    /**
     * Calculate the output history softScore considering the image modality
     * @param modalityRepresentation
     */
    public void calculateImageScore(ModalityRepresentation modalityRepresentation) {
        //check if the image argument has been mentioned before and if the image is currently displayed -> if so, do not open it a second time on the same device
        for(PhraseComponent argument: modalityRepresentation.getPhraseComponents()) {
            if (argument.getPowerSetModality() != null && !argument.getPowerSetModality().getModalitySet().isEmpty()) {
                if (argument.getPowerSetModality().getModalitySet().contains(ModalityType.IMAGE) && argument.getModalityRepresentationMap().get(ModalityType.IMAGE) > 0.5) {
                    //go over whole output history
                    for (Map<String, ComposedPlanComponent> historyMaps : OutputHistory.getOutputHistoryList()) {
                        for (String historyString : historyMaps.keySet()) {
                            if (historyString.equals(argument.getPredicateElement().getWorldObjectId())) {
                                //get the used plan components
                                ComposedPlanComponent composedPlanComponent = historyMaps.get(historyString);
                                for (PlanComponent planComponent : composedPlanComponent.getPlanComponents()) {
                                    if (planComponent.getModality().getModalityType().equals(ModalityType.IMAGE)) {
                                        //check if device once used for displaying image is still displaying the image -> if so: prevent displaying it twice
                                        if (((ImageDisplayingDevice) planComponent.getDevice()).getCurrentDisplayedImages().contains(planComponent.getOutput())) {
                                            imageScore -= 1;
                                            break;
                                        }
                                    }
                                }
                                maxReducedScore -= 1;
                            }
                        }
                    }
                }
            }
        }
    }
}
