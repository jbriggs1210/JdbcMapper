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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import java.util.List;
import java.util.Map;
import java.io.IOException;


/**
 * A wrapper around HttpServletResponse, associated with a given scope-key.  Delegates to the wrapped
 * response object for some functionality, but prevents output or error codes or forwards from actually
 * happening.
 */
public interface ScopedResponse
        extends HttpServletResponse
{
    /**
     * Get a cookie that was added to the response.
     */ 
    public Cookie getCookie( String cookieName );
    
    /**
     * Get all Cookies that were added to the response.
     */ 
    public Cookie[] getCookies();
    
    /**
     * Get all headers.
     *
     * @return a Map of header-name (String) -> headers (List).
     */
    public Map getHeaders();
    
    /**
     * Get all headers with the given name.
     * 
     * @return a List of headers (String, Integer, Date), or <code>null</code> if none are found.
     */ 
    public List getHeaders( String name );
    
    /**
     * Get the first header with the given name.
     * @return an Object (String, Integer, Date) that is the first header with the given name,
     *         or <code>null</code> if none is found.
     */ 
    public Object getFirstHeader( String name );
    
    public HttpServletResponse getOuterResponse();
    
    /**
     * Tell whether the response is in error.
     * 
     * @return <code>true</code> if {@link #sendError(int,String)} or {@link #sendError(int)} was called.
     */ 
    public boolean isError();

    /**
     * Get the status code on the response.
     * 
     * @return the status code, set by {@link #setStatus(int)}, {@link #sendError(int,String)}, or
     *         {@link #sendError(int)}; -1 if no status was set explicitly.
     */ 
    public int getStatusCode();

    /**
     * Get the status message on the response.
     * 
     * @return the status code, set by {@link #sendError(int,String)}, or <code>null</code> if none was set.
     */ 
    public String getStatusMessage();
    
    /**
     * Tell whether a browser redirect was sent.
     * 
     * @return <code>true</code> if {@link #sendRedirect} was called.
     */ 
    public boolean didRedirect();
    
    /**
     * Get the redirect URI.
     * 
     * @return the URI passed to {@link #sendRedirect}, or <code>null</code> if there was no redirect.
     */ 
    public String getRedirectURI();
    
    /**
     * Actually send the redirect that was suggested by {@link #sendRedirect}.
     * 
     * @throws IllegalStateException if {@link #sendRedirect} was not called.
     * @throws IOException if {@link HttpServletResponse#sendRedirect} causes an IOException.
     */ 
    public void applyRedirect() throws IOException;
}
