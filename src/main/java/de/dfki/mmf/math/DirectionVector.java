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

package de.dfki.mmf.math;

/**
 * Created by Magdalena Kaiser on 14.08.2016.
 */

/**
 * The direction vector between two points
 */
public class DirectionVector {

    private double xDirection;
    private double yDirection;
    private double zDirection;

    public DirectionVector(double xDirection, double yDirection, double zDirection) {
        this.xDirection = xDirection;
        this.yDirection = yDirection;
        this.zDirection = zDirection;
    }


    public double getxDirection() {
        return xDirection;
    }

    public void setxDirection(double xDirection) {
        this.xDirection = xDirection;
    }

    public double getyDirection() {
        return yDirection;
    }

    public void setyDirection(double yDirection) {
        this.yDirection = yDirection;
    }

    public double getzDirection() {
        return zDirection;
    }

    public void setzDirection(double zDirection) {
        this.zDirection = zDirection;
    }


    /**
     *
     * @return the length of the current direction vector
     */
    public double getVectorLength() {
        return Math.sqrt(xDirection*xDirection + yDirection*yDirection + zDirection*zDirection);
    }

    /**
     *
     * @return the unit direction vector of the current direction vector
     */
    public DirectionVector getUnitVector() {
        double size = getVectorLength();
        return new DirectionVector(this.getxDirection()/size, this.getyDirection()/size, this.getzDirection()/size);
    }

    /**
     *
     * @param otherVector
     * @return the scalar product between the current direction vector and otherVector
     */
    public double calculateScalarProduct(DirectionVector otherVector) {
        return  xDirection*otherVector.getxDirection() + yDirection*otherVector.getyDirection() + zDirection*otherVector.getzDirection();
    }

    /**
     *
     * @param otherVector
     * @return the angle in rad between the current direction vector and otherVector
     */
    public double calculateAngle(DirectionVector otherVector) {
        double scalarValue = calculateScalarProduct(otherVector);
        return Math.acos(scalarValue/(getVectorLength()*otherVector.getVectorLength()));
    }

}
