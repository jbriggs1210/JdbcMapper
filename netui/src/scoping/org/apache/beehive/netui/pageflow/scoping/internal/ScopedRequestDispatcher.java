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


import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.apache.beehive.netui.pageflow.scoping.ScopedServletUtils;
import org.apache.beehive.netui.util.logging.Logger;


/**
 * A request dispatcher that doesn't actually forward (but keeps track of the attempted
 * forward), and which does some extra work to do server includes into our ScopedRequest
 * and ScopedResponse.
 *
 * @see ScopedRequestImpl
 * @see ScopedResponseImpl
 */
public class ScopedRequestDispatcher
        implements RequestDispatcher
{
    private String _uri;

    private static final String REQUEST_URI_INCLUDE = "javax.servlet.include.request_uri";

    private static final Logger logger = Logger.getInstance( ScopedRequestDispatcher.class );
    
    
    /**
     * Constructor.
     * 
     * @param uri the URI to which we'll "forward" or include.
     */ 
    public ScopedRequestDispatcher( String uri )
    {
        _uri = uri;
    }

    /**
     * Does not actually cause a server forward of the request, but informs the ScopedRequest
     * object that a forward was attempted for a particular URI.
     * 
     * @param request the ScopedRequest, or a wrapper (ServletRequestWrapper) around it.
     * @param response the ScopedResponse, or a wrapper (ServletResponseWrapper) around it.
     */ 
    public void forward( ServletRequest request, ServletResponse response )
            throws ServletException, IOException
    {
        ScopedRequestImpl scopedRequest = ( ScopedRequestImpl ) ScopedServletUtils.unwrapRequest( request );
        assert scopedRequest != null : request.getClass().getName();
        scopedRequest.setForwardedURI( _uri );
        scopedRequest.doForward();
    }

    /**
     * Does a server include of the stored URI into the given ScopedRequest and ScopedResponse.
     * 
     * @param request the ScopedRequest, or a wrapper (ServletRequestWrapper) around it.
     * @param response the ScopedResponse, or a wrapper (ServletResponseWrapper) around it.
     */ 
    public void include( ServletRequest request, ServletResponse response )
            throws ServletException, IOException
    {
        assert request instanceof HttpServletRequest : request.getClass().getName();
        HttpServletRequest httpRequest = ( HttpServletRequest ) request;
        
        //
        // First, unwrap the request and response, looking for our ScopedRequest and ScopedResponse.
        //
        HttpServletRequest outerRequest = ScopedServletUtils.getOuterRequest( httpRequest );
        
        //
        // Need to set the "javax.servlet.include.request_uri" attribute on the outer request
        // before forwarding with a request dispatcher.  This attribute is used to keep track of
        // the included URI.
        //
        outerRequest.setAttribute( REQUEST_URI_INCLUDE, httpRequest.getRequestURI());
        
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Delegating to RequestDispatcher for URI " + _uri );
        }
        
        try
        {
            RequestDispatcher realDispatcher = outerRequest.getRequestDispatcher( _uri );
            
            if ( realDispatcher == null )
            {
                assert response instanceof HttpServletResponse : response.getClass().getName();
                ( ( HttpServletResponse ) response ).setStatus( HttpServletResponse.SC_NOT_FOUND );                
                logger.error( "Could not get RequestDispatcher for URI " + _uri );
            }
            else
            {
                realDispatcher.include( request, response );
            }
        }
        catch ( ServletException e )
        {
            logger.error( "Exception during RequestDispatcher.include().", e.getRootCause() );
            
            throw e;
        }
    }
}
