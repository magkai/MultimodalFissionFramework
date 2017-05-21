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

package de.dfki.mmf.history;

import de.dfki.mmf.output.ComposedPlanComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Magdalena Kaiser on 13.08.2016.
 */
/**
 * Output History to save previous references to objects in the world
 */
public class OutputHistory {

    //map to save combination of worldobjectid and composed plan component used in the last output
    private static Map<String, ComposedPlanComponent> lastOutputHistory = new HashMap<>();
    //list containing the whole previous output
    private static List<Map<String, ComposedPlanComponent>> outputHistoryList = new ArrayList<>();
    //map to save which id belongs to which type (helpful for OutputHistoryScorer)
    private static Map<String, String> idTypeHistoryMap = new HashMap<>();

    public static List<Map<String, ComposedPlanComponent>> getOutputHistoryList() {
        return outputHistoryList;
    }

    public static Map<String, ComposedPlanComponent> getLastOutputHistory() {
        return lastOutputHistory;
    }

    public static void setLastOutputHistory(Map<String, ComposedPlanComponent> lastOutputHist) {
        lastOutputHistory = lastOutputHist;
        outputHistoryList.add(lastOutputHistory);
    }

    public static void clearLastOutputHistory() {
        outputHistoryList.remove(lastOutputHistory);
        lastOutputHistory = new HashMap<>();
    }

    public static Map<String, String> getIdTypeHistoryMap() {
        return idTypeHistoryMap;
    }

    public static void addToIdTypeHistory(String id, String type) {
        idTypeHistoryMap.put(id, type);
    }

    public static void clearOutputHistory() {
        lastOutputHistory = new HashMap<>();
        outputHistoryList = new ArrayList<>();
        idTypeHistoryMap = new HashMap<>();
    }



}
