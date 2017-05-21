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

import de.dfki.mmf.input.LanguageFormat;
import de.dfki.mmf.input.predicates.*;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Magdalena Kaiser on 02.11.2016.
 */
/**
 * A Template Parser for the LanguageFormat "ENG_SIMPLENLG" in order to create a sentence via SimpleNLG out of the predicate
 */
public class SimpleNLGTemplateParser extends TemplateParser {

    public Predicate parseTemplate(Predicate predicate) {
        List<String>  predicateModifier = predicate.getPredicateModifiers();
        String predicateName = predicate.getPredicateName();
        PredicateElement[] args = predicate.getElements();
        Predicate modifiedPredicate = new Predicate(predicateName);
        //get template resource
        InputStream resource = this.getClass().getClassLoader().getResourceAsStream("PredicateTemplates.template");
        BufferedReader  br = new BufferedReader(new InputStreamReader(resource));
        try {
            //read line by line
            String line = br.readLine();
            while (line != null) {
                //search template for respective predicate
                if(line.startsWith(predicateName)) {
                    //count required number of variables for corresponding predicate
                    int varCount = StringUtils.countMatches(line, "$");
                    if(args != null) {
                        if (args.length != varCount) {
                            throw new IllegalArgumentException("Incorrect number of arguments used for predicate " + predicateName + ".");
                        }
                    }
                    //go to line with template output in corresponding languageFormat
                    while(!line.startsWith("(" +LanguageFormat.ENG_SIMPLENLG + ")")) {
                        line = br.readLine();
                    }
                    //plug in the predicate arguments into the template output
                    if(args != null) {
                        for (int i = 0; i < args.length; i++) {
                            line = line.replace("$x" + (i + 1), args[i].toString());
                        }
                    }
                    //retrieve the different parts of the template
                    List<String> matchList = new ArrayList<String>();
                    //retrieve the arguments stored in "()"
                    Pattern regex = Pattern.compile("\\{(.*?)\\}");
                    Matcher regexMatcher = regex.matcher(line);
                    while (regexMatcher.find()) {
                        // if argument is zoe=null -> this part of template will not be used
                        if(!regexMatcher.group(1).contains("zoe")) {
                            matchList.add(regexMatcher.group(1));
                        }
                    }
                    ArrayList<PredicateElement> predicateElements = new ArrayList<>();
                    ArrayList<PredicateAnnotation> predicateAnnotations = new ArrayList<>();
                    for (String str : matchList) {
                        //retrieve annotations for each element
                        String[] splitstring = str.split(":");
                        String grammaticalFunction = "";
                        String questionAnnotation = "";
                        if(splitstring[0].contains("/")) {
                            grammaticalFunction = splitstring[0].split("/")[0];
                            questionAnnotation = splitstring[0].split("/")[1];
                        }else {
                            grammaticalFunction = splitstring[0];
                        }
                        if(grammaticalFunction.trim().equals("NO_NLG")) {
                            predicateAnnotations.add(PredicateAnnotation.valueOf(grammaticalFunction));
                            splitstring[1] = splitstring[1].replaceAll("\\s+", " ");
                            splitstring[1] = splitstring[1].trim();
                            PredicateElement predicateElement = new StringPredicateElement(splitstring[1]);
                            predicateElements.add(predicateElement);
                            continue;
                        }
                        //check if a question for a specific argument is asked
                        if(splitstring[1].contains("[ma]")) {
                            if(!questionAnnotation.equals("")) {
                                predicateAnnotations.add(PredicateAnnotation.valueOf(questionAnnotation));
                            }else {
                                System.out.println("Warning: A question about a specific part of the sentence should be asked, but no corresponding question annotation was set.");
                            }
                            continue;
                        }else if(splitstring[1].contains("[xo]")) {
                            if(!questionAnnotation.equals("")) {
                                predicateAnnotations.add(PredicateAnnotation.valueOf(questionAnnotation));
                            }
                            continue;
                        }
                        splitstring[1] = splitstring[1].replaceAll("\\s+", " ");
                        splitstring[1] = splitstring[1].trim();
                        PredicateElement predicateElement = new StringPredicateElement(splitstring[1]);
                        predicateElements.add(predicateElement);
                        predicateElement.setPredicateElementAnnotation(PredicateElementAnnotation.valueOf(grammaticalFunction));
                    }
                    //create the modified predicate
                    modifiedPredicate.setElements(predicateElements.toArray(new StringPredicateElement[predicateElements.size()]));
                    //some annotations which affect the entire predicate
                    if(predicateModifier != null) {
                        if (predicateModifier.contains("[na]")) {
                            predicateAnnotations.add(PredicateAnnotation.NEGATION);
                        }
                        if (predicateModifier.contains("[xu]")) {
                            predicateAnnotations.add(PredicateAnnotation.YES_NO);
                        }
                        if (predicateModifier.contains("[ko]")) {
                            predicateAnnotations.add(PredicateAnnotation.IMPERATIVE);
                        }
                    }
                    modifiedPredicate.setPredicateAnnotations(predicateAnnotations);
                    break;
                }
                line = br.readLine();
                if(line == null) {
                    throw new UnsupportedOperationException("Predicate " + predicateName + " is unknown. You need to specify a template for this predicate first.");
                }
            }

        } catch (IOException e) {
            System.out.println("An IO-Exception occured while reading a line from buffered reader.");
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                System.out.println("An IO-Exception occured while trying to close the buffered reader.");
                e.printStackTrace();
            }
        }
        return modifiedPredicate;
    }
}
