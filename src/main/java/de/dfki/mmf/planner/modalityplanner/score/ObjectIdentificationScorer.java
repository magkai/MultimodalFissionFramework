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

import com.google.gson.JsonObject;
import de.dfki.mmf.input.worldmodel.WorldModel;
import de.dfki.mmf.modalities.ModalityType;
import de.dfki.mmf.planner.modalityplanner.ModalityRepresentation;
import de.dfki.mmf.planner.modalityplanner.PhraseComponent;
import de.dfki.mmf.planner.modalityplanner.SpeechOutputType;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.List;

/**
 * Created by Magdalena Kaiser on 27.11.2016.
 */

/**
 * Scorer that chooses appropriate speech output and fitting pointing action for identifying world objects
 */
public class ObjectIdentificationScorer extends AbstractScorer {

    private int maxReducedScore = 0;
    private int pointingObjectDescriptionScore = 0;
    private int speechTypeObjectDescriptionScore = 0;
    private int pointingObjectProximityScore = 0;
    private int speechTypeObjectProximityScore = 0;

    private void clearScores() {
        maxReducedScore = 0;
        pointingObjectDescriptionScore = 0;
        speechTypeObjectDescriptionScore = 0;
        pointingObjectProximityScore = 0;
        speechTypeObjectProximityScore = 0;
    }

    public HardSoftScore calculateScore(ModalityRepresentation modalityRepresentation) {
        int softScore = 0;

        //calculate scores considering available attributive object identifier and the proximity to other objects of the same type
        //the most suitable speech output type and performing pointing action or not should be determined
        calculateScoreObjectDescription(modalityRepresentation);
        calculateScoreObjectProximity(modalityRepresentation);

        //sum up single scores to receive overall softScore for a good object identification
        softScore += pointingObjectDescriptionScore;
        softScore += speechTypeObjectDescriptionScore;
        //special case: multiply pointingObjectProximityScore with 3 to overwrite general case were pointing is seen as very useful
        softScore += pointingObjectProximityScore*3;
        softScore += speechTypeObjectProximityScore;

        maxReducedScore += pointingObjectProximityScore*2;
        setMaximalReducedScore(maxReducedScore);
        //reset individual scores for next round
        clearScores();

        return HardSoftScore.valueOf(0, softScore);
    }

    /**
     * Calculate softScore considering whether an attributive object identifier is available or not
     * @param modalityRepresentation
     */
    public void calculateScoreObjectDescription(ModalityRepresentation modalityRepresentation) {
        for (PhraseComponent argument : modalityRepresentation.getPhraseComponents()) {
            if (argument.getPowerSetModality() != null && !argument.getPowerSetModality().getModalitySet().isEmpty()) {
                if (argument.getAttributiveObjectIdentifier() != null) {
                    //a unique attributive identifier has not been found or only a partial one has been found -> use pointing for clarification
                    if (!argument.getAttributiveObjectIdentifier().hasSuccess()) {
                        if (argument.getModalityRepresentationMap().containsKey(ModalityType.POINTING) && argument.getModalityRepresentationMap().get(ModalityType.POINTING) > 0.5) {
                            if (!argument.getPowerSetModality().getModalitySet().contains(ModalityType.POINTING)) {
                                pointingObjectDescriptionScore -= 1;
                            }
                            maxReducedScore -= 1;
                        }
                        if (!argument.getSpeechOutputType().equals(SpeechOutputType.THE_TYPE)) {
                            speechTypeObjectDescriptionScore -= 1;
                        }
                        maxReducedScore -= 1;
                    } else if (argument.getAttributiveObjectIdentifier().isPartialIdentifiable()) {
                        if (argument.getModalityRepresentationMap().containsKey(ModalityType.POINTING) && argument.getModalityRepresentationMap().get(ModalityType.POINTING) > 0.5) {
                            if (!argument.getPowerSetModality().getModalitySet().contains(ModalityType.POINTING)) {
                                pointingObjectDescriptionScore -= 1;
                            }
                            maxReducedScore -= 1;
                        }
                        if (!(argument.getSpeechOutputType().equals(SpeechOutputType.ATTRIBUTIVE_IDENTIFIER) || argument.getSpeechOutputType().equals(SpeechOutputType.THIS_TYPE))) {
                            speechTypeObjectDescriptionScore -= 1;
                        }
                        maxReducedScore -= 1;
                    //use attributive identifier for object referencing as default
                    } else {
                        if (!argument.getSpeechOutputType().equals(SpeechOutputType.ATTRIBUTIVE_IDENTIFIER)) {
                            speechTypeObjectDescriptionScore -= 1;
                        }
                        maxReducedScore -= 1;
                    }
                }
            }
        }
    }

    /**
     * Calculate softScore considering the object's proximity to other objects of the same type
     * @param modalityRepresentation
     */
    public void calculateScoreObjectProximity(ModalityRepresentation modalityRepresentation) {
        //check if pointing is problematic since there are other objects of same type in its near neighborhood
        for (PhraseComponent argument : modalityRepresentation.getPhraseComponents()) {
            if (argument.getPowerSetModality() != null && !argument.getPowerSetModality().getModalitySet().isEmpty()) {
                //only needed if pointing is possible
                if (argument.getModalityRepresentationMap().containsKey(ModalityType.POINTING) && argument.getModalityRepresentationMap().get(ModalityType.POINTING) > 0.5) {
                    if (argument.getAttributiveObjectIdentifier() != null && argument.getAttributiveObjectIdentifier().hasSuccess() && !argument.getAttributiveObjectIdentifier().isPartialIdentifiable()) {
                        for (JsonObject keyObject : WorldModel.getConeIntersectionMap().keySet()) {
                            if (argument.toString().contains(keyObject.get("worldobjectid").getAsString())) {
                                List<JsonObject> proximityList = WorldModel.getConeIntersectionMap().get(keyObject);
                                for (JsonObject closeObject : proximityList) {
                                    //using "this" or only "this" + type is not good if there are several objects of same type in environment
                                    if (closeObject.get("worldobjecttype").getAsString().equals(argument.getAttributiveObjectIdentifier().getType())) {
                                        if (argument.getSpeechOutputType().equals(SpeechOutputType.THIS)) {
                                            speechTypeObjectProximityScore -= 2;
                                        } else if (argument.getSpeechOutputType().equals(SpeechOutputType.THIS_TYPE) || argument.getSpeechOutputType().equals(SpeechOutputType.THE_TYPE)) {
                                            speechTypeObjectProximityScore -= 1;
                                        } else if (!argument.getSpeechOutputType().equals(SpeechOutputType.ATTRIBUTIVE_IDENTIFIER)) {
                                            speechTypeObjectProximityScore -= 1;
                                        }
                                        //using pointing is not a good idea if there are objects of same type close
                                        if (argument.getPowerSetModality().getModalitySet().contains(ModalityType.POINTING)) {
                                            pointingObjectProximityScore -= 1;
                                        }
                                        //maximal 2 for speech type and 1 for pointing can be reduced
                                        maxReducedScore -= 3;
                                        //not much difference if there are two objects of the same type or more -> break
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

}
