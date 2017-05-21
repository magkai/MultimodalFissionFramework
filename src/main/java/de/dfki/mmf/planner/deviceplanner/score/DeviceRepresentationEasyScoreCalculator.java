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

package de.dfki.mmf.planner.deviceplanner.score;

import de.dfki.mmf.controller.Controller;
import de.dfki.mmf.devices.Device;
import de.dfki.mmf.math.Position;
import de.dfki.mmf.input.worldmodel.UserModel;
import de.dfki.mmf.input.worldmodel.WorldModel;
import de.dfki.mmf.modalities.ModalityType;
import de.dfki.mmf.modalities.SpeechModality;
import de.dfki.mmf.planner.deviceplanner.DeviceRepresentation;
import de.dfki.mmf.planner.deviceplanner.PhraseModalityComponent;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;

import java.util.List;

/**
 * Created by Magdalena Kaiser on 09.08.2016.
 */

/**
 * Scorer for choosing devices based on some simple rules
 */
public class DeviceRepresentationEasyScoreCalculator implements EasyScoreCalculator<DeviceRepresentation> {


    @Override
    public Score calculateScore(DeviceRepresentation deviceRepresentation) {
        int hardScore = 0;
        int softScore = 0;

        List<PhraseModalityComponent> phraseModalityComponents = deviceRepresentation.getPhraseModalityComponents();

        //if devices are available -> using no device is wrong
        for(PhraseModalityComponent component: phraseModalityComponents) {
            if(component.getDevice() == null && !component.getPossibleDeviceList().isEmpty()) {
                hardScore -= 1;
            }
        }

        //for some modalities and if the referred object is part of the world model -> use device which is close to the object
        for(PhraseModalityComponent component: phraseModalityComponents) {
            if(component.getDevice() != null && component.getDevice().getDevicePosition() != null) {
                //check if it is in world model (it has a position) and don't use this for speech devices
                if (component.getArgumentObjectPosition() != null && !(component.getModality() instanceof SpeechModality)) {
                    double usedDistance = component.getDevice().getDevicePosition().calculateDistance(component.getArgumentObjectPosition());
                    for (Device possibleDevice : component.getPossibleDeviceList()) {
                        if (!possibleDevice.equals(component.getDevice())) {
                            double possibleDistance = possibleDevice.getDevicePosition().calculateDistance(component.getArgumentObjectPosition());
                            if (possibleDistance < usedDistance) {
                                softScore -= 1;
                            }
                        }
                    }
                }
            }
        }

        //for the choice of speech devices: use the one closest to the user -> user model required
        for(PhraseModalityComponent component: phraseModalityComponents) {
            if (component.getDevice() != null && component.getDevice().getDevicePosition() != null) {
                if (component.getModality().getModalityType().equals(ModalityType.SPEECH)) {
                    //only scenarios with one user are considered here (however can be adapted to several)
                    Position userPosition = null;
                    for(UserModel userModel: WorldModel.getUserModels()) {
                        if(userModel.getUserId().equals(Controller.getTalkingToUserList().get(0))) {
                            userPosition = userModel.getUserPosition();
                            break;
                        }
                    }
                    if(userPosition != null) {
                        double usedDistance = component.getDevice().getDevicePosition().calculateDistance(userPosition);
                        for (Device possibleDevice : component.getPossibleDeviceList()) {
                            if (!possibleDevice.equals(component.getDevice())) {
                                double possibleDistance = possibleDevice.getDevicePosition().calculateDistance(userPosition);
                                if (possibleDistance < usedDistance) {
                                    softScore -= 1;
                                }
                            }
                        }
                    }
                }
            }
        }

        return HardSoftScore.valueOf(hardScore, softScore);
    }


}
