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

package de.dfki.mmf.input.templates;

import de.dfki.mmf.input.predicates.Predicate;
import de.dfki.mmf.input.predicates.PredicateElement;
import de.dfki.mmf.input.predicates.StringPredicateElement;

import java.util.ArrayList;

/**
 * Created by Magdalena Kaiser on 10.11.2016.
 */
/**
 * Parser for templates defined in PredicateTemplates.template
 */
public abstract class TemplateParser {

    /**
     * Creates a predicate out of the string input
     */
    public Predicate createPredicateFromString(String predicateString) {
        //extract predicate name
        String predicateName = predicateString.substring(0,predicateString.indexOf("("));
        //retrieve predicate elements
        String argumentPart = predicateString.substring(predicateString.indexOf("(")+1,predicateString.indexOf(")"));
        String[] arguments = argumentPart.split(",");
        PredicateElement[] predicateArguments = null;

        //create StringPredicateElements
        if(!arguments[0].equals("")) {
            predicateArguments = new StringPredicateElement[arguments.length];
            for(int i = 0; i < arguments.length; i++) {
                predicateArguments[i] = new StringPredicateElement(arguments[i]);
            }
        }
       //retrieve predicate modifiers
        ArrayList<String> predicateModifiers = new ArrayList<>();
        if(predicateName.startsWith("[na]") || predicateName.startsWith("[xu]") || predicateName.startsWith("[ko]")) {
            String[] splitFront = predicateName.split(" ");
            predicateName = splitFront[splitFront.length-1];
            for(int i = 0; i < splitFront.length-1; i++) {
                predicateModifiers.add(splitFront[i]);
            }
           // System.out.println("predicateName: " + predicateName + " predicateMod: " + predicateModifiers.toString());
        }
        return new Predicate(predicateModifiers, predicateName, predicateArguments);
    }


    public abstract Predicate parseTemplate(Predicate predicate);
}
