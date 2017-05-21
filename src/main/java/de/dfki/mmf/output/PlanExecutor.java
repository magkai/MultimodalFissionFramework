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

package de.dfki.mmf.output;

import de.dfki.mmf.devices.Device;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Magdalena Kaiser on 29.06.2016.
 */

/**
 * Plan Executor - coordinates the device threads that execute the individual output
 */
public class PlanExecutor {

    private List<ComposedPlanComponent> plan = new ArrayList<ComposedPlanComponent>();

    public PlanExecutor(List<ComposedPlanComponent> plan) {
        this.plan = plan;
    }

    /**
     * executes the plan - each composed plan component after each other and the plan components of each composed plan component in parallel
     * @throws InterruptedException
     */
    public void executePlan() throws InterruptedException{
        //execute plan components in one composed plan component on the corresponding devices in parallel
        for(ComposedPlanComponent composedPlanComponent: plan) {
            List<PlanComponent> planComponents = composedPlanComponent.getPlanComponents();
            Thread deviceThreads[] = new Thread[planComponents.size()];
            //retrieve the corresponding device and output and create the threads for the execution
            //each device is a thread to execute the corresponding output
            for(int i = 0; i < planComponents.size(); i++) {
                Device device = planComponents.get(i).getDevice();
                Object output = planComponents.get(i).getOutput();
                device.setOutput(output);
                deviceThreads[i] = new Thread(device);
                deviceThreads[i].setDaemon(true);
            }
            //start the threads
            for(int j = 0; j < planComponents.size(); j++) {
                deviceThreads[j].start();
            }

           //join threads
           for(int j = 0; j < planComponents.size(); j++) {
                try {
                    deviceThreads[j].join();
                } catch (InterruptedException e) {
                    for(int i = 0; i < planComponents.size(); i++) {
                        if(i != j) {
                            deviceThreads[i].interrupt();
                        }
                    }
                    throw e;
                }
            }
            System.out.println("--------------------------------------------");
        }
    }

}
