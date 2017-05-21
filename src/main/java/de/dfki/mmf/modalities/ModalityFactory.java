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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Magdalena Kaiser on 23.08.2016.
 */

/**
 * Factory to create the different types of modalities
 */
public class ModalityFactory {

    /**
     * Create modality of certain type, each modality can only be create once
     * @param type
     * @return created modality
     */
    public Modality createModality(ModalityType type) {
        Modality modality;
        switch (type) {
            case SPEECH:
                modality = SpeechModality.getInstance();
                break;
            case POINTING:
                modality = PointingModality.getInstance();
                break;
            case GAZE:
                modality = GazeModality.getInstance();
                break;
            case NODDING_HEADSHAKING:
                modality = NoddingHeadShakingModality.getInstance();
                break;
            case WAVING:
                modality = WavingModality.getInstance();
                break;
            case IMAGE:
                modality = ImageModality.getInstance();
                break;

            default:
                throw new UnsupportedOperationException("Unknown modality type found.");
        }
        return modality;
    }

    /**
     *
     * @return list of modalites containing the Speech, Pointing and Gaze Modality (= default modalities)
     */
    public List<Modality> createDefaultModalities() {
        ArrayList<Modality> modalities = new ArrayList<>();
        modalities.add(SpeechModality.getInstance());
        modalities.add(PointingModality.getInstance());
        modalities.add(GazeModality.getInstance());
        return modalities;
    }
}
