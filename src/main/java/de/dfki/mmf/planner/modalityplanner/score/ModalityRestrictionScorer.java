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
import de.dfki.mmf.planner.modalityplanner.ModalityRepresentation;
import de.dfki.mmf.planner.modalityplanner.PhraseComponent;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

/**
 * Created by Magdalena Kaiser on 12.08.2016.
 */

/**
 * Scorer containing some examples to restrict certain modality usage
 */
public class ModalityRestrictionScorer extends AbstractScorer {

    private int maxModalityScore = 0;
    private int specificModalityScore = 0;
    private int limitModalityUsageScore = 0;
    private int pointingInRowScore = 0;

    private boolean useMaxMod = false;
    private boolean useSpecificMod = false;
    private ModalityType specificUsageModalityType;
    private boolean limitUsage = false;
    private ModalityType limitUsageModalityType;
    private boolean noPointingInRow = false;


    public boolean isUseMaxMod() {
        return useMaxMod;
    }

    public void setUseMaxMod(boolean useMaxMod) {
        this.useMaxMod = useMaxMod;
    }

    public boolean isUseSpecificMod() {
        return useSpecificMod;
    }

    public void setUseSpecificMod(boolean useSpecificMod) {
        this.useSpecificMod = useSpecificMod;
    }

    public ModalityType getSpecificUsageModalityType() {
        return specificUsageModalityType;
    }

    public void setSpecificUsageModalityType(ModalityType specificUsageModalityType) {
        this.specificUsageModalityType = specificUsageModalityType;
    }

    public boolean isLimitUsage() {
        return limitUsage;
    }

    public void setLimitUsage(boolean limitUsage) {
        this.limitUsage = limitUsage;
    }

    public ModalityType getLimitUsageModalityType() {
        return limitUsageModalityType;
    }

    public void setLimitUsageModalityType(ModalityType limitUsageModalityType) {
        this.limitUsageModalityType = limitUsageModalityType;
    }

    public boolean isNoPointingInRow() {
        return noPointingInRow;
    }

    public void setNoPointingInRow(boolean noPointingInRow) {
        this.noPointingInRow = noPointingInRow;
    }

    private void clearScores() {
        maxModalityScore = 0;
        specificModalityScore = 0;
        limitModalityUsageScore = 0;
        pointingInRowScore = 0;
    }

    public HardSoftScore calculateScore(ModalityRepresentation modalityRepresentation) {
        int hardScore = 0;
        int softScore = 0;

        //For demonstration: some examples how to restrict modality usage

        //use maximal possible multimodal output
        if(useMaxMod) {
            useMaxModality(modalityRepresentation);
        }

        //use certain modality whenever possible
        if(useSpecificMod && specificUsageModalityType != null) {
            useSpecificModality(specificUsageModalityType, modalityRepresentation);
        }

        //do not allow several pointing gestures after each other
        if(noPointingInRow) {
            noPointingTwoTimesInRow(modalityRepresentation);
        }

        //limit the usage of certain modality
        if(limitUsage && limitUsageModalityType != null) {
            limitUsageOfSpecificModality(limitUsageModalityType, modalityRepresentation);
        }

        //calculate overall softScore by summing up the individual scores
        softScore += maxModalityScore;
        softScore += specificModalityScore;
        softScore += limitModalityUsageScore;
        softScore += pointingInRowScore;

        //reset individual scores for next round
        clearScores();

        return HardSoftScore.valueOf(hardScore, softScore);
    }

    /**
     * use all modalities which are possible to receive a maximal multimodal output
     * @param modalityRepresentation
     */
    public void useMaxModality(ModalityRepresentation modalityRepresentation) {
        //get maximal possible multimodal output
        for(PhraseComponent argument: modalityRepresentation.getPhraseComponents()) {
            for(ModalityType possibleModalityType: argument.getModalityRepresentationMap().keySet()) {
                if(argument.getPowerSetModality() != null && !argument.getPowerSetModality().getModalitySet().isEmpty()) {
                    if (argument.getModalityRepresentationMap().get(possibleModalityType) > 0.5) {
                        if(!argument.getPowerSetModality().getModalitySet().contains(possibleModalityType)) {
                            maxModalityScore -= 1;
                        }
                    }
                }
            }
        }
    }

    /**
     * use specific modality whenever possible
     * @param modalityType
     * @param modalityRepresentation
     */
    public void useSpecificModality(ModalityType modalityType, ModalityRepresentation modalityRepresentation) {
        for(PhraseComponent argument: modalityRepresentation.getPhraseComponents()) {
            if (argument.getPowerSetModality() != null && !argument.getPowerSetModality().getModalitySet().isEmpty()) {
                if(argument.getModalityRepresentationMap().containsKey(modalityType) && argument.getModalityRepresentationMap().get(modalityType) > 0.5) {
                    if(!argument.getPowerSetModality().getModalitySet().contains(modalityType)) {
                        specificModalityScore -= 1;
                    }
                }
            }
        }
    }

    /**
     * use specific modality not more than for a third of the arguments
     * @param modalityType
     * @param modalityRepresentation
     */
    public void limitUsageOfSpecificModality(ModalityType modalityType, ModalityRepresentation modalityRepresentation) {
        int allowedUsageNbr = (int) Math.ceil(modalityRepresentation.getPhraseComponents().size()/3.0);
        int usageCount = 0;
        for(PhraseComponent argument: modalityRepresentation.getPhraseComponents()) {
            if (argument.getPowerSetModality() != null && !argument.getPowerSetModality().getModalitySet().isEmpty()) {
                if(argument.getPowerSetModality().getModalitySet().contains(modalityType)) {
                    usageCount++;
                }
            }
        }
        if(usageCount > allowedUsageNbr) {
            int diff = usageCount-allowedUsageNbr;
            limitModalityUsageScore -= diff;
        }
    }

    /**
     * avoid several pointing gestures in a row
     * @param modalityRepresentation
     */
    public void noPointingTwoTimesInRow(ModalityRepresentation modalityRepresentation) {
        //no pointing two times in a row
        boolean pointed = false;
        int counter = 0;
        int savedCounter = 0;
        for(PhraseComponent argument: modalityRepresentation.getPhraseComponents()) {
            if (argument.getPowerSetModality() != null && !argument.getPowerSetModality().getModalitySet().isEmpty()) {
                //if actually pointing is used two times after each other -> sofscore - 1
                if (argument.getPowerSetModality().getModalitySet().contains(ModalityType.valueOf("POINTING"))) {
                    if(pointed && counter == savedCounter+1) {
                        pointingInRowScore -= 1;
                        savedCounter = counter;
                    //memorize pointing has appeared
                    }else {
                        savedCounter = counter;
                        pointed = true;
                    }
                }

            }
            counter++;
        }
    }
}
