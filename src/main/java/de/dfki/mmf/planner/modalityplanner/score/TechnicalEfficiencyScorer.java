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

import de.dfki.mmf.modalities.ModalityType;
import de.dfki.mmf.planner.deviceplanner.PhraseModalityComponent;
import de.dfki.mmf.planner.modalityplanner.ModalityRepresentation;
import de.dfki.mmf.planner.modalityplanner.PhraseComponent;
import de.dfki.mmf.planner.modalityplanner.SpeechOutputType;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Magdalena Kaiser on 12.08.2016.
 */

/**
 * Scorer taking into account technical criteria like the fastness of the output
 */
public class TechnicalEfficiencyScorer extends AbstractScorer  {

    private int fastModalityScore = 0;
    //threshold set from outside to define what fast means for the given devices in the given context
    private double fastThreshold = -1.0;
    //threshold set from outside to define what slow means for the given devices in the given context
    private double slowThreshold = -1.0;

    public double getFastThreshold() {
        return fastThreshold;
    }

    public void setFastThreshold(double fastThreshold) {
        this.fastThreshold = fastThreshold;
    }

    public double getSlowThreshold() {
        return slowThreshold;
    }

    public void setSlowThreshold(double slowThreshold) {
        this.slowThreshold = slowThreshold;
    }

    private void clearScores() {
        fastModalityScore = 0;
    }

    public HardSoftScore calculateScore(ModalityRepresentation modalityRepresentation) {
        int softScore = 0;
        getFastestModality(modalityRepresentation);
        //if fast and slow Threshold are defined
        if(fastThreshold != -1.0 && slowThreshold != -1.0) {
            //check if output is slower than what is defined as fast
            if (fastModalityScore > fastThreshold) {
                //check if output is faster than what is defined as slow -> softscore-1, else softscore-2
                if(fastModalityScore < slowThreshold) {
                    softScore -= 1;
                }else {
                    softScore -= 2;
                }
            }
        }

        //reset individual scores for next round
        clearScores();

        return HardSoftScore.valueOf(0, softScore);
    }

    /**
     * use modalities which give the fastest output
     * @param modalityRepresentation
     * @return
     */
    public void getFastestModality(ModalityRepresentation modalityRepresentation) {
        // get argument - modality combination used in device planning
        List<PhraseModalityComponent> phraseModalityList = modalityRepresentation.getSolvedDeviceRepresentation().getPhraseModalityComponents();
        //go over all phrase components
        for(PhraseComponent phraseComponent: modalityRepresentation.getPhraseComponents()) {
            //get the current assigned set of modalities
            if(phraseComponent.getPowerSetModality() != null && !phraseComponent.getPowerSetModality().getModalitySet().isEmpty()) {
                Set<ModalityType> usedModalities = phraseComponent.getPowerSetModality().getModalitySet();
                ArrayList<PhraseModalityComponent> correspondingComponentList = new ArrayList<>();
                //search phrase component in corresponding argument - modality combination class
                for (PhraseModalityComponent phraseModalityComponent : phraseModalityList) {
                    if (phraseModalityComponent.getPredicateElement().toString().equals(phraseComponent.getPredicateElement().toString())) {
                        correspondingComponentList.add(phraseModalityComponent);
                    }
                }
                double possibleOutputDuration;
                double biggestOutputDuration = 0.0;
                for (PhraseModalityComponent correspondingComponent : correspondingComponentList) {
                    //get from the used modalities the slowest one
                    if (usedModalities.contains(correspondingComponent.getModality().getModalityType())) {
                        //check how long speech output takes
                        if(correspondingComponent.getModality().getModalityType().equals(ModalityType.SPEECH)) {
                            String speechString = "";
                            //check if component is object in world
                            if(phraseComponent.getAttributiveObjectIdentifier() != null) {
                                if(phraseComponent.getSpeechOutputType().equals(SpeechOutputType.THIS)) {
                                    speechString = "this";
                                }else if(phraseComponent.getSpeechOutputType().equals(SpeechOutputType.THIS_TYPE)) {
                                    speechString = "this " + phraseComponent.getAttributiveObjectIdentifier().getType();
                                }else if(phraseComponent.getSpeechOutputType().equals(SpeechOutputType.THE_TYPE)) {
                                    speechString = "the " + phraseComponent.getAttributiveObjectIdentifier().getType();
                                }else if(phraseComponent.getSpeechOutputType().equals(SpeechOutputType.IT)) {
                                    speechString = "it";
                                }
                                if(!speechString.equals("")) {
                                    possibleOutputDuration = correspondingComponent.getDevice().getDurationEstimation(speechString);
                                //use attributive identifier output and estimate how long it takes
                                }else {
                                    possibleOutputDuration = correspondingComponent.getDevice().getDurationEstimation(correspondingComponent.getModality().getModalityOutputMap().get(phraseComponent.getPredicateElement()));
                                }
                            //use speech output saved in map to estimate output
                            }else {
                                possibleOutputDuration = correspondingComponent.getDevice().getDurationEstimation(correspondingComponent.getModality().getModalityOutputMap().get(phraseComponent.getPredicateElement()));
                            }
                        }else {
                            //check how long output will take on chosen device for corresponding modality (which is not a speech modality)
                            possibleOutputDuration = correspondingComponent.getDevice().getDurationEstimation(correspondingComponent.getModality().getModalityOutputMap().get(phraseComponent.getPredicateElement()));
                        }
                        if (possibleOutputDuration > biggestOutputDuration) {
                            biggestOutputDuration = possibleOutputDuration;
                        }
                    }
                }
                fastModalityScore += biggestOutputDuration;
            }
        }
        //get average speed
        fastModalityScore = fastModalityScore/modalityRepresentation.getPhraseComponents().size();
    }
}
