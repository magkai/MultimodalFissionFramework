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

import de.dfki.mmf.controller.Controller;
import de.dfki.mmf.input.worldmodel.UserModel;
import de.dfki.mmf.input.worldmodel.WorldModel;
import de.dfki.mmf.modalities.ModalityType;
import de.dfki.mmf.planner.modalityplanner.ModalityRepresentation;
import de.dfki.mmf.planner.modalityplanner.PhraseComponent;
import de.dfki.mmf.planner.modalityplanner.SpeechOutputType;
import org.apache.commons.lang3.StringUtils;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Magdalena Kaiser on 11.11.2016.
 */

/**
 * Scorer taking into account information about the user and his/her preferences (e.g. using/avoiding a specific modality)
 */
public class UserInfoScorer extends AbstractScorer {

    private int maxReducedScore = 0;
    private int specificModUsedScore = 0;
    private int avoidSpecificModScore = 0;
    private int avoidSpeechOnlyOutputScore = 0;
    private int verboseSpeechOutputScore = 0;

    private void clearScores() {
        maxReducedScore = 0;
        specificModUsedScore = 0;
        avoidSpecificModScore = 0;
        avoidSpeechOnlyOutputScore = 0;
        verboseSpeechOutputScore = 0;
    }

    @Override
    public Score calculateScore(ModalityRepresentation modalityRepresentation) {
        int softScore = 0;
        //some examples about what to retrieve of the user model
        WorldModel.getUserModels().toString();
        for(UserModel userModel: WorldModel.getUserModels()) {
            for(String talkedUser: Controller.getTalkingToUserList()) {
                if(StringUtils.equalsIgnoreCase(talkedUser, userModel.getUserId())) {
                    //retrieve from user model whether user has a preferred modality
                    if (userModel.getUserProperties().has("preferredmodality")) {
                        String preferredModalityType = userModel.getUserProperties().get("preferredmodality").getAsString().toUpperCase();
                        useSpecificModality(ModalityType.valueOf(preferredModalityType), modalityRepresentation);
                    }
                    //does the user have any impairments -> certain modalities should be avoided
                    if (userModel.getUserProperties().has("impairment")) {
                        String impairment = userModel.getUserProperties().get("impairment").getAsString();
                        if (impairment.equals("seeing")) {
                            avoidCertainModalityUsage(new ArrayList<ModalityType>(Arrays.asList(ModalityType.valueOf("POINTING"), ModalityType.valueOf("GAZE"))), modalityRepresentation);
                        } else if (impairment.equals("hearing")) {
                            avoidCertainModalityUsage(new ArrayList<ModalityType>(Arrays.asList(ModalityType.valueOf("SPEECH"))), modalityRepresentation);
                        }
                    }
                    //does the user have not very good language skills in the language used for communication with him -> avoid using speech only
                    if (userModel.getUserProperties().has("languageskills")) {
                        String languageskills = userModel.getUserProperties().get("languageskills").getAsString();
                        if (languageskills.equals("low")) {
                            avoidSpeechOnlyOutput(modalityRepresentation);
                        }
                    }else if(userModel.getUserProperties().has("verbose")) {
                        if(userModel.getUserProperties().get("verbose").getAsBoolean()) {
                            verboseSpeechOutput(modalityRepresentation);
                        }
                    }
                    break;
                }
            }
        }
        //calculate softScore by summing up individuals scores
        softScore += specificModUsedScore;
        softScore += avoidSpecificModScore;
        //speech is very difficult to understand for user, other modalities very important -> score * 2
        softScore += avoidSpeechOnlyOutputScore*2;
        //since for the verbose speech output the attributive object identifier is very important -> score * 2
        softScore += verboseSpeechOutputScore*2;

        setMaximalReducedScore(maxReducedScore);
        //reset individual scores for next round
        clearScores();

        return HardSoftScore.valueOf(0, softScore);
    }

    /**
     * use specific modality whenever possible
     * @param modalityType
     * @param modalityRepresentation
     * @return
     */
    public void useSpecificModality(ModalityType modalityType, ModalityRepresentation modalityRepresentation) {
        for (PhraseComponent argument : modalityRepresentation.getPhraseComponents()) {
            if (argument.getPowerSetModality() != null && !argument.getPowerSetModality().getModalitySet().isEmpty()) {
                if(argument.getModalityRepresentationMap().containsKey(modalityType) && argument.getModalityRepresentationMap().get(modalityType) < 0.5) {
                    continue;
                }
                if (!argument.getPowerSetModality().getModalitySet().contains(modalityType)) {
                    specificModUsedScore -= 1;
                }
                maxReducedScore -= 1;
            }

        }
    }

    /**
     * avoid using a specific modality if possible
     * @param modalityTypeList
     * @param modalityRepresentation
     */
    public void avoidCertainModalityUsage(ArrayList<ModalityType> modalityTypeList, ModalityRepresentation modalityRepresentation) {
        for (PhraseComponent argument : modalityRepresentation.getPhraseComponents()) {
            if (argument.getPowerSetModality() != null && !argument.getPowerSetModality().getModalitySet().isEmpty()) {
                //go over set of currently chosen modalities and check if the modality which should be avoided is part of the set
                for (ModalityType powerSetModalityType : argument.getPowerSetModality().getModalitySet())  {
                    if (modalityTypeList.contains(powerSetModalityType)) {
                        //check if current argument can be expressed with other modalities, if so: softscore-1
                        for (ModalityType mapModalityType : argument.getModalityRepresentationMap().keySet()) {
                            if (!modalityTypeList.contains(mapModalityType) && argument.getModalityRepresentationMap().get(mapModalityType) > 0.5) {
                                avoidSpecificModScore -= 1;
                            }
                        }
                    }
                }
                //worst case: all modalities which should be avoided are actually used
                maxReducedScore -= modalityTypeList.size();
            }
        }
    }

    /**
     * use speech only in combination with other modality if possible
     * @param modalityRepresentation
     */
    public void avoidSpeechOnlyOutput(ModalityRepresentation modalityRepresentation) {
        for(PhraseComponent argument: modalityRepresentation.getPhraseComponents()) {
            if (argument.getPowerSetModality() != null && !argument.getPowerSetModality().getModalitySet().isEmpty()) {
                //check if only speech is used
                if(argument.getPowerSetModality().getModalitySet().size() == 1 && argument.getPowerSetModality().getModalitySet().contains(ModalityType.SPEECH)) {
                    //check how many other modalities can represent the argument
                    for(ModalityType modalityType: argument.getModalityRepresentationMap().keySet()) {
                        if(!modalityType.equals(ModalityType.SPEECH) && argument.getModalityRepresentationMap().get(modalityType) > 0.5)  {
                            avoidSpeechOnlyOutputScore -= 1;
                        }
                    }
                }
                if(argument.getAttributiveObjectIdentifier() != null) {
                    //better not use the attributive identifier (longer, possibly less understandable when speech should be avoided)
                    if(!argument.getSpeechOutputType().equals(SpeechOutputType.THIS_TYPE)) {
                        avoidSpeechOnlyOutputScore -= 1;
                    }
                    maxReducedScore -= 1;
                }
                //calculate maxreducedscore based on worst case: only speech is used
                for(ModalityType modalityType: argument.getModalityRepresentationMap().keySet()) {
                    if(!modalityType.equals(ModalityType.SPEECH) && argument.getModalityRepresentationMap().get(modalityType) > 0.5)  {
                        maxReducedScore -= 1;
                    }
                }
            }
        }
    }

    /**
     * verbose speech output should be provided
     * @param modalityRepresentation
     */
    public void verboseSpeechOutput(ModalityRepresentation modalityRepresentation) {
        //use speech heavily and use the attributive identifier to describe objects
        useSpecificModality(ModalityType.SPEECH, modalityRepresentation);
        for(PhraseComponent argument: modalityRepresentation.getPhraseComponents()) {
            if (argument.getPowerSetModality() != null && !argument.getPowerSetModality().getModalitySet().isEmpty()) {
                if(argument.getAttributiveObjectIdentifier() != null) {
                    if(!argument.getSpeechOutputType().equals(SpeechOutputType.ATTRIBUTIVE_IDENTIFIER)) {
                        verboseSpeechOutputScore -= 1;
                    }
                    maxReducedScore -= 1;
                }
            }
        }
    }

}



