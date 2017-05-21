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

package de.dfki.mmf.attributeselection;

import com.google.gson.JsonObject;
import de.dfki.mmf.input.predicates.PredicateElement;

/**
 * Created by Magdalena Kaiser on 26.07.2016.
 */

/**
 * Identifier containing the selected attributes to verbally reference a certain world object
 */
public class AttributiveObjectIdentifier {
    //the corresponding predicate element which contains the world object which should be referenced
    private PredicateElement predicateElement;
    private String worldId;
    private String type;
    //the selected attributes to verbally refer to the world object
    private JsonObject selectedAttributesMap;
    //set to true if the object cannot be uniquely identified using its properties
    private boolean partialIdentifier = false;
    //set to true if world object can be referenced at all
    private boolean success = false;

    public AttributiveObjectIdentifier(PredicateElement predicateElement, String worldId, String type) {
        this.predicateElement = predicateElement;
        this.worldId = worldId;
        this.type = type;
    }

    public boolean hasSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isPartialIdentifiable() {
        return partialIdentifier;
    }

    public void setPartialIdentifier(boolean partialIdentifier) {
        this.partialIdentifier = partialIdentifier;
    }

    public PredicateElement getPredicateElement() {
        return predicateElement;
    }

    public String getWorldId() {
        return worldId;
    }

    public String getType() {
        return type;
    }

    public JsonObject getSelectedAttributes() {
        return this.selectedAttributesMap;
    }

    public void setSelectedAttributes(JsonObject map) {
        this.selectedAttributesMap = map;
    }

}
