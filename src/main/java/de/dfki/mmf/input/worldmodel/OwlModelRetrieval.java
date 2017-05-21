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

package de.dfki.mmf.input.worldmodel;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import static org.semanticweb.owlapi.apibinding.OWLManager.createConcurrentOWLOntologyManager;

/**
 * Created by Magdalena Kaiser on 24.08.2016.
 */

/**
 * Retrieve properties of the objects defined in the OWL model
 */
public class OwlModelRetrieval {
    private String fileName;
    private OWLOntology ontology;
    private OWLReasoner reasoner;


    public OwlModelRetrieval(String fileName) {
        this.fileName = fileName;
    }


    public void init() throws Exception{
        OWLOntologyManager manager = createConcurrentOWLOntologyManager();
        this.ontology = load(manager);
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
        this.reasoner = reasonerFactory.createReasoner(ontology);
        this.reasoner.precomputeInferences();
    }

    /**
     * @param manager
     * @return loaded ontology
     * @throws OWLOntologyCreationException
     */
    @Nonnull
    private OWLOntology load(@Nonnull OWLOntologyManager manager) throws OWLOntologyCreationException {
         return manager.loadOntologyFromOntologyDocument(new File(fileName));
    }

    /**
     * Retrieve properties of the individuals in the owl model and save them in JsonObject
     */
    public ArrayList<JsonObject> processRetrieval()  {
        //resulting list of Json objects
        ArrayList<JsonObject> owlObjects = new ArrayList<>();
        Collection<OWLClassExpression> entityClasses;
        Set<OWLNamedIndividual> individuals = ontology.getIndividualsInSignature();
        //go over all instances of the class
        for (OWLNamedIndividual ind : individuals) {
            JsonObject owlObject = new JsonObject() ;
            // insert object properties and its corresponding values for the current individual found in the owl model
            for (OWLObjectProperty p : ontology.getObjectPropertiesInSignature()) {
                NodeSet<OWLNamedIndividual> individualValues = reasoner.getObjectPropertyValues(ind, p);
                Set<OWLNamedIndividual> values = individualValues.getFlattened();
                //get the name of the property
                String propertyName = p.getIRI().getShortForm();
                if(propertyName.toLowerCase().startsWith("has")) {
                    propertyName = propertyName.substring(3);
                }
                if (!values.isEmpty()) {
                    //add found values to array
                    Iterator<OWLNamedIndividual> iter = values.iterator();
                    JsonArray array = new JsonArray();
                    while(iter.hasNext()) {
                        array.add(iter.next().getIRI().getShortForm());
                    }
                    //add property-value pair to the Json object
                    owlObject.add(propertyName, array);
                }
            }
            //retrieve all the data properties and corresponding value from the model as well
            for (OWLDataProperty d : ontology.getDataPropertiesInSignature()) {
                Set<OWLLiteral> values = reasoner.getDataPropertyValues(ind, d);
                String propertyName = d.getIRI().getShortForm();
                if(propertyName.startsWith("has")) {
                    propertyName = propertyName.substring(3);
                }
                if (!values.isEmpty()) {
                    String[] resultSplit = new String[values.size()];
                    int i = 0;
                    //check the datatype of the corresponding literal
                    for(OWLLiteral literal: values) {
                        OWLDatatype datatype = literal.getDatatype();
                        if(datatype.isDouble()) {
                            resultSplit[i] = ""+literal.parseDouble();
                        }else if(datatype.isString()) {
                            resultSplit[i] = literal.getLiteral();
                        }else if(datatype.isBoolean()) {
                            resultSplit[i] = "" + literal.parseBoolean();
                        }else if(datatype.isInteger()) {
                            resultSplit[i] = "" + literal.parseInteger();
                        }else if(datatype.isFloat()) {
                            resultSplit[i] = "" + literal.parseFloat();
                        }
                        i++;
                    }
                    //add values of data property to array
                    JsonArray array = new JsonArray();
                    for(int j = 0; j < resultSplit.length; j++) {
                        array.add(resultSplit[j]);
                    }
                    //add property-value pair to Json object
                    owlObject.add(propertyName, array);
                }
            }
            if(owlObject.size() >=1) {
                //add "worldobjectid"
                String instanceName = ind.getIRI().getShortForm();
                owlObject.addProperty("worldobjectid", instanceName);
                //retrieve the type of the indivdual and add it as "worldobjecttype"
                entityClasses = EntitySearcher.getTypes(ind, ontology);
                JsonArray typeArray = new JsonArray();
                for(OWLClassExpression entityClass: entityClasses) {
                    typeArray.add(entityClass.asOWLClass().getIRI().getShortForm());
                }
                owlObject.add("worldobjecttype", typeArray);
                //add Json object to list
                owlObjects.add(owlObject);
            }
        }
        return owlObjects;
    }

}

