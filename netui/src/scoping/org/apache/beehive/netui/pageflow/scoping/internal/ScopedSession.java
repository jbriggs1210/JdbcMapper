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

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.ServletContext;
import java.io.Serializable;


/**
 * A wrapper around HttpSession, associated with a given scope-key.  All calls to setAttribute,
 * getAttribute, removeAttribute, etc. are scoped to this object, while most other functionality
 * delegates to the wrapped HttpSession.
 */
public class ScopedSession
        extends ScopedAttributeContainer
        implements HttpSession, Serializable
{
    private transient HttpSession _session;
    private transient ServletContext _servletContext;

    /**
     * This constructor exists only for deserialization.
     */ 
    public ScopedSession()
    {
        super( null );
    }

    public ScopedSession( HttpSession session, ServletContext cxt, Object scopeKey )
    {
        super( scopeKey );
        _session = session;
        _servletContext = cxt;
    }

    public long getCreationTime()
    {
        return _session.getCreationTime();
    }

    public String getId()
    {
        return getScopedName( _session.getId() );
    }

    public long getLastAccessedTime()
    {
        return _session.getLastAccessedTime();
    }

    public ServletContext getServletContext()
    {
        return _servletContext;
    }

    public void setMaxInactiveInterval( int i )
    {
        _session.setMaxInactiveInterval( i );
    }

    public int getMaxInactiveInterval()
    {
        return _session.getMaxInactiveInterval();
    }

    public HttpSessionContext getSessionContext()
    {
        return _session.getSessionContext();
    }

    public Object getValue( String s )
    {
        return getAttribute( s );
    }

    public String[] getValueNames()
    {
        return getAttributeNamesArray();
    }

    public void putValue( String s, Object o )
    {
        setAttribute( s, o );
    }

    public void removeValue( String s )
    {
        removeAttribute( s );
    }

    public void invalidate()
    {
        removeAllAttributes();
    }

    public boolean isNew()
    {
        return _session.isNew();
    }
    
    /**
     * Since _session is transient, this method is called by {@link ScopedRequestImpl#getSession}
     * to reinitialize it each time.
     */ 
    void setSession( HttpSession session, ServletContext cxt )
    {
        _session = session;
        _servletContext = cxt;
    }
    
    /**
     * Returns the real (outer) HttpSession.
     */ 
    public HttpSession getOuterSession()
    {
        return _session;
    }
}
