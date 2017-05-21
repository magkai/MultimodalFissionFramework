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

import java.util.List;

/**
 * Created by Magdalena Kaiser on 05.06.2016.
 */

/**
 * A predicate has the following form "[modifier] predicateName(predicateElement1, ..., predicateElementN)"
 * Additional, it receives annotations needed for the Natural Language Generation
 */
public class Predicate {

    private String predicateName;
    private List<String>  predicateModifiers;
    private PredicateElement[] elements;
    private List<PredicateAnnotation> annotations;

    public Predicate(String predicateName) {
        this.predicateName = predicateName;
    }

    public Predicate(List<String> predicateModifiers, String predicateName, PredicateElement[] elements) {
        this.predicateModifiers = predicateModifiers;
        this.predicateName = predicateName;
        this.elements = elements;
    }

    public Predicate(String predicateName, PredicateElement[] elements) {
        this.predicateName = predicateName;
        this.elements = elements;
    }


    public List<String> getPredicateModifiers() {
        return predicateModifiers;
    }

    public String getPredicateName() {
        return predicateName;
    }

    public PredicateElement[] getElements() {
        return elements;
    }

    public void setElements(PredicateElement[] elements) { this.elements = elements; }

    public List<PredicateAnnotation> getPredicateAnnotations() {
        return annotations;
    }

    public void setPredicateAnnotations(List<PredicateAnnotation> annotations) {
        this.annotations = annotations;
    }


}
