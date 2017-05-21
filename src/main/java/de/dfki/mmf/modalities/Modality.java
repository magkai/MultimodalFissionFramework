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

import de.dfki.mmf.devices.Device;
import de.dfki.mmf.input.predicates.Predicate;
import de.dfki.mmf.input.predicates.PredicateElement;

import java.util.*;

/**
 * Created by Magdalena Kaiser on 05.06.2016.
 */

/**
 * Definition of a Modality and its functionality
 */
public abstract class Modality {
    //each modality has a list of its corresponding devices
    protected List<Device> deviceList = new ArrayList<Device>();
    //corresponding modality type
    protected ModalityType modalityType;
    //save for each element what to output if modality is chosen in planning process
    protected Map<PredicateElement, Object> modalityOutputMap = new HashMap<>();

    public Modality(ModalityType modType) {
        modalityType = modType;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(modalityType.toString());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        return getClass() == obj.getClass();

    }

    /**
     * fill modality output map depending on the specific modality
     * @param predicate input predicate
     */
    public abstract void fillModalityOutputMap(Predicate predicate);


    /**
     *
     * @param predicate input predicate
     * @return map with value between 0.0 and 1.0 to state how well modality can present the corresponding element
     */
    public abstract Map<PredicateElement, Double> scorePresentability(Predicate predicate);


    public void addDevice(Device device) {
        deviceList.add(device);
    }


    public void removeDevice(Device device) {
        for(int i = 0; i < deviceList.size(); i++) {
            if(Objects.equals(deviceList.get(i).getDeviceId(),device.getDeviceId())) {
                deviceList.remove(i);
            }
        }
    }

    public List<Device> getDevices() {
        return deviceList;
    }

    public void setDevices(List<Device> devices) {
        deviceList.addAll(devices);
    }

    public ModalityType getModalityType() {
        return this.modalityType;
    }

    public String toString() {
        return this.modalityType.toString();
    }

    public Map<PredicateElement, Object> getModalityOutputMap() {
        return modalityOutputMap;
    }

    public void setModalityOutputMap(Map<PredicateElement, Object> modalityOutputMap) {
        this.modalityOutputMap = modalityOutputMap;
    }

    public void clearModalityOutputMap() {
        this.modalityOutputMap = new HashMap<>();
    }

}
