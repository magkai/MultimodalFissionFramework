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
import de.dfki.mmf.attributeselection.AttributiveObjectIdentifier;
import de.dfki.mmf.input.predicates.PredicateElement;
import de.dfki.mmf.modalities.ModalityType;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by Magdalena Kaiser on 01.08.2016.
 */
@PlanningEntity
@XStreamAlias("PhraseComponent")
/**
 * Planning entity containing the predicate element for which a suitable set of modalities should be chosen
 */
public class PhraseComponent {

    //predicate element for which suitable modalities should be found
    private PredicateElement predicateElement;
    //a PowersetModality containing any combination of available modalities
    private PowerSetModality powerSetModality;
    //map stating how well each modality can represent the predicat element with number between 0.0 - 1.0
    private Map<ModalityType, Double> modalityRepresentationMap = new EnumMap<ModalityType, Double>(ModalityType.class);
    //attributive identifier for elements referring to world objects otherwise null
    private AttributiveObjectIdentifier attributiveObjectIdentifier;
    //possible speech output type for elements referring to world objects
    private SpeechOutputType speechOutputType;

    public PhraseComponent() {

    }

    public PhraseComponent(PredicateElement element) {
        this.predicateElement = element;
    }

    public PredicateElement getPredicateElement() {
        return this.predicateElement;
    }

    public String toString() {
        return this.predicateElement.toString();
    }

    @PlanningVariable(valueRangeProviderRefs = {"modalityRange"})
    public PowerSetModality getPowerSetModality() {
        return powerSetModality;
    }

    public void setPowerSetModality(PowerSetModality powerSetModality) {
        this.powerSetModality = powerSetModality;
    }


    @PlanningVariable(valueRangeProviderRefs = {"speechOutputRange"})
    public  SpeechOutputType getSpeechOutputType() {
        return speechOutputType;
    }

    public void setSpeechOutputType(SpeechOutputType speechOutputType) {
        this.speechOutputType = speechOutputType;
    }


    public Map<ModalityType, Double> getModalityRepresentationMap() {
        return modalityRepresentationMap;
    }

    public void setModalityRepresentationMap(Map<ModalityType, Double> modalityRepresentationMap) {
        this.modalityRepresentationMap = modalityRepresentationMap;
    }

    public AttributiveObjectIdentifier getAttributiveObjectIdentifier() {
        return attributiveObjectIdentifier;
    }

    public void setAttributiveObjectIdentifier(AttributiveObjectIdentifier attributiveObjectIdentifier) {
        this.attributiveObjectIdentifier = attributiveObjectIdentifier;
    }


}
