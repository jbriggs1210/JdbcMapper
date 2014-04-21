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
package org.apache.beehive.netui.pageflow.handler;

import javax.servlet.ServletContext;


/**
 * Base handler interface.
 */
public interface Handler
{
    /**
     * Initialize.
     * 
     * @param handlerConfig the configuration object for this Handler.
     * @param previousHandler the previously-registered Handler, which this one can adapt.
     * @param servletContext the ServletContext for the webapp that is creating this object.
     */
    public void init( HandlerConfig handlerConfig, Handler previousHandler, ServletContext servletContext );
    
    /**
     * Reinitialize, normally used to reconsitute transient data that was lost during serialization.
     * 
     * @param servletContext the ServletContext for the webapp that is reinitializing this object.
     */
    public void reinit( ServletContext servletContext );
}
