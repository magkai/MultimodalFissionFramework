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

import de.dfki.mmf.planner.modalityplanner.ModalityRepresentation;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;

/**
 * Created by Magdalena Kaiser on 10.11.2016.
 */

/**
 * An abstract EasyScoreCalculator containing  a weight describing the importance of this scorer
 *  and a score containing the maximal value the score can be reduced
 */
public abstract class AbstractScorer implements EasyScoreCalculator<ModalityRepresentation> {

    //weight states the influence of the scorer in the final score calculation (default: 1.0)
    private double weight = 1.0;
    //score containing the maximal value the score can be reduced
    //currently not used in the calculation for the final score,however can be useful if all scores are seen as equally powerful
    //by dividing each score by its maximalReducedScore normalizes the scores
    private int maximalReducedScore;

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getMaximalReducedScore() {
        return maximalReducedScore;
    }

    public void setMaximalReducedScore(int maximalReducedScore) {
        this.maximalReducedScore = maximalReducedScore;
    }


}
