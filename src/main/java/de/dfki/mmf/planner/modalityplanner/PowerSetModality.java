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
import de.dfki.mmf.modalities.ModalityType;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Magdalena Kaiser on 31.07.2016.
 */
@XStreamAlias("PowerSetModality")
/**
 * Class containing a combination of different modalities
 */
public class PowerSetModality {

    private Set<ModalityType> modalitySet = new HashSet<ModalityType>();

    public PowerSetModality() {

    }

    public PowerSetModality(Set<ModalityType> modalitySet) {
        this.modalitySet = modalitySet;
    }

    public Set<ModalityType> getModalitySet() {
        return modalitySet;
    }

    public void setModalitySet(Set<ModalityType> modalitySet) {
        this.modalitySet = modalitySet;
    }

    public void addModality(ModalityType modality) {
        this.modalitySet.add(modality);
    }

    public void removeModality(ModalityType modality) {
        modalitySet.remove(modality);
    }

}
