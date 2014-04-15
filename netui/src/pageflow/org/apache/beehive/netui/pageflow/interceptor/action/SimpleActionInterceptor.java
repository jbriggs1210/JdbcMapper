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
package org.apache.beehive.netui.pageflow.interceptor.action;

import org.apache.beehive.netui.pageflow.interceptor.InterceptorException;
import org.apache.beehive.netui.pageflow.interceptor.InterceptorChain;

import java.net.URI;
import java.net.URISyntaxException;

class SimpleActionInterceptor
        extends ActionInterceptor
{
    public SimpleActionInterceptor( SimpleActionInterceptorConfig config )
    {
        init( config );
    }

    public void preAction( ActionInterceptorContext context, InterceptorChain chain )
            throws InterceptorException
    {
        if ( ! getActionInterceptorConfig().isAfterAction() ) doit( context, chain );
    }

    public void postAction( ActionInterceptorContext context, InterceptorChain chain )
            throws InterceptorException
    {
        if ( getActionInterceptorConfig().isAfterAction() ) doit( context, chain );
    }
    
    private void doit( ActionInterceptorContext context, InterceptorChain chain )
        throws InterceptorException
    {
        try
        {
            String path = getActionInterceptorConfig().getPath();
            setOverrideForward( new InterceptorForward( new URI( path ) ), context );
            chain.continueChain();
        }
        catch ( URISyntaxException e )
        {
            throw new InterceptorException( e );
        }
    }

    public void afterNestedIntercept( AfterNestedInterceptContext context ) throws InterceptorException
    {
    }
    
    public SimpleActionInterceptorConfig getActionInterceptorConfig()
    {
        return ( SimpleActionInterceptorConfig ) super.getConfig();
    }
}
