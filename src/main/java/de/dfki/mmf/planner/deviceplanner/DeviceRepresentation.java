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

package de.dfki.mmf.planner.deviceplanner;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import de.dfki.mmf.devices.Device;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Magdalena Kaiser on 09.08.2016.
 */
@PlanningSolution
@XStreamAlias("ModalityRepresentation")
/**
 * Solution class for the device planning process
 */
public class DeviceRepresentation implements Solution<HardSoftScore> {

    //list containing the <predicate element, modality> pairs
    private List<PhraseModalityComponent> phraseModalityComponents = new ArrayList<>();
    //list of available devices
    private List<Device> devices =  new ArrayList<>();

    private HardSoftScore score;

    @PlanningEntityCollectionProperty
    public List<PhraseModalityComponent> getPhraseModalityComponents() {
        return phraseModalityComponents;
    }

    public void setPhraseModalityComponents(List<PhraseModalityComponent> phraseModalityComponents) {
        this.phraseModalityComponents = phraseModalityComponents;
    }


    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
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
        facts.addAll(devices);
        // Do not add the planning entity's because that will be done automatically
        return facts;
    }


}
