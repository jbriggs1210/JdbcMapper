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

import org.apache.beehive.netui.pageflow.scoping.ScopedResponse;
import org.apache.beehive.netui.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.util.HashMap;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * A wrapper around HttpServletResponse, associated with a given scope-key.  Delegates to the wrapped
 * response object for some functionality, but prevents output or error codes or forwards from actually
 * happening.
 */
public class ScopedResponseImpl
        extends HttpServletResponseWrapper
        implements ScopedResponse
{
    private static final Cookie[] NO_COOKIES = new Cookie[0];
    
    private boolean _isError = false;
    private int _statusCode = -1;
    private String _redirectURI = null;
    private String _statusMessage = null;

    /** Map of name (String) -> headers (List).  There can be more than one for each name. **/
    private HashMap _headers = new HashMap();

    private static final String SET_COOKIE = "Set-Cookie";
    private static final Logger logger = Logger.getInstance( ScopedResponseImpl.class );


    public ScopedResponseImpl( HttpServletResponse servletResponse )
    {
        super( servletResponse );
    }

    public void sendError( int i, String s ) throws IOException
    {
        _isError = true;
        _statusCode = i;
        _statusMessage = s;

        if ( logger.isInfoEnabled() )
        {
            StringBuffer msg = new StringBuffer( "ScopedResponse error " ).append( i );
            logger.info( msg.append( ": " ).append( s ) );
        }
    }

    public void sendError( int i ) throws IOException
    {
        sendError( i, "" );
    }

    public void setStatus( int i )
    {
        setStatus( i, "" );
    }

    public void setStatus( int i, String s )
    {
        _statusCode = i;
        _statusMessage = s;
    }

    public void setContentLength( int i )
    {
        // don't do anything
    }

    public void setContentType( String s )
    {
        // don't do anything
    }

    public void setBufferSize( int i )
    {
        // don't do anything
    }

    public void resetBuffer()
    {
        // don't do anything
    }

    public void reset()
    {
        // don't do anything
    }

     //
    // Headers: We need some special handling for headers. Since we're
    // *including* portlets, the response received from WLS will have
    // no-op methods for all headers. So, this implementation collects
    // headers explicitly, to avoid losing them.
    //

    /**
     * Add a cookie to the response.
     */
    public void addCookie( Cookie cookie )
    {
        addObjectHeader(SET_COOKIE, cookie);
    }

    /**
     * Gets a cookie that was added to the response.
     */
    public Cookie getCookie( String cookieName )
    {
        List cookies = getHeaders(SET_COOKIE);
        if(cookies != null){
            // start looking from the back (ie. the last cookie set)
            for(int i = cookies.size(); --i > -1;) {
                Cookie cookie = (Cookie)cookies.get(i);
                if(cookie.getName().equals(cookieName)) {
                    return cookie;
                }
            }
        }

        return null;
    }

    /**
     * Gets all Cookies that were added to the response.
     */
    public Cookie[] getCookies()
    {
        List cookies = (List)_headers.get(SET_COOKIE);
        return cookies != null ? ( Cookie[] ) cookies.toArray( new Cookie[cookies.size()] ) : NO_COOKIES;
    }

    /**
     * Returns <code>true</code> if this response containes the given header.
     */
    public boolean containsHeader( String name )
    {
        return _headers.containsKey( name );
    }

    /**
     * Sets a response header with the given name and date-value.
     */
    public void setDateHeader( String name, long date )
    {
        setObjectHeader( name, new Date( date ) );
    }

    /**
     * Adds a response header with the given name and date-value.
     */
    public void addDateHeader( String name, long date )
    {
        addObjectHeader( name, new Date( date ) );
    }

    /**
     * Sets a response header with the given name and value.
     */
    public void setHeader( String name, String value )
    {
        setObjectHeader( name, value );
    }

    /**
     * Adds a response header with the given name and value.
     */
    public void addHeader( String name, String value )
    {
        addObjectHeader( name, value );
    }

    /**
     * Sets a response header with the given name and integer value.
     */
    public void setIntHeader( String name, int value )
    {
        setObjectHeader( name, new Integer( value ) );
    }

    /**
     * Adds a response header with the given name and integer value.
     */
    public void addIntHeader( String name, int value )
    {
        addObjectHeader( name, new Integer( value ) );
    }

    /**
     * Gets all headers.
     *
     * @return a Map of header-name (String) -> headers (List).
     */
    public Map getHeaders()
    {
        return _headers;
    }

    /**
     * Gets all headers with the given name.
     *
     * @return a List of headers (String, Integer, Date, Cookie), or <code>null</code> if none are found.
     */
    public List getHeaders( String name )
    {
        return ( List ) _headers.get( name );
    }

    /**
     * Gets the first header with the given name.
     * @return an Object (String, Integer, Date, Cookie) that is the first header with the given name,
     *         or <code>null</code> if none is found.
     */
    public Object getFirstHeader( String name )
    {
        List foundHeaders = ( List ) _headers.get( name );
        return ! foundHeaders.isEmpty() ? foundHeaders.get( 0 ) : null;
    }

    protected void addObjectHeader( String name, Object val )
    {
        List vals = ( List ) _headers.get( name );

        if ( vals == null )
        {
            vals = new ArrayList();
            _headers.put( name, vals );
        }

        vals.add( val );
    }

    protected void setObjectHeader( String name, Object val )
    {
        ArrayList vals = new ArrayList();
        vals.add( val );
        _headers.put( name, vals );
    }

    public HttpServletResponse getOuterResponse()
    {
        return (HttpServletResponse) getResponse();
    }

    public boolean isError()
    {
        return _isError;
    }

    public int getStatusCode()
    {
        return _statusCode;
    }

    public String getStatusMessage()
    {
        return _statusMessage;
    }

    public void sendRedirect( String redirectURI )
        throws IOException
    {
        _redirectURI = redirectURI;
    }

    /**
     * Actually send the redirect that was suggested by {@link #sendRedirect}.
     *
     * @throws IllegalStateException if {@link #sendRedirect} was not called.
     */
    public void applyRedirect()
        throws IOException
    {
        if ( _redirectURI != null )
        {
            super.sendRedirect( _redirectURI );
        }
        else
        {
            throw new IllegalStateException( "No redirect to apply." );
        }
    }

    public boolean didRedirect()
    {
        return _redirectURI != null;
    }

    public String getRedirectURI()
    {
        return _redirectURI;
    }
}
