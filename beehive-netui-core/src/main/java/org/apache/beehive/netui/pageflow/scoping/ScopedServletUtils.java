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

import org.apache.beehive.netui.pageflow.scoping.internal.ScopedRequestImpl;
import org.apache.beehive.netui.pageflow.scoping.internal.ScopedResponseImpl;
import org.apache.beehive.netui.util.logging.Logger;
import org.apache.struts.upload.MultipartRequestWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;

import java.net.URI;
import java.net.URISyntaxException;


/**
 * Utilities for creating scoped wrapper versions of HttpRequest, HttpResponse, ServletContext.  These
 * wrappers are the basis for a scoped servlet environment, which can be used to scope the Struts
 * framework.
 */
public class ScopedServletUtils
{
    public static final String SCOPE_ID_PARAM = "jpfScopeID";
    
    static final String ATTR_PREFIX = "_netui:";
    private static final String OVERRIDE_REQUEST_ATTR = ATTR_PREFIX + "overrideRequest";
    private static final String OVERRIDE_RESPONSE_ATTR = ATTR_PREFIX + "overrideResponse";
    
    private static final Logger logger = Logger.getInstance( ScopedServletUtils.class );
    
    
    /**
     * Get the cached ScopedRequest wrapper.  If none exists, creates one and caches it.
     * @deprecated Use {@link #getScopedRequest(HttpServletRequest, String, ServletContext, Object, boolean)}.
     *
     * @param realRequest the "real" (outer) HttpServletRequest, which will be wrapped.
     * @param overrideURI the request-URI for the wrapped object.  This URI must begin with the context path.
     * @param servletContext the current ServletContext.
     * @param scopeKey the scope-key associated with the new (or looked-up) scoped request.
     * @return the cached (or newly-created) ScopedRequest.
     */
    public static ScopedRequest getScopedRequest( HttpServletRequest realRequest, String overrideURI,
                                                  ServletContext servletContext, Object scopeKey )
    {
        return getScopedRequest( realRequest, overrideURI, servletContext, scopeKey, false );
    }
    
    /**
     * Get the cached ScopedRequest wrapper.  If none exists, creates one and caches it.
     *
     * @param realRequest the "real" (outer) HttpServletRequest, which will be wrapped.
     * @param overrideURI the request-URI for the wrapped object.  This URI must begin with the context path.
     * @param servletContext the current ServletContext.
     * @param scopeKey the scope-key associated with the new (or looked-up) scoped request.
     * @param seeOuterRequestAttributes if <code>true</code>, a request attribute will be "seen" in the outer request,
     *            if it is not found within the scoped request; if <code>false</code>, attributes are only seen when
     *            they are present in the scoped request.
     * @return the cached (or newly-created) ScopedRequest.
     */
    public static ScopedRequest getScopedRequest( HttpServletRequest realRequest, String overrideURI,
                                                  ServletContext servletContext, Object scopeKey,
                                                  boolean seeOuterRequestAttributes )
    {
        assert ! ( realRequest instanceof ScopedRequest );
        
        String requestAttr = getScopedName( OVERRIDE_REQUEST_ATTR, scopeKey );
        ScopedRequest scopedRequest = ( ScopedRequest ) realRequest.getAttribute( requestAttr );
        
        //
        // If it doesn't exist, create it and cache it.
        //
        if ( scopedRequest == null )
        {
            //
            // The override URI must start with a slash -- it's webapp-relative.
            //
            if ( overrideURI != null && ! overrideURI.startsWith( "/" ) ) overrideURI = '/' + overrideURI;
            
            scopedRequest = 
                new ScopedRequestImpl( realRequest, overrideURI, scopeKey, servletContext, seeOuterRequestAttributes );
            realRequest.setAttribute( requestAttr, scopedRequest );            
        }
        
        return scopedRequest;
    }

    /**
     * Get the cached wrapper servlet response.  If none exists, creates one and caches it.
     *
     * @param realResponse the "real" (outer) ServletResponse, which will be wrapped.
     * @param scopedRequest the ScopedRequest returned from {@link #getScopedRequest}.
     * @return the cached (or newly-created) ScopedResponse.
     */
    public static ScopedResponse getScopedResponse( HttpServletResponse realResponse,
                                                    ScopedRequest scopedRequest )
    {
        assert ! ( realResponse instanceof ScopedResponse );
        
        String responseAttr = getScopedName( OVERRIDE_RESPONSE_ATTR,
                                                                      scopedRequest.getScopeKey() );
        HttpServletRequest outerRequest = scopedRequest.getOuterRequest();
        ScopedResponse scopedResponse = ( ScopedResponse ) outerRequest.getAttribute( responseAttr );
        
        //
        // If it doesn't exist, create it and cache it.
        //
        if ( scopedResponse == null )
        {
            scopedResponse = new ScopedResponseImpl( realResponse );
            outerRequest.setAttribute( responseAttr, scopedResponse );
        }
        
        return scopedResponse;
    }

    /**
     * Find all scoped objects ({@link ScopedRequest}, {@link ScopedResponse})
     * which have a certain scope-key, replaces this scope-key with the new one, and re-caches the objects
     * the new scope-key.
     * @param oldScopeKey
     * @param newScopeKey
     * @param request the real (outer) request, where the scoped objects are cached.
     */ 
    public static void renameScope( Object oldScopeKey, Object newScopeKey, HttpServletRequest request )
    {
        assert ! ( request instanceof ScopedRequest );
        
        String requestAttr = getScopedName( OVERRIDE_REQUEST_ATTR, oldScopeKey );
        String responseAttr = getScopedName( OVERRIDE_RESPONSE_ATTR, oldScopeKey );
        ScopedRequest scopedRequest = ( ScopedRequest ) request.getAttribute( requestAttr );
        ScopedResponse scopedResponse = ( ScopedResponse ) request.getAttribute( responseAttr );
        
        if ( scopedRequest != null )
        {
            scopedRequest.renameScope( newScopeKey );
            request.removeAttribute( requestAttr );
            requestAttr = getScopedName( OVERRIDE_REQUEST_ATTR, newScopeKey );
            request.setAttribute( requestAttr, scopedRequest );
        }
        else
        {
            ScopedRequestImpl.renameSessionScope( oldScopeKey, newScopeKey, request );
        }

        if ( scopedResponse != null )
        {
            request.removeAttribute( responseAttr );
            responseAttr = getScopedName( OVERRIDE_RESPONSE_ATTR, newScopeKey );
            request.setAttribute( responseAttr, scopedResponse );
        }
    }    
    
    /**
     * Get a scoped version of a given name.
     * 
     * @param baseName the name to be scoped.
     * @param scopeKey the context key for scoping the name.
     * @return a scoped version of the given name.
     */ 
    public static String getScopedName( String baseName, Object scopeKey )
    {
        return scopeKey + baseName;
    }    
    
    /**
     * Tell whether this is a scoped request.
     * 
     * @param request the ServletRequest to test.
     * @return <code>true</code> if the given ServletRequest is a ScopedRequest.
     */ 
    /*
    public static boolean isScoped( ServletRequest request )
    {
 
    }
    */
        
    /**
     * Get the outer (unwrapped) request.
     * 
     * @param request the request to unwrap.
     * @return the outer request, if the given request is a ScopedRequest (or wraps a ScopedRequest);
     *         otherwise, the given request itself.
     */ 
    public static HttpServletRequest getOuterRequest( HttpServletRequest request )
    {
        ScopedRequest scopedRequest = unwrapRequest( request );
        return scopedRequest != null ? scopedRequest.getOuterRequest() : request;
    }
    
    /**
     * Get the outer (unwrapped) request.
     * 
     * @param request the request to unwrap.
     * @return the outer request, if the given request is a ScopedRequest (or wraps a ScopedRequest);
     *         otherwise, the given request itself.
     */ 
    public static ServletRequest getOuterServletRequest( ServletRequest request )
    {
        ScopedRequest scopedRequest = unwrapRequest( request );
        return scopedRequest != null ? scopedRequest.getOuterRequest() : request;
    }
    
    /**
     * Unwraps the contained ScopedRequest from the given ServletRequest, which may be a
     * ServletRequestWrapper.
     * 
     * @param request the ScopedRequest, or a wrapper (ServletRequestWrapper) around it.
     * @return the unwrapped ScopedRequest.
     * 
     * @exclude
     */ 
    public static ScopedRequest unwrapRequest( ServletRequest request )
    {
        // Unwrap the multipart request, if there is one.
        if ( request instanceof MultipartRequestWrapper )
        {
            request = ( ( MultipartRequestWrapper ) request ).getRequest();
        }

        while ( request instanceof ServletRequestWrapper )
        {
            if ( request instanceof ScopedRequest )
            {
                return ( ScopedRequest ) request;
            }
            else
            {
                request = ( ( ServletRequestWrapper ) request ).getRequest();
            }
        }
        
        return null;
    }  
    
    /**
     * Unwraps the contained ScopedResponseImpl from the given ServletResponse, which may be a
     * ServletResponseWrapper.
     * 
     * @param response the ScopedResponse, or a wrapper (ServletResponseWrapper) around it.
     * @return the unwrapped ScopedResponseImpl.
     * 
     * @exclude
     */ 
    public static ScopedResponse unwrapResponse( ServletResponse response )
    {
        while ( response instanceof ServletResponseWrapper )
         {
             if ( response instanceof ScopedResponse )
             {
                 return ( ScopedResponse ) response;
             }
             else
             {
                 response = ( ( ServletResponseWrapper ) response ).getResponse();
             }
         }
        
         return null;
     }
    
    /**
     * If the request is a ScopedRequest, this returns an attribute name scoped to
     * that request's scope-ID; otherwise, it returns the given attribute name.
     * 
     * @exclude
     */ 
    public static String getScopedSessionAttrName( String attrName, HttpServletRequest request )
    {
        String requestScopeParam = request.getParameter( SCOPE_ID_PARAM );
        
        if ( requestScopeParam != null )
        {
            return getScopedName( attrName, requestScopeParam );
        }
        
        ScopedRequest scopedRequest = unwrapRequest( request );
        return scopedRequest != null ? scopedRequest.getScopedName( attrName ) : attrName;
    }
    
    /**
     * If the request is a ScopedRequest, this returns an attribute whose name is scoped to that request's scope-ID;
     * otherwise, it is a straight passthrough to {@link HttpSession#getAttribute}.
     * 
     * @exclude
     */ 
    public static Object getScopedSessionAttr( String attrName, HttpServletRequest request )
    {
        HttpSession session = request.getSession( false );
        
        if ( session != null )
        {
            return session.getAttribute( getScopedSessionAttrName( attrName, request ) );
        }
        else
        {
            return null;
        }
    }

    /**
     * If the request is a ScopedRequest, this sets an attribute whose name is scoped to that request's scope-ID;
     * otherwise, it is a straight passthrough to {@link HttpSession#setAttribute}.
     * 
     * @exclude
     */ 
    public static void setScopedSessionAttr( String attrName, Object val, HttpServletRequest request )
    {
        request.getSession().setAttribute( getScopedSessionAttrName( attrName, request ), val );
    }

    /**
     * If the request is a ScopedRequest, this removes an attribute whose name is scoped to that request's scope-ID;
     * otherwise, it is a straight passthrough to {@link HttpSession#removeAttribute}.
     * 
     * @exclude
     */ 
    public static void removeScopedSessionAttr( String attrName, HttpServletRequest request )
    {
        HttpSession session = request.getSession( false );
        
        if ( session != null )
        {
            session.removeAttribute( getScopedSessionAttrName( attrName, request ) );
        }
    }
    
    /**
     * Get an attribute from the given request, and if it is a {@link ScopedRequest}, ensure that the attribute
     * is <strong>not</strong> "showing through" from the outer request, even if the ScopedRequest allows that by
     * default.
     * 
     * @exclude
     */ 
    public static Object getScopedRequestAttribute( String attrName, ServletRequest request )
    {
        if ( request instanceof ScopedRequest )
        {
            return ( ( ScopedRequest ) request ).getAttribute( attrName, false );
        }
        
        return request.getAttribute( attrName );
    }
    
    /**
     * Get the request URI, relative to the webapp root.
     *
     * @param request the current HttpServletRequest.
     */
    public static final String getRelativeURI( HttpServletRequest request )
    {
        return request.getServletPath();
    }

    /**
     * Get a URI relative to the webapp root.
     *
     * @param request the current HttpServletRequest.
     * @param uri the URI which should be made relative.
     */
    public static final String getRelativeURI( HttpServletRequest request, String uri )
    {
        return getRelativeURI( request.getContextPath(), uri );
    }
    
    /**
     * Get a URI relative to a given webapp root.
     *
     * @param contextPath the webapp context path, e.g., "/myWebapp"
     * @param uri the URI which should be made relative.
     */
    public static final String getRelativeURI( String contextPath, String uri )
    {
        String requestUrl = uri;
        int overlap = requestUrl.indexOf( contextPath + '/' );
        assert overlap != -1 : "contextPath: " + contextPath + ", uri: " + uri;
        return requestUrl.substring( overlap + contextPath.length() );
    }

    /**
     * Resolve "." and ".." in a URI.
     * @exclude
     */ 
    public static String normalizeURI( String uri )
    {
        //
        // If it's a relative URI, normalize it.  Note that we don't want to create a URI
        // (very expensive) unless we think we'll need to.  "./" catches "../" and "./".
        //
        if ( uri.indexOf( "./" ) != -1 )
        {
            try
            {
                uri = new URI( uri ).normalize().toString();
            }
            catch ( URISyntaxException e )
            {
                logger.error( "Could not parse relative URI " + uri );
            }
        }
        
        return uri;
    }
    
    
    /**
     * @exclude
     */ 
    public static String decodeURI( HttpServletRequest request )
    {
        return request.getContextPath() + request.getServletPath();     // TODO: always decoded?
    }
}
