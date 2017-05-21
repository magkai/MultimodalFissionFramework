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
import de.dfki.mmf.planner.modalityplanner.SpeechOutputType;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;

/**
 * Created by Magdalena Kaiser on 09.11.2016.
 */

/**
 * Scorer which calculate the overall score (the softScore based on the results of the different used ScoreCalculators and the hardScore)
 * for the modality selection
 */
public class FusionScorer implements EasyScoreCalculator<ModalityRepresentation> {

    @Override
    public Score calculateScore(ModalityRepresentation modalityRepresentation) {
        //get the hardscore -> how many impossible argument - modality combinations exist
        int hardScore = getHardConstraint(modalityRepresentation);
        int softScore = 0;
        double weights[] = new double[modalityRepresentation.getScorer().size()];
        int softScores[] = new int[modalityRepresentation.getScorer().size()];
        //calculate softscore of each of the individual scorers and then use the weighted sum for the final softscore
       for(int i = 0; i < modalityRepresentation.getScorer().size(); i++) {
           AbstractScorer scoreCalculator = modalityRepresentation.getScorer().get(i);
           Score newScore = scoreCalculator.calculateScore(modalityRepresentation);
           weights[i] = scoreCalculator.getWeight();
           softScores[i] = ((HardSoftScore) newScore).getSoftScore();
           softScore += weights[i] * softScores[i];
       }

        return HardSoftScore.valueOf(hardScore, softScore);
    }

    /**
     * rates impossible argument - modality combinations
     * @param modalityRepresentation
     * @return resulting hardScore
     */
    public int getHardConstraint(ModalityRepresentation modalityRepresentation) {
        int hardScore = 0;
        //hard constraint which checks how well modality is suitable for predicate argument
        for (PhraseComponent argument : modalityRepresentation.getPhraseComponents()) {
            if (argument.getPowerSetModality() != null && !argument.getPowerSetModality().getModalitySet().isEmpty()) {
                for (ModalityType modalityType : argument.getPowerSetModality().getModalitySet()) {
                    if (argument.getModalityRepresentationMap().containsKey(modalityType)) {
                        if (argument.getModalityRepresentationMap().get(modalityType) < 0.5) {
                            hardScore -= 1;
                        }
                    }
                }
                //pointing is required if in the speech output deictic references with "this" are used
                if(argument.getAttributiveObjectIdentifier() != null) {
                    if (argument.getSpeechOutputType().equals(SpeechOutputType.THIS_TYPE) || argument.getSpeechOutputType().equals(SpeechOutputType.THIS)) {
                        if (!argument.getPowerSetModality().getModalitySet().contains(ModalityType.POINTING)) {
                            hardScore -= 1;
                        }
                    }
                }
            } else {
                //test if there is a modality which can represent the argument, if so modalityset of argument should not be empty
                if (argument.getPowerSetModality() != null && argument.getPowerSetModality().getModalitySet().isEmpty()) {
                    for (ModalityType modalityType : argument.getModalityRepresentationMap().keySet()) {
                        if (argument.getModalityRepresentationMap().get(modalityType) > 0.5) {
                            hardScore -= 1;
                        }
                    }
                }
                //null is always wrong, check how many possible modalities exist
                else if(argument.getPowerSetModality() == null) {
                    for (ModalityType modalityType : argument.getModalityRepresentationMap().keySet()) {
                        if (argument.getModalityRepresentationMap().get(modalityType) > 0.5) {
                            hardScore -= 1;
                        }
                    }
                }
            }
        }
        return hardScore;
    }
}
