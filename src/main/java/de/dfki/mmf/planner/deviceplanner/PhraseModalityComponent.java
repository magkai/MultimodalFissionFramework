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
import de.dfki.mmf.input.predicates.PredicateElement;
import de.dfki.mmf.modalities.Modality;
import de.dfki.mmf.devices.Device;
import de.dfki.mmf.math.Position;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.util.List;

/**
 * Created by Magdalena Kaiser on 09.08.2016.
 */
@PlanningEntity
@XStreamAlias("PhraseModalityComponent")
/**
 * Planning Entity for the device planning containing a <predicate element, modality> pair for which a suitable device should be found
 */
public class PhraseModalityComponent {

    //predicate element for which suitable device should be found
    private PredicateElement predicateElement;
    //modality currently assigned to the element
    private Modality modality;
    //a possible device
    private Device device;
    //if the predicate element refers to world object -> position of this object
    private Position argumentObjectPosition;

    public PhraseModalityComponent(PredicateElement element, Modality modality) {
        this.predicateElement = element;
        this.modality = modality;
    }

    public PhraseModalityComponent() {

    }

    public Position getArgumentObjectPosition() {
        return argumentObjectPosition;
    }

    public void setArgumentObjectPosition(Position argumentObjectPosition) {
        this.argumentObjectPosition = argumentObjectPosition;
    }


    public PredicateElement getPredicateElement() {
        return this.predicateElement;
    }

    public Modality getModality() {
        return this.modality;
    }

    @PlanningVariable(valueRangeProviderRefs = {"deviceRange"})
    public Device getDevice() {
        return this.device;
    }

    @ValueRangeProvider(id = "deviceRange")
    /**
     *  only those devices can be used which correspond to a certain modality
     */
    public List<Device> getPossibleDeviceList() {
        return getModality().getDevices();
    }

    public void setDevice(Device device) {
        this.device = device;
    }

    public String toString() {
        return "(" + this.predicateElement.toString() + ", " + this.modality.toString() + ")";
    }


}
