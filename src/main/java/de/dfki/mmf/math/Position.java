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
 * Created by Magdalena Kaiser on 29.06.2016.
 */

/**
 * A position in the world
 */
public class Position {

    private double x;
    private double y;
    private double z;

    public Position(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public void setX(double xCoord) {
        this.x = xCoord;
    }

    public double getY() {
        return y;
    }

    public void setY(double yCoord) {
        this.y = yCoord;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double zCoord) {
        this.z = zCoord;
    }

    @Override
    public String toString() {
        return "Pos(" + x + "," + y + "," + z + ")";
    }

    /**
     *
     * @param otherPosition
     * @return the distance to otherPosition
     */
    public double calculateDistance(Position otherPosition) {
        return Math.sqrt(Math.pow(x-otherPosition.getX(), 2)+ Math.pow(y-otherPosition.getY(), 2) + Math.pow(z-otherPosition.getZ(), 2));
    }

    /**
     *
     * @param otherPosition
     * @return the direction vector formed from the current position to otherPosition
     */
    public DirectionVector calculateDirectionVector(Position otherPosition) {
        return new DirectionVector(otherPosition.getX()-x, otherPosition.getY()-y, otherPosition.getZ()-z);
    }

}
