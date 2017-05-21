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

package de.dfki.mmf.modalities;

import de.dfki.mmf.input.predicates.Predicate;
import de.dfki.mmf.input.predicates.PredicateElement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.dfki.mmf.modalities.ModalityType.NODDING_HEADSHAKING;

/**
 * Created by Magdalena Kaiser on 05.12.2016.
 */
public class NoddingHeadShakingModality extends Modality implements PredicateReferencingModality {

    private static volatile NoddingHeadShakingModality noddingShakingInstance = new NoddingHeadShakingModality();
    //list contains predicate names which can be highlighted with a nodding gesture
    private List<String> referencedPredicateNoddingList = null;
    //list contains predicate names which can be highlighted with a head shaking gesture
    private List<String> referencedPredicateHeadShakingList = null;

    public static NoddingHeadShakingModality getInstance() {
        return noddingShakingInstance;
    }

    private NoddingHeadShakingModality() {
        super(NODDING_HEADSHAKING);
    }

    public List<String> getReferencedPredicateNoddingList() {
        return referencedPredicateNoddingList;
    }

    public void setReferencedPredicateNoddingList(List<String> referencedPredicateNoddingList) {
        this.referencedPredicateNoddingList = referencedPredicateNoddingList;
    }

    public List<String> getReferencedPredicateHeadShakingList() {
        return referencedPredicateHeadShakingList;
    }

    public void setReferencedPredicateHeadShakingList(List<String> referencedPredicateHeadShakingList) {
        this.referencedPredicateHeadShakingList = referencedPredicateHeadShakingList;
    }

    public void fillModalityOutputMap(Predicate predicate) {
        if(referencedPredicateNoddingList.contains(predicate.getPredicateName())) {
            for(PredicateElement element: predicate.getElements()) {
                modalityOutputMap.put(element, "NODDING");
            }
        }else if(referencedPredicateHeadShakingList.contains(predicate.getPredicateName())) {
            for(PredicateElement element: predicate.getElements()) {
                modalityOutputMap.put(element, "HEADSHAKING");
            }
        }
    }


    @Override
    public Map<PredicateElement, Double> scorePresentability(Predicate predicate) {
        HashMap<PredicateElement, Double> scorePresentabilityMap = new HashMap<>();
        if(isReferencedPredicate(predicate.getPredicateName())) {
            for(PredicateElement element: predicate.getElements()) {
                scorePresentabilityMap.put(element, 1.0);
            }
        }else  {
            for(PredicateElement element: predicate.getElements()) {
                scorePresentabilityMap.put(element, 0.0);
            }
        }
        return scorePresentabilityMap;
    }


    @Override
    //in this case no specific element but the whole predicate is referenced
    public boolean isReferencedPredicateElement(PredicateElement element) {
        return false;
    }

    @Override
    public boolean isReferencedPredicate(String predicateName) {
        if(referencedPredicateNoddingList.contains(predicateName)) {
            return true;
        }else if(referencedPredicateHeadShakingList.contains(predicateName)) {
            return true;
        }
        return false;
    }
}
