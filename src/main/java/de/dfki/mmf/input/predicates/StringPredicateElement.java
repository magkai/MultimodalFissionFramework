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

package de.dfki.mmf.input.predicates;

/**
 * Created by Magdalena Kaiser on 15.06.2016.
 */
/**
 * A predicate element consisting of a string
 */
public class StringPredicateElement implements PredicateElement {

    private String element;
    private PredicateElementAnnotation annotation;
    private String worldObjectId;
    private String worldObjectType;


    public StringPredicateElement(String element) {
        this.element = element;
    }

    public String getElement() {
        return element;
    }

    public void setElement(String element) {
        this.element = element;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (this.getClass() != obj.getClass())
            return false;

        StringPredicateElement objElement = (StringPredicateElement) obj;
        return element.equals(objElement.getElement());
    }

    @Override
    public int hashCode() {
        return element.hashCode();
    }

    @Override
    public String toString() {
        return element;
    }


    public PredicateElementAnnotation getPredicateElementAnnotation() {
        return annotation;
    }

    public void setPredicateElementAnnotation(PredicateElementAnnotation annotation) {
        this.annotation = annotation;
    }

    public String getWorldObjectId() {
        return worldObjectId;
    }

    public void setWorldObjectId(String worldObjectId) {
        this.worldObjectId = worldObjectId;
    }

    public String getWorldObjectType() {
        return worldObjectType;
    }

    public void setWorldObjectType(String worldObjectType) {
        this.worldObjectType = worldObjectType;
    }


}
