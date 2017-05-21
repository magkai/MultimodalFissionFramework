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

package de.dfki.mmf.planner.modalityplanner;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import de.dfki.mmf.planner.deviceplanner.DeviceRepresentation;
import de.dfki.mmf.planner.modalityplanner.score.AbstractScorer;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.buildin.hardsoft.HardSoftScoreDefinition;
import org.optaplanner.persistence.xstream.impl.score.XStreamScoreConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@PlanningSolution
@XStreamAlias("ModalityRepresentation")
/**
 * Solution class for the Modality Planning
 */
public class ModalityRepresentation implements Solution<HardSoftScore> {
    //list of used scorers
    private List<AbstractScorer> scorer = new ArrayList<>();
    //list of the phrase components (the planning entities)
    private List<PhraseComponent> phraseComponents;
    //list of PowerSetModalities
    private List<PowerSetModality> powerSetModalityList;
    //list of different speech output
    private List<SpeechOutputType> speechOutputTypeList;
    //result from the device planning
    private DeviceRepresentation solvedDeviceRepresentation;

    public void addScorer(AbstractScorer scorer){
        this.scorer.add(scorer);
    }

    public List<AbstractScorer> getScorer() {
        return scorer;
    }

    public void setScorer(List<AbstractScorer> scorer) {
        this.scorer = scorer;
    }


    @XStreamConverter(value = XStreamScoreConverter.class, types = {HardSoftScoreDefinition.class})
    private HardSoftScore score;

    @PlanningEntityCollectionProperty
    public List<PhraseComponent> getPhraseComponents() {
        return phraseComponents;
    }

    public void setPhraseComponents(List<PhraseComponent> argList) {
        this.phraseComponents = argList;
    }

    @ValueRangeProvider(id = "modalityRange")
    public List<PowerSetModality> getPowerSetModalityList() {
        return powerSetModalityList;
    }

    public void setPowerSetModalityList(ArrayList<PowerSetModality> powerSetModalityList) {
        this.powerSetModalityList = powerSetModalityList;
    }

    @ValueRangeProvider(id = "speechOutputRange")
    public List<SpeechOutputType> getSpeechOutputTypeList() {
        return speechOutputTypeList;
    }

    public void setSpeechOutputTypeList(List<SpeechOutputType> speechOutputList) {
        this.speechOutputTypeList = speechOutputList;
    }

    public void setDefaultSpeechOutputTypeList() {
        speechOutputTypeList = new ArrayList<>();
        speechOutputTypeList.add(SpeechOutputType.ATTRIBUTIVE_IDENTIFIER);
        speechOutputTypeList.add(SpeechOutputType.THE_TYPE);
        speechOutputTypeList.add(SpeechOutputType.THIS_TYPE);
        speechOutputTypeList.add(SpeechOutputType.THIS);
        speechOutputTypeList.add(SpeechOutputType.IT);
    }


    public DeviceRepresentation getSolvedDeviceRepresentation() {
        return solvedDeviceRepresentation;
    }

    public void setSolvedDeviceRepresentation(DeviceRepresentation solvedDeviceRepresentation) {
        this.solvedDeviceRepresentation = solvedDeviceRepresentation;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public Collection<? extends Object> getProblemFacts() {
        List<Object> facts = new ArrayList<Object>();
        facts.addAll(powerSetModalityList);
        facts.addAll(speechOutputTypeList);
        // Do not add the planning entity's because that will be done automatically
        return facts;
    }

}
