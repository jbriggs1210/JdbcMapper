/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Header:$
 */
package org.apache.beehive.netui.compiler.model.validation;

import org.apache.beehive.netui.compiler.model.XmlElementSupport;
import org.apache.beehive.netui.compiler.model.XmlModelWriter;
import org.w3c.dom.Element;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Iterator;

class ValidatableEntity extends XmlElementSupport
{
    private String _entityName;
    private Map _fields = new LinkedHashMap();
    
    
    public ValidatableEntity( String entityName )
    {
        _entityName = entityName;
    }

    protected String getEntityName()
    {
        return _entityName;
    }
    
    public void addField( ValidatableField field )
    {
        _fields.put( field.getPropertyName(), field );
    }
    
    public ValidatableField getField( String fieldName )
    {
        return ( ValidatableField ) _fields.get( fieldName );
    }
    
    public void writeToElement(XmlModelWriter xw, Element element)
    {
        assert _entityName.equals(getElementAttribute(element, "name"))
                : _entityName + ", " + getElementAttribute(element, "name");
        
        for ( Iterator i = _fields.values().iterator(); i.hasNext(); )
        {
            ValidatableField field = ( ValidatableField ) i.next();
            String fieldPropertyName = field.getPropertyName();
            Element fieldElementToUse = findChildElement(xw, element, "field", "property", fieldPropertyName, true, null);
            field.writeXML(xw, fieldElementToUse);
        }
    }
}
