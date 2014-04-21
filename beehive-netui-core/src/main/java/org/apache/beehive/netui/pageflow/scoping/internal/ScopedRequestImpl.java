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

import org.apache.beehive.netui.pageflow.scoping.ScopedRequest;
import org.apache.beehive.netui.pageflow.scoping.ScopedServletUtils;
import org.apache.beehive.netui.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.Map;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.codec.DecoderException;


/**
 * A wrapper around HttpServletRequest, associated with a given scope-key.  All calls to setAttribute,
 * getAttribute, removeAttribute, etc. are scoped to this object, while most other functionality
 * delegates to the wrapped HttpServletRequest.
 * Instances of this class also keep track of their own request-URIs, which are independent of the
 * wrapped request-URIs.
 */
public class ScopedRequestImpl
        extends HttpServletRequestWrapper
        implements ScopedRequest
{
    private String _requestURI;
    private String _servletPath;
    private String _forwardedURI;
    private ScopedAttributeContainer _scopedContainer;
    private List _listenScopes;
    private String _overridePathInfo = null;
    private boolean _isActiveRequest = false;
    private boolean _seeOuterRequestAttributes = false;
    private Set _visibleOuterRequestAttrs;
    private Map _additionalParameters;


    static final String ATTR_PREFIX = "_netui:";
    private static final String OUR_SESSION_ATTR = ATTR_PREFIX + "scopedSession";
    private static final String STORED_ATTRS_ATTR = ATTR_PREFIX + "storedAttrs";

    private static final Logger _log = Logger.getInstance( ScopedRequestImpl.class );
    private static final URLCodec URL_CODEC = new URLCodec();


    public ScopedRequestImpl( HttpServletRequest req, String overrideRequestURI, Object scopeKey,
                              ServletContext servletContext, boolean seeOuterRequestAttributes )
    {
        super( req );
        
        _scopedContainer = new ScopedAttributeContainer( scopeKey );
        setRequestURI( overrideRequestURI );
        _seeOuterRequestAttributes = seeOuterRequestAttributes;
        
        if ( ! seeOuterRequestAttributes ) _visibleOuterRequestAttrs = new HashSet();
    }

    /**
     * @deprecated Use {@link #ScopedRequestImpl(HttpServletRequest, String, Object, ServletContext, boolean)}.
     */ 
    public ScopedRequestImpl( HttpServletRequest req, String overrideRequestURI, Object scopeKey,
                              ServletContext context )
    {
        this( req, overrideRequestURI, scopeKey, context, false );
    }

    public String getRequestedSessionId()
    {
        return super.getRequestedSessionId();
    }

    public String getRequestURI()
    {
        return _requestURI;
    }

    public void setRequestURI( String uri )
    {
        _requestURI = uri;
        
        if ( uri == null )
        {
            _servletPath = null;
            return;
        }
        
        //
        // Set the servlet path (decoded)
        //
        assert uri.startsWith( getOuterRequest().getContextPath() ) : uri;
        setServletPath( uri.substring( getOuterRequest().getContextPath().length() ) );
    }
        
    public void setRequestURI( String contextPath, String servletPath )
    {
        _requestURI = contextPath + servletPath;
        setServletPath( servletPath );
    }
    
    private void setServletPath( String servletPath )
    {
        String encoding = getCharacterEncoding();
        
        try
        {            
            if ( encoding == null ) encoding = "utf-8";
            servletPath = URL_CODEC.decode( servletPath, encoding );
        }
        catch ( DecoderException e )
        {
            _log.error( "error decoding path " + servletPath, e );
        }
        catch ( UnsupportedEncodingException e )
        {
            _log.error( "unsupported encoding " + encoding + " while decoding path " + servletPath, e );
        }
        
        _servletPath = ScopedServletUtils.normalizeURI( servletPath );
    }
    
    public StringBuffer getRequestURL()
    {
        HttpServletRequest outerRequest = getOuterRequest();
        StringBuffer url = new StringBuffer( outerRequest.getScheme() );
        url.append( "://" ).append( outerRequest.getServerName() );
        url.append( ':' ).append( outerRequest.getServerPort() );
        url.append( getRequestURI() );
        return url;
    }

    public String getServletPath()
    {
        return _servletPath;
    }

    public String getParameter( String paramName )
    {
        String retVal = getLocalParameter( paramName );
        if ( retVal == null )
        {
            retVal = getListenScopeParameter( paramName );
        }
        
        return retVal;
    }

    /**
     * Add a parameter to the request.
     * 
     * @param name the parameter name.
     * @param value the parameter value.
     */ 
    public void addParameter( String name, String value )
    {
        if ( _additionalParameters == null )
        {
            _additionalParameters = new HashMap();
        }
        
        _additionalParameters.put( name, value );
    }
    
    /**
     * Get the parameter from the scoped request only (don't check in listen scoped requests)
     * @param paramName
     * @return value of the parameter
     */
    public String getLocalParameter( String paramName )
    {
        if ( _additionalParameters != null )
        {
            String overrideParam = ( String ) _additionalParameters.get( paramName );

            if ( overrideParam != null )
            {
                return overrideParam;
            }
        }

        ServletRequest request = getRequest();
        String retVal = request.getParameter( _scopedContainer.getScopedName( paramName ) );

        if ( retVal == null && _isActiveRequest && paramName.startsWith( AUTOSCOPE_PREFIX ) )
        {
            retVal = request.getParameter( paramName );
        }
        
        return retVal;
    }

    /**
     * Get the parameter from the listen scoped requests
     * @param paramName
     * @return value of the parameter
     */
    public String getListenScopeParameter( String paramName )
    {
        String retVal = null;

        if ( _listenScopes != null )
        {
            for ( int i = 0, len = _listenScopes.size(); retVal == null && i < len; ++i )
            {
                String key = ScopedServletUtils.getScopedName( paramName, _listenScopes.get( i ) );
                retVal = getRequest().getParameter( key );
            }
        }

        return retVal;
    }

    
    public Enumeration getParameterNames()
    {
        ArrayList paramNames = new ArrayList();

        for ( Enumeration e = getRequest().getParameterNames(); e.hasMoreElements(); )
        {
            String scopedParamName = ( String ) e.nextElement();

            if ( _scopedContainer.isInScope( scopedParamName ) )
            {
                paramNames.add( _scopedContainer.removeScope( scopedParamName ) );
            }
            else if ( _isActiveRequest && scopedParamName.startsWith( AUTOSCOPE_PREFIX ) )
            {
                paramNames.add( scopedParamName );
            }
            else if ( _listenScopes != null )
            {
                for ( int i = 0, len = _listenScopes.size(); i < len; ++i )
                {
                    Object scope = _listenScopes.get( i );

                    if ( ScopedAttributeContainer.isInScope( scopedParamName, scope ) )
                    {
                        paramNames.add( ScopedAttributeContainer.removeScope( scopedParamName, scope ) );
                    }
                }
            }
        }

        return Collections.enumeration( paramNames );
    }

    public String[] getParameterValues( String paramName )
    {
        ServletRequest request = getRequest();
        String[] retVals = request.getParameterValues( _scopedContainer.getScopedName( paramName ) );

        if ( retVals == null && _isActiveRequest && paramName.startsWith( AUTOSCOPE_PREFIX ) )
        {
            retVals = request.getParameterValues( paramName );
        }

        if ( retVals == null && _listenScopes != null )
        {
            for ( int i = 0, len = _listenScopes.size(); retVals == null && i < len; ++i )
            {
                String key = ScopedServletUtils.getScopedName( paramName, _listenScopes.get( i ) );
                retVals = request.getParameterValues( key );
            }
        }

        return retVals;
    }

    public Map getParameterMap()
    {
        return filterParameterMap( getRequest().getParameterMap() );
    }
    
    /**
     * @exclude
     */ 
    public Map filterParameterMap( Map parameterMap )
    {
        HashMap map = new HashMap();

        for ( Iterator i = parameterMap.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry entry = ( Map.Entry ) i.next();
            String scopedParamName = ( String ) entry.getKey();

            if ( _scopedContainer.isInScope( scopedParamName ) )
            {
                map.put( _scopedContainer.removeScope( scopedParamName ), entry.getValue() );
            }
            else if ( _isActiveRequest && scopedParamName.startsWith( AUTOSCOPE_PREFIX ) )
            {
                map.put( scopedParamName, entry.getValue() );
            }
            else if ( _listenScopes != null )
            {
                for ( int j = 0, len = _listenScopes.size(); j < len; ++j )
                {
                    if ( ScopedAttributeContainer.isInScope( scopedParamName, _listenScopes.get( j ) ) )
                    {
                        map.put( ScopedAttributeContainer.removeScope( scopedParamName, _listenScopes.get( j ) ),
                                 entry.getValue() );
                    }
                }
            }
        }

        return map;
    }

    /**
     * Adds a scope to "listen" to.  This scope will see all request parameters from a ScopedRequest
     * of the given scope.
     */
    public void addListenScope( Object scopeKey )
    {
        assert scopeKey != null;

        if ( _listenScopes == null )
        {
            _listenScopes = new ArrayList();
        }

        _listenScopes.add( scopeKey );
    }

    public RequestDispatcher getRequestDispatcher( String uri )
    {
        return new ScopedRequestDispatcher( uri );
    }

    public void doForward()
    {
        String forwardedURI = _forwardedURI;
        
        if ( forwardedURI != null )
        {
            if ( ! forwardedURI.startsWith( "/" ) )
            {
                int lastSlash = _requestURI.lastIndexOf( '/' );
                assert lastSlash != -1 : _requestURI;
                setRequestURI( _requestURI.substring( 0, lastSlash + 1 ) + forwardedURI );
            }
            else
            {
                setRequestURI( getOuterRequest().getContextPath(), forwardedURI );
            }
            
            // Parse the query string and add parameters to the internal map
            parseQueryParameters();
        }
        else
        {
            setRequestURI( null );
        }
    }

    private void parseQueryParameters()
    {
        int queryIndex = _requestURI.indexOf("?");
        if (queryIndex < 0)
        {
            return;
        }

        String queryString = _requestURI.substring(queryIndex + 1);

        // hack off the query string
        _requestURI = _requestURI.substring(0, queryIndex);

        if (queryString.length() == 0)
        {
            return;
        }

        HashMap queryParameters = new HashMap();
        ParseUtils.parseQueryString(queryString, queryParameters, getCharacterEncoding());  

        Iterator itor = queryParameters.keySet().iterator();
        while (itor.hasNext())
        {
            Object key = itor.next();
            addParameter((String) key, (String) queryParameters.get(key));
        }
    }

    /**
     * Simply stores the URI that was being forwarded to.
     *
     * @param uri
     */
    public void setForwardedURI( String uri )
    {
        _forwardedURI = uri;
    }

    public String getForwardedURI()
    {
        return _forwardedURI;
    }

    /**
     * @deprecated Use {@link ScopedResponseImpl#didRedirect} instead.
     */ 
    public boolean didRedirect()
    {
        return false;
    }

    /**
     * Stores the current map of request attributes in the Session.
     * @deprecated Moved the persisting of attributes out of the beehive NetUI
     *             layer. Use {@link #getAttributeMap} to get the attributes.
     */
    public void persistAttributes()
    {
        String attrName = getScopedName( STORED_ATTRS_ATTR );
        getSession().setAttribute( attrName, _scopedContainer.getSerializableAttrs() );
    }

    /**
     * Restores the map of request attributes from a map saved in the Session.
     * @deprecated Moved the persisting of attributes out of the beehive NetUI
     *             layer. Use {@link #setAttributeMap} to set/merge the attributes.
     */
    public void restoreAttributes()
    {
        String attrName = getScopedName( STORED_ATTRS_ATTR );
        Map savedAttrs = ( Map ) getSession().getAttribute( attrName );

        if ( savedAttrs != null )
        {
            setAttributeMap( savedAttrs );
        }
    }

    /**
     * Get the current map of request attributes.
     */
    public Map getAttributeMap()
    {
        return _scopedContainer.getAttrMap();
    }

    /**
     * Set/merge the map of request attributes from a given map.
     * Do not call this method with a null Map.
     *
     * @param savedAttrs the Map of attributes to set or merge with
     *        the current map of request attributes
     */
    public void setAttributeMap( Map savedAttrs )
    {
        assert savedAttrs != null : "Map of attributes must be non-null";

        Map currentAttrs = _scopedContainer.getAttrMap();

        Map attrs = new HashMap();
        attrs.putAll( savedAttrs );

        if ( currentAttrs != null )
        {
            attrs.putAll( currentAttrs );
        }

        _scopedContainer.setAttrMap( attrs  );
    }

    public final HttpServletRequest getOuterRequest()
    {
        return ( HttpServletRequest ) getRequest();
    }

    public final Object getAttribute( String attrName )
    {
        return getAttribute( attrName, true );
    }
    
    public final Object getAttribute( String attrName, boolean allowOuterRequestAttributes )
    {
        if ( ! allowOuterRequestAttributes ) return _scopedContainer.getAttribute( attrName );
        
        ServletRequest outerRequest = getRequest();
        
        if ( ! _seeOuterRequestAttributes && _visibleOuterRequestAttrs.contains( attrName ) )
        {
            return outerRequest.getAttribute( attrName );
        }

        Object value = _scopedContainer.getAttribute( attrName );
        
        if ( value == null && _seeOuterRequestAttributes )
        {
            value = outerRequest.getAttribute( attrName );
        }
        
        return value;
    }

    public final void setAttribute( String attrName, Object o )
    {
        if ( ! _seeOuterRequestAttributes && _visibleOuterRequestAttrs.contains( attrName ) )
        {
            getRequest().setAttribute( attrName, o );
        }
        else
        {
            _scopedContainer.setAttribute( attrName, o );
        }
    }

    public final Enumeration getAttributeNames()
    {
        Set set = new HashSet();

        if ( ! _seeOuterRequestAttributes )
        {
            for ( Enumeration e = getRequest().getAttributeNames(); e.hasMoreElements(); )
            {
                Object attrName = e.nextElement();
                if ( _visibleOuterRequestAttrs.contains( attrName ) ) set.add( attrName );
            }
        }

        for ( Enumeration e = _scopedContainer.getAttributeNames(); e.hasMoreElements(); )
        {
            set.add( e.nextElement() );
        }
        
        if ( _seeOuterRequestAttributes )
        {
            for ( Enumeration e = getRequest().getAttributeNames(); e.hasMoreElements(); )
            {
                set.add( e.nextElement() );
            }
        }

        return Collections.enumeration( set );
    }

    public final void removeAttribute( String attrName )
    {
        if ( ! _seeOuterRequestAttributes && _visibleOuterRequestAttrs.contains( attrName ) )
        {
            getRequest().removeAttribute( attrName );
        }
        else
        {
            _scopedContainer.removeAttribute( attrName );
        }
    }

    public void registerOuterAttribute( String attrName )
    {
        assert ! _seeOuterRequestAttributes :
                "(attribute " + attrName + ") " +
                "this method is not valid unless the ScopedRequest is configured not to see outer request attributes";

        if (_seeOuterRequestAttributes) {
            _log.error("the ScopedRequest is already configured to see outer request attributes");
            return;
        }

        _visibleOuterRequestAttrs.add( attrName );
    }

    public final Object getScopeKey()
    {
        return _scopedContainer.getScopeKey();
    }

    public void renameScope( Object newScopeKey )
    {
        _scopedContainer.renameScope( newScopeKey );
    }

    public static void renameSessionScope( Object oldScopeKey, Object newScopeKey, HttpServletRequest outerRequest )
    {
        HttpSession realSession = outerRequest.getSession( false );

        if ( realSession != null )
        {
            String realSessionAttr = ScopedServletUtils.getScopedName( OUR_SESSION_ATTR, oldScopeKey );
            Object ourSession = realSession.getAttribute( realSessionAttr );
            realSessionAttr = ScopedServletUtils.getScopedName( OUR_SESSION_ATTR, newScopeKey );
            realSession.setAttribute( realSessionAttr, ourSession );
        }
    }

    public String getPathInfo()
    {
        return _overridePathInfo;
    }

    public void setPathInfo( String pathInfo )
    {
        _overridePathInfo = pathInfo;
    }

    /**
     * Makes this request listen to specially-prefixed request parameters.
     */
    public void setActiveRequest()
    {
        _isActiveRequest = true;
    }
    
    public final String getScopedName( String baseName )
    {
        return _scopedContainer.getScopedName( baseName );
    }

    /**
     * see if this scoped request is listening to any other scoped request
     * @return true if has listen scopes
     */
    public boolean hasListenScopes()
    {
         return _listenScopes != null && _listenScopes.size() > 0;
    }
}

