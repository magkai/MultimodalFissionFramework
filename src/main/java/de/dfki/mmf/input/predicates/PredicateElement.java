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
 * Defines one of the elements of a predicate
 * Each element may contain an annotation about its function in the sentence for the Natural Language Generation
 * If the element refers to a world object, the corresponding id and type can also be set
 */
public interface PredicateElement {

    PredicateElementAnnotation getPredicateElementAnnotation();
    void setPredicateElementAnnotation(PredicateElementAnnotation annotation);

    String getWorldObjectId();
    void setWorldObjectId(String worldObjectId);

    String getWorldObjectType();
    void setWorldObjectType(String worldObjectType);


}
