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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;


/**
 * A wrapper around ServletConfig, associated with a given scope-key.
 */
public class ScopedServletConfig
        extends AttributeContainer
        implements ServletConfig
{
    private ServletContext _context;
    private String _servletName;

    public ScopedServletConfig( ServletContext context, ServletConfig baseServletConfig )
    {
        _context = context;

        for ( Enumeration e = baseServletConfig.getInitParameterNames(); e.hasMoreElements(); )
        {
            String paramName = ( String ) e.nextElement();
            setAttribute( paramName, baseServletConfig.getInitParameter( paramName ) );
        }
        
        _servletName = baseServletConfig.getServletName();
    }

    public String getServletName()
    {
        return _servletName;
    }

    public ServletContext getServletContext()
    {
        return _context;
    }

    public String getInitParameter( String s )
    {
        return ( String ) getAttribute( s );
    }

    public Enumeration getInitParameterNames()
    {
        return getAttributeNames();
    }
}
