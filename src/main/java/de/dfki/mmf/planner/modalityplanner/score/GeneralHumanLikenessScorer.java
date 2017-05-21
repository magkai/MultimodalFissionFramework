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
/**
 * Created by Magdalena Kaiser on 19.06.2016.
 */

import de.dfki.mmf.modalities.ModalityType;
import de.dfki.mmf.planner.modalityplanner.ModalityRepresentation;
import de.dfki.mmf.planner.modalityplanner.PhraseComponent;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.Objects;

/**
 *  Scorers implementing some basic rules with the aim of appearing more human-like
 */
public class GeneralHumanLikenessScorer extends AbstractScorer {

    private int maxReducedScore = 0;
    private int speechScore = 0;
    private int predRefScore = 0;
    private int gazeScore = 0;
    private int pointingScore = 0;
    private int imageScore = 0;

    private void clearScores() {
        maxReducedScore = 0;
        speechScore = 0;
        predRefScore = 0;
        gazeScore = 0;
        pointingScore = 0;
        imageScore = 0;
    }

    public HardSoftScore calculateScore(ModalityRepresentation modalityRepresentation) {
        int softScore = 0;

        //calculate scores for human-like behavior considering different modalities
        calculateSpeechScore(modalityRepresentation);
        calculatePredicateRefModalityScore(modalityRepresentation);
        calculatePointingGazeScore(modalityRepresentation);
        calculateImageScore(modalityRepresentation);

        //sum up single scores to receive overall human-likeness softScore
        softScore += speechScore;
        softScore += predRefScore;
        softScore += gazeScore;
        //pointing is considered very important for identifying objects -> therefore receives double weight
        softScore += pointingScore * 2;
        softScore += imageScore;
        //since we used two times the pointingScore we need to add it to the maxReducedScore a second time as well
        maxReducedScore += pointingScore;

        setMaximalReducedScore(maxReducedScore);
        //reset individual scores for next round
        clearScores();

        return HardSoftScore.valueOf(0, softScore);
    }

    /**
     * Calculate human-likeness softScore considering the speech modality
     * @param modalityRepresentation
     */
    public void calculateSpeechScore(ModalityRepresentation modalityRepresentation) {
        //use always speech (other modalities as addition, language as main information transfer medium)
        for (PhraseComponent argument : modalityRepresentation.getPhraseComponents()) {
            if (argument.getPowerSetModality() != null && !argument.getPowerSetModality().getModalitySet().isEmpty()) {
                if (!argument.getPowerSetModality().getModalitySet().contains(ModalityType.SPEECH)) {
                    speechScore -= 1;
                }
                maxReducedScore -= 1;
            }
        }
    }

    /**
     * Calculate human-likeness softscore for predicate referencing modalities (nodding, head shaking, waving)
     * @param modalityRepresentation
     */
    public void calculatePredicateRefModalityScore(ModalityRepresentation modalityRepresentation) {
        //use waving, nodding and head shaking to enable more human-like behavior
        for (PhraseComponent argument : modalityRepresentation.getPhraseComponents()) {
            if (argument.getPowerSetModality() != null && !argument.getPowerSetModality().getModalitySet().isEmpty()) {
                //check if nodding/head-shaking is available
                if (argument.getModalityRepresentationMap().containsKey(ModalityType.NODDING_HEADSHAKING) && argument.getModalityRepresentationMap().get(ModalityType.NODDING_HEADSHAKING) > 0.5) {
                    if (!argument.getPowerSetModality().getModalitySet().contains(ModalityType.NODDING_HEADSHAKING)) {
                        predRefScore -= 1;
                    }
                    maxReducedScore -= 1;
                }
                //check if waving is available
                if(argument.getModalityRepresentationMap().containsKey(ModalityType.WAVING) && argument.getModalityRepresentationMap().get(ModalityType.WAVING) > 0.5) {
                    if (!argument.getPowerSetModality().getModalitySet().contains(ModalityType.WAVING)) {
                        predRefScore -= 1;
                    }
                    maxReducedScore -= 1;
                }
            }
        }
    }

    /**
     * Calculate human-likeness softScore considering the pointing and gaze modality
     * @param modalityRepresentation
     */
    public void calculatePointingGazeScore(ModalityRepresentation modalityRepresentation) {
        //prefer looking at certain user when referring to him/her instead of pointing
        //prefer pointing at things rather than just looking at them (more clear)
        for (PhraseComponent argument : modalityRepresentation.getPhraseComponents()) {
            if (argument.getPredicateElement().getWorldObjectType() != null) {
                if (argument.getPowerSetModality() != null && !argument.getPowerSetModality().getModalitySet().isEmpty()) {
                    //looking at objects and persons while talking about them is human-like
                    if (argument.getModalityRepresentationMap().containsKey(ModalityType.GAZE) && argument.getModalityRepresentationMap().get(ModalityType.GAZE) > 0.5) {
                        if (!argument.getPowerSetModality().getModalitySet().contains(ModalityType.GAZE)) {
                            gazeScore -= 1;
                        }
                        maxReducedScore -= 1;
                    }
                    //do not point at humans
                    if (Objects.equals(argument.getPredicateElement().getWorldObjectType(), "user")) {
                        if (argument.getModalityRepresentationMap().containsKey(ModalityType.POINTING) && argument.getModalityRepresentationMap().get(ModalityType.POINTING) > 0.5) {
                            if (argument.getPowerSetModality().getModalitySet().contains(ModalityType.POINTING)) {
                                pointingScore -= 1;
                            }
                            maxReducedScore -= 1;
                        }
                        //pointing at objects is very helpful to identify the object
                    } else if (!(Objects.equals(argument.getPredicateElement().getWorldObjectType(), "user")) && !(Objects.equals(argument.getPredicateElement().getWorldObjectType(), "robot"))) {
                        if (argument.getModalityRepresentationMap().containsKey(ModalityType.GAZE) && argument.getModalityRepresentationMap().containsKey(ModalityType.POINTING)) {
                            if (argument.getModalityRepresentationMap().containsKey(ModalityType.POINTING) && argument.getModalityRepresentationMap().get(ModalityType.POINTING) > 0.5) {
                                if (!argument.getPowerSetModality().getModalitySet().contains(ModalityType.POINTING)) {
                                    pointingScore -= 1;
                                }
                                maxReducedScore -= 1;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Calculate human-likeness softScore considering the image modality
     * @param modalityRepresentation
     */
    public void calculateImageScore(ModalityRepresentation modalityRepresentation) {
        //display image robot is talking about
        for (PhraseComponent argument : modalityRepresentation.getPhraseComponents()) {
            if (argument.getPowerSetModality() != null && !argument.getPowerSetModality().getModalitySet().isEmpty()) {
                if (argument.getModalityRepresentationMap().containsKey(ModalityType.IMAGE) && argument.getModalityRepresentationMap().get(ModalityType.IMAGE) > 0.5) {
                    if (!(argument.getPowerSetModality().getModalitySet().contains(ModalityType.IMAGE))) {
                        imageScore -= 1;
                    }
                    maxReducedScore -= 1;
                }
            }
        }
    }

}
