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
package org.apache.beehive.netui.pageflow.scoping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


/**
 * A wrapper around HttpServletRequest, associated with a given scope-key.  All calls to setAttribute,
 * getAttribute, removeAttribute, etc. are scoped to this object, while most other functionality
 * delegates to the wrapped HttpServletRequest.
 * Instances of this class also keep track of their own request-URIs, which are independent of the
 * wrapped request-URIs.
 */
public interface ScopedRequest
        extends HttpServletRequest
{
    public String AUTOSCOPE_PREFIX = "_autoscope_";
    
    
    public void setRequestURI( String uri );

    /**
     * Adds a scope to "listen" to.  This scope will see all request parameters from a ScopedRequest
     * of the given scope.
     */ 
    public void addListenScope( Object scopeKey );
    
    public void doForward();

    public String getForwardedURI();

    /**
     * @deprecated Use {@link ScopedResponse#didRedirect} instead.
     */ 
    public boolean didRedirect();
    
    /**
     * Stores the current map of request attributes in the Session.
     * @deprecated Moved the persisting of attributes out of the beehive NetUI
     *             layer. Use {@link #getAttributeMap} to get the attributes.
     */
    public void persistAttributes();

    /**
     * Restores the map of request attributes from a map saved in the Session.
     * @deprecated Moved the persisting of attributes out of the beehive NetUI
     *             layer. Use {@link #setAttributeMap} to set/merge the attributes.
     */
    public void restoreAttributes();
    
    /**
     * Get the current map of request attributes.
     */
    public Map getAttributeMap();

    /**
     * Set/merge the map of request attributes from a map (saved in the Session).
     */
    public void setAttributeMap( Map map );

    public HttpServletRequest getOuterRequest();
    
    public Object getScopeKey();
    
    public void renameScope( Object newScopeKey );
    
    /**
     * Makes this request listen to specially-prefixed request parameters.
     */ 
    public void setActiveRequest();
    
    public String getScopedName( String baseName );

    public void registerOuterAttribute( String attrName );

    public String getLocalParameter( String attrName );
    public String getListenScopeParameter( String attrName );
    public boolean hasListenScopes();
    
    /**
     * Same as <code>getAttribute</code>, but allows outer request attributes to be hidden explicitly, even if the implementation
     * of getAttribute shows them by default.
     */ 
    public Object getAttribute( String attrName, boolean allowOuterRequestAttributes );
    
    /**
     * @exclude
     */ 
    public Map filterParameterMap( Map parameterMap );
    
    /**
     * Simply stores the URI that was being forwarded to.
     */
    public void setForwardedURI( String uri );
}
