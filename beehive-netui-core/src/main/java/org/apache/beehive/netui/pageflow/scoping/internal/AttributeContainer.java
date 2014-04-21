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
package org.apache.beehive.netui.pageflow.scoping.internal;

import org.apache.beehive.netui.util.logging.Logger;

import java.util.HashMap;
import java.util.Enumeration;
import java.util.Collections;
import java.util.Map;
import java.util.Iterator;
import java.io.Serializable;


public class AttributeContainer
{
    private static final Logger logger = Logger.getInstance( AttributeContainer.class );
    
    private Map _attrs;

    public Object getAttribute( String attrName )
    {
        return ( _attrs != null ? _attrs.get( attrName ) : null );
    }

    public void setAttribute( String attrName, Object o )
    {
        if ( _attrs == null )
        {
            _attrs = new HashMap();
        }

        _attrs.put( attrName, o );
    }

    public Enumeration getAttributeNames()
    {
        if ( _attrs == null )
        {
            _attrs = new HashMap();
        }

        return Collections.enumeration( _attrs.keySet() );
    }

    public String[] getAttributeNamesArray()
    {
        if ( _attrs == null )
        {
            return new String[0];
        }

        return ( String[] ) _attrs.keySet().toArray( new String[0] );
    }

    public void removeAttribute( String attrName )
    {
        if ( _attrs != null )
        {
            _attrs.remove( attrName );
        }
    }

    public void removeAllAttributes()
    {
        _attrs = null;
    }

    protected final Map getSerializableAttrs()
    {
        Map ret = new HashMap();

        for ( Iterator i = _attrs.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry entry = ( Map.Entry ) i.next();

            if ( entry.getValue() instanceof Serializable )
            {
                ret.put( entry.getKey(), entry.getValue() );
            }
            else
            {
                if ( logger.isInfoEnabled() )
                {
                    logger.info( "Dropping non-serializable request attribute " + entry.getKey()
                                  + " (" + entry.getValue() + ")." );
                }
            }
        }

        return ret;
    }
    
    protected final Map getAttrMap()
    {
        return _attrs;
    }

    protected final void setAttrMap( Map attrs )
    {
        _attrs = attrs;
    }
}
